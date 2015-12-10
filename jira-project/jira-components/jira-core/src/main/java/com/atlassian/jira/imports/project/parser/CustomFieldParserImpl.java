package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalCustomField;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilderImpl;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * @since v3.13
 */
public class CustomFieldParserImpl implements CustomFieldParser
{
    private static final String CUSTOM_FIELD_PREFIX = "customfield_";

    public ExternalCustomField parseCustomField(final Map attributes) throws ParseException
    {
        if (attributes == null)
        {
            throw new IllegalArgumentException("The 'attributes' parameter cannot be null.");
        }
        // <CustomField id="10001" customfieldtypekey="com.atlassian.jira.plugin.system.customfieldtypes:textarea"
        //              customfieldsearcherkey="com.atlassian.jira.plugin.system.customfieldtypes:textsearcher" name="text cf"/>

        final String id = (String) attributes.get("id");
        final String name = (String) attributes.get("name");
        final String customfieldtypekey = (String) attributes.get("customfieldtypekey");

        // Validate the data
        if (StringUtils.isEmpty(id))
        {
            throw new ParseException("No 'id' field for CustomField.");
        }
        if (StringUtils.isEmpty(name))
        {
            throw new ParseException("No 'name' field for CustomField " + id + ".");
        }
        if (StringUtils.isEmpty(customfieldtypekey))
        {
            throw new ParseException("No 'customfieldtypekey' field for CustomField " + id + ".");
        }

        return new ExternalCustomField(id, name, customfieldtypekey);
    }

    public BackupOverviewBuilderImpl.ConfigurationContext parseCustomFieldConfiguration(final Map attributes) throws ParseException
    {
        if (attributes == null)
        {
            throw new IllegalArgumentException("The 'attributes' parameter cannot be null.");
        }

        // <ConfigurationContext id="10011" project="10001" key="customfield_10001" fieldconfigscheme="10011"/>
        final String id = (String) attributes.get("id");
        final String projectId = (String) attributes.get("project");
        String customFieldId = (String) attributes.get("key");
        final String fieldconfigscheme = (String) attributes.get("fieldconfigscheme");

        // Validate the data
        if (StringUtils.isEmpty(id))
        {
            throw new ParseException("No 'id' field for ConfigurationContext.");
        }
        if (StringUtils.isEmpty(customFieldId))
        {
            throw new ParseException("No 'key' field for ConfigurationContext " + id + ".");
        }
        // This table is used to store things other than custom field ConfigurationContexts so we only really want
        // to handle those that are custom field related
        if (!customFieldId.startsWith(CUSTOM_FIELD_PREFIX))
        {
            return null;
        }
        customFieldId = customFieldId.substring(CUSTOM_FIELD_PREFIX.length());
        if (StringUtils.isEmpty(fieldconfigscheme))
        {
            throw new ParseException("No 'fieldconfigscheme' field for ConfigurationContext " + id + ".");
        }
        // Note that projectId is allowed to be null - this means that this is the "global" config for any projects that don't have an explicit config.

        return new BackupOverviewBuilderImpl.ConfigurationContext(fieldconfigscheme, customFieldId, projectId);
    }

    public BackupOverviewBuilderImpl.FieldConfigSchemeIssueType parseFieldConfigSchemeIssueType(final Map attributes) throws ParseException
    {
        if (attributes == null)
        {
            throw new IllegalArgumentException("The 'attributes' parameter cannot be null.");
        }

        // <FieldConfigSchemeIssueType id="10012" issuetype="1" fieldconfigscheme="10011" fieldconfiguration="10011"/>
        final String id = (String) attributes.get("id");
        if (StringUtils.isEmpty(id))
        {
            throw new ParseException("No 'id' field for FieldConfigSchemeIssueType.");
        }
        final String fieldconfigscheme = (String) attributes.get("fieldconfigscheme");
        if (StringUtils.isEmpty(fieldconfigscheme))
        {
            throw new ParseException("No 'fieldconfigscheme' field for FieldConfigSchemeIssueType.");
        }
        // Issue type can be null and this represents a configuration for all issue types
        final String issuetype = (String) attributes.get("issuetype");

        return new BackupOverviewBuilderImpl.FieldConfigSchemeIssueType(fieldconfigscheme, issuetype);
    }
}
