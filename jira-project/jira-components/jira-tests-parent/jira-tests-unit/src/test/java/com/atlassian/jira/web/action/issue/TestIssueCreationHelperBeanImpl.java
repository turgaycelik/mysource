package com.atlassian.jira.web.action.issue;

import java.util.HashMap;

import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.customfields.OperationContextImpl;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.JiraContactHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class TestIssueCreationHelperBeanImpl
{
    @Mock
    private JiraLicenseService jiraLicenseService;
    @Mock
    private JiraContactHelper jiraContactHelper;
    @Mock
    private LicenseDetails licenseDetails;
    @Mock
    private UserUtil userUtil;
    @Mock
    private FieldManager fieldManager;
    @Mock
    private FieldScreenRendererFactory fieldScreenRendererFactory;

    MockI18nBean i18n = new MockI18nBean();
    
    @Rule
    public InitMockitoMocks initMocks = new InitMockitoMocks(this);
    
    private IssueCreationHelperBeanImpl helperBean;

    @Before
    public void setUp() throws Exception
    {
        when(jiraContactHelper.getAdministratorContactMessage(i18n)).thenReturn("please contact your JIRA administrators");

        helperBean = new IssueCreationHelperBeanImpl(userUtil, fieldManager, fieldScreenRendererFactory, jiraLicenseService, jiraContactHelper)
        {
            @Override
            LicenseDetails getLicenseDetails()
            {
                return licenseDetails;
            }
        };
    }

    @Test
    public void testValidateLicenseNoLicense()
    {

        when(licenseDetails.isLicenseSet()).thenReturn(false);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        helperBean.validateLicense(errorCollection, i18n);
        assertEquals("You are not able to create issues because your JIRA instance has an invalid license, please contact your JIRA administrators.",
                errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateLicenseLicenseExpired()
    {
        when(licenseDetails.isLicenseSet()).thenReturn(true);
        when(licenseDetails.isExpired()).thenReturn(true);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        helperBean.validateLicense(errorCollection, i18n);
        assertEquals(
                "You will not be able to create new issues because your JIRA evaluation period has expired, please contact your JIRA administrators.",
                errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidatePersonalLicenseUserLimitExceeded()
    {
        when(userUtil.hasExceededUserLimit()).thenReturn(true);
        when(licenseDetails.isLicenseSet()).thenReturn(true);
        when(licenseDetails.isExpired()).thenReturn(false);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        helperBean.validateLicense(errorCollection, i18n);
        assertEquals(
                "You will not be able to create new issues because the user limit for your JIRA instance has been exceeded, please contact your JIRA administrators.",
                errorCollection.getErrorMessages().iterator().next());

    }

    @Test
    public void testValidateLicenseHappy()
    {
        when(userUtil.hasExceededUserLimit()).thenReturn(false);
        when(licenseDetails.isLicenseSet()).thenReturn(true);
        when(licenseDetails.isExpired()).thenReturn(false);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        helperBean.validateLicense(errorCollection, i18n);
        assertFalse(errorCollection.hasAnyErrors());
    }

    @Test
    public void shouldPopulateFromDefaultsWhenSkippingScreenCheckIsDisabled()
    {
        //prepare fields
        FieldScreenRenderer fsr = mock(FieldScreenRenderer.class, RETURNS_DEEP_STUBS);
        FieldLayoutItem fli1 = createFieldLayoutItem(fsr, "field1", false);
        FieldLayoutItem fli2 = createFieldLayoutItem(fsr, "field2", false);
        when(fsr.getFieldLayout().getVisibleLayoutItems(any(Project.class), anyListOf(String.class))).thenReturn(ImmutableList.of(fli1, fli2));

        //prepare test issue
        Issue issue = mock(Issue.class, RETURNS_DEEP_STUBS);

        //prepare input params
        IssueInputParameters issueInput = new IssueInputParametersImpl();
        assertFalse("skipScreenCheck should be false by default", issueInput.skipScreenCheck());
        final HashMap<String,Object> fieldValuesHolder = Maps.newHashMap();

        helperBean.validateCreateIssueFields(new MockJiraServiceContext("user"), ImmutableList.of("field1", "field2"), issue, fsr, new OperationContextImpl(null, fieldValuesHolder), issueInput, i18n);

        verify(fli1.getOrderableField()).populateDefaults(fieldValuesHolder, issue);
        verify(fli2.getOrderableField()).populateDefaults(fieldValuesHolder, issue);
        verify(fli1.getOrderableField(), never()).populateFromParams(anyMapOf(String.class, Object.class), anyMapOf(String.class, String[].class));
        verify(fli2.getOrderableField(), never()).populateFromParams(anyMapOf(String.class, Object.class), anyMapOf(String.class, String[].class));
    }


    @Test
    public void shouldPopulateFromParamsWhenSkippigIsEnabled()
    {
        //prepare fields
        FieldScreenRenderer fsr = mock(FieldScreenRenderer.class, RETURNS_DEEP_STUBS);
        FieldLayoutItem fli1 = createFieldLayoutItem(fsr, "field1", false);
        FieldLayoutItem fli2 = createFieldLayoutItem(fsr, "field2", false);
        when(fsr.getFieldLayout().getVisibleLayoutItems(any(Project.class), anyListOf(String.class))).thenReturn(ImmutableList.of(fli1, fli2));

        Issue issue = mock(Issue.class, RETURNS_DEEP_STUBS);

        //prepare input params
        IssueInputParameters issueInput = new IssueInputParametersImpl();
        issueInput.setSkipScreenCheck(true);
        final HashMap<String,Object> fieldValuesHolder = Maps.newHashMap();

        helperBean.validateCreateIssueFields(new MockJiraServiceContext("user"), ImmutableList.of("field1", "field2"), issue, fsr, new OperationContextImpl(null, fieldValuesHolder), issueInput, i18n);

        verify(fli1.getOrderableField(), never()).populateDefaults(anyMapOf(String.class, Object.class), any(Issue.class));
        verify(fli2.getOrderableField(), never()).populateDefaults(anyMapOf(String.class, Object.class), any(Issue.class));
        verify(fli1.getOrderableField()).populateFromParams(fieldValuesHolder, issueInput.getActionParameters());
        verify(fli2.getOrderableField()).populateFromParams(fieldValuesHolder, issueInput.getActionParameters());
    }

    private static FieldLayoutItem createFieldLayoutItem(final FieldScreenRenderer fsr, final String fieldId, final boolean isVisible)
    {
        FieldLayoutItem fli1 = mock(FieldLayoutItem.class, RETURNS_DEEP_STUBS);
        when(fli1.getOrderableField().getId()).thenReturn(fieldId);
        when(fsr.getFieldScreenRenderLayoutItem(fli1.getOrderableField()).isShow(any(Issue.class))).thenReturn(isVisible);
        return fli1;
    }


}
