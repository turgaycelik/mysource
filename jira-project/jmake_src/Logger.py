from datetime import datetime


class Logger:
    L_TRACE = 50
    L_DEBUG = 40
    L_INFO = 30
    L_WARN = 20
    L_ERROR = 10
    L_NONE = 0
    L_ONLINE = 1
    L_OFFLINE = 2


    LEVELS_COLORS = {
        L_DEBUG: '36',
        L_INFO: '1',
        L_WARN: '33',
        L_ERROR: '31',
        L_TRACE: '36',

        L_ONLINE: '32',
        L_OFFLINE: '31'
    }

    def __init__(self, level = L_DEBUG):
        super().__init__()
        self.level = level

    def __out(self, levelName, level, text):
        if self.level >= level:
            for line in text.rstrip().split('\n'):
                print(self.color_text('{0} [{1}] {2}'.format(datetime.now().isoformat(sep=' '), levelName, line.strip()),
                      level))
        return 0

    def debug(self, text):
        return self.__out('DEBUG', Logger.L_DEBUG, text)

    def trace(self, text):
        return self.__out('TRACE', Logger.L_TRACE, text)

    def info(self, text):
        return self.__out('INFO ', Logger.L_INFO, text)

    def error(self, text):
        return self.__out('\x1b[0m\x1b[{0};7mERROR\x1b[0m\x1b[{0}m'.format(Logger.LEVELS_COLORS[Logger.L_ERROR]), Logger.L_ERROR,
            text)

    def warn(self, text):
        return self.__out('WARN ', Logger.L_WARN, text)

    def set_debug(self):
        self.level = Logger.L_DEBUG
        return self

    def set_trace(self):
        self.level = Logger.L_TRACE
        return self

    def set_info(self):
        self.level = Logger.L_INFO
        return self

    def set_error(self):
        self.level = Logger.L_ERROR
        return self

    def set_warn(self):
        self.level = Logger.L_WARN
        return self

    def set_none(self):
        self.level = Logger.L_NONE
        return self

    def is_trace(self):
        return self.level == Logger.L_TRACE

    @staticmethod
    def get_log_names():
        return ['debug', 'trace', 'info', 'error', 'warn', 'none']

    @staticmethod
    def color_text(string, level):
        return '\x1b[%sm%s\x1b[0m' % (Logger.LEVELS_COLORS[level], string)

LOG = Logger()