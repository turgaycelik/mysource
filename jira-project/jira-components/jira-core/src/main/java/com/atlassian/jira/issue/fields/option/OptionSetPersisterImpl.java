package com.atlassian.jira.issue.fields.option;

import java.util.Collection;
import java.util.List;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.MapBuilder;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

public class OptionSetPersisterImpl implements OptionSetPersister
{
    private static final Logger log = Logger.getLogger(OptionSetPersisterImpl.class);
    // --------------------------------------------------------------------------------------------------- Entity Fields

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final OfBizDelegator delegator;
    private final ConstantsManager constantsManager;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public OptionSetPersisterImpl(OfBizDelegator delegator, ConstantsManager constantsManager)
    {
        this.delegator = delegator;
        this.constantsManager = constantsManager;
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods
    public OptionSet create(FieldConfig config, Collection optionIds)
    {
        return update(config, optionIds);
    }

    public OptionSet update(FieldConfig config, Collection optionIds)
    {
        final Long configId = config.getId();
        final String fieldId = config.getFieldId();

        // Remove all options related to the current config
        delegator.removeByAnd(ENTITY_TABLE_NAME, EasyMap.build(ENTITY_FIELD_CONFIG, configId,
                                                               ENTITY_FIELD, fieldId));

        // Create the config sets
        if (optionIds != null)
        {
            int i = 0;
            for (final Object optionId : optionIds)
            {
                String option = (String) optionId;
                delegator.createValue(ENTITY_TABLE_NAME, MapBuilder.<String, Object>build(ENTITY_FIELD_CONFIG, configId,
                        ENTITY_FIELD, fieldId,
                        ENTITY_OPTION_ID, option,
                        ENTITY_SEQUENCE, new Long(i)));
                i++;
            }
        }
        else
        {
            // Deleted!
            log.info("All options removed for config id" + config.getId());
        }

        return getOptionSetByConfig(config);
    }

    public OptionSet getOptionSetByConfig(FieldConfig config)
    {
        List<GenericValue> gvs = delegator.findByAnd(ENTITY_TABLE_NAME,
                                       EasyMap.build(ENTITY_FIELD_CONFIG, config.getId(),
                                                     ENTITY_FIELD, config.getFieldId()),
                                       EasyList.build(ENTITY_SEQUENCE + DB_ASC_SUFFIX));

        OptionSet optionSet = new LazyLoadedOptionSet(constantsManager);
        for (final GenericValue optionConfigGv : gvs)
        {
            optionSet.addOption(optionConfigGv.getString(ENTITY_FIELD), optionConfigGv.getString(ENTITY_OPTION_ID));
        }

        return optionSet;
    }

    // -------------------------------------------------------------------------------------------------- Private helper
}
