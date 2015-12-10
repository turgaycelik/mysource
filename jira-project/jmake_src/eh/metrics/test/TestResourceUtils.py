import os

class TestResourcesUtils():
    @staticmethod
    def build_test_file_path(path):
        cwd = os.getcwd()
        jmake_src = '{0}jmake_src{0}'.format(os.sep)
        if jmake_src in cwd:
             end = cwd.rfind(jmake_src) + len(jmake_src)
             return os.sep.join([cwd[0:end-1]] + path)
        else:
            return os.sep.join(['.', 'jmake_src'] + path)

