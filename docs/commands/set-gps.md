# set-gps

Sets or removes GPS coordinates in the EXIF metadata of files.

## Usage

```bash
exifutils set-gps [OPTIONS] <FILE|DIR>...
```

## Options

| Option | Short | Description |
|--------|-------|-------------|
| `--coordinates` | `-c` | GPS coordinates in `latitude,longitude` format |
| `--delete` | `-d` | Delete GPS information from files |

## Coordinate Format

Coordinates use decimal degrees format: `latitude,longitude`

- Latitude: Positive = North, Negative = South
- Longitude: Positive = East, Negative = West

### Examples

| Location | Coordinates |
|----------|-------------|
| Paris (Eiffel Tower) | `48.8584,2.2945` |
| New York (Times Square) | `40.7580,-73.9855` |
| Sydney (Opera House) | `-33.8568,151.2153` |
| Tokyo (Shibuya) | `35.6595,139.7004` |

## Examples

### Set GPS coordinates

```bash
exifutils set-gps -c "48.8584,2.2945" /path/to/photos/
```

Sets GPS location to Eiffel Tower for all files.

### Delete GPS data

```bash
exifutils set-gps -d /path/to/photos/
```

Removes all GPS metadata from files. Useful for privacy before sharing photos.

### Single file

```bash
exifutils set-gps -c "40.7580,-73.9855" vacation-photo.jpg
```

## Use Cases

1. **Geotagging old photos:** Add location data to photos from cameras without GPS.

2. **Privacy protection:** Remove GPS data before sharing photos online.

3. **Correcting wrong location:** Replace incorrect GPS coordinates.

## Notes

- Only one of `--coordinates` or `--delete` can be used at a time
- GPS data is written to standard EXIF GPS tags
- Original files are modified in place (no backup created)
