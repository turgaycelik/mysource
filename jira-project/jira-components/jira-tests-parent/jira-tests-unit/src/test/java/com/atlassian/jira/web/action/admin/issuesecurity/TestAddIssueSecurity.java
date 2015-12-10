package com.atlassian.jira.web.action.admin.issuesecurity;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockHttp;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.SecurityTypeManager;
import com.atlassian.jira.security.type.SecurityType;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.MockUserKeyService;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.web.action.RedirectSanitiser;

import com.google.common.collect.ImmutableMap;
import com.mockobjects.servlet.MockHttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations.Mock;
import org.ofbiz.core.entity.GenericValue;

import webwork.action.ActionContext;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class TestAddIssueSecurity
{
    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    @Rule
    public final MockHttp mockHttp = MockHttp.withMocks(new MockHttpServletRequest(), new MockHttpServletResponse());

    private static final Long TEST_SCHEME_ID = 1L;
    private static final Long TEST_SECURITY_LEVEL_ID = 1L;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private MockUser user = new MockUser("user");

    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext = new MockAuthenticationContext(user);
    @Mock
    @AvailableInContainer
    private IssueSecuritySchemeManager issueSecuritySchemeManager;
    @Mock
    @AvailableInContainer
    private SecurityTypeManager issueSecurityTypeManager;
    @Mock
    @AvailableInContainer
    private IssueSecurityLevelManager issueSecurityLevelManager;
    @AvailableInContainer
    private UserKeyService userKeyService = new MockUserKeyService();
    @AvailableInContainer
    private RedirectSanitiser redirectSanitiser = new MockRedirectSanitiser();

    @Mock
    I18nHelper i18nHelper;

    private GenericValue scheme;

    @Mock
    private SecurityType schemeType;

    @Before
    public void setUp()
            throws Exception
    {
        scheme = new MockGenericValue("IssueSecurityScheme", ImmutableMap.of("id", new Long(TEST_SCHEME_ID), "name", "name"));
        when(issueSecuritySchemeManager.getScheme(TEST_SCHEME_ID)).thenReturn(scheme);

        when(issueSecurityTypeManager.getSchemeType(Mockito.anyString())).thenReturn(schemeType);

        request = (MockHttpServletRequest) mockHttp.mockRequest();
        response = (MockHttpServletResponse) mockHttp.mockResponse();
    }

    @After
    public void tearDown()
    {
        ActionContext.setSingleValueParameters(Collections.emptyMap());
    }

    @Test
    public void testFirstExistingIssueSecurityWithNoParameters() throws Exception
    {
        final AddIssueSecurity tested = createTested("reporter");
        setUpExistingSecurities(newSecurity("reporter"), newSecurity("assignee"));
        tested.execute();

        assertTrue("There are errors", tested.hasAnyErrors());
        assertArrayEquals(tested.getErrorMessages().toArray(), new String[] { tested.getText("admin.errors.this.issue.security.already.exists") });
    }

    @Test
    public void testAddNonFirstExistingIssueSecurityWithNoParameters() throws Exception
    {
        final AddIssueSecurity tested = createTested("reporter");
        setUpExistingSecurities(newSecurity("assignee"), newSecurity("reporter"));
        tested.execute();

        assertTrue("There are errors", tested.hasAnyErrors());
        assertTrue(tested.getErrorMessages().contains(tested.getText("admin.errors.this.issue.security.already.exists")));
    }

    @Test
    public void testAddNonExistingIssueSecurityWithNoParameters() throws Exception
    {
        final AddIssueSecurity tested = createTested("reporter");
        setUpExistingSecurities(newSecurity("other"), newSecurity("assignee"), mockSecurityGV("user", "someuser"));
        setUpExpectedRedirect("EditIssueSecurities!default.jspa?schemeId=1");
        tested.execute();

        assertFalse("There are no errors", tested.hasAnyErrors());
        verifyExpectedRedirect();
    }

    @Test
    public void testAddFirstExistingIssueSecurityWithParameters() throws Exception
    {
        final AddIssueSecurity tested = createTested("group");
        addParameterToContext("group", "jira-dev");
        setUpExistingSecurities(mockSecurityGV("group", "jira-dev"), newSecurity("assignee"));
        tested.execute();

        assertTrue("There are errors", tested.hasAnyErrors());
        assertArrayEquals(tested.getErrorMessages().toArray(), new String[] { tested.getText("admin.errors.this.issue.security.already.exists") });
    }

    @Test
    public void testAddNonFirstExistingIssueSecurityWithParameters() throws Exception
    {
        final AddIssueSecurity tested = createTested("group");
        addParameterToContext("group", "jira-dev");
        setUpExistingSecurities(mockSecurityGV("group", "confluence-dev"), mockSecurityGV("group", "jira-dev"),
                newSecurity("no-param"));
        tested.execute();

        assertTrue("There are errors", tested.hasAnyErrors());
        assertArrayEquals(tested.getErrorMessages().toArray(), new String[] { tested.getText("admin.errors.this.issue.security.already.exists") });
    }

    @Test
    public void testAddNonExistingIssueSecurityWithParameters() throws Exception
    {
        final AddIssueSecurity tested = createTested("user");
        addParameterToContext("user", "someuser");
        setUpExistingSecurities(newSecurity("no-param"), mockSecurityGV("user", "some-other-user"),
                mockSecurityGV("user", "yet-another-user"));
        setUpExpectedRedirect("EditIssueSecurities!default.jspa?schemeId=1");
        tested.execute();

        assertFalse("There are no errors", tested.hasAnyErrors());
        verifyExpectedRedirect();
    }

    @Test
    public void testAddIssueSecurityWithNoParameterGivenExistingIssueSecurityWithParameter() throws Exception
    {
        final AddIssueSecurity tested = createTested("user");
        setUpExistingSecurities(newSecurity("no-param"), mockSecurityGV("user", "some-other-user"),
                mockSecurityGV("user", "yet-another-user"));
        setUpExpectedRedirect("EditIssueSecurities!default.jspa?schemeId=1");
        tested.execute();

        assertFalse("There are no errors", tested.hasAnyErrors());
        verifyExpectedRedirect();
    }

    @Test
    public void testAddIssueSecurityWithParameterGivenExistingIssueSecurityWithNoParameter() throws Exception
    {
        final AddIssueSecurity tested = createTested("user");
        addParameterToContext("user", "someuser");
        setUpExistingSecurities(newSecurity("user"), mockSecurityGV("user", "some-other-user"),
                mockSecurityGV("user", "yet-another-user"));
        setUpExpectedRedirect("EditIssueSecurities!default.jspa?schemeId=1");
        tested.execute();

        assertFalse("There are no errors", tested.hasAnyErrors());
        verifyExpectedRedirect();
    }

    private AddIssueSecurity createTested(final String securityType)
    {
        final AddIssueSecurity tested = new AddIssueSecurity(issueSecuritySchemeManager, issueSecurityTypeManager, issueSecurityLevelManager, userKeyService);
        tested.setSchemeId(TEST_SCHEME_ID);
        tested.setSecurity(TEST_SECURITY_LEVEL_ID);
        tested.setType(securityType);
        return tested;
    }

    private void addParameterToContext(final String type, final String parameter)
    {
        final Map<Object, Object> singleValueParams = ActionContext.getSingleValueParameters();
        if(singleValueParams.containsKey(type))
        {
            ActionContext.setSingleValueParameters(ImmutableMap.copyOf(singleValueParams));
        }
        else
        {
            ActionContext.setSingleValueParameters(ImmutableMap.builder().putAll(singleValueParams).put(type, parameter).build());
        }
    }


    private void setUpExistingSecurities(final GenericValue... securities) throws Exception
    {
        when(issueSecuritySchemeManager.getEntities(scheme, TEST_SECURITY_LEVEL_ID)).thenReturn(Arrays.asList(securities));
    }

    private GenericValue newSecurity(final String type)
    {
        return new MockGenericValue("SchemeIssueSecurities", ImmutableMap.of("type", type));
    }

    private GenericValue mockSecurityGV(final String type, final String param)
    {
        return new MockGenericValue("SchemeIssueSecurities", ImmutableMap.of("type", type, "parameter", param));
    }

    private void setUpExpectedRedirect(final String url) throws Exception
    {
        response.setExpectedRedirect(url);
    }

    private void verifyExpectedRedirect()
    {
        response.verify();
    }
}