#!/bin/sh
GRADLE_VERSION=8.14.3
ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
GRADLE_HOME="$ROOT_DIR/.gradle/gradle-$GRADLE_VERSION"
if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
  mkdir -p "$ROOT_DIR/.gradle"
  ZIP="$ROOT_DIR/.gradle/gradle-$GRADLE_VERSION-bin.zip"
  echo "Downloading Gradle $GRADLE_VERSION..."
  if command -v curl >/dev/null 2>&1; then
    curl -fL "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$ZIP" || exit 1
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" || exit 1
  else
    echo "curl or wget is required." >&2
    exit 1
  fi
  unzip -q -o "$ZIP" -d "$ROOT_DIR/.gradle" || exit 1
fi
exec "$GRADLE_HOME/bin/gradle" "$@"
