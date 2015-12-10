import os
from CommandExecutor import SystemCallable, Callable
import BundledPluginsUtility
from catalina.WorkspaceLayout import WorkspaceLayout
from utils import PathUtils
from utils.FileUtils import FileUtils
from utils.WorkspaceUtils import WorkspaceUtils


class TomcatRunner(SystemCallable):
    def __init__(self, args):
        super().__init__(args)

    def plugin_resources(self, fileutils=FileUtils(),workspace_utils=WorkspaceUtils()):
        if self.args.disable_plugin_resources:
            return ''
        resources_marker = os.sep.join(['src', 'main', 'resources'])
        resources_marker_exclude = '.hg'
        plugins_roots = [os.sep.join(['jira-components', 'jira-plugins'])]
        if self.args.plugin_resources:
            plugins_roots.append(fileutils.abs_path(self.args.plugin_resources))
        plugins_roots.extend(os.sep.join([workspace_utils.get_jira_workspace_dir(), project])
                             for project in workspace_utils.get_workspace_projects_without_jira(self.args))

        return '-Dplugin.resource.directories=%s' % ','.join(
            PathUtils.abspath(path) for plugins_root in plugins_roots
            for (path, dirs, files) in fileutils.walk(plugins_root) if path.endswith(resources_marker)
            and not resources_marker_exclude in path)

    def get_env(self, debug_mode, dev_mode, log, layout):
        env = os.environ.copy()
        env['CATALINA_HOME'] = layout.tomcat_dir(False)
        env['CATALINA_TMPDIR'] = layout.tomcat_temp_dir()
        env['CATALINA_BASE'] = layout.tomcat_work_dir()

        if 'JAVA_OPTS' in env:
            log.warn('You have set JAVA_OPTS environment variable using it as base: ' + env['JAVA_OPTS'])
        else:
            env['JAVA_OPTS'] = ''

        env['JAVA_OPTS'] += self.__get_opts_for_jira(dev_mode, layout)

        if debug_mode:
            suspend_switch = 'y' if self.args.suspend else 'n'
            env['JAVA_OPTS'] += ' -agentlib:jdwp=transport=dt_socket,server=y,suspend='+suspend_switch+',address=' + str(
            self.args.debug_port)

        if self.args.extra_jvm_flags:
            for j in self.args.extra_jvm_flags:
                env['JAVA_OPTS'] += ' ' + j

        log.debug('Using JAVA_OPTS=' + env['JAVA_OPTS'])
        return env

    def __get_opts_for_jira(self, dev_mode, layout: WorkspaceLayout):
        args = ' -Xms128m'\
               ' -Xmx' + self.args.xmx + ''\
               ' -XX:MaxPermSize=' + self.args.max_perm_size + ''\
               ' -XX:+HeapDumpOnOutOfMemoryError'\
               ' -Djira.i18n.texthighlight=false'\
               ' -Dmail.debug=false'\
               ' -Dorg.apache.jasper.runtime.BodyContentImpl.LIMIT_BUFFER=true'\
               ' -Dmail.mime.decodeparameters=true'\
               ' -Djira.dev.mode=' + str(dev_mode).lower() +\
               ' -Djira.plugins.bundled.disable=false' \
               ' -Datlassian.plugins.tenant.smart.patterns=' + \
                                PathUtils.abspath('jira-components/jira-core/src/main/resources/tenant-smart-patterns.txt') +\
               ' -Djira.paths.set.allowed=true'\
               ' -Duser.language=en'\
               ' -Duser.region=AU'\
               ' -Duser.timezone=Australia/Sydney'\
               ' -Dplugin.webresource.batching.off=' + str(dev_mode).lower() +\
               ' -Djava.awt.headless=true'\
               ' -Djira.home=' + layout.jira_home()
        if not (self.args.bundled_plugins or layout.ondemand):
            args += ' -Djira.dev.bundledplugins.url=file://' + BundledPluginsUtility.BUNDLED_PLUGINS_LIST
        if layout.ondemand:
            args += ' -Dstudio.initial.data.xml=' + layout.studio_initial_data() +\
                    ' -Dstudio.home=' + layout.jira_home() +\
                    ' -Datlassian.darkfeature.com.atlassian.jira.config.CoreFeatures.ON_DEMAND=true'\
                    ' -Dcrowd.property.application.login.url=' + self.args.horde_layout.horde_application_login_url() +\
                    ' -Dcrowd.property.crowd.server.url=' + self.args.horde_layout.horde_server_url() +\
                    ' -Dstudio.webdav.directory=' + layout.webdav_dir()
            if self.args.manifesto_psd is not None:
                args += ' -Datlassian.jira.plugin.scan.directory=' + self.args.manifesto_psd

        if hasattr(self.args, 'clustered') and self.args.clustered:
            args += ' -Datlassian.cluster.scale=true' +\
                    ' -DjvmRoute=' + self.args.instance_name + \
                    ' -Datlassian.cache.ehcache=true'

        if self.args.enable_mail:
             args += ' -Datlassian.mail.senddisabled=false'\
                    ' -Datlassian.mail.fetchdisabled=false'

        return args + ' ' + self.plugin_resources()

class TomcatStarter(TomcatRunner):
    def __init__(self, args, debug_mode, file_utils=FileUtils()):
        super().__init__(args)
        self.debug_mode = debug_mode
        self.file_utils = file_utils
        self.dev_mode = not args.override_dev_mode

    def __call_super(self, logger):
        return super().__call__(logger)

    def __call__(self, logger):
        layout = self.args.layout
        self.__clear_temp(logger, layout.tomcat_work_dir() + os.sep + 'org')
        self.__clear_temp(logger, layout.tomcat_work_dir() + os.sep + 'webapps')
        self.__clear_temp(logger, layout.tomcat_temp_dir())

        logger.info('Starting tomcat on port ' + str(self.args.port))

        layout = self.args.layout
        self.env = self.get_env(self.debug_mode, self.dev_mode, logger, layout)
        if self.args.jrebel:
            jrebel_options = self.jrebel_options(logger)
            if jrebel_options is None:
                return Callable.do_not_proceed
            else:
                self.env['JAVA_OPTS'] += jrebel_options
        self.command(layout.tomcat_executable() + ' run')

        return self.__call_super(logger)

    def jrebel_options(self, log):
        jrebel_lib_location = self.jrebel_lib_location(log)
        if jrebel_lib_location is None:
            log.error(
                'Cannot find jrebel.jar in idea configuration. Please ensure that you have jrebel plugin installed')
            return None
        else:
            log.info('Using jrebel library from ' + jrebel_lib_location)
            return ' -Djira.rebel.root="%s" -javaagent:"%s"' % (PathUtils.abspath('.'), jrebel_lib_location)

    def jrebel_lib_location(self, log):
        if 'JREBEL_HOME' in self.env:
            return os.sep.join([self.env['JREBEL_HOME'], 'jrebel.jar'])
        else:
            for idea_config in self.file_utils.possible_idea_plugin_dirs():
                jrebel_paths = [ os.sep.join([idea_config, 'config', 'plugins', 'jr-ide-idea', 'lib', 'jrebel', 'jrebel.jar'])
                               , os.sep.join([idea_config, 'jr-ide-idea', 'lib', 'jrebel', 'jrebel.jar']) ]

                for jrebel_jar_file in jrebel_paths:
                    log.trace('Searching for jrebel.jar in ' + jrebel_jar_file)
                    if self.file_utils.file_exists(jrebel_jar_file):
                        return jrebel_jar_file

    def __clear_temp(self, log, dir):
        try:
            if self.file_utils.dir_exists(dir) and self.file_utils.listdir(dir):
                log.debug('deleting: ' + dir)
                self.file_utils.remove_dir(dir)
                # create the dir again:
                self.file_utils.existing_dir(dir)
        except IOError:
            # cannot delete?
            pass
        except OSError:
            # dir does not exist?
            pass

class TomcatStopper(TomcatRunner):
    def __init__(self, args):
        super().__init__(args)

    def __call__(self, logger):
        logger.info('Stopping tomcat on port ' + str(self.args.port))

        layout = self.args.layout
        self.env = self.get_env(False, False, logger, layout)
        self.command(layout.tomcat_executable() + ' stop')

        return super().__call__(logger)


