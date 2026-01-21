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

Native binaries are standalone executables that don't require Java.

1. Go to the repository **Actions** tab
2. Select **Build Native Executable** workflow
3. Run the workflow for your platform
4. Download the artifact ZIP:
   - `exifutils-linux-native` for Linux
   - `exifutils-windows-native` for Windows

#### Linux Installation

**System-wide (requires sudo):**

```bash
unzip exifutils-linux-native.zip
chmod +x exifutils
sudo mv exifutils /usr/local/bin/
```

**User-only (no sudo):**

```bash
unzip exifutils-linux-native.zip
chmod +x exifutils
mkdir -p ~/.local/bin
mv exifutils ~/.local/bin/
```

Make sure `~/.local/bin` is in your PATH. Add to `~/.bashrc` or `~/.zshrc`:

```bash
export PATH="$HOME/.local/bin:$PATH"
```

#### Windows Installation

1. Extract `exifutils.exe` from the ZIP
2. Move to a directory in your PATH, or add its location to PATH

### Option 2: Build from Source

Requires Java 21+ and Maven.

```bash
git clone https://github.com/your-username/exifutils.git
cd exifutils
mvn clean package -DskipTests
```

The JAR will be at `cli/target/exifutils-cli-1.0.0-SNAPSHOT-jar-with-dependencies.jar`.

Run with:

```bash
java -jar cli/target/exifutils-cli-1.0.0-SNAPSHOT-jar-with-dependencies.jar [command]
```

### Option 3: Build Native Binary Locally

Requires GraalVM 21+ with native-image.

```bash
git clone https://github.com/your-username/exifutils.git
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
