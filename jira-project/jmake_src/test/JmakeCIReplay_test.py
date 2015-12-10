import os
from unittest import TestCase
import xml.etree.ElementTree as XML

from JmakeCIReplay import JbacFailures
from utils.Mocks import Mock


class TestJbacFailureDetection(TestCase):

    def setUp(self):
        self.build_name = 'PROJ-ABUILD'
        self.build_number = '1234'
        self.url = Mock()
        self.xml = Mock()

        self.obj = JbacFailures(Mock(),
                                self.build_name,
                                self.build_number,
                                urlutils = self.url,
                                xml = self.xml,
                                auth = Mock(login = 'login', password = 'pass'))

    def test_extract_plan_name_from_stacktrace(self):

        name = self.obj._JbacFailures__extract_plan_name('Exception: something very $*#(@-3$*()#@-$#@ MY-JBAC-BUILD-4432: %$#*(%')
        self.assertEqual(name, 'MY-JBAC-BUILD-4432')

        name = self.obj._JbacFailures__extract_plan_name('THIS IS A JUST-ONE-PLAN-1111 \n MULTILINE \n STACKTRACE \n AND-WITH-PLAN-3333')
        self.assertEqual(name, 'JUST-ONE-PLAN-1111')

        name = self.obj._JbacFailures__extract_plan_name('SECOND LINE IS  \n NOT-ACTUALLY-VALID-3213 ')
        self.assertEqual(name, None)

        name = self.obj._JbacFailures__extract_plan_name('TEST-WITH-JUST-WORDS no-lower-case-323 TOO-MANY-TOKENS-DUDE-233 A-B-C-4')
        self.assertEqual(name, 'A-B-C-4')

    def test_url_contains_buildname_and_number(self):

        url = self.obj._JbacFailures__build_url()
        self.assertIn(self.build_name, url)
        self.assertIn(self.build_number, url)

    def test_detect_failures(self):

        datafile = os.sep.join(['.', 'jmake_src', 'testdata', 'bamboo-reply-01.xml'])
        try:
            with open(datafile, 'r') as f:
                text = f.read()
                self.url.expect_read(self.obj._JbacFailures__build_url(), 'login', 'pass', toReturn = text)
                self.xml.expect_parse_string(text, toReturn = XML.fromstring(text))
        except IOError:
            self.fail('Failed to access test data: ' + datafile)

        # run the test by invoking iterator on the object:
        results = [ tpl for tpl in self.obj ]

        self.assertIn( ('SPECIAL-BAMBOO-AGENT-9000', 'com.atlassian.my.class.failedTest'), results )
        self.assertIn( ('NOT-QUITE-SURE-2134', 'com.atlassian.not.my.class.anotherFail'), results )



