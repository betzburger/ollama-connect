#!/usr/bin/env bash
# Wraps the jpackage app-image (already built by :composeApp:createReleaseDistributable)
# into a distro-agnostic .AppImage. Usage: package-appimage.sh <x86_64|aarch64>
set -euo pipefail

ARCH_TAG="$1"
APP_NAME="Ollama Connect"
VERSION="2.2.0"

APP_IMAGE_SRC=$(find composeApp/build/compose/binaries/main-release/app -mindepth 1 -maxdepth 1 -type d | head -1)
echo "jpackage app-image source: $APP_IMAGE_SRC"
find "$APP_IMAGE_SRC" -maxdepth 3

APPDIR="$(pwd)/AppDir"
rm -rf "$APPDIR"
mkdir -p "$APPDIR"
cp -r "$APP_IMAGE_SRC"/. "$APPDIR"/

BIN_PATH=$(find "$APPDIR/bin" -maxdepth 1 -type f -perm -u+x | head -1)
BIN_NAME=$(basename "$BIN_PATH")
echo "Launcher binary: $BIN_NAME"

cp composeApp/src/androidMain/res/mipmap-xxxhdpi/ic_launcher.png "$APPDIR/$BIN_NAME.png"

cat > "$APPDIR/$BIN_NAME.desktop" <<EOF
[Desktop Entry]
Type=Application
Name=$APP_NAME
Exec="$BIN_NAME"
Icon=$BIN_NAME
Categories=Utility;
EOF

ln -sf "bin/$BIN_NAME" "$APPDIR/AppRun"

wget -q -O appimagetool "https://github.com/AppImage/AppImageKit/releases/download/continuous/appimagetool-${ARCH_TAG}.AppImage"
chmod +x appimagetool

export ARCH="$ARCH_TAG"
# Runners generally lack libfuse2, so extract-and-run instead of mounting.
./appimagetool --appimage-extract-and-run "$APPDIR" "${APP_NAME// /-}-${VERSION}-${ARCH_TAG}.AppImage"
