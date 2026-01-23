# Configuration

exifutils works out of the box with sensible defaults. Configuration is optional - only create a config file if you need to override defaults.

## Configuration File Location

| Platform | Path |
|----------|------|
| Linux | `~/.config/exifutils/application.conf` |
| Windows | `%APPDATA%\exifutils\application.conf` |
| macOS | `~/Library/Application Support/exifutils/application.conf` |

On Linux, the `XDG_CONFIG_HOME` environment variable is respected if set.

## Configuration Format

Configuration uses [HOCON format](https://github.com/lightbend/config/blob/main/HOCON.md).

A template is available at [`config/application-template.conf`](../config/application-template.conf).

## Settings Reference

All settings with their defaults:

### exifTool

| Setting | Default | Description |
|---------|---------|-------------|
| `exifTool.path` | `""` (use PATH) | Path to exiftool executable |

```hocon
exifTool {
  path = "/usr/local/bin/exiftool"
}
```

### dateTime

| Setting | Default | Description |
|---------|---------|-------------|
| `dateTime.timeZone` | System default | Default timezone for date/time operations |

Timezone uses [IANA Time Zone Database](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones) identifiers.

```hocon
dateTime {
  timeZone = "Europe/Paris"
}
```

### file

| Setting | Default | Description |
|---------|---------|-------------|
| `file.sortOrder` | `name` | Order in which input files are processed. Values: `name`, `last-modified`, `created` |

```hocon
file {
  sortOrder = "created"
}
```

This is useful when files have non-meaningful names (e.g., UUIDs) but their filesystem creation or modification dates reflect the correct order.

Can be overridden per-command with the `--order` / `-O` CLI option.

### rename

| Setting | Default | Description |
|---------|---------|-------------|
| `rename.pattern` | `IMG_${date,yyyyMMdd}_${date,HHmmss}` | Pattern for renaming files |

See [Pattern Syntax](patterns.md) for pattern format.

```hocon
rename {
  pattern = "IMG_${date,yyyyMMdd}_${date,HHmmss}"
}
```

### sort

| Setting | Default | Description |
|---------|---------|-------------|
| `sort.destination` | `""` | Default output directory (makes `-o` optional) |
| `sort.pattern` | `${date,yyyy}/${date,MM}` | Folder structure pattern |

See [Pattern Syntax](patterns.md) for pattern format.

```hocon
sort {
  destination = "/home/user/Pictures/sorted"
  pattern = "${date,yyyy}/${date,MM}/${date,dd}"
}
```

## Example Configuration

```hocon
# ~/.config/exifutils/application.conf

exifTool {
  path = "/usr/local/bin/exiftool"
}

dateTime {
  timeZone = "Europe/Bratislava"
}

file {
  sortOrder = "name"
}

rename {
  pattern = "${date,yyyy}-${date,MM}-${date,dd}_${date,HHmmss}"
}

sort {
  destination = "/media/photos/sorted"
  pattern = "${date,yyyy}/${date,MM}"
}
```

## Logging

Log files are stored in platform-specific locations:

| Platform | Path |
|----------|------|
| Linux | `~/.local/share/exifutils/logs/` |
| Windows | `%LOCALAPPDATA%\exifutils\logs\` |
| macOS | `~/Library/Logs/exifutils/` |

On Linux, `XDG_DATA_HOME` is respected if set.

Logs are written to `exifutils.log` and rolled daily or at 10MB. Old logs are compressed (`.gz`) and kept until total size exceeds 50MB.
