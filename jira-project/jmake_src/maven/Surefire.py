import os
from CommandExecutor import Callable, SystemCallable
from Logger import Logger, LOG
from utils.FileUtils import FileUtils
from utils.XmlUtils import XmlUtils


class Surefire():

    def __init__(self, fs: FileUtils=FileUtils(), xml: XmlUtils=XmlUtils()):
        super().__init__()
        self.roots = []
        self.fs = fs
        self.xml = xml
        self.report_header_logged = False

    def set_roots(self, roots):
        self.roots = [r for r in roots]

    def append_root(self, root: str):
        self.roots.append(root)

    def __generate_surefire_dirs(self, root):
        for path, files, dirs in self.fs.walk(root):
            if path.endswith('surefire-reports'):
                yield path

    def __generate_all_surefire_dirs(self):
        for root in self.roots:
            for path, files, dirs in self.fs.walk(root):
                if path.endswith('surefire-reports'):
                    yield path

    def clean(self, log: Logger=LOG):
        for surefire_dir in self.__generate_all_surefire_dirs():
            log.debug('Removing result dir from previous build: ' + surefire_dir)
            self.fs.remove_dir(surefire_dir)
        return Callable.success

    def clean_executable(self):
        def clean_callable(_: Logger):
            return self.clean(Logger().set_none())
        return clean_callable

    def report(self, log: Logger=LOG):
        rc = SystemCallable.success
        for surefire_dir in self.__generate_all_surefire_dirs():
            for result_file in (os.sep.join([surefire_dir, res_file]) for res_file in self.fs.listdir(surefire_dir) if res_file.endswith('.xml')):

                # for performance
                has_errors = False
                for line in open(result_file):
                    if '<testsuite' in line:
                        has_errors = not ('failures="0"' in line and 'errors="0"' in line)
                        break

                if has_errors:
                    root = self.xml.parse(result_file).getroot()
                    self.__report_test_suite_in_error(log, root.attrib['name'])
                    for reason in ['failure', 'error']:
                        for failed_testcase in root.findall('.//%s/..' % reason):
                            rc += 1
                            self.__report_test_error(log, reason, failed_testcase.attrib['classname'], failed_testcase.attrib['name'])
        return rc

    def report_executable(self):
        def report_callable(log: Logger):
            return self.report(log)
        return report_callable

    def __report_test_suite_in_error(self, log: Logger, suite:str):
        if not self.report_header_logged:
            log.error('')
            log.error('Tests have failed: ')
            log.error('')
            self.report_header_logged = True
        log.error('%s:' % suite)

    def __report_test_error(self, log: Logger, reason: str, class_name: str, test_name: str):
        log.error('   {0} {1}: {2}'.format(reason.upper(), class_name, test_name))
