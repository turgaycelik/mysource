from CommandExecutor import Callable
from utils.FileUtils import FileUtils


class LegacyOdSvnSetup(Callable):

    def __call__(self, logger, file_utils:FileUtils=FileUtils()):

        svndir = self.args.layout.studio_svn_dir()
        svnlink = self.args.layout.studio_svn_link()

        if not file_utils.file_exists(svnlink):
            # create symlink to svndir
            logger.info("Creating symlink: %s -> %s" % (svnlink, svndir))
            file_utils.symlink(svndir, svnlink)

        return Callable.success