package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.action.issue.customfields.MockCustomFieldType;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.MockCustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;

import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

public class TestUpgradeTask_Build571
{

    @Test
    public void testMetaData()
    {
        UpgradeTask_Build571 upgradeTask = new UpgradeTask_Build571(null, null, null);

        assertEquals("571", upgradeTask.getBuildNumber());
        assertEquals("Initialize versions & components field renderers to autocomplete renderer default", upgradeTask.getShortDescription());
    }
    
    @Test
    public void testDoUpgrade() throws Exception
    {
        final OfBizDelegator mockOfBizDelegator = createMock(OfBizDelegator.class);
        final CustomFieldManager mockCustomFieldManager = createMock(CustomFieldManager.class);
        final FieldLayoutManager mockLayoutManager = createMock(FieldLayoutManager.class);

        final GenericValue mockFieldLayoutItemGv1 = new MockGenericValue("FieldLayoutItem",
                MapBuilder.<String, Object>newBuilder().add("id", 10034L).add("fieldidentifier", "versions").toMap());
        final GenericValue mockFieldLayoutItemGv2 = new MockGenericValue("FieldLayoutItem",
                MapBuilder.<String, Object>newBuilder().add("id", 10064L).add("fieldidentifier", "components").toMap());
        final GenericValue mockFieldLayoutItemGv3 = new MockGenericValue("FieldLayoutItem",
                MapBuilder.<String, Object>newBuilder().add("id", 10037L).add("fieldidentifier", "fixVersions").toMap());
        final GenericValue mockFieldLayoutItemGv4 = new MockGenericValue("FieldLayoutItem",
                MapBuilder.<String, Object>newBuilder().add("id", 10039L).add("fieldidentifier", "customfield_23405").toMap());
        final GenericValue mockFieldLayoutItemGv5 = new MockGenericValue("FieldLayoutItem",
                MapBuilder.<String, Object>newBuilder().add("id", 10099L).add("fieldidentifier", "customfield_10780").toMap());
        final GenericValue mockFieldLayoutItemGv6 = new MockGenericValue("FieldLayoutItem",
                MapBuilder.<String, Object>newBuilder().add("id", 10079L).add("fieldidentifier", "customfield_10785").toMap());

        expect(mockOfBizDelegator.findByCondition("FieldLayoutItem", null, CollectionBuilder.list("fieldidentifier", "id"))).andReturn(CollectionBuilder.list(mockFieldLayoutItemGv1,
                mockFieldLayoutItemGv2, mockFieldLayoutItemGv3, mockFieldLayoutItemGv4, mockFieldLayoutItemGv5, mockFieldLayoutItemGv6));

        final MockCustomField cf1 = new MockCustomField();
        cf1.setCustomFieldType(new MockCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:multiversion", "Multi picker"));
        final MockCustomField cf2 = new MockCustomField();
        cf2.setCustomFieldType(new MockCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:text", "text picker"));
        final MockCustomField cf3 = new MockCustomField();
        cf3.setCustomFieldType(new MockCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:multiversion", "Another version"));
        expect(mockCustomFieldManager.getCustomFieldObject("customfield_23405")).andReturn(cf1);
        expect(mockCustomFieldManager.getCustomFieldObject("customfield_10780")).andReturn(cf2);
        expect(mockCustomFieldManager.getCustomFieldObject("customfield_10785")).andReturn(cf3);

        expect(mockOfBizDelegator.bulkUpdateByPrimaryKey("FieldLayoutItem",
                MapBuilder.singletonMap("renderertype", "frother-control-renderer"), CollectionBuilder.list(10039L, 10079L))).andReturn(2);
        expect(mockOfBizDelegator.bulkUpdateByAnd("FieldLayoutItem",
                MapBuilder.singletonMap("renderertype", "frother-control-renderer"), MapBuilder.singletonMap("fieldidentifier", "fixVersions"))).andReturn(1);
        expect(mockOfBizDelegator.bulkUpdateByAnd("FieldLayoutItem",
                MapBuilder.singletonMap("renderertype", "frother-control-renderer"), MapBuilder.singletonMap("fieldidentifier", "versions"))).andReturn(1);
        expect(mockOfBizDelegator.bulkUpdateByAnd("FieldLayoutItem",
                MapBuilder.singletonMap("renderertype", "frother-control-renderer"), MapBuilder.singletonMap("fieldidentifier", "components"))).andReturn(1);

        mockLayoutManager.refresh();

        replay(mockCustomFieldManager, mockOfBizDelegator, mockLayoutManager);

        UpgradeTask_Build571 upgradeTask = new UpgradeTask_Build571(mockOfBizDelegator, mockCustomFieldManager, mockLayoutManager);
        upgradeTask.doUpgrade(false);

        verify(mockCustomFieldManager, mockOfBizDelegator, mockLayoutManager);
    }
}
