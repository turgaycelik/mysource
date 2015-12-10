package com.atlassian.jira.web.action.browser;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.VersionProxy;
import com.atlassian.jira.project.util.ReleaseNoteManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionImpl;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.collect.ImmutableMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static webwork.action.Action.INPUT;

/**
 * Unit test of {@link ReleaseNote}.
 *
 * @since 6.2
 */
public class TestReleaseNote
{
    @Mock private I18nHelper mockI18nHelper;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        final JiraAuthenticationContext mockJiraAuthenticationContext = Mockito.mock(JiraAuthenticationContext.class);
        when(mockJiraAuthenticationContext.getI18nHelper()).thenReturn(mockI18nHelper);
        new MockComponentWorker().init()
                .addMock(OfBizDelegator.class, new MockOfBizDelegator())
                .addMock(JiraAuthenticationContext.class, mockJiraAuthenticationContext);
    }

    @After
    public void tearDownWorker()
    {
        ComponentAccessor.initialiseWorker(null);
    }

    @Test
    public void testGetVersionsCallsProjectManagerCorrectly() throws Exception
    {
        // Set up
        final VersionManager mockVersionManager = mock(VersionManager.class);

        final long projectId = 79;
        final Version unreleasedVersion = new VersionImpl(
                null, new MockGenericValue("Version", EasyMap.build("name", "Version 1", "id", 1001L)));

        final Collection<Version> unreleasedVersions = Arrays.asList(unreleasedVersion);
        final ProjectManager mockProjectManager = mock(ProjectManager.class);
        final GenericValue projectGV =
                UtilsForTests.getTestEntity("Project", EasyMap.build("name", "ABC Project 1", "id", projectId));
        final MockProject project = new MockProject(projectGV);
        when(mockProjectManager.getProjectObj(projectId)).thenReturn(project);
        when(mockVersionManager.getVersionsUnreleased(projectId, false)).thenReturn(unreleasedVersions);
        final Collection<Version> releasedVersions = Collections.emptyList();
        when(mockVersionManager.getVersionsReleased(projectId, false)).thenReturn(releasedVersions);
        final ReleaseNote releaseNote = new ReleaseNote(mockProjectManager, null, null, mockVersionManager);
        releaseNote.setProjectId(projectId);
        releaseNote.doDefault();
        when(mockI18nHelper.getText("common.filters.unreleasedversions")).thenReturn("Unreleased Versions");

        // Invoke
        final Collection versions = releaseNote.getVersions();

        // Check
        final Collection<?> expectedVersions =
                Arrays.asList(new VersionProxy(-2, "Unreleased Versions"), new VersionProxy(unreleasedVersion));
        assertEquals(expectedVersions, versions);
    }

    @Test
    public void testGetStyleNames()
    {
        // Set up
        final ReleaseNoteManager mockReleaseNoteManager = mock(ReleaseNoteManager.class);
        when(mockReleaseNoteManager.getStyles()).thenReturn(ImmutableMap.of("text", "text-template", "html", "html-template"));
        final ReleaseNote releaseNote = new ReleaseNote(null, mockReleaseNoteManager, null, null);

        // Invoke
        final Collection<?> styleNames = releaseNote.getStyleNames();

        // Check
        assertEquals(2, styleNames.size());
        assertTrue(styleNames.contains("text"));
        assertTrue(styleNames.contains("html"));
    }

    @Test
    public void testInputResultForInvalidVersion() throws Exception
    {
        assertInvalidVersion(null, null);
        assertInvalidVersion("-1", "");
        assertInvalidVersion("-2", "");
        assertInvalidVersion("-3", "");
    }

    private void assertInvalidVersion(final String version, final String styleName) throws Exception
    {
        // Set up
        final String errorMessage = "anything";
        when(mockI18nHelper.getText("releasenotes.version.select")).thenReturn(errorMessage);

        final ReleaseNote releaseNote = new ReleaseNote(null, null, null, null);
        releaseNote.setVersion(version);
        releaseNote.setStyleName(styleName);

        // Invoke
        final String result = releaseNote.execute();

        // Check
        assertEquals(INPUT, result);
        assertEquals(errorMessage, releaseNote.getErrors().get("version"));
    }

    @Test
    public void testGetterSetters()
    {
        final long projectId = 1L;
        final String styleName = "test-style";
        final String version = "test-version";

        final ProjectManager mockProjectManager = mock(ProjectManager.class);
        final ReleaseNote releaseNote = new ReleaseNote(mockProjectManager, null, null, null);

        releaseNote.setProjectId(projectId);
        assertEquals(projectId, releaseNote.getProjectId());

        releaseNote.setStyleName(styleName);
        assertEquals(styleName, releaseNote.getStyleName());

        releaseNote.setVersion(version);
        assertEquals(version, releaseNote.getVersion());
    }
}
