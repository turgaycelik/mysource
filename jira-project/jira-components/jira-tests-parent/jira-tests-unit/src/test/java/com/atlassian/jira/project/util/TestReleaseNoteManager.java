/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project.util;

import java.util.Collection;
import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationPropertiesImpl;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.template.mocks.VelocityTemplatingEngineMocks;
import com.atlassian.jira.user.MockUser;

import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.ofbiz.core.entity.GenericValue;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestReleaseNoteManager
{
    public TestReleaseNoteManager()
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
    }

    @Test
    public void testGetReleaseNoteStyles()
    {
        final Map expectedStyles = EasyMap.build("text", "text-template", "html", "html-template");
        final String releaseNoteName = "text, html";
        final String releaseNoteTemplate = "text-template, html-template";
        ApplicationPropertiesImpl applicationProperties = new MyApplicationProperties(releaseNoteName, releaseNoteTemplate);
        ReleaseNoteManager releaseNoteManager = new ReleaseNoteManager(applicationProperties, null, null, null, null);
        assertEquals(expectedStyles, releaseNoteManager.getStyles());
    }

    @Test
    public void testGetReleaseNoteStylesHandlesNulls()
    {
        ApplicationPropertiesImpl applicationProperties = new MyApplicationProperties(null, null);
        ReleaseNoteManager releaseNoteManager = new ReleaseNoteManager(applicationProperties, null, null, null, null);
        releaseNoteManager.getStyles(); // just check no exceptions
    }

    @Test
    public void testGetReleaseNoteStylesWithCorruptedProperties()
    {
        ApplicationPropertiesImpl applicationProperties = new MyApplicationProperties("text", null);
        ReleaseNoteManager releaseNoteManager = new ReleaseNoteManager(applicationProperties, null, null, null, null);
        try
        {
            releaseNoteManager.getStyles();
        }
        catch (RuntimeException re)
        {
            assertNotNull(re.getMessage());
        }
    }

    @Test
    public void testGetReleaseNoteWithInvalidStyleName()
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final User testUser = new MockUser("testuser");
        final GenericValue project = new MockGenericValue("Project", EasyMap.build("key", "ABC", "counter", new Long(1)));
        final Version version = new MockVersion(new MockGenericValue("Version", EasyMap.build("name", "Version 1", "id", new Long(1001), "sequence", new Long(1), "project", new Long(101), "released", "true", "archived", "true")));
        version.setName("Ver 1");
        version.setSequence(1L);

        final ApplicationPropertiesImpl applicationProperties = new MyApplicationProperties("text", "text-template", "text");
        final VelocityTemplatingEngine templatingEngine = VelocityTemplatingEngineMocks.alwaysOutput("BODY").get();
        final ConstantsManager constantsManager = mock(ConstantsManager.class);
        returnTwoIssueTypeGvs(constantsManager);

        final ReleaseNoteManager releaseNoteManager = new ReleaseNoteManager(applicationProperties, templatingEngine, constantsManager, null, null);
        assertEquals("BODY", releaseNoteManager.getReleaseNote(null, "xml", version, testUser, project));
    }

    @Test
    public void testGetReleaseNoteWithInvalidStyleNameAndInvalidDefault()
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final User testUser = new MockUser("testuser");
        final GenericValue project = new MockGenericValue("Project", EasyMap.build("key", "ABC", "counter", new Long(1)));
        final Version version = new MockVersion(new MockGenericValue("Version", EasyMap.build("name", "Version 1", "id", new Long(1001), "sequence", new Long(1), "project", new Long(101), "released", "true", "archived", "true")));
        version.setName("Ver 1");
        version.setSequence(1L);

        final ApplicationPropertiesImpl applicationProperties = new MyApplicationProperties("text", "text-template", "nicks");
        final VelocityTemplatingEngine templatingEngine = VelocityTemplatingEngineMocks.alwaysOutput("BODY").get();
        final ConstantsManager constantsManager = mock(ConstantsManager.class);
        returnTwoIssueTypeGvs(constantsManager);

        final ReleaseNoteManager releaseNoteManager = new ReleaseNoteManager(applicationProperties, templatingEngine, constantsManager, null, null);
        assertEquals("BODY", releaseNoteManager.getReleaseNote(null, "xml", version, testUser, project));
    }

    @Test
    public void testGetReleaseNote()
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final User testUser = new MockUser("testuser");
        final GenericValue project = new MockGenericValue("Project", EasyMap.build("key", "ABC", "counter", new Long(1)));
        final Version version = new MockVersion(new MockGenericValue("Version", EasyMap.build("name", "Version 1", "id", new Long(1001), "sequence", new Long(1), "project", new Long(101), "released", "true", "archived", "true")));
        version.setName("Ver 1");
        version.setSequence(1L);

        final ApplicationPropertiesImpl applicationProperties = new MyApplicationProperties("text", "text-template");
        final VelocityTemplatingEngine templatingEngine = VelocityTemplatingEngineMocks.alwaysOutput("BODY").get();
        final ConstantsManager constantsManager = mock(ConstantsManager.class);
        returnTwoIssueTypeGvs(constantsManager);


        final ReleaseNoteManager releaseNoteManager = new ReleaseNoteManager(applicationProperties, templatingEngine, constantsManager, null, null);
        assertEquals("BODY", releaseNoteManager.getReleaseNote(null, "text", version, testUser, project));
    }

    private OngoingStubbing<Collection<GenericValue>> returnTwoIssueTypeGvs(ConstantsManager constantsManager)
    {
        return when(constantsManager.getIssueTypes()).thenReturn
                (
                        ImmutableList.<GenericValue>of
                                (
                                        new MockGenericValue
                                                (
                                                        "IssueType",
                                                        EasyMap.build
                                                                (
                                                                        "id", "100", "name", "testtype",
                                                                        "description", "test issue type"
                                                                )
                                                ),
                                        new MockGenericValue
                                                (
                                                        "IssueType",
                                                        EasyMap.build
                                                                (
                                                                        "id", "200", "name", "another testtype",
                                                                        "description", "another test issue type")
                                                )
                                )
                );
    }

    private static class MyApplicationProperties extends ApplicationPropertiesImpl
    {
        private final String changeLogName;
        private final String changeLogTemplate;
        private final String defaultTemplate;

        public MyApplicationProperties(String changeLogName, String changeLogTemplate)
        {
            super(null);
            this.changeLogName = changeLogName;
            this.changeLogTemplate = changeLogTemplate;
            defaultTemplate = null;
        }
        public MyApplicationProperties(String changeLogName, String changeLogTemplate, String defaultTemplate)
        {
            super(null);
            this.changeLogName = changeLogName;
            this.changeLogTemplate = changeLogTemplate;
            this.defaultTemplate = defaultTemplate;
        }

        @Override
        public String getDefaultBackedString(String name)
        {
            if (ReleaseNoteManager.RELEASE_NOTE_NAME.equals(name))
            {
                return changeLogName;
            }
            else if (ReleaseNoteManager.RELEASE_NOTE_TEMPLATE.equals(name))
            {
                return changeLogTemplate;
            }
            else if (ReleaseNoteManager.RELEASE_NOTE_DEFAULT.equals(name))
            {
                return defaultTemplate;
            }
            else
            {
                return null;
            }
        }

        @Override
        public String getString(String name)
        {
            if (APKeys.JIRA_BASEURL.equals(name))
            {
                return "jira";
            }
            else
            {
                return null;
            }
        }
    }
}
