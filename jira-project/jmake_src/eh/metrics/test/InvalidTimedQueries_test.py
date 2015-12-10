from unittest import TestCase
import unittest
from eh.metrics.tailored.InvalidTimedQueries import InvalidTimedQueries
from utils.Mocks import Mock


# noinspection PyTypeChecker
class TestInvalidTimedQueries(TestCase):

    def setUp(self):
        self.metric = InvalidTimedQueries(metrics_logger=Mock())
        self.metric.pre_files_scan('test-module')

    def test_catches_timed_query_without_wait(self):
        self.metric.wants_file('somePageObject.java')
        self.metric.on_read_line('package somepackage;')
        self.metric.on_read_line('import com.atlassian.pageobjects.binder.WaitUntil;')

        self.metric.on_read_line('@WaitUntil')
        self.metric.on_read_line('public myMethod()')
        self.metric.on_read_line('{')
        self.metric.on_read_line('    myElement.timed().isPresent();')
        self.metric.on_read_line('}')

        self.assertEqual(self.metric.value, 1)

    def test_respects_polled_timed_queries(self):
        self.metric.wants_file('somePageObject.java')
        self.metric.on_read_line('package somepackage;')
        self.metric.on_read_line('import com.atlassian.pageobjects.binder.WaitUntil;')

        self.metric.on_read_line('@WaitUntil')
        self.metric.on_read_line('public myMethod()')
        self.metric.on_read_line('{')
        self.metric.on_read_line('    Poller.waitUntilTrue(myElement.timed().isPresent());')
        self.metric.on_read_line('}')

        self.assertEqual(self.metric.value, 0)

    def test_gives_up_on_no_import(self):
        self.metric.wants_file('somePageObject.java')
        self.metric.on_read_line('package somepackage;')

        self.assertFalse(self.metric.on_read_line('public class MyClass {'),
                         msg='should give up on no import of annotation.')

if __name__ == '__main__':
    unittest.main(verbosity=2)