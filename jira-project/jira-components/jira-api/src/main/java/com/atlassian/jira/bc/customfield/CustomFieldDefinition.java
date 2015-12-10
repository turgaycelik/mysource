package com.atlassian.jira.bc.customfield;

import com.atlassian.annotations.PublicApi;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The CustomFIeldDefinition class is a builder that allows you to build the definition of custom field. This definition
 * should be validated by {@link CustomFieldService#validateCreate(com.atlassian.crowd.embedded.api.User,
 * CustomFieldDefinition)} The outcome of validation is {@link CreateValidationResult} which is a validated definition
 * of custom field which can now be created using {@link CustomFieldService#validateCreate(com.atlassian.crowd.embedded.api.User,
 * CustomFieldDefinition)}
 * <p/>
 * Builder params: name   name of the custom field description - description of the custom field cfType - key of the
 * custom field type searcherKey - key to identify a searcher for the custom field projectIds - list of project id's in
 * which custom field should be available issueTypeIds - list of issue types id's in which custom field should be
 * available isGlobal - flag which overrides project id's. If set to true custom field is created in global context
 * allIssueTypes - flag which overrides issue types id's. If set to true custom filed will be available for all issue
 * types
 *
 * @since v6.0
 */
@PublicApi
public class CustomFieldDefinition
{
    private final String name;
    private final String description;
    private final String cfType;
    private final String searcherKey;
    private final List<Long> projectIds;
    private final List<String> issueTypeIds;
    private final boolean isGlobal;
    private final boolean allIssueTypes;
    private final boolean useDefaultSearcher;

    /**
     * This method is here is for API compatibility with 6.0 but is completely useless as this object is immutable.
     *
     * @deprecated Use a {@link Builder} from {@link #builder()} to create the definition. Since 6.1.
     */
    @Deprecated
    public CustomFieldDefinition()
    {
        name = null;
        description = null;
        cfType = null;
        searcherKey = null;
        projectIds = Collections.emptyList();
        issueTypeIds = Collections.emptyList();
        isGlobal = false;
        allIssueTypes = false;
        useDefaultSearcher = false;
    }

    private CustomFieldDefinition(Builder builder)
    {
        this.name = builder.name;
        this.description = builder.description;
        this.cfType = builder.cfType;
        this.searcherKey = builder.searcherKey;
        this.projectIds = copy(builder.projectIds);
        this.issueTypeIds = copy(builder.issueTypeIds);
        this.isGlobal = builder.isGlobal;
        this.allIssueTypes = builder.allIssueTypes;
        this.useDefaultSearcher = builder.defaultSearcher;
    }

    private static <T> List<T> copy(final Iterable<? extends T> elements)
    {
        return Collections.unmodifiableList(Lists.newArrayList(elements));
    }

    public String getCfType()
    {
        return cfType;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getSearcherKey()
    {
        return searcherKey;
    }

    public boolean isUseDefaultSearcher()
    {
        return useDefaultSearcher;
    }

    public List<Long> getProjectIds()
    {
        return projectIds;
    }

    public List<String> getIssueTypeIds()
    {
        return issueTypeIds;
    }

    public boolean isGlobal()
    {
        return isGlobal;
    }

    public boolean isAllIssueTypes()
    {
        return allIssueTypes;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private String name;
        private String description;
        private String cfType;
        private String searcherKey;
        private Set<Long> projectIds = Sets.newLinkedHashSet();
        private Set<String> issueTypeIds = Sets.newLinkedHashSet();
        private boolean isGlobal;
        private boolean allIssueTypes;
        private boolean defaultSearcher;

        public Builder name(final String name)
        {
            this.name = name;
            return this;
        }

        public Builder description(final String description)
        {
            this.description = description;
            return this;
        }

        public Builder cfType(final String cfType)
        {
            this.cfType = cfType;
            return this;
        }

        public Builder searcherKey(final String searcherKey)
        {
            this.defaultSearcher = false;
            this.searcherKey = searcherKey;
            return this;
        }

        public Builder defaultSearcher()
        {
            this.defaultSearcher = true;
            return this;
        }

        public Builder addProjectIds(final Long... projects)
        {
            projectIds.addAll(Arrays.asList(projects));
            return this;
        }

        public Builder addProjectId(final Long project)
        {
            projectIds.add(project);
            return this;
        }

        public Builder isGlobal(final boolean isGlobal)
        {
            this.isGlobal = isGlobal;
            return this;
        }

        public Builder isAllIssueTypes(final boolean allIssueTypes)
        {
            this.allIssueTypes = allIssueTypes;
            return this;
        }

        public Builder addIssueTypeId(final String issueTypeId)
        {
            this.issueTypeIds.add(issueTypeId);
            return this;
        }

        public Builder addIssueTypeIds(final String... issueTypeIds)
        {
            this.issueTypeIds.addAll(Arrays.asList(issueTypeIds));
            return this;
        }

        public CustomFieldDefinition build()
        {
            return new CustomFieldDefinition(this);
        }
    }
}
