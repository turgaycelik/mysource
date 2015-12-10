from functools import wraps
import os
from CommandExecutor import Callable
from maven.PomParser import PomParser
from utils.DataBean import DataBean
from utils.FileUtils import FileUtils
from utils.ProcessUtils import ProcessUtils


class WorkspaceValidator:
    def __init__(self, fileutils=FileUtils()):
        super().__init__()
        self.fileutils = fileutils

    def __call__(self, logger):
        #at least must exist pom.xml in parent directory
        jira_workspace_dir = self.fileutils.get_parent_dir_path()
        workspace_pom = os.sep.join([jira_workspace_dir, "pom.xml"])
        if not self.fileutils.file_exists(workspace_pom):
            logger.error('Workspace mode is set yet no workspace pom.xml has been found (%s)' % workspace_pom)
            return Callable.failure
        return Callable.success

class WorkspaceUtils:
    def get_workspace_projects_without_jira(self, args, fileutils=FileUtils()):
        workspace_projects_without_jira = [project for project in self.get_workspace_projects(fileutils)
                                           if project != fileutils.get_current_dir_name()]
        return workspace_projects_without_jira if args.with_workspace else []

    def get_jira_workspace_dir(self, file_utils=FileUtils()):
        return os.path.split(file_utils.getcwd())[0]


    def get_workspace_projects(self, file_utils=FileUtils()):
        #get parent directory of jira-project (where jira-project dir is in)
        jira_workspace_dir = self.get_jira_workspace_dir(file_utils)

        #look for poms in directories siblings to jira-project;
        #maybe in the future it should parse workspace pom.xml for that...
        return [dir for dir in file_utils.listdir(jira_workspace_dir)
                if file_utils.dir_exists(os.sep.join([jira_workspace_dir, dir])) and
                   file_utils.file_exists(os.sep.join([jira_workspace_dir, dir, "pom.xml"]))]

class MavenCallableWorkspaceDelegator:
    @staticmethod
    def after_init(self, path, file_utils, pom_parser_class_object=PomParser, workspace_utils=WorkspaceUtils()):
        self.project_prefix = file_utils.get_parent_dir_relpath(path)

        def generate_parsers_from_workspace():
            jira_workspace_dir = workspace_utils.get_jira_workspace_dir(file_utils)
            workspace_projects = workspace_utils.get_workspace_projects(file_utils)

            for parser in (pom_parser_class_object(os.sep.join([jira_workspace_dir, project, "pom.xml"]))
                           for project in workspace_projects):
                if parser.get_artifact_id() == 'jira-project':
                    #jira-project is often referred as jira.version property and not jira.project.version
                    #as this is out main project allow for exception here and set both properties for now
                    #might create mapping file for that but it seems overkill for now
                    mock_parser = DataBean()
                    mock_parser.get_artifact_id = lambda: 'jira'
                    mock_parser.get_version = lambda: parser.get_version()
                    yield mock_parser
                yield parser
            for parser in (pom_parser_class_object(os.sep.join([jira_workspace_dir, project, "pom.xml"]))
                           for project in workspace_projects):
                yield parser

        #need to figure out property name used in other projects for setting version of other artifact version
        #assume it's artifactId with '-' replaced '.'
        for parser in generate_parsers_from_workspace():
            self.property(parser.get_artifact_id().replace('-', '.') + '.version', parser.get_version())

    @staticmethod
    def change_params_for_workspace(f):

        @wraps(f)
        def _change_params_for_workspace(self, args=None, path=None, process_utils=ProcessUtils(),
                file_utils=FileUtils()):
            if args is None:
                return f(self, args, path, process_utils, file_utils)
            else:
                if getattr(args, 'with_workspace', False) and path is None:
                    path = file_utils.get_parent_dir_path()

                result = f(self, args, path, process_utils, file_utils)

                if getattr(args, 'with_workspace', False):
                    MavenCallableWorkspaceDelegator.after_init(self, path, file_utils)

                return result

        return _change_params_for_workspace