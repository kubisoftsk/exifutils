# rename

Renames media files based on their EXIF creation date.

## Usage

```bash
exifutils rename [OPTIONS] <FILE|DIR>...
```

## Options

| Option | Short | Description |
|--------|-------|-------------|
| `--output-dir` | `-o` | Output directory for renamed files (flat structure, no subdirectories) |
| `--write-date` | `-w` | Write analyzed date to file metadata |
| `--zone-id` | `-z` | Timezone ID for date writing (requires `-w`) |
| `--force-field` | `-F` | Force date extraction from specific EXIF field |

## Examples

### Basic renaming

```bash
exifutils rename /path/to/photos/
```

Renames files in place using the default pattern `IMG_${date,yyyyMMdd}_${date,HHmmss}`.

Example: `DSC_1234.jpg` becomes `IMG_20240715_143045.jpg`

### Rename to output directory

```bash
exifutils rename -o /output /input/with/subdirs/
```

Renames files and places them in a flat output directory (subdirectory structure is not preserved).

### Write date to metadata

```bash
exifutils rename -w /path/to/photos/
```

Renames files and writes the analyzed date back to their metadata.

### Write date with specific timezone

```bash
exifutils rename -w -z Europe/Paris /path/to/photos/
```

Renames files and writes date with explicit timezone to metadata.

### Force specific date field

```bash
exifutils rename -F FileModifyDate /path/to/old-videos/
```

Uses filesystem modification date for renaming. Useful for old files without EXIF data.

## Configuration

The rename pattern can be customized in `application.conf`:

```hocon
rename {
  pattern = "${date,yyyy}-${date,MM}-${date,dd}_${date,HHmmss}"
}
```

See [Pattern Syntax](../patterns.md) for pattern format details.

## Conflict Handling

When multiple files would result in the same name, a numeric suffix is added:

```
IMG_20240715_143045.jpg
IMG_20240715_143045_1.jpg
IMG_20240715_143045_2.jpg
```
