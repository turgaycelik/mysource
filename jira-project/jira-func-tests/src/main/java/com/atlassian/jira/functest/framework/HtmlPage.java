package com.atlassian.jira.functest.framework;

import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.util.url.URLUtil;
import com.atlassian.jira.webtests.table.HtmlTable;
import com.meterware.httpunit.WebLink;
import net.sourceforge.jwebunit.WebTester;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Returns information about the current HTML page in the func test.
 *
 * @since v4.0
 */
public class HtmlPage
{
    private WebTester tester;
    private Footer footer;

    public HtmlPage(final WebTester tester)
    {
        this.tester = tester;
    }

    public HtmlTable getHtmlTable(final String tableID)
    {
        return HtmlTable.newHtmlTable(tester, tableID);
    }

    public boolean isLinkPresentWithExactText(final String text)
    {
        final WebLink[] webLinks = getLinksWithExactText(text);
        return webLinks.length > 0;
    }

    public WebLink[] getLinksWithExactText(final String text)
    {
        try
        {
            return tester.getDialog().getResponse().getMatchingLinks(WebLink.MATCH_TEXT, text);
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
    }

    public String getXsrfToken()
    {
        XPathLocator locator = new XPathLocator(tester, "//meta[@id='atlassian-token']");
        final Element meta = (Element) locator.getNode();
        if (meta != null)
        {
            return URLUtil.encode(meta.getAttribute("content"));
        }
        return "";
    }

    public String getFreshXsrfToken()
    {
        // quick to load and parse and causes the XSRF token to be resynchronised
        tester.beginAt("/secure/ViewKeyboardShortcuts.jspa");
        return getXsrfToken();
    }

    /**
     * Adds a token to a given url in the FIRST position
     *
     * @param url the url in question
     * @return the url with the token in place
     */
    public String addXsrfToken(String url)
    {
        return URLUtil.addXsrfToken(getXsrfToken(), url);
    }

    public Footer getFooter()
    {
        if (footer == null)
            footer = new FooterImpl(tester.getDialog().getResponse());
        return footer;
    }
}
