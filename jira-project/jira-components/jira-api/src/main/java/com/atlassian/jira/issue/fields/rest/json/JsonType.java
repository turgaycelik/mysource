package com.atlassian.jira.issue.fields.rest.json;

import com.atlassian.annotations.PublicApi;

/**
 * Describes the format of the data that is returned in the JSON representation of a field.
 * This encapsualtes a subset of <a href="http://tools.ietf.org/html/draft-zyp-json-schema-03">JSON Schema</a>.
 *
 * @since v5.0
 */
@PublicApi
public class JsonType
{
    public static final String STRING_TYPE = "string";
    public static final String NUMBER_TYPE = "number";
    public static final String BOOLEAN_TYPE = "boolean";
    public static final String DATE_TYPE = "date";
    public static final String DATETIME_TYPE = "datetime";

    public static final String ARRAY_TYPE = "array";

    public static final String OPTION_TYPE = "option";
    public static final String ANY_TYPE = "any";

    public static final String COMMENT_TYPE = "comment";
    public static final String RESOLUTION_TYPE = "resolution";
    public static final String USER_TYPE = "user";
    public static final String GROUP_TYPE = "group";
    public static final String VERSION_TYPE = "version";
    public static final String VOTES_TYPE = "votes";
    public static final String WATCHES_TYPE = "watches";
    public static final String COMPONENT_TYPE = "component";
    public static final String ATTACHMENT_TYPE = "attachment";
    public static final String ISSUELINKS_TYPE = "issuelinks";
    public static final String PRIORITY_TYPE = "priority";
    public static final String PROGRESS_TYPE = "progress";
    public static final String STATUS_TYPE = "status";
    public static final String PROJECT_TYPE = "project";
    public static final String ISSUETYPE_TYPE = "issuetype";
    public static final String SECURITY_LEVEL_TYPE = "securitylevel";
    public static final String WORKLOG_TYPE = "worklog";
    public static final String TIME_TRACKING_TYPE = "timetracking";

    private final String type;
    private final String items;
    private final String system;
    private final String custom;
    private final Long customId;

    public JsonType(String type, String items, String system, String custom, Long customId)
    {
        this.type = type;
        this.items = items;
        this.system = system;
        this.custom = custom;
        this.customId = customId;
    }

    public String getType()
    {
        return type;
    }

    public String getItems()
    {
        return items;
    }

    public String getSystem()
    {
        return system;
    }

    public String getCustom()
    {
        return custom;
    }

    public Long getCustomId()
    {
        return customId;
    }
}
