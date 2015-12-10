package com.atlassian.jira.issue.fields;

import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.project.Project;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since v4.4
 */
public class MockProjectFieldLayoutSchemeHelper implements ProjectFieldLayoutSchemeHelper
{
    private Multimap<FieldLayout, Project> fieldLayoutProjectMapping;
    private Map<Long, List<Project>> schemeIdProjectMapping;

    public MockProjectFieldLayoutSchemeHelper()
    {
        final Map<FieldLayout, Collection<Project>> backingMap = Maps.newHashMap();
        this.fieldLayoutProjectMapping = Multimaps.newSetMultimap(backingMap, new Supplier<Set<Project>>()
        {
            @Override
            public Set<Project> get()
            {
                return new LinkedHashSet<Project>();
            }
        });
        this.schemeIdProjectMapping = Maps.newHashMap();
    }

    @Override
    public List<Project> getProjectsForScheme(Long schemeId)
    {
        return schemeIdProjectMapping.get(schemeId);
    }

    @Override
    public Multimap<FieldLayout, Project> getProjectsForFieldLayouts(Set<FieldLayout> fieldLayouts)
    {
        final Map<FieldLayout, Collection<Project>> backingMap = Maps.newHashMap();
        final Multimap<FieldLayout, Project> matchingFieldlayouts = Multimaps.newSetMultimap(backingMap, new Supplier<Set<Project>>()
        {
            @Override
            public Set<Project> get()
            {
                return new LinkedHashSet<Project>();
            }
        });
        for (FieldLayout fieldLayout : fieldLayouts)
        {
            matchingFieldlayouts.putAll(fieldLayout, fieldLayoutProjectMapping.get(fieldLayout));
        }
        return matchingFieldlayouts;
    }

    @Override
    public List<Project> getProjectsForFieldLayout(FieldLayout fieldLayout)
    {
        return Lists.newArrayList(fieldLayoutProjectMapping.get(fieldLayout));
    }

    public MockProjectFieldLayoutSchemeHelper setProjectsForFieldLayout(final FieldLayout fieldLayout, final List<Project> layoutProjects)
    {
        fieldLayoutProjectMapping.putAll(fieldLayout, layoutProjects);
        return this;
    }

    public MockProjectFieldLayoutSchemeHelper setProjectsForFieldLayoutScheme(final Long schemeId, final List<Project> projects)
    {
        schemeIdProjectMapping.put(schemeId, projects);
        return this;
    }
}
