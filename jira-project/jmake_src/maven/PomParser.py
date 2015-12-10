import os
from xml.etree.ElementTree import ParseError
from utils.XmlUtils import XmlUtils

DEFAULT_MAVEN_PACKAGING = 'jar'

class PomParser:
    ns = '{http://maven.apache.org/POM/4.0.0}'

    def __init__(self, pom_path, xml=XmlUtils()):
        path_abspath = os.path.abspath(pom_path)
        if not os.path.lexists(pom_path):
            raise IOError('Pom file not found %s' % path_abspath)
        try:
            self.pom_xml = xml.parse(path_abspath)
        except ParseError as e:
            raise ParseError('Error parsing %s: %s' % (path_abspath, e.msg))
        self.root_element = self.pom_xml.getroot()

    def get_version(self):
        value = self.root_element.find('./{0}version'.format(self.ns))
        return value.text if value is not None else None

    def get_artifact_id(self):
        value = self.root_element.find('./{0}artifactId'.format(self.ns))
        return value.text if value is not None else None

    def get_property(self, property):
        value = self.root_element.find('.//{0}properties/{0}{1}'.format(self.ns, property))
        return value.text if value is not None else None

    def get_developer_connection(self):
        value = self.root_element.find('./{0}scm/{0}developerConnection'.format(self.ns))
        return value.text if value is not None else None
