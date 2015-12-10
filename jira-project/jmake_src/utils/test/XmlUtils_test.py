from unittest import TestCase
import xml.etree.ElementTree as XML
from utils.XmlUtils import XmlUtils


class XmlUtilsTest(TestCase):

    def setUp(self):
        self.xml = XmlUtils()

    def test_produce_method(self):
        parent = XML.Element('parent', {'id': '1'})

        child1 = self.xml.produce(parent, ('child', {'id': '2'}))

        self.assertEqual(len(parent), 1)
        self.assertDictEqual(child1.attrib, {'id': '2'})
        self.assertIn(child1, parent)
        child1.attrib['name'] = 'child1'

        self.xml.produce(parent, ('child', {'name': 'child1'}))
        self.assertEqual(len(parent), 1)

        child2 = self.xml.produce(parent, ('child', {'name': 'child2'}))
        self.assertEqual(len(parent), 2)

        self.xml.produce(child2, ('sub', {'id': '3'}))
        self.assertEqual(len(child2), 1)


