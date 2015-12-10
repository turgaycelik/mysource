package com.atlassian.jira.issue.fields;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.UserJsonBean;
import com.atlassian.jira.issue.search.handlers.CreatorSearchHandlerFactory;
import com.atlassian.jira.issue.statistics.CreatorStatisticsMapper;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestCreatorSystemField
{
    CreatorSystemField systemFieldUnderTest;
    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private VelocityTemplatingEngine templatingEngine;
    @Mock
    @AvailableInContainer
    private ApplicationProperties applicationProperties;
    @Mock
    @AvailableInContainer
    private AvatarService avatarService;
    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext authenticationContext;
    @Mock
    private CreatorStatisticsMapper creatorStatisticsMapper;
    @Mock
    private UserManager userManager;
    @Mock
    private WebResourceManager webResourceManager;
    @Mock
    private FeatureManager featureManager;
    @Mock
    private JiraBaseUrls jiraBaseUrls;
    @Mock
    private FieldLayoutItem fieldLayoutItem;
    @Mock
    private ApplicationUser currentUser;
    @Mock
    private MockIssue issue;
    @Mock
    @AvailableInContainer
    private EmailFormatter emailFormatter;

    private CreatorSearchHandlerFactory creatorSearchHandlerFactory;

    @Before
    public void setUp() throws Exception
    {
        currentUser = new MockApplicationUser("selector");
        when(authenticationContext.getLoggedInUser()).thenReturn(currentUser.getDirectoryUser());
        when(userManager.getUserByName("selector")).thenReturn(currentUser);
        when(applicationProperties.getEncoding()).thenReturn("UTF-8");
        systemFieldUnderTest = new CreatorSystemField(
                templatingEngine,
                applicationProperties,
                authenticationContext,
                creatorSearchHandlerFactory,
                jiraBaseUrls,
                creatorStatisticsMapper, emailFormatter);
    }

    @Test
    public void testJson() throws Exception
    {
        when(issue.getCreator()).thenReturn(currentUser.getDirectoryUser());
        when(jiraBaseUrls.restApi2BaseUrl()).thenReturn("rest/");
        when(avatarService.getAvatarAbsoluteURL(eq(currentUser), eq(currentUser), isA(Avatar.Size.class))).thenReturn(new URI("http://www.example.com/avatar"));
        FieldJsonRepresentation jsonRepresentation =  systemFieldUnderTest.getJsonFromIssue(issue, false, fieldLayoutItem);
        UserJsonBean userBean = (UserJsonBean)jsonRepresentation.getStandardData().getData();
        assertEquals("rest/user?username=selector", userBean.getSelf());
    }







}
