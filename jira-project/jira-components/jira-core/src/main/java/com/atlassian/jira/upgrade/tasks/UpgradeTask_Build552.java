package com.atlassian.jira.upgrade.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.entity.GenericValueFunctions;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.LabelsSystemField;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenStore;
import com.atlassian.jira.issue.label.CachingLabelStore;
import com.atlassian.jira.issue.label.OfBizLabelStore;
import com.atlassian.jira.issue.search.CachingSearchRequestStore;
import com.atlassian.jira.issue.search.ClauseRenamingCloningVisitor;
import com.atlassian.jira.jql.parser.JqlParseException;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.jql.util.JqlStringSupport;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.portal.CachingPortletConfigurationStore;
import com.atlassian.jira.portal.OfbizPortletConfigurationStore;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.task.context.Contexts;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.collect.Sized;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.ClauseVisitor;

import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericModelException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.collect.CollectionBuilder.list;
import static com.atlassian.jira.util.collect.MapBuilder.newBuilder;

/**
 * Upgrade task to convert Label customfields to system fields.  The upgrade task looks for custom fields called
 * 'Labels' or the equivalent in other languages and will migrate their data to the new Labels system field.  If there's
 * several custom fields called 'Labels' they'll all be merged into the system field regardless of their context. All
 * other labels custom fields will simply be switched over to the new core labels custom field definition.
 * <p/>
 * This upgrade task will also update issue navigator columns, search requests, gadget configurations and delete any
 * values stored in the custom field values table for labels custom fields.
 *
 * @since v4.2
 */
public class UpgradeTask_Build552 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build552.class);

    public static final String CF_ENTITY = "CustomField";
    public static final String CF_VALUE_ENTITY = "CustomFieldValue";
    private static final String FIELD_LAYOUT_ITEM_ENTITY = "FieldLayoutItem";
    private static final String FIELD_SCREEN_LAYOUT_ITEM_ENTITY = "FieldScreenLayoutItem";
    private static final String FIELD_IDENTIFIER = "fieldidentifier";

    public static final String LABELS_CF_KEY = "com.atlassian.jira.plugin.labels:labels";
    public static final String LABEL_GADGET = "rest/gadgets/1.0/g/com.atlassian.jira.plugin.labels:labels-gadget/templates/plugins/labels/gadget/labels-gadget.xml";
    public static final String NEW_LABEL_GADGET = "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:labels-gadget/gadgets/labels-gadget.xml";

    private final OfBizDelegator ofBizDelegator;
    private final LocaleManager localeManager;
    private final ColumnLayoutManager columnLayoutManager;
    private final JqlQueryParser jqlQueryParser;
    private final JqlStringSupport jqlStringSupport;
    private final CustomFieldManager customFieldManager;
    private final I18nHelper.BeanFactory beanFactory;
    private final FieldScreenSchemeManager fieldScreenSchemeManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final FieldScreenStore fieldScreenStore;
    private final CachingLabelStore cachingLabelStore;
    private final CachingSearchRequestStore cachingSearchRequestStore;
    private final CachingPortletConfigurationStore cachingPortletConfigurationStore;
    private final List<GenericValue> fieldsToConvertToSystemGvs = new ArrayList<GenericValue>();


    public UpgradeTask_Build552(final OfBizDelegator ofBizDelegator, final LocaleManager localeManager,
            final ColumnLayoutManager columnLayoutManager, final JqlQueryParser jqlQueryParser,
            final JqlStringSupport jqlStringSupport, final CustomFieldManager customFieldManager,
            final I18nHelper.BeanFactory beanFactory, final FieldScreenSchemeManager fieldScreenSchemeManager,
            final FieldLayoutManager fieldLayoutManager, final FieldScreenStore fieldScreenStore, final CachingLabelStore cachingLabelStore,
            final CachingSearchRequestStore cachingSearchRequestStore, final CachingPortletConfigurationStore cachingPortletConfigurationStore)
    {
        super(false);
        this.ofBizDelegator = ofBizDelegator;
        this.localeManager = localeManager;
        this.columnLayoutManager = columnLayoutManager;
        this.jqlQueryParser = jqlQueryParser;
        this.jqlStringSupport = jqlStringSupport;
        this.customFieldManager = customFieldManager;
        this.beanFactory = beanFactory;
        this.fieldScreenSchemeManager = fieldScreenSchemeManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.fieldScreenStore = fieldScreenStore;
        this.cachingLabelStore = cachingLabelStore;
        this.cachingSearchRequestStore = cachingSearchRequestStore;
        this.cachingPortletConfigurationStore = cachingPortletConfigurationStore;
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        final List<GenericValue> customFieldGVs =
                ofBizDelegator.findByAnd(CF_ENTITY, MapBuilder.singletonMap("customfieldtypekey", LABELS_CF_KEY));
        //only need to do something if we actually have any old label custom fields configured!
        if (!customFieldGVs.isEmpty())
        {
            //first figure out which custom field(s) need to be merged into the system field. (all fields named 'Labels'
            //in multiple languages)
            final List<Long> fieldsToConvertToSystem = new ArrayList<Long>();
            final List<Long> customFieldIds = new ArrayList<Long>();
            final CustomFieldNameMatchingPredicate fieldNamePredicate = new CustomFieldNameMatchingPredicate("Labels", "issue.field.labels", localeManager, beanFactory);
            for (GenericValue customFieldGV : customFieldGVs)
            {
                if (fieldNamePredicate.evaluate(customFieldGV))
                {
                    fieldsToConvertToSystemGvs.add(customFieldGV);
                    fieldsToConvertToSystem.add(customFieldGV.getLong("id"));
                }
                customFieldIds.add(customFieldGV.getLong("id"));
            }

            log.info("Migrating labels custom field data to new table...");
            migrateCustomFieldData(fieldsToConvertToSystem, customFieldIds);
            log.info("Migrating labels custom field data to new table...DONE");

            //only need to convert navigator columns, search requests & gadgets for customfields converted to systemfields
            //for customfields, all of these will stay the same since only the CF Type & CF Searcher type are being
            //modified
            if (!fieldsToConvertToSystem.isEmpty())
            {
                log.info("Updating field screens and field configurations with new system field...");
                updateFieldScreensWithSystemField(fieldsToConvertToSystem);
                log.info("Updating field screens and field configurations with new system field...DONE");

                log.info("Updating issue navigator columns to use the new labels system field...");
                updateIssueNavigatorColumns(fieldsToConvertToSystem);
                log.info("Updating issue navigator columns to use the new labels system field...DONE");

                log.info("Updating saved filters to use the new labels system field...");
                updateSearchRequests();
                log.info("Updating saved filters to use the new labels system field...DONE");

                log.info("Updating label gadgets use the new labels system field...");
                updateGadgetConfigurations(fieldsToConvertToSystem);
                log.info("Updating label gadgets use the new labels system field...DONE");
            }

            log.info("Converting labels custom fields to new system custom fields...");
            //for all other custom fields, change the customfield type & searcher type to the new
            // system custom field type.  If there's no searcher defined we're still going to set it.  Customers can
            //always disable it again via the admin section.  Not having a searcher doesn't make too much sense because
            //label suggestions in the label dialog wont work.
            ofBizDelegator.bulkUpdateByAnd(CF_ENTITY,
                    FieldMap.build("customfieldtypekey", "com.atlassian.jira.plugin.system.customfieldtypes:labels").
                            add("customfieldsearcherkey", "com.atlassian.jira.plugin.system.customfieldtypes:labelsearcher"),
                    FieldMap.build("customfieldtypekey", LABELS_CF_KEY));
            log.info("Converting labels custom fields to new system custom fields...DONE");


            log.info("Removing label custom fields that were converted to system fields...");
            //finally remove the custom fields converted to system fields and clean up the custom field values table
            for (Long customFieldId : fieldsToConvertToSystem)
            {
                try
                {
                    customFieldManager.removeCustomFieldPossiblyLeavingOrphanedData(customFieldId);
                }
                catch (final RemoveException e)
                {
                    throw new RuntimeException("Error removing customfield '" + customFieldId + "'", e);
                }
            }
            log.info("Removing label custom fields that were converted to system fields...DONE");

            log.info("Cleaning up custom field values table...");
            ofBizDelegator.removeByOr(CF_VALUE_ENTITY, "customfield", customFieldIds);
            log.info("Cleaning up custom field values table...DONE");

            //finally make sure we refresh the custom field manager to ensure it's cache is in sync with the database (JRADEV-3619)
            customFieldManager.refresh();
            cachingLabelStore.onClearCache(null);
        }
    }

    void updateFieldScreensWithSystemField(final List<Long> fieldIdsToConvertToSystem)
    {
        try
        {
            for (final Long customFieldId : fieldIdsToConvertToSystem)
            {
                ofBizDelegator.bulkUpdateByAnd(FIELD_LAYOUT_ITEM_ENTITY,
                        MapBuilder.singletonMap(FIELD_IDENTIFIER, IssueFieldConstants.LABELS),
                        MapBuilder.singletonMap(FIELD_IDENTIFIER, CustomFieldUtils.CUSTOM_FIELD_PREFIX + customFieldId));

                ofBizDelegator.bulkUpdateByAnd(FIELD_SCREEN_LAYOUT_ITEM_ENTITY,
                        MapBuilder.singletonMap(FIELD_IDENTIFIER, IssueFieldConstants.LABELS),
                        MapBuilder.singletonMap(FIELD_IDENTIFIER, CustomFieldUtils.CUSTOM_FIELD_PREFIX + customFieldId));
            }

            //need to remove duplicates.  Once we've converted all the fieldlayout items/screen items to the
            //system field, there may be duplicates per field layout/screen since multiple customfields could have
            //been on a single layout/screen.
            removeDuplicateLayoutItems(FIELD_LAYOUT_ITEM_ENTITY, "fieldlayout");
            removeDuplicateLayoutItems(FIELD_SCREEN_LAYOUT_ITEM_ENTITY, "fieldscreentab");
        }
        finally
        {
            //make sure the manager's caches are properly cleared so they'll pick up the DB changes!
            fieldLayoutManager.refresh();
            fieldScreenStore.refresh();
            fieldScreenSchemeManager.refresh();
        }
    }

    private void removeDuplicateLayoutItems(String table, String relatedTable)
    {
        final List<Long> itemIdsToRemove = new ArrayList<Long>();
        final Set<Long> existingRelationIds = new HashSet<Long>();
        final List<GenericValue> itemGVs = ofBizDelegator.findByAnd(table, MapBuilder.singletonMap(FIELD_IDENTIFIER, IssueFieldConstants.LABELS));
        for (GenericValue itemGv : itemGVs)
        {
            final Long relationId = itemGv.getLong(relatedTable);
            //if we already have a labels field for the related table (i.e. fieldlayout or screen layout)
            //then remove this one.
            if(existingRelationIds.contains(relationId))
            {
                itemIdsToRemove.add(itemGv.getLong("id"));
            }
            else
            {
                existingRelationIds.add(relationId);
            }
        }
        
        if(!itemIdsToRemove.isEmpty())
        {
            try
            {
                ofBizDelegator.removeByOr(table, "id", itemIdsToRemove);
            }
            catch (GenericModelException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    void updateGadgetConfigurations(final List<Long> fieldsToConvertToSystem) throws GenericEntityException
    {
        final List<GenericValue> labelsGadgetGVs = ofBizDelegator.findByLike(OfbizPortletConfigurationStore.TABLE,
                MapBuilder.singletonMap(OfbizPortletConfigurationStore.Columns.GADGET_XML, LABEL_GADGET));
        final List<Long> labelGadgetIds = CollectionUtil.transform(labelsGadgetGVs, GenericValueFunctions.getLong(OfbizPortletConfigurationStore.Columns.ID));
        if (!labelGadgetIds.isEmpty())
        {
            final EntityCondition entityCondition = new EntityConditionList(Arrays.asList(new EntityExpr(OfbizPortletConfigurationStore.UserPreferenceColumns.PORTLETID,
                    EntityOperator.IN, labelGadgetIds), new EntityExpr(OfbizPortletConfigurationStore.UserPreferenceColumns.KEY, EntityOperator.EQUALS, "fieldId")), EntityOperator.AND);

            final List<GenericValue> userPrefGVs = ofBizDelegator.findByCondition(OfbizPortletConfigurationStore.USER_PREFERENCES_TABLE, entityCondition, null);
            for (GenericValue userPrefGV : userPrefGVs)
            {
                final String value = userPrefGV.getString(OfbizPortletConfigurationStore.UserPreferenceColumns.VALUE);
                final Long id = CustomFieldUtils.getCustomFieldId(value);
                if (id != null && fieldsToConvertToSystem.contains(id))
                {
                    userPrefGV.setString(OfbizPortletConfigurationStore.UserPreferenceColumns.VALUE, IssueFieldConstants.LABELS);
                    userPrefGV.store();
                }
            }

            //finally swap over to the new system gadget!
            ofBizDelegator.bulkUpdateByPrimaryKey(OfbizPortletConfigurationStore.TABLE,
                    MapBuilder.singletonMap(OfbizPortletConfigurationStore.Columns.GADGET_XML, NEW_LABEL_GADGET),
                    labelGadgetIds);

            cachingPortletConfigurationStore.onClearCache(null);
        }
    }

    void updateIssueNavigatorColumns(final List<Long> fieldsToConvertToSystem)
    {
        try
        {
            // first get the minimal data set required to determine which columnlayoutitems need updating
            final Map<Long, Long> layoutItemsToChange = new LinkedHashMap<Long, Long>();
            final List<Long> layoutItemsToRemove = new ArrayList<Long>();
            getColumnLayoutItemsToProcess(fieldsToConvertToSystem, layoutItemsToChange, layoutItemsToRemove);

            // remove the columnlayoutitems that need to be removed
            if (!layoutItemsToRemove.isEmpty())
            {
                ofBizDelegator.removeByOr("ColumnLayoutItem", "id", layoutItemsToRemove);
            }

            // update the columnlayoutitems to use the system field
            if (!layoutItemsToChange.isEmpty())
            {
                ofBizDelegator.bulkUpdateByPrimaryKey("ColumnLayoutItem", newBuilder(FIELD_IDENTIFIER, IssueFieldConstants.LABELS).toMap(),
                        new ArrayList<Long>(layoutItemsToChange.keySet()));
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        finally
        {
            //clear the columnlayoutmanager cache to ensure the columnlayouts will be reloaded from the DB, just in case
            //any upgrade tasks populated the cache before this upgrade task ran.
            columnLayoutManager.refresh();
        }
    }

    private void getColumnLayoutItemsToProcess(final List<Long> customFieldIds, final Map<Long, Long> layoutItemsToChange, final List<Long> layoutItemsToRemove)
    {
        List<String> customFieldStringIds = CollectionUtil.transform(customFieldIds, new Function<Long, String>()
        {
            public String get(final Long input)
            {
                return CustomFieldUtils.CUSTOM_FIELD_PREFIX + input;
            }
        });

        OfBizListIterator layoutItemsIterator = null;
        try
        {
            layoutItemsIterator = ofBizDelegator.findListIteratorByCondition("ColumnLayoutItem", new EntityExpr(FIELD_IDENTIFIER, EntityOperator.IN,
                    customFieldStringIds), null, Lists.newArrayList("id", "columnlayout"), Lists.newArrayList("columnlayout ASC", "horizontalposition ASC"), null);

            // keep track of which columnlayouts have multiple layoutitems with the custom field:
            // the first columnlayoutitem will be marked for change; the rest will be marked for removal.
            for (GenericValue gv = layoutItemsIterator.next(); gv != null; gv = layoutItemsIterator.next())
            {
                final Long layoutId = gv.getLong("columnlayout");
                final Long layoutItemId = gv.getLong("id");
                if (layoutItemsToChange.containsValue(layoutId))
                {
                    layoutItemsToRemove.add(layoutItemId);
                }
                else
                {
                    layoutItemsToChange.put(layoutItemId, layoutId);
                }
            }
        }
        finally
        {
            if (layoutItemsIterator != null)
            {
                layoutItemsIterator.close();
            }
        }
    }

    void migrateCustomFieldData(final List<Long> fieldsToConvertToSystem, final List<Long> customFieldIds)
    {
        //Copy the data accross for all custom fields!
        OfBizListIterator iterator = null;
        try
        {
            iterator = ofBizDelegator.findListIteratorByCondition(CF_VALUE_ENTITY, new EntityExpr("customfield", EntityOperator.IN,
                    customFieldIds), null, Lists.newArrayList("issue", "customfield", "textvalue"), null, null);

            for (GenericValue gv = iterator.next(); gv != null; gv = iterator.next())
            {
                final Long issueId = gv.getLong("issue");
                final Long customFieldId = gv.getLong("customfield");
                final String[] labels = StringUtils.split(gv.getString("textvalue"), LabelsSystemField.SEPARATOR_CHAR);
                if (labels != null)
                {
                    final Map<String, Object> params = MapBuilder.<String, Object>newBuilder().add(OfBizLabelStore.Columns.ISSUE_ID, issueId).toMutableMap();
                    if (!fieldsToConvertToSystem.contains(customFieldId))
                    {
                        params.put(OfBizLabelStore.Columns.CUSTOM_FIELD_ID, customFieldId);
                    }
                    for (String label : labels)
                    {
                        params.put(OfBizLabelStore.Columns.LABEL, label);
                        ofBizDelegator.createValue(OfBizLabelStore.TABLE, params);
                    }
                }
            }
        }
        finally
        {
            if (iterator != null)
            {
                iterator.close();
            }
        }
    }

    void updateSearchRequests() throws GenericEntityException
    {
        // Because MSSQL row/table locking sucks we can not just iterate over the SearchRequests, instead we need
        // to get all the ID's and then update the records in batches.
        final List<Long> searchRequestIds = getSearchRequestIds();

        // Lets create a logger that we can use to show how much of the upgrade task has been completed
        final Context percentageLogger = getSearchRequestPercentageLogger(searchRequestIds);

        // Lets run through all the search requests, converting them in batches of 200
        final List<Long> batchSearchRequestIds = new ArrayList<Long>();
        for (final Long searchRequestId : searchRequestIds)
        {
            batchSearchRequestIds.add(searchRequestId);
            if (batchSearchRequestIds.size() >= 200)
            {
                // If we hit our batch limit lets go ahead and convert what we have so far.
                updateSearchRequests(getSearchRequestGvsForIds(batchSearchRequestIds), percentageLogger);
                batchSearchRequestIds.clear();
            }
        }

        // Lets do the last batch which may be under 200
        if (!batchSearchRequestIds.isEmpty())
        {
            updateSearchRequests(getSearchRequestGvsForIds(batchSearchRequestIds), percentageLogger);
            cachingSearchRequestStore.onClearCache(null);
        }
    }

    Context getSearchRequestPercentageLogger(final List<Long> searchRequestIds)
    {
        final Context percentageLogger = Contexts.percentageLogger(new Sized()
        {
            public int size()
            {
                return searchRequestIds.size();
            }

            public boolean isEmpty()
            {
                return searchRequestIds.size() == 0;
            }
        }, log, "Converting search requests to use the new labels system field is {0}% complete.");

        percentageLogger.setName("Converting search requests to use the new labels system field.");
        return percentageLogger;
    }

    List<GenericValue> getSearchRequestGvsForIds(final List<Long> batchSearchRequestIds)
    {
        // look up all the SearchRequest GenericValues for the ID's in this batch
        final EntityCondition idCondition = new EntityExpr("id", EntityOperator.IN, batchSearchRequestIds);

        return ofBizDelegator.findByCondition("SearchRequest", idCondition, null);
    }

    List<Long> getSearchRequestIds()
    {
        final List<Long> searchRequestIds = new ArrayList<Long>();
        final List<GenericValue> searchRequestIdGvs = ofBizDelegator.findByCondition("SearchRequest", null, list("id"));
        for (final GenericValue searchRequestIdGv : searchRequestIdGvs)
        {
            searchRequestIds.add(searchRequestIdGv.getLong("id"));
        }
        return searchRequestIds;
    }

    private void updateSearchRequests(final List<GenericValue> searchRequestGvs, final Context percentageLogger)
            throws GenericEntityException
    {
        Map<String, String> substitutions = getSubstitutions(fieldsToConvertToSystemGvs);
        for (final GenericValue searchRequestGv : searchRequestGvs)
        {
            final Context.Task task = percentageLogger.start(searchRequestGv);
            try
            {
                updateSearchRequestIfContainsCustomFieldIds(searchRequestGv, substitutions);
            }
            finally
            {
                task.complete();
            }
        }
    }

    private void updateSearchRequestIfContainsCustomFieldIds(final GenericValue searchRequestGv, final Map<String, String> substitutions)
            throws GenericEntityException
    {
        // Read in the SearchRequest XML
        final String jql = searchRequestGv.getString("request");

        //JRA-23588 - Oracle can read in an empty string as null
        final String sanitisedJql = (jql == null) ? "" : jql;

        try
        {
            final Query query = jqlQueryParser.parseQuery(sanitisedJql);
            if (query.getWhereClause() != null)
            {
                ClauseVisitor<Clause> renamer = new ClauseRenamingCloningVisitor(substitutions);
                final Clause renamedClause = query.getWhereClause().accept(renamer);
                if (!renamedClause.equals(query.getWhereClause()))
                {
                    final QueryImpl newQuery = new QueryImpl(renamedClause, query.getOrderByClause(), null);
                    searchRequestGv.setString("request", jqlStringSupport.generateJqlString(newQuery));
                    searchRequestGv.store();
                }
            }
        }
        catch (JqlParseException e)
        {
            log.error("Error parsing query '" + jql + "'.  The Filter '" + searchRequestGv.getString("name") + "' will not be converted to use the new Labels system field!");
        }
    }

    Map<String, String> getSubstitutions(final List<GenericValue> fieldsToConvertToSystemGvs)
    {
        Map<String, String> substitutions = new HashMap<String, String>();
        for (GenericValue field : fieldsToConvertToSystemGvs)
        {
            String name = field.getString("name");
            Long id = field.getLong("id");
            substitutions.put(name, IssueFieldConstants.LABELS);
            substitutions.put("cf[" + id + "]", IssueFieldConstants.LABELS);
        }
        return substitutions;
    }

    @Override
    public String getShortDescription()
    {
        return "Converts label custom fields to the new label system field.";
    }

    @Override
    public String getBuildNumber()
    {
        return "552";
    }

    static class CustomFieldNameMatchingPredicate implements Predicate<GenericValue>
    {
        private final Set<String> labelNames = new HashSet<String>();

        public CustomFieldNameMatchingPredicate(final String name, final String i18nKey, final LocaleManager localeManager, final I18nHelper.BeanFactory beanFactory)
        {
            labelNames.add(name);
            for (Locale locale : localeManager.getInstalledLocales())
            {
                labelNames.add(beanFactory.getInstance(locale).getText(i18nKey));
            }
        }

        public boolean evaluate(final GenericValue input)
        {
            final String cfName = input.getString("name");
            return labelNames.contains(cfName);
        }
    }
}