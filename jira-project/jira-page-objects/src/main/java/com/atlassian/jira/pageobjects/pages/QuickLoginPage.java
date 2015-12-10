package com.atlassian.jira.pageobjects.pages;


import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import org.hamcrest.Matchers;

/**
 * Page object implementation for the QuickLoginPage (currently it uses /rest/auth/1/session)
 * 
 */
public class QuickLoginPage extends AbstractJiraPage
{
    private static final String QUICK_LOGIN_URL = "/rest/auth/1/session";
    private final URI uri;
    private final String username;

    @ElementBy(tagName = "pre")
    protected PageElement pre;

    public QuickLoginPage(final String username, final String password) {
        this.username = username;
        // os_destination is important - without this we could be redirected by JIRA in some cases
        this.uri = UriBuilder.fromPath(QUICK_LOGIN_URL).queryParam("os_username", username)
                .queryParam("os_password", password).queryParam("os_destination", QUICK_LOGIN_URL).build();
    }

    @Override
    public TimedCondition isAt()
    {
        return Conditions.and(pre.timed().isPresent(),
                Conditions.forMatcher(pre.timed().getText(), Matchers.containsString(String.format("\"name\":\"%s\"", username))));
    }

    @Override
    public String getUrl()
    {
        return uri.toString();
    }
}
