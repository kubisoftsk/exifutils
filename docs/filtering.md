# File Filtering

Every command that walks files (`sort`, `rename`, `set-date`, `shift-date`, `info`, `set-gps`) supports `--include`/`-i` and `--exclude`/`-x` to filter which files are processed by filename, without having to move files out of the input folder first.

## Basic Syntax

```bash
exifutils sort -o /output -x 'IMG-*-WA*.jpg' -x 'VID-*-WA*.mp4' -x 'Screenshot_*.png' /input
```

- Both options are **repeatable** — pass the flag multiple times to add more patterns.
- Pattern syntax is **glob by default** (Ant-style), or a **regex** via a `regex:` prefix. Both are backed by Java's [`FileSystem.getPathMatcher`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)):
  - `-x 'IMG-*-WA*.jpg'` — glob syntax.
  - `-x 'regex:^IMG-\d{8}-WA\d{4}\.jpg$'` — regex syntax. The regex has to match the whole filename, so use `regex:.*WA.*` to match a part of it.
- **Quote the patterns.** Without quotes the shell expands `*.jpg` itself before exifutils sees it.

### Glob Syntax Cheatsheet

| Symbol | Meaning | Example |
|--------|---------|---------|
| `*` | Any number of characters | `IMG_*.jpg` matches `IMG_0001.jpg` |
| `**` | Same as `*`, since only the filename is matched | |
| `?` | Exactly one character | `IMG_000?.jpg` matches `IMG_0001.jpg` |
| `[abc]` | One character from the set | `IMG_000[12].jpg` |
| `{a,b}` | One of the alternatives | `*.{jpg,png}` |

## Matching

- Matching is always against the **filename only** — never the full path. Patterns containing a directory separator (e.g. `thumbs/*.jpg`) never match anything.
- **Case sensitivity** follows the platform: patterns are case-sensitive on Linux and macOS (`*.jpg` does not match `IMG_0001.JPG`) and case-insensitive on Windows. To ignore case everywhere, list both variants (`*.{jpg,JPG}`) or use a regex with the `(?i)` flag (`regex:(?i).*\.jpg`).
- Filtering applies only while **walking a directory**. A file passed explicitly on the command line (e.g. `exifutils sort -o /out photo.jpg`) is always processed, regardless of `--include`/`--exclude`. This includes files expanded by the shell, so pass `/input` instead of `/input/*`.
- A pattern starting with `-` can be mistaken for an option, use the long form with `=` (e.g. `--exclude='-a*'`).

## Include and Exclude Order

`--include` narrows the set of files down to only those matching at least one include pattern. `--exclude` is then applied to whatever passed the include stage, dropping any file that matches an exclude pattern. With no `--include`, every file passes that stage.

## Examples

### Skip WhatsApp exports and screenshots

```bash
exifutils sort -o /output -x 'IMG-*-WA*.jpg' -x 'VID-*-WA*.mp4' -x 'Screenshot_*.png' /input
```

### Only process JPEGs

```bash
exifutils rename -i 'regex:(?i).*\.jpe?g' /path/to/photos/
```

### Regex exclude

```bash
exifutils info -x 'regex:IMG-\d{8}-WA\d{4}\.jpg' /path/to/photos/
```
