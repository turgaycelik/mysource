package com.atlassian.jira.issue.fields.layout.field;

import com.atlassian.jira.admin.RenderableProperty;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.component.MockComponentWorker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(ListeningMockitoRunner.class)
public class TestFieldLayoutItemImpl
{
    static final String DESCRIPTION_RAW = "raw-Custom field description";
    static final String DESCRIPTION_RENDERED = "rendered-Custom field description";

    @Mock RenderableProperty descriptionProperty;
    @Mock CustomField mockCustomField;
    @Mock FieldManager mockFieldManager;
    @Mock OrderableField mockOrderableField;
    @Mock FieldDescriptionHelper mockFieldDescriptionHelper;

    @Before
    public void setUp() throws Exception
    {
        when(descriptionProperty.getViewHtml()).thenReturn(DESCRIPTION_RENDERED);
        MockComponentWorker componentWorker = new MockComponentWorker();
        componentWorker.registerMock(FieldDescriptionHelper.class, mockFieldDescriptionHelper);
        ComponentAccessor.initialiseWorker(componentWorker);

        // mock out a system field
        when(mockFieldManager.isCustomField(mockOrderableField)).thenReturn(false);

        // mock out a custom field
        when(mockCustomField.getId()).thenReturn("ID");
        when(mockCustomField.getDescription()).thenReturn(DESCRIPTION_RAW);
        when(mockCustomField.getDescriptionProperty()).thenReturn(descriptionProperty);
        when(mockFieldManager.isCustomField(mockCustomField)).thenReturn(true);
        when(mockFieldManager.getCustomField("ID")).thenReturn(mockCustomField);

    }

    @Test
    public void fieldDescriptionForCustomFieldShouldUseFieldLayoutItemDescriptionIfSet()
    {
        // set a null description; assert that a rendered version of the custom field description is returned
        FieldLayoutItemImpl fieldLayoutItem = createFieldLayoutItem(mockCustomField, null);
        assertEquals(DESCRIPTION_RENDERED, fieldLayoutItem.getFieldDescription());
    }

    @Test
    public void fieldDescriptionForCustomFieldShouldUseRenderedVersionOfCustomFieldDescriptionIfFieldLayoutItemDescriptionIsNotSet()
    {
        final String fieldDescription = "description from field configuration";
        when(mockFieldDescriptionHelper.getDescription(fieldDescription)).thenReturn(fieldDescription);

        // set a non-null description; assert that it is returned as-is
        FieldLayoutItemImpl fieldLayoutItem = createFieldLayoutItem(mockCustomField, fieldDescription);
        assertEquals(fieldDescription, fieldLayoutItem.getFieldDescription());
    }

    @Test
    public void fieldDescriptionForSystemFieldShouldAlwaysUseFieldLayoutItemDescription()
    {

        final String fieldDescription = "description from field configuration";
        when(mockFieldDescriptionHelper.getDescription(fieldDescription)).thenReturn(fieldDescription);
        // set a null description; assert that it is returned (as field is not a CF)
        FieldLayoutItemImpl fieldLayoutItem = createFieldLayoutItem(mockOrderableField, null);
        assertNull(fieldLayoutItem.getFieldDescription());

        // set a non-null description; assert that it is returned
        fieldLayoutItem = createFieldLayoutItem(mockOrderableField, fieldDescription);
        assertEquals(fieldDescription, fieldLayoutItem.getFieldDescription());
    }

    @Test
    public void fieldDescriptionForOnDemandShouldAlwaysBeWikiRendered()
    {

    }

    private FieldLayoutItemImpl createFieldLayoutItem(OrderableField field, String fieldDescription)
    {
        return new FieldLayoutItemImpl(field, fieldDescription, false, true, null, null, mockFieldManager, mockFieldDescriptionHelper);
    }
}
