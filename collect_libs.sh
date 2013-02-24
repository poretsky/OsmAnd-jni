#!/bin/bash
ROOT_LOC=..
function copyLibs {
	if [ -d "$ROOT_LOC/binaries/$1/$2" ]; then 
      echo "Copy binaries $1 $2";
      cp "$ROOT_LOC"/binaries/$1/$2/libOsmAndJNI.$4 bin/OsmAndJNI-$1-$3.lib
      cp "$ROOT_LOC"/binaries/$1/$2/libOsmAndCoreUtils.$4 bin/OsmAndCoreUtils-$1-$3.lib
      cp "$ROOT_LOC"/binaries/$1/$2/libOsmAndCore.$4 bin/OsmAndCore-$1-$3.lib
      cp "$ROOT_LOC"/core/externals/qtbase-desktop/upstream.patched.$1.$2/lib/libQt5Core.$4.5.0.2 bin/Qt5Core-$1-$3.lib
    fi
}

# copyLibs linux amd64 amd64 so
copyLibs linux i686 x86 so
