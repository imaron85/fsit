#!/usr/bin/env sh

set -e

if [ -z "$MODRINTH_TOKEN" ]; then
  printf 'Enter MODRINTH_TOKEN: ' >&2

  stty -echo
  read -r MODRINTH_TOKEN
  stty echo
  printf '\n'
fi

case "$MODRINTH_TOKEN" in
'mrp_'*) ;;
*) exit 1 ;;
esac

readonly MODRINTH_TOKEN

srcpath="$(mktemp -dut 'fsit-src.XXXXXXXXXX')"
readonly srcpath

git clone https://codeberg.org/rvbsm/fsit.git "$srcpath"
cd "$srcpath" || exit 1

version=$(git describe --tags)
readonly version

CHANGELOG="$(awk -v version="$version" '/^## v[0-9]+.[0-9]+.[0-9]+/ { printit = $2 == version; next }; printit && NF' CHANGELOG.md)"
readonly CHANGELOG

if [ -z "$CHANGELOG" ]; then
  printf 'No changelog was found for version %s\n' "$version" >&2
  exit 1
fi

export CHANGELOG
./gradlew --no-daemon chiseledPublish
