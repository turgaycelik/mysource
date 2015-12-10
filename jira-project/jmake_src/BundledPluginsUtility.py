from functools import reduce
import os
import re
from Logger import LOG
from utils import PathUtils
from utils.FileUtils import FileUtils


JIRA_PLUGINS_DIR = os.sep.join(['jira-components', 'jira-plugins'])
JIRA_PLUGINS_DIR_ABS = os.path.abspath(os.sep.join(['jira-components', 'jira-plugins']))
JIRA_PLUGINS_POM_XML = os.sep.join([JIRA_PLUGINS_DIR, 'pom.xml'])
JIRA_PLUGIN_DIR = os.sep.join([JIRA_PLUGINS_DIR, '{0}'])
PLUGIN_TARGET_DIR = os.sep.join([JIRA_PLUGINS_DIR, '{}', 'target'])
PLUGIN_SRC_DIR = os.sep.join([JIRA_PLUGINS_DIR, '{}', 'src'])
PLUGIN_POM_DIR = os.sep.join([JIRA_PLUGINS_DIR, '{}', 'pom.xml'])
PLUGIN_JAR_FILE = os.sep.join([PLUGIN_TARGET_DIR, '{0}-{1}.jar'])
BUNDLED_PLUGINS_LIST = PathUtils.abspath(
    os.sep.join([PLUGIN_TARGET_DIR.format('jira-bundled-plugins'), 'atlassian-bundled-plugins.list']))
JMAKE_PROFILES_PLUGINS_LIST = os.path.abspath(
    os.sep.join([PLUGIN_TARGET_DIR.format('jira-bundled-plugins'), 'jmake-bundled-plugins-profiles.list']))
BUNDLED_PLUGINS_MODULE = JIRA_PLUGIN_DIR.format('jira-bundled-plugins')
BUNDLED_PLUGINS_POM = PLUGIN_POM_DIR.format('jira-bundled-plugins')
PLUGIN_FROM_PATH_EXTRACTOR = r'.+' + JIRA_PLUGINS_DIR + os.sep + '(.+)' + os.sep + "target.+"

class BundledPluginsUtility:
    def __init__(self, file_utils=FileUtils()):
        super().__init__()
        self.file_utils = file_utils


    def get_bundled_plugins_module(self):
        return [BUNDLED_PLUGINS_MODULE]

    def find_plugins_to_recompile(self, profiles):
        if not (self.bundled_plugins_list_ok(profiles)):
            return self.get_bundled_plugins_module()
        else:
            return [JIRA_PLUGIN_DIR.format(module[0]) for module in self.__get_all_local_plugins_from_list() if
                    self.module_needs_recompilation(module)]

    def remember_plugins_profiles(self, profiles):
        self.file_utils.write_lines(JMAKE_PROFILES_PLUGINS_LIST, profiles)

    def bundled_plugins_list_ok(self, profiles):
        return self.__bundled_plugins_list_exists() and self.__bundled_plugins_list_up_to_date() and\
               self.__all_bundled_plugins_in_local_repo() and self.__profiles_up_to_date(profiles)

    def __bundled_plugins_list_exists(self):
        path_lexists = self.file_utils.file_exists(BUNDLED_PLUGINS_LIST)
        if not path_lexists:
            self.log('Bundled plugins list do not exists : ' + BUNDLED_PLUGINS_LIST)
        return path_lexists

    def __bundled_plugins_list_up_to_date(self):
        pom_up_to_date = self.file_utils.getmtime(BUNDLED_PLUGINS_LIST) > self.file_utils.getmtime(BUNDLED_PLUGINS_POM)
        if not pom_up_to_date:
            self.log('Bundled plugins pom newer than bundled plugins list')
        return pom_up_to_date

    def __all_bundled_plugins_in_local_repo(self):
        paths_not_in_repo = [line for line in self.get_all_bundled_plugins_paths() if
                             not line.startswith(JIRA_PLUGINS_DIR_ABS) and not self.file_utils.file_exists(
                                 line)]

        all_plugins_in_repo = len(paths_not_in_repo) == 0
        if not all_plugins_in_repo:
            self.log('Not all bundled plugins in local maven repository: ' + str(paths_not_in_repo))
        return all_plugins_in_repo

    def get_all_bundled_plugins_paths(self):
        return self.file_utils.read_lines(BUNDLED_PLUGINS_LIST)

    def module_needs_recompilation(self, module):
        module_name, plugin_jar = module
        src_dir = PLUGIN_SRC_DIR.format(module_name)
        target_dir = PLUGIN_TARGET_DIR.format(module_name)
        pom_file = PLUGIN_POM_DIR.format(module_name)
        if not self.file_utils.file_exists(plugin_jar):
            self.log('Recompiling plugin {} jar file {} not found '.format(module_name, plugin_jar))
            return True

        #check also pom for modification
        last_src_modification = max(
            self.__find_last_modification(src_dir),
            self.file_utils.getmtime(pom_file))

        #if jar is older than target than also recompile
        last_target_modification = min(self.__find_last_modification(target_dir),
                                       self.file_utils.getmtime(plugin_jar))
        if last_src_modification > last_target_modification:
            self.log('Recompiling plugin {} found changes'.format(module_name))
            return True
        else:
            return False

    def log(self, msg):
        LOG.info(msg)

    def __find_last_modification(self, dir):
        last_mod_time = 0
        for root, dirs, files in self.file_utils.walk(dir):
            last_mod_time = max(last_mod_time,
                reduce(lambda act_max, file: max(act_max, self.file_utils.getmtime(root + os.sep + file)), files,
                    last_mod_time))
        return last_mod_time

    def __get_all_local_plugins_from_list(self):
        return [(re.search(PLUGIN_FROM_PATH_EXTRACTOR, path).group(1), path) for path in
                self.get_all_bundled_plugins_paths()
                if path.startswith(JIRA_PLUGINS_DIR_ABS)]

    def __profiles_up_to_date(self, profiles):
        if not self.file_utils.file_exists(JMAKE_PROFILES_PLUGINS_LIST):
            self.log(
                'File %s does not exist assuming that bundled plugins needs recompilation' % JMAKE_PROFILES_PLUGINS_LIST)
            return False
        previous_profiles = self.file_utils.read_lines(JMAKE_PROFILES_PLUGINS_LIST)
        if frozenset(previous_profiles) == frozenset(profiles):
            return True
        else:
            self.log("Bundled plugins compiled with different profiles recompiling")
            return False







