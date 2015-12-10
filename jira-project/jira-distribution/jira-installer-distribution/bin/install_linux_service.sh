#!/bin/bash

. `dirname $0`/user.sh #readin the username

jira_account=

if [ -z "$JIRA_USER" ]; then
        jira_account="jira"
else
        jira_account=$JIRA_USER
fi

if [[ $1 == "-u" ]]; then
    echo uninstalling JIRA as a service
    if [[ -x $(which update-rc.d) ]]; then
        update-rc.d -f $jira_account remove
        rm -f /etc/init.d/$jira_account
    else
        rm -f /etc/init.d/$jira_account /etc/rc1.d/{S,K}95$jira_account
        for (( i=1; i<=5; i++ )); do
            rm -f /etc/rc$i.d/{S,K}95$jira_account
        done
    fi
else

    if [[ -d /etc/init.d ]]; then
        echo installing JIRA as a service
        JIRA_BIN=`dirname $0`
        cat >/etc/init.d/$jira_account <<EOF
#!/bin/bash

# JIRA Linux service controller script
cd "$JIRA_BIN"

case "\$1" in
    start)
        ./start-jira.sh
        ;;
    stop)
        ./stop-jira.sh
        ;;
    *)
        echo "Usage: \$0 {start|stop}"
        exit 1
        ;;
esac
EOF
        chmod +x /etc/init.d/$jira_account
        if [[ -x $(which update-rc.d) ]]; then
            update-rc.d -f $jira_account defaults
        else
            ln -s /etc/init.d/$jira_account /etc/rc1.d/K95$jira_account
            ln -s /etc/init.d/$jira_account /etc/rc2.d/K95$jira_account
            ln -s /etc/init.d/$jira_account /etc/rc3.d/S95$jira_account
            ln -s /etc/init.d/$jira_account /etc/rc4.d/K95$jira_account
            ln -s /etc/init.d/$jira_account /etc/rc5.d/S95$jira_account
        fi
    fi
fi