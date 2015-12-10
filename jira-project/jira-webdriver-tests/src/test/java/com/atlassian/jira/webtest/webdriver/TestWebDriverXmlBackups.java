package com.atlassian.jira.webtest.webdriver;

import com.atlassian.jira.functest.config.ConfigurationChecker;
import com.atlassian.jira.functest.config.xml.FuncTestsXmlResources;
import junit.framework.TestCase;

import java.io.File;

/**
 * Test to ensure that Selenium test XMLs are clean.
 *
 * @since v4.0
 */
public class TestWebDriverXmlBackups extends TestCase
{

    public static final String WEBDRIVER_TEST_XML_RESOURCES = "xml/webdriver_test_xml_resources";

    public void testXmlData() throws Exception
    {
        final ConfigurationChecker configurationChecker = ConfigurationChecker.createDefaultChecker(xmlsLocation());
        final ConfigurationChecker.CheckResult checkResult = configurationChecker.check();

        if (checkResult.hasErrors())
        {
            fail("Func Test XML contains errors. Check out https://extranet.atlassian.com/x/GAW7b for details on what to do.\n" + checkResult.getFormattedMessage());
        }
    }

    private File xmlsLocation()
    {
        return FuncTestsXmlResources.getXmlLocation(WEBDRIVER_TEST_XML_RESOURCES);
    }
}
