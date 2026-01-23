# sort

Sorts media files into folders based on their EXIF creation date.

## Usage

```bash
exifutils sort [OPTIONS] <FILE|DIR>...
```

## Options

| Option | Short | Description |
|--------|-------|-------------|
| `--outputDir` | `-o` | Root output directory for sorted files. Optional if `sort.destination` is configured |
| `--pattern` | `-p` | Custom folder pattern using `${date,FORMAT}` syntax |
| `--rename` | `-r` | Rename files according to their creation date |
| `--write-date` | `-w` | Write analyzed date to file metadata |
| `--copy` | `-c` | Copy files instead of moving them |
| `--force-field` | `-F` | Force date extraction from specific EXIF field |
| `--order` | `-O` | Input file ordering: `name`, `last-modified`, `created`. Overrides config |

## Examples

### Basic sorting

```bash
exifutils sort -o /output/folder /input/folder
```

Sorts files into year/month folders (default pattern `${date,yyyy}/${date,MM}`).

Result:
```
/output/folder/
  2024/
    01/
      photo1.jpg
    07/
      photo2.jpg
```

### Custom folder pattern

```bash
exifutils sort -o /output -p '${date,yyyy}/${date,MM}/${date,dd}' /input
```

Sorts into year/month/day structure.

See [Pattern Syntax](../patterns.md) for more pattern examples.

### Sort and rename files

```bash
exifutils sort -o /output -r /input
```

Sorts files into folders and renames them based on the `rename.pattern` setting.

### Copy instead of move

```bash
exifutils sort -o /output -c /input
```

Copies files to the output directory, leaving originals in place. Useful for testing patterns before committing to a folder structure.

### Write date to metadata

```bash
exifutils sort -o /output -w /input
```

Sorts files and writes the analyzed date back to file metadata. Useful when files have dates that were inferred from filenames or other sources.

### Force specific date field

```bash
exifutils sort -o /output -F FileModifyDate /input
```

Uses filesystem modification date instead of EXIF date. Useful for old video files without proper metadata.

## Configuration

Default output directory can be configured:

```hocon
sort {
  destination = "/home/user/Pictures/sorted"
}
```

When configured, the `-o` option becomes optional:

```bash
exifutils sort /input  # Uses sort.destination from config
```
