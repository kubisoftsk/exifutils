#!/usr/bin/env python3
"""Release plan from conventional commits: next version, warnings and release notes.

Only reads the git history, changes nothing. No dependencies besides git.

usage: release_plan.py [--version X.Y.Z] [--notes FILE] [--summary FILE] [--output FILE]
"""
import argparse
import re
import subprocess
import sys
from dataclasses import dataclass, field

REPOSITORY = "https://github.com/kubisoftsk/exifutils"
INITIAL_VERSION = (0, 9, 0)

TYPES = "feat|fix|perf|revert|docs|refactor|test|build|ci|chore|style"
SUBJECT = re.compile(rf"^(?P<type>{TYPES})(?:\((?P<scope>[^)]+)\))?(?P<bang>!)?: (?P<text>.+)$")
# Body of a squash merge contains messages of the original commits as a list
SQUASHED_BREAKING = re.compile(rf"^\* (?:{TYPES})(?:\([^)]+\))?!: (?P<text>.+)$", re.MULTILINE)
BREAKING_FOOTER = re.compile(r"^BREAKING[ -]CHANGE: (?P<text>.+)$", re.MULTILINE)
VERSION = re.compile(r"^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)$")

GROUPS = [
    ("Features", ("feat",)),
    ("Bug fixes", ("fix",)),
    ("Performance", ("perf",)),
    ("Reverted", ("revert",)),
    ("Documentation", ("docs",)),
    ("Internal changes", ("refactor", "test", "build", "ci", "chore", "style")),
]
# Changes that affect users of the application
RELEASABLE = {"feat", "fix", "perf", "revert"}


@dataclass
class Commit:
    sha: str
    subject: str
    body: str = ""
    type: str | None = None
    scope: str | None = None
    text: str = ""
    breaking: list[str] = field(default_factory=list)

    def __post_init__(self):
        match = SUBJECT.match(self.subject)
        self.text = self.subject
        bang = False
        if match:
            self.type, self.scope, self.text = match["type"], match["scope"], match["text"]
            bang = bool(match["bang"])
        self.breaking = [m["text"].strip() for m in BREAKING_FOOTER.finditer(self.body)]
        self.breaking += [m["text"].strip() for m in SQUASHED_BREAKING.finditer(self.body)]
        if bang and not self.breaking:
            self.breaking = [self.text]


def parse_version(text):
    match = VERSION.match(text)
    if not match:
        raise ValueError(f"Invalid version '{text}', expected X.Y.Z")
    return tuple(int(part) for part in match.groups())


def format_version(version):
    return ".".join(str(part) for part in version)


def bump_kind(commits):
    if any(commit.breaking for commit in commits):
        return "major"
    if any(commit.type == "feat" for commit in commits):
        return "minor"
    return "patch"


def next_version(last, commits):
    """Before 1.0.0 a breaking change bumps minor; 1.0.0 is released only by giving the version by hand."""
    if last is None:
        return INITIAL_VERSION
    major, minor, patch = last
    kind = bump_kind(commits)
    if kind == "major" and major > 0:
        return major + 1, 0, 0
    if kind in ("major", "minor"):
        return major, minor + 1, 0
    return major, minor, patch + 1


def link_issues(text):
    return re.sub(r"\(#(\d+)\)", rf"([#\1]({REPOSITORY}/issues/\1))", text)


def capitalize(text):
    return text[:1].upper() + text[1:]


def line(commit):
    scope = f"**{commit.scope}:** " if commit.scope else ""
    return f"- {scope}{link_issues(capitalize(commit.text))} ({commit.sha[:7]})"


def release_notes(commits):
    parts = []
    breaking = [f"- {link_issues(capitalize(text))} ({c.sha[:7]})" for c in commits for text in c.breaking]
    if breaking:
        parts.append("### Breaking changes\n\n" + "\n".join(breaking))
    for title, types in GROUPS:
        lines = [line(c) for c in commits if c.type in types]
        if lines:
            parts.append(f"### {title}\n\n" + "\n".join(lines))
    other = [line(c) for c in commits if c.type is None]
    if other:
        parts.append("### Other changes\n\n" + "\n".join(other))
    return "\n\n".join(parts) + "\n"


def resolve(last, commits, requested=None):
    """Returns (version, suggested version, warnings), raises ValueError when the release cannot be made."""
    if not commits:
        raise ValueError("There are no commits since the last release")
    suggested = next_version(last, commits)
    warnings = []
    version = suggested
    if requested:
        version = parse_version(requested)
        if last and version <= last:
            raise ValueError(f"Version {requested} is not higher than the last release {format_version(last)}")
        if version < suggested:
            warnings.append(f"Requested {requested} is lower than {format_version(suggested)} suggested by the commits")
    if not any(c.type in RELEASABLE or c.breaking for c in commits):
        warnings.append("No feature, fix or breaking change: users will not notice this release")
    unconventional = sum(c.type is None for c in commits)
    if unconventional:
        warnings.append(f"{unconventional} commit(s) are not conventional and were not used for the version, "
                        "check 'Other changes'")
    return version, suggested, warnings


def git(*args):
    return subprocess.run(["git", *args], check=True, capture_output=True, text=True).stdout


def last_release():
    versions = []
    for tag in git("tag", "--list", "v*", "--merged", "HEAD").split():
        if VERSION.match(tag[1:]):
            versions.append(parse_version(tag[1:]))
    return max(versions, default=None)


def read_commits(last):
    revisions = f"v{format_version(last)}..HEAD" if last else "HEAD"
    output = git("log", "--no-merges", "--reverse", "--format=%H%x1f%s%x1f%b%x1e", revisions)
    return [Commit(*record.strip("\n").split("\x1f")) for record in output.split("\x1e") if record.strip()]


def summary(last, commits, version, suggested, warnings, notes):
    base = f"v{format_version(last)}" if last else "the beginning"
    if last is None:
        reason = "first release"
    else:
        reason = {"major": "breaking change", "minor": "new feature", "patch": "no feature or breaking change"}[
            bump_kind(commits)]
    rows = [
        f"# Release plan: {format_version(version)}",
        "",
        "| | |",
        "|---|---|",
        f"| Last release | {format_version(last) if last else 'none'} |",
        f"| Suggested version | {format_version(suggested)} ({reason}) |",
        f"| Version of this plan | **{format_version(version)}** |",
        f"| Commits since {base} | {len(commits)} |",
        f"| Breaking changes | {sum(len(c.breaking) for c in commits)} |",
    ]
    if last:
        rows.append(f"| Diff | {REPOSITORY}/compare/{base}...{commits[-1].sha[:7]} |")
    rows += [""] + [f"> [!WARNING]\n> {warning}\n" for warning in warnings]
    return "\n".join(rows) + "\n## Release notes\n\n" + notes


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--version", help="use this version instead of the suggested one")
    parser.add_argument("--notes", help="file for the release notes")
    parser.add_argument("--summary", help="file the summary is appended to, default is stdout")
    parser.add_argument("--output", help="file the outputs (version=X.Y.Z) are appended to")
    args = parser.parse_args()

    last = last_release()
    commits = read_commits(last)
    try:
        version, suggested, warnings = resolve(last, commits, args.version)
    except ValueError as e:
        print(f"::error::{e}" if args.output else f"Error: {e}", file=sys.stderr)
        return 1

    notes = release_notes(commits)
    text = summary(last, commits, version, suggested, warnings, notes)
    if args.notes:
        with open(args.notes, "w", encoding="utf-8") as f:
            f.write(notes)
    if args.summary:
        with open(args.summary, "a", encoding="utf-8") as f:
            f.write(text)
    else:
        print(text)
    if args.output:
        with open(args.output, "a", encoding="utf-8") as f:
            f.write(f"version={format_version(version)}\n")
    return 0


if __name__ == "__main__":
    sys.exit(main())
