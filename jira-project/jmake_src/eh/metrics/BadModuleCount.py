import re
from eh.metrics.MetricsCollector import MetricsCollector

class BadModuleCount(MetricsCollector):

    def pre_files_scan(self, module: str):
        super().pre_files_scan(module)
        self.value += 1
        self.current_module = module

    def hit(self, hit, metric_key = None):
        if self.current_module:
            self.value -= 1
        self.current_module = None
        self.log_hit(hit, metric_key)

    def post_files_scan(self, module: str):
        if self.current_module:
            self.log_hit("module marked as bad: %s" % self.clean_file_name(module))
        super().post_files_scan(module)