package com.atlassian.jira.webtests;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.StringReader;

public class AbstractTestIssueNavigatorXmlView extends AbstractTestIssueNavigatorView
{
    public AbstractTestIssueNavigatorXmlView(String name)
    {
        super(name);
    }

    public String assertAndGetLinkToFilterWithId(String filterId) throws DocumentException
    {
        String previousViewUrl = getLinkToIssueNavigator();
        //check that the link does not contain the reset parameter
        assertTrue(previousViewUrl.indexOf("reset=") == -1);
        //check that the link has the correct filter id
        assertEquals(getEnvironmentData().getBaseUrl().toString() + "/secure/IssueNavigator.jspa?requestId=" + filterId, previousViewUrl);
        return previousViewUrl;
    }

    public String getLinkToIssueNavigator() throws DocumentException
    {
        SAXReader saxReader = new SAXReader();
        org.dom4j.Document xmlResponse = saxReader.read(new StringReader(getDialog().getResponseText()));
        Element linkElement = xmlResponse.getRootElement().element("channel").element("link");
        // Do NOT trim the text, as if we do have spaces in the text, then some clients (e.g. Confluence's JIRA issues macro
        // will fail). So we need to do assertions against the raw value. JRA-16175
        return linkElement.getText();
    }
}
