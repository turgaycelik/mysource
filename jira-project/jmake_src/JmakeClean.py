from CommandExecutor import SystemCallable, Callable
from Logger import Logger
from catalina.WorkspaceLayout import WorkspaceLayout
from maven.Maven import MavenCallable
from module import JmakeModule
from utils.DataBean import DataBean
from utils.FileUtils import FileUtils
from utils.WorkspaceUtils import WorkspaceValidator


class Clean(JmakeModule):
    def __init__(self, fileutils=FileUtils()):
        super().__init__()
        self.command = 'clean'
        self.description = 'Cleans built artifacts in workspace. Provides options for cleaning downloaded tomcat '\
                           'binaries, JIRA home directory or modules that are not enabled. Unless you use the "deep '\
                           'clean" option, this will generate sources you need for proper compilation after cleaning '\
                           'target directories.'
        self.prevent_post_commands = True
        self.fileutils = fileutils
        self.check_branch = False

    def remove_if_present(self, args, executor, directory):
        if self.fileutils.file_exists(directory):
            executor.append(SystemCallable(args, 'rm -rf ' + directory))

    def __call__(self, args, executor):
        if args.with_workspace:
            executor.append(WorkspaceValidator())
        maven = MavenCallable(args)
        maven.phase('clean').profile('jmake')
        executor.append(maven)

        if args.jirahome:
            self.remove_if_present(args, executor, WorkspaceLayout.JIRA_HOME)
            self.remove_if_present(args, executor, WorkspaceLayout.JIRA_OD_HOME)
            self.remove_if_present(args, executor, WorkspaceLayout.JIRA_CLUSTERED_HOME_ROOT)
            self.remove_if_present(args, executor, WorkspaceLayout.JIRA_SHARED_HOME_ROOT)
        if args.tomcat:
            if self.fileutils.file_exists(WorkspaceLayout.TOMCAT_DOWNLOAD):
                executor.append(SystemCallable(args, 'rm -rf ' + WorkspaceLayout.TOMCAT_DOWNLOAD))

        if args.deep:
            executor.append(SystemCallable(args, 'find {0} -type d -name target | xargs rm -rf'
                    .format(self.fileutils.get_parent_dir_path() if args.with_workspace else '.')))
        else:
            executor.append(self.generate_jira_core_sources(args))

    def generate_jira_core_sources(self, args):
        def generate_jira_core_sources_closure(log: Logger):
            workspace_args = DataBean()
            workspace_args.with_workspace = args.with_workspace

            # attempt to compile jira-core in offline mode - assume jira-api is installed.
            jira_core_gen_src = MavenCallable(workspace_args).phase('generate-sources').skip_tests().project('jira-components/jira-core').option('-o')
            jira_core_gen_src(log)
            if jira_core_gen_src.returncode == Callable.success:
                return jira_core_gen_src.returncode
            else:
                # something failed: maybe jira-api was not actually installed? fix this:
                log.warn('Generate sources failed for jira-core. Will attempt to compile and install jira-api before giving up.')
                jira_api_install = MavenCallable(workspace_args).phase('install').skip_tests().project('jira-components/jira-api')
                jira_api_install(log)
                if jira_api_install.returncode != Callable.success:
                    return jira_api_install.returncode
                jira_core_gen_src.returncode = None
                jira_core_gen_src(log)
                return jira_core_gen_src.returncode

        return generate_jira_core_sources_closure

    def define_parser(self, parser):
        parser.add_argument('-d', '--deep', help='cleans all target directories', dest='deep', action='store_true')
        parser.add_argument('-j', '--jira-home', help='also cleans jira-home directory', dest='jirahome',
            action='store_true')
        parser.add_argument('-t', '--tomcat', help='also cleans all downloaded tomcats', dest='tomcat',
            action='store_true')
        parser.add_argument('-ws', '--with-workspace', action='store_true',
            help='If this flag is set - build in workspace mode. Maven will be run in parent of jira directory. '
                 'It is assumed there is some pom.xml there.',
            dest='with_workspace')
        parser.epilog = ''

class CleanAll(Clean):
    def __init__(self, fileutils=FileUtils()):
        super().__init__(fileutils)
        self.command = 'cleanall'
        self.description = 'Most thorough clean provided by jmake. '
        self.prevent_post_commands = True
        self.fileutils = fileutils
        self.check_branch = False

    def __call__(self, args, executor):
        args.jirahome = True
        args.tomcat = True
        args.deep = True
        return super().__call__(args, executor)

    def define_parser(self, parser):
        parser.add_argument('-ws', '--with-workspace', action='store_true',
                            help='If this flag is set - clean in workspace mode. '
                                 'Maven will be run in parent of jira directory. '
                                 'It is assumed there is some pom.xml there.',
                            dest='with_workspace')
        parser.epilog = ''