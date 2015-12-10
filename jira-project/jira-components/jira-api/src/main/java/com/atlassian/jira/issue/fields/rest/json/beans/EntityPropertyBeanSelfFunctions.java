package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.fugue.Function2;

/**
 * Functions which calculate self address of properties for various entities.
 * @since v6.2
 */
@ExperimentalApi
public class EntityPropertyBeanSelfFunctions
{
    public static class IssuePropertySelfFunction implements Function2<Long,String,String>
    {
        @Override
        public String apply(final Long entityId, final String encodedPropertyKey)
        {
            return String.format("issue/%d/properties/%s", entityId, encodedPropertyKey);
        }
    }

    public static class ProjectPropertySelfFunction implements Function2<Long,String,String>
    {
        @Override
        public String apply(final Long entityId, final String encodedPropertyKey)
        {
            return String.format("project/%d/properties/%s", entityId, encodedPropertyKey);
        }
    }

    public static class CommentPropertySelfFunction implements Function2<Long,String,String>
    {
        @Override
        public String apply(final Long entityId, final String encodedPropertyKey)
        {
            return String.format("comment/%d/properties/%s", entityId, encodedPropertyKey);
        }
    }
}
