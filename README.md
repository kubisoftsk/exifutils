# exifutils
A simple tool for organizing and renaming a bunch of media files chronologically by exif metadata.

## Prerequisites

- [exiftool](https://exiftool.org/) must be installed and available in PATH
  ```bash
  # Ubuntu/Debian
  sudo apt install libimage-exiftool-perl
  ```

## Installation (Native Binary)

1. Go to **Actions** → **Build Native Executable** → Run the workflow
2. Download the artifact ZIP for your platform (`exifutils-linux-native` or `exifutils-windows-native`)
3. Install:

**Linux:**
```bash
unzip exifutils-linux-native.zip
chmod +x exifutils
sudo mv exifutils /usr/local/bin/
```

Or install for current user only (no sudo):
```bash
mkdir -p ~/.local/bin
mv exifutils ~/.local/bin/
# Ensure ~/.local/bin is in your PATH
```

**Update:** Simply repeat the steps above - the new binary will replace the old one.

## Configuration

The application works out of the box with built-in defaults. Configuration is optional - only create `application.conf` if you need to override defaults.

Configuration uses [HOCON format](https://github.com/lightbend/config/blob/main/HOCON.md) with layered loading.

Configuration file location:

| Platform | Path |
|----------|------|
| **Linux** | `~/.config/exifutils/application.conf` |
| **Windows** | `%APPDATA%\exifutils\application.conf` |
| **macOS** | `~/Library/Application Support/exifutils/application.conf` |

On Linux, the `XDG_CONFIG_HOME` environment variable is respected if set.

A template is available at [`config/application-template.conf`](config/application-template.conf). Example:

```hocon
exifTool {
  path = "/usr/local/bin/exiftool"
}
```

## Settings

All available settings with their defaults are defined in [`core/src/main/resources/reference.conf`](core/src/main/resources/reference.conf).

| Setting | Default | Description |
|---------|---------|-------------|
| `exifTool.path` | `""` (use PATH) | Path to the exiftool executable |
| `dateTime.timeZone` | System default | Default timezone for date/time parsing ([TZDB identifiers](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones)) |
| `rename.pattern` | `IMG_${date,yyyyMMdd}_${date,HHmmss}` | Pattern for renaming files |
| `sort.pattern` | `${date,yyyy}/${date,MM}` | Pattern for folder structure when sorting |

### Date Pattern Syntax

Both `rename.pattern` and `sort.pattern` use the `${date,FORMAT}` syntax where FORMAT follows [Java DateTimeFormatter](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/format/DateTimeFormatter.html) patterns.

**Sort pattern examples:**

| Pattern | Output | Description |
|---------|--------|-------------|
| `${date,yyyy}/${date,MM}` | `2024/07` | Year/month (default) |
| `${date,yyyy}/${date,MM}/${date,dd}` | `2024/07/15` | Year/month/day |
| `${date,yyyy}/${date,ww}` | `2024/29` | Year/week of year |
| `${date,yyyy-MM}` | `2024-07` | Flat year-month |
| `${date,yyyy}/Q${date,Q}` | `2024/Q3` | Year/quarter |

The sort pattern can be overridden via command line using `-p` or `--pattern`:

```bash
exifutils sort -o /output -p '${date,yyyy}/${date,MM}/${date,dd}' /input
```

## Logging

Log files are stored in platform-specific locations:

| Platform | Path |
|----------|------|
| **Linux** | `~/.local/share/exifutils/logs/` |
| **Windows** | `%LOCALAPPDATA%\exifutils\logs\` |
| **macOS** | `~/Library/Logs/exifutils/` |

On Linux, the `XDG_DATA_HOME` environment variable is respected if set.

Logs are written to `exifutils.log` and rolled daily or when reaching 10MB. Old logs are compressed to `.gz` and kept until total size exceeds 50MB.

## Commands

### info

Prints extracted metadata information for given files.

```bash
exifutils info photo.jpg
exifutils info /path/to/photos/
```

### sort

Sorts media files into folders based on their EXIF creation date.

```bash
# Sort files into year/month folders (default pattern)
exifutils sort -o /output/folder /input/folder

# Sort with custom pattern
exifutils sort -o /output -p '${date,yyyy}/${date,MM}/${date,dd}' /input
```

See [Date Pattern Syntax](#date-pattern-syntax) for pattern examples.

### rename

Renames media files based on their EXIF creation date.

```bash
exifutils rename /path/to/photos/
```

The rename pattern can be configured in `application.conf` using the `rename.pattern` setting.

### set-date

Sets the creation date in EXIF metadata of media files. This command supports multiple modes for different use cases.

#### Options

| Option | Short | Description |
|--------|-------|-------------|
| `--date-time` | `-d` | Manually set local date and time (format: `yyyy-MM-dd HH:mm:ss`) |
| `--zone-id` | `-z` | Set timezone ID (e.g., `Europe/Paris`). Falls back to config default if not specified |
| `--pattern` | `-p` | Parse date from filename using custom DateTimeFormatter pattern |
| `--fix-zone` | `-f` | Fix timezone using existing EXIF local date/time |
| `--rename` | `-r` | Rename files after setting date |
| `--unknown` | `-u` | Only process files with unknown/missing dates |

#### Mode 1: Automatic filename parsing (default)

When no options are specified, the command attempts to parse the date from the filename using common patterns.

```bash
# Parse date from filenames like "IMG_20230815_143022.jpg"
exifutils set-date /path/to/photos/

# With explicit timezone
exifutils set-date -z Europe/Paris /path/to/photos/
```

**Use case:** Files received via messaging apps (WhatsApp, Telegram) that strip EXIF data but preserve date in filename.

#### Mode 2: Custom pattern parsing

Use a custom DateTimeFormatter pattern to parse dates from filenames.

```bash
# Parse filenames like "vacation-2023-08-15-photo.jpg"
exifutils set-date -p "yyyy-MM-dd" /path/to/photos/

# Parse filenames like "20230815_143022_HDR.jpg"
exifutils set-date -p "yyyyMMdd_HHmmss" /path/to/photos/
```

**Use case:** Files with non-standard naming conventions.

#### Mode 3: Manual date/time

Set a specific date and time for all files. When processing multiple files, each subsequent file gets +1 second to avoid conflicts.

```bash
# Set specific date and time
exifutils set-date -d "2023-08-15 14:30:00" photo.jpg

# Set date with specific timezone
exifutils set-date -d "2023-08-15 14:30:00" -z America/New_York photo.jpg

# Set date for multiple files (each gets +1 second)
exifutils set-date -d "2023-08-15 14:30:00" /path/to/photos/
```

**Use case:** Scanned photos, screenshots, or files where you know the exact date.

#### Mode 4: Fix timezone

Fixes the timezone for files that have correct local date/time in EXIF but wrong or missing timezone. This keeps the local time unchanged and only updates the timezone offset.

```bash
# Fix timezone to Europe/Athens for vacation photos
exifutils set-date -f -z Europe/Athens /path/to/greece-vacation/

# Fix timezone using default from config
exifutils set-date -f /path/to/photos/
```

**Use case:**
- Camera recorded correct local time but stored wrong timezone (e.g., UTC instead of local)
- Videos from phones that don't store timezone in metadata
- Photos taken abroad where camera timezone wasn't updated

#### Timezone Behavior

All modes support the `--zone-id` option. If not specified, the default timezone from configuration is used:

```hocon
# In application.conf
dateTime {
  timeZone = "Europe/Bratislava"
}
```

The timezone affects:
- The offset stored in EXIF metadata
- How the UTC time is calculated from local time

#### Additional Options

```bash
# Set date and rename files afterward
exifutils set-date -d "2023-08-15 14:30:00" -r /path/to/photos/

# Only set date for files that don't have EXIF date
exifutils set-date -u /path/to/photos/
```

#### Mutual Exclusivity

The following options cannot be combined:
- `--fix-zone` with `--date-time` (both provide the source of local date/time)
- `--fix-zone` with `--pattern` (both provide the source of local date/time)
- `--fix-zone` with `--unknown` (fix-zone needs existing EXIF date)

### shift-date

Shifts the creation date of media files by a specified duration. Useful when camera date/time was set incorrectly.

```bash
# Shift date forward by 2 hours
exifutils shift-date --duration "PT2H" /path/to/photos/

# Shift date backward by 1 day
exifutils shift-date --duration "-P1D" /path/to/photos/
```

Duration format follows [ISO 8601 duration format](https://en.wikipedia.org/wiki/ISO_8601#Durations).

### set-gps

Sets GPS coordinates in the EXIF metadata of files.

```bash
# Set GPS coordinates
exifutils set-gps --coordinates "48.8584,2.2945" /path/to/photos/

# Delete GPS data
exifutils set-gps --delete /path/to/photos/
```

### dedupe

Find and remove duplicate media files.

```bash
exifutils dedupe /path/to/photos/
```

## Command Line Arguments Processing

For each command line argument:
- If the argument is a path to a directory: Add all files from the directory, recursively
- If the argument is a file: Add the file individually