# set-date

Sets the creation date in EXIF metadata of media files. Supports multiple modes for different use cases.

## Usage

```bash
exifutils set-date [OPTIONS] <FILE|DIR>...
```

## Options

| Option | Short | Description |
|--------|-------|-------------|
| `--date-time` | `-d` | Manually set local date/time (format: `yyyy-MM-dd HH:mm:ss`) |
| `--zone-id` | `-z` | Timezone ID (e.g., `Europe/Paris`). Falls back to config default |
| `--pattern` | `-p` | Parse date from filename using DateTimeFormatter pattern |
| `--fix-zone` | `-f` | Fix timezone using existing EXIF local date/time |
| `--force-field` | `-F` | Force date extraction from specific EXIF field |
| `--rename` | `-r` | Rename files after setting date |
| `--unknown` | `-u` | Only process files with unknown/missing dates |

## Modes

### Mode 1: Automatic filename parsing (default)

When no options are specified, attempts to parse date from common filename patterns.

```bash
exifutils set-date /path/to/photos/
```

Works with filenames like:
- `IMG_20230815_143022.jpg`
- `Screenshot_2023-08-15-14-30-22.png`
- `VID_20230815_143022.mp4`

**Use case:** Files from messaging apps (WhatsApp, Telegram) that strip EXIF but preserve date in filename.

### Mode 2: Custom pattern parsing

Parse dates from filenames with non-standard naming conventions.

```bash
# Filenames like "vacation-2023-08-15-photo.jpg"
exifutils set-date -p "yyyy-MM-dd" /path/to/photos/

# Filenames like "20230815_143022_HDR.jpg"
exifutils set-date -p "yyyyMMdd_HHmmss" /path/to/photos/
```

The pattern uses [Java DateTimeFormatter](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/format/DateTimeFormatter.html) syntax.

### Mode 3: Manual date/time

Set a specific date and time for all files.

```bash
# Single file
exifutils set-date -d "2023-08-15 14:30:00" photo.jpg

# With timezone
exifutils set-date -d "2023-08-15 14:30:00" -z America/New_York photo.jpg

# Multiple files (each gets +1 second to avoid conflicts)
exifutils set-date -d "2023-08-15 14:30:00" /path/to/photos/
```

**Use case:** Scanned photos, screenshots, or files where you know the exact date.

### Mode 4: Fix timezone

Fixes timezone for files with correct local time but wrong/missing timezone offset.

```bash
# Fix timezone to Europe/Athens for vacation photos
exifutils set-date -f -z Europe/Athens /path/to/greece-vacation/

# Use default timezone from config
exifutils set-date -f /path/to/photos/
```

This keeps the local time unchanged and only updates the timezone offset.

**Use cases:**
- Camera recorded correct local time but stored wrong timezone (e.g., UTC instead of local)
- Videos from phones that don't store timezone in metadata
- Photos taken abroad where camera timezone wasn't updated

## Additional Options

### Set date and rename

```bash
exifutils set-date -d "2023-08-15 14:30:00" -r /path/to/photos/
```

### Only process files without dates

```bash
exifutils set-date -u /path/to/photos/
```

Skips files that already have EXIF date set.

### With forced field extraction

```bash
exifutils set-date -f -F CreateDate -z Europe/Paris /path/to/photos/
```

## Mutual Exclusivity

These options cannot be combined:
- `--fix-zone` with `--date-time` (both provide source of local date/time)
- `--fix-zone` with `--pattern` (both provide source of local date/time)
- `--fix-zone` with `--unknown` (fix-zone needs existing EXIF date)

## Timezone Behavior

All modes support `--zone-id`. If not specified, uses default from configuration:

```hocon
dateTime {
  timeZone = "Europe/Bratislava"
}
```

The timezone affects:
- The offset stored in EXIF metadata
- How UTC time is calculated from local time
