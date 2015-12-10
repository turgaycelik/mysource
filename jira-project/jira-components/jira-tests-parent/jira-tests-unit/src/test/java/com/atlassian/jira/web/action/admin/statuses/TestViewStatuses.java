package com.atlassian.jira.web.action.admin.statuses;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.config.ConstantsService;
import com.atlassian.jira.bc.config.StatusService;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.jira.workflow.WorkflowManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericEntityException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v6.1
 */
public class TestViewStatuses
{
    private static final String STATUS_ICON_URL = "some fancy icon url";

    @Rule
    public final RuleChain mockContainer = MockitoMocksInContainer.forTest(this);

    private ViewStatuses viewStatuses;

    @Mock
    private TranslationManager translationManager;

    @Mock
    private StatusService statusService;

    @Mock
    private StatusManager statusManager;

    @Mock
    private WorkflowManager workflowManager;

    @Mock
    private ConstantsService constantsService;

    @AvailableInContainer
    @Mock
    private JiraAuthenticationContext authenticationContext;

    @Mock
    private I18nHelper i18nHelper;

    @Before
    public void setUp()
    {
        viewStatuses = new ViewStatuses(translationManager, statusService, constantsService, i18nHelper, workflowManager);
    }

    @Test
    public void shouldBeAbleToUseBackwardCompatibilityConstructor()
    {
        final ViewStatuses viewStatuses2 = new ViewStatuses(translationManager, statusManager, workflowManager);
    }

    @Test
    public void shouldSetCategoryAsDefaultWhenDarkFeatureIsDisabled() throws GenericEntityException
    {
        when(constantsService.isStatusAsLozengeEnabled()).thenReturn(false);

        Status status = mock(Status.class);
        StatusCategory statusCategory = mock(StatusCategory.class);
        ServiceOutcome<StatusCategory> statusCategoryServiceOutcome = ServiceOutcomeImpl.ok(statusCategory);

        when(constantsService.getDefaultStatusCategory(any(User.class))).thenReturn(statusCategoryServiceOutcome);
        when(statusService.createStatus(any(ApplicationUser.class), anyString(), anyString(), anyString(), same(statusCategory))).thenReturn(ServiceOutcomeImpl.ok(status));

        viewStatuses.addConstant();

        verify(constantsService, never()).getStatusCategoryById(any(User.class), anyString());
    }

    @Test
    public void shouldSetCategorySelectedByUserWhenDarkFeatureIsEnabled() throws GenericEntityException
    {
        when(constantsService.isStatusAsLozengeEnabled()).thenReturn(true);

        viewStatuses.setStatusCategory(123L);

        Status status = mock(Status.class);
        StatusCategory statusCategory = mock(StatusCategory.class);
        ServiceOutcome<StatusCategory> statusCategoryServiceOutcome = ServiceOutcomeImpl.ok(statusCategory);

        when(constantsService.getStatusCategoryById(any(User.class), eq("123"))).thenReturn(statusCategoryServiceOutcome);
        when(statusService.createStatus(any(ApplicationUser.class), anyString(), anyString(), anyString(), same(statusCategory))).thenReturn(ServiceOutcomeImpl.ok(status));

        viewStatuses.addConstant();

        verify(constantsService, never()).getDefaultStatusCategory(any(User.class));
    }

    @Test
    public void shouldProvideGenericIconUrlWhenLozengesAreEnabled() throws GenericEntityException
    {
        // This test does not assert anything. Instead, it relies on mocked method createStatus of statusService
        // - if the method is invoked with other icon url value than desirable, an exception will be raised
        // making this test fail.

        when(constantsService.isStatusAsLozengeEnabled()).thenReturn(true);

        Status status = mock(Status.class);
        StatusCategory statusCategory = mock(StatusCategory.class);
        ServiceOutcome<StatusCategory> statusCategoryServiceOutcome = ServiceOutcomeImpl.ok(statusCategory);

        when(constantsService.getStatusCategoryById(any(User.class), anyString())).thenReturn(statusCategoryServiceOutcome);
        when(statusService.createStatus(any(ApplicationUser.class), anyString(), anyString(), eq(ViewStatuses.NEW_STATUS_DEFAULT_ICON), same(statusCategory))).thenReturn(ServiceOutcomeImpl.ok(status));

        viewStatuses.addConstant();
    }
}
