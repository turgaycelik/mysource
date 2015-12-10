package com.atlassian.jira.issue.fields;

import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class TestDefaultCustomFieldScopeFactory
{
    private DefaultCustomFieldScopeFactory factory;

    @Before
    public void setUp()
    {
        factory = new DefaultCustomFieldScopeFactory(mock(FieldConfigSchemeManager.class));
    }

    @Test (expected = NullPointerException.class)
    public void createForDoesNotAcceptANullCustomField()
    {
        factory.createFor(null);
    }

    @Test
    public void createForReturnsAnInstanceOfTheScopeWhenTheCustomFieldIsNotNull()
    {
        CustomField customField = mock(CustomField.class);

        CustomFieldScope scope = factory.createFor(customField);

        assertNotNull(scope);
    }
}
