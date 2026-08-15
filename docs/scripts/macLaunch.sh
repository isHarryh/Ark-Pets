#!/bin/sh

show_err() {
    osascript -e "display alert \"Start Failed\" as critical message \"$1\""
    exit 1
}

SCRIPT_PATH=$(cd "$(dirname "$0")" && pwd)
APP_ROOT=$(dirname "$SCRIPT_PATH")
ICON_PATH="$APP_ROOT/Resources/ArkPets.icns"

JAVA_EXEC="$APP_ROOT/runtime/Contents/Home/bin/java"
JAR_PATH=$(find "$APP_ROOT/app" -name "desktop*.jar" 2>/dev/null | head -n 1)

if [ -z "$JAR_PATH" ]; then
    show_err "Cannot found main jar."
fi

mkdir -p ~/Library/Application\ Support/ArkPets/
cd ~/Library/Application\ Support/ArkPets/ || show_err "Failed to prepare working dir"

if [ ! -x "$JAVA_EXEC" ]; then
  show_err "Java executable not found at $JAVA_EXEC"
fi

"$JAVA_EXEC" -Xdock:icon="$ICON_PATH" -Xdock:name="ArkPets Launcher" -jar "$JAR_PATH"
