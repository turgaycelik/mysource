import argparse
import os
import traceback

from CommandExecutor import ExecutionTimeCallable, Callable
from Diagnostics import Maven3Discovery, AutoCompleteDiscovery, BranchInconsistencyCheck, BranchInconsistencyMarker, GitBranchDiscovery
from Help import Help
from JmakeApiCheck import ApiCheck
from JmakeAutoComplete import AutoComplete
from JmakeCI import CI
from JmakeClean import Clean, CleanAll
from JmakeDebug import Debug
from JmakeClustered import Clustered
from JmakeFindbugs import Findbugs
from JmakeIdea import Idea
from JmakeInstall import Install
from JmakeManager import Manager
from JmakePostgresql import Postgresql
from JmakeMysql import Mysql
from JmakeQunit import Qunit
from JmakeRun import Run
from JmakeOndemand import OnDemand
from JmakeStandalone import Standalone
from JmakeUnitTest import UnitTest, JmakeUnitTest
from Logger import Logger, LOG
from Notifier import Notifier
from eh.metrics.EhMetrics import EhMetrics
from eh.metrics.EhMetricsInvestigate import EhMetricsInvestigate
from module import JmakeModule
from utils.ParserUtils import subparser_choices
from utils.ParserUtils import wrap_text


class MainModule(JmakeModule):
    def __init__(self, parser):
        super().__init__()
        self.parser = parser

    def get_submodules(self):
        return [UnitTest(), Debug(), Clean(), CleanAll(), Run(), OnDemand(), Standalone(), CI(), Findbugs(),
                ApiCheck(), Idea(), Qunit(), AutoComplete(self.parser), JmakeUnitTest(), Help(self.parser),
                EhMetrics(), EhMetricsInvestigate(), Install(), Clustered(), Postgresql(), Mysql()] + (
                [Manager()] if self.is_manager() else [])


class CommandDispatcher:
    def __init__(self, executor):
        super().__init__()
        self.executor = executor

    def dispatch_from_params(self, argv):
        main_help = 'jmake is a primary tool for JIRA devs.'

        parser = argparse.ArgumentParser(formatter_class=argparse.RawDescriptionHelpFormatter)
        self.__register_submodules(parser, MainModule(parser).get_submodules())
        self.__discover_help(main_help, parser)

        if argv[1:]:
            args = parser.parse_args(
                [arg for arg in argv[1:] if arg != 'clean' or argv[1] == arg or argv[1] == 'auto-complete'])

            args.mvn_clean = 'clean' in argv[2:]
            if args.func.check_branch and not 'JMAKE_DRY' in os.environ:
                git_branch = GitBranchDiscovery()
                self.executor.append(git_branch)
                if not args.mvn_clean:
                    self.executor.append(BranchInconsistencyCheck(git_branch))
                self.executor.append_post(BranchInconsistencyMarker(git_branch))

            try:
                args.func(args, self.executor)
            except Exception as e:
                self.executor.commands = [ lambda logger : logger.error('Exception occurred during preparing executor: %s' % str(e)) or Callable.success]
                self.executor.post_commands = []
                traceback.print_exc()
                return self.executor.execute()

            if args.log_level:
                try:
                    getattr(LOG, 'set_' + args.log_level)()
                except AttributeError:
                    LOG.error('Cannot set log level to %s' % args.log_level)
                    return Callable.failure

            if args.silent: args.func.prevent_post_commands = True
            if not args.func.prevent_post_commands:
                timer = ExecutionTimeCallable()
                self.executor.append_post(timer)
                if os.getenv('JMAKE_STFU') is None:
                    self.executor.append_post(Notifier(timer))
            if args.func.prevent_console_reset:
                self.executor.perform_console_reset = False
            if not args.func.prevent_post_diagnostics:
                self.__append_post_diagnostics()
        else:
            parser.print_help()
            self.__append_post_diagnostics()

        return self.executor.execute()

    def __register(self, module, subparsers):
        subParser = subparsers.add_parser(name=module.command, description=module.description)
        subParser.epilog = wrap_text(
            'NOTE: "clean" option can be added to each variant of jmake. It will make all '
            'maven runners add a clean phase additionally. You can decide which maven '
            'command to use by setting JMAKE_MVN_CMD in your environment')

        submodules = module.get_submodules()
        if submodules is not None:
            self.__register_submodules(subParser, submodules)
            format_usage = module.append_format_usage()
            if format_usage is not None:
                usage_text = subParser.format_usage() + "\n" + format_usage + "\n\nError: "
                subParser.format_usage = lambda : usage_text
        else:
            subParser.set_defaults(func=module)
            subParser.add_argument('-s', '--silent', help='turns off notifications', dest='silent', action='store_true')
            subParser.add_argument('--log-level', help='sets logging verbosity of jmake', action='store',
                                   dest='log_level', choices=Logger.get_log_names())
            module.define_parser(subParser)

    def __register_submodules(self, parser, submodules):
        subparsers = parser.add_subparsers()
        for module in submodules:
            self.__register(module, subparsers)

        # ****** Warning ! HACKS! ******
        # Between python 3.2 and 3.3 the argparse is different in the way it handles subparsers. The 3.2 considered them
        # as positionals and issued 'too few arguments' on the right subparser, and exited (cool!). 3.3 treats them
        # as optionals and just gets errors. We could just catch the exception and print help, but the info would not be
        # within the context of the correct subparser. So instead, for 3.3, it's hacked to be required and named.
        # see: http://bugs.python.org/issue16308
        subparsers.required = True
        subparsers.dest = 'sub-command'

    def __discover_help(self, main_help, parser):
        parser.description = main_help
        subparsers = subparser_choices(parser)
        for choice in sorted(subparsers.keys()):
            parser.description += '\n\n -> \x1b[%sm%s\x1b[0m\n%s' % (
                Logger.LEVELS_COLORS[Logger.L_DEBUG],
                choice, wrap_text(subparsers[choice].description))

    def __append_post_diagnostics(self):
        self.executor.append_post(Maven3Discovery(None))
        self.executor.append_post(AutoCompleteDiscovery())
