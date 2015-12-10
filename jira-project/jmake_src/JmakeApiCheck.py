from maven.Maven import MavenCallable, MavenVersionInaccessibleException
from module import JmakeModule

class ApiCheck(JmakeModule):
    def __init__(self):
        super().__init__()
        self.command = 'api-check'
        self.description = 'Runs the API checker on the jira-api.'

    def __call__(self, args, executor):
        maven = MavenCallable(args)
        try:
            maven.require_mvn3()
            maven.project('jira-components/jira-api').option('-am').phase('verify').property('performApiCheck')
            executor.append(maven)

        except MavenVersionInaccessibleException as e:
            executor.append(maven.mvn3_inaccessible_msg_callable('api-check', e))

    def define_parser(self, parser):
        MavenCallable.add_maven_switches(parser)
