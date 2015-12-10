import os
from CommandExecutor import SystemCallable, Callable
from JmakeDebug import Debug
from JmakeRun import Run
from catalina.WorkspaceLayout import WorkspaceBuilder
from maven.Maven import MavenCallable
from module import JmakeModule
from ondemand.HordeLayout import HordeLayout
from ondemand.HordeRunner import HordeRunner
from ondemand.HordeStatusChecker import HordeStatusEnforcer
from ondemand.LegacyOdSvnSetup import LegacyOdSvnSetup
from ondemand.ManifestoUtils import ManifestoUtils
from utils.FileUtils import FileUtils
from utils.WorkspaceUtils import WorkspaceUtils, WorkspaceValidator

OD_WEBAPP_PROJECT_DIR = os.sep.join(['jira-ondemand-project', 'jira-ondemand-webapp'])


class OnDemandInstall(JmakeModule):
    def __init__(self):
        super().__init__()
        self.command = 'install'
        self.description = 'Build and install (to your local maven repository) the OnDemand web application.'

    def __call__(self, args, executor):
        OnDemandInstall.define_install_actions(args, executor)

    @staticmethod
    def define_install_actions(args, executor):
        if args.quick:
            maven = MavenCallable(args, 'jira-ondemand-project')
            maven.phase('install').skip_tests().profile('func-mode-plugins')
            executor.append(maven.require_mvn2())
        else:
            maven = MavenCallable(args)
            maven.phase('install')
            maven.project('jira-ondemand-project/jira-ondemand-webapp') \
                .project('jira-ondemand-project/jira-ondemand-acceptance-tests').option('-am')
            maven.skip_tests().profile('ondemand').profile('func-mode-plugins')
            executor.append(maven.require_mvn2())

    def define_parser(self, parser):
        MavenCallable.add_maven_switches(parser)
        parser.add_argument('-q', '--quick',
                            help='executes quick build of OnDemand. JIRA core components will be picked up '
                                 '''from some location that only Maven knows (or more likely doesn't)''',
                            action='store_true')


class OnDemandRun(Run):

    template_file = os.sep.join(['.', 'jmake_src', 'data', 'download_poms_pom_template.xml'])

    def __init__(self):
        super().__init__()
        self.description = 'Runs JIRA-only OD instance using your code. This will download a tomcat for you, configure ' \
                           'it and start JIRA and Horde on your local machine. This will also download plugins from ' \
                           'manifesto from given zone/hash (manifesto required you to be in the ofice or connect via ' \
                           'VPN). This will not enable you to hotswap, JRebel or debug your running instance.'
        self.manifesto = ManifestoUtils()

    def __call__(self, args, executor):
        if args.setup_home:
            executor.append(lambda log: log.warn('Setup home option is not available for ondemand.') or 0)
        if args.bundled_plugins:
            executor.append(lambda log: log.warn('Bundled plugins option is implicitly on with ondemand.') or 0)

        super().__call__(args, executor)

    def define_parser(self, parser):
        super().define_parser(parser)
        parser.add_argument('--manifesto-hash', '-mh', action='store', default='default', dest='manifesto_hash',
                            help='Get plugins using a manifesto hash, zone or alias. Should handle most conventions: '
                                 '"dev", "dog", "prod", "jirastudio-dev", etc., or explicit hash. Case insensitive. '
                                 'Defaults to latest dev.')

        parser.add_argument('--manifesto-jira', action='store_true', dest='manifesto_jira',
                            help='Will not build JIRA but will take it from the given manifesto hash using '
                                 '--manifesto-hash parameter. Too bad JIRA repo must be cloned for this...')

        parser.add_argument('--ignore-manifesto-errors', action='store_true', dest='ignore_manifesto_errors',
                            help='Will not stop the build if manifesto calls are unsuccessful. Will start without'
                                 'manifesto driven PSD instead.')

        self.sample_usage(parser, 'To run an instance that uses your code for JIRA, but uses plugins from manifesto hash 123456abcdef:',
                          ['./jmake ondemand run --manifesto-hash 123456abcdef'])

        self.sample_usage(parser, 'To run an instance that uses JIRA and plugins from current DOG configuration:',
                          ['./jmake ondemand run --manifesto-hash DOG --manifesto-jira'])
        #we should consider using sparse checkouts see
        #http://stackoverflow.com/questions/600079/is-there-any-way-to-clone-a-git-repositorys-sub-directory-only

        parser.autocomplete_contributor = lambda : self.manifesto.generate_all_zones_and_aliases()


    #Override
    def get_tasks_to_build_jira(self, args, workspace_utils=WorkspaceUtils()):

        if not args.manifesto_jira:
            #for workspace build maven must be executed from parent directory
            maven = MavenCallable(args)
            maven.phase('package')

            maven.profile('ondemand').project(OD_WEBAPP_PROJECT_DIR).option('-am')
            maven.property('jira.home', args.layout.jira_home())

            maven.projects.extend(workspace_utils.get_workspace_projects_without_jira(args))
            self.add_devmode_plugins(args, maven)

            if not args.attach_sources:
                maven.property('skipSources')
            maven.skip_tests()
            if args.third_party_licensing:
                maven.profiles.extend(['third-party-licensing'])

            # return builder and manifesto plugins job:
            return [maven]
        else:

            mavenClean = MavenCallable(args, path=OD_WEBAPP_PROJECT_DIR)
            mavenClean.phase('clean')

            return [mavenClean,
                    self.manifesto_jira(args, manifesto=self.manifesto),
                    self.__create_target_dir_for_exploded_webapp(),
                    SystemCallable(args, command='unzip *.war -d war/exploded', cwd=os.sep.join([OD_WEBAPP_PROJECT_DIR, 'target']))]

    #Override
    def get_tasks_before_build_jira(self, args, executor):
        if args.with_workspace:
            #TODO: untested.
            executor.append(WorkspaceValidator())

        self.check_ports(args, executor)

        executor.append(LegacyOdSvnSetup(args))
        executor.append(HordeRunner(args))

    #Override
    def get_tasks_post_build_jira(self, args, executor):
        executor.append(self.manifesto_plugins(args, manifesto=self.manifesto))
        executor.append(HordeStatusEnforcer(args))

    #Override
    def build_layouts(self, args):
        """ This is called by superclass to build the directory layouts for the needed webapps. """
        args.layout = WorkspaceBuilder.buildLayout(args.tomcat_dir, args.tomcat_version, args.jira_home,
                                                   args.instance_name, True)
        args.horde_layout = HordeLayout(args.layout.jira_home())

    def handle_manifesto_exception(self, args, message, e, logger):
        logger.error(message)
        if args.ignore_manifesto_errors:
            args.manifesto_psd = None
            logger.debug(str(e))
            return Callable.success
        else:
            logger.error(str(e))
            return Callable.failure

    def manifesto_jira(self, args, manifesto: ManifestoUtils=ManifestoUtils(), fs: FileUtils=FileUtils()):

        def manifesto_jira_closure(logger):

            webapp_target_dir = fs.existing_dir(os.sep.join([OD_WEBAPP_PROJECT_DIR, 'target']))

            logger.info('Determining the manifesto hash...')
            try:
                manifesto_hash = manifesto.determine_hash(args.manifesto_hash)
            except Exception as e:
                return self.handle_manifesto_exception('Unable to resolve zone to hash.', args, e, logger)
            logger.info('Will setup the OD instance with the following hash: ' + manifesto_hash)

            logger.info('Determining manifest JIRA version...')
            try:
                manifesto_jira = manifesto.get_od_jira(manifesto_hash)
            except Exception as e:
                return self.handle_manifesto_exception('Unable to determine jira version from manifesto.', args, e,
                                                       logger)
            logger.info('Will use JIRA webapp version: %s.' % manifesto_jira['version'])

            logger.info('Downloading JIRA...')
            try:
                fs.filter_file(OnDemandRun.template_file,
                               os.sep.join([webapp_target_dir, 'pom.xml']),
                               {'${ARTIFACT_ITEMS}': self.__artifact_description_for_pom_template(manifesto_jira)})

                rc = MavenCallable(path=webapp_target_dir).phase('verify').option('-B')(logger)
            except Exception as e:
                return self.handle_manifesto_exception('Unable to download required plugins.', args, e, logger)
            return rc

        return manifesto_jira_closure

    def manifesto_plugins(self, args, manifesto: ManifestoUtils=ManifestoUtils(), fs: FileUtils=FileUtils()):

        def download_pom_location(manifesto_hash):
            return fs.abs_path(os.sep.join(['.', 'target', 'manifesto', 'psd-' + manifesto_hash]))

        def manifesto_plugins_closure(logger):
            logger.info('Determining the manifesto hash...')
            try:
                manifesto_hash = manifesto.determine_hash(args.manifesto_hash)
            except Exception as e:
                return self.handle_manifesto_exception('Unable to resolve zone to hash.', args, e, logger)
            logger.info('Will setup the OD instance with the following hash: ' + manifesto_hash)

            logger.info('Determining plugin list...')
            try:
                manifesto_plugins = manifesto.get_plugins_maven_artifacts(manifesto_hash)
            except Exception as e:
                return self.handle_manifesto_exception('Unable to determine required plugins from manifesto.', args, e,
                                                       logger)
            logger.info('Found %d plugins to install.' % len(manifesto_plugins))

            logger.info('Downloading plugins...')
            try:
                fs.filter_file(OnDemandRun.template_file,
                               os.sep.join([fs.existing_dir(download_pom_location(manifesto_hash)), 'pom.xml']),
                               {'${ARTIFACT_ITEMS}': ''.join(
                                   self.__artifact_description_for_pom_template(v) for k, v in manifesto_plugins.items())})

                rc = MavenCallable(path=download_pom_location(manifesto_hash)).phase('verify').option('-B')(logger)
            except Exception as e:
                return self.handle_manifesto_exception('Unable to download required plugins.', args, e, logger)
            args.manifesto_psd = download_pom_location(os.sep.join([manifesto_hash, 'target']))

            return rc

        return manifesto_plugins_closure

    def __create_target_dir_for_exploded_webapp(self, fs: FileUtils=FileUtils()):
        def create_target_dir_for_exploded_webapp_closure(_):
            fs.existing_dir(os.sep.join([OD_WEBAPP_PROJECT_DIR, 'target', 'war', 'exploded']))
            return 0
        return create_target_dir_for_exploded_webapp_closure

    def __artifact_description_for_pom_template(self, artifact):

        def in_tag(tagname, text):
            return '<{0}>{1}</{0}>'.format(tagname, text)

        return in_tag('artifactItem', ''.join([
            in_tag('groupId', artifact['groupId']),
            in_tag('artifactId', artifact['artifactId']),
            in_tag('version', artifact['version']),
            in_tag('outputDirectory', './target'),
            in_tag('type', artifact['packaging'])
        ]))


class OnDemandDebug(Debug, OnDemandRun):
    def __init__(self):
        super().__init__()
        self.description = 'Runs JIRA using your code. This will download a tomcat for you, configure it and start ' \
                           'JIRA-Only OD instance with Horde on your local machine. To debug it, use the default ' \
                           '"Remote" configuration from your IDEA (port 5005). Once connected this will hot-swap ' \
                           'code changes into your running JIRA (for core, not for plugins).'

    def define_parser(self, parser):
        super().define_parser(parser)
        self.sample_usage(parser, 'To debug your local code with the set of manifesto driven plugins from DEV:',
                          ['./jmake ondemand debug'])


class OnDemandDeploy(JmakeModule):
    def __init__(self):
        super().__init__()
        self.command = 'deploy'
        self.description = 'Build and deploy JIRA OD to unicorn. You should have you unicorn instance setup for local ' \
                           'development, If not read https://extranet.atlassian.com/x/kpOddQ'

    def __call__(self, args, executor):
        deploy_script_path = os.getcwd() + '/../ondemand-fireball/scripts/unicorn-deploy/dev-deploy.py'
        executor.append(lambda logger: logger.info('Checking if the deploy script is accessible.') or Callable.success)
        executor.append(lambda logger: Callable.success if os.path.isfile(deploy_script_path) else
        logger.error('Could not find the dev-deploy script. Ensure that it is located at ' + deploy_script_path) or
        Callable.do_not_proceed)

        if args.resolve_artifacts or args.mvn_clean:
            maven = MavenCallable(args, 'jira-ondemand-project')
            maven.require_mvn2()
            maven.project('jira-ondemand-webapp').option('-am')

            if args.resolve_artifacts:
                maven.phase('package').skip_tests()
            executor.append(maven)

        maven = MavenCallable(args)
        maven.phase('install').property('maven.test.skip')
        maven.project('jira-components/jira-webapp').option('-am')
        executor.append(maven.require_mvn2())

        script = SystemCallable(args,
                                '{0} --instance {1} --path jira-ondemand-webapp'.format(deploy_script_path,
                                                                                        args.deploy_host),
                                cwd='jira-ondemand-project')

        executor.append(script)

    def define_parser(self, parser):
        MavenCallable.add_maven_switches(parser)
        parser.add_argument('deploy_host', help='host to deploy the fireball to')
        parser.add_argument('-r', '--resolve-artifacts',
                            help='the deploying script will attempt to build your ondemand webapp '
                                 'in offline mode, but you local repo might not have some artifacts, like the scala maven plugin; '
                                 'if you need to run this option your need to run it once - if the deployment script fails on missing artifacts.',
                            action='store_true', dest='resolve_artifacts')


class OnDemand(JmakeModule):
    def __init__(self):
        super().__init__()
        self.command = 'ondemand'
        self.description = 'Aids in building, running, DoTing and deploying JIRA in OnDemand mode. '

    def get_submodules(self):
        return [OnDemandInstall(), OnDemandRun(), OnDemandDeploy(), OnDemandDebug()]

