from maven.Maven import MavenCallable, MavenVersionInaccessibleException
from module import JmakeModule
from utils.ProcessUtils import ProcessUtils


class Findbugs(JmakeModule):
    def __init__(self, process_utils=ProcessUtils()):
        super().__init__()
        self.command = 'findbugs'
        self.description = 'Runs the findbugs analysis on your code. You can also access this from the unit-tests option.'
        self.process_utils = process_utils

    def __call__(self, args, executor):
        maven = MavenCallable(args, process_utils=self.process_utils)
        try:
            maven.require_mvn3()
            maven.project('jira-components/jira-core').option('-am').phase('process-classes').profile('findbugs')
            if not args.skip_bp: maven.project('jira-components/jira-plugins/jira-bundled-plugins')
            executor.append(maven)
        except MavenVersionInaccessibleException as e:
            executor.append(maven.mvn3_inaccessible_msg_callable('findbugs', e))


    def define_parser(self, parser):
        MavenCallable.add_maven_switches(parser)
        parser.add_argument('-nobp', '--skip_bundled_plugins', action='store_true',
            help='checking bundled plugins will be skipped.', dest='skip_bp')

