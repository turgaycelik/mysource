from eh.metrics.MetricsCollector import MetricsCollector, MetricsLogger

class ManagersInJiraApi(MetricsCollector):

    def __init__(self, metrics_name: str = 'jiraapi.managers', metrics_logger: MetricsLogger = None):
        #noinspection PyTypeChecker
        super().__init__(metrics_name, None, metrics_logger)
        self.file_filter = lambda x : x.endswith('Manager.java')
        self.enabled = False

        self.count = 0
        self.not_deprecated = 0

        self.metric_key_count = '.'.join((self.key, 'count'))
        self.metric_key_count_not_deprecated = '.'.join((self.key, 'count', 'not_deprecated'))

    def pre_files_scan(self, module: str):
        super().pre_files_scan(module)
        self.enabled = module.endswith('jira-api')

    def wants_file(self, file_name: str):
        self.is_deprecated = False
        self.file = file_name
        return self.enabled and file_name.endswith('Manager.java')

    def on_read_line(self, line: str):
        if not self.is_deprecated and 'eprecated' in line:
            self.is_deprecated = True
            return True

        if line.lstrip(' \t').startswith('public') and 'Manager' in line:
            if 'class' in line:
                if not self.is_deprecated:
                    self.not_deprecated += 1
                    self.log_hit('file: %s, line: %s' % (self.file, line.replace("\n", "")), self.metric_key_count_not_deprecated)
                self.count += 1
                self.log_hit('file: %s, deprecated: %s, line: %s' % (self.file, self.is_deprecated, line.replace("\n", "")), self.metric_key_count)
                if self.verbose:
                    self.log.debug('Manager implementation in jira-api: ' + self.file)
            return False

        return True

    def get_metrics_keys(self):
        return [self.metric_key_count, self.metric_key_count_not_deprecated]

    def get_values(self):
        return { self.metric_key_count: self.produce_result(self.count, "Manager implementations in JIRA API"),
                 self.metric_key_count_not_deprecated: self.produce_result(self.not_deprecated, "Manager implementations in JIRA API - not deprecated") }








