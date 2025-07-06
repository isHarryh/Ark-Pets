#!/bin/sh

echo $1
cp -v ./docs/scripts/macLaunch.sh ./desktop/build/jpackage/ArkPets.app/Contents/MacOS/ArkPets

create-dmg --volname ArkPets \
--app-drop-link 300 50 \
--icon "ArkPets.app" 100 50 \
--icon "LICENSE" 500 50 \
--window-size 700 60 \
--icon-size 100 \
"./desktop/build/dist/ArkPets-v$1-mac-$(uname -m).dmg" ./desktop/build/jpackage/