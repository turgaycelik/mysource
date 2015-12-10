package com.atlassian.jira.issue.fields.layout.field;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.ofbiz.core.entity.GenericValue;

/**
 * Simple implementation of {@link com.atlassian.jira.issue.fields.layout.field.MockFieldLayoutManager}.
 *
 * @since v4.1
 */
public class MockFieldLayoutManager implements FieldLayoutManager
{
    private Map<Context, FieldLayout> contextToLayout = new HashMap<Context, FieldLayout>();
    private Map<Long, FieldLayout> idsToLayout = new HashMap<Long, FieldLayout>();
    private long currentId = 10101;
    private List<FieldLayoutScheme> fieldLayoutSchemes;
    private Map<Project, FieldConfigurationScheme> fieldConfigurationSchemes = new HashMap<Project, FieldConfigurationScheme>();
    private Map<Long, FieldConfigurationScheme> fieldConfigurationSchemeIds = new HashMap<Long, FieldConfigurationScheme>();

    public MockFieldLayout addLayoutItem(Issue issue)
    {
        MockFieldLayout item = new MockFieldLayout();
        item.setId(currentId++);

        contextToLayout.put(new Context(issue), item);
        idsToLayout.put(item.getId(), item);

        return item;
    }

    public FieldLayoutScheme createFieldLayoutScheme(final FieldLayoutScheme fieldLayoutScheme)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldLayoutScheme createFieldLayoutScheme(@Nonnull final String name, @Nullable final String description)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public FieldLayoutScheme copyFieldLayoutScheme(@Nonnull final FieldLayoutScheme scheme, @Nonnull final String name, @Nullable final String description)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public FieldConfigurationScheme getFieldConfigurationScheme(final Long schemeId)
    {
        return fieldConfigurationSchemeIds.get(schemeId);
    }

    public FieldLayoutScheme getMutableFieldLayoutScheme(final Long schemeId)
    {
        throw new UnsupportedOperationException();
    }

    public boolean fieldConfigurationSchemeExists(final String schemeName)
    {
        return false;
    }

    public List<EditableFieldLayout> getEditableFieldLayouts()
    {
        throw new UnsupportedOperationException();
    }

    public List<FieldLayoutScheme> getFieldLayoutSchemes()
    {
        return fieldLayoutSchemes;
    }

    public MockFieldLayoutManager setFieldLayoutSchemes(final List<FieldLayoutScheme> fieldLayoutSchemes)
    {
        this.fieldLayoutSchemes = fieldLayoutSchemes;
        return this;
    }

    public void updateFieldLayoutScheme(final FieldLayoutScheme fieldLayoutScheme)
    {
        throw new UnsupportedOperationException();
    }

    public void deleteFieldLayoutScheme(final FieldLayoutScheme fieldLayoutScheme)
    {
        throw new UnsupportedOperationException();
    }

    public FieldConfigurationScheme getFieldConfigurationScheme(final Project project)
    {
        return fieldConfigurationSchemes.get(project);
    }

    public MockFieldLayoutManager setFieldConfigurationScheme(final Project project, final FieldConfigurationScheme fieldConfigurationScheme)
    {
        fieldConfigurationSchemes.put(project, fieldConfigurationScheme);
        return this;
    }

    public MockFieldLayoutManager setFieldConfigurationScheme(final Long id, final FieldConfigurationScheme fieldConfigurationScheme)
    {
        fieldConfigurationSchemeIds.put(id, fieldConfigurationScheme);
        return this;
    }


    public FieldConfigurationScheme getFieldConfigurationScheme(final GenericValue project)
    {
        throw new UnsupportedOperationException();
    }

    public Set<FieldLayout> getUniqueFieldLayouts(final Project project)
    {
        Set<FieldLayout> uniqLayouts = new HashSet<FieldLayout>();
        for (Map.Entry<Context, FieldLayout> layoutEntry : contextToLayout.entrySet())
        {
            if (project.getId().equals(layoutEntry.getKey().getProject()))
            {
                uniqLayouts.add(layoutEntry.getValue());
            }
        }

        return uniqLayouts;
    }

    public void addSchemeAssociation(final GenericValue project, final Long fieldLayoutSchemeId)
    {
        throw new UnsupportedOperationException();
    }

    public void removeSchemeAssociation(final GenericValue project, final Long fieldLayoutSchemeId)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addSchemeAssociation(Project project, Long fieldLayoutSchemeId)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void removeSchemeAssociation(Project project, Long fieldLayoutSchemeId)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public FieldLayout getFieldLayout()
    {
        return getFieldLayout((Long)null);
    }

    public FieldLayout getFieldLayout(final Long id)
    {
        return idsToLayout.get(id);
    }

    public MockFieldLayoutManager setFieldLayout(final Long id, final FieldLayout fieldLayout)
    {
        idsToLayout.put(id, fieldLayout);
        return this;
    }

    public FieldLayout getFieldLayout(final GenericValue issue)
    {
        return contextToLayout.get(new Context(issue.getLong("project"), issue.getString("type")));
    }

    public FieldLayout getFieldLayout(final Issue issue)
    {
        return contextToLayout.get(new Context(issue));
    }

    public FieldLayout getFieldLayout(final Project project, final String issueTypeId)
    {
        return contextToLayout.get(new Context(project.getId(), issueTypeId));
    }

    public MockFieldLayoutManager setFieldLayout(final Project project, final String issueTypeId,
            final FieldLayout fieldLayout)
    {
        contextToLayout.put(new Context(project.getId(), issueTypeId), fieldLayout);
        return this;
    }

    public FieldLayout getFieldLayout(final GenericValue project, final String issueTypeId)
    {
        return contextToLayout.get(new Context(project.getLong("id"), issueTypeId));
    }

    public EditableDefaultFieldLayout getEditableDefaultFieldLayout()
    {
        throw new UnsupportedOperationException();
    }

    public void storeEditableDefaultFieldLayout(final EditableDefaultFieldLayout editableDefaultFieldLayout)
    {
        throw new UnsupportedOperationException();
    }

    public void storeEditableFieldLayout(final EditableFieldLayout editableFieldLayout)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public EditableFieldLayout storeAndReturnEditableFieldLayout(EditableFieldLayout editableFieldLayout)
    {
        throw new UnsupportedOperationException();
    }

    public void restoreDefaultFieldLayout()
    {
        throw new UnsupportedOperationException();
    }

    public void restoreSchemeFieldLayout(final GenericValue scheme)
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasDefaultFieldLayout()
    {
        return false;
    }

    public Collection<GenericValue> getProjects(final FieldConfigurationScheme fieldConfigurationScheme)
    {
        throw new UnsupportedOperationException();
    }

    public Collection<GenericValue> getProjects(final FieldLayoutScheme fieldLayoutScheme)
    {
        throw new UnsupportedOperationException();
    }

    public void refresh()
    {
    }

    public EditableFieldLayout getEditableFieldLayout(final Long id)
    {
        throw new UnsupportedOperationException();
    }

    public void deleteFieldLayout(final FieldLayout fieldLayout)
    {
        deleteValue(contextToLayout, fieldLayout);
        deleteValue(idsToLayout, fieldLayout);
    }

    public Collection<FieldLayoutSchemeEntity> getFieldLayoutSchemeEntities(final FieldLayoutScheme fieldLayoutScheme)
    {
        throw new UnsupportedOperationException();
    }

    public void createFieldLayoutSchemeEntity(final FieldLayoutSchemeEntity fieldLayoutSchemeEntity)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldLayoutSchemeEntity createFieldLayoutSchemeEntity(final FieldLayoutScheme fieldLayoutScheme, final String issueTypeId, final Long fieldConfigurationId)
    {
        throw new UnsupportedOperationException("Not implemented");
    }


    public void updateFieldLayoutSchemeEntity(final FieldLayoutSchemeEntity fieldLayoutSchemeEntity)
    {
        throw new UnsupportedOperationException();
    }

    public void removeFieldLayoutSchemeEntity(final FieldLayoutSchemeEntity fieldLayoutSchemeEntity)
    {
        throw new UnsupportedOperationException();
    }

    public void removeFieldLayoutScheme(final FieldLayoutScheme fieldLayoutScheme)
    {
        throw new UnsupportedOperationException();
    }

    public Collection<FieldConfigurationScheme> getFieldConfigurationSchemes(final FieldLayout fieldLayout)
    {
        throw new UnsupportedOperationException();
    }

    public Collection<GenericValue> getRelatedProjects(final FieldLayout fieldLayout)
    {
        throw new UnsupportedOperationException();
    }

    public boolean isFieldLayoutSchemesVisiblyEquivalent(final Long fieldConfigurationSchemeId1, final Long fieldConfigurationSchemeId2)
    {
        throw new UnsupportedOperationException();
    }

    public boolean isFieldLayoutsVisiblyEquivalent(final Long fieldLayoutId1, final Long fieldLayoutId2)
    {
        return false;
    }

    private static void deleteValue(Map<?, FieldLayout> layoutMap, FieldLayout layout)
    {
        for (Iterator<FieldLayout> iterator = layoutMap.values().iterator(); iterator.hasNext();)
        {
            FieldLayout fieldLayout = iterator.next();
            if (fieldLayout.equals(layout))
            {
                iterator.remove();
            }
        }
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    private static class Context
    {
        private final Long project;
        private final String issueType;

        private Context(final Long project, final String issueType)
        {
            this.project = project;
            this.issueType = issueType;
        }

        private Context(Issue issue)
        {
            final Project projectObject = issue.getProjectObject();
            this.project = projectObject != null ? projectObject.getId() : null;

            final IssueType issueType = issue.getIssueTypeObject();
            this.issueType = issueType != null ? issueType.getId() : null;
        }

        public Long getProject()
        {
            return project;
        }

        public String getIssueType()
        {
            return issueType;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final Context context = (Context) o;

            if (issueType != null ? !issueType.equals(context.issueType) : context.issueType != null)
            {
                return false;
            }
            if (project != null ? !project.equals(context.project) : context.project != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = project != null ? project.hashCode() : 0;
            result = 31 * result + (issueType != null ? issueType.hashCode() : 0);
            return result;
        }

    }
}
