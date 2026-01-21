# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- `--force-field` (`-F`) option for overriding date extraction source in `rename`, `sort`, `set-date`, `shift-date` commands
- `--fix-zone` (`-f`) flag for `set-date` command to fix timezone while preserving local time
- Custom pattern parsing (`-p`) for `set-date` command
- Default sort destination setting (`sort.destination`)
- Custom sorting pattern option (`sort.pattern`)

### Changed
- Documentation restructured into `docs/` directory

## [1.0.0] - TBD

### Added
- Initial release
- `info` command - Display EXIF metadata
- `sort` command - Organize files into date-based folder structure
- `rename` command - Rename files based on EXIF date
- `set-date` command - Set EXIF date from filename or manual input
- `shift-date` command - Shift EXIF date by duration
- `set-gps` command - Set or remove GPS coordinates
- `dedupe` command - Find duplicate files
- HOCON configuration support
- Cross-platform support (Linux, Windows, macOS)
- GraalVM native image support
