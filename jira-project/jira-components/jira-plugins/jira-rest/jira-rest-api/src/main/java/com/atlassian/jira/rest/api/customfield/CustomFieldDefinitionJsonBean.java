package com.atlassian.jira.rest.api.customfield;

import org.codehaus.jackson.annotate.JsonProperty;

import java.net.URI;

/**
 *
 * @since v6.0
 */
public class CustomFieldDefinitionJsonBean
{
    @JsonProperty
    private String name;

    @JsonProperty
    private String description;

    @JsonProperty
    private String type;

    @JsonProperty
    private String searcherKey;

    @JsonProperty
    private URI self;

    public CustomFieldDefinitionJsonBean() {}

    public CustomFieldDefinitionJsonBean(final String name, final String description, final String type, final String searcherKey, final URI self)
    {
        this.name = name;
        this.description = description;
        this.type = type;
        this.searcherKey = searcherKey;
        this.self = self;
    }

    public String name()
    {
        return name;
    }

    public void name(final String name)
    {
        this.name = name;
    }

    public String description()
    {
        return description;
    }

    public void description(final String description)
    {
        this.description = description;
    }

    public String type()
    {
        return type;
    }

    public void type(final String type)
    {
        this.type = type;
    }

    public String searcherKey()
    {
        return searcherKey;
    }

    public void searcherKey(final String searcherKey)
    {
        this.searcherKey = searcherKey;
    }

    public URI self()
    {
        return self;
    }

    public void self(final URI self)
    {
        this.self = self;
    }

    public static final CustomFieldDefinitionJsonBean DOC_EXAMPLE = new CustomFieldDefinitionJsonBean();

    static {
        DOC_EXAMPLE.name = "New custom field";
        DOC_EXAMPLE.description = "Custom field for picking groups";
        DOC_EXAMPLE.type = "com.atlassian.jira.plugin.system.customfieldtypes:grouppicker";
        DOC_EXAMPLE.searcherKey = "com.atlassian.jira.plugin.system.customfieldtypes:grouppickersearcher";
    }
}
