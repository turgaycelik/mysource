from unittest import TestCase

from Logger import LOG
from catalina.WorkspaceLayout import MockLayout
from ondemand.HordeLayout import HordeLayout
from ondemand.HordeRunner import HordeRunner
from utils.FileUtils import MockFileUtils
from utils.Mocks import Mock


class MockHordeRunner(HordeRunner):
    executed = False

    # noinspection PyUnusedLocal
    def _HordeRunner__call_super(self, logger):
        self.executed = True
        return 0


class TestHordeRunner(TestCase):
    def setUp(self):
        LOG.set_none()
        self.layout = MockLayout(True)
        self.horde_layout = HordeLayout(self.layout.jira_home())
        self.args = Mock()
        self.args.layout = self.layout
        self.args.horde_layout = self.horde_layout
        self.args.with_workspace = False
        self.args.mvn_clean = False
        self.horde_status_checker = MockStatusChecker()

    def test_horde_is_not_run_when_one_is_already_running(self):
        horde_runner = MockHordeRunner(self.args, MockFileUtils(), Mock(), self.horde_status_checker.running())
        horde_runner(Mock())
        self.assertFalse(horde_runner.executed,
                         "Parent class should not be executed when another instance is already running")

    def test_horde_skeleton_is_not_copied_when_directory_exists(self):
        file_utils = MockFileUtils()
        process_utils = Mock().default_check_output('Maven:3.3.2\nJava home:jdk_home\n:Java version 1.2.3_123'.encode())
        horde_runner = MockHordeRunner(self.args, file_utils, process_utils, self.horde_status_checker.not_running())
        horde_runner(Mock())
        self.assertTrue(file_utils.verify_copy_tree(self.horde_layout.horde_skeleton_dir(),
                                                    self.horde_layout.horde_home_dir(False)))
        self.assertFalse(file_utils.verify_remove_dir(self.horde_layout.horde_home_dir(False)))

    def test_clean_removes_old_home(self):
        self.args.mvn_clean = True
        file_utils = MockFileUtils()
        process_utils = Mock().default_check_output('Maven:3.3.2\nJava home:jdk_home\n:Java version 1.2.3_123'.encode())
        horde_runner = MockHordeRunner(self.args, file_utils, process_utils, self.horde_status_checker.not_running())
        horde_runner(Mock())
        self.assertTrue(file_utils.verify_remove_dir(self.horde_layout.horde_home_dir(False)))


class MockStatusChecker:
    def __init__(self):
        self.running_flag = True

    def running(self):
        self.running_flag = True
        return self

    def not_running(self):
        self.running_flag = False
        return self

    def is_running(self):
        return self.running_flag