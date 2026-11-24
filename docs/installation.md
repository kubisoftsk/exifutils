# Installation

## Prerequisites

[exiftool](https://exiftool.org/) must be installed and available in PATH.

### Linux (Ubuntu/Debian)

```bash
sudo apt install libimage-exiftool-perl
```

### macOS

```bash
brew install exiftool
```

### Windows

Download from [exiftool.org](https://exiftool.org/) and add to PATH.

## Installing exifutils

### Option 1: Native Binary (Recommended)

Native binaries are standalone executables that don't require Java. Download from [Releases](https://github.com/kubisoftsk/exifutils/releases):

- `exifutils-linux-x64` for Linux
- `exifutils-windows-x64.exe` for Windows

`SHA256SUMS` contains checksums of all files.

#### Linux Installation

**System-wide (requires sudo):**

```bash
chmod +x exifutils-linux-x64
sudo mv exifutils-linux-x64 /usr/local/bin/exifutils
```

**User-only (no sudo):**

```bash
chmod +x exifutils-linux-x64
mkdir -p ~/.local/bin
mv exifutils-linux-x64 ~/.local/bin/exifutils
```

Make sure `~/.local/bin` is in your PATH. Add to `~/.bashrc` or `~/.zshrc`:

```bash
export PATH="$HOME/.local/bin:$PATH"
```

#### Windows Installation

1. Rename `exifutils-windows-x64.exe` to `exifutils.exe`
2. Move it to a directory in your PATH, or add its location to PATH

### Option 2: Jar

Requires Java 21+. Works on all platforms including macOS. Download `exifutils.jar` from [Releases](https://github.com/kubisoftsk/exifutils/releases) and run it:

```bash
java -jar exifutils.jar [command]
```

Development snapshots are described in [Snapshots and Releases](releasing.md).

### Option 3: Build from Source

Requires Java 21+ and Maven.

```bash
git clone https://github.com/kubisoftsk/exifutils.git
cd exifutils
mvn clean package -Pfat-jar -DskipTests
```

The JAR will be at `cli/target/exifutils-cli-0.9.0-SNAPSHOT.jar`.

Run with:

```bash
java -jar cli/target/exifutils-cli-0.9.0-SNAPSHOT.jar [command]
```

### Option 4: Build Native Binary Locally

Requires GraalVM 21+ with native-image.

```bash
git clone https://github.com/kubisoftsk/exifutils.git
cd exifutils
mvn clean package -Pnative -DskipTests
```

The native binary will be at `cli/target/exifutils`.

## Updating

Simply repeat the installation steps. The new binary will replace the old one.

## Verifying Installation

```bash
exifutils --help
```

You should see the list of available commands.
