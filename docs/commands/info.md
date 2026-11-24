# info

Prints extracted metadata information for given files.

## Usage

```bash
exifutils info [OPTIONS] <FILE|DIR>...
```

## Options

| Option | Short | Description |
|--------|-------|-------------|
| `--all` | `-a` | Print all available metadata information |
| `--include` | `-i` | Only include files matching PATTERN (glob or `regex:` prefix). Repeatable |
| `--exclude` | `-x` | Exclude files matching PATTERN (glob or `regex:` prefix). Applied after `--include`. Repeatable |

See [File Filtering](../filtering.md) for more pattern examples.

## Examples

### Basic usage

```bash
exifutils info photo.jpg
```

Output shows the extracted creation date and basic metadata.

### Show all metadata fields

```bash
exifutils info -a photo.jpg
```

Lists all available EXIF fields. Useful for finding the correct field name when using `--force-field` with other commands.

### Process multiple files

```bash
exifutils info /path/to/photos/
```

Recursively processes all media files in the directory.

### Find available fields for force-field option

```bash
exifutils info -a video.mp4 | grep -i date
```

Shows all date-related fields available in the file.
