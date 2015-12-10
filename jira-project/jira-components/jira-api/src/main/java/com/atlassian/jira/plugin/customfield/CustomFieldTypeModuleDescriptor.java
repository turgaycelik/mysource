package com.atlassian.jira.plugin.customfield;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.config.managedconfiguration.ConfigurationItemAccessLevel;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.CustomFieldTypeCategory;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.web.bean.BulkMoveHelper;
import webwork.action.Action;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

@PublicApi
public interface CustomFieldTypeModuleDescriptor extends JiraResourcedModuleDescriptor<CustomFieldType>
{
    // ------------------------------------------------------------------------------------------------------- Constants
    public static final String TEMPLATE_NAME_VIEW = "view";
    public static final String TEMPLATE_NAME_EDIT = "edit";
    public static final String TEMPLATE_NAME_EDIT_DEFAULT = "edit-default";
    public static final String TEMPLATE_NAME_BULK_MOVE = "bulk-move";
    public static final String TEMPLATE_NAME_XML = "xml";
    public static final String TEMPLATE_NAME_COLUMN = "column-view";

    public static final String VELOCITY_VALUE_PARAM = "value";
    public static final String VELCITY_ACTION_PARAM = "action";

    /**
     * The name of the attribute to specify for a custom field type when you want new instances of this custom field type
     * to only be created by certain users. The values of this attribute are the string versions of {@link ConfigurationItemAccessLevel}.
     */
    public static final String MANAGED_FLAG = "managed-access-level";
    public static final String MANAGED_DESC = "managed-description-key";

    // -------------------------------------------------------------------------------------------------- Public Methods
    public boolean isViewTemplateExists();

    public boolean isColumnViewTemplateExists();

    public boolean isEditTemplateExists();

    public boolean isXMLTemplateExists();

    /**
     * Is this custom field type a "managed" type? The provider of this type may want to control the circumstances in
     * which it is used.
     * @return boolean
     */
    public boolean isTypeManaged();

    /**
     * @return the level which the user has to be to be able to create new instances of this custom field type; may return
     * null if level was not specified or was specified incorrectly
     */
    @Nullable
    public ConfigurationItemAccessLevel getManagedAccessLevel();

    /**
     * @return the I18n key of the description explaining the reason this type is managed.
     */
    public String getManagedDescriptionKey();

    // -------------------------------------------------------------------------------------------------- HTML Templates

    public String getEditHtml(FieldConfig config, Map customFieldValuesHolder, Issue issue, Action action,
                              Map displayParameters,
                              FieldLayoutItem fieldLayoutItem);

    public String getBulkMoveHtml(FieldConfig config, Map customFieldValuesHolder, Issue issue, Action action,
            Map displayParameters,
            FieldLayoutItem fieldLayoutItem, final Map<Long, BulkMoveHelper.DistinctValueResult> distinctValues, final BulkMoveHelper bulkMoveHelper);

    public String getEditDefaultHtml(FieldConfig config, Map customFieldValuesHolder, Issue issue, Action action,
                              Map displayParameters,
                              FieldLayoutItem fieldLayoutItem);

    //the value here is the customfield value
    public String getColumnViewHtml(CustomField field, Object value, Issue issue, Map displayParams, FieldLayoutItem fieldLayoutItem);

    //the value here is the customfield value
    public String getViewHtml(CustomField field, Object value, Issue issue, FieldLayoutItem fieldLayoutItem);

    public String getViewHtml(CustomField field, Object value, Issue issue, FieldLayoutItem fieldLayoutItem, Map displayParameters);

    public String getViewXML(CustomField field, Issue issue, FieldLayoutItem fieldLayoutItem, boolean raw);

    public String getDefaultViewHtml(FieldConfig fieldConfig, FieldLayoutItem fieldLayoutItem);

    /**
     * Returns the list of Searchers that this CustomFieldType declares as usable to search itself.
     * <p>
     * Note that it is also possible for a CustomFieldSearcher to declare a CustomFieldType that it is able to search on.
     *
     * @return the list of Searchers that this CustomFieldType declares as usable to search itself.
     */
    public Set<String> getValidSearcherKeys();

    /**
     * The set of categories that this CustomFieldType has declared as being a member of.
     */
    public Set<CustomFieldTypeCategory> getCategories();
}
