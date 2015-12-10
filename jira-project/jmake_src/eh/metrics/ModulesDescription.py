import io
import os
import types
from zipfile import ZipFile
from BundledPluginsUtility import BundledPluginsUtility
from CommandExecutor import Callable
from Logger import Logger
from eh.metrics.FileCount import FileCountAndSize, FileCount
from eh.metrics.GrepCount import GrepCount, GrepCountWithActivator
from eh.metrics.JavaPackageImports import JavaPackageImports
from eh.metrics.tailored.DeprecatedMethodUsage import DeprecatedMethodUsage
from eh.metrics.tailored.InvalidTimedQueries import InvalidTimedQueries
from eh.metrics.tailored.InvocationOfSoyTemplates import InvocationOfSoyTemplates
from eh.metrics.tailored.InvocationOfVelocityMacros import InvocationOfVelocityMacros
from eh.metrics.tailored.JUnitAssertWithoutMessage import JunitAssertWithoutMessage
from eh.metrics.tailored.JUnitFinder import JUnitFinder
from eh.metrics.tailored.ManagersInJiraApi import ManagersInJiraApi
from eh.metrics.tailored.WebTestFinder import WebTestFinder
from eh.metrics.tailored.BadPluginMetrics import MissingHostComponentsXml, MissingOsgiManifest, PluginXmlMinified
from maven.Maven import MavenCallable, MavenExecutionException
from utils.FileUtils import FileUtils

PLUGINS_PROFILES = []

ATLASSIAN_PLUGIN_XML = 'atlassian-plugin.xml'


class ModulesDescription(object):

    commonJUnitFinder = JUnitFinder()

    def __init__(self, collectors: list, description: str):
        self.collectors = collectors
        self.description = description

    def prepare_environment(self, log: Logger):
        """
        Invoked before all other methods this can be used to prepare execution environment
        :param log: loggger object
        """
        pass

    def get_collectors(self):
        return self.collectors

    def measured_modules(self):
        """
        Get iterator for all names of measured modules
        """
        pass

    def files(self, module):
        """
        Return iterator for all files within given module
        """
        pass

    def open_file(self, file):
        """
        Open file, the name of the file must be the one that was obtained by executing files method
        returns object with read_line method that returns one encoded line of the file
        """
        pass


class JIRADirectoryScanModulesDescription(ModulesDescription):
    """
    Describes modules in JIRA source
    """

    def __init__(self, fast: bool, file_utils: FileUtils=FileUtils()):

        collectors = [JavaPackageImports('usageof.CacheBuilder',
                                         'Imports of CacheBuilder: prevent caches other than EhCache',
                                         'com.google.common.cache.CacheBuilder').unchecked(),
                      JavaPackageImports('usageof.java.util.Calendar',
                                         'Stop using java.util.Calendar - use joda time instead',
                                         'java.util.Calendar'),
                      ManagersInJiraApi(),
                      FileCountAndSize('velocity', lambda x: x.endswith('.vm'), 'Get rid of velocity files').unchecked(),
                      FileCountAndSize('jsp', lambda x: x.endswith('.jsp'), 'Get rid of jsp files').unchecked(),
                      GrepCount('usageof.css.important', lambda x: x.endswith(('.css', '.less')),
                                'Get rid of css !important usages', '!important'),
                      GrepCount('usageof.system.getproperty',
                                lambda x: x.endswith('.java') and not x.endswith('/SystemPropertiesAccessor.java'),
                                'Get rid of System.getProperty family usages '
                                '(use JiraSystemProperties class if you must)',
                                ['System.getProperty', 'System.getProperties', 'Boolean.getBoolean',
                                 'System.clearProperty', 'Integer.getInteger', 'Long.getLong', 'System.setProperty',
                                 'System.setProperties']),
                      GrepCount('usageof.actioncontext.getrequest', lambda x: x.endswith('.java'),
                                'Get rid of ActionContext.getRequest usages', 'ActionContext.getRequest'),
                      GrepCount('usageof.webwork.action.jelly.tag', lambda x: x.endswith('.java'),
                                'Jelly dependencies on webwork actions blocks their removal',
                                ('extends ActionTagSupport', 'extends UserAwareActionTagSupport',
                                 'extends IssueSchemeAwareActionTagSupport', 'extends ProjectAwareActionTagSupport',
                                 'extends PermissionSchemeAwareActionTagSupport')),
                      GrepCount('usageof.not.component.accessor', lambda x: x.endswith('.java'),
                                'Should use component accessor to get components',
                                ['import com.atlassian.jira.ComponentManager',
                                 'import com.atlassian.jira.ManagerFactory',
                                 'import com.atlassian.jira.util.ComponentLocator'
                                 'import com.atlassian.jira.plugin.ComponentClassManager'],
                                'class'),
                      GrepCount('tests.qunit.core.count', lambda x: x.endswith(('-test.js', '-tests.js')),
                                'Keep count of Qunit tests', ['test(', 'asyncTest(']).unchecked().rising(),
                      GrepCount('frontend.number.of.soy.templates', lambda x: x.endswith('.soy'),
                                'Keep count of soy templates', '{/template}').unchecked().rising(),
                      GrepCount('frontend.number.of.ww.tags', lambda x: x.endswith('.jsp'),
                                'Get rid of jsp webwork tags', '<ww:').unchecked(),
                      GrepCount('frontend.number.of.velocity.macros', lambda x: x.endswith('.vm'),
                                'Rewrite velocity macros to soy', lambda x: x.strip().startswith('#macro')),
                      GrepCount('frontend.number.of.css.rules', lambda x: x.endswith(('.css', '.less')),
                                'Keep track of styles complexity.', '{').unchecked(),
                      GrepCount('frontend.number.of.lcss.mixins.invocations',
                                lambda x: x.endswith('.less'),
                                'Keep track of mixin complexity.',
                                lambda line: ');' in line and ':' not in line).unchecked(),
                      FileCount('soy', lambda x: x.endswith('.soy'), 'Keep count of soy files.').unchecked().rising(),
                      FileCount('frontend.helpers', lambda x: any(path in x for path in
                                ['/webapp/template/aui/', 'webapp/template/standard', 'webapp/ui/aui-layout']),
                                'Number of files with JIRA exceptions in AUI').unchecked(),
                      JavaPackageImports('usageof.generic.value.webwork.action',
                                         'Should not be using GenericValue in webwork actions',
                                         'org.ofbiz.core.entity.GenericValue', 'web.action'),
                      JavaPackageImports('usageof.property.set.webwork.action',
                                         'Should not be using Property Set in webwork actions',
                                         'com.opensymphony.module.propertyset.PropertySet', 'web.action'),
                      JavaPackageImports('usageof.crowd.user',
                                         'use UserRename user',
                                         'com.atlassian.crowd.embedded.api.User').unchecked(),
                      JavaPackageImports('usageof.event.publisher.webwork.action',
                                         'Should not be using Event Publisher in webwork actions',
                                         'com.atlassian.event.api.EventPublisher', 'web.action'),
                      JavaPackageImports('usageof.issue.event.dispatcher.webwork.action',
                                         'Should not be using IssueEventDispatcher in webwork actions',
                                         'com.atlassian.jira.event.issue.IssueEventDispatcher', 'web.action'),
                      JavaPackageImports('usageof.issue.event.manager.webwork.action',
                                         'Should not be using IssueEventManager in webwork actions',
                                         'com.atlassian.jira.event.issue.IssueEventManager', 'web.action'),
                      JavaPackageImports('usageof.ofbiz.delegator.interface.webwork.action',
                                         'Should not be calling straight to Ofbiz in webwork actions '
                                         '(DelegatorInterface)',
                                         'org.ofbiz.core.entity.DelegatorInterface', 'web.action'),
                      JavaPackageImports('usageof.ofbiz.delegator.webwork.action',
                                         'Should not be calling straight to Ofbiz in webwork actions (OfBizDelegator)',
                                         'com.atlassian.jira.ofbiz.OfBizDelegator', 'web.action'),
                      JavaPackageImports('usageof.entity.engine.webwork.action',
                                         'Should not be calling straight to Ofbiz in webwork actions (EntityEngine)',
                                         'com.atlassian.jira.entity.EntityEngine', 'web.action'),
                      JavaPackageImports('usageof.managers.webwork.action',
                                         'Should not be using Managers in webwork actions', 'Manager',
                                         'web.action', whitelist = ['.FeatureManager']),
                      ModulesDescription.commonJUnitFinder,
                      InvocationOfSoyTemplates().unchecked().rising(),
                      InvocationOfVelocityMacros().unchecked()] + ([] if fast else [DeprecatedMethodUsage().unchecked()])

        super().__init__(collectors, 'Scan JIRA source files')
        self.file_utils = file_utils

    def measured_modules(self):
        yield (os.sep.join(['jira-components', 'jira-api']))
        yield (os.sep.join(['jira-components', 'jira-core']))
        yield (os.sep.join(['jira-components', 'jira-webapp']))

        bp = os.sep.join(['jira-components', 'jira-plugins'])
        for plugin_dir in (os.sep.join([bp, entry]) for entry in self.file_utils.listdir(bp) if
                           self.file_utils.dir_exists(os.sep.join([bp, entry]))):
            yield plugin_dir

        bp = os.sep.join(['jira-ondemand-project', 'jira-ondemand-plugins'])
        for plugin_dir in (os.sep.join([bp, entry]) for entry in self.file_utils.listdir(bp) if
                           self.file_utils.dir_exists(os.sep.join([bp, entry]))):
            yield plugin_dir

    def files(self, module):
        for root, dirs, files in self.file_utils.walk(os.sep.join([module, 'src'])):
            for file in files:
                yield os.sep.join((root, file))

    def open_file(self, file):
        return self.file_utils.open(file)


class BundledPluginsModulesDescription(ModulesDescription):
    """
    Describes bundled plugins modules based on bundled plugins list
    """

    def __init__(self, fast: bool, plugins_utility=BundledPluginsUtility(), maven_callable=MavenCallable):
        plugins_stop_condition = ['plugins-version', 'pluginsVersion']
        collectors = [GrepCount('plugins.version.one.plugins', lambda x: x == 'atlassian-plugin.xml',
                                'Should not be adding new version 1 plugins',
                                ['pluginsVersion="1"', 'plugins-version="1"'], plugins_stop_condition),
                      FileCount('plugins.count', lambda x: x == 'atlassian-plugin.xml',
                                'Count of plugins in JIRA codebase.').unchecked().neutral(),
                      GrepCountWithActivator('plugins.version.web.panels.to.soy',
                                             lambda x: x == 'atlassian-plugin.xml',
                                             'Should migrate web panels to soy templates',
                                             r'<\s*resource.+type\s*=\s*"velocity"',
                                             start_count=r'<\s*web-panel', stop_count=r'<\s*/\s*web-panel\s*>',
                                             use_regex=True),
                      FileCount('bundled.jars.in.plugins', lambda x: x.endswith('.jar'),
                                'Try and use provided jars instead of bundling your own. See '
                                'https://developer.atlassian.com/display/DOCS/How+to+Speed+Up+Plugin+Startup for more '
                                'details'),
                      MissingHostComponentsXml('plugins.missing.component.import.xml',
                          'Should be specifying a atlassian-plugins-host-components.xml to bypass host component '
                          'scanning. See https://developer.atlassian.com/display/DOCS/How+to+Speed+Up+Plugin+Startup#HowtoSpeedUpPluginStartup-Bypasshostcomponentscanning '
                          'for more details').unchecked(),
                      MissingOsgiManifest('plugins.missing.osgi.instructions',
                          'Should be specifying a non-empty instructions element so the SDK will generate an OSGi '
                          'manifest. See https://developer.atlassian.com/display/DOCS/How+to+Speed+Up+Plugin+Startup#HowtoSpeedUpPluginStartup-GeneratetheOSGimanifestatbuildtime '
                          'for more details'),
                      # Won't check the plugin xml minified until AMPS 4.2.4 is released.
                      PluginXmlMinified('plugins.xml.not.minified',
                          'Should be minifiying your atlassian-plugin.xml using plugin SDK version 4.2.4 or later').unchecked().rising()]

        super().__init__(collectors, 'Scan Bundled plugins JARS')
        self.plugins_utility = plugins_utility
        self.plugin_zip = None
        self.plugin_xml_info = None
        self.fast = fast
        self.maven_callable = maven_callable

    def prepare_environment(self, log: Logger):
        self.plugins_list = self.__build_bundled_plugins_list(log)

    def measured_modules(self):
        for plugin in self.plugins_list:
            if self.plugin_zip is not None:
                self.plugin_zip.close()
            yield plugin

    def files(self, module):
        self.last_open_module = module
        self.plugin_zip = ZipFile(module)
        for file_name in self.plugin_zip.NameToInfo.keys():
            if file_name.startswith("META-INF/") or file_name.endswith(".xml"):
                yield module.split("/")[-1].rsplit('-', 1)[0] + "/" + file_name

    def open_file(self, file_name):
        if file_name.endswith('.xml') or file_name.endswith('MANIFEST.MF'):
            zip_open = self.plugin_zip.open(file_name.split('/', 1)[-1])
            return io.TextIOWrapper(zip_open, encoding='UTF-8')
        else:
            # We don't actually want to open the other files - we only use them for file counts.
            return None

    def __build_bundled_plugins_list(self, logger):

        if self.fast:

            if self.plugins_utility.bundled_plugins_list_ok(PLUGINS_PROFILES):
                return self.plugins_utility.get_all_bundled_plugins_paths()
            else:
                logger.info("Bundled plugins list does not exist and in fast mode - skipping measures on plugins")
                return []

        # noinspection PyUnusedLocal
        def process_output(myself, log: Logger, line: str, num: int):
            return line.startswith("[INFO] Building Atlassian") or line.startswith('[ERROR]') or logger.is_trace()

        if not self.plugins_utility.bundled_plugins_list_ok(PLUGINS_PROFILES):
            maven = self.maven_callable()
            maven.phase('package')
            maven.projects = self.plugins_utility.get_bundled_plugins_module()
            maven.property('skip.smartass.zip.update', 'true')
            maven.option('--no-plugin-updates').option('-am')
            maven.property('skipSources')
            maven.skip_tests()
            logger.info('Packaging plugins to build list of all plugins')
            maven.process_output = types.MethodType(process_output, maven)
            ret_code = maven(Logger().set_none())
            if ret_code != Callable.success:
                raise MavenExecutionException('Packaging of bundled plugins did not finish properly')
            self.plugins_utility.remember_plugins_profiles(PLUGINS_PROFILES)
            logger.info('Finished building bundled plugins.')
        else:
            logger.info('Bundled plugins list up to date skipping compilation')
        return self.plugins_utility.get_all_bundled_plugins_paths()


class JIRATestsModulesDescription(ModulesDescription):
    """
    Describes modules in JIRA source
    """

    def __init__(self, fast: bool, file_utils: FileUtils=FileUtils()):

        collectors = [JavaPackageImports('usageof.easymock',
                                         'Get rid of easymock and use Mockito',
                                         'org.easymock'),
                      JavaPackageImports('usageof.mockobjects',
                                         'Get rid of mockobjects and use Mockito',
                                         'com.mockobjects'),
                      JunitAssertWithoutMessage('usageof.assert.true.false',
                                                'Do not use assertTrue or assertFalse without specifying a message')
                      .unchecked(),
                      ModulesDescription.commonJUnitFinder,
                      WebTestFinder().unchecked().neutral(),
                      InvalidTimedQueries()]

        super().__init__(collectors, 'Scan JIRA test files')
        self.file_utils = file_utils

    def measured_modules(self):
        yield 'jira-func-tests'
        yield 'jira-webdriver-tests'
        yield 'jira-page-objects'
        yield os.sep.join(['jira-components', 'jira-tests-parent', 'jira-tests-unit'])
        yield os.sep.join(['jira-components', 'jira-tests-parent', 'jira-tests-legacy'])
        yield os.sep.join(['jira-components', 'jira-tests-parent', 'jira-tests'])

    def files(self, module):
        for root, dirs, files in self.file_utils.walk(os.sep.join([module, 'src'])):
            for file in files:
                yield os.sep.join((root, file))

    def open_file(self, file):
        return self.file_utils.open(file)
