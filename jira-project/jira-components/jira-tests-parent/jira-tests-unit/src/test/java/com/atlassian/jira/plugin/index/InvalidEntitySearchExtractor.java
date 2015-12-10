package com.atlassian.jira.plugin.index;

import java.util.Set;

import com.atlassian.jira.index.EntitySearchExtractor;
import com.atlassian.jira.project.Project;

import com.google.common.collect.ImmutableSet;

import org.apache.lucene.document.Document;

public class InvalidEntitySearchExtractor implements EntitySearchExtractor<Project>
{
    @Override
    public Set<String> indexEntity(final Context<Project> ctx, final Document doc)
    {
        return ImmutableSet.of();
    }
}
