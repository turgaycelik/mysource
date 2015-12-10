from unittest import TestCase
from eh.metrics.tailored.JUnitAssertWithoutMessage import JunitAssertWithoutMessage
from utils.Mocks import Mock

#noinspection PyTypeChecker
class TestJunitAssertWithoutMessage(TestCase):
    def test_missing_message_simple(self):
        metric = JunitAssertWithoutMessage('junit.assert.without.message', 'junit assert test', metrics_logger=Mock()).configure(Mock(), False)
        metric.pre_files_scan('tests')
        self.assertTrue(metric.wants_file('foo.java'), 'Wants a java file')
        metric.on_read_line('assertTrue(true)')
        metric.on_read_line('assertFalse(false)')
        self.assertEqual(2, metric.value, 'Should have two hits')
        metric.post_files_scan('tests')

    def test_missing_message_complex(self):
        metric = JunitAssertWithoutMessage('junit.assert.without.message', 'junit assert test', metrics_logger=Mock()).configure(Mock(), False)
        metric.pre_files_scan('tests')
        self.assertTrue(metric.wants_file('foo.java'), "Wants a java file")
        metric.on_read_line('assertTrue("foo".equals(bar))')
        metric.on_read_line('assertFalse("foo".equals(bar))')
        metric.on_read_line('assertTrue(methodcall(with, multiple, parameters))')
        metric.on_read_line('assertFalse(methodcall(foo, bar).build(woo, hoo))')
        metric.on_read_line('assertTrue("Expected image not found. Please see logs for details.", new ImageCell(stringInImageSource).equals(table, row, col));')
        metric.on_read_line('   Assert.assertTrue("Backup returned '" + text + "' which is not an absolute file.", file.isAbsolute());')
        metric.on_read_line("""assertTrue("Expected selected option '" + expectedId + "' for element '" + elementId + "'.", selectedOptionIds.contains(expectedId));""")
        self.assertEqual(4, metric.value, 'Should have four hits')
        metric.post_files_scan('tests')

    def test_with_message_simple(self):
        metric = JunitAssertWithoutMessage('junit.assert.without.message', 'junit assert test', metrics_logger=Mock()).configure(Mock(), False)
        metric.pre_files_scan('tests')
        self.assertTrue(metric.wants_file('foo.java'), 'Wants a java file')
        metric.on_read_line('assertTrue("This message will be useful when this assertion fails.", true)')
        metric.on_read_line('assertFalse("This message will be useful when this assertion fails.", false)')
        self.assertEqual(0, metric.value, 'Should have no hits')
        metric.post_files_scan('tests')

    def test_with_message_complex(self):
        metric = JunitAssertWithoutMessage('junit.assert.without.message', 'junit assert test', metrics_logger=Mock()).configure(Mock(), False)
        metric.pre_files_scan('tests')
        self.assertTrue(metric.wants_file('foo.java'), 'Wants a java file')
        metric.on_read_line('assertTrue(methodcall(with, multiple, parameters), true)')
        metric.on_read_line('assertFalse(methodcall(foo, bar).build(woo, hoo), false)')
        self.assertEqual(0, metric.value, 'Should have no hits')
        metric.post_files_scan('tests')

    def test_resets_state(self):
        metric = JunitAssertWithoutMessage('junit.assert.without.message', 'junit assert test', metrics_logger=Mock()).configure(Mock(), False)
        metric.pre_files_scan('tests')
        self.assertTrue(metric.wants_file('foo.java'), 'Wants a java file')
        metric.on_read_line('assertTrue(methodcall(with, multiple, parameters), true)')
        metric.on_read_line('assertFalse(methodcall(foo, bar).build(woo, hoo), false)')
        metric.on_read_line('assertTrue(methodcall(with, multiple, parameters))')
        self.assertEqual(1, metric.value, 'Should have one hits')
        metric.post_files_scan('tests')

    def test_import_asserts_statically(self):
        metric = JunitAssertWithoutMessage('junit.assert.without.message', 'junit assert test', metrics_logger=Mock()).configure(Mock(), False)
        metric.pre_files_scan('tests')
        self.assertTrue(metric.wants_file('foo.java'), 'Wants a java file')
        metric.on_read_line('import static org.junit.Assert.assertFalse;')
        metric.on_read_line('import static org.junit.Assert.assertTrue;')
        self.assertEqual(0, metric.value, 'Should have no hits')
        metric.post_files_scan('tests')

    def test_multi_line_assert(self):
        metric = JunitAssertWithoutMessage('junit.assert.without.message', 'junit assert test', metrics_logger=Mock()).configure(Mock(), False)
        metric.pre_files_scan('tests')
        self.assertTrue(metric.wants_file('foo.java'), 'Wants a java file')
        metric.on_read_line('assertTrue("Expected project email to be configured",')
        self.assertEqual(0, metric.value, 'Should have no hits')

    def test_without_static_imports(self):
        metric = JunitAssertWithoutMessage('junit.assert.without.message', 'junit assert test', metrics_logger=Mock()).configure(Mock(), False)
        metric.pre_files_scan('tests')
        self.assertTrue(metric.wants_file('foo.java'), 'Wants a java file')
        metric.on_read_line('Assert.assertTrue("Expected to be true", true)')
        metric.on_read_line('Assert.assertFalse(someclass.someMethodCall(param));')
        self.assertEqual(1, metric.value, 'Should have no hits')
        metric.post_files_scan('tests')
