import json
import os
import re
import types
import operator
from Logger import Logger
from eh.metrics.MetricsCollector import MetricsCollector, MetricsLogger
from maven.Maven import MavenCallable, MavenExecutionException


class DeprecatedMethodUsage(MetricsCollector):
    def __init__(self, metrics_name: str='deprecation.methods', description: str='Deprecated methods usage',
        metrics_logger: MetricsLogger = None):

        super().__init__(metrics_name, description, metrics_logger)
        self.deprecated_stats = {}

    #first try, just clean compile everything...
    def get_values(self):
        def process_output(myself, log: Logger, line: str, num: int):
            if '[deprecation]' in line:
                self.hit(re.sub(r'(.*java:)\[\d+,\d+\] \[deprecation\]( .*)', r'\1\2',
                    re.sub(r'\s*\[WARNING\]\s+' , '', line.replace(os.getcwd() + '/', '').replace('[WARNING]', ''))
                ))
                if self.log.is_trace():
                    self.add_to_stats(line)
                return self.verbose or self.log.is_trace()

            return line.startswith("[INFO] Building Atlassian") or line.startswith('[ERROR]') or self.log.is_trace()

        maven = MavenCallable()
        maven.phase('clean').phase('test-compile').profile('jira-metrics').profile('ondemand')
        maven.property('jira.exclude.bundled.plugins')
        maven.option('--no-plugin-updates')
        if self.log.is_trace():
            maven.option('-X')
        self.log.info('Compiling workspace to find deprecated methods usage...')
        maven.process_output = types.MethodType(process_output, maven)

        super().pre_files_scan('irrelevant')
        maven_return_code = self.run_maven(maven)
        super().post_files_scan('irrelevant')

        if maven_return_code != MavenCallable.success:
            raise MavenExecutionException

        self.log.trace(
            json.JSONEncoder(indent=True).encode(sorted(self.deprecated_stats.items(), key=operator.itemgetter(1))))
        self.log.info('Compilation finished.')
        return super().get_values() if maven.returncode == 0 else {}

    def run_maven(self, maven: MavenCallable):
        return maven(Logger().set_none())

    def add_to_stats(self, line):
        re_match = re.match(r'.*\[deprecation\] (.+?) in (.+?) has been deprecated', line)
        if re_match is None:
            return
        key = re_match.group(1) + '#' + re_match.group(2)
        if key in self.deprecated_stats:
            self.deprecated_stats[key] += 1
        else:
            self.deprecated_stats[key] = 1

    def pre_files_scan(self, module: str):
        pass

    def post_files_scan(self, module: str):
        pass
