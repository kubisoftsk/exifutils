# shift-date

Shifts the creation date of media files by a specified duration. Useful when camera date/time was set incorrectly.

## Usage

```bash
exifutils shift-date [OPTIONS] <FILE|DIR>...
```

## Options

| Option | Short | Description |
|--------|-------|-------------|
| `--duration` | `-d` | Duration to shift (required). Format: ISO 8601 |
| `--rename` | `-r` | Rename files after shifting date |
| `--force-field` | `-F` | Force date extraction from specific EXIF field |
| `--order` | `-O` | Input file ordering: `name`, `last-modified`, `created`. Overrides config |

## Duration Format

Uses [ISO 8601 duration format](https://en.wikipedia.org/wiki/ISO_8601#Durations):

```
PnDTnHnMnS
```

- `P` - Period marker (required)
- `nD` - Days
- `T` - Time marker (required if specifying hours/minutes/seconds)
- `nH` - Hours
- `nM` - Minutes
- `nS` - Seconds

Use `-` prefix for negative (backward) shift.

### Examples

| Duration | Meaning |
|----------|---------|
| `PT2H` | Forward 2 hours |
| `PT30M` | Forward 30 minutes |
| `PT1H30M` | Forward 1 hour 30 minutes |
| `-PT2H` | Backward 2 hours |
| `P1D` | Forward 1 day |
| `-P1D` | Backward 1 day |
| `P1DT12H` | Forward 1 day and 12 hours |

## Examples

### Shift forward by 2 hours

```bash
exifutils shift-date -d "PT2H" /path/to/photos/
```

Camera was set 2 hours behind actual time.

### Shift backward by 1 day

```bash
exifutils shift-date -d "-P1D" /path/to/photos/
```

Camera date was 1 day ahead.

### Shift and rename

```bash
exifutils shift-date -d "PT2H" -r /path/to/photos/
```

Shifts date and renames files based on the new date.

### Force specific date field

```bash
exifutils shift-date -d "PT6H" -F CreateDate /path/to/videos/
```

Uses CreateDate field as source for shift calculation.

## Use Cases

1. **Camera timezone was wrong:** You took photos abroad but forgot to change camera timezone. Shift by the timezone difference.

2. **Camera clock drift:** Camera clock slowly drifted over time. Shift to correct.

3. **Batch correction:** All photos from an event have wrong time due to camera misconfiguration.
