from eh.metrics.MetricsCollector import MetricsCollector, MetricsLogger
from utils.FileUtils import FileUtils

class FileCount(MetricsCollector):

    def __init__(self, metrics_name: str, file_filter, description: str = None, metrics_logger: MetricsLogger = None):
        super().__init__(metrics_name, description, metrics_logger)
        self.file_filter = file_filter

    def wants_file(self, file_name: str):
        if self.file_filter(file_name):
            if self.verbose:
                self.log.debug('File Count: ' + file_name)
            self.hit('file counted: %s' % file_name)
        return False

class FileCountAndSize(MetricsCollector):

    def __init__(self, metrics_name: str, file_filter, description: str = None, file_utils: FileUtils = FileUtils(),
            metrics_logger: MetricsLogger = None):
        super().__init__(metrics_name, description, metrics_logger)
        self.file_filter = file_filter
        self.count = 0
        self.size = 0
        self.file_utils = file_utils
        self.metric_key_count = '.'.join([self.key, 'count'])
        self.metric_key_cumulativesize = '.'.join([self.key, 'cumulativesize'])

    def wants_file(self, file_name: str):
        if self.file_filter(file_name):
            if self.verbose:
                self.log.debug('File Count: ' + file_name)
            self.count += 1

            size = self.file_utils.file_size(file_name)
            if self.verbose:
                self.log.debug('File Size: ' + str(size))
            self.size += size

            self.log_hit('file counted: %s' % file_name, self.metric_key_count)
            self.log_hit('file: %s, size: %s' % (file_name, size), self.metric_key_cumulativesize)
        return False

    def get_metrics_keys(self):
        return [self.metric_key_count, self.metric_key_cumulativesize]

    def get_values(self):
        return { self.metric_key_count : self.produce_result(self.count, self.description + ' - file count'),
                 self.metric_key_cumulativesize : self.produce_result(self.size, self.description + ' - file size') }



