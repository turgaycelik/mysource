import os
import re
from Logger import Logger
from eh.metrics import FALLING, NEUTRAL, RISING
from utils.DataBean import DataBean
from utils.FileUtils import FileUtils


class MetricsLogger():

    def __init__(self, file_utils: FileUtils = FileUtils()):
        super().__init__()
        self.file_utils = file_utils
        self.hit_files = {}

    def open(self, metrics_keys):
        # Rewrite file for fist time, append in other cases
        for metric in metrics_keys:
            file_mode = 'w' if metric not in self.hit_files else 'a'
            self.hit_files[metric] = self.file_utils.open(os.sep.join([self.file_utils.existing_dir(MetricsCollector.log_directory),
                                                          'eh-metrics-hit-for-%s.log' % metric]), file_mode)

    def close(self):
        for f in self.hit_files.values():
            f.close()

    def log(self, hit, metric_key):
        if self.hit_files[metric_key] is None:
            raise IOError('Hit file for %s is not open!' % metric_key)

        print(hit, file=self.hit_files[metric_key])


class ConfigurationError(Exception):
    pass


class MetricsCollector(object):

    log_directory = os.sep.join(['.', 'jmake_src', 'target', 'eh-metrics-hits'])

    def __init__(self, metrics_name: str, description: str = None, metrics_logger: MetricsLogger = None):
        super().__init__()
        self.key = metrics_name
        self.description = description if description else '(no description given)'
        self.verbose = None
        self.value = 0
        self.checked = True
        self.metrics_logger = metrics_logger if metrics_logger else MetricsLogger()
        self.direction = FALLING

    def get_metrics_keys(self):
        """
        Override if your collector produces more metrics.
        """
        return [self.key]

    def configure(self, log: Logger, verbose: bool):
        self.log = log
        self.verbose = verbose

        if self.direction == NEUTRAL and self.checked:
            raise ConfigurationError('Metric %s cannot be configured to neutral and checked.' % self.key)

        return self

    def pre_files_scan(self, module: str):
        """
        Override to do some crazy stuff before we scan all files.
        """
        self.metrics_logger.open(self.get_metrics_keys())
        return None

    def post_files_scan(self, module: str):
        """
        Override to do some crazy stuff after we scan all files.
        """
        self.metrics_logger.close()
        return None

    def get_values(self):
        """
        Return the stuff you measured. Override to return more than one metrice.
        """
        return {self.key : self.produce_result()}

    def wants_file(self, file_name: str):
        """
        Override to tell whether you are interested in the file. Be precise!
        """
        return False

    def on_read_line(self, line: str):
        """
        Override to consume lines. Return False if not more interested in the file, or True to request more lines.
        A baby fox dies each time you request more lines when it is impossible to affect your value.
        """
        return False

    def hit(self, hit, metric_key = None):
        self.value += 1
        self.log_hit(hit, metric_key)

    def log_hit(self, hit, metric_key = None):
        self.metrics_logger.log(hit, metric_key if metric_key is not None else self.key)

    def produce_result(self, value = None, description = None, checked = None, direction = None):

        value = self.value if value is None else value
        description = self.description if description is None else description
        checked = self.checked if checked is None else checked
        direction = self.direction if direction is None else direction

        return DataBean(value = value, description = description, checked = checked, direction = direction)

    def unchecked(self):
        return self.set_checked(False)

    def set_checked(self, whether: bool):
        self.checked = whether
        return self

    def falling(self):
        return self.set_direction(FALLING)

    def neutral(self):
        return self.set_direction(NEUTRAL)

    def rising(self):
        return self.set_direction(RISING)

    def set_direction(self, direction):
        self.direction = direction
        return self

    def clean_file_name(self, file_name):
        if file_name is None:
            return '(?)'
        if file_name.startswith('/'):
            if '/repository/' in file_name:
                return re.sub(r'.*/repository/', 'M2_REPOSITORY/', file_name)
            elif '/jira-components/' in file_name:
                return re.sub(r'.*/(jira-components/.*)', r'JIRA_ROOT/\1', file_name)
            else:
                self.log.warn('I don\'t know how to clean file name: %s' % file_name)
                return file_name
        else:
            return file_name

def requires_java_import(required_import: str):
    """
    If a on_read_line of a collector is decorated with this decorator it will
    cause the decoration to handle looking for an import statement before the
    decorated function is called. This is to fail-fast on java files that cannot
    contain what we want.
    """
    def wrapper(wrapped_function):
        def read_line_wrapper(self, line):

            if not hasattr(self, 'found_import'):
                self.found_import = False

            if line.strip().startswith('package '):
                self.found_import = False
                return True

            if self.found_import:
                return wrapped_function(self, line)
            elif line.strip().startswith('import ') and required_import in line:
                self.found_import = True
                return True
            elif '{' in line:
                return False
            else:
                return True
        return read_line_wrapper
    return wrapper
