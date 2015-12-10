package com.atlassian.jira.plugin.headernav;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.dbc.Assertions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class CreateMenuContextProvider extends AbstractJiraContextProvider
{
    static final String CREATE_MENU_SECTION = "system.user.create.options/create-menu-section";
    static final String FIRST_CREATE_MENU_LINK_KEY = "createIssueLink";

    private final SimpleLinkManager simpleLinkManager;
    private final JiraAuthenticationContext authenticationContext;

    public CreateMenuContextProvider(@Nonnull final SimpleLinkManager simpleLinkManager, @Nonnull final  JiraAuthenticationContext authenticationContext)
    {
        this.simpleLinkManager = Assertions.notNull(simpleLinkManager);
        this.authenticationContext = Assertions.notNull(authenticationContext);
    }

    @Override
    public Map<String, Object> getContextMap(@Nullable final User user, @Nullable final JiraHelper jiraHelper)
    {
        final SimpleLink firstCreateMenuLink = getFirstLinkForCreateMenuSection(jiraHelper);
        return MapBuilder.<String, Object>newBuilder(FIRST_CREATE_MENU_LINK_KEY, firstCreateMenuLink).toHashMap();
    }

    @Nullable
    private SimpleLink getFirstLinkForCreateMenuSection(@Nullable final JiraHelper helper)
    {
        final User user = authenticationContext.getLoggedInUser();
        final List<SimpleLink> linksForSection = simpleLinkManager.getLinksForSection(CREATE_MENU_SECTION, user, helper);
        if (linksForSection != null && !linksForSection.isEmpty())
        {
            return linksForSection.get(0);
        }
        return null;
    }
}
