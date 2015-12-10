import os
from CommandExecutor import Callable, SystemCallable
from Diagnostics import RequireSudo
from module import JmakeModule
from utils.FileUtils import FileUtils

MYSQL_DUMP_FILE = os.sep.join(['jmake_src', 'data', 'jira-mysql.sql'])


class MysqlInit(JmakeModule):

    def __init__(self):
        super().__init__()
        self.command = 'init'
        self.description = 'Sets up jmake user for your local mysql database.'
        self.check_branch = False

    def __call__(self, args, executor):

        commands = [
            'DROP DATABASE IF EXISTS jira_jmake;',
            'GRANT USAGE ON *.* TO \'jmake\'@\'%\';',
            'DROP USER \'jmake\'@\'%\';',
            'CREATE USER \'jmake\'@\'%\' IDENTIFIED BY \'jmake-dev-local\';',
            'CREATE DATABASE jira_jmake;',
            'GRANT ALL PRIVILEGES ON jira_jmake.* TO \'jmake\'@\'%\';'
        ]

        executor.append(lambda logger: logger.info('Attempting to make the following statements through mysql:'))
        for command in commands:
            executor.append(lambda logger, cmd=command: logger.info(cmd))
        # executor.append(RequireSudo())
        executor.append(SystemCallable(args, 'echo "%s" | mysql -u root -h 127.0.0.1' % ' '.join(commands)))


class MysqlRestore(JmakeModule):

    def __init__(self):
        super().__init__()
        self.command = 'restore'
        self.description = 'Loads the database contents with default JIRA contents.'

    def __call__(self, args, executor):
        executor.append(SystemCallable(args, 'echo "DROP DATABASE IF EXISTS jira_jmake" | mysql -u root -h 127.0.0.1'))
        executor.append(SystemCallable(args, 'echo "CREATE DATABASE jira_jmake" | mysql -u root -h 127.0.0.1'))
        executor.append(SystemCallable(args, 'mysql -u root -h 127.0.0.1 jira_jmake < %s' % MYSQL_DUMP_FILE))


class Mysql(JmakeModule):

    def __init__(self):
        super().__init__()
        self.command = 'mysql'
        self.description = 'Prepares databse for running "./jmake run --db mysql"'

    def get_submodules(self):
        return [MysqlInit(), MysqlRestore()]

