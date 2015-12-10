from CommandExecutor import SystemCallable
from maven.Maven import MavenCallable
from module import JmakeModule

class Install(JmakeModule):
    def __init__(self):
        super().__init__()
        self.command = 'install'
        self.description = 'Builds JIRA BTF and installs the snapshots in your local repo.'

    def __call__(self, args, executor):
        maven = MavenCallable(args)
        maven.phase('install')
        maven.property('skipTests', 'true')
        executor.append(maven)


    def define_parser(self, parser):
        MavenCallable.add_maven_switches(parser)

