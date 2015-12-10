import os
import os
from unittest import TestCase
import unittest
from Logger import LOG
from BundledPluginsUtility import BundledPluginsUtility, BUNDLED_PLUGINS_LIST, BUNDLED_PLUGINS_POM, JIRA_PLUGINS_DIR_ABS, PLUGIN_TARGET_DIR, PLUGIN_SRC_DIR, PLUGIN_POM_DIR, JMAKE_PROFILES_PLUGINS_LIST
from utils.FileUtils import MockFileUtils


class TestBundledPluginsUtility(TestCase):
    @classmethod
    def setUpClass(cls):
        LOG.set_none()

    def setUp(self):
        self.file_utils = MockFileUtils()
        self.finder = BundledPluginsUtility(self.file_utils)

    def test_bundled_plugins_recompilation_when_no_list(self):
        plugins = self.finder.find_plugins_to_recompile([])

        self.assertListEqual(plugins, ['jira-components/jira-plugins/jira-bundled-plugins'])

    def test_bundled_plugins_list_is_not_up_to_date(self):
        self.file_utils.expect_file_exists(BUNDLED_PLUGINS_LIST, toReturn=True)
        self.file_utils.expect_getmtime(BUNDLED_PLUGINS_LIST, toReturn=100)
        self.file_utils.expect_getmtime(BUNDLED_PLUGINS_POM, toReturn=101)

        plugins = self.finder.find_plugins_to_recompile([])
        self.assertListEqual(plugins, ['jira-components/jira-plugins/jira-bundled-plugins'])

    def test_bundled_plugins_recompilation_when_not_all_in_local_repo(self):
        self.prepare_bp_ok()
        self.prepare_plugin_that_should_not_be_recompiled()
        self.file_utils.expect_read_lines(BUNDLED_PLUGINS_LIST, toReturn=['file1', 'file2'])
        self.file_utils.expect_file_exists('file1', toReturn=True)
        plugins = self.finder.find_plugins_to_recompile([])
        self.assertListEqual(plugins, ['jira-components/jira-plugins/jira-bundled-plugins'])

    def test_bundled_plugins_recompilation_profiles_does_not_equal(self):
        self.prepare_bp_ok()
        self.prepare_plugin_that_should_not_be_recompiled()
        self.file_utils.expect_read_lines(BUNDLED_PLUGINS_LIST, toReturn=['file1', 'file2'])
        self.file_utils.expect_file_exists(JMAKE_PROFILES_PLUGINS_LIST, toReturn=True)
        self.file_utils.expect_read_lines(JMAKE_PROFILES_PLUGINS_LIST, toReturn=['profiles1', 'profile2'])
        self.file_utils.expect_file_exists('file1', toReturn=True)
        self.file_utils.expect_file_exists('file2', toReturn=True)
        plugins = self.finder.find_plugins_to_recompile(['profile1', 'profile3'])
        self.assertListEqual(plugins, ['jira-components/jira-plugins/jira-bundled-plugins'])

    def test_bundled_plugins_recompilation_when_old_profiles_files_does_not_exists(self):
        self.prepare_bp_ok()
        self.prepare_plugin_that_should_not_be_recompiled()
        self.file_utils.expect_read_lines(BUNDLED_PLUGINS_LIST, toReturn=['file1', 'file2'])
        self.file_utils.expect_file_exists('file1', toReturn=True)
        self.file_utils.expect_file_exists('file2', toReturn=True)
        plugins = self.finder.find_plugins_to_recompile(['profile1', 'profile3'])
        self.assertListEqual(plugins, ['jira-components/jira-plugins/jira-bundled-plugins'])

    def test_bundled_plugins_are_not_recompiled_when_profiles_equal(self):
        self.prepare_bp_ok()

        self.prepare_plugin_that_should_not_be_recompiled()
        self.file_utils.expect_read_lines(BUNDLED_PLUGINS_LIST, toReturn=['file1', 'file2'])
        self.file_utils.expect_file_exists(JMAKE_PROFILES_PLUGINS_LIST, toReturn=True)
        self.file_utils.expect_read_lines(JMAKE_PROFILES_PLUGINS_LIST, toReturn=['profile1', 'profile2'])
        self.file_utils.expect_file_exists('file1', toReturn=True)
        self.file_utils.expect_file_exists('file2', toReturn=True)
        plugins = self.finder.find_plugins_to_recompile(['profile2', 'profile1'])
        self.assertListEqual(plugins, [])

    def test_plugin_should_be_recompile_when_jar_does_not_exist(self):
        self.prepare_bp_ok()
        self.prepare_plugin_that_should_not_be_recompiled()
        self.file_utils.expect_read_lines(BUNDLED_PLUGINS_LIST,
            toReturn=[JIRA_PLUGINS_DIR_ABS + os.sep + 'module1' + os.sep + 'target' + os.sep + 'test.jar'])
        self.file_utils.expect_file_exists(JMAKE_PROFILES_PLUGINS_LIST, toReturn=True)
        self.file_utils.expect_read_lines(JMAKE_PROFILES_PLUGINS_LIST, toReturn=[])
        plugins = self.finder.find_plugins_to_recompile([])

        self.assertListEqual(plugins, ['jira-components/jira-plugins/module1'])

    def test_plugin_should_be_recompiled_when_src_newer_than_target(self):
        self.prepare_bp_ok()
        self.prepare_plugin_that_should_not_be_recompiled()
        self.file_utils.expect_file_exists(JMAKE_PROFILES_PLUGINS_LIST, toReturn=True)
        self.file_utils.expect_read_lines(JMAKE_PROFILES_PLUGINS_LIST, toReturn=[])
        module_name = 'module1'
        plugin_jar = JIRA_PLUGINS_DIR_ABS + os.sep + module_name + os.sep + 'target' + os.sep + 'test.jar'
        self.file_utils.expect_read_lines(BUNDLED_PLUGINS_LIST, toReturn=[plugin_jar])
        self.file_utils.expect_file_exists(plugin_jar, toReturn=True)
        self.file_utils.expect_getmtime('root' + os.sep + 'src', toReturn=200)
        self.file_utils.expect_getmtime('root' + os.sep + 'target', toReturn=100)
        self.file_utils.expect_walk(PLUGIN_TARGET_DIR.format(module_name), {'files': ['target']})
        self.file_utils.expect_walk(PLUGIN_SRC_DIR.format(module_name), {'files': ['src']})
        plugins = self.finder.find_plugins_to_recompile([])

        self.assertListEqual(plugins, ['jira-components/jira-plugins/module1'])

    def test_plugin_should_be_recompiled_when_jar_older_than_source(self):
        self.prepare_bp_ok()
        self.prepare_plugin_that_should_not_be_recompiled()
        self.file_utils.expect_file_exists(JMAKE_PROFILES_PLUGINS_LIST, toReturn=True)
        self.file_utils.expect_read_lines(JMAKE_PROFILES_PLUGINS_LIST, toReturn=[])
        module_name = 'module1'
        plugin_jar = JIRA_PLUGINS_DIR_ABS + os.sep + module_name + os.sep + 'target' + os.sep + 'test.jar'
        self.file_utils.expect_read_lines(BUNDLED_PLUGINS_LIST, toReturn=[plugin_jar])
        self.file_utils.expect_file_exists(plugin_jar, toReturn=True)
        self.file_utils.expect_getmtime('root' + os.sep + 'src', toReturn=200)
        self.file_utils.expect_getmtime('root' + os.sep + 'target', toReturn=300)
        self.file_utils.expect_getmtime(plugin_jar, toReturn=100)
        self.file_utils.expect_walk(PLUGIN_TARGET_DIR.format(module_name), {'files': ['target']})
        self.file_utils.expect_walk(PLUGIN_SRC_DIR.format(module_name), {'files': ['src']})
        plugins = self.finder.find_plugins_to_recompile([])

        self.assertListEqual(plugins, ['jira-components/jira-plugins/module1'])

    def test_plugin_should_be_recompiled_when_pom_newer_than_target(self):
        module_name = 'module1'
        self.prepare_bp_ok()
        self.prepare_plugin_that_should_not_be_recompiled()
        self.file_utils.expect_file_exists(JMAKE_PROFILES_PLUGINS_LIST, toReturn=True)
        self.file_utils.expect_read_lines(JMAKE_PROFILES_PLUGINS_LIST, toReturn=[])
        plugin_jar = JIRA_PLUGINS_DIR_ABS + os.sep + module_name + os.sep + 'target' + os.sep + 'test.jar'
        self.file_utils.expect_read_lines(BUNDLED_PLUGINS_LIST, toReturn=[plugin_jar])
        self.file_utils.expect_file_exists(plugin_jar, toReturn=True)
        self.file_utils.expect_getmtime('root' + os.sep + 'src', toReturn=100)
        self.file_utils.expect_getmtime('root' + os.sep + 'target', toReturn=200)
        self.file_utils.expect_getmtime(PLUGIN_POM_DIR.format(module_name), toReturn=300)
        self.file_utils.expect_walk(PLUGIN_TARGET_DIR.format(module_name), {'files': ['target']})
        self.file_utils.expect_walk(PLUGIN_SRC_DIR.format(module_name), {'files': ['src']})

        plugins = self.finder.find_plugins_to_recompile([])

        self.assertListEqual(plugins, ['jira-components/jira-plugins/module1'])

    def test_remember_plugin_profiles_writes_file(self):
        profiles = ['p1', 'p2', 'p3']

        self.finder.remember_plugins_profiles(profiles)
        self.assertTrue(self.file_utils.verify_write_lines(JMAKE_PROFILES_PLUGINS_LIST, profiles))

    def prepare_bp_ok(self):
        self.file_utils.expect_file_exists(BUNDLED_PLUGINS_LIST, toReturn=True)
        self.file_utils.expect_getmtime(BUNDLED_PLUGINS_LIST, toReturn=101)
        self.file_utils.expect_getmtime(BUNDLED_PLUGINS_POM, toReturn=100)

    def prepare_plugin_that_should_not_be_recompiled(self):
        """
        This method adds some noise to files structure to make sure that 'good' plugins are not recompiled
        """
        module_name = 'goodModule'
        plugin_jar = JIRA_PLUGINS_DIR_ABS + os.sep + module_name + os.sep + 'target' + os.sep + 'test.jar'
        self.file_utils.expect_read_lines(BUNDLED_PLUGINS_LIST, toReturn=[plugin_jar])
        self.file_utils.expect_file_exists(plugin_jar, toReturn=True)
        self.file_utils.expect_getmtime('root' + os.sep + 'srcGood', toReturn=100)
        self.file_utils.expect_getmtime('root' + os.sep + 'targetGood', toReturn=200)
        self.file_utils.expect_getmtime(PLUGIN_POM_DIR.format(module_name), toReturn=150)
        self.file_utils.expect_walk(PLUGIN_TARGET_DIR.format(module_name), {'files': ['targetGood']})
        self.file_utils.expect_walk(PLUGIN_SRC_DIR.format(module_name), {'files': ['srcGood']})

    def test_bundled_plugins_recompilation_added_new_profile(self):
        self.prepare_bp_ok()
        self.prepare_plugin_that_should_not_be_recompiled()
        self.file_utils.expect_read_lines(BUNDLED_PLUGINS_LIST, toReturn=['file1', 'file2'])
        self.file_utils.expect_file_exists(JMAKE_PROFILES_PLUGINS_LIST, toReturn=True)
        self.file_utils.expect_read_lines(JMAKE_PROFILES_PLUGINS_LIST, toReturn=['profile1', 'profile2'])
        self.file_utils.expect_file_exists('file1', toReturn=True)
        self.file_utils.expect_file_exists('file2', toReturn=True)
        plugins = self.finder.find_plugins_to_recompile(['profile1', 'profile2', 'profile3'])
        self.assertListEqual(plugins, ['jira-components/jira-plugins/jira-bundled-plugins'])

if __name__ == '__main__':
    unittest.main(verbosity=2)