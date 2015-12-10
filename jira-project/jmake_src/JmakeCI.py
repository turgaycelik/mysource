import os
import re
from CommandExecutor import Callable
from JmakeCIReplay import CIInvestigate, CIReplay
from maven.Maven import MavenCallable
from module import JmakeModule


class CI(JmakeModule):
    def __init__(self):
        JmakeModule.__init__(self)
        self.command = 'ci'
        self.description = 'Runs CI related tasks. You may run batches of funk and webdriver tests. This will evolve '\
                           'into resembling Bamboo builds as closely as possible, but not just yet.'

    def get_submodules(self):
        return [CIFuncTest(), CIJobs(), CISingleTest(), CIWebdriverTest(), CIInvestigate(), CIReplay()]


class CIFuncTest(JmakeModule):
    def __init__(self):
        JmakeModule.__init__(self)
        self.command = 'func'
        self.description = 'Run Func tests'

    def __call__(self, args, executor):
        #executor.append(build_runner(args))
        args.mvn_clean = False
        for batch_number in range(1, args.func_batches + 1):
            executor.append(batch_func_test_runner(args, batch_number, {}))

    def define_parser(self, parser):
        parser.add_argument('-bf', '--func-batch-number', type=int, default=20, help='Number of batches',
            dest='func_batches')
        MavenCallable.add_maven_switches(parser)


class CIJobs(JmakeModule):
    def __init__(self):
        JmakeModule.__init__(self)
        self.command = 'jobs'
        self.description = 'Run specific job'

    def __call__(self, args, executor):
        args.mvn_clean = False
        for job in args.job_name:
            executor.append(self.job_runner(job, args))

    def define_parser(self, parser):
        parser.add_argument('job_name', nargs='+', help='Job names to run')
        parser.add_argument('-bf', '--func-batch-number', type=int, default=20, help='Total number of func batches',
            dest='func_batches')
        MavenCallable.add_maven_switches(parser)

    def job_runner(self, job_name, args):
        if job_name == 'COMPILE':
            return build_runner(args)
        elif job_name == 'FUNCUNIT':
            return func_unit_test_runner(args)
        elif job_name == 'UALINT':
            return ual_int_func_test_runner(args)

        match = re.match(r'([A-Z]+)(\d+)', job_name)
        if match is None:
            return self.print_allowed_jobs_info()
        if match.group(1) == 'FUNC':
            return batch_func_test_runner(args, int(match.group(2)), {})
        else:
            return self.print_allowed_jobs_info()

    def print_allowed_jobs_info(self):
        def allowed_jobs_info_closure(logger):
            logger.error('The only allowed jobs are COMPILE FUNCxx FUNCUNIT UALINT')
            return Callable.do_not_proceed

        return allowed_jobs_info_closure


class CISingleTest(JmakeModule):
    def __init__(self):
        JmakeModule.__init__(self)
        self.command = 'single'
        self.description = 'Run single func test'

    def __call__(self, args, executor):
        maven = build_runner(args)
        maven.project('jira-func-tests')
        maven.option('-am')
        maven.property('test', ','.join(args.test_class))
        maven.property('failIfNoTests', 'false')
        maven.profile('jmake-func-tests')
        maven.can_run_in_parallel()
        executor.append(maven)

    def single_test_runner(self, args, test_class):
        return batch_func_test_runner(args, 1, {'jira.functest.single.testclass': test_class})

    def define_parser(self, parser):
        parser.add_argument('test_class', nargs='+', help='Test class to run')
        MavenCallable.add_maven_switches(parser)

class CIWebdriverTest(JmakeModule):
    def __init__(self):
        JmakeModule.__init__(self)
        self.command = 'webdriver'
        self.description = 'Runs WebDriver tests on the last JIRA instance ran by ./jmake run or ./jmake debug.'

    def __call__(self, args, executor):
        executor.append(webdriver_test_runner(args))

    def define_parser(self, parser):
        parser.add_argument('-t', '--tests', help='Run only given webdriver tests', dest='tests')
        MavenCallable.add_maven_switches(parser)


def build_runner(args):
    maven = MavenCallable(args)
    maven.phase('test')
    maven.properties.update({'maven.test.unit.skip': 'true',
                             'maven.test.func.skip': 'true',
                             'maven.test.selenium.skip': 'true'})
    return maven


def batch_func_test_runner(args, batch_number, additional_options=None):
    if not additional_options: additional_options = {}
    batch_total = 1 if 'func_batches' not in args else args.func_batches
    options = {'maven.test.unit.skip': 'true',
               'jira.security.disabled': 'true',
               'atlassian.test.suite.numbatches': batch_total,
               'atlassian.test.suite.batch': batch_number,
               'jira.minify.skip': 'true',
               'func.mode.plugins': None,
               'reference.plugins': None
    }
    options.update(additional_options)
    return general_func_test_runner(args, options)


def func_unit_test_runner(args):
    options = {'maven.test.func.skip': 'true',
               'jira.security.disabled': 'true',
               'jira.minify.skip': 'true'}
    return general_func_test_runner(args, options)


def general_func_test_runner(args, additional_options=None):
    if not additional_options: additional_options = {}
    maven = MavenCallable(args)
    maven.projects = ['jira-distribution/jira-func-tests-runner']
    maven.option('-am')
    maven.profile('distribution')
    maven.phase('verify')
    maven.properties.update(additional_options)
    maven.property('system.bamboo.agent.home', os.path.abspath(os.sep.join(['jira-distribution', 'jira-func-tests-runner', 'target'])))
    return maven


def ual_int_func_test_runner(args):
    maven = MavenCallable(args)
    maven.projects = ['jira-distribution/jira-webapp-dist', 'jira-distribution/jira-integration-tests']
    maven.option('-am')
    maven.profile('distribution')
    maven.phase('verify')
    maven.properties.update({'maven.test.unit.skip': 'true',
                             'jira.security.disabled': 'true',
                             'java.awt.headless': 'true',
                             'jira.minify.skip': 'true',
                             'func.mode.plugins': 'true'})
    return maven


def webdriver_test_runner(args):
    maven = MavenCallable(args)
    maven.projects = ['jira-webdriver-tests']
    maven.option('-am')
    maven.profile('jmake-webdriver-tests')
    maven.phase('test')
    maven.properties.update({'failIfNoTests': 'false',
                             'maven.test.unit.skip': 'true',
                             'system.bamboo.agent.home': os.path.abspath(os.sep.join(['jira-distribution', 'jira-webdriver-tests-runner', 'target']))
    })
    if args.tests is not None:
        maven.properties.update({'test': args.tests})
    return maven

