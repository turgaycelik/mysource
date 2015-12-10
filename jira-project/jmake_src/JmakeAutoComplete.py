from functools import reduce
import os
import sys
from CommandExecutor import Callable
from module import JmakeModule
from utils.ParserUtils import subparser_choices


class AutoComplete(JmakeModule):
    """
    Possible improvements:
        1. (DONE)
            optional long and short forms (like -i and --install) will be auto-completed separately (one will not
            exclude the other)
        2. (DONE)
            'choices' are not mutually exclusive (once you selected 'alacarte' for jmake ondemand run, 'jira-only' will
            still be suggested)
        3. arguments with data (like --jira-port JIRA_PORT) is unaware of the fact that syntax requires a compulsory
            argument, neither that it might have a default.
        4. autocomplete is not aware of positional arguments sequence other than sub-parsers.
    """

    def __init__(self, parser):
        super().__init__()
        self.prevent_post_commands = True
        self.prevent_console_reset = True
        self.prevent_post_diagnostics = True
        self.check_branch = False
        self.main_parser = parser
        self.command = 'auto-complete'
        self.description = 'Used by the jmake autocomplete feature. Unfortunately it is bash that sorts the options, so '\
                           'jmake cannot influence that. You need to install it first (run with -i). This part of jmake '\
                           'is tricky, it WILL work for bash, but you might find some unexpected or unclear behavior.'

    def __call__(self, args, executor):

        # since I do not want any of the options to be caught by the parser, I need to consider them manually:
        if len(args.input) == 1:
            if args.input[0] == '-h' or args.input[0] == '--help':
                executor.append(print_autocomplete_help(self.main_parser))
                return
            elif args.input[0] == '-i' or args.input[0] == '--install':
                executor.append(install_autocomplete)
                return


        # kill logging (no warnings, diagnostics, etc., or bash will munch on it):
        def kill_logging(logger):
            logger.set_none()
            return Callable.success
        executor.append(kill_logging)

        # now, determine auto-complete:
        def determine_autocomplete(logger):
            print(' '.join(self.__determine_autocomplete(args, logger)))
            return Callable.success
        executor.append(determine_autocomplete)

    def __determine_autocomplete(self, args, logger):
        # bug in python: it will cut out '--' from args:
        if sys.argv[len(sys.argv) - 1] == '--':
            args.input.append('--')

        if len(args.input) > 0: args.input[0] = 'jmake'

        parser = self.main_parser
        free_args = args.input[1:]

        while len(free_args):
            command = free_args[0]
            subparsers = subparser_choices(parser)
            if subparsers and command in subparsers.keys():
                parser = subparsers[command]
                free_args = free_args[1:]
            else:
                break

        result = map_options(parser)
        result.append(['clean'])
        try:
            contributed = parser.autocomplete_contributor()
            if contributed:
                result.append(contributed)
        except AttributeError:
            pass

        flattened_result = (item for group in result for item in group)
        free_size = len(free_args)
        if free_size > 0:
            last_el = free_args[free_size - 1]
            partial_match = last_el if last_el not in flattened_result else None
            filtered_result = (item for group in result if not [x for x in group if x in free_args] for item in group)

            if partial_match is None:
                result = filtered_result
            else:
                result = (o for o in filtered_result if o.startswith(partial_match))
        else:
            result = flattened_result

        return result

    def define_parser(self, parser):
        parser.add_argument('input', nargs='*', help='user input')
        parser.add_argument('-i', '--install', action='store_true', help='install jmake autocomplete in your system '
                                                                         '(requires sudo).')
        # catch all "words" as positionals, not optionals:
        parser.prefix_chars = ''
        parser.epilog = ''


def map_options(parser):
    options = []

    for action in parser._actions:
        if isinstance(action.choices, dict) and action.choices:
            options.append([x for x in sorted(action.choices.keys())])
        elif isinstance(action.choices, list) and action.choices:
            options.append(action.choices)
        if action.option_strings is not None and action.option_strings:
            options.append(action.option_strings)

    return options

def get_autocomplete_file(logger):
    paths = ['/etc/bash_completion.d', # Linux
             '/usr/local/etc/bash_completion.d', # Mac brew
             '/opt/local/etc/bash_completion.d',       # MacPorts  for bash 3
             '/opt/local/share/bash-completion/completions']  # MacPorts for bash 4

    # find existing file from the list:
    bash_completion_directory = reduce(lambda result, path: path if result is None and os.path.isdir(path) else result,
        paths, None)

    if not bash_completion_directory:
        logger.error('You need "bash-completion" package installed. It is expected that one of these directories exist on your system:')
        for p in paths:
            logger.error('  ' + p)
        logger.error('Autocomplete installation cannot proceed without "bash-completion".')
        logger.error('Either raise a bug against jmake with your setup detailed, or link your bash-completion location at one of the specified paths.')
        return None

    return '%s/com.atlassian.jmake' % bash_completion_directory

def install_autocomplete(logger):

    autocomplete_file = get_autocomplete_file(logger)
    if autocomplete_file:
        logger.info('Installing autocomplete...')

        if os.path.isfile(autocomplete_file):
            logger.info(
                'The autocomplete for jmake seems to be installed. If you want it reinstalled, delete ' + autocomplete_file)
            return Callable.success

        try:
            with open(autocomplete_file, 'w') as f:
                f.write(
                    '_jmake() {\n\tCOMPREPLY=(`./jmake auto-complete ${COMP_WORDS[@]}`)\n} &&\ncomplete -F _jmake ./jmake\n')
                logger.debug('Autocomplete installed at: ' + autocomplete_file)
                logger.info(
                    'Autocomplete has been installed. It will be autoloaded, but it will not work in the current shell.')
                logger.info('To activate it now, source it yourself: $ . ' + autocomplete_file)

        except IOError:
            logger.error("Could not write file: '%s'. Try with sudo." % autocomplete_file)
    return Callable.success


def print_autocomplete_help(parser):
    def print_autocomplete_help_closure(logger):
        subparser_choices(parser)['auto-complete'].print_help()
        return Callable.success
    return print_autocomplete_help_closure
