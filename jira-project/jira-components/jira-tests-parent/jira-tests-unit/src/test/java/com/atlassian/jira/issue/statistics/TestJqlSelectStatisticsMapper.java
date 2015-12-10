/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.statistics;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.issue.customfields.converters.SelectConverterImpl;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.statistics.GroupPickerStatisticsMapper;
import com.atlassian.jira.issue.customfields.statistics.ProjectSelectStatisticsMapper;
import com.atlassian.jira.issue.customfields.statistics.SelectStatisticsMapper;
import com.atlassian.jira.issue.customfields.statistics.TextStatisticsMapper;
import com.atlassian.jira.issue.customfields.statistics.UserPickerStatisticsMapper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operator.Operator;

import com.mockobjects.dynamic.Mock;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestJqlSelectStatisticsMapper extends MockControllerTestCase
{
    private ClauseNames clauseNames = new ClauseNames("cf[10001]");
    private JiraAuthenticationContext authenticationContext;
    private CustomFieldInputHelper customFieldInputHelper;
    private User user;

    @Before
    public void setUp() throws Exception
    {
        authenticationContext = getMock(JiraAuthenticationContext.class);
        user = new MockUser("fred");
        expect(authenticationContext.getLoggedInUser()).andStubReturn(user);
        customFieldInputHelper = EasyMock.createMock(CustomFieldInputHelper.class);
    }

    @Test
    public void testGetSearchUrlSuffixNullSearchRequest() throws Exception
    {
        final CustomField customField = mockController.getMock(CustomField.class);
        expect(customField.getClauseNames()).andReturn(new ClauseNames("customfield_10001"));
        expect(customField.getName()).andReturn("Number CF");
        expect(customFieldInputHelper.getUniqueClauseName(user, "customfield_10001", "Number CF")).andReturn("cf[10001]");
        mockController.replay();
        EasyMock.replay(customFieldInputHelper);
        SelectStatisticsMapper mapper = new SelectStatisticsMapper(customField, new SelectConverterImpl(null), authenticationContext, customFieldInputHelper);
        assertNull(mapper.getSearchUrlSuffix(null, null));
        mockController.verify();
    }

    @Test
    public void testGetSearchUrlSuffixNullValue() throws Exception
    {
        final CustomField customField = mockController.getMock(CustomField.class);
        expect(customField.getClauseNames()).andReturn(new ClauseNames("cf[10001]"));
        expect(customField.getName()).andReturn("Number CF");
        final SearchRequest searchRequest = mockController.getMock(SearchRequest.class);
        expect(customFieldInputHelper.getUniqueClauseName(user, "cf[10001]", "Number CF")).andReturn("cf[10001]");

        searchRequest.getQuery();
        mockController.setReturnValue(new QueryImpl());

        mockController.replay();
        EasyMock.replay(customFieldInputHelper);
        SelectStatisticsMapper mapper = new SelectStatisticsMapper(customField, new SelectConverterImpl(null), authenticationContext, customFieldInputHelper);
        final SearchRequest urlSuffix = mapper.getSearchUrlSuffix(null, searchRequest);
        assertEquals(new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.IS, EmptyOperand.EMPTY), urlSuffix.getQuery().getWhereClause());
        mockController.verify();
        EasyMock.verify(customFieldInputHelper);
    }

    @Test
    public void testGetSearchUrlSuffix() throws Exception
    {
        final CustomField customField = mockController.getMock(CustomField.class);
        expect(customField.getClauseNames()).andReturn(new ClauseNames("cf[10001]"));
        expect(customField.getName()).andReturn("Number CF");

        final SearchRequest searchRequest = mockController.getMock(SearchRequest.class);
        EasyMock.expect(customFieldInputHelper.getUniqueClauseName(user, "cf[10001]", "Number CF")).andReturn("cf[10001]");

        searchRequest.getQuery();
        mockController.setReturnValue(new QueryImpl());

        mockController.replay();
        EasyMock.replay(customFieldInputHelper);
        SelectStatisticsMapper mapper = new SelectStatisticsMapper(customField, new SelectConverterImpl(null), authenticationContext, customFieldInputHelper);
        Option option = new MockOption(null, null, 1L, "value", null, 1L);
        final SearchRequest urlSuffix = mapper.getSearchUrlSuffix(option, searchRequest);
        assertEquals(new TerminalClauseImpl(clauseNames.getPrimaryName(), Operator.EQUALS, "value"), urlSuffix.getQuery().getWhereClause());
        mockController.verify();
        EasyMock.verify(customFieldInputHelper);
    }

    @Test
    public void testEquals()
    {
        mockController.replay();
        Mock mockCustomField = new Mock(CustomField.class);
        mockCustomField.setStrict(true);
        String cfId = "customfield_10001";
        mockCustomField.expectAndReturn("getId", cfId);
        mockCustomField.expectAndReturn("getClauseNames", new ClauseNames("cf[10001]"));


        SelectStatisticsMapper mapper = new SelectStatisticsMapper((CustomField) mockCustomField.proxy(), new SelectConverterImpl(null), authenticationContext, customFieldInputHelper);

        // identity test
        assertTrue(mapper.equals(mapper));
        assertEquals(mapper.hashCode(), mapper.hashCode());

        Mock mockCustomField2 = new Mock(CustomField.class);
        mockCustomField2.setStrict(true);
        mockCustomField2.expectAndReturn("getId", "customfield_10001");
        mockCustomField2.expectAndReturn("getClauseNames", new ClauseNames("cf[10001]"));

        SelectStatisticsMapper mapper2 = new SelectStatisticsMapper((CustomField) mockCustomField2.proxy(), new SelectConverterImpl(null), authenticationContext, customFieldInputHelper);

        // As the mappers are using the same custom field they should be equal
        assertTrue(mapper.equals(mapper2));
        mockCustomField.verify();
        mockCustomField2.verify();
        assertEquals(mapper.hashCode(), mapper2.hashCode());

        CustomField mockCustomField3 = EasyMock.createMock(CustomField.class);
        final String cfId2 = "customfield_10002";
        EasyMock.expect(mockCustomField3.getId()).andReturn(cfId2);
        EasyMock.expect(mockCustomField3.getId()).andReturn(cfId2);
        EasyMock.expect(mockCustomField3.getClauseNames()).andReturn(new ClauseNames("cf[10001]"));
        EasyMock.replay(mockCustomField3);
        SelectStatisticsMapper mapper3 = new SelectStatisticsMapper(mockCustomField3, new SelectConverterImpl(null), authenticationContext, customFieldInputHelper);

        // As the mappers are using different custom field they should *not* be equal
        assertFalse(mapper.equals(mapper3));
        assertFalse(mapper.hashCode() == mapper3.hashCode());


        assertFalse(mapper.equals(null));
        assertFalse(mapper.equals(new Object()));
        assertFalse(mapper.equals(new IssueKeyStatisticsMapper()));
        assertFalse(mapper.equals(new IssueTypeStatisticsMapper(null)));
        // ensure other implmentations of same base class are not equal even though they both extend
        // they inherit the equals and hashCode implementations
        assertFalse(mapper.equals(new UserPickerStatisticsMapper((CustomField) mockCustomField.proxy(), null, null)));
        assertFalse(mapper.equals(new TextStatisticsMapper((CustomField) mockCustomField.proxy())));
        assertFalse(mapper.equals(new ProjectSelectStatisticsMapper((CustomField) mockCustomField.proxy(), null)));
        assertFalse(mapper.equals(new GroupPickerStatisticsMapper((CustomField) mockCustomField.proxy(), null, authenticationContext, customFieldInputHelper)));

        EasyMock.verify(mockCustomField3);
        mockCustomField.verify();
    }
}
