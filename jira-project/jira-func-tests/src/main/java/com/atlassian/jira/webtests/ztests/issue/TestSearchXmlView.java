package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.navigator.NavigatorSearch;
import com.atlassian.jira.functest.framework.sharing.GroupTestSharingPermission;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import net.sourceforge.jwebunit.WebTester;

import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.hamcrest.core.StringContains;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR })
public class TestSearchXmlView extends FuncTestCase
{
    // should be double slash here, but http unit will strip it
    private static final String HTML_FRAGMENT = "/--><html><body>hi</body>;<!--";
    private static final String HTML_FRAGMENT_ENCODED = StringEscapeUtils.escapeXml(HTML_FRAGMENT);

    public void testFilterAllIssues() throws IOException, DocumentException
    {
        administration.restoreData("TestXMLIssueView.xml");
        long filterId = createFilterForAllIssues();
        tester.gotoPage("/sr/jira.issueviews:searchrequest-xml/" + filterId + "/SearchRequest-" + filterId + ".xml?tempMax=200");
        Document doc = getDocument(tester);
        XPath xpath = DocumentHelper.createXPath("//item/title");

        List nodes = xpath.selectNodes(doc);
        assertEquals(3, nodes.size());
        assertTrue(((Element) nodes.get(0)).getText().startsWith("[MKY-1]"));
        assertTrue(((Element) nodes.get(1)).getText().startsWith("[HSP-2]"));
        assertTrue(((Element) nodes.get(2)).getText().startsWith("[HSP-1]"));
    }

    public void testFilterAllIssuesWithCustomSort() throws IOException, DocumentException
    {
        administration.restoreData("TestXMLIssueView.xml");
        long filterId = createFilterForAllIssues();
        tester.gotoPage("/sr/jira.issueviews:searchrequest-xml/" + filterId + "/SearchRequest-" + filterId + ".xml?tempMax=200&sorter/field=issuekey&sorter/order=ASC");
        Document doc = getDocument(tester);
        XPath xpath = DocumentHelper.createXPath("//item/title");

        List nodes = xpath.selectNodes(doc);
        assertEquals(3, nodes.size());
        assertTrue(((Element) nodes.get(0)).getText().startsWith("[HSP-1]"));
        assertTrue(((Element) nodes.get(1)).getText().startsWith("[HSP-2]"));
        assertTrue(((Element) nodes.get(2)).getText().startsWith("[MKY-1]"));
    }

    public void testFilterAllIssuesWithCustomSortAndPaging() throws IOException, DocumentException
    {
        administration.restoreData("TestXMLIssueView.xml");
        long filterId = createFilterForAllIssues();
        tester.gotoPage("/sr/jira.issueviews:searchrequest-xml/" + filterId + "/SearchRequest-" + filterId + ".xml?tempMax=2&sorter/field=issuekey&sorter/order=ASC&pager/start=1");
        Document doc = getDocument(tester);
        XPath xpath = DocumentHelper.createXPath("//item/title");

        List nodes = xpath.selectNodes(doc);
        assertEquals(2, nodes.size());
        assertTrue(((Element) nodes.get(0)).getText().startsWith("[HSP-2]"));
        assertTrue(((Element) nodes.get(1)).getText().startsWith("[MKY-1]"));
    }

    public void testSearchRequestXMLViewEncodesQueryString() throws IOException
    {
        administration.restoreBlankInstance();
        String linkKey = navigation.issue().createIssue("homosapien", "Bug", "Test issue to link to");

        tester.gotoPage(String.format("/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?tempMax=10000&jqlQuery=&%s", HTML_FRAGMENT));

        String responseText = tester.getDialog().getResponse().getText();

        assertThat(responseText, StringContains.containsString(HTML_FRAGMENT_ENCODED));
        assertThat(responseText, not(StringContains.containsString(HTML_FRAGMENT)));
    }

    private long createFilterForAllIssues()
    {
        return Long.parseLong(backdoor.filters().createFilter("", "All Issues"));
    }

    private Document getDocument(WebTester tester) throws IOException, DocumentException
    {
        SAXReader reader = new SAXReader();
        return reader.read(tester.getDialog().getResponse().getInputStream());
    }
}
