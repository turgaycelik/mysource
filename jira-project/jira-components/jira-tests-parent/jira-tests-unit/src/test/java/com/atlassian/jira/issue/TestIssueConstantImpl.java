package com.atlassian.jira.issue;

import java.util.Collections;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.util.BaseUrl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v5.2
 */
@RunWith (MockitoJUnitRunner.class)
public class TestIssueConstantImpl
{
    @Mock
    private BaseUrl baseUrl;

    @Test
    public void testGetCompleteIconUrlNoUrl()
    {
        checkNullUrl("");
        checkNullUrl("\t   ");
        checkNullUrl(null);
    }

    @Test
    public void testGetCompleteIconUrlAbsolute()
    {
        checkAbsoluteUrl("http://something.png");
        checkAbsoluteUrl("https://what.png");
    }

    @Test
    public void testGetCompleteIconUrlRelative()
    {
        Mockito.when(baseUrl.getBaseUrl()).thenReturn("/jira");

        IssueConstantImpl issueConstant = new IssueConstantImpl(generateIssueType("/images"), null, null, baseUrl);
        assertEquals("/jira/images", issueConstant.getCompleteIconUrl());
    }

    private void checkNullUrl(String url)
    {
        IssueConstantImpl issueConstant = new IssueConstantImpl(generateIssueType(url), null, null, baseUrl);
        assertNull(issueConstant.getCompleteIconUrl());
    }

    private void checkAbsoluteUrl(String url)
    {
        IssueConstantImpl issueConstant = new IssueConstantImpl(generateIssueType(url), null, null, baseUrl);
        assertEquals(url, issueConstant.getCompleteIconUrl());
    }

    private static MockGenericValue generateIssueType(String url)
    {
        return new MockGenericValue("IssueType", Collections.singletonMap("iconurl", url));
    }
}
