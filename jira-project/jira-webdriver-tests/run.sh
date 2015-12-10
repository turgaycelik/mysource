#!/bin/bash

OPTS="$*"

RESULTDIR="target/test-reports"
CAPTURE="${RESULTDIR}/capture_raw.mkv"
SUBTITLES="${RESULTDIR}/capture.srt"
CHAPTERS="${RESULTDIR}/capture.chap"
OUTPUT="${RESULTDIR}/capture.mkv"

vncdisplay=""

killvnc() {
    if ! test -z "${vncdisplay}"; then
        echo stopping vncserver on $DISPLAY
        vncserver -kill $vncdisplay >/dev/null 2>&1
    fi
}

createMovie() {
    if test -e "$CAPTURE" && test -e "$SUBTITLES" && test -e "$CHAPTERS"; then
        mkvmerge --default-language eng -o "$OUTPUT" --language 1:eng --default-track 1:true "$CAPTURE" \
        --title "Selenium" --sub-charset 0:utf8 --language 0:eng --default-track 0:true "$SUBTITLES" \
        --chapter-language eng --chapter-charset utf8 --chapters "$CHAPTERS"

        if test $? -eq 0; then
            rm -f "$CAPTURE"
        fi
    fi
}

displayEnv() {

	echo "---------------------------------------------"
	echo "Displaying Environment Variables"
	echo "---------------------------------------------"
	env
	echo "---------------------------------------------"
}

echo starting vncserver
vncdisplay=$(vncserver 2>&1 | perl -ne '/^New .* desktop is (.*)$/ && print"$1\n"')
if [[ -z "$vncdisplay" ]]; then
     echo "failed to create a vncserver or get its display identifier"
     exit 2
fi

export DISPLAY=$vncdisplay
echo vncserver started on $DISPLAY

displayEnv

# Move the mouse pointer out of the way
# echo Moving mouse pointer to 10 10.
# xwarppointer abspos 10 10

#Make sure the VNC server is killed always. Why wont you just die!
trap killvnc INT TERM EXIT

if [ -f "${M2_HOME}/bin/mvn" ]; then
  MVN=${M2_HOME}/bin/mvn
else
  MVN=mvn
fi

echo Starting $MVN $OPTS
$MVN $OPTS
MVN_STATUS=$?

createMovie

exit $MVN_STATUS
