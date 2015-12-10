from utils.FileUtils import FileUtils


class MysqlUtils:

    DATABASE_NAME = 'jira_jmake'

    def __init__(self, fs:FileUtils=FileUtils()):
        super().__init__()
        self.fs = fs


    def running(self):
        return False

    def setup_callable(self):
        return lambda logger: logger.error("not implemented yet")

    def jmake_user_inspection(self):

        return lambda logger: logger.error("not implemented yet")

    def restore_data(self, data_file):
        pass
