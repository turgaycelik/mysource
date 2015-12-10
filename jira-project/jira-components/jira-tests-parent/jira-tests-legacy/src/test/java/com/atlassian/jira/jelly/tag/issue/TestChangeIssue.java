/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.issue;

import java.sql.Timestamp;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import com.atlassian.jira.jelly.tag.util.JellyTagUtils;
import com.atlassian.jira.web.FieldVisibilityManager;

import com.google.common.collect.ImmutableMap;

import org.easymock.classextension.EasyMock;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;

import electric.xml.Document;
import electric.xml.Element;

public class TestChangeIssue extends AbstractJellyTestCase
{
    private FieldVisibilityManager origFieldVisibilityManager;
    private GenericValue project;

    public TestChangeIssue(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        origFieldVisibilityManager = ComponentAccessor.getComponent(FieldVisibilityManager.class);
        final FieldVisibilityManager visibilityManager = EasyMock.createMock(FieldVisibilityManager.class);
        EasyMock.expect(visibilityManager.isFieldHidden((String)EasyMock.anyObject(), (Issue)EasyMock.anyObject())).andReturn(false).anyTimes();
        EasyMock.expect(visibilityManager.isFieldVisible((String) EasyMock.anyObject(), (Issue) EasyMock.anyObject())).andReturn(true).anyTimes();
        EasyMock.replay(visibilityManager);
        ManagerFactory.addService(FieldVisibilityManager.class, visibilityManager);

        UtilsForTests.getTestEntity("ProjectKey", EasyMap.build("projectKey", "ABC", "projectId", new Long(1)));
        project = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC", "name", "A Project", "id", new Long(1)));
        // Create test issue
        UtilsForTests.getTestEntity("Issue", EasyMap.build("project", project.getLong("id"), "number", 1L, "updated", new Timestamp(1000)));
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        ManagerFactory.addService(FieldVisibilityManager.class, origFieldVisibilityManager);
    }

    public void testTheTag() throws Exception
    {
        final String scriptFilename = "change-issue-tag.test.the-tag.jelly";
        Document document = runScript(scriptFilename);

        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        GenericValue issue = EntityUtil.getOnly(CoreFactory.getGenericDelegator().findByAnd("Issue", ImmutableMap.of("project", project.getLong("id"), "number", 1L)));
        assertNotNull(issue);
        assertEquals(JellyTagUtils.parseDate("2000-01-14 12:00:00.0"), issue.get("updated"));
    }

    protected String getRelativePath()
    {
        return "tag" + FS + "issue" + FS;
    }
}
