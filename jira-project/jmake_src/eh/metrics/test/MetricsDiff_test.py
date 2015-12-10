import os
from unittest import TestCase
from eh.metrics.EhMetricsInvestigate import MetricsDiff, MetricsDiffResult
from utils.FileUtils import MockFileUtils
from utils.Mocks import Mock

class TestMetricsDiff(TestCase):

    def test_diff_lines_with_deleted_line(self):
        self._test_diff_lines_impl(['a'], [], ['- a'])

    def test_diff_lines_with_added_line(self):
        self._test_diff_lines_impl([], ['a'], ['+ a'])

    def test_diff_lines_with_added_and_deleted_line(self):
        self._test_diff_lines_impl(['b'], ['a'], ['- b', '+ a'])

    def test_diff_lines_with_changed_line(self):
        self._test_diff_lines_impl(
            ['a', 'some long line', 'c', 'z'],
            ['a', 'some very long line', 'c', 'z'],
            [
                '- some long line',
                '+ some very long line',
                '?      +++++'
            ]
        )

    def test_diff_lines_without_changes(self):
        self._test_diff_lines_impl([], [], [])

    def _test_diff_lines_impl(self, lines_before, lines_after, expected_diff):
        #noinspection PyTypeChecker
        md = MetricsDiff(None)
        diff = list(md.diff_lines(lines_before, lines_after))
        self.assertEqual(expected_diff, diff)

    def test_diff_metrics_with_new_metrics(self):
        self._test_diff_metrics_impl(
            ['x'],
            [MetricsDiff.HIT_LOG_FILE_NAME_PATTERN % 'x'], [''],
            [MetricsDiffResult('x')],
            file_contents={
                'x': {'before':[], 'now': []}
            }
        )

    def test_diff_metrics_with_deleted_metrics(self):
        self._test_diff_metrics_impl(
            ['x'],
            [''], ['x'],
            [MetricsDiffResult('x')],
            file_contents={
                'x': {'before':[], 'now': []}
            }
        )

    def test_diff_metrics_with_metrics_change(self):
        self._test_diff_metrics_impl(
            ['x'],
            ['x'], ['x'],
            [MetricsDiffResult('x', ['- A', '+ B'])],
            file_contents={
                'x': {
                    'before': ['A'],
                    'now': ['B']
                }
            }
        )

    def test_diff_metrics_with_metrics_change_in_multiple_files(self):
        self._test_diff_metrics_impl(
            ['x', 'y'],
            ['x', 'y'], ['x'],
            [MetricsDiffResult('x', ['- A', '+ B']), MetricsDiffResult('y', ['+ Metrics', '+ New'])],
            file_contents={
                'x': {
                    'before': ['A'],
                    'now': ['B']
                },
                'y': {
                    'before': None,
                    'now': ['New', 'Metrics']
                }
            }
        )

    def _test_diff_metrics_impl(self, metrics_to_compare, files_now, files_before, expected_result, file_contents):
        file_utils = MockFileUtils()
        files_now = map(lambda f : MetricsDiff.HIT_LOG_FILE_NAME_PATTERN % f, files_now)
        files_before = map(lambda f : MetricsDiff.HIT_LOG_FILE_NAME_PATTERN % f, files_before)
        file_utils.expect_listdir('now', toReturn=files_now)
        file_utils.expect_listdir('before', toReturn=files_before)

        for file_name,file_contents in file_contents.items():
            for when in ['before', 'now']:
                if file_contents[when] is not None:
                    file_utils.expect_read_lines(os.sep.join([when, MetricsDiff.HIT_LOG_FILE_NAME_PATTERN % file_name]), toReturn = file_contents[when])

        log = Mock()
        #noinspection PyTypeChecker
        md = MetricsDiff(file_utils)
        diffs = list(md.diff_metrics(log, 'now', 'before', metrics_to_compare))
        self.assertEqual(expected_result, diffs)

