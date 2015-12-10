package com.atlassian.jira.functest.framework.parser;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.LocatorEntry;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A parser of the System Info Page
 *
 * @since v3.13
 */
public class SystemInfoParserImpl extends AbstractFuncTestUtil implements SystemInfoParser
{
    private final Navigation navigation;

    public SystemInfoParserImpl(WebTester tester, Navigation navigation)
    {
        this(tester, null, 2, navigation);
    }

    public SystemInfoParserImpl(WebTester tester, JIRAEnvironmentData environmentData, int logIndentLevel, Navigation navigation)
    {
        super(tester, environmentData, logIndentLevel);
        this.navigation = navigation;
    }

    public SystemInfo getSystemInfo()
    {
        tester.gotoPage("secure/admin/jira/ViewSystemInfo.jspa");
        Locator loc = new XPathLocator(tester, "//table[@id='system_info_table']/tr/td");
        int count = 0;
        String key = "";
        final Map sysInfoMap = new HashMap();
        for (Iterator iterator = loc.iterator(); iterator.hasNext();)
        {
            LocatorEntry entry = (LocatorEntry) iterator.next();
            Element td = (Element) entry.getNode();
            if (td.hasAttribute("colspan")) {
                continue;
            }

            if (count % 2 == 0)
            {
                key = entry.getText();
            }
            else
            {
                String value = entry.getText().trim();
                sysInfoMap.put(key, value);
            }
            count++;
        }

        return new SystemInfo()
        {
            public String getAppServer()
            {
                return getProperty("Application Server Container");
            }

            public String getJavaVersion()
            {
                return getProperty("JVM Version");
            }

            public String getDatabaseType()
            {
                return getProperty("Database type");
            }

            public String getSystemEncoding()
            {
                return getProperty("System Encoding");
            }

            public String getProperty(final String displayedKey)
            {
               String s =  (String) sysInfoMap.get(displayedKey);
               return (s == null ? "" : s);
            }
        };
    }
}
