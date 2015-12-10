import os
import sys
from CommandExecutor import Callable, SystemCallable
from JmakeAutoComplete import get_autocomplete_file
from Logger import Logger
from maven.Maven import MavenCallable, MavenVersionInaccessibleException
from utils.FileUtils import FileUtils


class Maven3Discovery(Callable):

    def __init__(self, args):
        super().__init__(args)

    def __call__(self, logger):

        try:
            MavenCallable().require_mvn3()
        except MavenVersionInaccessibleException:
            if os.getenv('JMAKE_STFU') is None:
                logger.warn('jmake was not able to find your Maven 3. But it tried:')
                logger.warn(' * M3_HOME is not set,')
                logger.warn(' * running mvn3 just like that did not work - if you have Maven 3 installed, you might also create a symlink on your path called "mvn3" for jmake to find.')
                logger.warn(' * running mvn just like that ended up in maven 2 or failed (however it is NOT recommended to put mvn 3 on your path as "mvn")')
                logger.warn('Download and install latest maven 3.0.x (3.0.5 or later, but not 3.1.y):')
                logger.warn(' * manually from http://maven.apache.org/download.cgi, then set your M3_HOME acordingly.')


        return Callable.success


class AutoCompleteDiscovery(Callable):

    def __init__(self):
        super().__init__(None)

    def __call__(self, logger):
        autocomplete_file = get_autocomplete_file(Logger().set_none())
        if autocomplete_file and not os.path.isfile(autocomplete_file):
            logger.warn('You did not install autocomplete yet! Try it out:')
            logger.warn('$ sudo ./jmake auto-complete --install')

        return Callable.success


class GitBranchDiscovery(SystemCallable):

    def __init__(self):
        super().__init__(None, 'git rev-parse --abbrev-ref HEAD')

    def process_output(self, logger : Logger, line : str, num : int):
        if line:
            self.branch = line
        return False

    def __call__(self, logger):
        return super().__call__(Logger().set_none())


class RequireSudo(SystemCallable):

    def __init__(self):
        super().__init__(None, 'whoami')
        self.root = False

    def process_output(self, logger : Logger, line : str, num : int):
        self.root = (line.strip() == 'root')
        return False

    def __call__(self, logger):
        rc = super().__call__(Logger().set_none())
        if rc != self.success:
            logger.error('User verification failed!')
            return rc

        if self.root:
            return Callable.success
        else:
            logger.error('This command requires administrative rights. Rerun it with sudo like this:')
            logger.error('$ sudo ./jmake ' + ' '.join(list(sys.argv[1:])))
            return self.do_not_proceed


class BranchInconsistencyCheck(Callable):

    def __init__(self, inspector):
        super().__init__(None)
        self.inspector = inspector

    def __call__(self, logger, fileutils=FileUtils()):
        file_name = os.sep.join(['.',  'target', '.jmake-branch'])
        try:
            with fileutils.open(file_name, 'r', encoding='utf-8') as file:
                previous_branch = file.readline().rstrip('\n')

            if not previous_branch == self.inspector.branch:
                logger.error('jmake detected that it was previously run on a different branch (%s) current branch: (%s).' % (previous_branch, self.inspector.branch))
                logger.error('It is advised to clean the old targets.')
                logger.info('To override: rm ' + file_name)
                self.inspector.inconsistent = True
                return Callable.do_not_proceed
        except IOError:
            pass
        return Callable.success


class BranchInconsistencyMarker(Callable):

    def __init__(self, inspector):
        super().__init__(None)
        self.inspector = inspector

    def __call__(self, logger, fileutils=FileUtils()):
        if not getattr (self.inspector, 'inconsistent', False):
            target_dir = fileutils.existing_dir('target')
            file = os.sep.join([target_dir, '.jmake-branch'])
            with open(file, 'w', encoding='utf-8') as file:
                file.write(self.inspector.branch)
        return Callable.success


TYPE_WAIT = 'wait'
TYPE_PROBE = 'probe'

class LocalPortInspector(Callable):

    """
    Will halt the build if specified port is blocked by another app.
    """

    def __init__(self, port: int, quiet: bool=False, port_name='required port', repeat: bool=True, tries: int=15,
            interval_secs: int=2, inspection_type: str=TYPE_WAIT):
        super().__init__(None)
        self.port = port
        self.quiet = quiet
        self.name = port_name
        self.repeat = repeat
        self.interval_secs = interval_secs
        self.tries = tries if repeat else 1
        self.inspection_type = inspection_type
        self.port_setter = None

    def probe(self, port_setter):
        self.inspection_type = TYPE_PROBE
        self.port_setter = port_setter
        return self

    def wait(self):
        self.inspection_type = TYPE_WAIT
        return self

    def __call__(self, logger):

        if not self.quiet:
            logger.debug("Checking if %s %s is free..." % (self.name, str(self.port)))

        tried = 0
        while True:
            checker = PortChecker(self.port)
            if checker():
                tried += 1
                if not self.quiet:
                    logger.error('Port %s is not free (jmake could connect to it).' % str(self.port))
                if self.repeat and (tried < self.tries):
                    if self.inspection_type == TYPE_WAIT:
                        logger.info('Attempt %d of %d failed. Retrying in %d seconds.' % (tried, self.tries, self.interval_secs))
                        external_sleep = SystemCallable(None, 'sleep %d' % self.interval_secs)
                        external_sleep(logger)
                        if external_sleep.returncode != Callable.success:
                            return external_sleep.returncode
                        continue
                    if self.inspection_type == TYPE_PROBE:
                        self.port += 1
                        logger.info('Attempt %d of %d failed. Probing port %s...' % (tried, self.tries, self.port))
                        continue
                logger.error('Stopping build.')
                return Callable.do_not_proceed
            else:
                if self.port_setter:
                    self.port_setter(self.port)
            return Callable.success


class AllPortsChecker(SystemCallable):
    """Checks status of the the port return true if port is open (something is listening) and false otherwise """
    def __init__(self):
        super().__init__(None, '''netstat -aln | awk '$6 == "LISTEN" { print $4 }' ''')
        self.open_ports = set()

    def get_open_ports(self):
        return self.open_ports

    def process_output(self, logger: Logger, line: str, num: int):
        trimmed = line
        if ':' in trimmed:
            trimmed = trimmed[(trimmed.rfind(':') + 1):]
        if '.' in line:
            trimmed = trimmed[(trimmed.rfind('.') + 1):]

        try:
            # at times, there is nothing left in
            self.open_ports.add(int(trimmed))
        except Exception:
            logger.warn('Could not get port from "%s"' % line)
        return False

    def __call__(self, logger=None):
        return super().__call__(Logger().set_none())


class PortChecker(AllPortsChecker):
    """Checks status of the the port return true if port is open (something is listening) and false otherwise """
    def __init__(self, port):
        super().__init__()
        self.port = port
        self.open = False

    def __call__(self, logger=None):
        super().__call__(Logger().set_none())
        return self.port in self.open_ports




