import re
import types
from eh.metrics.MetricsCollector import MetricsCollector, MetricsLogger


class GrepCount(MetricsCollector):
    def __init__(self, metrics_name: str, file_filter, description: str=None, grep_str=None, stop_object=lambda _ : False,
            use_regex:bool=False, metrics_logger: MetricsLogger = None):
        """

        :param metrics_name: name for the metrics
        :param file_filter:function for filtering files
        :param description: description for the metrics
        :param grep_str: str,list or function object that will be passed subsequent lines and return true on hits
        :param stop_object: str,list or function object that will be passed subsequent lines and should return true if
        no more lines are required from the file
        :param use_regex: if true all strings will be treated as regular expressions
        :param metrics_logger: MetricsLogger instance

        """
        super().__init__(metrics_name, description, metrics_logger)
        self.file_filter = file_filter
        self.grep_str_fn = self.get_filter_function(grep_str, use_regex)
        self.grep_str = grep_str

        self.stop_str_fn = self.get_filter_function(stop_object, use_regex)
        self.stop_str = stop_object

    def wants_file(self, file_name: str):
        self.file_name = file_name
        return self.file_filter(file_name)

    def on_read_line(self, line: str):

        found = self.grep_str_fn(line)
        if found:
            if self.verbose:
                self.log.debug(str(self.grep_str) + ' in: ' + self.file_name)
            grep_by = self.grep_str if not isinstance(
                self.grep_str, types.FunctionType) else '<FUNCTION %s>' % self.grep_str.__name__

            self.hit('file matched: %s, grep_str: %s, line: %s' % (self.file_name, grep_by, line.replace('\n', '')))
        return not self.stop_str_fn(line)

    def get_filter_function(self, filter_str, use_regex: bool):
        if filter_str is None:
            return lambda _: True
        elif callable(filter_str):
            return filter_str
        elif type(filter_str) == str:
            if use_regex:
                compiled_regex = re.compile(filter_str)
                return lambda line: compiled_regex.search(line) is not None
            else:
                return lambda line: filter_str in line
        else:
            if use_regex:
                compiled_regexes = [re.compile(str_regex) for str_regex in filter_str]
                return lambda line: next((1 for regex in compiled_regexes if regex.search(line) is not None),
                                         None) is not None
            else:
                return lambda line: next((word for word in filter_str if word in line), None) is not None


class GrepCountWithActivator(GrepCount):
    def __init__(self, metrics_name: str, file_filter, description: str=None, grep_str=None, stop_object=lambda _ : False,
            start_count=None, stop_count=None,use_regex=False, metrics_logger: MetricsLogger = None):
        """

        :param metrics_name: as in GrepCount
        :param file_filter: as in GrepCount
        :param description: as in GrepCount
        :param grep_str: as in GrepCount
        :param stop_object: as in GrepCount
        :param start_count: str,list,function,regex indicating when we get into the scope of counting occurrences
         of grep_str
        :param stop_count: str,list,function,regex indicating when we get out of scope of counting of grep_str
        :param metrics_logger: MetricsLogger instance
        """

        self.in_scope=False
        self.start_count_fn = self.get_filter_function(start_count,use_regex)
        self.stop_count_fn = self.get_filter_function(stop_count,use_regex)
        self.inner_grep_str_fn = self.get_filter_function(grep_str, use_regex)
        def grep_function(line):

            if self.in_scope:
                self.in_scope = self.stop_count_fn(line) is False
                return self.inner_grep_str_fn(line)
            else:
                self.in_scope = self.start_count_fn(line)
                return False

        super().__init__(metrics_name, file_filter, description, grep_function, stop_object, use_regex, metrics_logger)
