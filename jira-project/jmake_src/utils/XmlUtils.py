from functools import reduce
import xml.etree.ElementTree as XML
from utils.Mocks import Mock

class XmlUtils:

    def parse(self, path):
        return XML.parse(path)

    def parse_string(self, string: str):
        return XML.fromstring(string)

    def __find_child(self, parent, name, attr):
        return parent.find(reduce(lambda result, e: result + '[@%s="%s"]' % e, attr.items(), './' + name))

    def child_exists(self, parent, name, attr):
        return self.__find_child(parent, name, attr) is not None

    def produce(self, parent, *branch):

        if len(branch):
            descendant = branch[0]
            name = descendant[0]
            attr = descendant[1]
            child = self.__find_child(parent, name, attr)
            if child is None:
                child = XML.SubElement(parent, name, attr)
            return self.produce(child, *(branch[1:]))
        else:
            return parent

    def copy_tree(self, parent, tree_root):
        """
        Copies tree_root and all of its children under parent element.
        """
        if tree_root is not None:
            child = self.produce(parent, (tree_root.tag, tree_root.attrib))
            child.text = tree_root.text
            child.tail = tree_root.tail

            for subtree in tree_root:
                self.copy_tree(child, subtree)

    def tostring(self, tree_root):
        return XML.tostring(tree_root)

class MockXmlUtils(XmlUtils):

    def __init__(self):
        super().__init__()
        self.doms = {}

    def expect_parse(self, path, dom):
        self.doms[path] = dom

    def parse(self, path):
        return Mock().expect_getroot(toReturn=self.doms[path]) if path in self.doms else None