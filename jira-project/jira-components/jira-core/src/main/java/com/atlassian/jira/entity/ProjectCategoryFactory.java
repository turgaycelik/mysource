package com.atlassian.jira.entity;

import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectCategoryImpl;
import org.ofbiz.core.entity.GenericValue;

/**
 * ProjectCategory EntityFactory
 *
 * @since v4.4
 */
public class ProjectCategoryFactory extends AbstractEntityFactory<ProjectCategory>
{
    @Override
    public String getEntityName()
    {
        return "ProjectCategory";
    }

    @Override
    public ProjectCategory build(GenericValue genericValue)
    {
        if (genericValue == null)
        {
            return null;
        }
        Builder builder = new Builder();
        builder.id(genericValue.getLong("id"));
        builder.name(genericValue.getString("name"));
        builder.description(genericValue.getString("description"));
        return builder.build();
    }

    @Override
    public FieldMap fieldMapFrom(ProjectCategory value)
    {
        return new FieldMap("id", value.getId())
                .add("name", value.getName())
                .add("description", value.getDescription());
    }

    public static class Builder
    {
        private Long id;
        private String name;
        private String description;

        public Builder id(Long id)
        {
            this.id = id;
            return this;
        }

        public Builder name(String name)
        {
            this.name = name;
            return this;
        }

        public Builder description(String description)
        {
            this.description = description;
            return this;
        }

        public ProjectCategory build()
        {
            return new ProjectCategoryImpl(id, name, description);
        }
    }
}
