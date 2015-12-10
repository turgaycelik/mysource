import os
from unittest import TestCase
import unittest
from Logger import LOG
from utils.Mocks import Mock
from catalina.ContextPreparer import ContextPreparer
from catalina.WorkspaceLayout import MockLayout
from utils.FileUtils import FileUtils

SERVER_XML_VARIABLES = ['${jira.port}', '${jira.shutdown.port}', '${jira.context}', '${jira.docbase}', '${tomcat.workdir}']

class TestContextPreparer(TestCase):
    def setUp(self):
        self.layout = MockLayout(False)
        LOG.set_none()


    def tearDown(self):
        self.layout.remove()


    def test_server_xml_is_filtered(self):
        file_utils = FileUtils()
        #        having
        args = Mock(port = 1234, sh_port = 8123, jira_context = 'strange', layout = self.layout, ssl = False)
        context_preparer = ContextPreparer(args)
        web_wml_content = 'just testing web wml'

        with open(os.path.join(self.layout.tomcat_conf_src(), ContextPreparer.WEB_XML_NAME), 'w') as web_xml:
            web_xml.write(web_wml_content)
        with open(os.path.join(self.layout.tomcat_conf_src(), ContextPreparer.SERVER_XML_NAME), 'w') as server_xml:
            for line in SERVER_XML_VARIABLES: server_xml.write(line + '\n')

        #       when
        context_preparer(LOG)
        #       then

        web_xml_dest = os.path.join(self.layout.tomcat_conf_dest(), ContextPreparer.WEB_XML_NAME)
        self.assertTrue(os.path.lexists(web_xml_dest), 'Expected %s file to be created' % web_xml_dest)
        self.assertListEqual(file_utils.read_lines(web_xml_dest), [web_wml_content])
        server_xml_dest = os.path.join(self.layout.tomcat_conf_dest(), ContextPreparer.SERVER_XML_NAME)
        self.assertTrue(os.path.lexists(server_xml_dest), 'Expected %s file to be created' % server_xml_dest)
        self.assertListEqual(file_utils.read_lines(server_xml_dest),
            [str(args.port), str(args.sh_port), args.jira_context, self.layout.jira_webapp_dir(), self.layout.tomcat_work_dir()])

if __name__ == '__main__':
    unittest.main()