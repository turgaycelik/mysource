/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.customfields.converters.SelectConverterImpl;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.statistics.GroupPickerStatisticsMapper;
import com.atlassian.jira.issue.customfields.statistics.SelectStatisticsMapper;
import com.atlassian.jira.issue.customfields.statistics.TextStatisticsMapper;
import com.atlassian.jira.issue.customfields.statistics.UserPickerStatisticsMapper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.security.JiraAuthenticationContext;

import com.mockobjects.dynamic.Mock;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestTextStatisticsMapper
{
    @Test
    public void testEquals()
    {
        Mock mockCustomField = new Mock(CustomField.class);
        mockCustomField.setStrict(true);
        String cfId = "customfield_10001";
        mockCustomField.expectAndReturn("getId", cfId);

        TextStatisticsMapper mapper = new TextStatisticsMapper((CustomField) mockCustomField.proxy());

        // identity test
        assertTrue(mapper.equals(mapper));
        assertEquals(mapper.hashCode(), mapper.hashCode());

        Mock mockCustomField2 = new Mock(CustomField.class);
        mockCustomField2.setStrict(true);
        mockCustomField2.expectAndReturn("getId", "customfield_10001");
        TextStatisticsMapper mapper2 = new TextStatisticsMapper((CustomField) mockCustomField2.proxy());

        // As the mappers are using the same custom field they should be equal
        assertTrue(mapper.equals(mapper2));
        mockCustomField.verify();
        mockCustomField2.verify();
        assertEquals(mapper.hashCode(), mapper2.hashCode());

        Mock mockCustomField3 = new Mock(CustomField.class);
        mockCustomField3.setStrict(true);
        mockCustomField3.expectAndReturn("getId", "customfield_10002");
        TextStatisticsMapper mapper3 = new TextStatisticsMapper((CustomField) mockCustomField3.proxy());

        // As the mappers are using different custom field they should *not* be equal
        assertFalse(mapper.equals(mapper3));
        mockCustomField3.verify();
        assertFalse(mapper.hashCode() == mapper3.hashCode());

        assertFalse(mapper.equals(null));
        assertFalse(mapper.equals(new Object()));
        assertFalse(mapper.equals(new IssueKeyStatisticsMapper()));
        assertFalse(mapper.equals(new IssueTypeStatisticsMapper(null)));
        // ensure other implmentations of same base class are not equal even though they both extend
        // they inherit the equals and hashCode implementations
        assertFalse(mapper.equals(new UserPickerStatisticsMapper((CustomField) mockCustomField.proxy(), null, null)));
        assertFalse(mapper.equals(new ProjectStatisticsMapper(null, "clause", cfId)));
        Mock authenticationContext = new Mock(JiraAuthenticationContext.class);
        Mock customFieldInputHelper = new Mock(CustomFieldInputHelper.class);
        ClauseNames clauseNames = new ClauseNames("clauseName");
        mockCustomField.expectAndReturn("getClauseNames", clauseNames);
        assertFalse(mapper.equals(new GroupPickerStatisticsMapper((CustomField) mockCustomField.proxy(), null, (JiraAuthenticationContext) authenticationContext.proxy(), (CustomFieldInputHelper) customFieldInputHelper.proxy())));
        assertFalse(mapper.equals(new SelectStatisticsMapper((CustomField) mockCustomField.proxy(), new SelectConverterImpl(null), (JiraAuthenticationContext) authenticationContext.proxy(), (CustomFieldInputHelper) customFieldInputHelper.proxy())));

        mockCustomField.verify();
    }
}
