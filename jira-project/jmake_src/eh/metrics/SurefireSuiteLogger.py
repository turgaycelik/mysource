import os
from utils.FileUtils import FileUtils

class TestCase():
    def __init__(self, name: str, time: float, classname: str, failed: bool = False, failed_log: str = None):
        super().__init__()
        self.name = name
        self.time = time
        self.classname = classname
        self.failed = failed
        self.failed_log = failed_log

class SurefireSuiteLoggerFactory():

    def __init__(self, fs: FileUtils = FileUtils()):
        super().__init__()
        self.fs = fs

    def new_logger(self, suite_name: str):
        return SurefireSuiteLogger(suite_name, self.fs)


class SurefireSuiteLogger():

    target_dir = os.sep.join(['.', 'jmake_src', 'target', 'metrics-results-as-tests'])

    def __init__(self, suite_name: str, fs: FileUtils = FileUtils()):
        super().__init__()
        self.suite_name = suite_name
        self.fs = fs
        self.test_cases=[]

    def failed(self, name, time, classname, log):
        self.test_cases += [TestCase(name, time, classname, True, log)]
        pass

    def success(self, name, time, classname):
        self.test_cases += [TestCase(name, time, classname)]
        pass

    def to_xml(self):
        return ['<?xml version="1.0" encoding="UTF-8"?>', self.__generate_suite_start(), '<properties/>'
               ] + self.__generate_test_cases() + [
                   '</testsuite>'
               ]

    def save(self):
        dest_dir = self.fs.existing_dir(SurefireSuiteLogger.target_dir)
        file_name = os.sep.join([dest_dir, 'TEST-%s.xml' % self.suite_name])
        self.fs.write_lines(file_name, self.to_xml())

    def __generate_suite_start(self):
        failures = 0
        time = 0
        errors = 0
        tests = 0

        for case in self.test_cases:
            tests += 1
            failures += 1 if case.failed else 0
            time += case.time


        return '<testsuite failures="%s" time="%s" errors="%s" skipped="0" tests="%s" name="%s">' % (
            failures, time, errors, tests, self.suite_name
        )

    def __generate_test_cases(self):
        res = []
        for case in self.test_cases:
            res += [
                '<testcase time="%s" classname="%s" name="%s">' % (case.time, case.classname, case.name)
            ] + (['<failure>%s</failure>' % case.failed_log] if case.failed else []) + [
                '</testcase>'
            ]

        return res