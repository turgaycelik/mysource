from unittest import TestCase
import unittest

from ondemand.ManifestoUtils import ManifestoUtils
from utils.FileUtils import MockFileUtils
from utils.Mocks import Mock


class TestManifestoUtils(TestCase):
    def setUp(self):
        self.fs = MockFileUtils()
        self.url = Mock()
        self.manifesto = ManifestoUtils(url_utils=self.url, fs=self.fs)

        self.url.expect_read('https://manifesto.uc-inf.net/api/env/jirastudio-dev', None, None,
                             toReturn='{ "hash": "123456" }')

    def test_resolve_aliases(self):
        h = self.manifesto.determine_hash('dev')
        self.assertEqual(h, '123456')

    def test_get_plugins(self):
        self.url.expect_read('https://manifesto.uc-inf.net/static/12345/psd', None, None,
                             toReturn='{ "plugins": { "jira" : {"myplugin" : { "groupId" : "g", "artifactId" : "a", "version": "v"}}}}')

        plugins = self.manifesto.get_plugins_maven_artifacts('12345')
        self.assertDictEqual({"myplugin": {"groupId": "g", "artifactId": "a", "version": "v"}}, plugins)

    def test_requests_are_cached(self):
        self.url.expect_read('https://manifesto.uc-inf.net/api/env/jirastudio-dev', None, None,
                             toReturn='{ "hash": "123456" }')

        h = self.manifesto.determine_hash('jirastudio-dev')
        self.assertEqual(h, '123456')

        h = self.manifesto.determine_hash('jirastudio-dev')
        self.assertEqual(h, '123456')

        self.assertEqual(1, self.url.callcount_read())


if __name__ == '__main__':
    unittest.main(verbosity=2)