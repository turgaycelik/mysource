import os
import shutil
from CommandExecutor import Callable
from utils.FileUtils import FileUtils

class ContextPreparer(Callable):
    WEB_XML_NAME = 'web.xml'
    SERVER_XML_NAME = 'server.xml'
    SSL_SERVER_XML_NAME = 'server-ssl.xml'

    def __init__(self, args, fileutils=FileUtils()):
        super().__init__(args)
        self.fileutils = fileutils

    def __call__(self, logger):
        logger.debug('Preparing JIRA context in ' + self.args.layout.tomcat_conf_dest())
        self.prepare_base_dir(self.args.layout)
        return Callable.success

    def prepare_base_dir(self, layout):
        shutil.copy(os.sep.join([layout.tomcat_conf_src(), self.WEB_XML_NAME]), layout.tomcat_conf_dest())
        self.fileutils.filter_file(os.sep.join([layout.tomcat_conf_src(), self.SSL_SERVER_XML_NAME if self.args.ssl else self.SERVER_XML_NAME]),
            os.sep.join([layout.tomcat_conf_dest(), self.SERVER_XML_NAME]), self.replace_values(layout))


    def replace_values(self, layout):
        return {
            '${jira.port}': str(self.args.port),
            '${jira.shutdown.port}': str(self.args.sh_port),
            '${jira.context}': str(self.args.jira_context),
            '${jira.docbase}': layout.jira_webapp_dir(),
            '${tomcat.workdir}': layout.tomcat_work_dir()
        }








