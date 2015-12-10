import inspect
import types
from unittest import TestCase
from eh.metrics import ModulesDescription
from eh.metrics.ModulesDescription import BundledPluginsModulesDescription, JIRADirectoryScanModulesDescription
from eh.metrics.tailored.DeprecatedMethodUsage import DeprecatedMethodUsage
from maven.Maven import MavenExecutionException
from utils.Mocks import Mock


class TestBundledPluginsModulesDescription(TestCase):
    def test_init_with_fast_does_not_add_deprecated_methods(self):
        # having
        module_desc = JIRADirectoryScanModulesDescription(True)
        # when
        collectors = module_desc.get_collectors()

        #then
        m = map(lambda collector: collector.__class__, collectors)
        self.assertNotIn(DeprecatedMethodUsage, m, "DeprecatedMethodUsage should not be added in fast mode")


# noinspection PyTypeChecker
class TestJIRADirectoryScanModulesDescription(TestCase):
    def setUp(self):
        self.plugins_utility = Mock()
        self.logger = Mock()
        return_self = lambda _: self.maven

        self.maven = Mock(option=return_self, phase=return_self)

        # this is a hack to implement callable object, whe this is called then executed method on mock is invoked
        self.maven.__class__.__call__ = types.MethodType(lambda ignore, _: self.maven.executed() or 0, self.maven)
        self.maven_creator = lambda: self.maven

    def test_prepare_environment_without_bundled_plugins_list_packages_project(self):
        self.plugins_utility.expect_bundled_plugins_list_ok([], toReturn=False)
        module_desc = BundledPluginsModulesDescription(False, self.plugins_utility, self.maven_creator)
        # when
        module_desc.prepare_environment(self.logger)

        # then
        self.assertTrue(self.maven.verify_executed(), 'Expected maven have been executed')


    def test_prepare_environment_with_bundled_plugins_list_skips_packaging(self):
        # having
        module_desc = BundledPluginsModulesDescription(False, self.plugins_utility, self.maven_creator)
        self.plugins_utility.expect_bundled_plugins_list_ok([], toReturn=True)
        # when
        module_desc.prepare_environment(self.logger)
        # then
        self.assertFalse(self.maven.verify_executed(), 'Maven should not be called')

    def test_prepare_environment_throws_exception_when_cannot_run_maven(self):
        module_desc = BundledPluginsModulesDescription(False, self.plugins_utility, self.maven_creator)
        self.maven.__class__.__call__ = types.MethodType(lambda ignore, _: self.maven.executed() or 1, self.maven)

        self.assertRaises(MavenExecutionException, module_desc.prepare_environment, self.logger)

    def test_checks_are_not_run_when_fast_mode_and_no_bp_list(self):
        #having
        self.plugins_utility.expect_bundled_plugins_list_ok([], toReturn=False)
        module_desc = BundledPluginsModulesDescription(True, self.plugins_utility, self.maven_creator)
        self.plugins_utility.expect_get_all_bundled_plugins_paths(toReturn=['plugin1', 'plugin2'])
        module_desc.prepare_environment(self.logger)

        #when
        modules = module_desc.measured_modules()
        #then
        self.assertListEqual(list(modules), [], 'There should be no modules in fast mode ')
        self.assertFalse(self.maven.verify_executed(), 'Expected maven have not executed')

    def test_checks_are_run_when_fast_mode_and_bp_list_exists(self):
        #having

        self.plugins_utility.expect_bundled_plugins_list_ok([], toReturn=True)
        module_desc = BundledPluginsModulesDescription(True, self.plugins_utility, self.maven_creator)
        plugin_modules = ['plugin1', 'plugin2']
        self.plugins_utility.expect_get_all_bundled_plugins_paths(toReturn=plugin_modules)
        module_desc.prepare_environment(self.logger)

        #when
        modules = module_desc.measured_modules()
        #then
        self.assertListEqual(list(modules), plugin_modules, 'There should modules when in fast mode ')
        self.assertFalse(self.maven.verify_executed(), 'Expected maven have not been executed')


class TestModulesDescriptionsConsistency(TestCase):
    def test_modules_descriptors_do_not_define_the_same_metrics(self):
        self.maxDiff = None
        class_predicate = lambda cls: (inspect.isclass(cls)
                                       and issubclass(cls, ModulesDescription.ModulesDescription)
                                       and cls != ModulesDescription.ModulesDescription)

        collectors = {id(collector): collector.key for class_name, modules_description_class in
                      inspect.getmembers(ModulesDescription, class_predicate)
                      for collector in modules_description_class(False).get_collectors()}

        self.assertListEqual(sorted(collectors.values()), sorted(set(collectors.values())),
                             'Collectors must have unique keys in all defined modules')

    def test_collectors_do_not_contain_comma_in_key(self):
        self.maxDiff = None
        class_predicate = lambda cls: (inspect.isclass(cls)
                                       and issubclass(cls, ModulesDescription.ModulesDescription)
                                       and cls != ModulesDescription.ModulesDescription)

        collectors = [collector for class_name, modules_description_class in
                      inspect.getmembers(ModulesDescription, class_predicate)
                      for collector in modules_description_class(False).get_collectors()]

        for collector in collectors:
            self.assertNotIn(',', collector.key)
