import os
from CommandExecutor import Callable, SystemCallable
from Diagnostics import RequireSudo
from module import JmakeModule
from utils.FileUtils import FileUtils

DB_NAME = 'jira_jmake'
DB_USER = 'jmake'
DB_PASSWORD = 'jmake-dev-local';

POSTGRES_DUMP_FILE = os.sep.join(['jmake_src', 'data', 'jira-postgres.sql'])
PGPASS_FILE = os.sep.join([os.path.expanduser('~'), '.pgpass'])
PGPASS_CREDENTIALS = '*:*:*:%s:%s' % (DB_USER, DB_PASSWORD)

class PostgresqlInit(JmakeModule):

    def __init__(self):
        super().__init__()
        self.command = 'init'
        self.description = 'Sets up %s user for your local postgres database. Must be ran with administrative' \
                           ' privilages.' % (DB_USER,)
        self.check_branch = False

    def __call__(self, args, executor):

        commands = [
            'DROP USER %s ;' % (DB_USER,),
            'CREATE USER %s WITH PASSWORD \'%s\';' % (DB_USER, DB_PASSWORD),
            'ALTER USER %s CREATEDB;' % (DB_USER,),
            'DROP DATABASE %s;' % (DB_NAME,),
            'CREATE DATABASE %s WITH OWNER %s;' % (DB_NAME, DB_USER),
            'GRANT ALL PRIVILEGES ON DATABASE %s to %s;' % (DB_NAME, DB_USER)
        ]

        executor.append(lambda logger: logger.info('Attempting to make the following statements through psql using user "postgres":'))
        for command in commands:
            executor.append(lambda logger, cmd=command: logger.info(cmd))
        executor.append(RequireSudo())
        # 'su -' will simulate a full login but will clear the PATH environment variable (because of env_reset) and there is not a unique way to modify this behaviour on different platforms.
        # http://askubuntu.com/questions/128413/setting-the-path-so-it-applies-to-all-users-including-root-sudo
        executor.append(SystemCallable(args, 'echo "%s" | su postgres -c psql' % ' '.join(commands)))


class PostgresqlRestore(JmakeModule):

    def __init__(self):
        super().__init__()
        self.command = 'restore'
        self.description = 'Loads the database contents with default JIRA contents.'

    def __call__(self, args, executor):
        executor.append(Postgresql.create_jmake_credentials_file(args))
        executor.append(SystemCallable(args, 'dropdb -U %s -w %s' % (DB_USER, DB_NAME)))
        executor.append(SystemCallable(args, 'createdb -U %s -w %s' % (DB_USER, DB_NAME)))
        executor.append(SystemCallable(args, 'psql -d %s -U %s -w -f %s' % (DB_NAME, DB_USER, POSTGRES_DUMP_FILE)))


class Postgresql(JmakeModule):

    def __init__(self):
        super().__init__()
        self.command = 'postgres'
        self.description = 'Prepares database for running "./jmake run --db postgresql"'

    def get_submodules(self):
        return [PostgresqlInit(), PostgresqlRestore()]

    @staticmethod
    def create_jmake_credentials_file(args, fs:FileUtils=FileUtils()):
        def jmake_user_credentials_closure(logger):
            if fs.file_exists(PGPASS_FILE):
                with open(PGPASS_FILE, 'r') as f:
                    for line in f:
                        if PGPASS_CREDENTIALS in line:
                            logger.info('Password for user "%s" already exist in %s' % (DB_USER, PGPASS_FILE))
                            return Callable.success


            logger.info('Adding default password for db user "%s"... ' % (DB_USER,))
            try:
                with open(PGPASS_FILE, 'a') as f:
                    f.write(PGPASS_CREDENTIALS)
                    f.write('\n')

                #  postgres requires this file is with mode 0600 otherwise it won't trust it:
                rc = SystemCallable(args, 'chmod 0600 %s' % PGPASS_FILE)(logger)
                return rc
            except Exception as e:
                logger.error('Could not add default jmkae credentials to % file.' % PGPASS_FILE)
                return Callable.do_not_proceed

        return jmake_user_credentials_closure










