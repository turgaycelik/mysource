from eh.metrics.MetricsCollector import MetricsCollector, MetricsLogger

NO_PACKAGE = ';'

class JavaPackageImports(MetricsCollector):

    def __init__(self, metrics_name: str, description: str = None, importClass: str = None, package: str = NO_PACKAGE,
        whitelist = None, metrics_logger: MetricsLogger = None):

        super().__init__(metrics_name, description, metrics_logger)

        self.importClass = importClass
        self.package = package
        self.parsedPackage = None
        self.whitelist = whitelist if whitelist else list()

    def wants_file(self, file_name: str):
        wants = file_name.endswith('.java')
        if wants:
            self.file_name = file_name

        self.parsedPackage = None
        return wants

    def on_read_line(self, line: str):
        if not self.parsedPackage:
            if line.lstrip(' \t').startswith('package'):
                if self.package in line:
                    self.parsedPackage = True
                    return True
                else:
                    # wrong package: release file.
                    return False
            return True

        if line.startswith('import'):
            if any(whitelist_class in line for whitelist_class in self.whitelist):
                return True
            if self.importClass in line:
                if self.verbose:
                    if self.package == NO_PACKAGE:
                        self.log.debug('JavaPackageImportCount: %s imported in: %s' % (self.importClass, self.file_name))
                    else:
                        self.log.debug('JavaPackageImportCount: %s imported from %s package in: %s' % (self.importClass, self.package, self.file_name))
                self.hit('file: %s, import: %s from %s package, line: %s' % (self.file_name, self.importClass, self.package, line.replace("\n", "")))
                return True

        return not '{' in line