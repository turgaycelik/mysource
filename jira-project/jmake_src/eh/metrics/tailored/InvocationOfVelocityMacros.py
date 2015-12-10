import re
from eh.metrics.MetricsCollector import MetricsCollector, MetricsLogger


class InvocationOfVelocityMacros(MetricsCollector):

    def __init__(self, metrics_logger: MetricsLogger=None):
        super().__init__('frontend.invocations.of.vm.macros', 'Keep count of vm macros invocations', metrics_logger)

        self.line_handler = None
        self.vm_directive_pattern = re.compile('(?<![\\\\#])#\s*\{?([-_a-zA-Z0-9]+)}?\s*\(')
        self.vm_reserved_words = ['for', 'foreach', 'macro', 'set', 'if', 'elseif', 'else', 'include', 'stop', 'parse',
                                  'end']

    def wants_file(self, file_name: str):
        wants = file_name.endswith('.vm')
        if wants:
            self.file_name = self.clean_file_name(file_name)
        return wants

    def on_read_line(self, line: str):
        line = line[:line.find('##')]
        if '#' in line:
            for match in (m for m in re.findall(self.vm_directive_pattern, line) if m not in self.vm_reserved_words):
                self.hit('found invocation of %s macro in %s' % (match, self.file_name))
        return True

    def hit(self, hit, metric_key=None):
        if self.verbose:
            self.log.debug(hit)
        super().hit(hit, metric_key)
