import os
import platform
import re
import shutil
from zipfile import ZipFile
from utils.Mocks import Mock

from utils import PathUtils

class FileUtils:
    def __init__(self):
        super().__init__()

    def get_current_dir_name(self):
        return os.path.split(self.getcwd())[1]

    def get_parent_dir_path(self):
        return os.path.split(self.getcwd())[0]

    def get_parent_dir_relpath(self, path):
        return os.path.relpath(self.getcwd(), path)

    def getcwd(self):
        return os.getcwd()

    def file_exists(self, path):
        return os.path.lexists(path)

    def dir_exists(self, path):
        return os.path.isdir(path)

    def getmtime(self, path):
        return os.path.getmtime(path)

    def walk(self, path):
        return os.walk(path)

    def read_lines(self, path):
        with open(path) as file:
            return [line.strip() for line in file.readlines()]

    def abs_path(self, path):
        return PathUtils.abspath(path)

    def existing_dir(self, path):
        result = PathUtils.abspath(path)
        if not os.path.lexists(result): os.makedirs(result)
        return result

    def write_lines(self, file_name, lines):
        self.existing_dir(os.path.dirname(file_name))
        with open(file_name, mode='w') as file:
            file.write('\n'.join(lines))

    def remove(self, file_name):
        os.remove(file_name)

    def remove_dir(self, dir_name):
        shutil.rmtree(dir_name)

    def possible_idea_plugin_dirs(self):
        if platform.system() == 'Darwin':
            config_dir = os.path.expanduser(os.sep.join(['~', 'Library', 'Application Support', 'IntelliJIdea']))
        elif platform.system() == 'Windows':
            config_dir = os.path.expanduser(os.sep.join(['~', 'IntelliJIdea']))
        else:
            config_dir = os.path.expanduser(os.sep.join(['~', '.IntelliJIdea']))

        return (config_dir + ver for ver in ['13', '12', '11', '10', '90'])

    def filter_file(self, src_file, dst_file, replace_values):
        rc = re.compile('|'.join(map(re.escape, replace_values)))

        def translate(match):
            return replace_values[match.group(0)]

        with open(dst_file, 'w') as out, open(src_file) as input:
            file_content = input.read()
            content = rc.sub(translate, file_content)
            out.write(content)

    def open(self, file, mode='r', encoding='iso-8859-1'):
        return open(file, mode=mode, encoding=encoding)

    def copy_file(self, src, dest):
        shutil.copyfile(src, dest)

    def copy_tree(self, src, dest):
        shutil.copytree(src, dest)

    def touch(self, path):
        os.utime(path, None)

    def listdir(self, dir):
        return os.listdir(dir)

    def file_size(self, file:str):
        return os.stat(file).st_size

    def rename(self, old_name: str, new_name: str):
        return os.rename(old_name, new_name)

    def extract_zip(self, zip_file, path):
        with ZipFile(zip_file) as jira_home_zip:
            jira_home_zip.extractall(path=path)
            jira_home_zip.close()

    def symlink(self, src, dst):
        """ Create a symbolic link pointing to src named dst. """
        return os.symlink(src, dst)



class MockFileUtils(Mock):
    def __init__(self):
        super().__init__()
        self.default_file_exists(False)
        self.default_dir_exist(False)
        self.default_getmtime(0)

        self.filtered_files = []
        self.walk_dirs = {}

    def expect_walk(self, path, walk_dict):
        """
        Prepare a tree structure to walk. walk_dict for structure:
          root
          +-dir1
            --file1_1
          +-dir2        # empty dir
          +-file1
          --file2

        should look like this:
        { 'files': ['file1', 'file2'],
          'dir1': { 'files:': ['file1_1']},
          'dir2': {} }

        """
        self.walk_dirs[path] = walk_dict

    def abs_path(self, path):
        return path

    def walk(self, path):
        def generate_walk(w_dict, root):
            files = w_dict['files'] if 'files' in w_dict else []
            dirs = [k for k, v in w_dict.items() if isinstance(v, dict)]
            yield (root, dirs, files)
            for d in dirs:
                for item in generate_walk(w_dict[d], os.sep.join([root, d])): yield item

        for t in generate_walk(self.walk_dirs[path] if path in self.walk_dirs else {}, 'root'):
            yield t

    def existing_dir(self, path):
        return path

    def open(self, path='', mode=''):
        class MockFile():
            def write(self, content):
                pass

            def close(self):
                pass

            def __iter__(self):
                return iter([])

            def __exit__(self, exc_type, exc_val, exc_tb):
                self.close()

            def __enter__(self):
                return self

        return MockFile()

    def filter_file(self, src_file, dst_file, replace_values):
        self.filtered_files.append((src_file, dst_file, replace_values))

    def verify_filter_file(self, src_file, dest_file):
        for (src, dst, replace) in self.filtered_files:
            if src == src_file and dst == dest_file:
                return replace if replace is not None else {}
        return None
