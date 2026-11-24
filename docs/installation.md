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

### Option 1: Jar (Recommended)

Requires Java 21+. Works on Linux, Windows and macOS. Download `exifutils.jar` from [Releases](https://github.com/kubisoftsk/exifutils/releases) (or directly the [latest one](https://github.com/kubisoftsk/exifutils/releases/latest/download/exifutils.jar)) and run it:

```bash
java -jar exifutils.jar [command]
```

`SHA256SUMS` contains the checksum. Development snapshots are described in [Snapshots and Releases](releasing.md).

On Linux and macOS, a small script in a directory from `PATH` makes it available as `exifutils`:

```bash
mkdir -p ~/.local/share/exifutils ~/.local/bin
mv exifutils.jar ~/.local/share/exifutils/
printf '#!/bin/sh\nexec java -jar "$HOME/.local/share/exifutils/exifutils.jar" "$@"\n' > ~/.local/bin/exifutils
chmod +x ~/.local/bin/exifutils
```

### Option 2: Build from Source

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

### Option 3: Build Native Binary Locally (Experimental)

Requires GraalVM 21+ with native-image. Native binaries do not work correctly yet, see [#53](https://github.com/kubisoftsk/exifutils/issues/53).

```bash
git clone https://github.com/kubisoftsk/exifutils.git
cd exifutils
mvn clean package -Pnative -DskipTests
```

The native binary will be at `cli/target/exifutils`.

## Updating

Simply repeat the installation steps. The new file will replace the old one.

## Verifying Installation

```bash
exifutils --help
```

You should see the list of available commands.
