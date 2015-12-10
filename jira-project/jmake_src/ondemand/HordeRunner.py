import os

from CommandExecutor import Callable
from maven.Maven import MavenCallable
from ondemand.HordeStatusChecker import HordeStatusChecker
from utils.FileUtils import FileUtils
from utils.ProcessUtils import ProcessUtils


class HordeRunner(MavenCallable):
    def __init__(self, args=object, file_utils:FileUtils=FileUtils(), process_utils=ProcessUtils(),
            horde_status_checker=None, background=True):
        super().__init__(args, args.horde_layout.horde_home_dir(False), process_utils, file_utils)
        self.horde_layout = args.horde_layout
        if background:
            self.background()
            self.redirect_output(self.horde_layout.horde_log_file())
        self.file_utils = file_utils
        self.horde_status_checker = horde_status_checker if horde_status_checker is not None else HordeStatusChecker(
            self.horde_layout)

    def __call_super(self, logger):
        return super().__call__(logger)

    def __call__(self, logger):
        if self.horde_status_checker.is_running():
            logger.info(
                'Horde is already running at url "%s" not running another instance' % self.horde_layout.horde_status_url())
            return Callable.success
        else:
            logger.info('Horde is not running starting new instance')

        if self.args.mvn_clean:
            self.file_utils.remove_dir(self.horde_layout.horde_home_dir(False))

        jira_home = self.args.layout.jira_home()
        if not self.file_utils.dir_exists(self.horde_layout.horde_home_dir(False)):
            logger.info('Copying horde configuration from %s to %s' % (
                self.horde_layout.horde_skeleton_dir(), self.horde_layout.horde_home_dir(False)))
            self.file_utils.copy_tree(self.horde_layout.horde_skeleton_dir(), self.horde_layout.horde_home_dir(False))

            files_to_move_to_parent_directory = ['studio.license.decoded', 'dbconfig.xml']
            for file_to_move in files_to_move_to_parent_directory:
                self.file_utils.filter_file(os.sep.join([self.horde_layout.horde_home_dir(False), file_to_move]),
                                            os.sep.join([jira_home, file_to_move]),
                                            {'${jirahome}': jira_home})
        else:
            logger.info("%s exists - skipping skeleton setup " % self.horde_layout.horde_home_dir())
        self.property('jira.home', self.file_utils.abs_path(jira_home))
        self.phase('jetty:run-war')
        return self.__call_super(logger)
