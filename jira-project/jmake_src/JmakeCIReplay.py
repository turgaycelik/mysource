from functools import reduce
import os
import re
from xml.etree.ElementTree import ParseError
from CommandExecutor import Callable, SystemCallable
from Diagnostics import GitBranchDiscovery
from jbac.JBAC import JbacAuthentication, Jbac
import Logger
from maven.Maven import MavenCallable
from module import JmakeModule
from utils.FileUtils import FileUtils
from utils.UrlUtils import UrlUtils
from utils.XmlUtils import XmlUtils

class CIReplay(JmakeModule):

    def __init__(self, fileutils=FileUtils()):
        JmakeModule.__init__(self)
        self.command = 'replay'
        self.description = 'Replay JBAC failure locally. The most common use case is to replay a file prepared by ' \
                           '"ci investigate". The autocomplete should provide these filenames for your convenience. ' \
                           'You can use this to create a "local hallelujah client" to which you can supply a file ' \
                           'containing tests to run and it will run them as if they were read from the JMS queue.'
        self.fileutils = fileutils

    def __call__(self, args, executor):

        build_methods = { 'func' : CIReplay.process_functest,
                          'wd'   : CIReplay.process_webdriver }

        build_as = None

        for type in CIInvestigate.planname:
            if args.replay_file.startswith(CIInvestigate.planname[type]):
                build_as = type

        if args.force_func: build_as = 'func'
        if args.force_wd: build_as = 'wd'


        if build_as in build_methods:
            executor.append(lambda logger : logger.debug('Will build for: ' + build_as) or Callable.success)
            build_methods[build_as](args, executor, self.fileutils)
        else:
            executor.append(lambda logger : logger.error('Jmake could not determine the type of the build. Use --build-as-func or --build-as-wd.') or Callable.do_not_proceed)

    @staticmethod
    def process_functest(args, executor, fileutils = FileUtils()):

        #probably this can be improved somehow - ask Min'an.

        runner_dir = os.sep.join(['jira-distribution', 'jira-func-tests-runner', 'target'])
        if not args.quick:
            maven = MavenCallable()
            maven.phase('package').project('jira-distribution/jira-func-tests-runner').option('-am')
            maven.profile('distribution')
            maven.property('maven.test.skip', 'true').property('jira.minify.skip', 'true').property('func.mode.plugins')
            maven.property('reference.plugins').property('jira.func.tests.runner.create').property('obrRepository', 'NONE')
            maven.property('skipSources')
            executor.append(maven.can_run_in_parallel())

            unpack_script = os.path.abspath(os.sep.join(['jira-distribution', 'jira-func-tests-runner', 'src', 'main', 'dist', 'prepare-runner.sh']))
            executor.append(SystemCallable(args, unpack_script, cwd=runner_dir))

        maven = MavenCallable(path = runner_dir)
        maven.phase('verify').profile('hallelujahClient').property('jira.security.disabled', 'true')

        if fileutils.file_exists(args.replay_file):
            replay_file = os.path.abspath(args.replay_file)
        else:
            replay_file = os.path.abspath(os.sep.join(['target', 'replays', args.replay_file]))
        maven.property('hallelujah.local.test.list', replay_file)

        maven.property('system.bamboo.agent.home', os.path.abspath(runner_dir))
        executor.append(maven)

    @staticmethod
    def process_webdriver(args, executor, fileutils = FileUtils()):

        maven = MavenCallable()
        maven.project('jira-distribution/jira-webdriver-tests-runner').option('-am')
        maven.phase('verify').profile('hallelujahClient').profile('distribution')
        maven.property('maven.test.unit.skip', 'true')

        if fileutils.file_exists(args.replay_file):
            replay_file = os.path.abspath(args.replay_file)
        else:
            replay_file = os.path.abspath(os.sep.join(['target', 'replays', args.replay_file]))
        maven.property('jira.user.extra.jvmargs', '"-Dhallelujah.local.test.list=%s"' % replay_file)
        maven.property('func.mode.plugins').property('reference.plugins')

        maven.property('system.bamboo.agent.home', os.path.abspath(os.sep.join(['jira-distribution', 'jira-webdriver-tests-runner', 'target'])))
        executor.append(maven.can_run_in_parallel())

    def define_parser(self, parser):
        parser.add_argument('replay_file', type=str, metavar='replay-file',
                            help='Replay file produced with jmake CI investigate, or absolute path to a custom file.')
        parser.add_argument('--build-as-func', action='store_true', help='Force run as func test', dest='force_func')
        parser.add_argument('--build-as-wd', action='store_true', help='Force run as wd test', dest='force_wd')
        parser.add_argument('-q', '--quick', action='store_true', help='Do not compile the tests - attempt to run only',
                            dest='quick')

        self.sample_usage(parser, 'Replay a test batch locally using files produced by "ci investigate" (using autocomplete):',
                          ['./jmake ci replay [tab]'])
        self.sample_usage(parser, 'Replay a custom prepared batch as func (webdriver) in /tmp/mybatch:',
                          ['./jmake ci replay --run-as-func (--run-as-wd) /tmp/mybatch'])


        MavenCallable.add_maven_switches(parser)
        parser.autocomplete_contributor = lambda : os.listdir(os.sep.join(['target', 'replays']))


class JbacFailures:
    __client_pattern = re.compile('(?<=[\n\t ])[A-Z0-9]+-[A-Z0-9]+-[A-Z0-9]+-[0-9]+')

    def __init__(self, logger: Logger, build_name: str, build_number: str, urlutils=UrlUtils(), xml=XmlUtils(),
            auth=None):
        self.buildname = build_name
        self.buildnumber = build_number
        self.auth = auth
        self.logger = logger
        self.urlutils = urlutils
        self.xml = xml

    def __iter__(self):
        if self.auth is None:
            self.auth = JbacAuthentication.get()
        def failures_generator():
            url = self.__build_url()

            self.logger.info('Querying JBAC: ' + url)
            text = self.urlutils.read(url, self.auth.login, self.auth.password)
            self.logger.info('Got a reply.')

            try:
                root = self.xml.parse_string(text)
                for error in root.findall('.//failedTests/testResult'):
                    class_name = error.attrib['className']
                    method_name = error.attrib['methodName']
                    plan_name = self.__extract_plan_name(error.find('.//message').text)
                    if plan_name:
                        yield (plan_name, class_name + '.' + method_name)
            except ParseError:
                self.logger.error('Could not parse JBAC reply.')
                self.logger.debug('\n' + text)

        return failures_generator()

    def __build_url(self):
        return '%s/rest/api/latest/result/%s-%s?expand=testResults.failedTests.testResult.errors' % (Jbac.url, self.buildname, self.buildnumber)

    def __extract_plan_name(self, stacktrace: str):
        if '\n' in stacktrace:
            first_stacktrace_line = stacktrace[:stacktrace.find('\n')]
        else:
            first_stacktrace_line = stacktrace
        try:
            return reduce((lambda r, e : r), (m.group(0) for m in JbacFailures.__client_pattern.finditer(first_stacktrace_line)) )
        except TypeError:
            return None

class JbacTestExecution:

    # this will get simplified, because of the extra artifacts tests will produce.

    class_method_pattern = re.compile('[a-zA-Z0-9]+(\.[a-zA-Z0-9]+){4,30}')

    def __init__(self, plan_name: str, urlutils=UrlUtils()):
        self.auth = JbacAuthentication.get()
        self.plan_name = plan_name
        self.urlutils = urlutils

    def get_test_sequence(self):
        if not hasattr(self, 'sequence'):
            log = self.urlutils.read(self.__build_url(), self.auth.login, self.auth.password,
                filter=(lambda x : '===' in x and 'Finished' in x)).split('\n')

            self.sequence = [ match.group(0) for line in log for match in JbacTestExecution.class_method_pattern.finditer(line) ]
        return self.sequence

    def __build_url(self):
        return '%s/download/%s/build_logs/%s.log' % (Jbac.url, self.__extract_job_name(self.plan_name), self.plan_name)

    def __extract_job_name(self, plan_name: str):
        return plan_name[:plan_name.rfind('-')]


class CIInvestigate(JmakeModule):

    planname = {'func': 'MASTERONE-CIFUNCHAL',
                  'wd': 'MASTERONE-CIWEBDRIVER'}

    jobname = {'func': 'HALLELUJAHTESTSERVER',
                 'wd': 'WDSERVER'}

    def __init__(self, urlutils=UrlUtils(), xml=XmlUtils(), fileutils=FileUtils()):
        JmakeModule.__init__(self)
        self.command = 'investigate'
        self.description = 'Investigates JBAC failures and prepares to reproduce them locally. This command will ' \
                           'produce a file, that will contain information what tests failed and what should be done ' \
                           'to replay what happened locally. By default, the failed tests will be accompanied with ' \
                           '5 tests that preceded it on the particular run in an attempt to repliacate the way the ' \
                           'instance might have been corrupted by previous tests (which is random on regular rerun ' \
                           'due to how hallelujah works).'
        self.urlutils = urlutils
        self.xml = xml
        self.fileutils = fileutils
        self.replays_dir = self.fileutils.existing_dir(os.sep.join(['target', 'replays']))

    def get_build_name(self, logger, args, branchDiscovery, urlutils=UrlUtils(), xml=XmlUtils()):
        if branchDiscovery.branch == 'master':
            return self.__fulljobname(args.type)
        else:
            # translate the branch into bamboo branch name:
            bamboo_branch_name = branchDiscovery.branch.replace('/', '-')
            logger.info('Trying to find bamboo branch "%s"...' % bamboo_branch_name)

            auth = JbacAuthentication.get()
            url = '%s/rest/api/latest/plan/%s?expand=branches&max-results=10000' % (Jbac.url, CIInvestigate.planname[args.type])
            logger.info('Querying JBAC: ' + url)
            text = self.urlutils.read(url, auth.login, auth.password)

            try:
                root = self.xml.parse_string(text)
                for branchElement in root.findall('.//branches/branch'):
                    branch_key = branchElement.attrib['key']
                    branch_name = branchElement.attrib['shortName']
                    if branch_name == bamboo_branch_name:
                        logger.debug('Bamboo branch plan key is: "%s".' % branch_key)
                        return '-'.join([branch_key, CIInvestigate.jobname[args.type]])
            except ParseError:
                logger.debug('\n' + text)
                logger.error('Could not parse JBAC reply.')

            logger.warn('Could not find the Bamboo branch for branch: "%s". Will inspect master instead.' % bamboo_branch_name)
            return self.__fulljobname(args.type)

    def __call__(self, args, executor):

        if args.build_number == 0: args.build_number = 'latest'

        # we need the current branch because we might need to inspect a bamboo branch build.
        branchDiscovery = GitBranchDiscovery()
        executor.append(branchDiscovery)

        def inspect_jbac(logger):

            failed_hallelujah_builds = {}

            build_name = self.get_build_name(logger, args, branchDiscovery)
            for plan_name, test in JbacFailures(logger, build_name, args.build_number):
                if plan_name not in failed_hallelujah_builds:
                    tests = []
                    failed_hallelujah_builds[plan_name] = tests
                else:
                    tests = failed_hallelujah_builds[plan_name]
                tests.append(test)

            if not failed_hallelujah_builds:
                logger.info("No failures detected on JBAC")
                return 0


            build_number = None
            for plan_name in failed_hallelujah_builds:
                if build_number is None:
                    build_number = plan_name[plan_name.rfind('-') + 1:]
                logger.info("Found failures for: " + plan_name)
                for failed_test in failed_hallelujah_builds[plan_name]:
                    logger.info('  ' + failed_test + '()')

                logger.info("Inspecting " + plan_name + '...')
                test_sequence = JbacTestExecution(plan_name).get_test_sequence()

                for failed_test in failed_hallelujah_builds[plan_name]:
                    idx_to = test_sequence.index(failed_test) + 1
                    idx_from = idx_to - args.traceback if idx_to > args.traceback else 0
                    filename = os.sep.join([self.replays_dir, plan_name + '.' + failed_test + '.testlist'])
                    self.__save_to_file(filename, test_sequence[idx_from:idx_to], logger)

            filename = os.sep.join([self.replays_dir, CIInvestigate.planname[args.type] + '-' + build_number + '.all-failed-tests.testlist'])
            self.__save_to_file(filename, (t for plan, l in failed_hallelujah_builds.items() for t in l), logger)

            return Callable.success

        executor.append(inspect_jbac)

    def __fulljobname(self, type: str):
        return '-'.join([CIInvestigate.planname[type], CIInvestigate.jobname[type]])

    def __save_to_file(self, filename: str, iterable, logger: Logger):
        with open(filename, mode='w', encoding='utf-8') as f:
            f.write('\n'.join(iterable))
            logger.info('writing: ' + filename)

    def define_parser(self, parser):
        parser.add_argument('-t', '--traceback', type=int, default=6,
            help='Number of tests (including the failed one) in the run batch (for debugging tests that cause '
                 'corruption to instance state).',
            dest='traceback')
        parser.add_argument('-n', '--build-number', type=int, default=0, help='Build number, or 0 for latest',
            dest='build_number')
        parser.add_argument('type', choices=['func', 'wd'], default='func', help='webdriver or func tests')

        self.sample_usage(parser, 'Find out about failures on latest JBAC func/webdriver test build:',
                          ['./jmake ci investigate [func wd]'])
        self.sample_usage(parser, 'Find out about failures on specific build number on JBAC:',
                          ['./jmake ci investigate [func wd] --build-number 1234'])
        self.sample_usage(parser, 'Replay the failures locally (see more help):',
                          ['./jmake ci replay --help'])

        MavenCallable.add_maven_switches(parser)