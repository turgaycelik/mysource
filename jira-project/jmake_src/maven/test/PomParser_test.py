from unittest import TestCase
import unittest
from maven.PomParser import PomParser


class TestPomParser(TestCase):
    parser = PomParser('jmake_src/testdata/pomparser.xml')

    def test_get_version(self):
        version = self.parser.get_version()
        self.assertEqual(version, '10.0.0')

    def test_get_artifact_id(self):
        artifact_id = self.parser.get_artifact_id()
        self.assertEqual(artifact_id, 'jira-project')

    def test_get_properties_return_property_value(self):
        value = self.parser.get_property('test.property')
        self.assertEqual(value, 'value1', 'Expected value to be defined')

    def test_get_properties_when_no_property_defined_returns_none(self):
        value = self.parser.get_property('test.property.new')
        self.assertIsNone(value, 'Expected value to be undefined')


if __name__ == '__main__':
    unittest.main(verbosity=2)