package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.impl.MultiSelectCFType;
import com.atlassian.jira.issue.customfields.impl.SelectCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.collect.MapBuilder;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This task migrates Custom field values for Select and MultiSelect types to store the id of the option rather than the
 * value.
 *
 * @since v4.4
 */
public class UpgradeTask_Build633 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build633.class);

    public static final String CF_VALUE_ENTITY = "CustomFieldValue";
    public static final String CF_VALUE_OPTION = "CustomFieldOption";

    private final CustomFieldManager customFieldManager;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;
    private final IssueManager issueManager;
    private final OfBizDelegator ofBizDelegator;
    private final GenericConfigManager genericConfigManager;

    private static List<String> atlassianList;
    /** The blacklist contains dodgy subclasses from 3rd parties that we should not convert. */
    private static List<String> blacklist;

    static {
        atlassianList = new ArrayList<String>();
        atlassianList.add("com.atlassian.jira.issue.customfields.impl.SelectCFType");
        atlassianList.add("com.atlassian.jira.issue.customfields.impl.MultiSelectCFType");

        blacklist = new ArrayList<String>();
        // Tempo's billing key.
        blacklist.add("is.origo.jira.plugin.customFields.BillingKeyCustomField");
    }

    public UpgradeTask_Build633(CustomFieldManager customFieldManager, FieldConfigSchemeManager fieldConfigSchemeManager, OfBizDelegator ofBizDelegator, IssueManager issueManager, GenericConfigManager genericConfigManager)
    {
        super(false);
        this.customFieldManager = customFieldManager;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.ofBizDelegator = ofBizDelegator;
        this.issueManager = issueManager;
        this.genericConfigManager = genericConfigManager;
    }

    public String getBuildNumber()
    {
        return "633";
    }

    public String getShortDescription()
    {
        return "Converting Custom field values for Select and MultiSelect types to store the id of the option rather than the value.";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        List<CustomField> customFieldList = getCustomFields();
        int i = 0;
        for (CustomField customField : customFieldList)
        {
            i++;
            log.info("Updating custom field '" + customField.getName() + "', " + i + " of " + customFieldList.size());
            processCustomFieldOptions(customField);
            processCustomFieldDefaults(customField);
        }
    }

    private void processCustomFieldDefaults(CustomField customField)
    {
        Set<Long> configIds = new HashSet<Long>();
        List<FieldConfigScheme> fieldConfigSchemes = fieldConfigSchemeManager.getConfigSchemesForField(customField);
        for (FieldConfigScheme fieldConfigScheme : fieldConfigSchemes)
        {
            Map<String,FieldConfig> configs = fieldConfigScheme.getConfigs();
            for (FieldConfig fieldConfig : configs.values())
            {
                configIds.add(fieldConfig.getId());
            }
        }

        for (Long configId : configIds)
        {
            Object defaultObject = genericConfigManager.retrieve(CustomFieldType.DEFAULT_VALUE_TYPE, configId.toString());
            if (defaultObject != null)
            {
                Set<Long> defaultIds = new HashSet<Long>();
                if (defaultObject instanceof Collection)
                {
                    Collection defaults = (Collection) defaultObject;
                    for (Object aDefault : defaults)
                    {
                        if (aDefault instanceof String)
                        {
                            final List<GenericValue> optionGvs = ofBizDelegator.findByAnd(CF_VALUE_OPTION,
                                    MapBuilder.build("customfield", customField.getIdAsLong(), "customfieldconfig", configId, "value", aDefault));
                            if (optionGvs.size() > 0)
                            {
                                defaultIds.add(optionGvs.get(0).getLong("id"));
                            }
                        }
                    }
                    genericConfigManager.update(CustomFieldType.DEFAULT_VALUE_TYPE, configId.toString(), defaultIds);
                }
                else if (defaultObject instanceof String)
                {
                    String aDefault = (String) defaultObject;
                    if (aDefault != null)
                    {
                        final List<GenericValue> optionGvs = ofBizDelegator.findByAnd(CF_VALUE_OPTION,
                                MapBuilder.build("customfield", customField.getIdAsLong(), "customfieldconfig", configId, "value", aDefault));
                        if (optionGvs.size() > 0)
                        {
                            genericConfigManager.update(CustomFieldType.DEFAULT_VALUE_TYPE, configId.toString(), optionGvs.get(0).getLong("id"));
                        }
                        else
                        {
                            genericConfigManager.remove(CustomFieldType.DEFAULT_VALUE_TYPE, configId.toString());
                        }
                    }
                }
                else
                {
                    genericConfigManager.remove(CustomFieldType.DEFAULT_VALUE_TYPE, configId.toString());
                }
            }
        }

    }

    private void processCustomFieldOptions(CustomField customField) throws GenericEntityException
    {
        // We need to read all the Custom field options and for eac one, get the issue, so we know the field config
        // Then update the custom field data with the id in place of the option
        

        //Copy the data across for all custom fields!
        int logCount = 0;
        OfBizListIterator iterator = null;
        try
        {
            iterator = ofBizDelegator.findListIteratorByCondition(CF_VALUE_ENTITY, new EntityExpr("customfield", EntityOperator.EQUALS,
                    customField.getIdAsLong()), null, null, null, null);

            for (GenericValue gv = iterator.next(); gv != null; gv = iterator.next())
            {
                // If the number value is already set we have been here before.  Just skip that.
                if (gv.get("numbervalue") == null)
                {
                    final Long issueId = gv.getLong("issue");
                    final Long customFieldId = gv.getLong("customfield");
                    FieldConfig fieldConfig = null;
                    Issue issue = issueManager.getIssueObject(issueId);
                    if (issue != null)
                    {
                        fieldConfig = fieldConfigSchemeManager.getRelevantConfig(issue, customField);
                    }
                    else
                    {
                        // This shouldn't happen but JAC has lots of broken data.
                        List<FieldConfigScheme> allConfigSchemes = fieldConfigSchemeManager.getConfigSchemesForField(customField);
                        if (allConfigSchemes != null && allConfigSchemes.size() > 0)
                        {
                            Map<String, FieldConfig> allConfigs = allConfigSchemes.get(0).getConfigs();
                            if (allConfigs != null && allConfigs.size() > 0)
                            {
                                fieldConfig = allConfigs.values().iterator().next();
                            }
                        }
                    }

                    if (fieldConfig != null)
                    {
                        final List<GenericValue> optionGvs = ofBizDelegator.findByAnd(CF_VALUE_OPTION,
                               MapBuilder.build("customfield", customFieldId, "customfieldconfig", fieldConfig.getId(), "value", gv.getString("stringvalue")));

                        if (optionGvs.size() == 0)
                        {
                            // For Atlassian's own types just blow the bad data away
                            if (atlassianList.contains(customField.getCustomFieldType().getClass().getName()))
                            {
                                gv.setString("stringvalue", null);
                                gv.set("numbervalue", Long.valueOf(-1l));
                                maybeLog("cleared", customField.getName(), gv.getString("stringvalue"), logCount);
                                logCount++;
                            }
                            // For 3rd part fields leave the corrupt data.
                            else
                            {
                                gv.set("numbervalue", Long.valueOf(-1l));
                                maybeLog("not converted", customField.getName(), gv.getString("stringvalue"), logCount);
                                logCount++;
                            }
                        }
                        else
                        {
                            Long optionId = optionGvs.get(0).getLong("id");
                            gv.setString("stringvalue", optionId.toString());
                            gv.set("numbervalue", optionId);
                        }
                        gv.store();
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

    private void maybeLog(String s, String name, String stringvalue, int logCount)
    {
        if (logCount <= 10)
        {
            log.info("No option found for Custom field '" + name + "' Field value '" + stringvalue + "'. Value " + s + ".");
        }
        else if (logCount == 11)
        {
            log.info("No option found for Custom field '" + name + "' ... more than 10 times.");
        }
    }

    /**
     * Create a list of all the custom fields we need to migrate.
     *
     * @return List of Custom fields to convert.
     */
    private List<CustomField> getCustomFields()
    {
        /*
           * We use the manager here, even though that is basically bad practise in an Upgrade Task,
           * because the code to find the CustomFieldType is really deep and ugly and it would seem
           * wrong to copy and paste reams of it into this class.
           */

        List<CustomField> selectCustomFields = new ArrayList<CustomField>();
        List<CustomField> customFields = customFieldManager.getCustomFieldObjects();
        for (CustomField customField : customFields)
        {
            CustomFieldType type = customField.getCustomFieldType();
            if (type instanceof SelectCFType || type instanceof MultiSelectCFType)
            {
                // Ignore field types from the blacklist
                if (!blacklist.contains(type.getClass().getName()))
                {
                    selectCustomFields.add(customField);
                }
            }
        }
        return selectCustomFields;
    }

}