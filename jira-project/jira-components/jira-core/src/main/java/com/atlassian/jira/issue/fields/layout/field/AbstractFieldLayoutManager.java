package com.atlassian.jira.issue.fields.layout.field;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.field.CustomFieldUpdatedEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.map.CacheObject;
import com.atlassian.jira.web.bean.I18nBean;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Iterables.transform;

@EventComponent
abstract public class AbstractFieldLayoutManager implements FieldLayoutManager
{
    private static final Logger LOG = Logger.getLogger(AbstractFieldLayoutManager.class);

    // Entity names
    public static final String FIELD_LAYOUT = "FieldLayout";
    public static final String SCHEME = "FieldLayoutScheme";
    public static final String SCHEME_ASSOCIATION = "ProjectFieldLayoutScheme";

    // Field names
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String TYPE = "type";

    private static final Map<String,Object> IS_TYPE_DEFAULT = ImmutableMap.<String,Object>of(TYPE, TYPE_DEFAULT);

    protected final FieldManager fieldManager;
    protected final OfBizDelegator ofBizDelegator;
    protected final I18nHelper.BeanFactory i18nFactory;

    private final List<FieldLayoutItem> defaultFieldLayoutItems;

    // Stores the scheme's field layouts using scheme as a key
    private final Cache<CacheObject<Long>, FieldLayout> fieldLayoutCache;


    public AbstractFieldLayoutManager(final FieldManager fieldManager, final OfBizDelegator ofBizDelegator,
            final I18nHelper.BeanFactory i18nFactory, final CacheManager cacheManager)
    {
        this.fieldManager = notNull("fieldManager", fieldManager);
        this.ofBizDelegator = ofBizDelegator;
        this.i18nFactory = i18nFactory;

        this.defaultFieldLayoutItems = createDefaultFieldLayoutItems(fieldManager);
        this.fieldLayoutCache = cacheManager.getCache(AbstractFieldLayoutManager.class.getName() + ".fieldLayoutCache",
                new FieldLayoutCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());
    }

    private List<FieldLayoutItem> createDefaultFieldLayoutItems(final FieldManager fieldManager)
    {
        final I18nHelper i18n = i18nFactory.getInstance((ApplicationUser)null);
        return ImmutableList.<FieldLayoutItem>builder()
                .add(createDefaultItem(fieldManager, i18n, IssueFieldConstants.SUMMARY, true))
                .add(createDefaultItem(fieldManager, i18n, IssueFieldConstants.ISSUE_TYPE, true))
                .add(createDefaultItem(fieldManager, i18n, IssueFieldConstants.SECURITY, false))
                .add(createDefaultItem(fieldManager, i18n, IssueFieldConstants.PRIORITY, false))
                .add(createDefaultItem(fieldManager, i18n, IssueFieldConstants.DUE_DATE, false))
                .add(createDefaultItem(fieldManager, i18n, IssueFieldConstants.COMPONENTS, false))
                .add(createDefaultItem(fieldManager, i18n, IssueFieldConstants.AFFECTED_VERSIONS, false))
                .add(createDefaultItem(fieldManager, i18n, IssueFieldConstants.FIX_FOR_VERSIONS, false))
                .add(createDefaultItem(fieldManager, i18n, IssueFieldConstants.ASSIGNEE, false))
                .add(createDefaultItem(fieldManager, i18n, IssueFieldConstants.REPORTER, true))
                .add(createDefaultItem(fieldManager, i18n, IssueFieldConstants.ENVIRONMENT, false))
                .add(createDefaultItem(fieldManager, i18n, IssueFieldConstants.DESCRIPTION, false))
                .add(createDefaultItem(fieldManager, i18n, IssueFieldConstants.TIMETRACKING, false))
                .add(createDefaultItem(fieldManager, i18n, IssueFieldConstants.RESOLUTION, false))
                .add(createDefaultItem(fieldManager, i18n, IssueFieldConstants.ATTACHMENT, false))
                .add(createDefaultItem(fieldManager, i18n, IssueFieldConstants.COMMENT, false))
                .add(createDefaultItem(fieldManager, i18n, IssueFieldConstants.LABELS, false))
                .add(createDefaultItem(fieldManager, i18n, IssueFieldConstants.WORKLOG, false))
                .add(createDefaultItem(fieldManager, i18n, IssueFieldConstants.ISSUE_LINKS, false))
                .build();
    }

    private FieldLayoutItem createDefaultItem(final FieldManager fieldManager, final I18nHelper i18n, final String fieldId, final boolean required)
    {
        return new FieldLayoutItemImpl.Builder()
                .setOrderableField(fieldManager.getOrderableField(fieldId))
                .setFieldDescription(getDefaultDescription(i18n, fieldId))
                .setHidden(false)
                .setRequired(required)
                .setFieldManager(fieldManager)
                .build();
    }



    @EventListener
    public void onCustomFieldUpdated(CustomFieldUpdatedEvent event)
    {
        fieldLayoutCache.removeAll();
    }



    protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
    {
        return defaultFieldLayoutItems;
    }

    public FieldLayout getFieldLayout()
    {
        return getRelevantFieldLayout(null);
    }

    public FieldLayout getFieldLayout(final Issue issue)
    {
        if (issue == null)
        {
            throw new IllegalArgumentException("Issue cannot be null.");
        }
        return getFieldLayout(issue.getProjectObject(), issue.getIssueTypeObject().getId());
    }

    public EditableDefaultFieldLayout getEditableDefaultFieldLayout()
    {
        final FieldLayout relevantFieldLayout = getRelevantFieldLayout(null);
        return new EditableDefaultFieldLayoutImpl(relevantFieldLayout.getGenericValue(), relevantFieldLayout.getFieldLayoutItems());
    }

    public void storeEditableDefaultFieldLayout(final EditableDefaultFieldLayout editableDefaultFieldLayout)
    {
        storeEditableFieldLayout(editableDefaultFieldLayout);
        refreshCaches(editableDefaultFieldLayout.getId());
        refreshCaches(null);
    }

    /*
     * THIS METHOD MUST BE SYNCHRONIZED!!!! So that only one thread updates the database at any one time. "Fields are
     * duplicated" if this method is not synchronized.
     */
    @Override
    public synchronized EditableFieldLayout storeAndReturnEditableFieldLayout(final EditableFieldLayout editableFieldLayout)
    {
        // FieldLayout (id, layoutscheme)
        // FieldLayoutItem (id, fieldlayout, fieldidentifier, verticalposition, ishidden, isrequired)
        // Find the default field layout in the database if it exists

        try
        {
            final OfBizDelegator ofBizDelegator = ComponentAccessor.getOfBizDelegator();

            GenericValue fieldLayoutGV = editableFieldLayout.getGenericValue();

            if (editableFieldLayout.getGenericValue() == null)
            {
                // There is no default, create a new one
                fieldLayoutGV = ofBizDelegator.createValue(FIELD_LAYOUT, FieldMap.build(
                        NAME, editableFieldLayout.getName(),
                        DESCRIPTION, editableFieldLayout.getDescription(),
                        TYPE, editableFieldLayout.getType() ));
            }
            else
            {
                fieldLayoutGV.store();
            }

            // Remove Field Layout Items. The removeRalted method seems to cause problems (duplicated records) on Tomcat, hence it is not used.
            final List<GenericValue> fieldLayoutItemGVs = fieldLayoutGV.getRelated("ChildFieldLayoutItem");
            ofBizDelegator.removeAll(fieldLayoutItemGVs);

            // Retrieve a list of Field Layout Items for this layout
            final List<FieldLayoutItem> fieldLayoutItems = editableFieldLayout.getFieldLayoutItems();
            final Long newId = fieldLayoutGV.getLong("id");
            for (FieldLayoutItem fieldLayoutItem : fieldLayoutItems)
            {
                ofBizDelegator.createValue("FieldLayoutItem", new FieldMap()
                        .add("fieldlayout", newId)
                        .add("description", fieldLayoutItem.getRawFieldDescription())
                        .add("fieldidentifier", fieldLayoutItem.getOrderableField().getId())
                        .add("ishidden", Boolean.toString(fieldLayoutItem.isHidden()))
                        .add("isrequired", Boolean.toString(fieldLayoutItem.isRequired()))
                        .add("renderertype", fieldLayoutItem.getRendererType() ));
            }

            refreshCaches(newId);
            return getEditableFieldLayout(newId);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException("Could not load the default FieldLayout", e);
        }
    }

    public void storeEditableFieldLayout(final EditableFieldLayout editableFieldLayout)
    {
        storeAndReturnEditableFieldLayout(editableFieldLayout);
    }

    protected void refreshCaches(final Long id)
    {
        // Remove the scheme's field layout from the cache
        fieldLayoutCache.remove(CacheObject.wrap(id));

        // Clear the ColumnLayout cache
        ComponentAccessor.getColumnLayoutManager().refresh();
    }

    public boolean hasDefaultFieldLayout()
    {
        final OfBizDelegator ofBizDelegator = ComponentAccessor.getOfBizDelegator();
        final GenericValue fieldLayoutGV = EntityUtil.getOnly(ofBizDelegator.findByAnd("FieldLayout", IS_TYPE_DEFAULT));
        return (fieldLayoutGV == null);
    }

    public void restoreDefaultFieldLayout()
    {
        final OfBizDelegator ofBizDelegator = ComponentAccessor.getOfBizDelegator();
        try
        {
            final GenericValue fieldLayoutGV = EntityUtil.getOnly(ofBizDelegator.findByAnd("FieldLayout", IS_TYPE_DEFAULT));
            if (fieldLayoutGV != null)
            {
                fieldLayoutGV.removeRelated("ChildFieldLayoutItem");
                fieldLayoutGV.remove();
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }

        // Clear the cache
        refresh();
    }

    protected synchronized void restoreFieldLayout(final Long id)
    {
        try
        {
            // Remove the records from the database
            final OfBizDelegator ofBizDelegator = ComponentAccessor.getOfBizDelegator();
            final GenericValue fieldLayoutGV = ofBizDelegator.findById("FieldLayout", id);
            if (fieldLayoutGV != null)
            {
                // Remove Field Layout Items. The removeRalted method seems to cause problems (duplicated records) on Tomcat, hence it is not used.
                final List<GenericValue> fieldLayoutItemGVs = fieldLayoutGV.getRelated("ChildFieldLayoutItem");
                ofBizDelegator.removeAll(fieldLayoutItemGVs);
                fieldLayoutGV.remove();
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }

        // Clear the cache
        refresh();
    }

    public void refresh()
    {
        fieldLayoutCache.removeAll();
    }

    /**
     * Retrieves the field layout given a given id. If the id is null the default field layout is retrieved
     *
     * @param id field layout id
     * @return field layout
     */
    protected FieldLayout getRelevantFieldLayout(final Long id)
    {
        return fieldLayoutCache.get(CacheObject.wrap(id));
    }

    private GenericValue loadDefaultFieldLayoutGenericValue() throws GenericEntityException
    {
        return EntityUtil.getOnly(ofBizDelegator.findByAnd("FieldLayout", IS_TYPE_DEFAULT));
    }

    private GenericValue loadFieldLayoutGenericValue(final Long id) throws GenericEntityException
    {
        if (id != null)
        {
            final GenericValue fieldLayoutGV = ofBizDelegator.findById("FieldLayout", id);
            if (fieldLayoutGV != null)
            {
                return fieldLayoutGV;
            }
        }
        return loadDefaultFieldLayoutGenericValue();
    }

    private List<FieldLayoutItem> loadInitialFieldLayoutItems(final FieldLayout resultingLayout)
    {
        final List<FieldLayoutItem> fieldLayoutItems = new ArrayList<FieldLayoutItem>(defaultFieldLayoutItems.size());
        addAll(fieldLayoutItems, transform(defaultFieldLayoutItems, new SetFieldLayoutAndManager(resultingLayout)));

        final List<CustomField> customFieldObjects = ComponentAccessor.getCustomFieldManager().getCustomFieldObjects();
        for (CustomField customField : customFieldObjects)
        {
            fieldLayoutItems.add(mapCustomField(resultingLayout, customField));
        }
        return fieldLayoutItems;
    }

    private List<FieldLayoutItem> loadFieldLayoutItems(final FieldLayout resultingLayout, final GenericValue fieldLayoutGV)
            throws GenericEntityException
    {
        if (fieldLayoutGV == null)
        {
            return loadInitialFieldLayoutItems(resultingLayout);
        }

        // These objects are ephemeral, so they are sized large-ish
        final Set<String> seenFieldIds = new HashSet<String>(256);
        final List<FieldLayoutItem> fieldLayoutItems = new ArrayList<FieldLayoutItem>(256);

        final List<GenericValue> related = fieldLayoutGV.getRelated("ChildFieldLayoutItem");
        for (GenericValue fieldLayoutItemGV : related)
        {
            final String fieldId = fieldLayoutItemGV.getString("fieldidentifier");
            if (fieldManager.isOrderableField(fieldId))
            {
                seenFieldIds.add(fieldId);
                fieldLayoutItems.add(toFieldLayoutItem(resultingLayout, fieldLayoutItemGV, fieldId));
            }
            else
            {
                // JRA-4423
                LOG.info("Field layout contains non-orderable field with id '" + fieldId + "'.");
            }
        }

        final Set<OrderableField> orderableFields = fieldManager.getOrderableFields();
        for (final OrderableField orderableField : orderableFields)
        {
            if (seenFieldIds.add(orderableField.getId()))
            {
                final FieldLayoutItemImpl fieldLayoutItem = mapOrderableField(resultingLayout, orderableField);
                fieldLayoutItems.add(fieldLayoutItem);
            }
        }

        return fieldLayoutItems;
    }

    private FieldLayout loadFieldLayout(Long id)
    {
        try
        {
            final GenericValue fieldLayoutGV =loadFieldLayoutGenericValue(id);
            final FieldLayoutImpl resultingLayout = new FieldLayoutImpl(fieldLayoutGV, null);

            final List<FieldLayoutItem> fieldLayoutItems;
            if (fieldLayoutGV == null)
            {
                fieldLayoutItems = loadInitialFieldLayoutItems(resultingLayout);
            }
            else
            {
                fieldLayoutItems = loadFieldLayoutItems(resultingLayout, fieldLayoutGV);
            }

            removeUnavailableFields(fieldLayoutItems);
            // associate all FieldLayoutItemImpl with this FieldLayout
            resultingLayout.setFieldLayoutItems(new ArrayList<FieldLayoutItem>(fieldLayoutItems));
            return resultingLayout;
        }
        catch (final GenericEntityException e)
        {
            LOG.error(e, e);
            throw new DataAccessException("Could not retrieve Field Layout.", e);
        }
    }

    private FieldLayoutItemImpl toFieldLayoutItem(final FieldLayout resultingLayout, final GenericValue fieldLayoutItemGV, final String fieldId)
    {
        return new FieldLayoutItemImpl.Builder()
                .setOrderableField(fieldManager.getOrderableField(fieldId))
                .setFieldDescription(fieldLayoutItemGV.getString("description"))
                .setHidden(Boolean.valueOf(fieldLayoutItemGV.getString("ishidden")))
                .setRequired(Boolean.valueOf(fieldLayoutItemGV.getString("isrequired")))
                .setRendererType(fieldLayoutItemGV.getString("renderertype"))
                .setFieldLayout(resultingLayout)
                .setFieldManager(fieldManager)
                .build();
    }

    private FieldLayoutItemImpl mapOrderableField(final FieldLayout resultingLayout, final OrderableField orderableField)
    {
        return new FieldLayoutItemImpl.Builder()
                                    .setOrderableField(orderableField)
                                    .setFieldDescription(getDefaultDescription(orderableField.getId()))
                                    .setHidden(false)
                                    .setRequired(fieldManager.isMandatoryField(orderableField))
                                    .setFieldLayout(resultingLayout)
                                    .setFieldManager(fieldManager)
                                    .build();
    }

    private void removeUnavailableFields(final List<FieldLayoutItem> fieldLayoutItems)
    {
        final Set<Field> unavailableFields = fieldManager.getUnavailableFields();
        for (final Iterator<FieldLayoutItem> iterator = fieldLayoutItems.iterator(); iterator.hasNext();)
        {
            if (unavailableFields.contains(iterator.next().getOrderableField()))
            {
                iterator.remove();
            }
        }
    }


    private FieldLayoutItem mapCustomField(final FieldLayout resultingLayout, final CustomField customField)
    {
        // Always create FieldLayoutItems for custom fields with null descriptions as custom fields have
        // their own descriptions.
        return new FieldLayoutItemImpl.Builder()
                        .setOrderableField(customField)
                        .setFieldDescription(null)
                        .setFieldLayout(resultingLayout)
                        .setFieldManager(fieldManager)
                        .build();
    }

    protected String getDefaultDescription(final String fieldId)
    {
        return getDefaultDescription(getI18nHelper(), fieldId);
    }

    private String getDefaultDescription(final I18nHelper i18n, final String fieldId)
    {
        // TODO : Should get these strings on a per-user basis (i.e. get locale of user and return corresponding string).
        // TODO : At present, the default locale is used.
        if (IssueFieldConstants.ENVIRONMENT.equals(fieldId))
        {
            return i18n.getText("environment.field.description");
        }
        else if (IssueFieldConstants.TIMETRACKING.equals(fieldId))
        {
            return i18n.getText("timetracking.field.description", "*w *d *h *m", "4d, 5h 30m, 60m", "3w");
        }
        else if (IssueFieldConstants.WORKLOG.equals(fieldId))
        {
            return i18n.getText("worklog.field.description");
        }

        return null;
    }

    ///CLOVER:OFF
    protected I18nHelper getI18nHelper()
    {
        return i18nFactory.getInstance((ApplicationUser)null);
    }
    ///CLOVER:ON

    private class FieldLayoutCacheLoader implements CacheLoader<CacheObject<Long>, FieldLayout>
    {
        @Override
        public FieldLayout load(CacheObject<Long> from)
        {
            return loadFieldLayout(from.getValue());
        }
    }

    class SetFieldLayoutAndManager implements Function<FieldLayoutItem,FieldLayoutItem>
    {
        private final FieldLayout fieldLayout;

        SetFieldLayoutAndManager(final FieldLayout fieldLayout)
        {
            this.fieldLayout = fieldLayout;
        }

        @Override
        public FieldLayoutItem apply(final FieldLayoutItem item)
        {
            return new FieldLayoutItemImpl.Builder(item)
                    .setFieldManager(fieldManager)
                    .setFieldLayout(fieldLayout)
                    .build();
        }
    }
}
