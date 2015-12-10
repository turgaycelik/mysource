from eh.metrics.MetricsCollector import MetricsCollector, MetricsLogger, requires_java_import


class InvalidTimedQueries(MetricsCollector):

    def __init__(self, metrics_logger: MetricsLogger=None):
        super().__init__('pageobjects.invalid.timed.queries', 'Prevent @WaitUntil methods without waiting inside.', metrics_logger)
        self.poll_methods = [
            'waitUntil(',
            'waitUntilTrue(',
            'waitUntilFalse(',
            'bind(LoadingBarrier.class).await()'
        ]

    def wants_file(self, file_name: str):

        wants = file_name.endswith('.java')

        if wants:
            self.file_name = self.clean_file_name(file_name)
            self.in_scanning = False
        return wants

    @requires_java_import('com.atlassian.pageobjects.binder.WaitUntil')
    def on_read_line(self, line: str):

        if self.in_scanning:
            if self.method_started:
                if any(poll_method in line for poll_method in self.poll_methods):
                    self.in_scanning = False
            else:
                if '{' in line:
                    self.method_started = True
            if '}' in line:
                self.hit('method annotated with @WaitUntil without actual waiting inside in %s' % self.file_name)
                self.in_scanning = False

        else:
            if '@WaitUntil' in line:
                self.in_scanning = True
                self.method_started = False

        return True

    def hit(self, hit, metric_key=None):
        if self.verbose:
            self.log.debug(hit)
        super().hit(hit, metric_key)
