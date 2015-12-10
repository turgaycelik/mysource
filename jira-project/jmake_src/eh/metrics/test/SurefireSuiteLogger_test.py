import os
from unittest import TestCase
from eh.metrics.SurefireSuiteLogger import SurefireSuiteLogger
from utils.FileUtils import MockFileUtils
from utils.Mocks import Mock

#noinspection PyTypeChecker
class TestSurefireSuiteLogger(TestCase):

    def test_save(self):
        fs = MockFileUtils()

        expected_content = [
            '<?xml version="1.0" encoding="UTF-8"?>',
            '<testsuite failures="0" time="0" errors="0" skipped="0" tests="0" name="suiteName">',
            '<properties/>',
            '</testsuite>'
        ]

        expected_file_name = os.sep.join([SurefireSuiteLogger.target_dir, "TEST-suiteName.xml"])
        fs.expect_write_lines(expected_file_name, expected_content, toReturn = None)
        logger = SurefireSuiteLogger('suiteName', fs)
        logger.save()
        self.assertTrue(fs.verify_write_lines(expected_file_name, expected_content))

    def test_to_xml_with_failed_test(self):
        expected_xml = [
            '<?xml version="1.0" encoding="UTF-8"?>',
            '<testsuite failures="1" time="0.01" errors="0" skipped="0" tests="1" name="suiteName">',
            '<properties/>',
            '<testcase time="0.01" classname="className" name="failedTest">',
            '<failure>log lines</failure>',
            '</testcase>',
            '</testsuite>'
        ]

        logger = SurefireSuiteLogger('suiteName', Mock())
        logger.failed(name="failedTest", time=0.01, classname="className", log="log lines")

        self.assertEqual(expected_xml, logger.to_xml())

    def test_to_xml_with_successful_test(self):
        expected_xml = [
            '<?xml version="1.0" encoding="UTF-8"?>',
            '<testsuite failures="0" time="0.02" errors="0" skipped="0" tests="1" name="suiteName">',
            '<properties/>',
            '<testcase time="0.02" classname="className" name="successfulTest">',
            '</testcase>',
            '</testsuite>'
        ]

        logger = SurefireSuiteLogger('suiteName', Mock())
        logger.success(name="successfulTest", time=0.02, classname="className")

        self.assertEqual(expected_xml, logger.to_xml())

    def test_to_xml_with_successful_and_failed_test(self):
        expected_xml = [
            '<?xml version="1.0" encoding="UTF-8"?>',
            '<testsuite failures="1" time="0.03" errors="0" skipped="0" tests="2" name="suiteName">',
            '<properties/>',
            '<testcase time="0.02" classname="className" name="successfulTest">',
            '</testcase>',
            '<testcase time="0.01" classname="className" name="failedTest">',
            '<failure>log lines</failure>',
            '</testcase>',
            '</testsuite>'
        ]

        logger = SurefireSuiteLogger('suiteName', Mock())
        logger.success(name="successfulTest", time=0.02, classname="className")
        logger.failed(name="failedTest", time=0.01, classname="className", log="log lines")
        
        self.assertEqual(expected_xml, logger.to_xml())