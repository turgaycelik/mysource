import re
from eh.metrics.MetricsCollector import MetricsCollector, MetricsLogger, requires_java_import


class InvocationOfSoyTemplates(MetricsCollector):

    def __init__(self, metrics_logger: MetricsLogger=None):
        super().__init__('frontend.invocations.of.soy', 'Keep count of soy invocations', metrics_logger)
        self.invocation_types = [
            (lambda x: x.endswith('.soy'), self.handle_soy_invocations),
            (lambda x: x.endswith('.jsp'), self.handle_jsp_invocations),
            (lambda x: x.endswith('.vm'), self.handle_vm_invocations),
            (lambda x: x.endswith('.java'), self.handle_java_invocations),
            (lambda x: x.endswith('/actions.xml'), self.handle_action_declarations)
        ]
        self.line_handler = None
        self.javaRendererTypeDeclarationPattern = re.compile('SoyTemplateRenderer\s+([^\s]+)')

    def wants_file(self, file_name: str):

        self.file_name = None
        self.line_handler = None

        self.javaSoyRendererReferenceNameWithRenderCall = None
        self.javaLinesWithRenderCalls = []

        for handler in self.invocation_types:
            if handler[0](file_name):
                self.line_handler = handler[1]
                self.file_name = file_name
                return True

        return False

    def on_read_line(self, line: str):
        if self.line_handler is not None:
            return self.line_handler(line)
        return False

    def handle_soy_invocations(self, line: str):
        if '{/call}' in line:
            self.hit('soy template direct invocation in %s.' % self.file_name)
        return True

    def handle_jsp_invocations(self, line: str):
        if '</ui:soy>' in line:
            self.hit('soy template invocation from jsp in %s.' % self.file_name)
        return True

    def handle_vm_invocations(self, line: str):
        if '$soyRenderer.render' in line:
            self.hit('soy template invocation from vm in %s.' % self.file_name)
        return True

    def handle_action_declarations(self, line: str):
        if 'type="soy"' in line:
            self.hit('soy template declared as action view in %s.' % self.file_name)
        return True

    @requires_java_import('com.atlassian.soy.renderer.SoyTemplateRenderer')
    def handle_java_invocations(self, line: str):

        if '.render(' in line:
            if self.javaSoyRendererReferenceNameWithRenderCall is None:
                self.javaLinesWithRenderCalls.append(line)
            else:
                if self.javaSoyRendererReferenceNameWithRenderCall in line:
                    self.hit('soy template rendered from Java file: %s.' % self.file_name)

        if self.javaSoyRendererReferenceNameWithRenderCall is None:
            for match in re.findall(self.javaRendererTypeDeclarationPattern, line):
                self.javaSoyRendererReferenceNameWithRenderCall = match + '.render('
                for deferred_match in self.javaLinesWithRenderCalls:
                    if self.javaSoyRendererReferenceNameWithRenderCall in deferred_match:
                        self.hit('soy template rendered from Java file: %s.' % self.file_name)

        return True

    def hit(self, hit, metric_key=None):
        if self.verbose:
            self.log.debug(hit)
        super().hit(hit, metric_key)


