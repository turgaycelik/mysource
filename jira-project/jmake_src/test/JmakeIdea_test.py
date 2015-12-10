import os
from unittest import TestCase
import xml.etree.ElementTree as XML

from CommandExecutor import Callable
from JmakeIdea import get_targets_for, ensure_project_was_opened, process_clean, process_dev_profiles, process_run_configs, process_project_local_settings, process_jmake_module, process_compiler_settings
from Logger import Logger
from utils.Mocks import Mock
from utils.FileUtils import MockFileUtils
from utils.XmlUtils import MockXmlUtils


class JmakeIdeaTest(TestCase):
    def setUp(self):
        self.logger = Logger().set_none()
        self.fileutils = MockFileUtils()
        self.xml = MockXmlUtils()

    def test_targets_for_projects(self):
        structure = {'files': ['pom.xml'],
                     'target': {},
                     'project1': {'files': ['pom.xml'],
                                  'target': {}},
                     'project2': {'target': {}},
                     'project3': {'files': ['pom.xml']}}
        self.fileutils.expect_walk('.', structure)

        self.assertListEqual(sorted(get_targets_for(['.'], self.fileutils)), sorted([os.sep.join(['root', 'target']),
                                                                                os.sep.join(
                                                                                    ['root', 'project1', 'target']),
                                                                                os.sep.join(
                                                                                    ['root', 'project3', 'target'])]))

    def test_project_opened(self):
        self.assertEqual(Callable.do_not_proceed, ensure_project_was_opened(self.logger, self.fileutils))
        self.fileutils.expect_dir_exists('.%s.idea' % os.sep, toReturn=True)
        self.assertEqual(Callable.success, ensure_project_was_opened(self.logger, self.fileutils))


    def test_jmake_idea_clean(self):

        process_clean(self.logger, self.fileutils)
        # no files existed, so none were removed:
        self.assertEqual(self.fileutils.callcount_remove_dir(), 0)
        self.assertEqual(self.fileutils.callcount_remove(), 0)

        files = [ os.sep.join(['.', '.idea', 'runConfigurations', 'Tomcat_6.xml']),
                  os.sep.join(['.', '.idea', 'runConfigurations', 'JIRA_OnDemand.xml']),
                  os.sep.join(['.', '.idea', 'runConfigurations', 'Selenium_Tests.xml']),
                  os.sep.join(['.', '.idea', 'artifacts', 'JIRA.xml']),
                  os.sep.join(['jira-ide-support', 'src', 'main', 'resources', 'jira.idea.properties'])]

        dirs = [ os.sep.join(['.', 'tomcatBase']), os.sep.join(['.', 'classes']) ]
        for file in files: self.fileutils.expect_file_exists(file, toReturn=True)
        for dir in dirs: self.fileutils.expect_dir_exists(dir, toReturn=True)
        process_clean(self.logger, self.fileutils)

        # the existing files/dirs should be deleted, but only those.
        for file in files: self.assertTrue(self.fileutils.verify_remove(file))
        self.assertEqual(len(files), self.fileutils.callcount_remove())
        for dir in dirs: self.assertTrue(self.fileutils.verify_remove_dir(dir))
        self.assertEqual(len(dirs), self.fileutils.callcount_remove_dir())

    def test_javac_memory_setter(self):

        file = os.sep.join(['.idea', 'compiler.xml'])
        element = XML.Element('project', {'version':'4'})

        self.xml.expect_parse(file, element)
        process_compiler_settings(Mock(force=False), self.xml)(self.logger)

        def expect_javac_settings(how_much):
            self.assertEqual(len(element), 2)
            child = element[0]
            self.assertEqual(child.tag, 'component')
            self.assertEqual(len(child), 1)
            child = child[0]
            self.assertEqual(child.tag, 'option')
            self.assertIn('name', child.attrib)
            self.assertEqual(child.attrib['name'], 'MAXIMUM_HEAP_SIZE')
            self.assertIn('value', child.attrib)
            self.assertEqual(child.attrib['value'], how_much)

        # element should be created:
        expect_javac_settings('512')
        child = element[0][0]

        child.attrib['value'] = '256'
        process_compiler_settings(Mock(force=False), self.xml)(self.logger)
        expect_javac_settings('512')

        child.attrib['value'] = '768'
        process_compiler_settings(Mock(force=False), self.xml)(self.logger)
        expect_javac_settings('768')

        process_compiler_settings(Mock(force=True), self.xml)(self.logger)
        expect_javac_settings('512')

    def test_install_dev_profiles(self):

        file = os.sep.join(['.', '.idea', 'workspace.xml'])
        element = XML.Element('project', {'version':'4'})
        self.xml.expect_parse(file, element)

        process_dev_profiles(self.xml)(self.logger)

        list = element[0][0][0]
        self.assertEqual(len(list), 3)
        profiles = ['func-mode-plugins', 'pseudo-loc', 'dev-mode-plugins']
        for elem in list:
            self.assertIn(elem.attrib['value'], profiles)

        list.remove(list[0])
        process_dev_profiles(self.xml)(self.logger)
        self.assertEqual(len(list), 3)

    def test_install_run_configs(self):

        workspace_file = os.sep.join(['.idea', 'workspace.xml'])
        ws_root = XML.Element('project', {'version':'4'})
        ws_component = self.xml.produce(ws_root, ('component',  {'name': 'RunManager'}))
        for cfg in ['cfg1', 'cfg4', 'another']:
            self.xml.produce(ws_component, ('configuration', {'default': 'false', 'name': cfg}))

        num_cfg = 6
        idea_runners_file = os.sep.join(
            ['jira-ide-support', 'src', 'main', 'resources', 'ideaTemplates', 'runConfigurations.xml'])
        idea_root = XML.Element('project', {'version':'4'})
        idea_component = self.xml.produce(idea_root, ('component',  {'name': 'RunManager'}))
        for cfg in ('cfg' + str(e) for e in range(num_cfg)):
            self.xml.produce(idea_component, ('configuration', {'default': 'false', 'name': cfg}))

        self.xml.expect_parse(workspace_file, ws_root)
        self.xml.expect_parse(idea_runners_file, idea_root)

        args = Mock(force = False)
        process_run_configs(args, self.xml)(self.logger)

        self.assertEqual(len(ws_component), num_cfg + 1)
        cfg2 = self.xml.produce(ws_component, ('configuration', {'name': 'cfg2'}))
        cfg2.attrib['is-sad'] = 'notsomuch'

        process_run_configs(args, self.xml)(self.logger)
        self.assertEqual(len(ws_component), num_cfg + 1)
        cfg2 = self.xml.produce(ws_component, ('configuration', {'name': 'cfg2'}))
        self.assertEqual(cfg2.attrib['is-sad'], 'notsomuch')
        cfg2.attrib['snowflakes'] = 'omg-so-many!'

        args.force = True
        process_run_configs(args, self.xml)(self.logger)
        self.assertEqual(len(ws_component), num_cfg + 1)
        cfg2 = self.xml.produce(ws_component, ('configuration', {'name': 'cfg2'}))
        self.assertNotIn('is-sad', cfg2.attrib)
        self.assertNotIn('snowflakes', cfg2.attrib)

    def test_install_project_local_settings(self):

        workspace_codestyle_file = os.sep.join(['.idea', 'codeStyleSettings.xml'])

        element = XML.Element('project', {'version':'4'})
        self.xml.produce(element,
                         ('component', {'name': 'ProjectCodeStyleSettingsManager'}),
                         ('option', {'name': 'USE_PER_PROJECT_SETTINGS', 'value': 'true'}))
        self.xml.expect_parse(workspace_codestyle_file,element)

        process_project_local_settings(Mock(force=False), self.fileutils, self.xml)(self.logger)

        self.assertEqual(self.fileutils.callcount_copy_file(), 0)
        self.assertEqual(self.fileutils.callcount_touch(), 0)

        element = XML.Element('project', {'version':'4'})
        self.xml.produce(element,
            ('component', {'name': 'ProjectCodeStyleSettingsManager'}),
            ('option', {'name': 'USE_PER_PROJECT_SETTINGS', 'value': 'false'}))
        self.xml.expect_parse(workspace_codestyle_file,element)

        process_project_local_settings(Mock(force=False), self.fileutils, self.xml)(self.logger)

        self.assertEqual(self.fileutils.callcount_copy_file(), 1)
        self.assertTrue(self.fileutils.verify_copy_file(
            os.sep.join(['jira-ide-support', 'src', 'main', 'resources', 'ideaTemplates', 'codeStyleSettings.xml']),
            os.sep.join(['.idea', 'codeStyleSettings.xml'])))
        self.assertEqual(self.fileutils.callcount_touch(), 1)
        self.assertTrue(self.fileutils.verify_touch(os.sep.join(['.idea', 'workspace.xml'])))

    def test_installing_jmake_module(self):

        workspace_modules_file = os.sep.join(['.', '.idea', 'modules.xml'])
        element = XML.Element('project', {'version':'4'})
        self.xml.expect_parse(workspace_modules_file,  element)

        # test that it is created:
        self.fileutils = MockFileUtils()
        process_jmake_module(Mock(force=False), self.fileutils, self.xml)(self.logger)
        self.assertEqual(self.fileutils.callcount_copy_file(), 1)
        module_list = element[0][0]
        self.assertEqual(len(module_list), 1)
        module = module_list[0]
        self.assertEqual(module.tag, 'module')
        self.assertIn('filepath', module.attrib)
        self.assertIn('fileurl', module.attrib)

        # test that it is not overridden:
        self.fileutils = MockFileUtils()
        module.attrib['angry'] = 'nerdz'
        process_jmake_module(Mock(force=False), self.fileutils, self.xml)(self.logger)
        self.assertEqual(self.fileutils.callcount_copy_file(), 1)
        module = module_list[0]
        self.assertEqual(module.tag, 'module')
        self.assertIn('filepath', module.attrib)
        self.assertIn('fileurl', module.attrib)
        self.assertIn('angry', module.attrib)
        self.assertEqual(module.attrib['angry'], 'nerdz')

        # test, that it will not be created when iml file exists
        module_list.remove(module)
        self.fileutils = MockFileUtils()
        self.fileutils.expect_file_exists(os.sep.join(['.', 'jmake_src', 'jmake_src.iml']), toReturn=True)
        process_jmake_module(Mock(force=False), self.fileutils, self.xml)(self.logger)
        self.assertEqual(self.fileutils.callcount_copy_file(), 0)
        self.assertEqual(len(module_list), 0)

        # force should override that
        process_jmake_module(Mock(force=True), self.fileutils, self.xml)(self.logger)
        self.assertEqual(self.fileutils.callcount_copy_file(), 1)
        self.assertEqual(len(module_list), 1)
