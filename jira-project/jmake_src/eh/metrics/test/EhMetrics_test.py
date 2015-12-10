import os
import unittest
from unittest import TestCase
from CommandExecutor import Callable
from eh.metrics.EhMetrics import EhMetrics
from eh.metrics.MetricsCollector import MetricsCollector
from utils.FileUtils import MockFileUtils
from utils.Mocks import Mock

SERIALIZED_METRICS = '{"test": "ok"}'

#noinspection PyTypeChecker
class TestEhMetrics(TestCase):
    def setUp(self):
        super().setUp()
        self.git = Mock()
        self.git.expect_get_remotes(toReturn=['stash!'])

        self.json = Mock()
        self.json.default_as_str(SERIALIZED_METRICS)

        self.fs = MockFileUtils()
        self.executor = []
        self.metrics_processor = Mock()

        self.called_process_metrics = False

        def call_process_metrics(_):
            self.called_process_metrics = True
            return Callable.success

        self.metrics_processor.default_process_metrics(call_process_metrics)

        self.called_generate_report = False

        def call_generate_report(_):
            self.called_generate_report = True
            return Callable.success

        self.metrics_processor.default_generate_report(call_generate_report)

        self.called_check_values = False

        def call_check_values(_):
            self.called_check_values = True
            return Callable.success

        self.metrics_processor.default_check_values(call_check_values)

        self.ehmetrics = EhMetrics(git=self.git, fs=self.fs,
                                   metrics_processor=self.metrics_processor, json_writer=self.json)

        self.executor_cursor = 0

    def args(self, verbose=False, log=Mock(), branch=None, buildno=None, fast=False, note=False,
                    matching=None, non_interactive=False):
        return Mock(verbose=verbose, log=log, branch=branch, buildno=buildno, fast=fast, note=note,
                    matching=matching, non_interactive=non_interactive)

    def test_basic_report_generation(self):
        self.ehmetrics(self.args(), self.executor)

        self.__next_remotes()
        self.__next_fetch_notes()
        self.__next_is_clean_workspace()
        self.__next_clean_logs()
        self.__next_record_commit()
        self.__next_process_metrics()
        self.__next_generate_report()
        self.__next_check_values()
        self.__finished()

    def test_remote_set_when_no_remote_present(self):
        self.git.expect_get_remotes(toReturn=[])

        self.ehmetrics(self.args(), self.executor)

        self.__next_remotes(withSet=True)
        self.__next_fetch_notes()
        self.__next_is_clean_workspace()
        self.__next_record_commit()
        self.__next_clean_logs()
        self.__next_process_metrics()
        self.__next_generate_report()
        self.__next_check_values()
        self.__finished()

    def test_failed_branch_check_stops_executor(self):
        self.git.expect_current_branch(toReturn='master')
        self.ehmetrics(self.args(branch='snowflake'), self.executor)

        self.__next_remotes()
        self.__next_fetch_notes()
        self.assertNotEqual(Callable.success, self.__next_check_branch())

    def test_branch_check(self):
        self.git.expect_current_branch(toReturn='snowflake')
        self.ehmetrics(self.args(branch='snowflake'), self.executor)

        self.__next_remotes()
        self.__next_fetch_notes()
        self.assertEqual(Callable.success, self.__next_check_branch())
        self.__next_is_clean_workspace()
        self.__next_record_commit()
        self.__next_clean_logs()
        self.__next_process_metrics()
        self.__next_generate_report()
        self.__next_check_values()
        self.__finished()

    def test_produce_and_push_notes(self):
        self.git.expect_is_clean_workspace(toReturn=True)
        self.ehmetrics(self.args(note=True), self.executor)

        self.__next_remotes()
        self.__next_fetch_notes()
        self.assertEqual(Callable.success, self.__next_is_clean_workspace())
        self.__next_record_commit()
        self.__next_clean_logs()
        self.__next_process_metrics()
        self.__next_generate_report()
        self.__next_check_values()
        self.__next_set_user()
        self.__next_put_notes()
        self.__next_push_notes('jira-stats')
        self.__finished()

    def test_will_not_push_on_workspace_with_changes(self):
        self.git.expect_is_clean_workspace(toReturn=False)
        self.ehmetrics(self.args(note=True), self.executor)

        self.__next_remotes()
        self.__next_fetch_notes()
        self.assertNotEqual(Callable.success, self.__next_is_clean_workspace())

    def test_using_developer_connection(self):
        scm_header = 'scm:git:'
        myserver = 'bitbucket.wonderland.universe.com'

        pom_parser = Mock()
        pom_parser.expect_get_developer_connection(toReturn=(scm_header + myserver))

        self.ehmetrics.set_remote(Mock(), pom_parser=pom_parser)
        self.assertTrue(self.git.verify_set_remote('origin', myserver))

    def __next(self):
        self.assertLess(self.executor_cursor, len(self.executor),
                        'No more executor jobs! Only %s jobs produced.' % str(len(self.executor)))
        rc = self.executor[self.executor_cursor](Mock())
        self.executor_cursor += 1
        return rc

    def __finished(self):
        self.assertEqual(self.executor_cursor, len(self.executor),
                         'there are more executor jobs than %s!' % str(self.executor_cursor))

    def __next_remotes(self, withSet=False):
        self.assertFalse(self.git.verify_get_remotes())
        self.assertEqual(0, self.git.callcount_set_remote())
        self.__next()
        self.assertTrue(self.git.verify_get_remotes())
        self.assertEqual(1 if withSet else 0, self.git.callcount_set_remote())

    def __next_fetch_notes(self, ref: str='*'):
        self.assertEqual(0, self.git.callcount_fetch_notes())
        self.__next()
        self.assertTrue(self.git.verify_fetch_notes(ref))

    def __next_is_clean_workspace(self):
        self.assertFalse(self.git.verify_is_clean_workspace())
        rc = self.__next()
        self.assertTrue(self.git.verify_is_clean_workspace())
        return rc

    def __next_record_commit(self):
        self.git.expect_current_commit(toReturn='0011')
        self.fs.expect_write_lines(os.sep.join([self.fs.existing_dir(MetricsCollector.log_directory),'.commit']), '0011', toReturn=None)
        self.__next()
        self.git.verify_write_lines(os.sep.join([self.fs.existing_dir(MetricsCollector.log_directory),'.commit']), '0011')

    def __next_clean_logs(self):
        self.fs.expect_dir_exists(MetricsCollector.log_directory, toReturn=True)
        self.fs.expect_remove_dir(MetricsCollector.log_directory, toReturn=None)
        self.__next()
        self.fs.verify_remove_dir(MetricsCollector.log_directory)

    def __next_process_metrics(self):
        self.assertFalse(self.called_process_metrics)
        self.__next()
        self.assertTrue(self.called_process_metrics)

    def __next_generate_report(self):
        self.assertFalse(self.called_generate_report)
        self.__next()
        self.assertTrue(self.called_generate_report)

    def __next_check_values(self):
        self.assertFalse(self.called_check_values)
        self.__next()
        self.assertTrue(self.called_check_values)

    def __next_check_branch(self):
        self.assertFalse(self.git.verify_current_branch())
        rc = self.__next()
        self.assertTrue(self.git.verify_current_branch())
        return rc

    def __next_set_user(self):
        self.assertEqual(0, self.git.callcount_set_user())
        self.__next()
        self.assertEqual(1, self.git.callcount_set_user())

    def __next_put_notes(self):
        self.assertEqual(0, self.git.callcount_put_notes())
        self.__next()
        self.assertTrue(self.git.verify_put_notes(SERIALIZED_METRICS, 'jira-stats', 'HEAD', True))

    def __next_push_notes(self, ref:str):
        self.assertFalse(self.git.verify_push_notes(ref))
        self.__next()
        self.assertTrue(self.git.verify_push_notes(ref))


if __name__ == '__main__':
    unittest.main(verbosity=2)


