package com.atlassian.jira.webtests.util.issue;

import java.io.ByteArrayInputStream;

import com.atlassian.jira.functest.framework.LocatorFactory;
import com.atlassian.jira.functest.rules.RestRule;

import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.PostMethodWebRequest;

import org.w3c.dom.Element;

import net.sourceforge.jwebunit.WebTester;

/**
 * Class with methods to inline edit fields on an issue.
 */
public class IssueInlineEdit
{
    private LocatorFactory locator;
    private WebTester tester;
    private RestRule restRule;

    public IssueInlineEdit(final LocatorFactory locator, final WebTester tester, final RestRule restRule)
    {
        this.locator = locator;
        this.tester = tester;
        this.restRule = restRule;
    }

    public void inlineEditField(String issueId, String fieldName, String fieldValue) throws Exception
    {
        // Get the token to be able to make the next request
        Element node = (Element) locator.css("meta[name=atlassian-token]").getNode();
        String token = node.getAttribute("content");

        // Simulate inline edit of field
        String body = fieldName + "=" + fieldValue + "&"
                + "issueId=" + issueId + "&"
                + "singleFieldEdit=true&"
                + "fieldsToForcePresent=" + fieldName + "&"
                + "atl_token=" + token;
        POST("AjaxIssueAction.jspa?decorator=none", body);
    }

    private void POST(final String url, final String postBody) throws Exception
    {
        restRule.before();

        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);

        PostMethodWebRequest request = new PostMethodWebRequest(restRule.getBaseUrlPlus(url), new ByteArrayInputStream(postBody.getBytes()), "application/x-www-form-urlencoded");
        tester.getDialog().getWebClient().sendRequest(request);

        restRule.after();
    }
}
