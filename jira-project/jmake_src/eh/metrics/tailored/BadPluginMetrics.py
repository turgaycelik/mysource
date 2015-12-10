from eh.metrics.BadModuleCount import BadModuleCount
from eh.metrics.MetricsCollector import MetricsLogger, MetricsCollector
import xml.dom.minidom

class MissingHostComponentsXml(BadModuleCount):

    def wants_file(self, file_name: str):
        if file_name.endswith("META-INF/spring/atlassian-plugins-host-components.xml"):
            if self.verbose:
                self.log.debug('Found component imports xml: ' + file_name)
            self.hit('file counted (this is ok) in module %s, file: %s' % (
                self.clean_file_name(self.current_module), self.clean_file_name(file_name)))
        return False

class MissingOsgiManifest(BadModuleCount):

    def __init__(self, metrics_name: str, description: str = None, metrics_logger: MetricsLogger = None):
        super().__init__(metrics_name, description, metrics_logger)

    def wants_file(self, file_name: str):
        self.file_name = file_name
        return file_name.endswith("MANIFEST.MF")

    def on_read_line(self, line: str):
        if line.startswith("Import-Package:"):
            self.hit('import package found in osgi manifest (this is ok) in module %s, file %s' % (
                self.clean_file_name(self.current_module), self.clean_file_name(self.file_name)))
            return False

        return True

class PluginXmlMinified(MetricsCollector):

    def pre_files_scan(self, module: str):
        super().pre_files_scan(module)
        self.current_module = module
        self.current_xml = ""

    def wants_file(self, file_name: str):
        if file_name.endswith("atlassian-plugin.xml") and not self.current_xml:
            self.file_name = file_name
            return True

        return False

    def on_read_line(self, line: str):
        self.current_xml += line
        return True

    def post_files_scan(self, module: str):
        if self.current_xml:
            pretty_small_xml = xml.dom.minidom.parseString(self.current_xml).toprettyxml("", "")

            if pretty_small_xml.count('\n') < self.current_xml.count('\n'):
                self.hit('found unminified plugin xml in module %s, file %s' % (
                    self.clean_file_name(self.current_module), self.clean_file_name(self.file_name)))

        self.current_xml = ""
        super().post_files_scan(module)