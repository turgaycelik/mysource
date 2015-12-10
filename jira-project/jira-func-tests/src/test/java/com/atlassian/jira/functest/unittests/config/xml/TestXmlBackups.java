package com.atlassian.jira.functest.unittests.config.xml;

import com.atlassian.jira.functest.config.ConfigurationChecker;
import com.atlassian.jira.functest.config.xml.FuncTestsXmlResources;
import junit.framework.TestCase;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Test to ensure that func test XMLs are clean.
 *
 * @since v4.0
 */
public class TestXmlBackups extends TestCase
{
    public void testXmlData() throws Exception
    {
        final ConfigurationChecker configurationChecker = ConfigurationChecker.createDefaultChecker(xmlsLocation());
        final ConfigurationChecker.CheckResult checkResult = configurationChecker.check();

        if (checkResult.hasErrors())
        {
            fail("Func Test XML contains errors. Check out https://extranet.atlassian.com/x/GAW7b for details on what to do.\n"
                    + checkResult.getFormattedMessage());
        }
    }

    private File xmlsLocation()
    {
        return FuncTestsXmlResources.getXmlLocation();
    }
}
