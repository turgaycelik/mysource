#!/bin/bash

function killVnc {
    VNC_PID_FILE=`echo $HOME/.vnc/*:20.pid`
    if [ -n "$VNC_PID_FILE" -a -f "$VNC_PID_FILE" ]; then
        vncserver -kill :20 >/dev/null 2>&1
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

displayEnv() {

	echo "---------------------------------------------"
	echo "Displaying Environment Variables"
	echo "---------------------------------------------"
	env
	echo "---------------------------------------------"
}

echo starting vncserver

killVnc

#echo vncserver :20
vncserver :20 >/dev/null 2>&1
echo vncserver started on :20

displayEnv

# Move the mouse pointer out of the way
# echo Moving mouse pointer to 10 10.
# xwarppointer abspos 10 10

