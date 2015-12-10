from functools import wraps, reduce
import os
import re

from CommandExecutor import SystemCallable
from utils.FileUtils import FileUtils
from utils.ProcessUtils import ProcessUtils
from utils.WorkspaceUtils import MavenCallableWorkspaceDelegator


#add prefix to each project when it's added
def transform_projects_for_parent_dir_execution(f):
    @wraps(f)
    def _transform_projects_for_parent_dir_execution(self, project, *args, **kwargs):
        return f(self, os.sep.join([self.project_prefix, project]) if self.project_prefix else project, *args, **kwargs)

    return _transform_projects_for_parent_dir_execution


def avoid_none_arg(f):
    @wraps(f)
    def _avoid_none_arg(*args, **kwargs):
        if len(args) > 1 and args[1] is None:
            # return self, first param:
            return args[0]
        else:
            return f(*args, **kwargs)

    return _avoid_none_arg


class MavenVersionInaccessibleException(Exception):
    pass


class MavenExecutionException(Exception):
    pass


class MavenContext:
    maven_ver_pattern = re.compile('\\d[\\.\\d]+(_\\d+)?')

    def __init__(self, process_utils):
        super().__init__()
        self.process_utils = process_utils
        self.maven_search_attempts = []

        mvn_home = os.environ.get(self.maven_env())
        mvn_alternative = os.environ.get('JMAKE_MVN_CMD')
        if mvn_alternative is not None:
            self.__set_maven_command(mvn_alternative, 'Environment variable override [JMAKE_MVN_CMD]')
        elif mvn_home is not None and mvn_home:
            self.__set_maven_command(os.sep.join([mvn_home.rstrip(os.sep), 'bin', 'mvn']),
                                     'Maven home variable [%s] (not checked for maven version)' % self.maven_env(),
                                     True)
        else:
            self.maven_search_attempts.append( ('(none)', 'Maven home variable [%s] not set' % self.maven_env(), 'Skipped'))
            for alias in self.command_aliases():
                if not hasattr(self, 'mvn_command'):
                    self.__set_maven_command(alias, 'Trying an mvn command from PATH')

        if not hasattr(self, 'mvn_command'):
            raise MavenVersionInaccessibleException(self.__prepare_error_info())

    def __set_maven_command(self, mvn_command, attempt: str, do_not_match_version=False):
        #check, if it's the right maven:
        try:
            env = os.environ.copy()
            if 'M2_HOME' in env: del env['M2_HOME']
            self.maven_version_output = self.process_utils.check_output([mvn_command, '--version'], env=env).decode()
            maven_version = self.__parse_version_info(self.maven_version_output)
            if do_not_match_version or self.maven_version_matches(maven_version):
                self.maven_search_attempts.append((mvn_command, attempt, 'Successful'))
                self.mvn_command = mvn_command
            else:
                self.maven_search_attempts.append((mvn_command, attempt, 'Wrong maven version (%s)' % maven_version))
        except OSError:
            self.maven_search_attempts.append((mvn_command, attempt, 'Binary not found'))

    def before_execute(self):
        pass

    def after_execute(self):
        pass

    def maven_env(self):
        return None

    def maven_version_matches(self, version):
        return False

    def command(self, logger):
        command = getattr(self, 'mvn_command', None)
        if command is not None:
            for attempt in self.__attempts():
                logger.debug(attempt)
            logger.info('mvn_cmd: "%s", '
                        'mvn_ver: "%s", java_ver: "%s", java_home: "%s"' % (
                            self.mvn_command, self.__parse_version_info(self.maven_version_output),
                            self.__java_version(self.maven_version_output), self.__java_path(self.maven_version_output)))

            return command
        else:
            raise MavenVersionInaccessibleException(self.__prepare_error_info())

    def __parse_version_info(self, maven_version_output):
        line = filter(lambda line: 'Maven' in line, maven_version_output.split('\n')).__next__()
        return MavenContext.maven_ver_pattern.search(line).group()

    def __java_version(self, maven_version_output):
        line = filter(lambda line: 'Java version' in line, maven_version_output.split('\n')).__next__()
        return MavenContext.maven_ver_pattern.search(line).group()

    def __java_path(self, maven_version_output):
        java_home_str = 'Java home'
        line = filter(lambda line: java_home_str in line, maven_version_output.split('\n')).__next__()
        return line[len(java_home_str) + 1:].strip()

    def command_aliases(self):
        return []

    def __prepare_error_info(self):
        return 'Could not find Maven executable with attempts: \n' + '\n'.join(self.__attempts())

    def __attempts(self):
        for attempt in ('* ' + ': '.join(tpl) for tpl in self.maven_search_attempts):
            yield attempt


class Maven2(MavenContext):
    def __init__(self, process_utils):
        super().__init__(process_utils)

    def maven_env(self):
        return 'M2_HOME'

    def maven_version_matches(self, version):
        return version.startswith('2')

    def command_aliases(self):
        return ['atlas-mvn', 'mvn2', 'mvn']


class Maven3(MavenContext):
    def __init__(self, process_utils):
        super().__init__(process_utils)

    def maven_env(self):
        return 'M3_HOME'

    def maven_version_matches(self, version):
        return version.startswith('3')

    def before_execute(self):
        # Remove M2_HOME for maven3:
        m2_home = os.environ.get('M2_HOME', None)
        if m2_home is not None:
            self.cached_m2_home = m2_home
            del os.environ['M2_HOME']

    def after_execute(self):
        # Restore M2_HOME
        m2_home = getattr(self, 'cached_m2_home', None)
        if m2_home is not None:
            os.environ['M2_HOME'] = m2_home

    def command_aliases(self):
        return ['mvn3', 'mvn']


class MavenCallable(SystemCallable):
    @MavenCallableWorkspaceDelegator.change_params_for_workspace
    def __init__(self, args=object, path=None, process_utils=ProcessUtils(), file_utils=FileUtils()):
        super().__init__(args, cwd=path)

        self.maven_version = None
        self.projects = []
        self.profiles = []
        self.phases = []
        self.properties = dict()
        self.options = []
        self.parallel = False
        self.process_utils = process_utils
        self.project_prefix = ""
        options_from_args = {'mvn_offline': '-o',
                             'mvn_update': '-U',
                             'mvn_debug': '-X',
                             'mvn_errors': '-e'}

        for key in options_from_args.keys():
            if getattr(args, key, False):
                self.option(options_from_args[key])
        if getattr(args, 'mvn_clean', False):
            self.phase('clean')

    def __call__(self, logger):
        self.command(self.__evaluate_command(logger))
        if self.maven_version is not None:
            self.maven_version.before_execute()

        self.__setup_maven_opts(logger)
        returncode = super().__call__(logger)

        if self.maven_version is not None:
            self.maven_version.after_execute()

        return returncode

    def __setup_maven_opts(self, logger):
        env = os.environ.copy()
        default_mvn_options = JavaOptionParser('-Xms256m -Xmx1024m -XX:MaxPermSize=512m', logger, 'maven')
        if 'MAVEN_OPTS' in env: default_mvn_options.merge(env['MAVEN_OPTS'])
        env['MAVEN_OPTS'] = default_mvn_options.get_options()
        logger.debug('Using MAVEN_OPTS=%s' % env['MAVEN_OPTS'])
        self.env = env

    @staticmethod
    def add_maven_switches(parser):
        if not getattr(parser, 'with_mvn_common_options', False):
            parser.add_argument('-u', '--update',
                                help='maven tasks ran by this will update snapshots and released artifacts.',
                                action='store_true',
                                dest='mvn_update')
            parser.add_argument('-o', '--offline', help='maven tasks ran by this will work offline.',
                                action='store_true', dest='mvn_offline')
            parser.add_argument('-X', '--maven-debug', help='maven tasks ran by this will generate debug info.',
                                action='store_true', dest='mvn_debug')
            parser.add_argument('-e', '--errors', help='maven tasks ran by this will generate error messages.',
                                action='store_true', dest='mvn_errors')
            parser.with_mvn_common_options = True

    def __evaluate_command(self, logger):
        if self.maven_version is None:
            try:
                self.maven_version = Maven3(self.process_utils)
            except MavenVersionInaccessibleException as e:
                logger.debug('Cannot locate maven 3: \n %s \nFalling back to maven 2.' % str(e))
                try:
                    self.maven_version = Maven2(self.process_utils)
                except MavenVersionInaccessibleException as e:
                    logger.error('Cannot locate maven 2: \n' + str(e))

        if self.maven_version is None:
            logger.error('Maven is not accessible. Please set your M2_HOME or M3_HOME variables, \n'
                         'or set your maven executable by JMAKE_MVN_CMD or set your PATH to include mvn!')
            raise MavenVersionInaccessibleException()

        return ' '.join(filter(lambda e: e != '', [self.maven_version.command(logger),
                                                   ' '.join(self.phases),
                                                   '' if len(self.projects) == 0 else '-pl ' + ','.join(self.projects),
                                                   ' '.join(self.options),
                                                   '' if len(self.profiles) == 0 else '-P' + ','.join(self.profiles),
                                                   ' '.join(('-D%s' % k if v is None else '-D%s=%s' % (k, v) for k, v in
                                                             self.properties.items())),
                                                   '-T1.5C' if self.parallel and isinstance(self.maven_version,
                                                                                            Maven3) else '']
        ))

    @avoid_none_arg
    def profile(self, profile):
        self.profiles.append(profile)
        return self

    @avoid_none_arg
    def phase(self, phase):
        self.phases.append(phase)
        return self

    @avoid_none_arg
    def property(self, key, value=None):
        self.properties[key] = value
        return self

    @avoid_none_arg
    def option(self, option):
        self.options.append(option)
        return self

    @transform_projects_for_parent_dir_execution
    @avoid_none_arg
    def project(self, project):
        self.projects.append(project)
        return self

    def can_run_in_parallel(self):
        self.parallel = True
        return self

    def skip_tests(self):
        self.property('skipTests', 'true')
        return self

    def require_mvn2(self):
        if self.maven_version is None:
            self.maven_version = Maven2(self.process_utils)
        else:
            if not isinstance(self.maven_version, Maven2):
                raise Exception('Maven version already constrained to: ' + self.maven_version.__class__.__name__)
        return self

    def require_mvn3(self):
        if self.maven_version is None:
            self.maven_version = Maven3(self.process_utils)
        else:
            if not isinstance(self.maven_version, Maven3):
                raise Exception('Maven version already constrained to: ' + self.maven_version.__class__.__name__)
        return self

    def mvn2_inaccessible_msg_callable(self, taskname, exception=None):
        return self.__mvn_inaccessible_message_callable(taskname, 'Maven2', 'M2_HOME', exception)

    def mvn3_inaccessible_msg_callable(self, taskname, exception=None):
        return self.__mvn_inaccessible_message_callable(taskname, 'Maven3', 'M3_HOME', exception)

    def __mvn_inaccessible_message_callable(self, taskname, mvn, env, exception):
        return lambda logger: logger.error(
            '{} is not accessible, but is required for "{}". Set your {} variable, or put mvn on your PATH! {}'
                .format(mvn, taskname, env, '' if exception is None else '\n' + str(exception))) or 0


class JavaOptionParser:
    modifiers = {'': 1,
                 'k': 1024,
                 'K': 1024,
                 'm': 1024 * 1024,
                 'M': 1024 * 1024,
                 'g': 1024 * 1024 * 1024,
                 'G': 1024 * 1024 * 1024,
                 't': 1024 * 1024 * 1024 * 1024,
                 'T': 1024 * 1024 * 1024 * 1024}
    number_finder = re.compile(r'^(-.+?)([0-9]+)([kKmMgGtT]?)$')

    def __init__(self, defaults:str, logger, name='java'):
        self.options = self.__parse_options(defaults)
        self.logger = logger
        self.name = name

    def __merge_options(self, merge_opts):
        def merge_func(merge_result, option):
            key = option[0]
            value = option[1]
            if key in merge_result:
                act_value = merge_result[key]
                if act_value[1] > value[1]:
                    self.logger.warn(
                        "Overriding default %s option '%s' to lower value '%s' " % (self.name, act_value[0], value[0]))
                elif act_value[1] < value[1]:
                    self.logger.debug(
                        "Replacing default %s option '%s' with value '%s'" % (self.name, act_value[0], value[0]))

            merge_result[key] = value
            return merge_result

        return reduce(merge_func, merge_opts.items(), dict(self.options))

    def merge(self, user_options):
        self.options = self.__merge_options(self.__parse_options(user_options))

    def get_options(self):
        return ' '.join(sorted(option[0] for option in self.options.values())).strip()

    def __parse_options(self, defaults):
        return dict(map(self.__parse_option, defaults.strip().split(' ') if not defaults is None else []))

    def __parse_option(self, option_value):
        search = JavaOptionParser.number_finder.search(option_value)
        if search is not None:
            return search.group(1), (option_value, int(search.group(2)) * JavaOptionParser.modifiers[search.group(3)])
        else:
            return option_value, (option_value, 1)
