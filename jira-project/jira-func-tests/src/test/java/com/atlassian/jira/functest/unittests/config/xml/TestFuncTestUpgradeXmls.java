package com.atlassian.jira.functest.unittests.config.xml;

import com.atlassian.jira.functest.config.CheckOptions;
import com.atlassian.jira.functest.config.CheckOptionsUtils;
import com.atlassian.jira.functest.config.ConfigFile;
import com.atlassian.jira.functest.config.xml.FuncTestsXmlResources;
import com.atlassian.jira.functest.config.xml.UpgradeXmlVerifier;
import org.dom4j.Document;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * Checks known upgrade XMLs for a suppress upgrade statement.
 *
 * @since v6.0
 * @see com.atlassian.jira.functest.config.xml.UpgradeXmlVerifier
 */
public class TestFuncTestUpgradeXmls
{

    @Test
    public void upgradeXmlsMustContainSuppressUpgradeCheck()
    {
        new UpgradeXmlVerifier(FuncTestsXmlResources.getXmlLocation()).verify();
    }

    /**
     * Run this test to automatically add the suppress upgrade check. It works for both XML and zip
     * restore file formats.
     *
     */
    @Test
    @Ignore("This test should only be run manually")
    public void fixXml()
    {
        final ConfigFile file = ConfigFile.create(new File(FuncTestsXmlResources.getXmlLocation(), "ActiveObjectsBadData.zip"));
        final Document doc = file.readConfig();
        CheckOptions options = CheckOptionsUtils.parseOptions(doc);
        options = CheckOptionsUtils.disableIn(options, "upgrade");
        CheckOptionsUtils.writeOptions(doc, options);
        file.writeFile(doc);
    }
}
