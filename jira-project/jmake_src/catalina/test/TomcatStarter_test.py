from functools import reduce
import os
from unittest import TestCase
import unittest
from CommandExecutor import Callable
from ondemand.HordeLayout import HordeLayout
from utils.Mocks import Mock
import BundledPluginsUtility
from catalina.TomcatRunner import TomcatStarter
from catalina.WorkspaceLayout import MockLayout
from utils.FileUtils import MockFileUtils
from utils import PathUtils

class MockTomcatStarter(TomcatStarter):
    executed = False

    #noinspection PyUnusedLocal
    def _TomcatStarter__call_super(self, logger):
        self.executed = True
        return 0


class TestTomcatStarter(TestCase):
    def setUp(self):
        self.layout = MockLayout(False)
        self.args = Mock(suspend=False)
        self.args.port = 1234
        self.args.layout = self.layout
        self.args.jrebel = False
        self.args.override_dev_mode = False
        self.args.extra_jvm_flags = ''
        self.args.max_perm_size = '384m'
        self.args.xmx = '1024m'
        self.args.enable_mail = False
        self.args.bundled_plugins = False
        self.args.plugin_resources = ''
        self.args.manifesto_psd = None
        self.args.clustered = False

    def tearDown(self):
        self.layout.remove()


    def test_simple_startup_without_options(self):
        #having
        tomcat_starter = MockTomcatStarter(self.args, False, MockFileUtils())
        #when
        tomcat_starter(Mock())
        #then

        #noinspection PyUnresolvedReferences
        self.assertEqual(tomcat_starter._SystemCallable__command, self.layout.tomcat_executable() + ' run')
        self.assertTrue(tomcat_starter.executed, 'Process should have been executed')
        opts = self.__get_opts_dict(tomcat_starter)
        self.__assertParameter(opts, '-Djira.home', self.layout.jira_home())
        self.__assertParameter(opts, '-Djira.plugins.bundled.disable', 'false')
        self.__assertParameter(opts, '-Djira.dev.bundledplugins.url',
            'file://' + BundledPluginsUtility.BUNDLED_PLUGINS_LIST)
        self.__assertParameter(opts, '-Xms128m')
        self.__assertParameter(opts, '-Xmx1024m')
        self.__assertParameter(opts, '-XX:MaxPermSize', '384m')
        self.assertNotIn('-Djira.rebel.root', opts)
        self.assertNotIn('-javaagent:', opts)
        self.assertNotIn('-agentlib:jdwp:', opts)
        #now check tomcat environment variables
        self.__assertParameter(tomcat_starter.env, 'CATALINA_HOME', self.layout.tomcat_dir(False))
        self.__assertParameter(tomcat_starter.env, 'CATALINA_TMPDIR', self.layout.tomcat_temp_dir())
        self.__assertParameter(tomcat_starter.env, 'CATALINA_BASE', self.layout.tomcat_work_dir())

    def test_ondemand_option_adds_properties(self):
        #having
        self.args.layout.ondemand = True
        self.args.horde_layout = HordeLayout(self.layout.jira_home())
        tomcat_starter = MockTomcatStarter(self.args, True, MockFileUtils())
        #when
        tomcat_starter(Mock())
        #then
        opts = self.__get_opts_dict(tomcat_starter)
        self.__assertParameter(opts, '-Dstudio.initial.data.xml', self.layout.studio_initial_data())
        self.__assertParameter(opts, '-Dstudio.home', self.layout.jira_home())
        self.__assertParameter(opts, '-Datlassian.darkfeature.com.atlassian.jira.config.CoreFeatures.ON_DEMAND', 'true')
        self.__assertParameter(opts, '-Dcrowd.property.application.login.url', self.args.horde_layout.horde_application_login_url())
        self.__assertParameter(opts, '-Dcrowd.property.crowd.server.url', self.args.horde_layout.horde_server_url())
        self.__assertParameter(opts, '-Dstudio.webdav.directory', self.layout.webdav_dir())
        self.assertNotIn('-Djira.dev.bundledplugins.url', opts)


    def test_debug_parameter_should_add_agentlib_jdwp_param(self):
        #having
        tomcat_starter = MockTomcatStarter(self.args, True, MockFileUtils())
        self.args.debug_port = 8546
        #when
        tomcat_starter(Mock())

        #then
        opts = self.__get_opts_dict(tomcat_starter)
        self.__assertParameter(opts, '-agentlib:jdwp', 'transport=dt_socket,server=y,suspend=n,address=8546')
        self.assertNotIn('-javaagent:', opts)

    def test_jrebel_is_discovered_and_java_opts_are_set(self):
        #having
        self.args.jrebel = True
        file_utils = MockFileUtils()
        file_utils.expect_possible_idea_plugin_dirs(toReturn=['idea12', 'idea13', 'idea129'])
        jrebel_path = os.path.join('idea13', 'config', 'plugins', 'jr-ide-idea', 'lib', 'jrebel', 'jrebel.jar')
        file_utils.expect_file_exists(jrebel_path, toReturn=True)
        tomcat_starter = MockTomcatStarter(self.args, False, file_utils)

        #when
        return_code = tomcat_starter(Mock())

        #then
        self.assertEqual(return_code, Callable.success, 'Expected successful return code')
        opts = self.__get_opts_dict(tomcat_starter)
        self.__assertParameter(opts, '-javaagent:"' + jrebel_path + '"')
        self.__assertParameter(opts, '-Djira.rebel.root', '"' + PathUtils.abspath('.') + '"')
        self.assertNotIn('-agentlib:jdwp:', opts)

    def test_bundled_plugins_removes_jira_dev_bundledplugins_url(self):
        self.args.bundled_plugins = True
        #having
        tomcat_starter = MockTomcatStarter(self.args, False, MockFileUtils())
        #when
        tomcat_starter(Mock())
        #then
        opts = self.__get_opts_dict(tomcat_starter)
        self.assertNotIn('-Djira.dev.bundledplugins.url', opts);


    def test_execution_email_enabled(self):

        self.args.enable_mail = True
        #having
        tomcat_starter = MockTomcatStarter(self.args, False, MockFileUtils())
        #when
        tomcat_starter(Mock())
        #then

        self.assertTrue(tomcat_starter.executed, 'Process should have been executed')
        opts = self.__get_opts_dict(tomcat_starter)
        self.__assertParameter(opts, '-Djira.home', self.layout.jira_home())
        self.__assertParameter(opts, '-Datlassian.mail.senddisabled', 'false')
        self.__assertParameter(opts, '-Datlassian.mail.fetchdisabled', 'false')

    def test_execution_is_suppressed_when_jrebel_not_discovered(self):
        #having
        self.args.jrebel = True
        tomcat_starter = MockTomcatStarter(self.args, False, MockFileUtils().expect_possible_idea_plugin_dirs(toReturn=[]))
        log = Mock()
        #when
        return_code = tomcat_starter(log)

        #then
        self.assertEqual(return_code, Callable.do_not_proceed, 'Expected successful return code')

        self.assertTrue(log.callcount_error() > 0)
        # verify, that jrebel related error message was logged:
        self.assertTrue(reduce(lambda r, e: r or 'jrebel' in e, log.error.made_calls, False))

    def __assertParameter(self, opts, key, value=''):
        self.assertIn(key, opts)
        self.assertIn(opts[key], value)

    def __get_opts_dict(self, tomcat_starter):
        env_java_opts_ = tomcat_starter.env['JAVA_OPTS']
        self.assertIsNotNone(env_java_opts_)
        opts = dict(map(lambda str: str.split('=', 1) if '=' in str else [str, ''], env_java_opts_.split(' ')))
        return opts

if __name__ == '__main__':
    unittest.main(verbosity=2)
