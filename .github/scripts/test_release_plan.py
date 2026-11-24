import os
import subprocess
import sys
import tempfile
import unittest

import release_plan as rp


def commit(subject, body="", sha="0123456789abcdef"):
    return rp.Commit(sha, subject, body)


class CommitTest(unittest.TestCase):

    def test_type_scope_and_text(self):
        c = commit("feat(sort): add copy mode (#12)")
        self.assertEqual(("feat", "sort", "add copy mode (#12)", []), (c.type, c.scope, c.text, c.breaking))

    def test_unconventional(self):
        for subject in ("Various updates", "feat add something", "Feature: x", "wip: x"):
            self.assertIsNone(commit(subject).type, subject)

    def test_breaking_by_exclamation_mark(self):
        self.assertEqual(["drop Java 17"], commit("feat!: drop Java 17").breaking)
        self.assertEqual(["drop Java 17"], commit("chore(build)!: drop Java 17").breaking)

    def test_breaking_by_footer(self):
        c = commit("feat: new exit codes", "Details.\n\nBREAKING CHANGE: answering n exits with 4")
        self.assertEqual(["answering n exits with 4"], c.breaking)
        self.assertEqual(["x"], commit("fix: y", "BREAKING-CHANGE: x").breaking)

    def test_breaking_in_squash_merge_body(self):
        c = commit("feat: filters (#36)", "* feat: one\n\n* fix(cli)!: changed exit code\n\n* docs: two")
        self.assertEqual(["changed exit code"], c.breaking)

    def test_mention_of_breaking_change_in_text_is_not_breaking(self):
        self.assertEqual([], commit("docs: explain", "This is not a BREAKING CHANGE: just text").breaking)


class NextVersionTest(unittest.TestCase):

    def check(self, expected, last, *subjects):
        self.assertEqual(expected, rp.next_version(last, [commit(s) for s in subjects]))

    def test_first_release(self):
        self.check((0, 9, 0), None, "feat!: anything")

    def test_before_one(self):
        self.check((0, 9, 1), (0, 9, 0), "fix: a", "docs: b")
        self.check((0, 10, 0), (0, 9, 3), "fix: a", "feat: b")
        self.check((0, 10, 0), (0, 9, 3), "fix!: a")

    def test_from_one(self):
        self.check((1, 2, 4), (1, 2, 3), "fix: a")
        self.check((1, 3, 0), (1, 2, 3), "feat: a", "fix: b")
        self.check((2, 0, 0), (1, 2, 3), "feat: a", "fix!: b")

    def test_unconventional_is_patch(self):
        self.check((1, 2, 4), (1, 2, 3), "Various updates")


class ResolveTest(unittest.TestCase):

    def test_requested_version(self):
        version, suggested, _ = rp.resolve((0, 9, 0), [commit("feat: a")], "1.0.0")
        self.assertEqual(((1, 0, 0), (0, 10, 0)), (version, suggested))

    def test_requested_version_lower_than_suggested_is_warning(self):
        version, _, warnings = rp.resolve((1, 2, 3), [commit("feat: a")], "1.2.4")
        self.assertEqual((1, 2, 4), version)
        self.assertIn("lower than 1.3.0", warnings[0])

    def test_rejected_versions(self):
        for requested in ("1.2.3", "1.2.2", "v1.3.0", "1.3", "1.3.0-rc1", "01.3.0", "1.3.0; rm -rf /"):
            with self.assertRaises(ValueError, msg=requested):
                rp.resolve((1, 2, 3), [commit("feat: a")], requested)

    def test_no_commits(self):
        with self.assertRaises(ValueError):
            rp.resolve((1, 2, 3), [])

    def test_warnings(self):
        _, _, warnings = rp.resolve((1, 2, 3), [commit("docs: a"), commit("Various updates")])
        self.assertEqual(2, len(warnings))
        self.assertEqual([], rp.resolve((1, 2, 3), [commit("fix: a")])[2])


class NotesTest(unittest.TestCase):

    def test_groups_and_links(self):
        notes = rp.release_notes([
            commit("fix: second (#7)"), commit("feat(sort): first"), commit("ci: x"), commit("Old message"),
            commit("feat!: third"),
        ])
        order = [notes.index(title) for title in
                 ("### Breaking changes", "### Features", "### Bug fixes", "### Internal changes", "### Other changes")]
        self.assertEqual(sorted(order), order)
        self.assertIn("- **sort:** First (0123456)", notes)
        self.assertIn(f"Second ([#7]({rp.REPOSITORY}/issues/7))", notes)


class GitTest(unittest.TestCase):
    """Whole script on a real repository."""

    def setUp(self):
        self.dir = tempfile.TemporaryDirectory()
        self.addCleanup(self.dir.cleanup)
        self.git("init", "-q", "-b", "main")

    def git(self, *args):
        config = ["-c", "user.name=test", "-c", "user.email=test@example.com", "-c", "commit.gpgsign=false",
                  "-c", "tag.gpgsign=false"]
        subprocess.run(["git", *config, *args], cwd=self.dir.name, check=True, capture_output=True)

    def commit(self, message):
        self.git("commit", "-q", "--allow-empty", "-m", message)

    def plan(self, *args):
        output = os.path.join(self.dir.name, "output")
        if os.path.exists(output):
            os.remove(output)
        script = os.path.abspath(rp.__file__)
        result = subprocess.run([sys.executable, script, "--output", output, *args], cwd=self.dir.name,
                                capture_output=True, text=True)
        version = None
        if os.path.exists(output):
            with open(output) as f:
                version = f.read().strip()
        return result.returncode, version, result.stdout + result.stderr

    def test_releases(self):
        self.commit("feat: a")
        self.assertEqual((0, "version=0.9.0"), self.plan()[:2])
        self.git("tag", "v0.9.0")
        code, version, text = self.plan()
        self.assertEqual((1, None), (code, version))
        self.assertIn("no commits", text)
        self.commit("fix: b")
        self.assertEqual((0, "version=0.9.1"), self.plan()[:2])
        self.git("tag", "v0.9.1")
        self.git("tag", "v0.10.0")
        self.git("tag", "vnext")
        self.commit("feat: c\n\nBREAKING CHANGE: d")
        code, version, text = self.plan()
        self.assertEqual((0, "version=0.11.0"), (code, version))
        self.assertIn("| Commits since v0.10.0 | 1 |", text)
        self.assertEqual((1, None), self.plan("--version", "0.10.0")[:2])
        self.assertEqual((0, "version=1.0.0"), self.plan("--version", "1.0.0")[:2])

    def test_tag_of_other_branch_is_ignored(self):
        self.commit("feat: a")
        self.git("tag", "v0.9.0")
        self.git("checkout", "-q", "-b", "other")
        self.commit("feat: b")
        self.git("tag", "v5.0.0")
        self.git("checkout", "-q", "main")
        self.commit("fix: c")
        self.assertEqual((0, "version=0.9.1"), self.plan()[:2])


if __name__ == "__main__":
    unittest.main()
