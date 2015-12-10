import os
from unittest import TestCase
from JmakeFindbugs import Findbugs
from JmakeUnitTest import UnitTest
from utils.Mocks import Mock
from maven.Maven import Maven3

def provideM3Home(cls):
    if not 'M3_HOME' in os.environ:
        os.environ['M3_HOME'] = '/usr/bin/mvn3'
        cls.tearDownClass = lambda: os.environ.pop('M3_HOME')

class MockExecutor():

    def __init__(self):
        self.list = []
        self.post_list = []

    def append(self, item):
        self.list.append(item)

    def append_post(self, item):
        self.post_list.append(item)

    def __len__(self):
        return len(self.list)

    def __iter__(self):
        return self.list

    def __getitem__(self, item):
        return self.list[item]

class JmakeUnitTest(TestCase):
    @classmethod
    def setUpClass(cls):
        provideM3Home(cls)

    def setUp(self):
        self.jmake_ut = UnitTest(Mock())
        self.executor = MockExecutor()

    def test_verify_dependencies_is_added_always(self):
        self.jmake_ut(Mock(vdep=False, skip_bp=False, findbugs=False), self.executor)

        self.assertEqual(len(self.executor), 2)
        maven = self.executor[1]
        self.assertIn('verifyDependencies', maven.properties)

    def test_verify_dependencies_works_with_compile(self):
        self.jmake_ut(Mock(vdep=True, skip_bp=False, findbugs=False), self.executor)

        self.assertEqual(len(self.executor), 2)
        maven = self.executor[1]
        self.assertIn('verifyDependencies', maven.properties)
        self.assertIn('compile', maven.phases)

    def test_skip_bundled_plugins(self):
        self.jmake_ut(Mock(vdep=False, skip_bp=True, findbugs=False), self.executor)

        self.assertEqual(len(self.executor), 2)
        maven = self.executor[1]
        self.assertIn('verifyDependencies', maven.properties)
        for project in maven.projects:
            self.assertNotIn('bundled-plugins', project)

    def test_findbugs_switch(self):
        process_utils = Mock()
        process_utils.default_check_output(b'Apache Maven: 3.0.5')
        self.jmake_ut = UnitTest(Mock(), process_utils=process_utils)
        self.jmake_ut(Mock(vdep=False, skip_bp=False, findbugs=True), self.executor)

        self.assertEqual(len(self.executor), 2)
        maven = self.executor[1]
        self.assertIn('verifyDependencies', maven.properties)
        self.assertIsInstance(maven.maven_version, Maven3)
        self.assertIn('findbugs', maven.profiles)


class JmakeFindbugsTest(TestCase):
    @classmethod
    def setUpClass(cls):
        provideM3Home(cls)

    def setUp(self):
        process_utils = Mock()
        process_utils.default_check_output(b'Apache Maven: 3.0.5')
        self.jmake_fb = Findbugs(process_utils=process_utils)
        self.executor = []

    def test_findbugs_needs_maven3(self):
        self.jmake_fb(Mock(vdep=False, skip_bp=False, findbugs=False), self.executor)

        self.assertEqual(len(self.executor), 1)
        maven = self.executor[0]
        self.assertIsInstance(maven.maven_version, Maven3)
        self.assertIn('findbugs', maven.profiles)
        self.assertIn('process-classes', maven.phases)

    def test_skip_bundled_plugins(self):
        self.jmake_fb(Mock(vdep=False, skip_bp=True, findbugs=False), self.executor)

        self.assertEqual(len(self.executor), 1)
        maven = self.executor[0]
        for project in maven.projects:
            self.assertNotIn('bundled-plugins', project)


