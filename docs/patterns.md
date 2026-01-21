# Pattern Syntax

Several commands use pattern syntax for renaming files and creating folder structures.

## Basic Syntax

Patterns use `${date,FORMAT}` placeholders where FORMAT follows [Java DateTimeFormatter](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/format/DateTimeFormatter.html) patterns.

## Common Format Symbols

| Symbol | Meaning | Example |
|--------|---------|---------|
| `yyyy` | Year (4 digits) | 2024 |
| `yy` | Year (2 digits) | 24 |
| `MM` | Month (2 digits) | 07 |
| `MMM` | Month name (short) | Jul |
| `dd` | Day of month | 15 |
| `HH` | Hour (24-hour) | 14 |
| `mm` | Minute | 30 |
| `ss` | Second | 45 |
| `ww` | Week of year | 29 |
| `Q` | Quarter | 3 |
| `E` | Day name (short) | Mon |
| `EEEE` | Day name (full) | Monday |

## Rename Patterns

Used by the `rename` command and `rename.pattern` setting.

| Pattern | Output |
|---------|--------|
| `IMG_${date,yyyyMMdd}_${date,HHmmss}` | `IMG_20240715_143045.jpg` |
| `${date,yyyy}-${date,MM}-${date,dd}_${date,HHmmss}` | `2024-07-15_143045.jpg` |
| `${date,yyyyMMdd_HHmmss}` | `20240715_143045.jpg` |

The file extension is always preserved.

## Sort Patterns

Used by the `sort` command and `sort.pattern` setting. Creates folder hierarchies.

| Pattern | Output | Description |
|---------|--------|-------------|
| `${date,yyyy}/${date,MM}` | `2024/07/` | Year/month (default) |
| `${date,yyyy}/${date,MM}/${date,dd}` | `2024/07/15/` | Year/month/day |
| `${date,yyyy}/${date,ww}` | `2024/29/` | Year/week |
| `${date,yyyy-MM}` | `2024-07/` | Flat year-month |
| `${date,yyyy}/Q${date,Q}` | `2024/Q3/` | Year/quarter |
| `${date,yyyy}/${date,MMM}` | `2024/Jul/` | Year/month name |

## Examples

### Organize by year and month

```bash
exifutils sort -o /output -p '${date,yyyy}/${date,MM}' /input
```

Result:
```
/output/
  2024/
    01/
      photo1.jpg
    07/
      photo2.jpg
  2023/
    12/
      photo3.jpg
```

### Organize by year, month, and day

```bash
exifutils sort -o /output -p '${date,yyyy}/${date,MM}/${date,dd}' /input
```

### Rename with date prefix

Configure in `application.conf`:

```hocon
rename {
  pattern = "${date,yyyy}-${date,MM}-${date,dd}_${date,HHmmss}"
}
```

Then:

```bash
exifutils rename /path/to/photos/
```

`DSC_1234.jpg` (taken 2024-07-15 14:30:45) becomes `2024-07-15_143045.jpg`
