import os

from utils.FileUtils import FileUtils


class HordeLayout:
    HORDE_DIR = 'horde'
    HORDE_LOG_FILE = 'horde.log'
    CROWD_URL = 'http://localhost:4990/crowd'
    HORDE_STATUS_URL = '%s/rest/healthcheck/checkDetails' % CROWD_URL
    HORDE_APPLICATION_LOGIN_URL = '%s/console/' % CROWD_URL
    HORDE_SERVER_URL = '%s/services/' % CROWD_URL

    def __init__(self, jira_home_dir, file_utils=FileUtils()):
        self.jira_home_dir = jira_home_dir
        self.file_utils = file_utils
        self.horde_home = os.sep.join([self.jira_home_dir, HordeLayout.HORDE_DIR])

    def horde_home_dir(self, create=True):
        return self.file_utils.existing_dir(self.horde_home) if create else self.horde_home

    def horde_skeleton_dir(self):
        return os.sep.join(['.', 'jmake_src', 'ondemand', 'data'])

    def horde_log_file(self):
        return os.sep.join([self.horde_home, HordeLayout.HORDE_LOG_FILE])

    def horde_status_url(self):
        return HordeLayout.HORDE_STATUS_URL

    def horde_application_login_url(self):
        return HordeLayout.HORDE_APPLICATION_LOGIN_URL

    def horde_server_url(self):
        return HordeLayout.HORDE_SERVER_URL