import os
import types
from unittest import TestCase
import unittest
from CommandExecutor import Callable
from Diagnostics import LocalPortInspector

from JmakeRun import Run, process_local_test_settings, SetupJiraHomeHsqldb
from Logger import LOG
from catalina.WorkspaceLayout import WorkspaceBuilder
from utils.WorkspaceUtils import WorkspaceValidator, MavenCallableWorkspaceDelegator
from utils.Mocks import Mock
from catalina.ContextPreparer import ContextPreparer
from catalina.TomcatDownloader import TomcatDownloader
from catalina.TomcatRunner import TomcatStarter, TomcatStopper
from maven.Maven import MavenCallable
from utils.FileUtils import MockFileUtils, FileUtils


class MocArgs(Mock):
    def __init__(self, bundled_plugins=False, build_war=False, attach_sources=False, ref_plugins=False,
            override_dev_mode=False, setup_home=False, with_workspace=False,
            third_party_licensing=True):
        super().__init__(bundled_plugins=bundled_plugins,
                         build_war=build_war,
                         attach_sources=attach_sources,
                         ref_plugins=ref_plugins,
                         control='',
                         override_dev_mode=override_dev_mode,
                         mvn_clean=False,
                         mvn_update=False,
                         mvn_offline=False,
                         mvn_errors=False,
                         mvn_debug=False,
                         setup_home=setup_home,
                         with_workspace=with_workspace,
                         tomcat_version=WorkspaceBuilder.TOMCAT7,
                         third_party_licensing=third_party_licensing,
                         jira_home='jirahome',
                         layout=Mock(),
                         clustered=False,
                         postgres=False,
                         mysql=False)


class JmakeRunTest(TestCase):
    def setUp(self):
        LOG.set_none()
        self.stale_plugins = Mock()
        self.stale_plugins.expect_get_bundled_plugins_module(toReturn=['bundled_plugins'])
        self.stale_plugins.default_find_plugins_to_recompile([])
        self.module = Run(self.stale_plugins)
        self.module._Run__build_tomcat_layout = lambda args: None

    def test_simple_build_with_no_flags(self):
        executor = []
        args = MocArgs()

        self.module(args, executor)

        maven_callable = self.__assert_common_build_params(executor)
        self.__assert_property(maven_callable, 'skipSources')
        self.__assert_property(maven_callable, 'jira.exclude.bundled.plugins')
        self.__assert_property(maven_callable, 'jira.do.not.prepare.war')
        self.assertIsNone(maven_callable._SystemCallable__cwd)

    def test_reference_plugins_added_when_flag_set_and_plugins_needs_to_be_recompiled(self):
        executor = []
        args = MocArgs(ref_plugins=True)
        self.stale_plugins.default_find_plugins_to_recompile(['just-fake-plugin'])
        self.module(args, executor)

        maven_callable = self.__assert_common_build_params(executor)
        self.assertIn('reference-plugins', maven_callable.profiles)

    def test_third_party_licensing_disabled_removes_profile_and_maven_task(self):
        executor = []
        args = MocArgs(third_party_licensing=False)
        self.module(args, executor)
        self.assertEqual(len(executor), 9, "Should have added tasks to execute")
        self.assertIsInstance(executor[0], LocalPortInspector)
        self.assertIsInstance(executor[1], LocalPortInspector)
        self.assertIsInstance(executor[2], types.FunctionType)
        maven_callable = executor[3]
        self.assertIsInstance(maven_callable, MavenCallable)

        self.assertNotIn('third-party-licensing', maven_callable.profiles)

    def test_reference_plugins_not_added_when_flag_set_and_plugins_does_not_need_recompilation(self):
        executor = []
        args = MocArgs(ref_plugins=True)
        self.module(args, executor)

        maven_callable = self.__assert_common_build_params(executor)
        self.assertListEqual(maven_callable.profiles, ['func-mode-plugins', 'pseudo-loc', 'dev-mode-plugins',
                                                       'reference-plugins', 'third-party-licensing'])

    def test_bundled_plugins_are_build_when_flag_present(self):
        executor = []
        args = MocArgs(True)

        self.module(args, executor)

        maven_callable = self.__assert_common_build_params(executor)
        self.assertIsInstance(maven_callable, MavenCallable)
        #bundled plugins are build when this flag is not present
        self.assertNotIn('jira.exclude.bundled.plugins', maven_callable.properties)

    def test_bundled_plugins_are_build_when_clean_flag(self):
        executor = []
        args = MocArgs()
        args.mvn_clean = True

        self.module(args, executor)

        maven_callable = self.__assert_common_build_params(executor)
        self.assertIn(self.stale_plugins.get_bundled_plugins_module()[0], maven_callable.projects)
        self.assertIn('clean', maven_callable.phases)

    def test_war_is_build_when_buid_war_flag_set(self):
        executor = []
        args = MocArgs(build_war=True)
        args.mvn_clean = True

        self.module(args, executor)

        maven_callable = self.__assert_common_build_params(executor)
        self.assertNotIn('jira.do.not.prepare.war', maven_callable.properties)

    def test_sources_are_added_when_flag_present(self):
        executor = []
        args = MocArgs(attach_sources=True)
        args.mvn_clean = True

        self.module(args, executor)

        maven_callable = self.__assert_common_build_params(executor)
        self.assertNotIn('skipSources', maven_callable.properties)

    def test_maven_is_run_from_different_dir_when_workspace_flag_present(self):
        executor = []
        args = MocArgs(with_workspace=True)
        args.mvn_clean = True

        self.module(args, executor)

        maven_callable = self.__assert_workspace_build_params(executor)
        self.assertIsNotNone(maven_callable._SystemCallable__cwd)

    def test_validate_workspace_params_happy_path(self):
        fileutils = MockFileUtils()

        workspace_dir = '/home/abracadabra/IdeaProjects/rest-workspace'
        jira_project_dir = 'jira-project'
        pom_xml = 'pom.xml'

        fileutils.default_getcwd(os.sep.join([workspace_dir, jira_project_dir]))
        fileutils.expect_file_exists(os.sep.join([workspace_dir, pom_xml]), toReturn=True)
        fileutils.expect_get_parent_dir_path(toReturn=workspace_dir)

        callable_status = WorkspaceValidator(fileutils)(Mock())

        self.assertEqual(callable_status, Callable.success)

    def test_validate_workspace_params_fails_without_workspace(self):
        fileutils = MockFileUtils()

        workspace_dir = '/home/abracadabra/IdeaProjects/rest-workspace'
        jira_project_dir = 'jira-project'
        pom_xml = 'pom.xml'

        fileutils.expect_getcwd(toReturn=os.sep.join([workspace_dir, jira_project_dir]))
        fileutils.expect_file_exists(os.sep.join([workspace_dir, pom_xml]), toReturn=False)
        fileutils.expect_get_parent_dir_path(toReturn=workspace_dir)
        logger = Mock()

        callable_status = WorkspaceValidator(fileutils)(logger)

        self.assertEqual(logger.callcount_error(), 1)
        self.assertEqual(callable_status, Callable.failure)

    def test_transform_maven_executable_for_workspace(self):
        fileutils = MockFileUtils()

        workspace_dir = '/home/abracadabra/IdeaProjects/rest-workspace'
        jira_project_dir = 'jira-project'
        jira_rest_dir = 'jira-rest'
        pom_xml = 'pom.xml'

        fileutils.expect_get_parent_dir_relpath(workspace_dir, toReturn=jira_project_dir)

        fileutils.default_getcwd(os.sep.join([workspace_dir, jira_project_dir]))
        fileutils.expect_listdir(workspace_dir,
                                 toReturn=[jira_project_dir, jira_rest_dir, '.idea', pom_xml, 'README.txt'])

        fileutils.expect_dir_exists(os.sep.join([workspace_dir, jira_project_dir]), toReturn=True)
        fileutils.expect_dir_exists(os.sep.join([workspace_dir, jira_rest_dir]), toReturn=True)
        fileutils.expect_dir_exists(os.sep.join([workspace_dir, '.idea']), toReturn=True)

        fileutils.expect_file_exists(os.sep.join([workspace_dir, jira_project_dir, pom_xml]), toReturn=True)
        fileutils.expect_file_exists(os.sep.join([workspace_dir, jira_rest_dir, pom_xml]), toReturn=True)

        parser_mocks = {os.sep.join([workspace_dir, jira_project_dir, pom_xml]):
                            Mock().expect_get_artifact_id(toReturn=jira_project_dir).expect_get_version(toReturn='10.0.0-SNAPSHOT'),
                        os.sep.join([workspace_dir, jira_rest_dir, pom_xml]):
                            Mock().expect_get_artifact_id(toReturn=jira_rest_dir).expect_get_version(toReturn='9.1.2-SNAPSHOT')}

        PROJECT1 = os.sep.join(['jira-components', 'jira-webapp'])
        PROJECT2 = os.sep.join(['jira-components', 'jira-plugins', 'jira-bundled-plugins'])
        maven_mock = Mock(projects = [PROJECT1, PROJECT2])

        MavenCallableWorkspaceDelegator.after_init(maven_mock, workspace_dir, fileutils,
                                                   pom_parser_class_object=lambda path: parser_mocks[path])

        for k, v in {'jira.version': '10.0.0-SNAPSHOT',
                     'jira.project.version': '10.0.0-SNAPSHOT',
                     'jira.rest.version': '9.1.2-SNAPSHOT'}.items():
            self.assertTrue(maven_mock.verify_property(k, v))

    def test_shutdown(self):
        executor = []
        args = MocArgs()
        args.control = 'shutdown'

        self.module(args, executor)

        self.assertEqual(len(executor), 1)
        self.assertIsInstance(executor[0], TomcatStopper)

    def test_restart(self):
        executor = []
        args = MocArgs()
        args.control = 'restart'

        self.module(args, executor)

        self.assertEqual(len(executor), 5)
        self.assertIsInstance(executor[0], TomcatStopper)
        self.assertIsInstance(executor[1], LocalPortInspector)
        self.assertIsInstance(executor[2], LocalPortInspector)
        self.assertIsInstance(executor[3], types.FunctionType)
        self.assertIsInstance(executor[4], TomcatStarter)

    def test_setup_jira_home_adds_task_to_executor(self):
        executor = []
        args = MocArgs(setup_home=True)

        self.module(args, executor)

        self.assertEqual(len(executor), 11)
        self.assertIsInstance(executor[2], SetupJiraHomeHsqldb)

    def test_quickstart(self):
        executor = []
        args = MocArgs()
        args.control = 'quickstart'

        self.module(args, executor)

        self.assertEqual(len(executor), 4)
        self.assertIsInstance(executor[0], LocalPortInspector)
        self.assertIsInstance(executor[1], LocalPortInspector)
        self.assertIsInstance(executor[2], types.FunctionType)
        self.assertIsInstance(executor[3], TomcatStarter)

    def test_process_local_test_settings(self):
        fileutils = MockFileUtils()

        xml_location_key = '${test.xml.location}'
        file1 = os.sep.join(['.', 'jira-func-tests', 'src', 'main', 'resources', 'localtest.properties'])
        file2 = os.sep.join(['.', 'jira-webdriver-tests', 'src', 'main', 'resources', 'localtest.properties'])
        file3 = os.sep.join(
            ['.', 'jira-distribution', 'jira-integration-tests', 'src', 'main', 'resources', 'localtest.properties'])
        template = os.sep.join(['jira-func-tests', 'src', 'main', 'resources', 'localtest.template'])
        fileutils.expect_file_exists(file2, toReturn=True)
        fileutils.expect_file_exists(file3, toReturn=True)

        args = MocArgs()
        args.port = 98765
        args.jira_context = 'substituteThis'
        process_local_test_settings(args, fileutils)(Mock())

        # overwrite all each time:
        filtered_files = fileutils.filtered_files
        self.assertEqual(3, len(filtered_files))

        for f in [file1, file2, file3]:
            rpl = fileutils.verify_filter_file(template, f)
            self.assertIsNotNone(rpl)
            self.assertIn('${jira.port}', rpl)
            self.assertEqual(str(args.port), rpl['${jira.port}'])
            self.assertIn("${jira.context}", rpl)
            self.assertEqual(args.jira_context, rpl['${jira.context}'])
            self.assertIn(xml_location_key, rpl)

        self.assertIn('jira-func-tests', fileutils.verify_filter_file(template, file1)[xml_location_key])
        self.assertIn('jira-webdriver-tests', fileutils.verify_filter_file(template, file2)[xml_location_key])
        self.assertIn('jira-func-tests', fileutils.verify_filter_file(template, file3)[xml_location_key])

    def __assert_property(self, maven_callable, key, value=None):
        self.assertIn(key, maven_callable.properties)
        self.assertEqual(maven_callable.properties[key], value)

    def __assert_common_build_params(self, executor):
        self.assertEqual(len(executor), 10, "Should have added tasks to execute")
        self.assertIsInstance(executor[0], LocalPortInspector)
        self.assertIsInstance(executor[1], LocalPortInspector)
        self.assertIsInstance(executor[2], types.FunctionType)
        self.assertIsInstance(executor[3], MavenCallable)
        maven_callable = executor[4]
        self.assertIsInstance(maven_callable, MavenCallable)
        self.assertIsInstance(executor[5], types.LambdaType)
        self.assertIsInstance(executor[6], TomcatDownloader)
        self.assertIsInstance(executor[7], ContextPreparer)
        self.assertIsInstance(executor[8], types.FunctionType)
        self.assertIsInstance(executor[9], TomcatStarter)
        self.__assert_property(maven_callable, 'skipTests', 'true')
        self.assertListEqual(maven_callable.options, ['-am'])
        self.assertIn('package', maven_callable.phases)
        self.assertIn('jira-components/jira-webapp', maven_callable.projects)
        return maven_callable

    def __assert_workspace_build_params(self, executor):
        self.assertEqual(len(executor), 11, "Should have added tasks to execute")
        self.assertIsInstance(executor[0], WorkspaceValidator)
        self.assertIsInstance(executor[1], LocalPortInspector)
        self.assertIsInstance(executor[2], LocalPortInspector)
        self.assertIsInstance(executor[3], types.FunctionType)
        self.assertIsInstance(executor[4], MavenCallable)
        maven_callable = executor[5]
        self.assertIsInstance(maven_callable, MavenCallable)
        self.assertIsInstance(executor[6], types.LambdaType)
        self.assertIsInstance(executor[7], TomcatDownloader)
        self.assertIsInstance(executor[8], ContextPreparer)
        self.assertIsInstance(executor[9], types.FunctionType)
        self.assertIsInstance(executor[10], TomcatStarter)
        self.__assert_property(maven_callable, 'skipTests', 'true')
        self.assertListEqual(maven_callable.options, ['-am'])
        self.assertIn('package', maven_callable.phases)
        self.assertIn(os.sep.join([FileUtils().get_current_dir_name(),'jira-components/jira-webapp']), maven_callable.projects)
        return maven_callable

# noinspection PyTypeChecker
class SetupJiraHomeTest(TestCase):
    def test_setup_home_dir_when_already_exists(self):
        file_utils = MockFileUtils()
        file_utils.ordered_file_exists('some-home', toReturn=True)
        args = MocArgs(setup_home=True)
        args.layout.expect_jira_home(toReturn='some-home')
        setup_jira_home = SetupJiraHomeHsqldb(args, file_utils)

        return_code = setup_jira_home(Mock())
        self.assertEqual(return_code, Callable.success, 'Invalid return code')
        file_utils.verify_all_ordered()

    def test_setup_home_dir_has_to_unzip_and_filter_file(self):
        file_utils = MockFileUtils()
        jira_home = 'some-home'
        file_utils.expect_file_exists(jira_home, toReturn=False)

        args = Mock(layout=Mock().expect_jira_home(toReturn=jira_home))
        setup_jira_home = SetupJiraHomeHsqldb(args, file_utils)

        return_code = setup_jira_home(Mock())
        self.assertEqual(return_code, Callable.success, 'Invalid return code')

        self.verify_template_is_filtered_and_removed(file_utils,
                                                     os.sep.join([jira_home, SetupJiraHomeHsqldb.DB_CONFIG]),
                                                     os.sep.join([jira_home, SetupJiraHomeHsqldb.DB_CONFIG_TEMPLATE]))
        self.verify_template_is_filtered_and_removed(file_utils,
                                                     os.sep.join([jira_home, SetupJiraHomeHsqldb.DB_DIRECTORY,
                                                                  SetupJiraHomeHsqldb.DB_SCRIPT]),
                                                     os.sep.join([jira_home, SetupJiraHomeHsqldb.DB_DIRECTORY,
                                                                  SetupJiraHomeHsqldb.DB_SCRIPT_TEMPLATE]))


    def verify_template_is_filtered_and_removed(self, file_utils, dest_file, template_xml):
        filter_dict = file_utils.verify_filter_file(template_xml, dest_file)
        self.assertIsNotNone(filter_dict,
                             'Expected call to filter_files with params %s, %s but got %s' % (
                                 template_xml, dest_file, file_utils.filtered_files))
        self.assertDictEqual(filter_dict, {'${jirahome}': 'some-home'}, 'Invalid replacement for jirahome')
        self.assertTrue((file_utils.verify_remove(template_xml)), 'The file %s was not removed' % template_xml)


if __name__ == '__main__':
    unittest.main(verbosity=2)
