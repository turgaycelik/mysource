from itertools import islice
from json import JSONDecoder, JSONEncoder
import os
import re
from urllib.request import pathname2url
from CommandExecutor import Callable
from Logger import Logger
from eh.metrics import STATS_REF_NAME, STATS_EXCLUSION_REF_NAME, FALLING, RISING, NEUTRAL
from eh.metrics.MetricsWriter import JsonWriter
from eh.metrics.SurefireSuiteLogger import SurefireSuiteLoggerFactory
from utils.FileUtils import FileUtils
from utils.GitUtils import GitUtils


class MetricsProcessor(object):

    def __init__(self, surefire_suite_logger_factory: SurefireSuiteLoggerFactory = SurefireSuiteLoggerFactory()):
        super().__init__()
        self.surefire_suite_logger_factory = surefire_suite_logger_factory

    def process_metrics(self, args, modules_descriptions: list, bean):

        def process_metrics_closure(log: Logger):
            bean.metrics = {}
            for modules_description in modules_descriptions:

                collectors = modules_description.get_collectors()
                if args.matching:
                    collectors = [collector for collector in collectors if re.search(args.matching,collector.key)]

                if len(collectors) == 0:
                    log.info("All collectors from module %s filtered out: skipping" % modules_description.__class__.__name__)
                    continue

                modules_description.prepare_environment(log)

                # configure collectors:
                for collector in collectors:
                    collector.configure(log = log, verbose = args.verbose)

                for module in modules_description.measured_modules():
                    log.debug('Scanning: ' + str(module))
                    for collector in collectors:
                        collector.pre_files_scan(module)

                    for file in modules_description.files(module):

                        readers = [ collector for collector in collectors if collector.wants_file(file) ]

                        if readers:
                            with modules_description.open_file(file) as f:
                                line = f.readline()
                                while line and readers:
                                    readers = [r for r in readers if r.on_read_line(line)]
                                    line = f.readline()

                    for collector in collectors:
                        collector.post_files_scan(module)

                for collector in collectors:
                    for k, v in collector.get_values().items():
                        # Note: if a collector is calculating metrics throughout different modules,
                        # this will override the previous results of whatever module it was inspecting,
                        # and if the collector accumulates the numbers, eventually the final number
                        # will be the result. This must be one instance of thw collector across all modules.
                        bean.metrics['jira.stats.' + k] = v
            log.debug('Done scanning modules.')

            if args.buildno:
                bean.build_number = args.buildno

            log.info('Generated metrics:\n' + JsonWriter().as_str(bean, indent=4))

            return Callable.success

        return process_metrics_closure

    def generate_report(self, metrics, file_utils: FileUtils, git: GitUtils, json_writter: JsonWriter = JsonWriter()):
        def generate_report_closure(log: Logger):
            self.__generate_report_files({
                                             'points': list(self.__generate_data_points(log, metrics, git))
                                         }, file_utils, log, json_writter)

            return Callable.success

        return generate_report_closure

    def __generate_report_files(self, data, file_utils: FileUtils, log: Logger, json_writter: JsonWriter):
        report_output_dir = os.sep.join(["target", "eh-metrics-report"])
        if file_utils.file_exists(report_output_dir):
            file_utils.remove_dir(report_output_dir)

        report_skeleton = os.sep.join(["jmake_src", "eh", "metrics", "skeleton", "report"])
        file_utils.copy_tree(report_skeleton, report_output_dir)
        # Generate data file
        data_js_value = '(function() { var data = %s; executeReport(data); })();' % json_writter.as_str(data)
        # Save data to file
        data_js_file = os.sep.join([report_output_dir, "js", "data.js"])
        file_utils.write_lines(data_js_file, [data_js_value])
        # Display log info
        report_file_url = "file://%s/index.html" % pathname2url(file_utils.abs_path(report_output_dir))
        log.info("Report generated to %s, open in browser: %s" % (report_output_dir, report_file_url))

    def __generate_data_points(self, log: Logger, metrics, git: GitUtils):
        for x in list(self.__generate_data_points_for_last_commits(log, git))[-99:]:
            yield x
        yield self.__generate_data_point_for_commit(metrics.__dict__, git.get_commit_details(STATS_REF_NAME), log)

    def __generate_data_points_for_last_commits(self, log: Logger, git: GitUtils):
        last_data_points = {'metrics': {}}
        for commit in list(git.generate_annotated_commits_with_details(STATS_REF_NAME))[::-1]:
            decoded_stats = JSONDecoder().decode(commit['note'])
            current_data_points = self.__generate_data_point_for_commit(decoded_stats, commit, log)
            if not current_data_points['metrics'] == last_data_points['metrics']:
                last_data_points = current_data_points
                yield last_data_points

    def __generate_data_point_for_commit(self, point_data, commit_details, log: Logger):
        last_commit = dict(commit_details)
        if 'note' in last_commit:
            del last_commit['note']

        point_data['commits'] = [last_commit]
        point_data['date'] = commit_details['date']  # date of last commit, as timestamp
        point_data['build_number'] = point_data.get('build_number', 'current')
        log.trace('stats for commit %s after changes: %s' % (commit_details['hash'], str(point_data) ))
        return point_data

    def check_values(self, args, metrics, git: GitUtils, fs: FileUtils):

        def check_values_closure(logger: Logger):
            metrics_in_error = {}

            logger.debug('Comparing results to previous build...')
            test_log = self.surefire_suite_logger_factory.new_logger("ehMetrics")

            # get the last commit annotated with stats generation:
            commit = git.latest_annotated_commit_with_details(STATS_REF_NAME)
            if commit is None:
                logger.warn('No previous metrics found.')

                # log all as success
                for key, result in ( (k, metrics.metrics[k]) for k in sorted(metrics.metrics.keys())):
                    test_log.success(key, 0, 'EHMetrics')

                test_log.save()

                return Callable.success

            decoded_note = JSONDecoder().decode(commit['note'])
            decoded_stats = decoded_note['metrics']
            logger.info('Comparing against %s by %s.' % (commit['hash'], commit['commiter']))

            # get all exclusions:
            for exclusion in git.generate_annotated_commits_with_details(STATS_EXCLUSION_REF_NAME, commit_range=commit['hash'] + '..HEAD'):
                decoded_exclusion = JSONDecoder().decode(exclusion['note'])
                logger.info('Considering exclusion by %s: %s'
                            % (decoded_exclusion['committer'], decoded_exclusion['reason']))
                for key, value in decoded_exclusion['exclusion'].items():
                    if key in decoded_stats and decoded_stats[key]['value'] < value:
                        logger.debug('Overriding %s to %s' % (key, str(value)))
                        decoded_stats[key]['value'] = value

            for key, result in metrics.metrics.items():
                failed = False
                if key not in decoded_stats:
                    logger.warn('metric %s (%s) does not have a previous count.' % (key, result.description))
                else:
                    oldvalue = decoded_stats[key]['value']
                    newvalue = result.value
                    if newvalue > oldvalue and result.direction == FALLING:
                        if result.checked:
                            failed = True
                            error_message = 'metric %s (%s) increased from %s to %s.' % (
                                key, result.description, decoded_stats[key]['value'], result.value)
                            test_log.failed(key, 0, 'EHMetrics', error_message)
                            logger.error(error_message)
                            metrics_in_error[key] = {'was': oldvalue, 'is': newvalue}
                        else:
                            logger.debug('unchecked falling metric %s (%s) increased from %s to %s.'
                                         % (key, result.description, decoded_stats[key]['value'], result.value))
                    elif oldvalue > newvalue and result.direction == RISING:
                        if result.checked:
                            failed = True
                            error_message = 'metric %s (%s) decreased from %s to %s.' % (
                                key, result.description, decoded_stats[key]['value'], result.value)
                            test_log.failed(key, 0, 'EHMetrics', error_message)
                            logger.error(error_message)
                            metrics_in_error[key] = {'was': oldvalue, 'is': newvalue}
                        else:
                            logger.debug('unchecked rising metric %s (%s) decreased from %s to %s.'
                                         % (key, result.description, decoded_stats[key]['value'], result.value))
                    elif oldvalue > newvalue and result.direction == FALLING:
                        if result.checked:
                            if newvalue == 0:
                                logger.info(' *** Achievement Unlocked! metric %s (%s) is clean! ***'
                                            % (key, result.description))
                            else:
                                logger.info('metric %s (%s) decreased by %s. Still %s to go.'
                                            % (key, result.description, decoded_stats[key]['value'] - result.value, result.value))
                        else:
                            logger.debug('unchecked falling metric %s (%s) decreased from %s to %s.'
                                         % (key, result.description, decoded_stats[key]['value'], result.value))

                    elif newvalue > oldvalue and result.direction == RISING:
                        if result.checked:
                                logger.info('metric %s (%s) increased by %s. Nice going!'
                                            % (key, result.description, decoded_stats[key]['value'] - result.value))
                        else:
                            logger.debug('unchecked rising metric %s (%s) increased from %s to %s.'
                                         % (key, result.description, decoded_stats[key]['value'], result.value))
                    elif newvalue != oldvalue and result.direction == NEUTRAL:
                        logger.debug('unchecked metric %s (%s) changed by %s.'
                                    % (key, result.description, result.value - decoded_stats[key]['value']))

                if not failed:
                    test_log.success(key, 0, 'EHMetrics')

            test_log.save()

            for key, result in decoded_stats.items():
                if key not in metrics.metrics and not args.matching:
                    logger.warn('missing count for %s (%s).' % (key, result['description']))

            if metrics_in_error:

                with fs.open(os.sep.join([fs.existing_dir(os.sep.join(['.', 'target'])), '.jmake.eh-metrics.failed-metrics.txt']), mode='w') as f:
                    f.write(commit['hash'])
                    f.write(':')
                    f.write(','.join((key[len('jira.stats.'):] for key in metrics_in_error.keys())))
                    f.write(':')
                    if 'build_number' in decoded_note:
                        f.write(decoded_note['build_number'])

                if args.non_interactive:
                    logger.error('Failing because of metrics increase.')
                    return Callable.do_not_proceed
                else:
                    if not git.is_clean_workspace():
                        logger.error('Failing because of metrics increase.')
                        logger.info('To find out what exactly went wrong, run ./jmake eh-metrics-investigate')
                        logger.error('I cannot make you accept the increase in eh-metrics count, because your '
                                     'workspace contains local changes. ')
                        logger.error('Please commit your work and re-run stats '
                                     'generation.')
                        logger.error('Or, perhaps reconsider your changes instead of committing an increase in '
                                     'eh-metrics count.')
                    else:
                        # attempt to get the information needed to build exclusion note:
                        return self.exclusion_note(metrics_in_error, git, logger)

            return Callable.success

        return check_values_closure

    def exclusion_note(self, metrics_in_error: dict, git: GitUtils, logger: Logger):
        logger.info('Please consult the above warnings and errors concerning discrepancies in your build compared to '
                    'latest build.')
        answer = input('Do you want to accept the violations (Yes/No)? [No]: ')
        if not answer.upper() == 'YES':
            logger.info('I am treating this as a No (you typed: "%s").' % answer)
            logger.error('Failing because of metrics increase.')
            return Callable.do_not_proceed

        reason = ''
        while not reason.strip():
            reason = input('Provide a nonempty commit message (reason) for the increase [""]: ')

        username = '%s <%s>' % (git.get_config_by_key('user.name'), git.get_config_by_key('user.email'))
        metrics = {}
        for key, value in metrics_in_error.items():
            metrics[key] = value['is']
        exclusion = {'reason': reason,
                     'committer': username,
                     'exclusion': metrics}

        note = JSONEncoder().encode(exclusion)

        if git.put_notes(note, STATS_EXCLUSION_REF_NAME, 'HEAD') != Callable.success:
            logger.error('Error encountered while setting note. Consult above messages if any.')
            return Callable.do_not_proceed

        if git.push_notes(STATS_EXCLUSION_REF_NAME) != Callable.success:
            logger.error('Error encountered while pushing notes. Consult above messages if any.')
            return Callable.do_not_proceed

        logger.info('The exclusion note has been pushed.')
        logger.warn('Remember to push your commit. DO NOT rebase (use merge instead) or the exclusion will be lost.')

        return Callable.success
