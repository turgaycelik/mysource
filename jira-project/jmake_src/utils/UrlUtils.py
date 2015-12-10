import base64
from shutil import copyfileobj
from urllib.request import HTTPBasicAuthHandler, Request, urlopen
import sys
from CommandExecutor import SystemCallable
from Logger import Logger
from utils.FileUtils import FileUtils


class UrlReader(SystemCallable):
    def __init__(self, url: str, user: str, password: str, filter):
        if (user is not None) and (password is not None):
            if "'" in (user + password):
                raise Exception("Aborting curl call: your username and/or password contain a suspicious character and I'm not risking building a command line using it.")
            super().__init__(None, "curl -s --user '%s:%s' '%s'" % (user, password, url))
        else:
            super().__init__(None, "curl -s '%s'" % url)
        self.out = ''
        self.filter = filter

    def process_output(self, logger: Logger,  line : str, num : int):
        if self.filter(line):
            self.out += line + '\n'
        return False


class UrlUtils:

    def __init__(self, fs: FileUtils = FileUtils()):
        self.fs = fs
        pass

    def read(self, url: str, user: str, password: str, filter=lambda x: True):
        curl = UrlReader(url, user, password, filter)
        curl(Logger().set_none())
        return curl.out

    def download(self, url: str, user: str, password: str, output:str):
        auth_handler = HTTPBasicAuthHandler()
        auth_handler.add_password(None, url, user, password)

        req = Request(url, headers = {
            'Authorization': self.__build_basic_auth(user, password)
        })

        with urlopen(req) as remote_file:
            with self.fs.open(output, 'wb', None) as local_file:
                copyfileobj(remote_file, local_file)

        return True

    def __build_basic_auth(self, user, password):
        return 'Basic %s' % (
            base64.urlsafe_b64encode(bytes('%s:%s' % (user, password), sys.getdefaultencoding())).decode()
        )

