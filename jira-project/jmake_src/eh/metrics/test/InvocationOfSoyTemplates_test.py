from unittest import TestCase
import unittest
from eh.metrics.tailored.InvocationOfSoyTemplates import InvocationOfSoyTemplates
from utils.Mocks import Mock


# noinspection PyTypeChecker
class TestSoyInvocations(TestCase):

    def setUp(self):
        self.metric = InvocationOfSoyTemplates(metrics_logger=Mock())
        self.metric.pre_files_scan('test-module')

    def test_wants_files(self):
        self.assertTrue(self.metric.wants_file('some-file.soy'), 'should scan soy files.')
        self.assertTrue(self.metric.wants_file('another.vm'), 'should scan vm files.')
        self.assertTrue(self.metric.wants_file('legacy.jsp'), 'should scan jsp files.')
        self.assertTrue(self.metric.wants_file('lots.of.java'), 'should scan java files.')
        self.assertTrue(self.metric.wants_file('from/somewhere/actions.xml'), 'should scan action.xml file.')

        self.assertFalse(self.metric.wants_file('from/somewhere/else/epicactions.xml'), 'should not scan this file.')

    def test_java_scanner_with_variable(self):
        self.metric.wants_file('my-servlet.java')
        self.metric.on_read_line('package somepackage;')

        self.metric.on_read_line('import com.atlassian.soy.renderer.SoyTemplateRenderer;')
        self.metric.on_read_line(' SoyTemplateRenderer myRenderer = null')
        self.metric.on_read_line(' myRenderer.render();')
        self.metric.on_read_line(' myOtherRenderer.render();')

        self.assertEqual(self.metric.value, 1)

    def test_java_scanner_without_import_gives_up(self):
        self.metric.wants_file('my-servlet.java')
        self.metric.on_read_line('package somepackage;')

        self.assertFalse(self.metric.on_read_line('public class MyClass {'),
                         msg='should give up on no import of soy renderer.')

    def test_java_scanner_with_method(self):
        self.metric.wants_file('my-servlet.java')
        self.metric.on_read_line('package somepackage;')

        self.metric.on_read_line('import com.atlassian.soy.renderer.SoyTemplateRenderer;')
        self.metric.on_read_line('public SoyTemplateRenderer getRenderer() {')
        self.metric.on_read_line('  this.getRenderer().render(myTemplate);')
        self.metric.on_read_line('  getSoyRenderer().render(somethingElse);')
        self.metric.on_read_line('  soyRenderer.render(somethingElse);')

        self.assertEqual(self.metric.value, 1)

    def test_java_scanner_defers_possible_hits(self):
        self.metric.wants_file('my-servlet.java')
        self.metric.on_read_line('package somepackage;')

        self.metric.on_read_line('import com.atlassian.soy.renderer.SoyTemplateRenderer;')
        self.metric.on_read_line('  this.getRenderer().render(templateA);')
        self.metric.on_read_line('  other.getRenderer().render(templateB);')
        self.metric.on_read_line('  someOtherRenderer.render(weird);')
        self.metric.on_read_line('  getRenderer().render(templateC);')
        self.metric.on_read_line('private SoyTemplateRenderer getRenderer() {')
        self.metric.on_read_line('  this.getRenderer().render(templateE);')

        self.assertEqual(self.metric.value, 4)

    def test_java_scanner_deferred_hits_get_lost(self):
        self.metric.wants_file('my-servlet.java')
        self.metric.on_read_line('package somepackage;')

        self.metric.on_read_line('import com.atlassian.soy.renderer.SoyTemplateRenderer;')
        self.metric.on_read_line('  this.getRenderer().render(templateA);')
        self.metric.on_read_line('  other.getRenderer().render(templateB);')
        self.metric.on_read_line('  someOtherRenderer.render(weird);')
        self.metric.on_read_line('  getRenderer().render(templateC);')
        self.metric.on_read_line('  this.getRenderer().render(templateE);')

        self.metric.wants_file('another-servlet.java')

        self.assertEqual(self.metric.value, 0)

if __name__ == '__main__':
    unittest.main(verbosity=2)