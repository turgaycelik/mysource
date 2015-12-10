from functools import partial
import os
import subprocess
from xml.dom import minidom
from CommandExecutor import Callable
from maven.Maven import MavenCallable
from module import JmakeModule
from utils.FileUtils import FileUtils
from utils.XmlUtils import XmlUtils


def ensure_project_was_opened(logger, fileutils = FileUtils()):
    idea_dir = '.%s.idea' % os.sep
    if fileutils.dir_exists(idea_dir):
        return Callable.success
    else:
        logger.error('Your IDEA directory (%s) is missing. Open the JIRA project by pointing the pom.xml '
                     'and rerun jmake idea.' % idea_dir)
        return Callable.do_not_proceed


def exit_idea(logger):
    logger.info('In order to remove files from IDEA, you must close it.')

    def is_idea_closed():
        status, output = subprocess.getstatusoutput('jps -v | grep idea | cut -d" " -f-2')
        if output:
            logger.info('These are your IDEA processes: ')
            print(output)
            return False
        return True

    try:
        while not is_idea_closed():
            choice = input(
                'Please close IDEA and press return (you may type "force" to bypass the check, or CTRL-C to abort): ')
            if choice == 'force': break
    except KeyboardInterrupt:
        print('\n')
        logger.info('(User Interrupt) Why so afraid? ... well, have it your way.')
        return Callable.do_not_proceed

    return Callable.success


def process_clean(logger, fileutils=FileUtils()):
    logger.info('Removing old run configurations...')
    run_cfg_dir = os.sep.join(['.', '.idea', 'runConfigurations'])
    legacy_run_configs = ['Func_Tests', 'JIRA_OnDemand', 'Tomcat_6', 'Tomcat_7', 'Unit_Tests__Fast_',
                          'Unit_Tests__Legacy_', 'WebDriver_Tests', 'Selenium_Tests']
    for file in ( run_cfg_dir + os.sep + item + '.xml' for item in legacy_run_configs ):
        try:
            if fileutils.file_exists(file):
                fileutils.remove(file)
                logger.debug('Removed file: ' + file)
        except IOError:
            logger.error('Could not remove file:' + file)
            return Callable.success - 4

    logger.info('Removing old artifacts...')
    old_artifact = os.sep.join(['.', '.idea', 'artifacts', 'JIRA.xml'])
    try:
        if fileutils.file_exists(old_artifact):
            fileutils.remove(old_artifact)
            logger.debug('Deleted: ' + old_artifact)
    except IOError:
        logger.error('Could not remove file: ' + old_artifact)
        return Callable.success - 4

    logger.info('Removing target dirs for old artifacts...')
    for target_dir in (os.sep.join(['.', item]) for item in ['classes', 'tomcatBase']):
        try:
            if fileutils.dir_exists(target_dir):
                fileutils.remove_dir(target_dir)
                logger.debug('Removed directory: ' + target_dir)
        except IOError:
            logger.error('Could not remove directory:' + target_dir)
            return Callable.success - 4

    logger.info('Removing jira.idea.properties...')
    legacy_idea_properties = os.sep.join(['jira-ide-support', 'src', 'main', 'resources', 'jira.idea.properties'])
    try:
        if fileutils.file_exists(legacy_idea_properties):
            fileutils.remove(legacy_idea_properties)
            logger.debug('Deleted: ' + legacy_idea_properties)
    except IOError:
        logger.error('Could not remove file: ' + legacy_idea_properties)
        return Callable.success - 4

    return Callable.success


def process_compiler_settings(args, xml=XmlUtils()):
    def process_compiler_settings_closure(logger):
        logger.info('Checking for compiler configuration...')
        compiler_file = '.idea%scompiler.xml' % os.sep
        try:
            tree = xml.parse(compiler_file)
        except IOError:
            logger.error('Could not open ' + compiler_file)
            return Callable.success - 1

        javac_element = xml.produce(tree.getroot(),
            ('component', {'name': 'JavacSettings'}),
            ('option', {'name': 'MAXIMUM_HEAP_SIZE'}))

        needs_save = False

        if int(javac_element.get('value', '-1')) < 512 or args.force:
            javac_element.set('value', '512')
            needs_save = True
            logger.debug('Updated javac memory to 512M')

        resource_list = xml.produce(tree.getroot(),
            ('component' , {'name': 'CompilerConfiguration'}),
            ('wildcardResourcePatterns' , {}))

        resources_to_add = ['vm', 'soy']

        for resource in resources_to_add:
            name = 'entry'
            attr = {'name': ('?*.' + resource)}
            if not xml.child_exists(resource_list, name, attr):
                logger.debug('Added "%s" files as resources in compiler settings.' % resource)
                xml.produce(resource_list, (name, attr))
                needs_save = True

        if needs_save:
            try:
                tree.write(compiler_file)
            except IOError:
                logger.error('Could not save ' + compiler_file)
                return Callable.success - 2

        return Callable.success

    return process_compiler_settings_closure


def process_run_configs(args, xml=XmlUtils()):
    def run_configs_closure(logger):
        logger.info('Checking for run configurations in workspace...')
        workspace_file = '.idea%sworkspace.xml' % os.sep
        idea_runners_file = os.sep.join(
            ['jira-ide-support', 'src', 'main', 'resources', 'ideaTemplates', 'runConfigurations.xml'])

        try:
            workspace_tree = xml.parse(workspace_file)
        except IOError:
            logger.error('Could not open ' + workspace_file)
            return Callable.success - 1

        try:
            run_config_tree = xml.parse(idea_runners_file)
        except IOError:
            logger.error('Could not open ' + idea_runners_file)
            return Callable.success - 1

        component_element = run_config_tree.getroot()[0]
        workspace_component = xml.produce(workspace_tree.getroot(), (component_element.tag, component_element.attrib))

        num_configurations_written = 0

        for run_config_element in component_element:
            workspace_config = workspace_component.find(
                './%s[@name="%s"]' % (run_config_element.tag, run_config_element.attrib['name']))
            if workspace_config is None:
                logger.debug('copying run configuration ' + run_config_element.attrib['name'])
                xml.copy_tree(workspace_component, run_config_element)
                num_configurations_written += 1
            elif args.force:
                workspace_component.remove(workspace_config)
                logger.debug('overwriting run configuration ' + run_config_element.attrib['name'])
                xml.copy_tree(workspace_component, run_config_element)
                num_configurations_written += 1

        if num_configurations_written > 0:
            try:
                #os.rename(workspace_file, workspace_file + '.jmake_backup')
                workspace_tree.write(workspace_file)
                logger.info('Added %d new run configurations.' % num_configurations_written)
            except IOError:
                logger.error('Could not save ' + workspace_file)
                return Callable.success - 2

        return Callable.success

    return run_configs_closure


def process_target_exclusions(args, xml=XmlUtils()):
    def process_exclusions_closure(logger):
        project_file = 'jira-project.iml'
        paths_to_exclude = get_targets_for(['jira-ondemand-project', 'jira-distribution'])
        paths_to_exclude += ['jirahome', 'jirahome-od', 'svn', 'jirahome-shared', 'jirahome-clustered']

        try:
            tree = xml.parse(project_file)
        except IOError:
            logger.error('Could not open ' + project_file)
            return Callable.success - 1

        content_nodes = tree.getroot().findall('./component/content')
        save_needed = False
        for content in content_nodes:
            for file in paths_to_exclude:
                save_needed |= add_single_exclusion(content, file)

        if save_needed:
            logger.debug('Added new exclusions to %s.' % project_file)
            pretty_xml = minidom.parseString(xml.tostring(tree.getroot())).toprettyxml()
            file = open(project_file, 'wt', encoding='utf-8')
            file.write(pretty_xml)
            file.close()

        return Callable.success

    return process_exclusions_closure


def add_single_exclusion(content, file, xml=XmlUtils()):
    file_uri = 'file://$MODULE_DIR$/%s' % file
    if len(content.findall('./excludeFolder[@url="%s"]' % file_uri)) == 0:
        xml.produce(content, ('excludeFolder', {'url': file_uri}))
        return True
    return False

def get_targets_for(projects, fileutils=FileUtils()):
    return [os.path.join(root, 'target') for project in projects for root, dirs, files in fileutils.walk(project) if
            'pom.xml' in files and root.find('target') == -1]

def idea_templates(executor, args, fileutils=FileUtils()):

    executor.append(lambda logger : logger.info('Setting up code style templates...') or 0)
    executor.append(MavenCallable(args).project('jira-ide-support').profile('ide-setup').profile('idea-templates').option(
        '-am').phase('generate-sources'))

    executor.append(process_project_local_settings(args))

def process_project_local_settings(args, fileutils=FileUtils(), xml=XmlUtils()):
    def process_project_local_settings_closure(logger):
        code_style_name = 'codeStyleSettings.xml'
        code_style_src = os.sep.join(['jira-ide-support', 'src', 'main', 'resources', 'ideaTemplates', code_style_name])
        code_style_dst = os.sep.join(['.idea', code_style_name])

        uses_per_project_settings = False

        try:
            tree = xml.parse(code_style_dst)

            element = xml.produce(tree.getroot(),
                ('component', {}),
                ('option', {'name': 'USE_PER_PROJECT_SETTINGS'}))

            uses_per_project_settings = element.attrib['value'] == 'true' if 'value' in element.attrib else False
        except IOError:
            pass

        if args.force or not uses_per_project_settings:
            logger.info('Installing project code style...')
            fileutils.copy_file(code_style_src, code_style_dst)
            #make sure to wake up idea:
            fileutils.touch(os.sep.join(['.idea', 'workspace.xml']))
        elif uses_per_project_settings:
            logger.debug('Not installing project code style, because already set to per-project configuration. Use --force to override.')

        return Callable.success

    return process_project_local_settings_closure

def process_jmake_module(args, fileutils=FileUtils(), xml=XmlUtils()):
    def process_jmake_module_closure(logger):
        logger.info('Installing jmake dev module...')
        jmake_module_file = os.sep.join(['.', 'jmake_src', 'jmake_src.iml'])
        if not fileutils.file_exists(jmake_module_file) or args.force:
            # module file does not exist: create it and add it to the module list.
            jmake_module_template = os.sep.join(['jira-ide-support', 'src', 'main', 'resources', 'ideaTemplates', 'jmake', 'jmake_src.iml.template'])
            logger.debug('Copying %s to %s' % (jmake_module_template, jmake_module_file))
            fileutils.copy_file(jmake_module_template, jmake_module_file)

            workspace_modules_file = os.sep.join(['.', '.idea', 'modules.xml'])

            try:
                workspace_tree = xml.parse(workspace_modules_file)
            except IOError:
                logger.error('Could not open ' + workspace_modules_file)
                return Callable.success - 1


            module = xml.produce(workspace_tree.getroot(),
                ('component', {}),
                ('modules', {}),
                ('module', { 'filepath': os.sep.join(['$PROJECT_DIR$', 'jmake_src', 'jmake_src.iml']) } ))

            if not 'fileurl' in module.attrib:
                logger.debug('Adding module entry to %s' % workspace_modules_file)
                module.attrib['fileurl'] = 'file://$PROJECT_DIR$/jmake_src/jmake_src.iml'
                try:
                    #os.rename(workspace_file, workspace_file + '.jmake_backup')
                    workspace_tree.write(workspace_modules_file)
                    logger.debug('Saved successfully.')
                except IOError:
                    logger.error('Could not save ' + workspace_modules_file)
                    return Callable.success - 2

        return Callable.success

    return process_jmake_module_closure

def process_dev_profiles(xml=XmlUtils()):
    def process_dev_profiles_closure(logger):
        logger.info('Enabling dev profiles...')

        workspace_file = os.sep.join(['.', '.idea', 'workspace.xml'])
        try:
            workspace_tree = xml.parse(workspace_file)
        except IOError:
            logger.error('Could not open ' + workspace_file)
            return Callable.success - 1

        profiles = ['func-mode-plugins', 'pseudo-loc', 'dev-mode-plugins']

        profile_list = xml.produce(workspace_tree.getroot(),
                ('component', {'name': 'MavenImportPreferences'}),
                ('option', {'name': 'enabledProfiles'}),
                ('list', {}))

        existing_profiles = [child.attrib['value'] for child in profile_list]
        item_added = False

        for profile in ( e for e in profiles if e not in existing_profiles ):
            logger.debug ('Adding profile: ' + profile)
            item_added = True
            xml.produce(profile_list, ('option', {'value': profile}))

        if item_added:
            try:
                #os.rename(workspace_file, workspace_file + '.jmake_backup')
                workspace_tree.write(workspace_file)
                logger.debug('Saved successfully.')
            except IOError:
                logger.error('Could not save ' + workspace_file)
                return Callable.success - 2
        return Callable.success

    return process_dev_profiles_closure


class Idea(JmakeModule):
    def __init__(self):
        super().__init__()
        self.command = 'idea'
        self.description = 'Prepares IDEA IDE to work with JIRA. In particular, it does all the stuff that you can skip '\
                           'with extra options so treat this as a list of what it actually does. Note, that you must first '\
                           'open the JIRA project in your IDEA (point it at the main pom) for this to work. If you have '\
                           'previously setup your IDEA and you have the old JIRA artifact installed, run with clean, or, '\
                           'better yet, remove your .idea directory and reopen the project from pom, then run jmake idea.'
        self.check_branch = False

    def __call__(self, args, executor):
        executor.append(ensure_project_was_opened)
        if args.mvn_clean:
            executor.append(exit_idea)
            executor.append(process_clean)
        if not args.skip_idea_templates: idea_templates(executor, args)
        if not args.skip_compiler: executor.append(process_compiler_settings(args))
        if not args.skip_run_configurations: executor.append(process_run_configs(args))
        if not args.skip_target_exclusions: executor.append(process_target_exclusions(args))
        if not args.skip_jmake_module: executor.append(process_jmake_module(args))
        if not args.skip_dev_profiles: executor.append(process_dev_profiles())

        executor.append(MavenCallable().phase('install').skip_tests().project('jira-components/jira-api').option('-am'))
        executor.append(MavenCallable().phase('generate-sources').project('jira-components/jira-core'))

    def define_parser(self, parser):
        MavenCallable.add_maven_switches(parser)
        parser.add_argument('--skip-idea-templates', action='store_true', help='installs the code style and comment '
                                                                               'templates into your IDEA config directory (global).',
            dest='skip_idea_templates')
        parser.add_argument('--skip-compiler', action='store_true', help='updates IDEA compiler settings.',
            dest='skip_compiler')
        parser.add_argument('--skip-run-configurations', action='store_true', help='injects JIRA run configurations '
                                                                                   'into your workspace.',
            dest='skip_run_configurations')
        parser.add_argument('--skip-target-exclusions', action='store_true',
            help='excludes targets from ondemand and distribution projects ',
            dest='skip_target_exclusions')
        parser.add_argument('--skip-jmake-module', action='store_true', help='sets up jmake development module.',
            dest='skip_jmake_module')
        parser.add_argument('--skip-dev-profiles', action='store_true', help='enables development profiles in IDEA.',
            dest='skip_dev_profiles')

        parser.add_argument('-f', '--force', action='store_true', help='jmake will not blindly rewrite stuff '
                                                                       'in your workspace, unless you really want to.',
            dest='force')


def inject(object, f):
    """
    Injects f as method of object. f must have at least 1 argument, 'self'.
    NOTE: Since python 3.3 will not woirk for Element objects from xml.etree
    """
    setattr(object, f.__name__, partial(f, object))
    return object

