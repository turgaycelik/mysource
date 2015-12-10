package com.atlassian.jira.dev.reference.plugin.extractor;

import java.util.Set;

import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.jira.issue.Issue;

import com.google.common.collect.ImmutableSet;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

public class IssueSearchExtractorImpl implements com.atlassian.jira.index.IssueSearchExtractor
{
    @Override
    public Set<String> indexEntity(final Context<Issue> ctx, final Document doc)
    {
        final Issue entity = ctx.getEntity();
        doc.add(new Field("reverse_summary", StringUtils.reverse(entity.getSummary()), Field.Store.NO, Field.Index.ANALYZED));
        return ImmutableSet.of("reverse_summary");
    }
}
