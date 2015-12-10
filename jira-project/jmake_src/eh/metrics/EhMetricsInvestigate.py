import argparse
from difflib import ndiff
import os
import re
from CommandExecutor import CommandExecutor, Callable
from jbac.JBAC import Jbac, JbacAuthentication
import Logger
from module import JmakeModule
from utils.BambooUtils import BambooUtils
from utils.FileUtils import FileUtils
from utils.ParserUtils import wrap_text
from utils.TarUtils import TarUtils
from utils.UrlUtils import UrlUtils


class MetricsDiffResult():

    def __init__(self, metric: str, diff = ([])):
        super().__init__()
        self.metric = metric
        self.diff = diff

    def __eq__(self, other):
        return self.metric == other.metric and list(self.diff) == list(other.diff)

    def __str__(self):
        return 'MetricsDiffResult{ metric = %s, diff = %s }' % (self.metric, list(self.diff))

    def __repr__(self):
        return self.__str__()


class MetricsDiff():
    HIT_LOG_FILE_NAME_PATTERN = 'eh-metrics-hit-for-%s.log'

    def __init__(self, file_utils: FileUtils):
        super().__init__()
        self.file_utils = file_utils


    def diff_lines(self, generator_a, generator_b):
        res = ndiff(sorted(generator_a), sorted(generator_b))
        for line in (line for line in res if re.match(r'[+-?].*', line)):
            yield line.replace('\n', '')

    def list_log_files(self, dir_name):
        for f in (f for f in self.file_utils.listdir(dir_name) if f.endswith('.log')):
            yield f

    def __get_all_metrics_list(self, dir_now):
        metrics_to_compare = []
        for f in self.file_utils.listdir(dir_now):
            m = re.match('eh-metrics-hit-for-(.*).log', f)
            if m is not None:
                metrics_to_compare.append(m.group(1))
        return metrics_to_compare

    def diff_metrics(self, log: Logger, dir_now: str, dir_before: str, metrics_to_compare):
        files_now = list(self.list_log_files(dir_now))
        files_before = list(self.list_log_files(dir_before))

        if metrics_to_compare is None:
            metrics_to_compare = self.__get_all_metrics_list(dir_now)

        for m in metrics_to_compare:
            file_name = MetricsDiff.HIT_LOG_FILE_NAME_PATTERN % m
            lines_now = [] if not file_name in files_now else sorted(self.file_utils.read_lines(os.sep.join([dir_now, file_name])))
            lines_before = [] if not file_name in files_before else sorted(self.file_utils.read_lines(os.sep.join([dir_before, file_name])))

            diff = self.diff_lines(lines_before, lines_now)
            yield MetricsDiffResult(m, list(diff))


class EhMetricsInvestigate(JmakeModule):

    def __init__(self, fs:FileUtils=FileUtils(), url: UrlUtils=UrlUtils(), tar_utils = TarUtils(), bamboo_utils: BambooUtils = BambooUtils()):
        super().__init__()
        self.command = 'eh-metrics-investigate'
        self.prevent_post_diagnostics = True
        self.description = wrap_text('Finds the reason for failing engineering health metrics. Should work '
                                     'automatically, comparing the latest result of ./jmake eh-metrics with the '
                                     'latest tagged CI build. You should run this command immediately after failing '
                                     './jmake eh-metrics, and with no parameters. The parameters available to this '
                                     'command are mostly for debug or tricky usages.')
        self.fs = fs
        self.url = url
        self.tar_utils = tar_utils
        self.bamboo_utils = bamboo_utils

    def get_hits_for_build(self, log: Logger, target_dir: str, build_number: str):
        job, bn = self.bamboo_utils.decode_build_number(build_number)
        url = '%s/browse/%s/artifact/%s/eh-metrics-hits.tgz/eh-metrics-hits.tgz' % (Jbac.url, bn, job)
        local_file = os.sep.join([target_dir, 'eh-metrics-hits-%s.tgz' % bn])
        if not self.fs.file_exists(local_file):
            log.info('Missing metrics hits for build %s (file not exists: %s)' % (build_number, local_file))
            log.info('You can download them from %s' % url)
            log.info('Or, I can do that for you (you may CTRL-C out of here at any time and do this manually).')
            auth = JbacAuthentication.get()
            if not self.url.download(url, auth.login, auth.password, local_file):
                log.error('Failed to download the detailed metrics file from JBAC')
                return Callable.failure
        else:
            log.debug('Metrics hits for build %s exists, cool' % build_number)

        # unpack to temporary directory
        temp_dir = os.sep.join([target_dir, 'eh-metrics-hits-temp'])
        if self.fs.dir_exists(temp_dir):
            log.debug('Removing temp dir: %s' % temp_dir)
            self.fs.remove_dir(temp_dir)

        log.debug('unpacking %s to %s...' % (local_file, temp_dir))
        try:
            self.tar_utils.unpack_file(local_file, temp_dir)
        except Exception as e:
            log.error('Failed to unpack tar file %s. Error: %s' % (local_file, e))
            return Callable.failure, None

        # move to proper directory
        commit_file = os.sep.join([temp_dir, '.commit'])
        if not self.fs.file_exists(commit_file):
            log.error('Commit file does not exists! Is the metrics hits package corrupted? '
                      'Expected file not found: %s' % commit_file)
        commit_hash = self.fs.read_lines(commit_file)[0]
        hits_dir = os.sep.join([target_dir, 'eh-metrics-hits-%s-%s' % (commit_hash, bn)])
        if self.fs.dir_exists(hits_dir):
            log.debug('Removing destination dir: %s' % hits_dir)
            self.fs.remove_dir(hits_dir)
        self.fs.rename(temp_dir, hits_dir)
        return Callable.success, hits_dir

    def perform_diff(self, log, hits_dir_current, hits_dir_before, metrics_to_compare):
        differ = MetricsDiff(self.fs)

        for diff in differ.diff_metrics(log, hits_dir_current, hits_dir_before, metrics_to_compare):
            if len(diff.diff) == 0:
                log.info('No differences found for metric %s' % diff.metric)
            else:
                log.info('Differences for metric %s:' % diff.metric)
                for line in diff.diff:
                    log.info(line)
                log.info("~~ END OF DIFF")

    def __call__(self, args, executor: CommandExecutor):
        target_dir = self.fs.existing_dir(os.sep.join(['.', 'jmake_src', 'target']))

        def investigate_builds(log):
            builds_to_compare = None if not args.compare_builds else args.compare_builds.split(',')
            if builds_to_compare is not None and len(builds_to_compare) != 2:
                log.error('Argument compare_builds should be in format: BN-FIRST,BN-SECOND')
                log.error('Found: %s' % args.compare_builds)
                return Callable.failure

            ec, hits_dir_before = self.get_hits_for_build(log, target_dir, builds_to_compare[0])
            if ec != Callable.success:
                return ec

            ec, hits_dir_current = self.get_hits_for_build(log, target_dir, builds_to_compare[1])
            if ec != Callable.success:
                return ec

            metrics_to_compare = None if args.metrics is None else args.metrics.split(',')
            self.perform_diff(log, hits_dir_current, hits_dir_before, metrics_to_compare)

            return Callable.success

        def investigate_local(log):
            failed_metrics_file = os.sep.join(['target', '.jmake.eh-metrics.failed-metrics.txt'])

            if not self.fs.file_exists(failed_metrics_file):
                log.warn('The file %s doesn\'t exists. Did you run eh-metrics? Has it failed?' % failed_metrics_file)

            if (not any([args.metrics, args.build_number, args.compare_builds])) and self.fs.file_exists(failed_metrics_file):
                commit_hash_unused, metrics_string, build_number_before = self.fs.read_lines(failed_metrics_file)[0].split(':')
                metrics_to_compare = metrics_string.split(',')
            else:
                metrics_to_compare = None if args.metrics is None else args.metrics.split(',')
                build_number_before = args.build_number

            if build_number_before is None:
                log.error('I don\'t know the build number to compare with, sorry :(. '
                          'Did you run eh-metrics? Has it failed? You can always give me a build '
                          'number using --build-number. But this message is unlikely to appear.')
                return Callable.failure

            hits_dir_current = os.sep.join([target_dir, 'eh-metrics-hits'])
            ec, hits_dir_before = self.get_hits_for_build(log, target_dir, build_number_before)
            if ec != Callable.success:
                return ec

            if not self.fs.dir_exists(hits_dir_current):
                log.error('Could not find current eh-metrics hits, did you run ./jmake eh-metrics?')
                return Callable.failure

            self.perform_diff(log, hits_dir_current, hits_dir_before, metrics_to_compare)
            return Callable.success

        if args.compare_builds is not None:
            executor.append(investigate_builds)
        else:
            executor.append(investigate_local)

    def define_parser(self, parser):
        parser.formatter_class = argparse.RawDescriptionHelpFormatter
        parser.add_argument('--metrics', dest='metrics', type=str,
            help='Comma separated list of metrics to investigate (you should not need to set this).')
        parser.add_argument('--build-number', dest='build_number', type=str,
            help='Build number for the correct stats (you should not need to set this)')
        parser.add_argument('--compare-builds', dest='compare_builds', type=str,
            help='Comma separated build numbers to compare (use this argument if you want compare two builds '
                 'instead of comparing current results with last successful build)')

        self.sample_usage(parser, 'Compare failing local with last successful remote',
                          ['./jmake eh-metrics && ./jmake eh-metrics-investigate'])
        self.sample_usage(parser, 'Compare two local results',
                          ['# Checkout old version and generate metrics',
                           'git checkout <commit1> && ./jmake eh-metrics',
                           '# Then pack them',
                           '(cd jmake_src/target/eh-metrics-hits && tar -cf ../eh-metrics-hits-OLD.tgz .)',
                           '# Now checkout new version and generate metrics',
                           'git checkout <commit2> && ./jmake eh-metrics',
                           '# And compare them',
                           './jmake eh-metrics-investigate --build-number OLD'])
        self.sample_usage(parser, 'Compare two remote results',
                          ['./jmake eh-metrics-investigate --compare-builds MASTERTWO-EM-JOB1-64,MASTERTWO-EM-JOB1-65'])
