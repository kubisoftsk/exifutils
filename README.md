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

Notes: 
What needs to be done:
Photos: nothing needed
Videos:

- [ ] If CreationDate is not present, it should be added with time zone, e.g. 2020-01-01T00:00:00+00:00,
      add WRITE flag -w to command line to allow write of that time zone data after analysis
- if that creationdate with zone is present, no need to analyse the gps - i think this is already implemented
- add --shift flag to set-date command that allows shifting the date by a certain amount of time and to fix time zone data 
 because some files may be incorrectly tagged with the wrong time zone, its probably rate tough 

Onplus newest does not add location tag to video

## Command Line Arguments Processing

For each command line argument:
    - If the argument is a path to a directory:
        - Add all files from the directory, recursively
    - Else:
        - Add the file individually and continue