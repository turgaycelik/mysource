import os
from unittest import TestCase
import unittest
from utils.Mocks import Mock
from maven.Maven import JavaOptionParser, MavenCallable

class MockLogger(object):
    def __init__(self):
        super().__init__()
        self.warnings = []
        self.debugs = []
        self.infos = []

    def warn(self, msg):
        self.warnings.append(msg)

    def info(self, msg):
        self.infos.append(msg)

    def color_text(self, msg, color):
        return msg

    def debug(self, msg):
        self.debugs.append(msg)

class TestMavenCallable(TestCase):
    def setUp(self):
        os.environ = {}
        self.logger = Mock()
        super().setUp()

    def test_information_about_maven_and_java_is_logged(self):
        utils = Mock().default_check_output('Maven:3.3.2\nJava home:jdk_home\n:Java version 1.2.3_123'.encode())
        maven = MavenCallable(process_utils=utils)
        maven.require_mvn3()
        maven.maven_version.command(self.logger)
        self.assertIn('mvn_cmd: "mvn3", mvn_ver: "3.3.2", java_ver: "1.2.3_123", java_home: "jdk_home"',
            self.logger.info.made_calls[0])


class TestJavaOptionParser(TestCase):
    def setUp(self):
        super().setUp()
        self.logger = Mock()

    def test_random_numbers_in_property(self):
        parser = JavaOptionParser('-Xms128m -Xmx512m', self.logger)

        parser.merge('-Xms256m -Xmx24m -Dfile.encoding=ISO-1-12')

        self.assertEqual('-Dfile.encoding=ISO-1-12 -Xms256m -Xmx24m', parser.get_options())
        self.assertEqual(1, self.logger.callcount_warn(), "Unexpected number of warning messages")
        self.assertEqual(1, self.logger.callcount_debug(), "Unexpected number of debug messages")

    def test_unknown_unit(self):
        parser = JavaOptionParser('-Xms128p -Xmx512m', self.logger)

        parser.merge('-Xms256m -Xmx24m')

        self.assertEqual('-Xms128p -Xms256m -Xmx24m', parser.get_options())
        self.assertEqual(1, self.logger.callcount_warn(), "Unexpected number of warning messages")
        self.assertEqual(0, self.logger.callcount_debug(), "Unexpected number of debug messages")

    def test_higher_memory_limits_overrides_defaults_without_warning(self):
        parser = JavaOptionParser(' -Xms128m -Xmx512m     -XX:MaxPermSize=256m ', self.logger)

        parser.merge('-Xms256m -Xmx1024m -XX:MaxPermSize=512m -Xbatch')

        self.assertEqual('-XX:MaxPermSize=512m -Xbatch -Xms256m -Xmx1024m', parser.get_options())
        self.assertEqual(0, self.logger.callcount_warn(), "Unexpected number of warning messages")
        self.assertEqual(3, self.logger.callcount_debug(), "Unexpected number of debug messages")

    def test_lower_memory_limits_overrides_defaults_with_warning(self):
        parser = JavaOptionParser('-Xms128m -Xmx512m -XX:MaxPermSize=256m', self.logger)

        parser.merge('-Xms56m -Xmx24t -XX:MaxPermSize=512m')

        self.assertEqual('-XX:MaxPermSize=512m -Xms56m -Xmx24t', parser.get_options())
        self.assertEqual(1, self.logger.callcount_warn(), "Unexpected number of warning messages")
        self.assertEqual(2, self.logger.callcount_debug(), "Unexpected number of debug messages")


    def test_additional_parameters_are_added_seamlessly(self):
        parser = JavaOptionParser('-Xms128m -Xmx512m -XX:MaxPermSize=256m', self.logger)

        parser.merge('-XX:someOption -Xas154mb')

        self.assertEqual('-XX:MaxPermSize=256m -XX:someOption -Xas154mb -Xms128m -Xmx512m', parser.get_options())
        self.assertEqual(0, self.logger.callcount_warn(), "Unexpected number of warning messages")
        self.assertEqual(0, self.logger.callcount_debug(), "Unexpected number of debug messages")


    def test_parameters_with_different_units_are_considered(self):
        parser = JavaOptionParser('-Xms128 -Xmx512k -Xa2t -XX:MaxPermSize=256g', self.logger)

        parser.merge('-Xms1g -Xmx512m -Xa2 -XX:MaxPermSize=256')

        self.assertEqual('-XX:MaxPermSize=256 -Xa2 -Xms1g -Xmx512m', parser.get_options())

        self.assertEqual(2, self.logger.callcount_warn(), "Unexpected number of warning messages")
        self.assertEqual(2, self.logger.callcount_debug(), "Unexpected number of debug messages")

if __name__ == "__main__":
    unittest.main(verbosity=2)