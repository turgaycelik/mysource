import re
from eh.metrics import RISING, FALLING
from eh.metrics.MetricsCollector import MetricsCollector, MetricsLogger

junit3_base_classes = ['Junit3ListeningTestCase',
                       'LegacyJiraMockTestCase',
                       'AbstractWebworkTestCase',
                       'AbstractChartReportTestCase',
                       'AbstractJellyTestCase',
                       'AbstractProjectRolesTest',
                       'AbstractTestViewIssueColumns',
                       'AbstractTestPermissionsStatisticsAndSearching',
                       'AbstractUsersIndexingTestCase',
                       'AbstractUsersTestCase',
                       'AbstractWebworkTestCase',
                       'AbstractWikiAttachmentTestCase',
                       'AbstractWikiTestCase',
                       'TestSearchWithPermissions',
                       ' TestCase']

junit4_annotations = ['@RunWith']


class JUnitFinder(MetricsCollector):

    def __init__(self, metrics_logger: MetricsLogger = None):
        super().__init__('tests.junit', 'Count and classify junit tests.', metrics_logger)
        self.public_class_def_extends = re.compile('public.+class.+extends.+')
        self.public_class_def_not_extends = re.compile('^(?!extends)public.+class.+')
        self.junit3_test_method = re.compile('\s*public\s.*\s?void\s+test.*\(.*\).*')

        self.results = {}

        self.line_finders = {
            3: self.junit3_test_counter,
            4: self.junit4_test_counter
        }

        self.print_next_line = False

    def wants_file(self, file_name: str):
        self.file_name = file_name
        self.junit = None
        return file_name.endswith('.java')

    def on_read_line(self, line: str):

        if not self.junit:
            if '@WebTest' in line:
                return False
            elif re.match(self.public_class_def_extends, line):
                if [s for s in junit3_base_classes if s in line]:
                    self.junit = 3
                else:
                    self.junit = 4
            elif [a for a in junit4_annotations if a in line]:
                self.junit = 4
            elif re.match(self.public_class_def_not_extends, line):
                self.junit = 4

            return True

        self.line_finders[self.junit](line)

        return True

    def junit3_test_counter(self, line):
        if re.match(self.junit3_test_method, line):
            self.__increase('3.count', line)
            if self.verbose:
                self.log.debug('Found JUnit3 test (%s) in %s.' % (self.key + '.3.count', self.file_name))

            if 'jira-tests-legacy' not in self.file_name:
                self.__increase('3.nonlegacy', line)
                if self.verbose:
                    self.log.debug('Found JUnit3 test outside of legacy module (%s) in %s.'
                                   % (self.key + '3.nonlegacy', self.file_name))

    def junit4_test_counter(self, line):
        if '@Test' in line or '@org.junit.Test' in line:
            self.__increase('4.count', line)
            if self.verbose:
                self.log.debug('Found JUnit4 test (%s) in %s.' % (self.key + '4.count', self.file_name))

    def get_metrics_keys(self):
        return map(lambda k: '%s.%s' % (self.key, k), ['3.count', '3.nonlegacy', '4.count'])

    def __increase(self, key, line):
        n = self.results[key] if key in self.results else 0
        self.results[key] = n + 1
        self.log_hit('in %s found %s, line: %s' % (self.file_name, key, line), self.key + '.' + key)

    def get_values(self):
        result = {}
        self.__append(result, '3.count', 'Get rid of JUnit 3 test cases', FALLING)
        self.__append(result, '3.nonlegacy', 'Contain JUnit 3 test cases to legacy module', FALLING)
        self.__append(result, '4.count', 'Keep count of JUnit 4 test cases', RISING, False)
        return result

    def __append(self, result: dict, key: str, description: str, direction: str, checked: bool=True):
        value = self.results[key] if key in self.results else 0
        result[self.key + '.' + key] = self.produce_result(value, description, checked, direction)
