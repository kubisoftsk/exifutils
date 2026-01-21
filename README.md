# exifutils

A command-line tool for organizing and renaming media files by their EXIF metadata.

## Features

- **Sort** photos and videos into date-based folder structures
- **Rename** files based on creation date
- **Set/fix dates** from filenames or manual input
- **Shift dates** to correct camera clock errors
- **Manage GPS** coordinates (add or remove)
- Cross-platform: Linux, Windows, macOS
- Native binaries (no Java required at runtime)

## Quick Start

### Prerequisites

Install [exiftool](https://exiftool.org/):

```bash
# Ubuntu/Debian
sudo apt install libimage-exiftool-perl

# macOS
brew install exiftool
```

### Install

Download native binary from [Releases](../../releases) or build from source.

```bash
# Linux
unzip exifutils-linux-native.zip
chmod +x exifutils
sudo mv exifutils /usr/local/bin/
```

See [Installation Guide](docs/installation.md) for detailed instructions.

### Basic Usage

```bash
# Sort photos into year/month folders
exifutils sort -o ~/Pictures/sorted ~/Pictures/unsorted

# Rename files by date (IMG_20240715_143045.jpg)
exifutils rename ~/Pictures/folder

# View file metadata
exifutils info photo.jpg

# Fix dates from WhatsApp filenames
exifutils set-date ~/Pictures/whatsapp-photos/
```

## Documentation

- [Installation](docs/installation.md)
- [Configuration](docs/configuration.md)
- [Pattern Syntax](docs/patterns.md)

### Commands

- [info](docs/commands/info.md) - Display metadata
- [sort](docs/commands/sort.md) - Organize into folders
- [rename](docs/commands/rename.md) - Rename by date
- [set-date](docs/commands/set-date.md) - Set EXIF date
- [shift-date](docs/commands/shift-date.md) - Shift date by duration
- [set-gps](docs/commands/set-gps.md) - Manage GPS data
- [dedupe](docs/commands/dedupe.md) - Find duplicates

## Configuration

Works out of the box. Optional config file for customization:

| Platform | Path |
|----------|------|
| Linux | `~/.config/exifutils/application.conf` |
| Windows | `%APPDATA%\exifutils\application.conf` |
| macOS | `~/Library/Application Support/exifutils/application.conf` |

See [Configuration Guide](docs/configuration.md) for details.

## License

[MIT](LICENSE)
