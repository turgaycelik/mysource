import os
import unittest
from CommandExecutor import SystemCallable, Callable
from Logger import Logger
from maven.Maven import MavenCallable, MavenVersionInaccessibleException
from maven.Surefire import Surefire
from module import JmakeModule
from utils.FileUtils import FileUtils
from utils.ProcessUtils import ProcessUtils

dependencies_file = os.sep.join(['.', 'jira-components', 'jira-core', 'dependencies.txt'])
dependencies_tmp = dependencies_file + '.tmp'


class UnitTest(JmakeModule):
    def __init__(self, fileutils=FileUtils(), surefire: Surefire=None, process_utils=ProcessUtils()):
        super().__init__()
        self.command = 'unit-tests'
        self.description = 'Runs JIRA Unit Tests and verifies dependencies. This can also be used to run the findbugs '\
                           'analysis on code covered with Unit Tests. This runs unit tests from the JIRA core, '\
                           'unit tests of the func test module and from bundled plugins modules.'
        self.fileutils = fileutils
        self.surefire = surefire if surefire else Surefire()
        self.process_utils = process_utils

    def __call__(self, args, executor):

        if self.fileutils.file_exists(dependencies_tmp):
            executor.append(lambda logger: self.fileutils.remove(dependencies_tmp) or 0)

        maven = MavenCallable(args, process_utils=self.process_utils)
        maven.projects = ['jira-components/jira-tests-parent/jira-tests',
                          'jira-components/jira-tests-parent/jira-tests-unit']
        if not args.skip_legacy:
            maven.project('jira-components/jira-tests-parent/jira-tests-legacy')
        maven.option('-am')
        maven.phase('compile' if args.vdep else 'test')
        maven.property('verifyDependencies').property('jira.minify.skip', 'true').property('performApiCheck').property(
            'ajEnforcer')

        maven.property('maven.test.func.skip', 'true').property('maven.test.selenium.skip', 'true')
        if not args.skip_bp:
            report_projects = ['jira-components/jira-tests-parent',
                               'jira-func-tests',
                               'jira-components/jira-plugins']
            maven.project('jira-components/jira-plugins/jira-bundled-plugins')
        else:
            report_projects = ['jira-components/jira-tests-parent',
                               'jira-func-tests']
            maven.project('jira-func-tests')

        try:
            maven.require_mvn3().can_run_in_parallel()
        except MavenVersionInaccessibleException:
            pass

        self.surefire.set_roots(report_projects)
        executor.append(self.surefire.clean_executable())

        if args.findbugs:
            try:
                executor.append(maven.phase('test').profile('findbugs').require_mvn3())
            except MavenVersionInaccessibleException as e:
                executor.append(maven.mvn3_inaccessible_msg_callable('unit tests with findbugs', e))
        else:
            if args.vdep:
                executor.append(maven.phase('compile'))
            else:
                executor.append(maven.phase('test').option('--fail-at-end'))
                executor.append_post(self.surefire.report_executable())

        executor.append_post(DependenciesReport(args))

    def define_parser(self, parser):
        MavenCallable.add_maven_switches(parser)
        parser.add_argument('-fb', '--findbugs', action='store_true',
            help='include findbugs check (requires maven 3).')
        parser.add_argument('-vd', '--verify-deps-only', action='store_true',
            help='only verify dependencies, without running tests.', dest='vdep')
        parser.add_argument('--skip-bundled-plugins', action='store_true',
            help='skips unit tests from bundled plugins.', dest='skip_bp')
        parser.add_argument('--skip-legacy-tests', action='store_true',
            help='skips legacy (integration) unit tests.', dest='skip_legacy')


class DependenciesReport(SystemCallable):

    def __init__(self, args):
        super().__init__(args, command='diff %s %s' % (dependencies_file, dependencies_tmp), cwd=None)
        self.left = []
        self.right = []

    def __call__(self, logger):
        if os.path.isfile(dependencies_tmp):
            self.logger = logger
            super().__call__(Logger().set_none())

        if self.left or self.right:
            self.logger.error('')
            self.logger.error('Dependency verification failed: ')

            if self.right:
                self.logger.error('')
                self.logger.error('The following dependencies are not present in the report: ')
                for dep in self.right:
                    self.logger.error('  ' + dep)

            if self.left:
                self.logger.error('')
                self.logger.error('The following dependencies are present in the report but no longer calculated: ')
                for dep in self.left:
                    self.logger.error('  ' + dep)

            self.logger.error('')
            self.logger.error('The runtime dependencies of jira-core have changed. You need to review and update '
                    '''dependencies.txt, and then make sure 'jmake unit-tests' passes.''')

            self.logger.info('Here is a quick one for you: to accept the changed dependencies, just run this: ')
            self.logger.info(' $ cp %s %s' % (dependencies_tmp, dependencies_file))
        return Callable.success

    def process_output(self, logger: Logger,  line : str, num : int):
        if line.startswith('< '):
            self.left.append(self.__strip_sha(line))
        elif line.startswith('> '):
            self.right.append(self.__strip_sha(line))
        return False

    def __strip_sha(self, line: str):
        return '|'.join(line[2:].split('|')[:2])


class JmakeUnitTest(JmakeModule):
    def __init__(self):
        super().__init__()
        self.command = 'jm-unit-test'
        self.description = 'Runs Unit Tests for jmake.'
        self.check_branch = False

    def __call__(self, args, executor):
        def runJmakeUnitTest(logger):
            if unittest.main(argv=['fake', 'discover', '-p', '%s*_test.py' % args.pattern, '-v'], exit=False, module=None).result.wasSuccessful():
                return 0
            else:
                logger.error('Jmake unit tests failed')
                return 1

        executor.append(runJmakeUnitTest)

    def define_parser(self, parser):
        parser.add_argument('pattern',
            help='pattern for running a specific test class(or classes). provide just the file name. '
                 'a "*_test.py" will be appended to the given pattern', nargs='?', default='')
        parser.epilog = ''

