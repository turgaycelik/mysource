import platform
from unittest import TestCase
import unittest
from catalina.WorkspaceLayout import WorkspaceBuilder

prev_platform_function = platform.system

class TestWorkspaceLayout(TestCase):
    @classmethod
    def tearDownClass(cls):
        platform.system = prev_platform_function

    def test_get_executables_for_tomcat6_adds_setclasspath_sh_on_linux(self):
        layout = self.__getLayout('tcdir', 'tomcat6')
        platform.system = lambda: 'Linux'
        executables = layout.tomcat_executables()
        self.assertListEqual(executables, ['tcdir/apache-tomcat-6.0.35/bin/catalina.sh',
                                           'tcdir/apache-tomcat-6.0.35/bin/setclasspath.sh'])

    def test_get_executables_for_tomcat7_does_not_add_setclasspath_sh_on_linux(self):
        layout = self.__getLayout('tcdir', 'tomcat7')
        platform.system = lambda: 'Linux'
        executables = layout.tomcat_executables()
        self.assertListEqual(executables, ['tcdir/apache-tomcat-7.0.47/bin/catalina.sh'])

    def test_get_executables_for_tomcat6_adds_setclasspath_sh_on_windows(self):
        layout = self.__getLayout('tcdir', 'tomcat6')
        platform.system = lambda: 'Windows'
        executables = layout.tomcat_executables()
        self.assertListEqual(executables, ['tcdir/apache-tomcat-6.0.35/bin/catalina.bat',
                                           'tcdir/apache-tomcat-6.0.35/bin/setclasspath.bat'])

    def test_get_executables_for_tomcat7_does_not_add_setclasspath_sh_on_windows(self):
        layout = self.__getLayout('tcdir', 'tomcat7')
        platform.system = lambda: 'Windows'
        executables = layout.tomcat_executables()
        self.assertListEqual(executables, ['tcdir/apache-tomcat-7.0.47/bin/catalina.bat'])

    def __getLayout(self, tomcatDir, tomcatVersion):
        layout = WorkspaceBuilder.buildLayout(tomcatDir, tomcatVersion, './jirahome', 'work', False)
        layout._WorkspaceLayout__existing_dir = lambda path: path
        return layout

if __name__ == '__main__':
    unittest.main(verbosity=5)
