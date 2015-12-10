package com.atlassian.jira.webtest.webdriver;

import com.atlassian.jira.functest.config.xml.FuncTestsXmlResources;
import com.atlassian.jira.functest.config.xml.UpgradeXmlVerifier;
import org.junit.Test;

import java.io.File;

/**
 * Test upgrade XMLs for WebDriver
 *
 * @since v6.0
 * @see UpgradeXmlVerifier
 */
public class TestWebDriverUpgradeXmls
{

    @Test
    public void upgradeXmlsMustContainSuppressUpgradeCheck()
    {
        new UpgradeXmlVerifier(xmls()).verify();
    }

    private File xmls()
    {
        return FuncTestsXmlResources.getXmlLocation(TestWebDriverXmlBackups.WEBDRIVER_TEST_XML_RESOURCES);
    }
}
