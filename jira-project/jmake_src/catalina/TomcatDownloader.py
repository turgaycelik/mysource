from CommandExecutor import Callable
from maven.Maven import MavenCallable
from utils.FileUtils import FileUtils

class TomcatDownloader(MavenCallable):
    def __init__(self, args=object, path=None, file_utils=FileUtils()):
        super().__init__(args, path)
        self.file_utils = file_utils


    def __call__(self, logger):
        layout = self.args.layout
        tomcat_base_dir = layout.tomcat_dir(False)

        if self.file_utils.file_exists(tomcat_base_dir):
            logger.debug('Tomcat directory ' + tomcat_base_dir + ' exists skipping download')
            return Callable.success
        else:
            logger.info('Tomcat does not exists downloading')
            tomcat_download_dir = layout.tomcat_download_dir()
            self.project('jira-ide-support')
            self.property('tomcat.dir', tomcat_download_dir)
            self.profile('download-tomcat').profile('ide-setup')
            self.phase('initialize')
            return self.__call_supper(logger)

    def __call_supper(self, logger):
        return super().__call__(logger)


