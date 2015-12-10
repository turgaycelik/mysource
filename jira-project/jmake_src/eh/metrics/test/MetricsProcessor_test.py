import os
from unittest import TestCase
import unittest
from CommandExecutor import Callable
from eh.metrics import STATS_REF_NAME, STATS_EXCLUSION_REF_NAME, FALLING, RISING
from eh.metrics.MetricsProcessor import MetricsProcessor
from utils.DataBean import DataBean
from utils.FileUtils import MockFileUtils
from utils.Mocks import Mock

class MockFile(Mock):

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        pass


# noinspection PyTypeChecker
class TestMetricsProcessor(TestCase):

    def setUp(self):
        pass

    def test_metrics_processor(self):
        processor = MetricsProcessor()

        logger = Mock()

        module = 'module1'

        collector = Mock()
        collector.expect_get_values(toReturn={'value1' : 23})
        collector.expect_wants_file('a', toReturn=True)
        collector.expect_wants_file('b', toReturn=False)
        collector.expect_on_read_line('aline', toReturn=False)

        file_a = MockFile()
        file_a.expect_readline(toReturn='aline')

        module_description = Mock()
        module_description.expect_get_collectors(toReturn=[collector])
        module_description.expect_measured_modules(toReturn=[module])
        module_description.expect_files(module, toReturn=['a', 'b'])
        module_description.expect_open_file('a', toReturn=file_a)

        module_descriptions = [module_description]

        args = Mock(matching=None, buildno = 'ORLY-9')

        result = DataBean()

        processor.process_metrics(args, module_descriptions, result)(logger)

        self.assertTrue(module_description.verify_prepare_environment(logger))
        self.assertTrue(collector.verify_pre_files_scan(module))
        self.assertTrue(collector.verify_on_read_line('aline'))
        self.assertEqual(1, collector.callcount_on_read_line())
        self.assertTrue(collector.verify_post_files_scan(module))
        self.assertDictEqual({'jira.stats.value1' : 23}, result.metrics)
        self.assertEqual('ORLY-9', result.build_number)


    def test_metrics_processor_does_not_call_prepare_module_when_all_collectors_are_filtered_out(self):
        processor = MetricsProcessor()

        logger = Mock()

        module = 'module1'

        collector = Mock(key='filtered')

        module_description = Mock()
        module_description.expect_get_collectors(toReturn=[collector])

        collector2 = Mock(key='testing')
        collector2.expect_get_values(toReturn={'value1': 23})

        module_description2 = Mock()
        module_description2.expect_get_collectors(toReturn=[collector2])
        module_description2.expect_measured_modules(toReturn=[module])
        module_description2.expect_files(module, toReturn=[])

        module_descriptions = [module_description, module_description2]

        args = Mock(matching='test')

        result = DataBean()

        processor.process_metrics(args, module_descriptions, result)(logger)

        self.assertFalse(module_description.verify_prepare_environment(logger))
        self.assertTrue(module_description2.verify_prepare_environment(logger))


    def test_generate_report(self):
        git = Mock().expect_generate_annotated_commits_with_details("jira-stats", toReturn=[
            {
                'hash': '12121212',
                'shorthash': '1212',
                'commiter': 'Mark <unit@tester>',
                'date': 1371114910,
                'note': '{"metrics": {"test.metric": 1}, "build_number": "BN-2"}'
            },
            {
                'hash': '58585858',
                'shorthash': '5858',
                'commiter': 'Frank <frank@localhost>',
                'date': 1371111910,
                'note': '{"metrics": {"test.metric": 3}, "build_number": "BN-1"}'
            },
        ]).ordered_get_commit_details("jira-stats", toReturn={
            'hash': '34343434',
            'shorthash': '3434',
            'commiter': 'Unit Tester <unit@tester>',
            'date': 1371114916,
            'note': ''
        })

        json_writter = Mock().ordered_as_str({'points': [
            {
                'metrics': {
                    'test.metric': 3
                },
                'commits': [
                    {
                        'hash': '58585858',
                        'shorthash': '5858',
                        'commiter': 'Frank <frank@localhost>',
                        'date': 1371111910
                    }
                ],
                'date': 1371111910,
                'build_number': 'BN-1'
            },
            {
                'metrics': {
                    'test.metric': 1
                },
                'commits': [
                    {
                        'hash': '12121212',
                        'shorthash': '1212',
                        'commiter': 'Mark <unit@tester>',
                        'date': 1371114910
                    }
                ],
                'date': 1371114910,
                'build_number': 'BN-2'
            },
            {
                'metrics': {
                    'test.metric': 2
                },
                'commits': [
                    {
                        'date': 1371114916,
                        'commiter': 'Unit Tester <unit@tester>',
                        'shorthash': '3434',
                        'hash': '34343434'
                    }
                ],
                'date': 1371114916,
                'build_number': 'current'
            }
        ]}, toReturn='__JSON__')

        file_utils = MockFileUtils()
        log = Mock()

        current_metrics = DataBean(metrics={
            "test.metric": 2
        })

        processor = MetricsProcessor()
        generate_report_closure = processor.generate_report(current_metrics, file_utils, git, json_writter)
        generate_report_closure(log)
        self.assertTrue(file_utils.verify_write_lines(os.sep.join(['target','eh-metrics-report','js','data.js']),
            ['(function() { var data = __JSON__; executeReport(data); })();']))

    def test_check_values_ignores_additional_and_missing_keys(self):
        processor = MetricsProcessor()

        args = Mock(non_interactive=True)
        git = Mock()


        git.expect_latest_annotated_commit_with_details(STATS_REF_NAME, toReturn={'note': '{"metrics":{'
                                                                        '"key1": {"value": 20, "description": ""}, '
                                                                        '"key2": {"value": 10, "description": ""}   }}',
                                                                                  'hash': '123efef',
                                                                                  'commiter': 'me'})
        git.expect_generate_annotated_commits_with_details(STATS_EXCLUSION_REF_NAME, commit_range='123efef..HEAD', toReturn=[])

        metrics = DataBean()
        metrics.metrics = {'key2': DataBean(value=10, description='', checked=True, direction=FALLING),
                           'key3': DataBean(value=10, description='', checked=True, direction=FALLING)}

        self.assertEqual(Callable.success, processor.check_values(args, metrics, git, MockFileUtils())(Mock()))

    def test_check_values_falling(self):
        processor = MetricsProcessor()

        args = Mock(non_interactive=True)
        git = Mock()

        git.expect_latest_annotated_commit_with_details(STATS_REF_NAME, toReturn={'note': '{"metrics":{"key1": {"value": 10, "description": ""}}}',
                                                                                  'hash': 'abcdef',
                                                                                  'commiter': 'me'})
        git.expect_generate_annotated_commits_with_details(STATS_EXCLUSION_REF_NAME, commit_range='abcdef..HEAD', toReturn=[])

        metrics = DataBean()
        metrics.metrics = {'key1': DataBean(value=20, description='', checked=True, direction=FALLING)}

        self.assertEqual(Callable.do_not_proceed, processor.check_values(args, metrics, git, MockFileUtils())(Mock()))

    def test_check_values_rising(self):
        processor = MetricsProcessor()

        args = Mock(non_interactive=True)
        git = Mock()

        git.expect_latest_annotated_commit_with_details(STATS_REF_NAME, toReturn={'note': '{"metrics":{"key1": {"value": 20, "description": ""}}}',
                                                                                  'hash': 'abcdef',
                                                                                  'commiter': 'me'})
        git.expect_generate_annotated_commits_with_details(STATS_EXCLUSION_REF_NAME, commit_range='abcdef..HEAD', toReturn=[])

        metrics = DataBean()
        metrics.metrics = {'key1': DataBean(value=10, description='', checked=True, direction=RISING)}

        self.assertEqual(Callable.do_not_proceed, processor.check_values(args, metrics, git, MockFileUtils())(Mock()))

    def test_check_values_with_exclusions(self):
        processor = MetricsProcessor()

        args = Mock(non_interactive=True)
        git = Mock()

        git.expect_latest_annotated_commit_with_details(STATS_REF_NAME, toReturn={'note': '{"metrics":{"key1": {"value": 10, "description": ""}}}',
                                                                                  'hash': '123456',
                                                                                  'commiter': 'me'})
        exclusion = {'note': '{"committer": "testbot", "reason": "none", "exclusion": {"key1": 15}}'}
        git.expect_generate_annotated_commits_with_details(STATS_EXCLUSION_REF_NAME, commit_range='123456..HEAD', toReturn=[exclusion])

        metrics = DataBean()
        metrics.metrics = {'key1': DataBean(value=12, description='', checked=True, direction=FALLING)}

        self.assertEqual(Callable.success, processor.check_values(args, metrics, git, MockFileUtils())(Mock()))

    def test_check_values_with_unchecked(self):
        processor = MetricsProcessor()

        args = Mock(non_interactive=True)
        git = Mock()

        git.expect_latest_annotated_commit_with_details(STATS_REF_NAME, toReturn={'note': '{"metrics":{"key1": {"value": 10, "description": ""}, '
                                                                                                      '"key2": {"value": 10, "description": ""}}}',
                                                                                  'hash': '123456',
                                                                                  'commiter': 'me'})
        git.expect_generate_annotated_commits_with_details(STATS_EXCLUSION_REF_NAME, commit_range='123456..HEAD', toReturn=[])

        metrics = DataBean()
        metrics.metrics = {'key1': DataBean(value=12, description='', checked=False, direction=FALLING)}
        metrics.metrics = {'key1': DataBean(value=8, description='', checked=False, direction=RISING)}

        self.assertEqual(Callable.success, processor.check_values(args, metrics, git, MockFileUtils())(Mock()))

    def test_check_values_respects_other_violations(self):
        processor = MetricsProcessor()

        args = Mock(non_interactive=True)
        git = Mock()

        git.expect_latest_annotated_commit_with_details(STATS_REF_NAME, toReturn={'note': '{"metrics":{"key1": {"value": 10, "description": ""},'
                                                                                                      '"key2": {"value": 10, "description": ""}}}',
                                                                                  'hash': '123456',
                                                                                  'commiter': 'me'})
        exclusion = {'note': '{"committer": "testbot", "reason": "none", "exclusion": {"key1": 15}}'}
        git.expect_generate_annotated_commits_with_details(STATS_EXCLUSION_REF_NAME, commit_range='123456..HEAD', toReturn=[exclusion])

        metrics = DataBean()
        metrics.metrics = {'key1': DataBean(value=8, description='', checked=True, direction=FALLING),
                           'key2': DataBean(value=12, description='', checked=True, direction=FALLING)}

        self.assertEqual(Callable.do_not_proceed, processor.check_values(args, metrics, git, MockFileUtils())(Mock()))

    def test_check_values_generates_tests_report_when_no_previous_metrics(self):
        test_suite_logger = Mock()
        test_suite_logger_factory_mock = Mock().ordered_new_logger("ehMetrics", toReturn = test_suite_logger)

        test_suite_logger.ordered_success('key2', 0, 'EHMetrics', toReturn = None)
        test_suite_logger.ordered_success('key3', 0, 'EHMetrics', toReturn = None)
        test_suite_logger.ordered_save(toReturn = None)

        processor = MetricsProcessor(test_suite_logger_factory_mock)

        metrics = DataBean(metrics = {
            'key2': DataBean(value=10, description='', checked=True, direction=FALLING),
            'key3': DataBean(value=10, description='', checked=True, direction=FALLING)
        })

        args = Mock(non_interactive=True)
        self.assertEqual(Callable.success, processor.check_values(args, metrics, Mock(), MockFileUtils())(Mock()))

        test_suite_logger.verify_all_ordered()

    def test_check_values_generates_tests_report_with_previous_metrics(self):
        test_suite_logger = Mock()
        test_suite_logger_factory_mock = Mock().ordered_new_logger("ehMetrics", toReturn = test_suite_logger)

        test_suite_logger.ordered_failed('key2', 0, 'EHMetrics', 'metric key2 () increased from 9 to 10.', toReturn = None)
        test_suite_logger.ordered_success('key3', 0, 'EHMetrics', toReturn = None)
        test_suite_logger.ordered_save(toReturn = None)

        processor = MetricsProcessor(test_suite_logger_factory_mock)

        metrics = DataBean(metrics = {
            'key2': DataBean(value=10, description='', checked=True, direction=FALLING),
            'key3': DataBean(value=10, description='', checked=True, direction=FALLING)
        })

        git = Mock()
        git.ordered_latest_annotated_commit_with_details(STATS_REF_NAME, toReturn={
            'note': '{"metrics":{"key1": {"value": 10, "description": ""}, "key2": {"value": 9, "description": ""}}}',
            'hash': '123456',
            'commiter': 'me'
        })
        git.ordered_generate_annotated_commits_with_details(STATS_EXCLUSION_REF_NAME, commit_range='123456..HEAD',
            toReturn=[])

        args = Mock(non_interactive=True)
        self.assertEqual(Callable.do_not_proceed, processor.check_values(args, metrics, git, MockFileUtils())(Mock()))

        git.verify_all_ordered()
        test_suite_logger.verify_all_ordered()

if __name__ == '__main__':
    unittest.main(verbosity=2)
