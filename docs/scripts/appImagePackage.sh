#!/bin/bash

cd desktop/build || exit

NO_STRIP=true linuxdeploy --appdir=ArkPets.AppDir \
-e "jpackage/ArkPets/bin/ArkPets" \
--icon-file="jpackage/ArkPets/lib/ArkPets.png" \
--create-desktop-file

cp -rv jpackage/ArkPets/lib/* "ArkPets.AppDir/usr/lib"

appimagetool ArkPets.AppDir/ "dist/ArkPets-v$1-linux-$(uname -m).AppImage"

rm -rv ArkPets.AppDir