import os

from CommandExecutor import Callable

class Notifier(Callable):

    def __init__(self, execution_time):
        super().__init__(None)
        self.execution_time = execution_time

    def __call__(self, logger):
        logger.debug('Sending notification that the build has finished.')

        icon = os.path.abspath(os.sep.join(['.', 'jira-components', 'jira-webapp', 'src', 'main', 'webapp', 'images', '16jira.png']))
        text = 'Build complete. You may now continue to advance humanity.\n(jmake finished in %s sec.)' % self.execution_time.time
        commands = [ 'notify-send -t 60000 -i {1} JMAKE "{0}"',
                     'zenity --info --title=JMAKE --timeout=60 --window-icon="{1}" --text="{0}"',
                     'growlnotify JMAKE --image "{1}" -m "{0}"'
                     #''' osascript -e 'tell app "System Events" to display alert "JMAKE" message "{0}"' '''
                     ]

        any((os.system((command + ' 2>/dev/null').format(text, icon)) == 0 for command in commands))
        return Callable.success
