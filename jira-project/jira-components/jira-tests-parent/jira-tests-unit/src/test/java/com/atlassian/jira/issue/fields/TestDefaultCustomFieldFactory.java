package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.jql.context.FieldConfigSchemeClauseContextUtil;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptors;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptors;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestDefaultCustomFieldFactory
{
    private DefaultCustomFieldFactory factory;

    @Before
    public void setUp()
    {
        new MockComponentWorker().init();
        factory = createFactory();
    }

    @Test (expected = NullPointerException.class)
    public void createDoesNotAcceptANullGenericValue()
    {
        factory.create(null);
    }

    @Test
    public void createInstantiatesAnInstanceCorrectly()
    {
        GenericValue genericValue = mock(GenericValue.class);

        CustomField customField = factory.create(genericValue);

        assertThat(customField.getGenericValue(), is(genericValue));
    }

    @Test (expected = NullPointerException.class)
    public void copyOfDoesNotAcceptANullCustomField()
    {
        factory.copyOf(null);
    }

    @Test
    public void copyOfInstantiatesAnInstanceCorrectly()
    {
        GenericValue genericValue = mockGenericValue();
        CustomField customField = customFieldWith(genericValue);

        CustomField copy = factory.copyOf(customField);

        assertThat(copy.getGenericValue(), equalTo(genericValue));
    }

    private GenericValue mockGenericValue()
    {
        String anyEntityName = "customField";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", Long.valueOf(1));

        ModelEntity modelEntity = mock(ModelEntity.class);
        when(modelEntity.getEntityName()).thenReturn(anyEntityName);

        return new MockGenericValue(anyEntityName, modelEntity, map);
    }

    private CustomField customFieldWith(GenericValue genericValue)
    {
        CustomField customField = mock(CustomField.class);
        when(customField.getGenericValue()).thenReturn(genericValue);
        return customField;
    }

    private DefaultCustomFieldFactory createFactory()
    {
        return new DefaultCustomFieldFactory(
                mock(JiraAuthenticationContext.class),
                mock(FieldConfigSchemeManager.class),
                mock(PermissionManager.class),
                mock(RendererManager.class),
                mock(FieldConfigSchemeClauseContextUtil.class),
                mock(CustomFieldDescription.class),
                mock(FeatureManager.class),
                mock(TranslationManager.class),
                mock(CustomFieldScopeFactory.class),
                mock(CustomFieldTypeModuleDescriptors.class),
                mock(CustomFieldSearcherModuleDescriptors.class)
        );
    }
}
