import re
from eh.metrics.MetricsCollector import MetricsCollector, MetricsLogger


class WebTestFinder(MetricsCollector):

    def __init__(self, metrics_logger: MetricsLogger = None):
        self.class_def_pattern = re.compile('\s*public\s(.*\s)?class\s.*')
        self.test_method_pattern = re.compile('.*public\s(.*\s)?void\s(.*\s)?test.*\(.*\).*')
        self.results = {}
        super().__init__("tests.webtests", "Keeps count on web tests", metrics_logger)

    def wants_file(self, file_name: str):
        self.test_type = None
        self.file_name = file_name
        return file_name.endswith('.java')

    def increase(self, what, line):
        self.log_hit('in %s found %s, line: %s' % (self.file_name, what, line),  self.key + '.' + self.test_type)
        if self.test_type in self.results:
            self.results[self.test_type] += 1
        else:
            self.results[self.test_type] = 1

    def on_read_line(self, line: str):
        if not self.test_type:
            if '@WebTest' in line:
                if 'Category.WEBDRIVER_TEST' in line:
                    self.test_type = 'webdriver'
                elif 'Category.FUNC_TEST' in line:
                    self.test_type = 'rest' if 'Category.REST' in line else 'func'

            if re.match(self.class_def_pattern, line):
                # not a web test: release.
                return False
            return True
        else:

            if self.test_type == 'webdriver':
                # JUnit4:
                if '@Test' in line:
                    self.increase('junit4', line)
            else:
                # JUnit3:
                if re.match(self.test_method_pattern, line):
                    self.increase('junit3', line)
            return True

    def get_metrics_keys(self):
        return map(lambda k: '%s.%s' % (self.key, k), ['webdriver', 'func', 'rest'])

    def get_values(self):
        result = {}
        self.__append(result, 'webdriver', 'Keep count of webdriver tests.')
        self.__append(result, 'func', 'Keep count of func tests.')
        self.__append(result, 'rest', 'Keep count of rest tests.')
        return result

    def __append(self, result: dict, key: str, description: str):
        value = self.results[key] if key in self.results else 0
        result[self.key + '.' + key] = self.produce_result(value, description)
