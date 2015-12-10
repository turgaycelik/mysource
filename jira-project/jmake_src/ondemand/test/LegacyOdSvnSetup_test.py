from unittest import TestCase
import unittest

from ondemand.LegacyOdSvnSetup import LegacyOdSvnSetup
from utils.FileUtils import MockFileUtils
from utils.Mocks import Mock


class TestLegacyOdSetup(TestCase):
    def setUp(self):
        self.legacyOdSetup = LegacyOdSvnSetup(Mock(layout=Mock().expect_studio_svn_link(toReturn='/tmp/link')
                                                   .expect_studio_svn_dir(toReturn='/tmp/svn')))
        self.fs = MockFileUtils()

    def testLinkIsNotCreatedWhenExists(self):
        self.fs.expect_file_exists('/tmp/link', toReturn=True)
        self.legacyOdSetup(Mock(), file_utils=self.fs)
        self.assertEqual(self.fs.callcount_symlink(), 0)

    def testLinkIsCreatedWhenNotExists(self):
        self.fs.expect_file_exists('/tmp/link', toReturn=False)
        self.legacyOdSetup(Mock(), file_utils=self.fs)
        self.assertTrue(self.fs.verify_symlink('/tmp/svn', '/tmp/link'))


if __name__ == '__main__':
    unittest.main(verbosity=2)
