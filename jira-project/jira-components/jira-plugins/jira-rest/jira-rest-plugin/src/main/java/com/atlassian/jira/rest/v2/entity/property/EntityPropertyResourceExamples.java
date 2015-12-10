package com.atlassian.jira.rest.v2.entity.property;

import com.atlassian.jira.util.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * @since v6.2
 */
public class EntityPropertyResourceExamples
{
    public static final com.atlassian.jira.issue.fields.rest.json.beans.EntityPropertyBean GET_PROPERTY_RESPONSE_200;
    public static final EntityPropertiesKeysBean GET_PROPERTIES_KEYS_RESPONSE_200;

    static
    {
        GET_PROPERTY_RESPONSE_200 = new com.atlassian.jira.issue.fields.rest.json.beans.EntityPropertyBean("issue.support",
                new JSONObject(ImmutableMap.of(
                        "hipchat.room.id", "support-123",
                        "support.time", "1m")).toString(),
                "http://www.example.com/jira/rest/api/2/issue/EX-2/properties/issue.support"
        );
        GET_PROPERTIES_KEYS_RESPONSE_200 = new EntityPropertiesKeysBean(Lists.newArrayList(
                new EntityPropertiesKeysBean.EntityPropertyKeyBean("issue.support", "http://www.example.com/jira/rest/api/2/issue/EX-2/properties/issue.support")
        ));
    }
}
