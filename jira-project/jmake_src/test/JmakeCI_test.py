from unittest import TestCase
import unittest
from JmakeCI import CIFuncTest
from JmakeCI import CIJobs
from JmakeCI import CISingleTest
from JmakeCI import CIWebdriverTest
from utils.Mocks import Mock

class FuncArgs:
    def __init__(self):
        self.func_batches = 20

    def __iter__(self):
        return iter(['func_batches'])

    def _func_batches(self, batches):
        self.func_batches = batches
        return self


class JmakeCIFuncTest(TestCase):
    def setUp(self):
        self.jmake_ci_func = CIFuncTest()
        self.executor = []

    def test_func_default(self):
        self.jmake_ci_func(FuncArgs(), self.executor)
        self.assertEqual(len(self.executor), 20)
        maven = self.executor[0]
        self.assertEqual(20, maven.properties['atlassian.test.suite.numbatches'])
        self.assertEqual(1, maven.properties['atlassian.test.suite.batch'])
        self.assertListEqual(maven.phases, ['verify'])

    def test_func_different_number_of_batches(self):
        self.jmake_ci_func(FuncArgs()._func_batches(100), self.executor)
        self.assertEqual(len(self.executor), 100)
        maven = self.executor[50]
        self.assertEqual(100, maven.properties['atlassian.test.suite.numbatches'])
        self.assertEqual(51, maven.properties['atlassian.test.suite.batch'])

class JobsArgs(Mock):

    def __iter__(self):
        return iter(self.job_name)

class JmakeCIJobs(TestCase):
    def setUp(self):
        self.jmake_ci_jobs = CIJobs()
        self.executor = []

    def test_jobs_func(self):
        self.jmake_ci_jobs(JobsArgs(job_name = ['FUNC04']), self.executor)
        self.assertEqual(len(self.executor), 1)
        maven = self.executor[0]
        self.assertEqual(4, maven.properties['atlassian.test.suite.batch'])
        self.assertListEqual(maven.phases, ['verify'])

    def test_jobs_unit(self):
        self.jmake_ci_jobs(Mock(job_name = ['FUNCUNIT']), self.executor)
        self.assertEqual(len(self.executor), 1)
        maven = self.executor[0]
        def contains_with_true(param):
            self.assertIn(param, maven.properties)
            self.assertEqual('true', maven.properties[param])

        contains_with_true('maven.test.func.skip')
        contains_with_true('jira.security.disabled')
        contains_with_true('jira.minify.skip')

        self.assertListEqual(maven.phases, ['verify'])

    def test_jobs_ualint(self):
        self.jmake_ci_jobs(Mock(job_name = ['UALINT']), self.executor)
        self.assertEqual(len(self.executor), 1)
        maven = self.executor[0]
        self.assertListEqual(maven.phases, ['verify'])
        self.assertListEqual(maven.projects, ['jira-distribution/jira-webapp-dist', 'jira-distribution/jira-integration-tests'])

    def test_jobs_compile(self):
        self.jmake_ci_jobs(Mock(job_name = ['COMPILE'], with_workspace=False), self.executor)
        self.assertEqual(len(self.executor), 1)
        maven = self.executor[0]
        self.assertDictEqual(maven.properties, {'maven.test.unit.skip': 'true',
                                                'maven.test.func.skip': 'true',
                                                'maven.test.selenium.skip': 'true'})
        self.assertListEqual(maven.phases, ['test'])

    def test_jobs_incorrect(self):
        self.jmake_ci_jobs(Mock(job_name = ['NONEXISTINGJOB']), self.executor)
        self.assertEqual(len(self.executor), 1)
        allowed_jobs_info_closure = self.executor[0]
        logger = Mock()
        allowed_jobs_info_closure(logger)
        self.assertTrue(logger.verify_error('The only allowed jobs are COMPILE FUNCxx FUNCUNIT UALINT'))


class SingleArgs (Mock):

    def __iter__(self):
        return iter(self.test_class)

class JmakeCISingleTest(TestCase):
    def setUp(self):
        self.jmake_ci_single = CISingleTest()
        self.executor = []

    def test_single_default(self):
        self.jmake_ci_single(SingleArgs(test_class=['just.mocked.test'], mvn_clean=False, with_workspace=False), self.executor)
        self.assertEqual(len(self.executor), 1, 'Expected only one callable to be registered')
        maven = self.executor[0]
        self.assertListEqual(maven.projects, ['jira-func-tests'])
        self.assertDictEqual(maven.properties, {'failIfNoTests': 'false',
                                                'maven.test.func.skip': 'true',
                                                'maven.test.selenium.skip': 'true',
                                                'maven.test.unit.skip': 'true',
                                                'test': 'just.mocked.test'},
            'Invalid properties for meven execution')

        self.assertListEqual(maven.phases, ['test'])

class JmakeCIWebdriverTest(TestCase):
    def setUp(self):
        self.jmake_ci_webdriver = CIWebdriverTest()
        self.executor = []

    def test_webdriver_default(self):
        self.jmake_ci_webdriver(Mock(mvn_clean=False), self.executor)
        maven = self.executor[0]
        self.assertListEqual(maven.phases, ['test'])
        self.assertListEqual(maven.profiles, ['jmake-webdriver-tests'])

if __name__ == '__main__':
    unittest.main(verbosity=2)