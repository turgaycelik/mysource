package com.atlassian.jira.bc.customfield;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * Result of validating the custom field definition {@link com.atlassian.jira.bc.customfield.CustomFieldDefinition}.
 * @since v6.0
 */
@PublicApi
public class CreateValidationResult
{
    private User user;
    private String name;
    private String description;
    private CustomFieldType customFieldType;
    private CustomFieldSearcher customFieldSearcher;
    private List<JiraContextNode> contextNodes;
    private List<GenericValue> issueTypes;

    public User getUser()
    {
        return user;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public CustomFieldType getCustomFieldType()
    {
        return customFieldType;
    }

    public CustomFieldSearcher getCustomFieldSearcher()
    {
        return customFieldSearcher;
    }

    public List<JiraContextNode> getContextNodes()
    {
        return contextNodes;
    }

    public List<GenericValue> getIssueTypes()
    {
        return issueTypes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private CreateValidationResult instance;

        public Builder()
        {
            instance = new CreateValidationResult();
        }

        public Builder user(final User user) {
        	instance.user = user;
        	return this;
        }

        public Builder name(final String name) {
        	instance.name = name;
        	return this;
        }

        public Builder description(final String description) {
        	instance.description = description;
        	return this;
        }

        public Builder customFieldType(final CustomFieldType customFieldType) {
        	instance.customFieldType = customFieldType;
        	return this;
        }

        public Builder customFieldSearcher(final CustomFieldSearcher customFieldSearcher) {
        	instance.customFieldSearcher = customFieldSearcher;
        	return this;
        }

        public Builder contextNodes(final List<JiraContextNode> contextNodes) {
        	instance.contextNodes = contextNodes;
        	return this;
        }

        public Builder issueTypes(final List<GenericValue> issueTypes) {
        	instance.issueTypes = issueTypes;
        	return this;
        }

        public CreateValidationResult build() {
            return instance;
        }
    }

}

