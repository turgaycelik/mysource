package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.MockOrderableField;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.rest.api.issue.JsonTypeBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;

import static com.atlassian.jira.matchers.ReflectionEqualTo.reflectionEqualTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestFieldBean
{
    @Mock
    private FieldManager fieldManager;

    @Mock
    private SearchHandlerManager searchHandlerManager;

    @Test
    public void testConvertOrderableFieldToShortBean()
    {
        final Field field = new MockOrderableField("summary", "Summary");

        final Collection<ClauseNames> clauseNames = new ArrayList<ClauseNames>();
        clauseNames.add(new ClauseNames("summary"));

        final JsonType jsonType = JsonTypeBuilder.system(JsonType.STRING_TYPE, IssueFieldConstants.SUMMARY);
        final JsonTypeBean schema = new JsonTypeBean(jsonType.getType(), jsonType.getItems(), jsonType.getSystem(), jsonType.getCustom(), jsonType.getCustomId());

        when(fieldManager.isOrderableField(field)).thenReturn(true);
        when(fieldManager.isNavigableField(field)).thenReturn(true);
        when(searchHandlerManager.getJqlClauseNames(field.getId())).thenReturn(clauseNames);

        final FieldBean fieldBean = FieldBean.shortBean(field, fieldManager, searchHandlerManager);
        fieldBean.setSchema(schema);

        assertThat(fieldBean, reflectionEqualTo(FieldBean.DOC_EXAMPLE_2));
    }

    @Test
    public void testConvertCustomFieldToShortBean()
    {

        final CustomField field = Mockito.mock(CustomField.class);
        field.setName("New custom field");

        final Collection<ClauseNames> clauseNames = new ArrayList<ClauseNames>();
        clauseNames.add(new ClauseNames("cf[10101]", "New custom field"));

        final JsonType schema = JsonTypeBuilder.custom("project", "com.atlassian.jira.plugin.system.customfieldtypes:project", 10101L);

        when(fieldManager.isOrderableField(field)).thenReturn(true);
        when(fieldManager.isNavigableField(field)).thenReturn(true);
        when(field.getId()).thenReturn("customfield_10101");
        when(field.getName()).thenReturn("New custom field");
        when(field.getJsonSchema()).thenReturn(schema);
        when(searchHandlerManager.getJqlClauseNames(field.getId())).thenReturn(clauseNames);

        final FieldBean fieldBean = FieldBean.shortBean(field, fieldManager, searchHandlerManager);
        assertThat(fieldBean, reflectionEqualTo(FieldBean.DOC_EXAMPLE_CF));
    }
}

