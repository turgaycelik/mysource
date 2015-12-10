/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.bc.customfield.CustomFieldService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomFieldDescription;
import com.atlassian.jira.issue.managers.MockCustomFieldManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import webwork.action.Action;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestCreateCustomField
{
    @Mock
    private ReindexMessageManager reindexMessageManager;
    @Mock
    private CustomFieldContextConfigHelper customFieldContextConfigHelper;
    @Mock
    private CustomFieldDescription customFieldDescription;
    @Mock
    private FeatureManager featureManager;
    @Mock
    private ManagedConfigurationItemService managedConfigurationItemService;
    @Mock
    private CustomFieldService customFieldServicel;
    @Mock
    private ConstantsManager constantsManager;
    @Mock
    private CustomFieldManager customFieldManager;
    @Mock
    private ProjectManager projectManager;
    @Mock
    private CustomFieldService customFieldService;
    @Mock
    private SubTaskManager subTaskManager;
    @AvailableInContainer
    @Mock
    private JiraAuthenticationContext authContext;
    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    @Before
    public void setUp() throws Exception
    {
        when(authContext.getI18nHelper()).thenReturn(new MockI18nBean());
    }
    @Test
    public void testDoChooseType1() throws Exception
    {
        final CustomFieldValidatorImpl customFieldValidator = new CustomFieldValidatorImpl(new MockCustomFieldManager(), managedConfigurationItemService);
        CreateCustomField customField = new CreateCustomField(customFieldValidator, constantsManager, customFieldManager, projectManager, subTaskManager, customFieldDescription, featureManager, customFieldService);
        customField.setFieldType("");

        String returnValue = customField.doCustomFieldType();
        assertThat(returnValue, is(Action.INPUT));
        assertThat(customField.getErrorMessages().size(), is(1));
        assertThat(customField.getErrorMessages().iterator().next(), is("Invalid field type specified."));
    }
}

