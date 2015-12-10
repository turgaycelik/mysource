
from maven.Maven import MavenCallable
from maven.Surefire import Surefire
from module import JmakeModule


class Qunit(JmakeModule):
    def __init__(self, surefire: Surefire=None):
        super().__init__()
        self.command = 'qunit'
        self.description = 'Runs JIRA qunit tests.'
        self.surefire = surefire if surefire else Surefire()

    def __call__(self, args, executor):
        maven = MavenCallable(args)
        maven.project('jira-distribution/jira-webdriver-tests-runner').option('-am')
        maven.phase('integration-test').profile('distribution')
        maven.property('maven.test.unit.skip', 'true').property('maven.test.func.skip', 'true').property(
            'jira.security.disabled', 'true').property('dev.mode.plugins').property(
            'jira.functest.single.testclass', 'com.atlassian.jira.webtest.webdriver.qunit.TestQunit').property(
            'jira.minify.skip', 'true').property('func.mode.plugins').property('java.awt.headless', 'true')

        self.surefire.set_roots(maven.projects)
        executor.append(self.surefire.clean_executable())
        executor.append(maven)
        # because the qunit suite contains just the runner it will always be successful, unless the tests
        # cannot be ran. Thus, the report has to provide exit code (while it should run in post_commands):
        executor.append(self.surefire.report_executable())

    def define_parser(self, parser):
        MavenCallable.add_maven_switches(parser)

