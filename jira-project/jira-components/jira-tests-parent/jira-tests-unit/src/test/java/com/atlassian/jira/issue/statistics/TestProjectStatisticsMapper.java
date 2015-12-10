/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.collect.MapBuilder;

import com.mockobjects.dynamic.Mock;

import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class TestProjectStatisticsMapper
{
    private final String clauseName = "name";

    @Test
    public void testGetDocumentConstant()
    {
        ProjectStatisticsMapper sorter = new ProjectStatisticsMapper(null);
        assertEquals(DocumentConstants.PROJECT_ID, sorter.getDocumentConstant());
    }

    @Test
    public void testGetDocumentConstantCustom()
    {
        ProjectStatisticsMapper sorter = new ProjectStatisticsMapper(null, clauseName, "abc");
        assertEquals("abc", sorter.getDocumentConstant());
    }

    @Test
    public void testEqualsWithDefaultDocumentConstant()
    {
        ProjectStatisticsMapper sorter = new ProjectStatisticsMapper(null);
        assertEquals(sorter, sorter);
        assertEquals(sorter.hashCode(), sorter.hashCode());

        Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.setStrict(true);

        // Ensure we have a new string object
        ProjectStatisticsMapper sorter2 = new ProjectStatisticsMapper((ProjectManager) mockProjectManager.proxy());
        assertEquals(sorter, sorter2);
        assertEquals(sorter.hashCode(), sorter2.hashCode());
        mockProjectManager.verify();
    }

    @Test
    public void testEqualsWithCustomDocumentConstant()
    {
        String documentConstant = "abc";
        ProjectStatisticsMapper sorter = new ProjectStatisticsMapper(null, clauseName, documentConstant);
        assertEquals(sorter, sorter);
        assertEquals(sorter.hashCode(), sorter.hashCode());

        Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.setStrict(true);

        // Ensure we have a new string object
        ProjectStatisticsMapper sorter2 = new ProjectStatisticsMapper((ProjectManager) mockProjectManager.proxy(), new String(clauseName), new String("abc"));
        assertEquals(sorter, sorter2);
        assertEquals(sorter.hashCode(), sorter2.hashCode());
        mockProjectManager.verify();

        assertFalse(sorter.equals(new ProjectStatisticsMapper(null, clauseName, "def")));
        assertFalse(sorter.equals(new ProjectStatisticsMapper((ProjectManager) mockProjectManager.proxy(), clauseName, "def")));
        assertFalse(sorter.equals(null));
        assertFalse(sorter.equals(new Object()));
        // Ensure a different sorter with the same document constant is not equal
        assertFalse(sorter.equals(new TextFieldSorter(documentConstant)));
        assertFalse(sorter.equals(new IssueKeyStatisticsMapper()));
    }

    @Test
    public void testGetValueFromLuceneField()
    {
        final ProjectManager mockProjectManager = createMock(ProjectManager.class);

        final MockGenericValue projectGV = new MockGenericValue("Project", MapBuilder.singletonMap("id", 23232L));
        expect(mockProjectManager.getProject(23232L)).andReturn(projectGV);

        replay(mockProjectManager);
        final ProjectStatisticsMapper projectStatsMapper = new ProjectStatisticsMapper(mockProjectManager, null, null);
        final Object nullResult = projectStatsMapper.getValueFromLuceneField(null);
        assertNull(nullResult);
        final Object anotherNullResult = projectStatsMapper.getValueFromLuceneField("-1");
        assertNull(anotherNullResult);

        final Object projectObject = projectStatsMapper.getValueFromLuceneField("23232");
        assertEquals(projectGV, projectObject);

        verify(mockProjectManager);
    }

}
