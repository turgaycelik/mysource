package com.atlassian.jira.web.action.admin.statuses;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.config.ConstantsService;
import com.atlassian.jira.bc.config.StatusService;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.RedirectSanitiser;

import com.mockobjects.servlet.MockHttpServletResponse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @since v6.1
 */
public class TestEditStatus
{
    private static final String STATUS_ICON_URL = "some fancy icon url";

    @Rule
    public final RuleChain mockContainer = MockitoMocksInContainer.forTest(this);

    private EditStatus editStatus;

    @Mock
    private ConstantsService constantsService;

    @Mock
    private StatusService statusService;

    @AvailableInContainer
    private RedirectSanitiser redirectSanitiser = new MockRedirectSanitiser();

    @AvailableInContainer
    @Mock
    private JiraAuthenticationContext authenticationContext;

    @Before
    public void setUp()
    {
        editStatus = new EditStatus(statusService, constantsService);
    }

    @Test
    public void testShouldSetDefaultCategoryOnSaveWhenLozengesAreDisabled() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewStatuses.jspa");
        when(constantsService.isStatusAsLozengeEnabled()).thenReturn(false);

        Status status = mock(Status.class);
        StatusCategory statusCategory = mock(StatusCategory.class);
        ServiceOutcome<StatusCategory> statusCategoryServiceOutcome = ServiceOutcomeImpl.ok(statusCategory);

        when(statusService.getStatusById(any(ApplicationUser.class), anyString())).thenReturn(status);
        when(constantsService.getDefaultStatusCategory(any(User.class))).thenReturn(statusCategoryServiceOutcome);
        when(statusService.editStatus(any(ApplicationUser.class), same(status), anyString(), anyString(), anyString(), isNull(StatusCategory.class))).thenReturn(ServiceOutcomeImpl.ok(status));

        editStatus.doExecute();

        verify(constantsService, never()).getStatusCategoryById(any(User.class), anyString());
        response.verify();
    }

    @Test
    public void testShouldSetCategorySelectedByUserOnSaveWhenLozengesAreEnabled() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewStatuses.jspa");
        when(constantsService.isStatusAsLozengeEnabled()).thenReturn(true);

        editStatus.setStatusCategory(123L);

        Status status = mock(Status.class);
        StatusCategory statusCategory = mock(StatusCategory.class);
        ServiceOutcome<StatusCategory> statusCategoryServiceOutcome = ServiceOutcomeImpl.ok(statusCategory);

        when(statusService.getStatusById(any(ApplicationUser.class), anyString())).thenReturn(status);
        when(constantsService.getStatusCategoryById(any(User.class), eq("123"))).thenReturn(statusCategoryServiceOutcome);
        when(statusService.editStatus(any(ApplicationUser.class), same(status), anyString(), anyString(), anyString(), same(statusCategory))).thenReturn(ServiceOutcomeImpl.ok(status));

        editStatus.doExecute();

        verify(constantsService, never()).getDefaultStatusCategory(any(User.class));
        response.verify();
    }

    @Test
    public void testShouldComplementIconUrlWhenLozengesAreEnabled() throws Exception
    {
        MockHttpServletResponse response;
        when(constantsService.isStatusAsLozengeEnabled()).thenReturn(true);

        Status status = mock(Status.class);
        when(status.getIconUrl()).thenReturn(STATUS_ICON_URL);
        StatusCategory statusCategory = mock(StatusCategory.class);
        ServiceOutcome<StatusCategory> statusCategoryServiceOutcome = ServiceOutcomeImpl.ok(statusCategory);

        when(statusService.getStatusById(any(ApplicationUser.class), anyString())).thenReturn(status);
        when(constantsService.getStatusCategoryById(any(User.class), anyString())).thenReturn(statusCategoryServiceOutcome);
        when(statusService.editStatus(any(ApplicationUser.class), same(status), anyString(), anyString(), eq(STATUS_ICON_URL), same(statusCategory))).thenReturn(ServiceOutcomeImpl.ok(status));

        // check with null
        response = JiraTestUtil.setupExpectedRedirect("ViewStatuses.jspa");
        editStatus.setIconurl(null);
        editStatus.doExecute();
        response.verify();

        // check with an empty string
        response = JiraTestUtil.setupExpectedRedirect("ViewStatuses.jspa");
        editStatus.setIconurl("");
        editStatus.doExecute();
        response.verify();
    }
}
