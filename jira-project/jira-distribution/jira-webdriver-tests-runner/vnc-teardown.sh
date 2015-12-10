

function killVnc {
    echo stopping vncserver on :20

    VNC_PID_FILE=`echo $HOME/.vnc/*:20.pid`
    if [ -n "$VNC_PID_FILE" -a -f "$VNC_PID_FILE" ]; then
        vncserver -kill :20 2>&1
        if [ -f  "$VNC_PID_FILE" ]; then
            VNC_PID=`cat $VNC_PID_FILE`
            echo "Killing VNC pid ($VNC_PID) directly..."
            kill -9 $VNC_PID
            vncserver -kill :20 >/dev/null 2>&1

            if [ -f  "$VNC_PID_FILE" ]; then
                echo "Failed to kill vnc server"
                exit -1
            fi
        fi
    fi
}

trap killVnc INT TERM EXIT

RESULTDIR="$HOME/jira-distribution/jira-webdriver-tests-runner/target/test-reports"
CAPTURE="${RESULTDIR}/capture_raw.mkv"
SUBTITLES="${RESULTDIR}/capture.srt"
CHAPTERS="${RESULTDIR}/capture.chap"
OUTPUT="${RESULTDIR}/capture.mkv"

#createMovie
if test -e "$CAPTURE" && test -e "$SUBTITLES" && test -e "$CHAPTERS"; then
    mkvmerge --default-language eng -o "$OUTPUT" --language 1:eng --default-track 1:true "$CAPTURE" \
    --title "Selenium" --sub-charset 0:utf8 --language 0:eng --default-track 0:true "$SUBTITLES" \
    --chapter-language eng --chapter-charset utf8 --chapters "$CHAPTERS"

    if test $? -eq 0; then
        rm -f "$CAPTURE"
    fi
fi
