from unittest import TestCase
import unittest
from eh.metrics.FileCount import FileCount, FileCountAndSize
from eh.metrics.JavaPackageImports import JavaPackageImports
from eh.metrics.MetricsCollector import MetricsCollector
from eh.metrics.tailored.DeprecatedMethodUsage import DeprecatedMethodUsage
from eh.metrics.tailored.JUnitFinder import JUnitFinder
from eh.metrics.tailored.ManagersInJiraApi import ManagersInJiraApi
from eh.metrics.test.TestResourceUtils import TestResourcesUtils
from eh.metrics.tailored.WebTestFinder import WebTestFinder
from maven.Maven import MavenCallable, MavenExecutionException
from utils.FileUtils import MockFileUtils
from utils.Mocks import Mock

class MockDeprecatedMethodUsage(DeprecatedMethodUsage):
    def __init__(self, metrics_name: str='deprecation.methods', description: str='Deprecated methods usage'):
        #noinspection PyTypeChecker
        super().__init__(metrics_name, description, MockFileUtils())
        self.maven_runs = []
        self.return_code=0

    def maven_return_code(self, code):
        self.return_code = code

    def run_maven(self, maven: MavenCallable):
        self.maven_runs.append(maven)
        maven.returncode = self.return_code
        return self.return_code

#noinspection PyTypeChecker
class TestMetrics(TestCase):

    numbers =  [ ('eleven', 11),
                 ('twelve', 12),
                 ('thirteen', 13),
                 ('fourteen', 14),
                 ('fifteen', 15),
                 ('sixteen', 16),
                 ('seventeen', 17),
                 ('eighteen', 18),
                 ('nineteen', 19),
                 ('twenty', 20)    ]

    hello_date_file = TestResourcesUtils.build_test_file_path(['testdata', 'HelloDate.java'])

    def valueof(self, obj):
        return obj.get_values()['test'].value

    def test_file_count(self):
        filter = lambda x : 'teen' in x
        obj = FileCount('test', filter, metrics_logger=Mock()).configure(Mock(), False)

        obj.pre_files_scan('test-module')
        for word, size in TestMetrics.numbers:
            self.assertFalse(obj.wants_file(word), 'File Count metrics cannot ask to open files.')
        obj.post_files_scan('test-module')

        self.assertEqual(sum (1 for word, size in TestMetrics.numbers if filter(word)), self.valueof(obj))

    def test_file_and_size(self):

        filter = lambda x : x.startswith('t')

        m = MockFileUtils()
        for word, size in TestMetrics.numbers:
            m.expect_file_size(word, toReturn = size)

        obj = FileCountAndSize('test', filter, file_utils= m, metrics_logger=Mock()).configure(Mock(), False)

        obj.pre_files_scan('test-module')
        for word, size in TestMetrics.numbers:
            self.assertFalse(obj.wants_file(word), 'File And Size Count metrics cannot ask to open files.')
        obj.post_files_scan('test-module')

        files = 0
        bytes = 0
        for word, size in TestMetrics.numbers:
            if filter(word):
                bytes += size
                files += 1

        self.assertEqual(files, obj.get_values()['test.count'].value)
        self.assertEqual(bytes, obj.get_values()['test.cumulativesize'].value)



    def read_file(self, filename, obj: MetricsCollector):
        lastline = None
        with open(filename, encoding='utf-8') as f:
            for line in f.readlines():
                lastline = line
                if not obj.on_read_line(line):
                    break
        return lastline

    def test_java_imports_gives_up_on_class_def(self):
        obj = JavaPackageImports('test', 'description', 'util', metrics_logger=Mock()).configure(Mock(), False)

        obj.pre_files_scan('test-module')
        self.assertTrue(obj.wants_file(TestMetrics.hello_date_file))
        lastline = self.read_file(TestMetrics.hello_date_file, obj)
        obj.post_files_scan('test-module')

        self.assertIn('//marker', lastline)
        self.assertEqual(1, self.valueof(obj))

    def test_java_imports_releases_file_on_wrong_package(self):
        obj = JavaPackageImports('test', 'description', 'util', 'some.other.package', metrics_logger=Mock()).configure(Mock(), False)

        self.assertTrue(obj.wants_file(TestMetrics.hello_date_file))
        lastline = self.read_file(TestMetrics.hello_date_file, obj)

        self.assertIn('package', lastline, msg='parser should release the file after mismatched package.')
        self.assertEqual(0, self.valueof(obj))

    def test_java_imports_respects_package(self):
        obj = JavaPackageImports('test', 'description', 'util', 'mypackage', metrics_logger=Mock()).configure(Mock(), False)

        obj.pre_files_scan('test-module')
        self.assertTrue(obj.wants_file(TestMetrics.hello_date_file))
        lastline = self.read_file(TestMetrics.hello_date_file, obj)
        obj.post_files_scan('test-module')

        self.assertIn('//marker', lastline)
        self.assertEqual(1, self.valueof(obj))

    def test_java_imports_respects_package_whitelist(self):
        obj = JavaPackageImports('test', 'description', 'Manager', 'mypackage', whitelist = ['WhitelistedManager'], metrics_logger=Mock()).configure(Mock(), False)

        obj.pre_files_scan('test-module')
        self.assertTrue(obj.wants_file(TestMetrics.hello_date_file))
        obj.on_read_line('package mypackage;')
        obj.on_read_line('import WhitelistedManager;')
        obj.on_read_line('import IssueManager;')
        obj.post_files_scan('test-module')

        self.assertEqual(1, self.valueof(obj))

    def test_managers_in_jira_api(self):
        obj = ManagersInJiraApi(metrics_logger=Mock()).configure(Mock(), False)

        obj.pre_files_scan('jira-core')
        self.assertFalse(obj.wants_file('Utils.java'))
        self.assertFalse(obj.wants_file('MyManager.java'))

        obj.pre_files_scan('jira-api')
        self.assertFalse(obj.wants_file('Utils.java'))
        self.assertTrue(obj.wants_file('MyManager.java'))
        self.assertTrue(obj.on_read_line('class MyManager {'))
        self.assertFalse(obj.on_read_line('public interface MyManager {'))
        self.assertEqual(0, obj.get_values()['jiraapi.managers.count'].value)

        self.assertTrue(obj.wants_file('MyManager.java'))
        self.assertFalse(obj.on_read_line('public class MyManager {'))
        self.assertEqual(1, obj.get_values()['jiraapi.managers.count'].value)
        self.assertEqual(1, obj.get_values()['jiraapi.managers.count.not_deprecated'].value)

        self.assertTrue(obj.wants_file('MyManager.java'))
        self.assertTrue(obj.on_read_line('@Deprecated'))
        self.assertFalse(obj.on_read_line('public class MyManager {'))
        self.assertEqual(2, obj.get_values()['jiraapi.managers.count'].value)
        self.assertEqual(1, obj.get_values()['jiraapi.managers.count.not_deprecated'].value)

        self.assertTrue(obj.wants_file('MyManager.java'))
        self.assertFalse(obj.on_read_line('public enum MyManager {'))
        self.assertEqual(2, obj.get_values()['jiraapi.managers.count'].value)
        self.assertEqual(1, obj.get_values()['jiraapi.managers.count.not_deprecated'].value)

    def test_deprecated_methods_usage_in_normal_run(self):

        tested_metric = MockDeprecatedMethodUsage('test').configure(Mock(), False)

        tested_metric.pre_files_scan('test-module')
        self.assertEqual(0, self.valueof(tested_metric))
        self.assertEqual(1, len(tested_metric.maven_runs))
        maven = tested_metric.maven_runs[0]
        self.assertIn('jira-metrics', maven.profiles)
        self.assertIn('ondemand', maven.profiles)
        self.assertIn('test-compile', maven.phases)
        maven.process_output(Mock(), 'lorem [deprecation] ipsum', 100)
        self.assertEqual(1, self.valueof(tested_metric))
        maven.process_output(Mock(), 'lorem ipsum', 101)
        self.assertEqual(1, self.valueof(tested_metric))
        tested_metric.post_files_scan('test-module')

    def test_exception_is_raised_when_non_zero_results_from_maven(self):

        tested_metric = MockDeprecatedMethodUsage('test').configure(Mock(), False)
        tested_metric.maven_return_code(1)
        self.assertRaises(MavenExecutionException, tested_metric.get_values)

    def test_junit4_finder(self):

        tested_metric = JUnitFinder(metrics_logger=Mock())

        self.assertTrue(tested_metric.wants_file('tested/class/Class1.java'))
        self.assertTrue(tested_metric.on_read_line('public class Class1 {'))
        self.assertTrue(tested_metric.on_read_line('public void testBehavior1() {'))
        self.assertTrue(tested_metric.on_read_line('@Test'))
        self.assertTrue(tested_metric.on_read_line('public void testSomething() {'))
        self.assertTrue(tested_metric.on_read_line('@Test'))
        self.assertTrue(tested_metric.on_read_line('public void shouldFallIfNotSupported() {'))
        self.assertEqual(2, tested_metric.get_values()['tests.junit.4.count'].value)
        self.assertEqual(0, tested_metric.get_values()['tests.junit.3.count'].value)
        self.assertEqual(0, tested_metric.get_values()['tests.junit.3.nonlegacy'].value)

        self.assertTrue(tested_metric.wants_file('tested/class/Class9.java'))
        self.assertTrue(tested_metric.on_read_line('public class Class9 extends SomeClassThatIsNotJunit3 {'))
        self.assertTrue(tested_metric.on_read_line('@Test'))
        self.assertTrue(tested_metric.on_read_line('public void testMethod123() {'))
        self.assertEqual(3, tested_metric.get_values()['tests.junit.4.count'].value)
        self.assertEqual(0, tested_metric.get_values()['tests.junit.3.count'].value)
        self.assertEqual(0, tested_metric.get_values()['tests.junit.3.nonlegacy'].value)

    def test_junit3_finder(self):

        tested_metric = JUnitFinder(metrics_logger=Mock())

        self.assertTrue(tested_metric.wants_file('tested/class/Class2.java'))
        self.assertTrue(tested_metric.on_read_line('public class Class2 extends TestCase {'))
        self.assertTrue(tested_metric.on_read_line('public void testBehavior1() {'))
        self.assertTrue(tested_metric.on_read_line('@Test'))
        self.assertTrue(tested_metric.on_read_line('public void testSomething() {'))
        self.assertTrue(tested_metric.on_read_line('@Test'))
        self.assertTrue(tested_metric.on_read_line('public void shouldFallIfNotSupported() {'))
        self.assertEqual(0, tested_metric.get_values()['tests.junit.4.count'].value)
        self.assertEqual(2, tested_metric.get_values()['tests.junit.3.count'].value)
        self.assertEqual(2, tested_metric.get_values()['tests.junit.3.nonlegacy'].value)

    def test_junit3_legacy(self):

        tested_metric = JUnitFinder(metrics_logger=Mock())

        self.assertTrue(tested_metric.wants_file('/jira-tests-legacy/tested/class/Class3.java'))
        self.assertTrue(tested_metric.on_read_line('public class Class3 extends AbstractTestViewIssueColumns {'))
        self.assertTrue(tested_metric.on_read_line('public void testBehavior1() {'))
        self.assertTrue(tested_metric.on_read_line('public void shouldDoSomething() {'))
        self.assertTrue(tested_metric.on_read_line('@Test'))
        self.assertTrue(tested_metric.on_read_line('public void shouldFallIfNotSupported() {'))
        self.assertEqual(0, tested_metric.get_values()['tests.junit.4.count'].value)
        self.assertEqual(1, tested_metric.get_values()['tests.junit.3.count'].value)
        self.assertEqual(0, tested_metric.get_values()['tests.junit.3.nonlegacy'].value)

    def test_junit_does_not_count_webtests_and_releases_file(self):

        tested_metric = JUnitFinder()

        self.assertTrue(tested_metric.wants_file('/tested/class/LooksLikeJunit4.java'))
        self.assertFalse(tested_metric.on_read_line('@WebTest ({Category.WEBDRIVER_TEST})'))

        self.assertTrue(tested_metric.wants_file('/tested/class/LooksLikeJunit3.java'))
        self.assertFalse(tested_metric.on_read_line('@WebTest ({Category.FUNC_TEST})'))

    def test_webtest_finder_does_not_count_unit_and_releases_file(self):

        tested_metric = WebTestFinder()

        self.assertTrue(tested_metric.wants_file('/jira-tests-legacy/tested/class/Class8.java'))
        self.assertFalse(tested_metric.on_read_line('public class Class8 extends AbstractTestViewIssueColumns {'))

    def test_webtest_looks_for_junit3_in_func(self):

        tested_metric = WebTestFinder(metrics_logger=Mock())

        self.assertTrue(tested_metric.wants_file('/jira-tests-legacy/tested/class/Class8.java'))
        self.assertTrue(tested_metric.on_read_line('@WebTest({Category.FUNC_TEST})'))
        self.assertTrue(tested_metric.on_read_line('public class Class8 extends SomeClassWillBeInferredJunit3 {'))
        self.assertTrue(tested_metric.on_read_line('public void testBehavior1() {'))
        self.assertTrue(tested_metric.on_read_line('public void shouldFallIfNotSupported() {'))
        self.assertTrue(tested_metric.on_read_line('@Test'))
        self.assertTrue(tested_metric.on_read_line('public void testFalling() {'))
        self.assertEqual(2, tested_metric.get_values()['tests.webtests.func'].value)
        self.assertEqual(0, tested_metric.get_values()['tests.webtests.rest'].value)
        self.assertEqual(0, tested_metric.get_values()['tests.webtests.webdriver'].value)

    def test_webtest_distinguishes_func_and_rest(self):

        tested_metric = WebTestFinder(metrics_logger=Mock())

        self.assertTrue(tested_metric.wants_file('/jira-tests-legacy/tested/class/FuncTest.java'))
        self.assertTrue(tested_metric.on_read_line('@WebTest({Category.FUNC_TEST})'))
        self.assertTrue(tested_metric.on_read_line('public class FuncTest extends Something {'))
        self.assertTrue(tested_metric.on_read_line('public void testBehavior1() {'))

        self.assertTrue(tested_metric.wants_file('/jira-tests-legacy/tested/class/RestTest.java'))
        self.assertTrue(tested_metric.on_read_line('@WebTest({Category.FUNC_TEST, Category.REST})'))
        self.assertTrue(tested_metric.on_read_line('public class RestTest extends SomethingElse {'))
        self.assertTrue(tested_metric.on_read_line('public void testRest() {'))

        self.assertEqual(1, tested_metric.get_values()['tests.webtests.func'].value)
        self.assertEqual(1, tested_metric.get_values()['tests.webtests.rest'].value)
        self.assertEqual(0, tested_metric.get_values()['tests.webtests.webdriver'].value)

    def test_webtest_looks_for_junit4_in_webdriver(self):

        tested_metric = WebTestFinder(metrics_logger=Mock())

        self.assertTrue(tested_metric.wants_file('/jira-tests-legacy/tested/class/Webdriver.java'))
        self.assertTrue(tested_metric.on_read_line('@WebTest({Category.WEBDRIVER_TEST})'))
        self.assertTrue(tested_metric.on_read_line('public class Webdriver extends SomeClassWillBeInferredJunit4 {'))
        self.assertTrue(tested_metric.on_read_line('@Test'))
        self.assertTrue(tested_metric.on_read_line('public void shouldFallIfNotSupported() {'))
        self.assertTrue(tested_metric.on_read_line('@Test'))
        self.assertTrue(tested_metric.on_read_line('public void testFalling() {'))
        self.assertEqual(0, tested_metric.get_values()['tests.webtests.func'].value)
        self.assertEqual(0, tested_metric.get_values()['tests.webtests.rest'].value)
        self.assertEqual(2, tested_metric.get_values()['tests.webtests.webdriver'].value)


if __name__ == '__main__':
    unittest.main(verbosity=2)


