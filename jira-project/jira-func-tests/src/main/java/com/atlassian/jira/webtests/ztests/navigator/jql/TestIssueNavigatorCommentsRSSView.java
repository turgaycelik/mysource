package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Test of the Search Request View for Comments and using JQL.
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestIssueNavigatorCommentsRSSView extends AbstractJqlFuncTest
{
    private List<String> homosapienIssueKeys = CollectionBuilder.newBuilder("HSP-10", "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-3", "HSP-2","HSP-1").asList();
    private List<String> monkeyIssueKeys     = CollectionBuilder.newBuilder("MKY-10", "MKY-9", "MKY-8", "MKY-7", "MKY-6", "MKY-5", "MKY-4", "MKY-3", "MKY-2","MKY-1").asList();

    private XPathFactory factory = XPathFactory.newInstance();

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestIssueNavigatorCommentsRSSView.xml");
    }

    public void testIssueNavigatorCommentsRSSView() throws Exception
    {
        navigation.login(FRED_USERNAME);

        List<Comment> comments = CollectionBuilder.newBuilder(new Comment("", "A comment from Fred", "Fred"),  new Comment("", "I was here... - You not! :P", ADMIN_FULLNAME), new Comment("", "Again a comment for issue", ADMIN_FULLNAME), new Comment("", "Another comment for issue", ADMIN_FULLNAME), new Comment("", "A comment for issue", ADMIN_FULLNAME) ).asList();
        for (String homosapienIssueKey : homosapienIssueKeys)
        {
            _executeCommentsRSSView("issue = \"" + homosapienIssueKey + "\"");
            _verifyIssueComments(comments);
        }

        for (String monkeyIssueKey : monkeyIssueKeys)
        {
            _executeCommentsRSSView("issue = \"" + monkeyIssueKey + "\"");
            _verifyIssueComments(comments);
        }

        List<String> queries = new ArrayList<String>();

        queries.add("affectedVersion IS EMPTY AND assignee IS EMPTY AND component IS EMPTY AND description IS EMPTY AND environment IS EMPTY AND fixVersion IS EMPTY AND level IS EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY");
        queries.add("affectedVersion IS NOT EMPTY AND assignee IS EMPTY AND component IS EMPTY AND description IS EMPTY AND environment IS EMPTY AND fixVersion IS EMPTY AND level IS EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY");
        queries.add("assignee IS NOT EMPTY AND component IS EMPTY AND description IS EMPTY AND environment IS EMPTY AND fixVersion IS EMPTY AND level IS EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY");
        queries.add("component IS NOT EMPTY AND description IS EMPTY AND environment IS EMPTY AND fixVersion IS EMPTY AND level IS EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY");
        queries.add("description IS NOT EMPTY AND environment IS EMPTY AND fixVersion IS EMPTY AND level IS EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY");
        queries.add("environment IS NOT EMPTY AND fixVersion IS EMPTY AND level IS EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY");
        queries.add("fixVersion IS NOT EMPTY AND level IS EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY");
        queries.add("level IS NOT EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY");
        queries.add("originalEstimate IS NOT EMPTY AND reporter IS EMPTY");
        queries.add("reporter IS NOT EMPTY");

        
        assertEquals(homosapienIssueKeys.size(), monkeyIssueKeys.size());

        for (int i = 0; i < queries.size(); i++)
        {
            String jqlQuery = queries.get(i);
            _executeCommentsRSSView(jqlQuery);

            List<Comment> tempComment = new ArrayList<Comment>();

            String hspKey    =  homosapienIssueKeys.get(homosapienIssueKeys.size() - i - 1);
            String monkeyKey =  monkeyIssueKeys.get(monkeyIssueKeys.size() - i - 1);
            tempComment.add(new Comment(hspKey,     "A comment from Fred", "Fred"));
            tempComment.add(new Comment(monkeyKey,  "A comment from Fred", "Fred"));
            tempComment.add(new Comment(hspKey, "I was here... - You not! :P", ADMIN_FULLNAME));
            tempComment.add(new Comment(monkeyKey, "I was here... - You not! :P", ADMIN_FULLNAME));
            tempComment.add(new Comment(hspKey, "Again a comment for issue", ADMIN_FULLNAME));
            tempComment.add(new Comment(monkeyKey, "Again a comment for issue", ADMIN_FULLNAME));
            tempComment.add(new Comment(hspKey, "Another comment for issue", ADMIN_FULLNAME));
            tempComment.add(new Comment(monkeyKey, "Another comment for issue", ADMIN_FULLNAME));
            tempComment.add(new Comment(hspKey, "A comment for issue", ADMIN_FULLNAME));
            tempComment.add(new Comment(monkeyKey, "A comment for issue", ADMIN_FULLNAME));
            _verifyIssueComments(tempComment);
        }

        navigation.login(ADMIN_USERNAME);

        for (int i = 0; i < queries.size(); i++)
        {
            String jqlQuery = queries.get(i);
            _executeCommentsRSSView(jqlQuery);

            List<Comment> tempComment = new ArrayList<Comment>();

            String hspKey    =  homosapienIssueKeys.get(homosapienIssueKeys.size() - i - 1);
            String monkeyKey =  monkeyIssueKeys.get(monkeyIssueKeys.size() - i - 1);
            tempComment.add(new Comment(hspKey, "Not visible for Fred", ADMIN_FULLNAME));
            tempComment.add(new Comment(monkeyKey, "Not visible for Fred", ADMIN_FULLNAME));
            tempComment.add(new Comment(hspKey,     "A comment from Fred", "Fred"));
            tempComment.add(new Comment(monkeyKey,  "A comment from Fred", "Fred"));
            tempComment.add(new Comment(hspKey, "I was here... - You not! :P", ADMIN_FULLNAME));
            tempComment.add(new Comment(monkeyKey, "I was here... - You not! :P", ADMIN_FULLNAME));
            tempComment.add(new Comment(hspKey, "Again a comment for issue", ADMIN_FULLNAME));
            tempComment.add(new Comment(monkeyKey, "Again a comment for issue", ADMIN_FULLNAME));
            tempComment.add(new Comment(hspKey, "Another comment for issue", ADMIN_FULLNAME));
            tempComment.add(new Comment(monkeyKey, "Another comment for issue", ADMIN_FULLNAME));
            tempComment.add(new Comment(hspKey, "A comment for issue", ADMIN_FULLNAME));
            tempComment.add(new Comment(monkeyKey, "A comment for issue", ADMIN_FULLNAME));
            _verifyIssueComments(tempComment);
        }

        //NOT parseable
        try
        {
            tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);

            _executeCommentsRSSView("sdfdsfBLJLSKJLSKJF");
            final int responseCode = tester.getDialog().getResponse().getResponseCode();
            assertEquals(400, responseCode);
        }
        finally
        {
            tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(true);
        }

        //NOT valid
        try
        {
            tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);

            _executeCommentsRSSView("issue = \"BLJLSKJLSKJF\"");
            final int responseCode = tester.getDialog().getResponse().getResponseCode();
            assertEquals(400, responseCode);
        }
        finally
        {
            tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(true);
        }
    }

    private void _verifyIssueComments(final List<Comment> comments)
            throws IOException, SAXException, ParserConfigurationException, XPathExpressionException
    {
        final String xpathExpr = "//rss/channel/item";
        String responseText = tester.getDialog().getResponse().getText();
        Document doc = XMLUnit.buildControlDocument(responseText);
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile(xpathExpr);
        NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        assertEquals(comments.size(), nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            final Node node = nodeList.item(i);
            final TestIssueNavigatorCommentsRSSView.Comment comment = comments.get(i);

            final String issueKey = comment.getIssueKey();
            if(issueKey != null && issueKey.length() > 0)
            {
                final Node titleNode = node.getChildNodes().item(1);
                String title = titleNode.getFirstChild().getNodeValue();
                assertEquals("Incorrect issue key in title! Expected: " + issueKey + " Title:" + title, true, title.contains(issueKey) );
            }

            final Node authorNode = node.getChildNodes().item(7);
            String author = authorNode.getFirstChild().getNodeValue();
            assertEquals("Incorrect Author. Expected: " + author, true, author.contains(comment.getAuthor()));
            
            final Node descriptionNode = node.getChildNodes().item(9);
            final String description = descriptionNode.getFirstChild().getNodeValue();
            assertEquals("Incorrect comment (order)!", true, description.contains(comment.getDescription() ) );
        }
    }

    private void _executeCommentsRSSView(String jqlQuery) throws UnsupportedEncodingException
    {
        final String encodedQuery = URLEncoder.encode(jqlQuery, "UTF-8");
        final String url = "/sr/jira.issueviews:searchrequest-comments-rss/temp/SearchRequest.xml?jqlQuery=" + encodedQuery + "&tempMax=1000";
        tester.gotoPage(url);
    }

    class Comment
    {
        private final String issueKey;
        private final String description;
        private final String author;

        Comment(String issueKey, String description, final String author)
        {
            this.issueKey = issueKey;
            this.description = description;
            this.author = author;
        }

        public String getIssueKey()
        {
            return issueKey;
        }

        public String getDescription()
        {
            return description;
        }

        public String getAuthor()
        {
            return author;
        }
    }
    
}
