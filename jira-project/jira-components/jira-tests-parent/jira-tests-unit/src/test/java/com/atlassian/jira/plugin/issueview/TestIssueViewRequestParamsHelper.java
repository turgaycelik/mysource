package com.atlassian.jira.plugin.issueview;

import java.util.HashSet;
import java.util.Set;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.MockCustomField;
import com.atlassian.jira.issue.fields.MockOrderableField;
import com.atlassian.jira.issue.fields.OrderableField;

import com.mockobjects.dynamic.Mock;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestIssueViewRequestParamsHelper
{
    @Test
    public void testNoParamsDefined()
    {
        Mock mockFieldManager = new Mock(FieldManager.class);
        mockFieldManager.expectAndReturn("getOrderableFields", getOrderableFieldIds());

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl((FieldManager) mockFieldManager.proxy());
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build());

        assertFalse(issueViewFieldParams.isCustomViewRequested());
        assertFalse(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertTrue(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManager.verify();
    }

    @Test
    public void testNullFieldParamsDefined()
    {
        Mock mockFieldManager = new Mock(FieldManager.class);
        mockFieldManager.expectAndReturn("getOrderableFields", getOrderableFieldIds());

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl((FieldManager) mockFieldManager.proxy());
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", null));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertFalse(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertTrue(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManager.verify();
    }

    @Test
    public void testEmptyFieldParamsDefined()
    {
        Mock mockFieldManager = new Mock(FieldManager.class);
        mockFieldManager.expectAndReturn("getOrderableFields", getOrderableFieldIds());

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl((FieldManager) mockFieldManager.proxy());
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[0]));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertFalse(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertTrue(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManager.verify();
    }

    @Test
    public void testProperFieldParamsDefined()
    {
        MockControl mockFieldManagerControl = MockClassControl.createControl(FieldManager.class);
        FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getOrderableFields();
        mockFieldManagerControl.setReturnValue(getOrderableFieldIds());
        mockFieldManager.getField("name1");
        mockFieldManagerControl.setReturnValue(createField("name1", null, null));
        mockFieldManager.getField("name3");
        mockFieldManagerControl.setReturnValue(createField("name3", null, null));

        mockFieldManagerControl.replay();

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl(mockFieldManager);
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[] { "name1", "name3" }));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertTrue(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertFalse(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManagerControl.verify();
    }

    @Test
    public void testInvalidFieldsParamsDefined()
    {
        MockControl mockFieldManagerControl = MockClassControl.createControl(FieldManager.class);
        FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getOrderableFields();
        mockFieldManagerControl.setReturnValue(getOrderableFieldIds());
        mockFieldManager.getField("name5");
        mockFieldManagerControl.setReturnValue(null);
        mockFieldManager.getField("name6");
        mockFieldManagerControl.setReturnValue(null);


        mockFieldManagerControl.replay();

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl(mockFieldManager);
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[] { "name5", "name6" }));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertFalse(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertTrue(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManagerControl.verify();
    }

    @Test
    public void testValidAndInvalidFieldParamsDefined()
    {
        MockControl mockFieldManagerControl = MockClassControl.createControl(FieldManager.class);
        FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getOrderableFields();
        mockFieldManagerControl.setReturnValue(getOrderableFieldIds());
        mockFieldManager.getField("name1");
        mockFieldManagerControl.setReturnValue(createField("name1", null, null));
        mockFieldManager.getField("name5");
        mockFieldManagerControl.setReturnValue(null);

        mockFieldManagerControl.replay();

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl(mockFieldManager);
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[] { "name1", "name5" }));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertTrue(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertFalse(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManagerControl.verify();
    }

    @Test
    public void testCustomFieldParamsDefined()
    {
        MockControl mockFieldManagerControl = MockClassControl.createControl(FieldManager.class);
        FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getOrderableFields();
        mockFieldManagerControl.setReturnValue(getOrderableFieldIds());
        mockFieldManager.getCustomField("customfield_10000");
        mockFieldManagerControl.setReturnValue(createCustomField("customfield_10000"));
        mockFieldManager.getCustomField("customfield_10001");
        mockFieldManagerControl.setReturnValue(createCustomField("customfield_10001"));

        mockFieldManagerControl.replay();

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl(mockFieldManager);
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[] { "customfield_10000", "customfield_10001" }));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertTrue(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertTrue(issueViewFieldParams.getFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManagerControl.verify();
    }

    @Test
    public void testAllCustomFieldParamsDefined()
    {
        MockControl mockFieldManagerControl = MockClassControl.createControl(FieldManager.class);
        FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getOrderableFields();
        mockFieldManagerControl.setReturnValue(getOrderableFieldIds());

        mockFieldManagerControl.replay();

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl(mockFieldManager);
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[] { "allcustom" }));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertTrue(issueViewFieldParams.isAnyFieldDefined());
        assertTrue(issueViewFieldParams.isAllCustomFields());
        assertTrue(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManagerControl.verify();
    }

    @SuppressWarnings ({ "ThrowableInstanceNeverThrown" })
    @Test
    public void testInvalidCustomFieldParamsDefined()
    {
        MockControl mockFieldManagerControl = MockClassControl.createControl(FieldManager.class);
        FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getOrderableFields();
        mockFieldManagerControl.setReturnValue(getOrderableFieldIds());
        mockFieldManager.getCustomField("customfield_10000");
        mockFieldManagerControl.setThrowable(new IllegalArgumentException());

        mockFieldManagerControl.replay();

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl(mockFieldManager);
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[] { "customfield_10000" }));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertFalse(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertTrue(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManagerControl.verify();
    }


    @Test
    public void testRegualAndCustomFieldParamsDefined()
    {
        MockControl mockFieldManagerControl = MockClassControl.createControl(FieldManager.class);
        FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getOrderableFields();
        mockFieldManagerControl.setReturnValue(getOrderableFieldIds());
        mockFieldManager.getField("name1");
        mockFieldManagerControl.setReturnValue(createField("name1", null, null));
        mockFieldManager.getCustomField("customfield_10001");
        mockFieldManagerControl.setReturnValue(createCustomField("customfield_10001"));

        mockFieldManagerControl.replay();

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl(mockFieldManager);
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[] { "name1", "customfield_10001" }));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertTrue(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertFalse(issueViewFieldParams.getFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManagerControl.verify();
    }

    @Test
    public void testRegualAndNonIssueAndAllCustomFieldParamsDefined()
    {
        MockControl mockFieldManagerControl = MockClassControl.createControl(FieldManager.class);
        FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getOrderableFields();
        mockFieldManagerControl.setReturnValue(getOrderableFieldIds());
        mockFieldManager.getField("name1");
        mockFieldManagerControl.setReturnValue(createField("name1", null, null));
        mockFieldManager.getField("link");
        mockFieldManagerControl.setReturnValue(null);

        mockFieldManagerControl.replay();

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl(mockFieldManager);
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[] { "name1", "link", "allcustom" }));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertTrue(issueViewFieldParams.isAnyFieldDefined());
        assertTrue(issueViewFieldParams.isAllCustomFields());
        assertFalse(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManagerControl.verify();
    }

    @Test
    public void testRegualMappedFieldParamsDefined()
    {
        MockControl mockFieldManagerControl = MockClassControl.createControl(FieldManager.class);
        FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getOrderableFields();
        mockFieldManagerControl.setReturnValue(getOrderableFieldIds());
        mockFieldManager.getField("project");
        mockFieldManagerControl.setReturnValue(createField("project", null, null));

        mockFieldManagerControl.replay();

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl(mockFieldManager);
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[] { "pid" }));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertTrue(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertFalse(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManagerControl.verify();
    }

    @Test
    public void testTimetrackingFieldParamsDefined()
    {
        MockControl mockFieldManagerControl = MockClassControl.createControl(FieldManager.class);
        FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getOrderableFields();
        mockFieldManagerControl.setReturnValue(getOrderableFieldIds());
        mockFieldManager.getField(IssueFieldConstants.TIME_SPENT);
        mockFieldManagerControl.setReturnValue(createField(IssueFieldConstants.TIME_SPENT, null, null));
        mockFieldManager.getField(IssueFieldConstants.TIME_ESTIMATE);
        mockFieldManagerControl.setReturnValue(createField(IssueFieldConstants.TIME_ESTIMATE, null, null));
        mockFieldManager.getField(IssueFieldConstants.TIME_ORIGINAL_ESTIMATE);
        mockFieldManagerControl.setReturnValue(createField(IssueFieldConstants.TIME_ORIGINAL_ESTIMATE, null, null));
        mockFieldManager.getField(IssueFieldConstants.AGGREGATE_TIME_SPENT);
        mockFieldManagerControl.setReturnValue(createField(IssueFieldConstants.AGGREGATE_TIME_SPENT, null, null));
        mockFieldManager.getField(IssueFieldConstants.AGGREGATE_TIME_ESTIMATE);
        mockFieldManagerControl.setReturnValue(createField(IssueFieldConstants.AGGREGATE_TIME_ESTIMATE, null, null));
        mockFieldManager.getField(IssueFieldConstants.AGGREGATE_TIME_ORIGINAL_ESTIMATE);
        mockFieldManagerControl.setReturnValue(createField(IssueFieldConstants.AGGREGATE_TIME_ORIGINAL_ESTIMATE, null, null));

        mockFieldManagerControl.replay();

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl(mockFieldManager);
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[] { IssueFieldConstants.TIMETRACKING }));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertTrue(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertFalse(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManagerControl.verify();
    }

    @Test
    public void testTimespentFieldParamsDefined()
    {
        MockControl mockFieldManagerControl = MockClassControl.createControl(FieldManager.class);
        FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getOrderableFields();
        mockFieldManagerControl.setReturnValue(getOrderableFieldIds());
        mockFieldManager.getField(IssueFieldConstants.TIME_SPENT);
        mockFieldManagerControl.setReturnValue(createField(IssueFieldConstants.TIME_SPENT, null, null));

        mockFieldManagerControl.replay();

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl(mockFieldManager);
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[] { IssueFieldConstants.TIME_SPENT }));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertTrue(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertFalse(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManagerControl.verify();
    }

    private Set<OrderableField> getOrderableFieldIds()
    {
        Set<OrderableField> fields = new HashSet<OrderableField>();
        fields.add(createOrderableField("name1", null, null));
        fields.add(createOrderableField("name2", null, null));
        fields.add(createOrderableField("name3", null, null));
        fields.add(createOrderableField("name4", null, null));
        return fields;
    }

    private OrderableField createOrderableField(final String id, final String name, final String nameKey)
    {
        final MockOrderableField mockOrderableField = new MockOrderableField(id, name);
        return mockOrderableField;
            }

    private Field createField(final String id, final String name, final String nameKey)
    {
        return new Field()
        {

            public String getId()
            {
                return id;
            }

            public String getNameKey()
            {
                return nameKey;
            }

            public String getName()
            {
                return name;
            }

            public int compareTo(final Object o)
            {
                return 0;
            }
        };
    }

    private CustomField createCustomField(final String id)
    {
        final MockCustomField customField = new MockCustomField();
        customField.setId(id);
        return customField;
    }
}
