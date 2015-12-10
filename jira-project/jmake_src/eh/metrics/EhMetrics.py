import os
from xml.etree.ElementTree import ParseError

from CommandExecutor import CommandExecutor, Callable
from Logger import Logger
from eh.metrics import STATS_REF_NAME
from eh.metrics.MetricsCollector import MetricsCollector
from eh.metrics.MetricsProcessor import MetricsProcessor
from eh.metrics.MetricsWriter import JsonWriter
from eh.metrics.ModulesDescription import JIRADirectoryScanModulesDescription, BundledPluginsModulesDescription, JIRATestsModulesDescription
from maven.PomParser import PomParser
from module import JmakeModule
from utils.DataBean import DataBean
from utils.FileUtils import FileUtils
from utils.GitUtils import GitUtils

class EhMetrics (JmakeModule):
    """
    Gather metrics values from the code.
    """

    def __init__(self, git:GitUtils = GitUtils(), fs:FileUtils = FileUtils(), metrics_processor = MetricsProcessor(),
                json_writer = JsonWriter()):
        super().__init__()
        self.command = 'eh-metrics'
        self.prevent_post_diagnostics = True
        self.description = 'Gathers engineering health metrics from the JIRA code. If any violations occur, you will be ' \
                           'able to add exclusion information interactively. You should pull all latest changes ' \
                           'before you run this.'
        self.git = git
        self.fs = fs
        self.metrics_processor = metrics_processor
        self.json_writer = json_writer

    def __call__(self, args, executor: CommandExecutor):

        def check_remotes(log):
            if len(self.git.get_remotes()) == 0:
                self.set_remote(log)
            return Callable.success
        executor.append(check_remotes)

        if not args.fast:
            executor.append(lambda log : Callable.success if self.git.fetch_notes('*') == 0 else
            log.error('FATAL: git: Failure to fetch notes from origin.') or Callable.do_not_proceed)

        if args.branch:
            def branch_check(logger):
                current_branch = self.git.current_branch()
                if not current_branch == args.branch:
                    logger.error('Branch check failed. You seem to be on "%s"; switch to "%s" first!' % (current_branch, args.branch))
                    return Callable.do_not_proceed
                else:
                    return Callable.success
            executor.append(branch_check)

        def check_workspace(log: Logger):
            if args.note or not args.non_interactive:
                if not self.git.is_clean_workspace():
                    if args.note:
                        log.error('I cannot write notes with local changes. Commit your work first, so that notes can '
                                  'be attached to your commit.')
                        return Callable.do_not_proceed
                    else:
                        log.warn('You have uncommitted changes - if engineering health metrics are increased, you will '
                                 'not be able to add an exclusion note for the build.')
            return Callable.success
        executor.append(check_workspace)

        def clean_logs(log: Logger):
            if self.fs.dir_exists(MetricsCollector.log_directory):
                log.debug('Removing directory: %s' % MetricsCollector.log_directory)
                self.fs.remove_dir(MetricsCollector.log_directory)
            return Callable.success
        executor.append(clean_logs)

        def record_commit(log: Logger):
            self.fs.write_lines(os.sep.join([self.fs.existing_dir(MetricsCollector.log_directory),'.commit']), [self.git.current_commit()])
            return Callable.success
        executor.append(record_commit)

        metrics = DataBean()

        modules_descriptions = [JIRADirectoryScanModulesDescription(args.fast, file_utils=self.fs),
                                BundledPluginsModulesDescription(args.fast),
                                JIRATestsModulesDescription(args.fast)]

        executor.append(self.metrics_processor.process_metrics(args, modules_descriptions, metrics))
        executor.append(self.metrics_processor.generate_report(metrics, self.fs, self.git))
        executor.append(self.metrics_processor.check_values(args, metrics, self.git, self.fs))

        if args.note:
            executor.append(lambda log: self.git.set_user('jmake stats runner', 'jmake@atlassian.com'))
            executor.append(lambda log: self.git.put_notes(self.json_writer.as_str(metrics), STATS_REF_NAME, 'HEAD', True))
            executor.append(lambda log: self.git.push_notes(STATS_REF_NAME))

    def define_parser(self, parser):
        parser.add_argument('-v', '--verbose', dest='verbose', action='store_true',
            help='Each violation will be logged.')
        parser.add_argument('-b', '--branch-restriction', dest='branch', type=str, default=None,
            help='Metrics will be calculated only when on the specified branch (needed for CI).')
        parser.add_argument('--build-number', dest='buildno', type=str, default=None,
            help='Build number to be added to the metrics (needed for CI).')
        parser.add_argument('--fast', dest='fast', action='store_true',
            help='Avoids long-running metrics calculation (good for testing).')
        parser.add_argument('--write-note-on-head-ci-only-DO-NOT-USE-LOCALLY', dest='note', action='store_true',
            help='Writes a note onto HEAD with the metrics and pushes it (exclusively for CI).')
        parser.add_argument('--matching', dest='matching', type=str, default=None,
            help='Calculates only metrics which key contains given substring.')
        parser.add_argument('--non-interactive', dest='non_interactive', action='store_true',
            help='Avoids interactive flows, ie. confirming metrics increase (needed for CI)')

        self.sample_usage(parser, 'Run full check of eh-metrics.', ['./jmake eh-metrics'])
        self.sample_usage(parser, 'Make exclusion note only on metric x.y.z',
                          ['./jmake eh-metrics --matching x.y.z',
                           '# follow instructions in interactive mode'])
        self.sample_usage(parser, 'Show each violation of metric x.y.z',
                          ['./jmake eh-metrics --matching x.y.z --verbose'])
        self.sample_usage(parser, 'Show a diff of two revisions (aka "How did I break it?")',
                          ['./jmake eh-metrics-investigate --help'])

    def set_remote(self, log: Logger, pom_parser = None):

        if pom_parser is None:
            try:
                pom_parser = PomParser(os.sep.join(['.', 'pom.xml']))
            except ParseError as e:
                log.error('Parse error in main project pom: ' + str(e))
                return Callable.do_not_proceed
            except OSError as e:
                log.error('Could not read main project pom: ' + str(e))
                return Callable.do_not_proceed

        developerConnection = pom_parser.get_developer_connection()

        git_scm_type = 'scm:git:'
        if not developerConnection.startswith(git_scm_type):
            # worried that it's not?
            raise Exception('I can\'t believe this is happening.')

        self.git.set_remote('origin', developerConnection[len(git_scm_type):])
