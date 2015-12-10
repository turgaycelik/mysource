package com.atlassian.jira.issue.fields.option;

import com.atlassian.jira.issue.fields.config.FieldConfig;

import java.util.Collection;

public interface OptionSetPersister
{
    static final String ENTITY_TABLE_NAME = "OptionConfiguration";

    static final String ENTITY_FIELD_CONFIG = "fieldconfig";
    static final String ENTITY_FIELD = "fieldid";
    static final String ENTITY_OPTION_ID = "optionid";

    static final String ENTITY_SEQUENCE = "sequence";
    static final String DB_ASC_SUFFIX = " ASC";

    OptionSet create(FieldConfig config, Collection optionIds);

    OptionSet update(FieldConfig config, Collection optionIds);

    OptionSet getOptionSetByConfig(FieldConfig config);
}
