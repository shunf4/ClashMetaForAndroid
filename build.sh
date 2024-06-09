#!/bin/sh

[ -r ~/.local/share/riv-include.source ] && source ~/.local/share/riv-include.source

if [ "$IN_WINGCCENV" != "1" ] && [[ "$(type wingccenv | head -n 1)" = *" is a function" ]]; then
	IN_WINGCCENV=1 wingccenv bash "$0" "$@"
	exit "$?"
fi

if [ "$IN_FQ" != "1" ] && [[ "$(type fq | head -n 1)" = *" is a function" ]]; then
	IN_FQ=1 fq bash "$0" "$@"
	exit "$?"
fi

if [ "$IN_JAVAENV17" != "1" ] && [[ "$(type javaenv17 | head -n 1)" = *" is a function" ]]; then
	IN_JAVAENV17=1 javaenv17 bash "$0" "$@"
	exit "$?"
fi

which java
echo "JAVA_HOME: ${JAVA_HOME}"

./gradlew    "$@"   app:assembleMeta-AlphaRelease
