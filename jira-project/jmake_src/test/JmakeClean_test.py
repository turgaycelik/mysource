from unittest import TestCase
import unittest
from CommandExecutor import SystemCallable
from JmakeClean import Clean
from utils.Mocks import Mock
from catalina.WorkspaceLayout import WorkspaceLayout
from maven.Maven import MavenCallable

class JmakeCleanTest(TestCase):
    def setUp(self):
        self.file_utils = Mock()
        self.file_utils.default_file_exists(False)
        self.jmake_clean = Clean(self.file_utils)
        self.executor = []

    def test_clean_basic(self):
        self.jmake_clean(Mock(deep=False, tomcat=False, jirahome=False, with_workspace=False), self.executor)
        self.assertEqual(len(self.executor), 2)
        self.assertIsInstance(self.executor[0], MavenCallable)
        self.assertIn('clean', self.executor[0].phases)
        self.assertIsNotNone(self.executor[1])

    def test_clean_deep(self):
        self.jmake_clean(Mock(deep=True, tomcat=False, jirahome=False, with_workspace=False), self.executor)
        self.assertEqual(len(self.executor), 2)
        self.assertIsInstance(self.executor[1], SystemCallable)
        self.assertIn('rm -rf', self.executor[1]._SystemCallable__command)
        self.assertIn('target', self.executor[1]._SystemCallable__command)
        self.assertIn(' . ', self.executor[1]._SystemCallable__command)

    def test_clean_workspace(self):
        workspace_dir = '/home/abracadabra/IdeaProjects/rest-workspace'
        self.file_utils.expect_get_parent_dir_path(toReturn=workspace_dir)
        self.jmake_clean(Mock(deep=True, tomcat=False, jirahome=False, with_workspace=True), self.executor)
        self.assertEqual(len(self.executor), 3)
        self.assertIsInstance(self.executor[2], SystemCallable)
        self.assertIn('rm -rf', self.executor[2]._SystemCallable__command)
        self.assertIn('target', self.executor[2]._SystemCallable__command)
        self.assertIn(workspace_dir, self.executor[2]._SystemCallable__command)

    def test_clean_tomcat(self):
        self.file_utils.expect_file_exists(WorkspaceLayout.TOMCAT_DOWNLOAD, toReturn=True)
        self.jmake_clean(Mock(deep=False, tomcat=True, jirahome=True, with_workspace=False), self.executor)
        self.assertEqual(len(self.executor), 3)
        self.assertIsInstance(self.executor[1], SystemCallable)
        self.assertIn('rm -rf', self.executor[1]._SystemCallable__command)
        self.assertIn(WorkspaceLayout.TOMCAT_DOWNLOAD, self.executor[1]._SystemCallable__command)

    def test_clean_jirahome(self):
        self.file_utils.expect_file_exists(WorkspaceLayout.JIRA_HOME, toReturn=True)
        self.jmake_clean(Mock(deep=False, tomcat=True, jirahome=True, with_workspace=False), self.executor)
        self.assertEqual(len(self.executor), 3)
        self.assertIsInstance(self.executor[1], SystemCallable)
        self.assertIn('rm -rf', self.executor[1]._SystemCallable__command)
        self.assertIn(WorkspaceLayout.JIRA_HOME, self.executor[1]._SystemCallable__command)

    def test_clean_jira_od_home(self):
        self.file_utils.expect_file_exists(WorkspaceLayout.JIRA_OD_HOME, toReturn=True)
        self.jmake_clean(Mock(deep=False, tomcat=True, jirahome=True, with_workspace=False), self.executor)
        self.assertEqual(len(self.executor), 3)
        self.assertIsInstance(self.executor[1], SystemCallable)
        self.assertIn('rm -rf', self.executor[1]._SystemCallable__command)
        self.assertIn(WorkspaceLayout.JIRA_OD_HOME, self.executor[1]._SystemCallable__command)

    def test_clean_all_jira_homes(self):
        all_jirahomes = [WorkspaceLayout.JIRA_HOME, WorkspaceLayout.JIRA_OD_HOME,
                         WorkspaceLayout.JIRA_CLUSTERED_HOME_ROOT, WorkspaceLayout.JIRA_SHARED_HOME_ROOT]
        for home in all_jirahomes:
            self.file_utils.expect_file_exists(home, toReturn=True)
        self.jmake_clean(Mock(deep=False, tomcat=True, jirahome=True, with_workspace=False), self.executor)
        self.assertEqual(len(self.executor), 6)
        for executable in self.executor[1:4]:
            self.assertIsInstance(executable, SystemCallable)
            self.assertIn('rm -rf', executable._SystemCallable__command)
            self.assertTrue(any(home in executable._SystemCallable__command for home in all_jirahomes))

    def test_clean_full(self):
        self.file_utils.expect_file_exists(WorkspaceLayout.TOMCAT_DOWNLOAD, toReturn=True)
        self.file_utils.expect_file_exists(WorkspaceLayout.JIRA_HOME, toReturn=True)
        self.jmake_clean(Mock(deep=True, tomcat=True, jirahome=True, with_workspace=False), self.executor)
        self.assertEqual(len(self.executor), 4)


if __name__ == '__main__':
    unittest.main()