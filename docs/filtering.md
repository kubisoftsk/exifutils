# File Filtering

Every command that walks files (`sort`, `rename`, `set-date`, `shift-date`, `info`, `set-gps`) supports `--include`/`-i` and `--exclude`/`-x` to filter which files are processed by filename, without having to move files out of the input folder first.

## Basic Syntax

```bash
exifutils sort -o /output -x 'IMG-*-WA*.jpg' -x 'VID-*-WA*.mp4' -x 'Screenshot_*.png' /input
```

- Both options are **repeatable** — pass the flag multiple times to add more patterns.
- Pattern syntax is **glob by default** (with an optional `glob:` prefix), or a **regex** via a `regex:` prefix:
  - `-x 'IMG-*-WA*.jpg'` or `-x 'glob:IMG-*-WA*.jpg'` — glob syntax, as described for Java's [`FileSystem.getPathMatcher`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)).
  - `-x 'regex:^IMG-\d{8}-WA\d{4}\.jpg$'` — [Java regex](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/regex/Pattern.html) syntax. The regex has to match the whole filename, so use `regex:.*WA.*` to match a part of it.
- **Quote the patterns.** Without quotes the shell expands `*.jpg` itself before exifutils sees it.
- A pattern starting with `-` can be mistaken for an option, use the long form with `=` (e.g. `--exclude='-a*'`).

### Glob Syntax Cheatsheet

| Symbol | Meaning | Example |
|--------|---------|---------|
| `*` | Any number of characters | `IMG_*.jpg` matches `IMG_0001.jpg` |
| `**` | Same as `*`, since only the filename is matched | |
| `?` | Exactly one character | `IMG_000?.jpg` matches `IMG_0001.jpg` |
| `[abc]` | One character from the set | `IMG_000[12].jpg` |
| `[a-c]` | One character from the range | `IMG_000[1-5].jpg` |
| `[!abc]` | One character not in the set | `IMG_000[!12].jpg` |
| `{a,b}` | One of the alternatives | `*.{jpg,png}` |
| `\` | Escapes the next character | `\[a\].jpg` matches `[a].jpg` |

## Matching

- Matching is always against the **filename only** — never the full path.
- Matching **ignores case** on all platforms, for globs and regexes, including letters with diacritics: `*.jpg` matches `IMG_0001.JPG`. To match case, use a regex with the `(?-i)` flag (e.g. `regex:(?-i).*\.jpg`).
- Filters apply to all files in the same way, whether they were found in a directory or given on the command line. So `-x 'Screenshot_*' /input/*` (expanded by the shell) also skips the screenshots. Excluding an explicitly given file is not an error, but the file has to exist.
- Filters do not apply to directories. `-i '*.jpg' /input` processes the JPEGs in `/input`, and an excluded name never skips a directory.

## Include and Exclude Order

A file is processed if it matches at least one `--include` pattern (or there is no `--include`) and matches no `--exclude` pattern. `--include` is always evaluated first and `--exclude` is then applied to what is left, so **exclude wins** if a file matches both. The order of the options on the command line does not matter: `-i '*.jpg' -x '*-WA*'` and `-x '*-WA*' -i '*.jpg'` are the same.

## Invalid Patterns

Patterns that cannot match any filename are rejected before any file is searched, with exit code `2`:

- an empty or blank pattern
- a pattern containing a path separator (e.g. `thumbs/*.jpg`). On Windows, this includes a backslash in a glob, unless it escapes one of `[ ] { } ,`
- an invalid glob or regex (e.g. `[` or `regex:(`)

## Examples

### Skip WhatsApp exports and screenshots

```bash
exifutils sort -o /output -x 'IMG-*-WA*.jpg' -x 'VID-*-WA*.mp4' -x 'Screenshot_*.png' /input
```

### Only process JPEGs

```bash
exifutils rename -i '*.jpg' -i '*.jpeg' /path/to/photos/
```

### Regex exclude

```bash
exifutils info -x 'regex:IMG-\d{8}-WA\d{4}\.jpg' /path/to/photos/
```
