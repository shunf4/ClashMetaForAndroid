#!/bin/sh

export KTO_OKN_IS_SOURCE_MORE_MODE=1
[ -r /etc/profile.d/01-kto-startup.source.sh ] && source /etc/profile.d/01-kto-startup.source.sh

if [ "$IN_WINGCCENV" != "1" ] && [[ "$(type kto_wingccenv | head -n 1)" = *" is a function" ]]; then
	IN_WINGCCENV=1 kto_wingccenv bash "$0" "$@"
	exit "$?"
fi

if [ "$IN_FQ" != "1" ] && [[ "$(type fq | head -n 1)" = *" is a function" ]]; then
	IN_FQ=1 fq bash "$0" "$@"
	exit "$?"
fi

if [ "$IN_JAVAENV21" != "1" ] && [[ "$(type javaenv21 | head -n 1)" = *" is a function" ]]; then
	IN_JAVAENV21=1 javaenv21 bash "$0" "$@"
	exit "$?"
fi

which java
echo "JAVA_HOME: ${JAVA_HOME}"

./gradlew    "$@"   app:assembleAlphaRelease

