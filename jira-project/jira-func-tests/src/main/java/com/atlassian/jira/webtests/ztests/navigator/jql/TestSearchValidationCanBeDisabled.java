package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.RestApiClient;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestSearchValidationCanBeDisabled extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        backdoor.restoreBlankInstance();
        backdoor.getTestkit().issues().createIssue("HSP", "my issue");
    }

    public void testIssueNavXmlView() throws Exception
    {
        final String hsp1 = "HSP-1"; // exists
        final String hsp123 = "HSP-123"; // doesn't exist
        final String jql = String.format("key in (%s, %s)", hsp1, hsp123);

        SearchRequestClient client = new SearchRequestClient();

        ClientResponse withValidationResponse = client.get(jql, true);
        assertThat(withValidationResponse.getStatus(), equalTo(400));
        withValidationResponse.close();

        ClientResponse noValidationResponse = client.get(jql, false);
        assertThat(noValidationResponse.getStatus(), equalTo(200));

        Document responseXml = parseResponseXml(noValidationResponse);
        assertThat(responseXml, containsIssueWithKey(hsp1));
        assertThat(responseXml, not(containsIssueWithKey(hsp123)));
    }

    private Document parseResponseXml(final ClientResponse response)
    {
        InputStream is = response.getEntityInputStream();
        try
        {
            is.reset();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            return builder.parse(is);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error parsing XML", e);
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                // damn.
            }
        }
    }

    @Override
    protected boolean isDumpHTML()
    {
        return false;
    }

    static Matcher<Document> containsIssueWithKey(String issueKey)
    {
        return new ContainsIssueWithKeyMatcher(issueKey);
    }

    /**
     * Hamcrest matcher for the issue nav XML view.
     */
    private static class ContainsIssueWithKeyMatcher extends TypeSafeMatcher<Document>
    {
        private final String issueKey;

        public ContainsIssueWithKeyMatcher(final String issueKey)
        {
            this.issueKey = issueKey;
        }

        @Override
        protected boolean matchesSafely(Document doc)
        {
            try
            {
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();
                XPathExpression expr = xpath.compile(String.format("/rss/channel/item/key[contains(text(), '%s')]", issueKey));

                return issueKey.equals(expr.evaluate(doc));
            }
            catch (Exception e)
            {
                throw new RuntimeException("Error parsing XML", e);
            }
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("a response containing ").appendValue("HSP-1");
        }
    }

    /**
     * Client for testing the issue nav XML view.
     */
    private class SearchRequestClient extends RestApiClient<SearchRequestClient>
    {
        public SearchRequestClient()
        {
            super(getEnvironmentData());
        }

        public ClientResponse get(String jql, boolean validateQuery)
        {
            WebResource resource = resourceRoot(getEnvironmentData().getBaseUrl().toExternalForm())
                    .path("/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml")
                    .queryParam("tempMax", "1000")
                    .queryParam("jqlQuery", jql)
                    .queryParam("validateQuery", String.valueOf(validateQuery));

            return resource.get(ClientResponse.class);
        }
    }
}
