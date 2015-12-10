package com.atlassian.jira.issue.customfields.impl;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.hamcrest.collection.IsArrayWithSize;
import org.junit.Test;

import com.google.common.collect.Lists;

public class TestSelectCustomFieldPermissionQueryBuilder
{
    /**
     * Checks if query (as a part of permission's filer security level) is built
     * with correct clauses in case there are appropriate CF options for group
     * name.
     */
    @Test
    public void testGetQueryForGroupOptionFound() throws Exception
    {
        BooleanQuery queryForGroup = (BooleanQuery) SelectCustomFieldPermissionQueryBuilder.getQueryForGroup("cf", "group", Lists.newArrayList("1", "2"));
        assertThat(queryForGroup.getClauses(), IsArrayWithSize.arrayWithSize(2));
        assertThat(queryForGroup.getClauses()[0].getQuery(), is((Query)new TermQuery(new Term("cf_raw", "1"))));
        assertThat(queryForGroup.getClauses()[1].getQuery(), is((Query)new TermQuery(new Term("cf_raw", "2"))));
    }

    /**
     * Checks if query is built with correct clauses in case there are not appropriate CF options for group name.
     */
    @Test
    public void testGetQueryForGroupOptionNotFound() throws Exception
    {
        BooleanQuery queryForGroup = (BooleanQuery) SelectCustomFieldPermissionQueryBuilder.getQueryForGroup("cf", "group", Lists.<String>newArrayList());
        assertThat(queryForGroup.getClauses(), IsArrayWithSize.arrayWithSize(1));
        assertThat(queryForGroup.getClauses()[0].getQuery(), is((Query)new TermQuery(new Term("cf_raw", "group"))));
    }

}
