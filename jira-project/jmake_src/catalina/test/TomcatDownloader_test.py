from unittest import TestCase
import unittest
from utils.Mocks import Mock
from catalina.TomcatDownloader import TomcatDownloader
from catalina.WorkspaceLayout import MockLayout
from utils.FileUtils import MockFileUtils


class TestTomcatDownloader(TestCase):
    def setUp(self):
        self.layout = MockLayout(False)
        self.args = Mock(layout=self.layout, mvn_clean=False, with_workspace=False)

    def tearDown(self):
        self.layout.remove()

    def test_downloading_skipped_when_tomcat_in_place(self):
        #having
        mock_file_utils = MockFileUtils()
        tomcat_downloader = MockTomcatDownloader(self.args, file_utils=mock_file_utils)
        mock_file_utils.expect_file_exists(self.layout.tomcat_dir(False), toReturn=True)
        #when
        tomcat_downloader(Mock())
        #then
        self.assertFalse(tomcat_downloader.executed, 'Downloading task was run')

    def test_maven_download_executed_when_tomcat_does_not_exist(self):
        #having
        mock_file_utils = MockFileUtils()
        tomcat_downloader = MockTomcatDownloader(self.args, file_utils=mock_file_utils)

        #when
        tomcat_downloader(Mock())

        #then
        self.assertTrue(tomcat_downloader.executed, 'Downloading task was expected to execute')
        self.assertListEqual(tomcat_downloader.projects, ['jira-ide-support'])
        self.assertListEqual(tomcat_downloader.profiles, ['download-tomcat', 'ide-setup'])
        self.assertListEqual(tomcat_downloader.phases, ['initialize'])
        self.assertDictEqual(tomcat_downloader.properties, {'tomcat.dir': self.layout.tomcat_download_dir()})


class MockTomcatDownloader(TomcatDownloader):
    executed = False

    #noinspection PyUnusedLocal
    def _TomcatDownloader__call_supper(self, logger):
        self.executed = True

if __name__ == '__main__':
    unittest.main(verbosity=2)
