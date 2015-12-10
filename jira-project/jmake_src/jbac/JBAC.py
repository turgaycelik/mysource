from getpass import getpass
import os
from CommandExecutor import SystemCallable, Callable
from Logger import Logger, LOG
from utils.FileUtils import FileUtils


class Jbac:
    url = 'https://jira-bamboo.internal.atlassian.com'


class OneLiner(SystemCallable):

    def process_output(self, logger: Logger, line: str, num: int):
        self.line = line
        return False


class JbacAuthentication:

    __auth = None

    @staticmethod
    def get():
        if JbacAuthentication.__auth is None:
            auth = JbacAuthentication.__hack_maven_settings()
            auth = JbacAuthentication.__user_input() if auth is None else auth
            JbacAuthentication.__auth = auth
        return JbacAuthentication.__auth

    @staticmethod
    def __user_input():
        login = input('JBAC login: ')
        password = getpass('JBAC password: ')
        return JbacAuthentication(login, password)

    @staticmethod
    def __hack_maven_settings(fs: FileUtils=FileUtils()):
        # attempt to locate maven by running atlas-version
        oneliner = OneLiner(None, 'atlas-version | grep "ATLAS Maven"')
        oneliner(LOG)
        if oneliner.returncode != Callable.success:
            return None
        maven_home = oneliner.line[oneliner.line.find(':') + 2:]

        target_dir = fs.existing_dir(os.sep.join(['.', 'jmake_src', 'target']))
        if not fs.file_exists(os.sep.join([target_dir, 'Decipher.class'])):
            oneliner = OneLiner(None, '$JAVA_HOME/bin/javac -cp %s:. -d %s Decipher.java'
                        % (os.sep.join([maven_home, 'lib', '*']),
                           target_dir), cwd=os.sep.join(['.', 'jmake_src', 'jbac', 'java']))
            oneliner(LOG)

        oneliner = OneLiner(None, '$JAVA_HOME/bin/java -cp %s:%s Decipher'
                            % (os.sep.join([maven_home, 'lib', '*']), target_dir))
        oneliner(LOG)
        if oneliner.returncode != Callable.success:
            return None
        credentials = oneliner.line.split(':')
        return JbacAuthentication(credentials[0], credentials[1])

    def __init__(self, login: str, password: str):
        self.login = login
        self.password = password
