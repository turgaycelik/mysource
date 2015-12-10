from unittest import TestCase
from eh.metrics.tailored.BadPluginMetrics import MissingHostComponentsXml, MissingOsgiManifest, PluginXmlMinified
from eh.metrics.test.TestResourceUtils import TestResourcesUtils
from utils.Mocks import Mock

#noinspection PyTypeChecker
class TestMissingComponentImportsXml(TestCase):
    def test_component_import_xml_present(self):
        metric = MissingHostComponentsXml('test', 'description of a test', metrics_logger=Mock()).configure(Mock(), False)
        metric.pre_files_scan('good_plugin')
        metric.wants_file('META-INF/spring/atlassian-plugins-host-components.xml')
        metric.post_files_scan('good_plugin')
        self.assertEqual(0, metric.value, "Shouldn't have a hit")

    def test_component_import_xml_absent(self):
        metric = MissingHostComponentsXml('test', 'description of a test', metrics_logger=Mock()).configure(Mock(), False)
        metric.pre_files_scan('bad_plugin')
        metric.post_files_scan('bad_plugin')
        self.assertEqual(1, metric.value, "Should have a hit")

class TestMissingOsgiManifest(TestCase):
    def test_no_manifest_found(self):
        metric = MissingOsgiManifest('test', 'description of a test', metrics_logger=Mock()).configure(Mock(), False)
        metric.pre_files_scan('bad_plugin')
        metric.post_files_scan('bad_plugin')
        self.assertEqual(1, metric.value, "Should have a hit")

    def test_manifest_found_no_instructions(self):
        metric = MissingOsgiManifest('test', 'description of a test', metrics_logger=Mock()).configure(Mock(), False)
        metric.pre_files_scan('bad_plugin')
        self.assertEqual(metric.wants_file('META-INF/MANIFEST.MF'), True, "Should want to look at manifest")
        metric.on_read_line("this is a manifest, but it's a bad one.")
        metric.post_files_scan('bad_plugin')
        self.assertEqual(1, metric.value, "Should have a hit")

    def test_manifest_found_with_instructions(self):
        metric = MissingOsgiManifest('test', 'description of a test', metrics_logger=Mock()).configure(Mock(), False)
        metric.pre_files_scan('good_plugin')
        self.assertEqual(metric.wants_file('META-INF/MANIFEST.MF'), True, "Should want to look at manifest")
        metric.on_read_line("Import-Package: com.atlassian.awesome:1.0")
        self.assertEqual(0, metric.value, "Shouldn't have a hit")

class TestPluginXmlMinified(TestCase):

    minified_plugin_xml = TestResourcesUtils.build_test_file_path(['testdata', 'minified-atlassian-plugin.xml'])
    original_plugin_xml = TestResourcesUtils.build_test_file_path(['testdata', 'original-atlassian-plugin.xml'])

    def test_plugin_xml_minified(self):
        metric = PluginXmlMinified('test', 'plugin xml minified test', metrics_logger=Mock()).configure(Mock(), False)
        metric.pre_files_scan('good_plugin')
        # like file count, wants_file will actually read the file
        self.assertEqual(metric.wants_file(TestPluginXmlMinified.minified_plugin_xml), True, "Wants the plugin xml.")

        with open(TestPluginXmlMinified.minified_plugin_xml) as f:
            line = f.readline()
            while line:
                metric.on_read_line(line)
                line = f.readline()

        metric.post_files_scan('good_plugin')
        self.assertEqual(0, metric.value, "Shouldn't have a hit")

    def test_plugin_xml_unminified(self):
        metric = PluginXmlMinified('test', 'plugin xml minified test', metrics_logger=Mock()).configure(Mock(), False)
        metric.pre_files_scan('bad_plugin')
        # like file count, wants_file will actually read the file
        self.assertEqual(metric.wants_file(TestPluginXmlMinified.original_plugin_xml), True, "Wants the plugin xml.")

        with open(TestPluginXmlMinified.original_plugin_xml) as f:
            line = f.readline()
            while line:
                self.assertEqual(metric.on_read_line(line), True, "Should want all the lines")
                line = f.readline()

        self.assertEqual(metric.wants_file("META-INF/MANIFEST.MF"), False, "Doesn't want another file.")
        metric.post_files_scan('bad_plugin')
        self.assertEqual(1, metric.value, "Should have a hit")

    def test_plugin_xml_unminified_in_two_modules(self):
        metric = PluginXmlMinified('test', 'plugin xml minified test', metrics_logger=Mock()).configure(Mock(), False)
        # scan first module
        metric.pre_files_scan('bad_plugin')
        self.assertEqual(metric.wants_file('atlassian-plugin.xml'), True, "Wants the plugin xml.")
        metric.on_read_line('<?xml version="1.0" ?>\n<xml/>')
        self.assertEqual(metric.wants_file("atlassian-plugin.xml"), False, "Doesn't want another file.")
        metric.post_files_scan('bad_plugin')
        self.assertEqual(1, metric.value, "Should have a hit")

        # scan seconds module
        metric.pre_files_scan('second_bad_plugin')
        # vvv FAILS HERE vvv
        self.assertEqual(metric.wants_file('atlassian-plugin.xml'), True, "Wants the plugin xml.")
        metric.on_read_line('<?xml version="1.0" ?>\n<xml/>')
        self.assertEqual(metric.wants_file("atlassian-plugin.xml"), False, "Doesn't want another file.")
        metric.post_files_scan('second_bad_plugin')
        # vvv WOULD FAIL HERE TOO vvv
        self.assertEqual(2, metric.value, "Should have a hit")