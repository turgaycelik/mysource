package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.customfield.CreateValidationResult;
import com.atlassian.jira.bc.customfield.CustomFieldDefinition;
import com.atlassian.jira.bc.customfield.CustomFieldService;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.rest.api.customfield.CustomFieldDefinitionJsonBean;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugins.rest.common.util.RestUrlBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericEntityException;

import javax.ws.rs.core.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Smoke-mock test, look for real integration tests in Jira REST Java Client
 *
 * @since v6.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestFieldResource
{
    @Mock
    private User testUser;

    @Mock
    private JiraBaseUrls jiraBaseUrls;
    @Mock
    private FieldManager fieldManager;
    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;
    @Mock
    private CustomFieldService customFieldService;
    @Mock
    private ContextUriInfo contextUriInfo;
    @Mock
    private RestUrlBuilder restUrlBuilder;
    @Mock
    private SearchHandlerManager searchHandlerManager;

    @Test
    public void createCustomField() throws GenericEntityException {
        final FieldResource fieldResource = new FieldResource(fieldManager, jiraAuthenticationContext, customFieldService, searchHandlerManager);

        final CustomFieldDefinitionJsonBean customFieldDefinitionJsonBean = Mockito.mock(CustomFieldDefinitionJsonBean.class);

        final CustomFieldDefinition customFieldDefinition = Mockito.mock(CustomFieldDefinition.class);
        final CreateValidationResult createValidationResult = Mockito.mock(CreateValidationResult.class);
        final CustomField customField = Mockito.mock(CustomField.class);
        final ServiceOutcome<CreateValidationResult> createValidationResultServiceOutcome = new ServiceOutcomeImpl<CreateValidationResult>(new SimpleErrorCollection(), createValidationResult);

        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(testUser);
        when(customFieldService.validateCreate(any(User.class), any(CustomFieldDefinition.class))).thenReturn(createValidationResultServiceOutcome);
        when(customFieldService.create(createValidationResult)).thenReturn(new ServiceOutcomeImpl<CustomField>(new SimpleErrorCollection(),customField));

        final Response response = fieldResource.createCustomField(customFieldDefinitionJsonBean);
        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test(expected = RESTException.class)
    public void assumeValidationErrorDuringCustomFieldCreation() throws GenericEntityException {
        final FieldResource fieldResource = new FieldResource(fieldManager, jiraAuthenticationContext, customFieldService, searchHandlerManager);

        final CustomFieldDefinitionJsonBean customFieldDefinitionJsonBean = Mockito.mock(CustomFieldDefinitionJsonBean.class);

        final CustomFieldDefinition customFieldDefinition = Mockito.mock(CustomFieldDefinition.class);
        final CreateValidationResult createValidationResult = Mockito.mock(CreateValidationResult.class);
        final CustomField customField = Mockito.mock(CustomField.class);
        final ServiceOutcome validationOutcome = Mockito.mock(ServiceOutcome.class);

        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(testUser);
        when(customFieldService.validateCreate(any(User.class), any(CustomFieldDefinition.class))).thenReturn(validationOutcome);
        when(validationOutcome.isValid()).thenReturn(false);
        when(validationOutcome.getErrorCollection()).thenReturn(new SimpleErrorCollection());
        when(customFieldService.create(createValidationResult)).thenReturn(new ServiceOutcomeImpl<CustomField>(new SimpleErrorCollection(), customField));

        fieldResource.createCustomField(customFieldDefinitionJsonBean);

    }

}
