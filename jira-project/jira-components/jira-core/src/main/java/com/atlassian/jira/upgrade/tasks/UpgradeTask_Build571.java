package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.renderer.HackyRendererType;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.collect.MultiMap;
import com.atlassian.jira.util.collect.MultiMaps;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This upgrade task initializes field renderers to the frother-control-renderer for the system versions & components
 * fields as well as the multi version custom field.
 *
 * @since v4.2
 */
public class UpgradeTask_Build571 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build571.class);

    private final OfBizDelegator ofBizDelegator;
    private final CustomFieldManager customFieldManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final MultiMap<String, Long, Set<Long>> customFieldIdToFieldLayoutItemId = MultiMaps.createSetMultiMap();
    private static final String FIELD_LAYOUT_ITEM = "FieldLayoutItem";
    private static final String FIELD_RENDERERTYPE = "renderertype";
    private static final String FIELD_FIELDIDENTIFIER = "fieldidentifier";
    private static final String MULTI_VERSION_CF_KEY = "com.atlassian.jira.plugin.system.customfieldtypes:multiversion";
    private static final String FIELD_ID = "id";

    public UpgradeTask_Build571(final OfBizDelegator ofBizDelegator, final CustomFieldManager customFieldManager, final FieldLayoutManager fieldLayoutManager)
    {
        super(false);
        this.ofBizDelegator = ofBizDelegator;
        this.customFieldManager = customFieldManager;
        this.fieldLayoutManager = fieldLayoutManager;
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {

        //figure out which customfields to update and bulk update all of them
        //this table should not be overly large so reading it all into a List should be fine rather than iterating.
        final List<GenericValue> fieldLayoutItemGVs = ofBizDelegator.findByCondition(FIELD_LAYOUT_ITEM, null, CollectionBuilder.list(FIELD_FIELDIDENTIFIER, FIELD_ID));
        for (GenericValue fieldLayoutItemGV : fieldLayoutItemGVs)
        {
            final String customFieldId = fieldLayoutItemGV.getString(FIELD_FIELDIDENTIFIER);
            if (customFieldId.startsWith(CustomFieldUtils.CUSTOM_FIELD_PREFIX))
            {
                customFieldIdToFieldLayoutItemId.putSingle(customFieldId, fieldLayoutItemGV.getLong(FIELD_ID));
            }
        }
        final List<Long> fieldLayoutItemIdsToUpdate = new ArrayList<Long>();
        for (Map.Entry<String, Set<Long>> entry : customFieldIdToFieldLayoutItemId.entrySet())
        {
            final CustomField customFieldObject = customFieldManager.getCustomFieldObject(entry.getKey());
            if (customFieldObject != null)
            {
                final String customFieldKey = customFieldObject.getCustomFieldType().getKey();
                if (customFieldKey.equals(MULTI_VERSION_CF_KEY))
                {
                    fieldLayoutItemIdsToUpdate.addAll(entry.getValue());
                }
            }
        }
        try
        {
            log.info("Updating " + fieldLayoutItemIdsToUpdate.size() + " custom field field layout item(s) with autocomplete renderer default");
            ofBizDelegator.bulkUpdateByPrimaryKey(FIELD_LAYOUT_ITEM,
                    MapBuilder.singletonMap(FIELD_RENDERERTYPE, HackyRendererType.FROTHER_CONTROL.getKey()),
                    fieldLayoutItemIdsToUpdate);

            //update all the system fields with the frothe control renderer
            log.info("Updating system fields field layout item(s) with autocomplete renderer default");
            ofBizDelegator.bulkUpdateByAnd(FIELD_LAYOUT_ITEM,
                    MapBuilder.singletonMap(FIELD_RENDERERTYPE, HackyRendererType.FROTHER_CONTROL.getKey()),
                    MapBuilder.build(FIELD_FIELDIDENTIFIER, IssueFieldConstants.FIX_FOR_VERSIONS));
            ofBizDelegator.bulkUpdateByAnd(FIELD_LAYOUT_ITEM,
                    MapBuilder.singletonMap(FIELD_RENDERERTYPE, HackyRendererType.FROTHER_CONTROL.getKey()),
                    MapBuilder.build(FIELD_FIELDIDENTIFIER, IssueFieldConstants.AFFECTED_VERSIONS));
            ofBizDelegator.bulkUpdateByAnd(FIELD_LAYOUT_ITEM,
                    MapBuilder.singletonMap(FIELD_RENDERERTYPE, HackyRendererType.FROTHER_CONTROL.getKey()),
                    MapBuilder.build(FIELD_FIELDIDENTIFIER, IssueFieldConstants.COMPONENTS));
        }
        finally
        {
            //clear the fieldlayout manager cache to make sure we don't display stale values in the UI after
            //the upgrade.
            fieldLayoutManager.refresh();
        }

    }

    @Override
    public String getShortDescription()
    {
        return "Initialize versions & components field renderers to autocomplete renderer default";
    }

    @Override
    public String getBuildNumber()
    {
        return "571";
    }
}
