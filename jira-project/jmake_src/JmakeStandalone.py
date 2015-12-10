from CommandExecutor import SystemCallable
from maven.Maven import MavenCallable
from module import JmakeModule

class Standalone(JmakeModule):
    def __init__(self):
        super().__init__()
        self.command = 'standalone'
        self.description = 'Builds JIRA standalone distribution just as if you unpacked your behind-the-firewall product '\
                           'and run it as an option.'

    def __call__(self, args, executor):
        maven = MavenCallable(args)
        maven.project('jira-distribution/jira-standalone-distribution').option('-am')
        maven.phase('package').profile('distribution')
        maven.property('maven.test.unit.skip', 'true').property('maven.test.func.skip', 'true').property(
            'maven.test.selenium.skip', 'true')
        executor.append(maven)

        executor.append(SystemCallable(args, 'bin/unpackStandalone' + (' run' if args.run else '')))

    def define_parser(self, parser):
        MavenCallable.add_maven_switches(parser)
        parser.add_argument('-r', '--run', action='store_true', help='runs the unpacked JIRA package.')

