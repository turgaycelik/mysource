package com.atlassian.jira.upgrade.tasks;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.impl.MultiUserCFType;
import com.atlassian.jira.issue.customfields.impl.UserCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This task migrates Custom field values for User and MultiUser types to store the key of the user rather than the
 * username.
 *
 * @since v6.0
 */
public class UpgradeTask_Build6045 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build6045.class);

    private final CustomFieldManager customFieldManager;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;
    private final GenericConfigManager genericConfigManager;

    public UpgradeTask_Build6045(CustomFieldManager customFieldManager, FieldConfigSchemeManager fieldConfigSchemeManager,
            GenericConfigManager genericConfigManager)
    {
        super(false);
        this.customFieldManager = customFieldManager;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.genericConfigManager = genericConfigManager;
    }

    public String getBuildNumber()
    {
        return "6045";
    }

    public String getShortDescription()
    {
        return "Converting default Custom field values for User and MultiUser types to store the key of the user rather than the username.";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        List<CustomField> customFieldList = getCustomFields();
        int i = 0;
        for (CustomField customField : customFieldList)
        {
            i++;
            log.info("Updating custom field '" + customField.getName() + "', " + i + " of " + customFieldList.size());
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
                if (defaultObject instanceof Collection)
                {
                    Set<String> defaultIds = Sets.newHashSet();
                    Collection defaultsFromDb = (Collection) defaultObject;
                    for (Object aDefault : defaultsFromDb)
                    {
                        if (aDefault instanceof String)
                        {
                            defaultIds.add(IdentifierUtils.toLowerCase((String) aDefault));
                        }
                    }
                    genericConfigManager.update(CustomFieldType.DEFAULT_VALUE_TYPE, configId.toString(), defaultIds);
                }
                else if (defaultObject instanceof String)
                {
                    String aDefault = (String) defaultObject;
                    if (aDefault != null)
                    {
                        genericConfigManager.update(CustomFieldType.DEFAULT_VALUE_TYPE, configId.toString(),
                                IdentifierUtils.toLowerCase(aDefault));
                    }
                }
                else
                {
                    genericConfigManager.remove(CustomFieldType.DEFAULT_VALUE_TYPE, configId.toString());
                }
            }
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

        List<CustomField> userCustomFields = Lists.newArrayList();
        List<CustomField> allCustomFields = customFieldManager.getCustomFieldObjects();
        for (CustomField customField : allCustomFields)
        {
            CustomFieldType type = customField.getCustomFieldType();
            if (type instanceof UserCFType || type instanceof MultiUserCFType)
            {
                userCustomFields.add(customField);
            }
        }
        return userCustomFields;
    }

}