import os
from CommandExecutor import Callable
from Diagnostics import LocalPortInspector
from Logger import LOG
from BundledPluginsUtility import BundledPluginsUtility
from catalina.WorkspaceLayout import WorkspaceBuilder, WorkspaceLayout
from maven.Maven import MavenCallable
from module import JmakeModule
from catalina.ContextPreparer import ContextPreparer
from catalina.TomcatDownloader import TomcatDownloader
from catalina.TomcatRunner import TomcatStarter, TomcatStopper
from utils.FileUtils import FileUtils
from utils.XmlUtils import XmlUtils
from utils import PathUtils
from utils.WorkspaceUtils import WorkspaceValidator, WorkspaceUtils


DEFAULT_HTTP_PORT = 8090
DEFAULT_SHUTDOWN_PORT = 8099


class Run(JmakeModule):
    def __init__(self, stale_plugins=BundledPluginsUtility()):
        super().__init__()
        self.command = 'run'
        self.description = 'Runs JIRA using your code. This will download a tomcat for you, configure it and '\
                           'start JIRA on your local machine. This will not enable you to hotswap, JRebel or debug '\
                           'your running instance.'
        self.stale_plugins = stale_plugins
        self.prevent_post_commands = True

    def get_tasks_before_build_jira(self, args, executor):
        if args.with_workspace:
            executor.append(WorkspaceValidator())
        self.check_ports(args, executor)

        if args.setup_home:
            if args.postgres:
                executor.append(lambda logger: logger.error('Setup home not available for postgresql yet!'))
            elif args.mysql:
                executor.append(lambda logger: logger.error('Setup home not available for mysql yet!'))
            else:
                executor.append(SetupJiraHomeHsqldb(args))
        if args.postgres:
            executor.append(SetupPostgresqlConfig(args))
        elif args.mysql:
            executor.append(SetupMysqlConfig(args))

    def get_tasks_post_build_jira(self, args, executor):
        pass

    def __call__(self, args, executor):
        self.build_layouts(args)

        if args.control == 'shutdown':
            executor.append(TomcatStopper(args))
            return
        if args.control == 'restart':
            executor.append(TomcatStopper(args))


        # common preparation part for all commands running JIRA

        self.get_tasks_before_build_jira(args, executor)

        if args.control == 'quickstart':
            if hasattr(args, 'clustered') and args.clustered:
                executor.append(ContextPreparer(args))
            # just do nothing here as quickstart will require only common tasks
        elif args.control == '':
            executor.append(ensure_reloadable_resources)
            # check BUILDENG-4706 to understand why this plugin requires building JIRA twice
            if args.third_party_licensing:
                executor.append(self.__third_party_licensing(args))

            for task in self.get_tasks_to_build_jira(args): executor.append(task)

            executor.append(TomcatDownloader(args))
            executor.append(ContextPreparer(args))

        # now components that are common for all controls


        self.get_tasks_post_build_jira(args, executor)

        executor.append(process_local_test_settings(args))
        executor.append(TomcatStarter(args, self.debug()))

    def check_ports(self, args, executor):
        executor.append(LocalPortInspector(args.port, port_name='http port'))

        if args.sh_port == "auto":
            def set_sh_port(port):
                args.sh_port = port

            executor.append(LocalPortInspector(DEFAULT_SHUTDOWN_PORT, port_name='shutdown port').probe(set_sh_port))
        else:
            executor.append(LocalPortInspector(args.sh_port, port_name='shutdown port'))


    def define_parser(self, parser):
        parser.add_argument('-p', '--jira-port', type=int, default=DEFAULT_HTTP_PORT, help='The tomcat port for JIRA', dest='port')
        parser.add_argument('-sp', '--jira-shutdown-port', default='auto',
                            help='The tomcat shutdown port for JIRA (default is "auto" and will be scanned for, starting from %s)' % DEFAULT_SHUTDOWN_PORT,
                            dest='sh_port')
        parser.add_argument('-j', '--jira-home', default=WorkspaceLayout.JIRA_HOME_DEFAULT_MARKER,
                            help='JIRA home directory',
                            dest='jira_home')
        parser.add_argument('-c', '--jira-context', default='/jira', help='JIRA web application context',
                            dest='jira_context')
        parser.add_argument('-w', '--jira-webapp', default=WorkspaceLayout.JIRA_WEBAPP,
                            help='JIRA web application directory', dest='jira_webapp_dir')
        parser.add_argument('-d', '--tomcat-dir', default=WorkspaceLayout.TOMCAT_DOWNLOAD,
                            help='Directory where tomcat will be downloaded', dest='tomcat_dir')
        parser.add_argument('-v', '--tomcat-version', default=WorkspaceBuilder.TOMCAT7,
                            choices=[WorkspaceBuilder.TOMCAT6, WorkspaceBuilder.TOMCAT7],
                            help='Version of tomcat to start',
                            dest='tomcat_version')
        parser.add_argument('-bp', '--bundled-plugins', action='store_true',
                            help='If this flag is set bundled plugins will be added to jira exploded directory and used',
                            dest='bundled_plugins')
        parser.add_argument('-bw', '--build-war', action='store_true',
                            help='If this flag is set jira.war will be produced otherwise only exploded directory is assembled',
                            dest='build_war')
        parser.add_argument('-ws', '--with-workspace', action='store_true',
                            help='If this flag is set - build in workspace mode. '
                                 'Maven will be run in parent of jira directory. '
                                 'It is assumed there is some pom.xml there.',
                            dest='with_workspace')
        parser.add_argument('-as', '--attach-sources', action='store_true',
                            help='If this flag is set build will generate sources jar', dest='attach_sources')
        parser.add_argument('-jr', '--jrebel', action='store_true',
                            help='If this flag is set tomcat will be started with jrebel agent', dest='jrebel')
        parser.add_argument('-rp', '--ref-plugin', action='store_true',
                            help='If this flag is set reference plugins will be deployed with JIRA', dest='ref_plugins')
        parser.add_argument('-tp', '--third-party-licensing', action='store_true',
                            help='If this flag is set, the LGPL list in the about page will be created',
                            dest='third_party_licensing')
        parser.add_argument('-in', '--instance-name',
                            help='this will name your instance, which means you will be able to '
                                 'run and maintain multiple ones on different ports', dest='instance_name',
                            default='work')
        parser.add_argument('control', choices=['shutdown', 'restart', 'quickstart', ''], default='', nargs='?',
                            help='Controls the instance:\n' +
                                 'shutdown shuts the running instance, respects instance names.'
                                 'restart restarts the running instance.' +
                                 'quickstart starts the instance without building it.')
        parser.add_argument('--override-dev-mode', dest='override_dev_mode', action='store_true',
                            help='JIRA will be started in dev mode by default. this overrides the dev_mode setting.')
        parser.add_argument('-J', dest='extra_jvm_flags', action='append',
                            help='Pass flag directly to the tomcat JVM. ' +
                                 'Flags are appended to the end of other JVM args, so you can (for example) '
                                 'override memory settings with -J-Xmx2g .')
        parser.add_argument('--max-perm-size', dest='max_perm_size', type=str, default='384m',
                            help='Amount of PermGen memory for the tomcat process')
        parser.add_argument('--xmx', dest='xmx', type=str, default='1024m',
            help='Maximum memory setting.')

        parser.add_argument('--plugin-resources', dest='plugin_resources', type=str, default='',
            help='Add all resource sub-folders for the plugin to resources scanned for changes by JIRA')

        parser.add_argument('--disable-plugin-resources', dest='disable_plugin_resources', action='store_true',
            help='Do not use reloadable plugin resources (good for standalone-like testing)')

        parser.add_argument('--enable-mail', dest='enable_mail', action='store_true',
                            help='Start JIRA with the mail handlers enabled even if in dev mode.')
        parser.add_argument('-sh', '--setup-home', dest='setup_home', action='store_true',
                            help='Setup JIRA home directory so you can directly login as admin:admin. '
                                 'If current JIRA home directory is not empty this parameter has no effect')

        parser.add_argument('--postgres', dest='postgres', action='store_true',
                            help='Sets up JIRA with local postgresql db. See ./jmake postgres --help.')

        parser.add_argument('--mysql', dest='mysql', action='store_true',
                            help='Sets up JIRA with local mysql db. See ./jmake mysql --help.')

        parser.add_argument('--ssl', dest='ssl', action='store_true',
                            help='Sets up JIRA with ssl. You also need a key produced with java keytool. Bother lukasz w. to '
                                 'add more how-to here.')

        self.sample_usage(parser, 'To pass more than one argument with -J use:', [
            './jmake run -J-Dfoo=bar -J-Dfee=baz',
            './jmake run -J "-Dfoo=bar -Dfee=baz"'])

        self.sample_usage(parser, 'To skip the setup steps on clean jirahome using hsqldb (will not work with real db):', [
            './jmake run --setup-home',
            './jmake debug --setup-home'
        ])

        MavenCallable.add_maven_switches(parser)

    def build_layouts(self, args):
        args.layout = WorkspaceBuilder.buildLayout(args.tomcat_dir, args.tomcat_version, args.jira_home,
                                                   args.instance_name, False)

    def debug(self):
        return False

    def get_tasks_to_build_jira(self, args, workspace_utils=WorkspaceUtils()):
        #for workspace build maven must be executed from parent directory
        maven = MavenCallable(args)
        maven.phase('package')

        if not args.build_war:
            maven.property('jira.do.not.prepare.war')

        maven.project('jira-components/jira-webapp').option('-am')
        maven.property('jira.home', args.layout.jira_home())

        maven.projects.extend(workspace_utils.get_workspace_projects_without_jira(args))
        self.add_plugins_maven_goals(args, maven)
        if not args.attach_sources:
            maven.property('skipSources')
        maven.skip_tests()
        if args.third_party_licensing:
            maven.profiles.extend(['third-party-licensing'])

        # return builder and the bundled plugins
        return [maven, lambda logger: self.stale_plugins.remember_plugins_profiles(maven.profiles) or 0]

    def __third_party_licensing(self, args):

        licensing_maven_executable = MavenCallable(args)
        licensing_maven_executable.phase('verify').phases.extend(['license:bom', 'license:download'])
        licensing_maven_executable.profiles.extend(['distribution', '!build-source-distribution'])
        licensing_maven_executable.property('maven.test.skip', 'true')
        return licensing_maven_executable

    def add_devmode_plugins(self, args, maven):
        profiles_for_plugins = ['func-mode-plugins', 'pseudo-loc', 'dev-mode-plugins']
        if args.ref_plugins:
            profiles_for_plugins.append('reference-plugins')
        maven.profiles.extend(profiles_for_plugins)
        return profiles_for_plugins

    def add_plugins_maven_goals(self, args, maven):
        profiles_for_plugins = self.add_devmode_plugins(args, maven)

        if not args.bundled_plugins:
            maven.property('jira.exclude.bundled.plugins')

        if args.mvn_clean:
            LOG.info('Clean parameter present recompiling bundled plugins')
            modules_to_build = self.stale_plugins.get_bundled_plugins_module()
        else:
            modules_to_build = self.stale_plugins.find_plugins_to_recompile(profiles_for_plugins)

        for module in modules_to_build:
            maven.project(module)

        if len(modules_to_build) == 0:
            LOG.info('Bundled plugins up to date skipping compilation')


def process_local_test_settings(args, fileutils=FileUtils()):
    def process_local_test_settings_closure(logger):
        logger.info('Preparing local test settings for your new instance...')
        template = os.sep.join(['jira-func-tests', 'src', 'main', 'resources', 'localtest.template'])

        template_renderings = {'jira-func-tests': 'jira-func-tests',
                               'jira-webdriver-tests': 'jira-webdriver-tests',
                               os.sep.join(['jira-distribution', 'jira-integration-tests']): 'jira-func-tests'}

        for project, xml_location in template_renderings.items():
            dir = fileutils.existing_dir(os.sep.join(['.', project, 'src', 'main', 'resources']))
            dest = os.sep.join([dir, 'localtest.properties'])

            # just for unit tests this settings dict is not reused:
            settings = {'${jira.port}': str(args.port),
                        '${jira.context}': args.jira_context,
                        '${test.xml.location}': PathUtils.abspath(xml_location)}

            logger.debug('Processing ' + template + ' to ' + dest)
            fileutils.filter_file(template, dest, settings)
        return Callable.success

    return process_local_test_settings_closure


def ensure_reloadable_resources(logger, xml=XmlUtils()):
    artifact_file = os.sep.join(['.idea', 'artifacts', 'atlassian_jira_webapp_war_exploded.xml'])
    try:
        tree = xml.parse(artifact_file)
    except IOError:
        return Callable.success

    webinf_element = xml.produce(tree.getroot(),
                                 ('artifact', {}),
                                 ('root', {}),
                                 ('element', {'id': 'directory', 'name': 'WEB-INF'}),
                                 ('element', {'id': 'directory', 'name': 'classes'}))

    modules = ['jira-api', 'jira-core']
    needs_save = False

    for module in modules:
        if not xml.child_exists(webinf_element, 'element', {'name': module}):
            xml.produce(webinf_element, ('element', {'id': 'module-output', 'name': module}))
            needs_save = True

    if needs_save:
        logger.debug('Updating JIRA artifact to enable resources reload...')
        try:
            tree.write(artifact_file)
        except IOError:
            logger.error('Could not save ' + artifact_file)
            return Callable.success - 2

    return Callable.success


class SetupJiraHomeHsqldb:
    JIRA_HOME_ZIP = os.sep.join(['jmake_src', 'data', 'jirahome.zip'])
    DB_CONFIG_TEMPLATE = 'dbconfig-template.xml'
    DB_CONFIG = 'dbconfig.xml'
    DB_SCRIPT_TEMPLATE = 'jiradb-template.script'
    DB_SCRIPT = 'jiradb.script'
    DB_DIRECTORY = 'database'

    def __init__(self, args, file_utils: FileUtils=FileUtils()):
        super().__init__()
        self.args = args
        self.fs = file_utils

    def __call__(self, logger):
        jira_home = self.args.layout.jira_home()
        if self.fs.file_exists(jira_home):
            logger.warn('Directory "%s" already exists, leaving as it is. '
                        'If you want clean instance please remove "%s".' % (jira_home, jira_home))
        else:
            logger.info('Unpacking %s into %s' % (SetupJiraHomeHsqldb.JIRA_HOME_ZIP, jira_home))
            self.fs.extract_zip(SetupJiraHomeHsqldb.JIRA_HOME_ZIP, jira_home)
            db_config_template = os.sep.join([jira_home, SetupJiraHomeHsqldb.DB_CONFIG_TEMPLATE])

            self.fs.filter_file(db_config_template,
                                os.sep.join([jira_home, SetupJiraHomeHsqldb.DB_CONFIG]),
                                {'${jirahome}': jira_home})
            self.fs.remove(db_config_template)

            db_script_template = os.sep.join([jira_home, SetupJiraHomeHsqldb.DB_DIRECTORY, SetupJiraHomeHsqldb.DB_SCRIPT_TEMPLATE])
            self.fs.filter_file(db_script_template,
                                os.sep.join([jira_home, SetupJiraHomeHsqldb.DB_DIRECTORY, SetupJiraHomeHsqldb.DB_SCRIPT]),
                                {'${jirahome}': jira_home})
            self.fs.remove(db_script_template)

        return Callable.success


class SetupPostgresqlConfig:

    DB_CONFIG = 'dbconfig.xml'
    DB_CONFIG_TEMPLATE = os.sep.join(['.', 'jmake_src', 'data', 'dbconfig-postgresql.xml'])

    def __init__(self, args, file_utils: FileUtils=FileUtils()):
        super().__init__()
        self.args = args
        self.fs = file_utils

    def __call__(self, logger):
        jira_home = self.args.layout.jira_home()
        logger.info('Preparing postgresql configuration...')
        self.fs.copy_file(SetupPostgresqlConfig.DB_CONFIG_TEMPLATE,
                          os.sep.join([self.fs.existing_dir(jira_home), SetupJiraHomeHsqldb.DB_CONFIG]))
        return Callable.success

class SetupMysqlConfig:

    DB_CONFIG = 'dbconfig.xml'
    DB_CONFIG_TEMPLATE = os.sep.join(['.', 'jmake_src', 'data', 'dbconfig-mysql.xml'])

    def __init__(self, args, file_utils: FileUtils=FileUtils()):
        super().__init__()
        self.args = args
        self.fs = file_utils

    def __call__(self, logger):
        jira_home = self.args.layout.jira_home()
        logger.info('Preparing mysql configuration...')
        self.fs.copy_file(SetupMysqlConfig.DB_CONFIG_TEMPLATE,
                          os.sep.join([self.fs.existing_dir(jira_home), SetupJiraHomeHsqldb.DB_CONFIG]))
        return Callable.success

