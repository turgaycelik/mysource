import os
import platform
import shutil
import tempfile
from maven.PomParser import PomParser
from utils.FileUtils import FileUtils
from utils import PathUtils


class WorkspaceLayout:
    JIRA_HOME_DEFAULT_MARKER = '|DEFAULT_JIRA_HOME|'
    JIRA_HOME = os.sep.join(['.', 'jirahome'])
    JIRA_OD_HOME = os.sep.join(['.', 'jirahome-od'])
    JIRA_CLUSTERED_HOME_ROOT = os.sep.join(['.', 'jirahome-clustered'])
    JIRA_SHARED_HOME_ROOT = os.sep.join(['.', 'jirahome-shared'])
    STUDIO_INIT_DATA_FILE = 'studio-initial-data.xml'
    WEBDAV_DIRECTORY = 'webdav'
    JIRA_WEBAPP = os.sep.join(['.', 'jira-components', 'jira-webapp', 'target', 'jira'])
    JIRA_OD_WEBAPP = os.sep.join(['.', 'jira-ondemand-project', 'jira-ondemand-webapp', 'target', 'war', 'exploded'])
    JIRA_OD_SVN_DIR = os.sep.join(['.', 'svn-home'])
    JIRA_OD_SVN_LINK = os.sep.join([os.path.dirname(os.path.abspath('.')), 'svn'])
    JIRA_CLUSTERED_PROPERTIES_FILE = 'cluster.properties'
    TOMCAT_DOWNLOAD = os.sep.join(['.', 'tomcat'])

    def __init__(self, tomcat, version, tomcat_directory, jira_home_dir, instance_name='work', ondemand=False, file_utils=FileUtils()):
        self.tomcat_directory = tomcat_directory
        self.tomcat_family = tomcat
        self.tomcat_version = version
        self.file_utils = file_utils
        self.instance_name = instance_name
        self.ondemand = ondemand
        if jira_home_dir == WorkspaceLayout.JIRA_HOME_DEFAULT_MARKER:
            self.jira_home_dir = WorkspaceLayout.JIRA_OD_HOME if ondemand else WorkspaceLayout.JIRA_HOME
        else:
            self.jira_home_dir = jira_home_dir

    def tomcat_download_dir(self):
        return self.__existing_dir(self.tomcat_directory)

    def tomcat_dir(self, create=True):
        path = self.tomcat_download_dir() + '{0}apache-tomcat-{1}'.format(os.sep, self.tomcat_version)
        return self.__existing_dir(path) if create else path

    def tomcat_work_dir(self):
        return self.__existing_dir(os.sep.join(['.', 'target', self.tomcat_family, self.instance_name]))

    def tomcat_temp_dir(self):
        return self.__existing_dir(os.sep.join(['.', 'target', self.tomcat_family, self.instance_name + '-temp']))

    def tomcat_conf_src(self):
        return os.sep.join(
            ['.', 'jira-ide-support', 'src', 'main', 'resources', 'tomcatBase', self.tomcat_family, 'conf'])

    def tomcat_conf_dest(self):
        return self.__existing_dir(os.sep.join([self.tomcat_work_dir(), 'conf']))

    def tomcat_executable(self):
        """
        Get start script for tomcat
        """
        if platform.system() == 'Windows':
            return os.sep.join([self.tomcat_dir(), 'bin', 'catalina.bat'])
        else:
            return os.sep.join([self.tomcat_dir(), 'bin', 'catalina.sh'])

    def tomcat_executables(self):
        """
        Get all file that will be executed for this tomcat version
        """
        executables = [self.tomcat_executable()]
        if self.tomcat_family == WorkspaceBuilder.TOMCAT6:
            if platform.system() == 'Windows':
                executables.append(os.sep.join([self.tomcat_dir(), 'bin', 'setclasspath.bat']))
            else:
                executables.append(os.sep.join([self.tomcat_dir(), 'bin', 'setclasspath.sh']))

        return executables

    def jira_webapp_dir(self):

        return PathUtils.abspath(WorkspaceLayout.JIRA_OD_WEBAPP) if self.ondemand \
            else PathUtils.abspath(WorkspaceLayout.JIRA_WEBAPP)

    def jira_home(self):
        return PathUtils.abspath(self.jira_home_dir)

    def studio_initial_data(self):
        return PathUtils.abspath(os.sep.join([self.jira_home_dir, WorkspaceLayout.STUDIO_INIT_DATA_FILE]))

    def studio_svn_dir(self):
        return self.__existing_dir(WorkspaceLayout.JIRA_OD_SVN_DIR)

    def studio_svn_link(self):
        return WorkspaceLayout.JIRA_OD_SVN_LINK

    def webdav_dir(self):
        return PathUtils.abspath(os.sep.join([self.jira_home_dir, WorkspaceLayout.WEBDAV_DIRECTORY]))

    def cluster_properties(self):
        return os.sep.join([self.jira_home(), WorkspaceLayout.JIRA_CLUSTERED_PROPERTIES_FILE])

    def __existing_dir(self, path):
        return self.file_utils.existing_dir(path)


class WorkspaceBuilder:
    TOMCAT6 = 'tomcat6'
    TOMCAT7 = 'tomcat7'
    VERSIONS = {TOMCAT6: 'tomcat6x.version', TOMCAT7: 'tomcat7x.version'}

    @staticmethod
    def buildLayout(tomcatDir, tomcatVersion, jira_home, instance_name, ondemand):
        pom_parser = PomParser('pom.xml')
        version_ = pom_parser.get_property(WorkspaceBuilder.VERSIONS[tomcatVersion])
        return WorkspaceLayout(tomcatVersion, version_, tomcatDir, jira_home, instance_name, ondemand)


class MockLayout:
    def __init__(self, ondemand: bool):
        self.temp_dir = tempfile.mkdtemp()
        self.utils = FileUtils()
        self.ondemand = ondemand

    def tomcat_conf_src(self):
        return self.utils.existing_dir(os.path.join(self.temp_dir, 'src'))

    def tomcat_conf_dest(self):
        return self.utils.existing_dir(os.path.join(self.temp_dir, 'dest'))

    def jira_webapp_dir(self):
        return os.path.join(self.temp_dir, 'jira_webapp_dir')

    def tomcat_work_dir(self):
        return os.path.join(self.temp_dir, 'tomcat_work_dir')

    def tomcat_temp_dir(self):
        return os.path.join(self.temp_dir, 'tomcat_temp_dir')

    def tomcat_dir(self, create):
        return os.path.join(self.temp_dir, 'tomcat_dir')

    def jira_home(self):
        return os.path.join(self.temp_dir, 'jira_home')

    def studio_ititial_data(self):
        return os.path.join(self.temp_dir, 'jira_home', 'some-data.xml')

    def tomcat_executable(self):
        return os.path.join(self.tomcat_dir(False), 'some-runnable.sh')

    def tomcat_download_dir(self):
        return os.path.join(self.temp_dir, 'jira_home')

    def remove(self):
        shutil.rmtree(self.temp_dir)

    def studio_initial_data(self):
        return os.path.join(self.temp_dir, 'jira_home', 'some-initial-data.xml')

    def webdav_dir(self):
        return os.path.join(self.temp_dir, 'jira_home', 'webdav')
