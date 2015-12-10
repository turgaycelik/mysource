package com.atlassian.jira.project.version;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.fugue.Option;
import com.atlassian.jira.association.NodeAssocationType;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.event.project.VersionMergeEvent;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.UpdateIssueRequest;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.CollectionReorderer;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class TestDefaultVersionManager
{
    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private ProjectManager projectManager;
    @Mock
    private NodeAssociationStore nodeAssociationStore;
    @Mock
    private IssueIndexManager issueIndexManager;
    @Mock
    private IssueManager issueManager;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    @AvailableInContainer
    private OfBizDelegator ofBizDelegator;
    @Mock
    private VersionStore versionStore;

    private DefaultVersionManager versionManager;

    private MockProject project;
    private GenericValue projectGV;

    private Version version1;
    private Version version2;
    private Version version3;
    private Version version4;
    private Version version5;
    private Version version6;
    private Version version7;
    private Version version8;

    private Version versionOne;

    private ApplicationUser mockUser = new MockApplicationUser("admin");
    private UpdateIssueRequest updateIssueRequest;

    @Before
    public void setUp() throws Exception
    {
        versionManager = new DefaultVersionManager(
                issueManager,
                new CollectionReorderer<Version>(),
                nodeAssociationStore,
                issueIndexManager,
                projectManager,
                versionStore,
                eventPublisher);

        updateIssueRequest = UpdateIssueRequest.builder().
                eventDispatchOption(EventDispatchOption.ISSUE_UPDATED).
                sendMail(false).build();

        project = new MockProject(1L, "ABC", "Project 1");
        projectGV = new MockGenericValue("Project", ImmutableMap.of("key", project.getKey(), "name", project.getName(), "id",
                project.getId()));

        when(projectManager.getProject(project.getId())).thenReturn(projectGV);
        when(projectManager.getProjectObj(project.getId())).thenReturn(project);

        version1 = new VersionImpl(projectManager, new MockGenericValue("Version", ImmutableMap.builder()
                .put("name", "Version 1")
                .put("id", Long.valueOf(1001))
                .put("sequence", Long.valueOf(1))
                .put("project", project.getId())
                .build()));
        version2 = new VersionImpl(projectManager, new MockGenericValue("Version", ImmutableMap.builder()
                .put("name", "Version 2")
                .put("id", Long.valueOf(1002))
                .put("sequence", Long.valueOf(2))
                .put("project", project.getId())
                .put("releasedate", new Timestamp(1))
                .put("description", "The description")
                .build()));
        version3 = new VersionImpl(projectManager, new MockGenericValue("Version", ImmutableMap.builder()
                .put("name", "Version 3")
                .put("id", Long.valueOf(1003))
                .put("sequence", Long.valueOf(3))
                .put("project", project.getId())
                .build()));
        version4 = new VersionImpl(projectManager, new MockGenericValue("Version", ImmutableMap.builder()
                .put("name", "Version 4")
                .put("id", Long.valueOf(1004))
                .put("sequence", Long.valueOf(4))
                .put("project", project.getId())
                .put("archived", "true")
                .put("released", "true")
                .build()));
        version5 = new VersionImpl(projectManager, new MockGenericValue("Version", ImmutableMap.builder()
                .put("name", "Version 5")
                .put("id", Long.valueOf(1005))
                .put("sequence", Long.valueOf(5))
                .put("project", project.getId())
                .put("archived", "true")
                .put("released", "true")
                .build()));
        version6 = new VersionImpl(projectManager, new MockGenericValue("Version", ImmutableMap.builder()
                .put("name", "Version 6")
                .put("id", Long.valueOf(1006))
                .put("sequence", Long.valueOf(6))
                .put("project", project.getId())
                .put("archived", "true")
                .put("released", "true")
                .build()));
        version7 = new VersionImpl(projectManager, new MockGenericValue("Version", ImmutableMap.builder()
                .put("name", "Version 7")
                .put("id", Long.valueOf(1007))
                .put("sequence", Long.valueOf(7))
                .put("project", project.getId())
                .put("released", "true")
                .build()));
        version8 = new VersionImpl(projectManager, new MockGenericValue("Version", ImmutableMap.builder()
                .put("name", "Version 8")
                .put("id", Long.valueOf(1008))
                .put("sequence", Long.valueOf(8))
                .put("project", project.getId())
                .put("archived", "true")
                .build()));

        versionOne = new VersionImpl(projectManager, new MockGenericValue("Version", ImmutableMap.builder()
                .put("name", "Version 1")
                .put("id", Long.valueOf(1005))
                .put("sequence", Long.valueOf(1))
                .put("project", project.getId())
                .put("archived", Boolean.FALSE)
                .put("released", Boolean.FALSE)
                .build()));

        resetAndInitVersionStore();
    }

    // ---- Create Version Tests ----
    @Test
    public void testCreateVersionInvalidParams() throws Exception
    {
        expectedException.expect(CreateException.class);
        expectedException.expectMessage("You cannot create a version without a name.");
        versionManager.createVersion(null, null, null, (Long) null, null);
    }

    @Test
    public void testCreateFirstVersion() throws GenericEntityException, CreateException
    {
        final String expectedVersionName = "Version Name";
        final Version version = versionManager.createVersion(expectedVersionName, null, null, project.getId(), null);
        assertVersion(expectedVersionName, null, null, project, Long.valueOf(1L), version);
    }

    @Test
    public void testCreateSecondVersionHasCorrectSequenceId() throws GenericEntityException, CreateException
    {
        final String expectedVersionName = "Version Name";
        when(versionStore.getVersionsByProject(project.getId())).thenReturn(Collections.singletonList(version1.getGenericValue()));
        final Version version = versionManager.createVersion(expectedVersionName, null, null, project.getId(), null);
        assertVersion(expectedVersionName, null, null, project, Long.valueOf(2L), version);
    }

    @Test
    public void testCreateVersionInFirstSequencePosition() throws GenericEntityException, CreateException
    {
        when(versionStore.getVersionsByProject(project.getId())).thenReturn(Collections.singletonList(version1.getGenericValue()));
        when(ofBizDelegator.createValue(
                OfBizDelegator.VERSION,
                ImmutableMap.<String, Object>builder()
                        .put("name", "Version 2")
                        .put("project", project.getId())
                        .put("sequence", Long.valueOf(1L))
                        .build())).thenReturn(version2.getGenericValue());

        final String expectedVersionName = "Version Name";
        final Version version = versionManager.createVersion(expectedVersionName, null, null, project.getId(), new Long(-1));
        assertVersion(expectedVersionName, null, null, project, Long.valueOf(1L), version);
        assertEquals(new Long(2), version1.getSequence());
    }

    private void assertVersion(final String expectedVersionName, final Date expectedReleaseDate, final String expectedDescription,
            final Project expectedProject, final Long expectedSequence, final Version actual)
    {
        assertEquals(expectedVersionName, actual.getName());
        assertEquals(expectedReleaseDate, actual.getReleaseDate());
        assertEquals(expectedDescription, actual.getDescription());
        assertEquals(expectedProject, actual.getProjectObject());
        assertEquals(expectedSequence, actual.getSequence());
    }

    @Test
    public void testCreateVersionWithReleaseDateAndDescription() throws GenericEntityException, CreateException
    {
        final String expectedVersionName = "Version Name";
        final String expectedDescription = "Version description";
        final Date expectedReleaseDate = new Date();

        when(versionStore.getVersionsByProject(project.getId())).thenReturn(Collections.singletonList(version1.getGenericValue()));
        final Version version = versionManager.createVersion(expectedVersionName, expectedReleaseDate, expectedDescription, project.getId(),
                null);
        assertVersion(expectedVersionName, expectedReleaseDate, expectedDescription, project, 2L, version);
    }

    // ---- Version Scheduling Tests ----
    @Test
    public void testStoreReorderedVersionListNoAffectedIssues() throws GenericEntityException
    {
        _testStoreReorderedVersionList();
    }

    @Test
    public void testStoreReorderedVersionListWithAffectedIssues() throws GenericEntityException
    {

        _testStoreReorderedVersionList();
    }

    private void _testStoreReorderedVersionList() throws GenericEntityException
    {
        // versions 2+3 are swapped, so we expect that the issues for these versions are flushed
        versionManager.storeReorderedVersionList(Arrays.asList(version1, version3, version2, version4));

        assertEquals(new Long(2), version3.getSequence());
        assertEquals(new Long(3), version2.getSequence());
    }

    @Test
    public void testDeleteVersion() throws GenericEntityException
    {
        // it is called after version was deleted - because of this one it does not contains version 2
        when(versionStore.getVersionsByProject(project.getId())).thenReturn(
                Arrays.asList(version1.getGenericValue(), version3.getGenericValue(), version4.getGenericValue()));
        versionManager.deleteVersion(version2);

        // checks that request version was deleted over store
        verify(versionStore).deleteVersion(version2.getGenericValue());

        // order was corrupted
        // checks that new sequence is OK
        assertEquals(new Long(2), version3.getSequence());
        assertEquals(new Long(3), version4.getSequence());
        // and that it was stored
        verify(versionStore).storeVersions(Arrays.asList(version3, version4));
    }

    // ---- Version Edit Tests ----
    @Test
    public void testEditVersionNameWithInvalidName() throws GenericEntityException
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("You must specify a valid version name.");
        versionManager.editVersionDetails(null, null, null, null);
    }

    @Test
    public void testEditVersionNameWithDuplicateName() throws GenericEntityException
    {
        project.setVersions(Collections.singletonList(version1));
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("A version with this name already exists in this project.");
        versionManager.editVersionDetails(versionOne, "Version 1", null, projectGV);
    }

    @Test
    public void testEditVersionName() throws GenericEntityException
    {
        final String newVersionName = "Version 2";
        versionManager.editVersionDetails(version1, newVersionName, null, projectGV);
        assertEquals(newVersionName, version1.getName());
    }

    @Test
    public void testMoveVersionAfterToFirstAndLast()
    {
        // assert the intial sequence order (1, 2, 3 and 4)
        assertEquals(new Long(1), version1.getSequence());
        assertEquals(new Long(2), version2.getSequence());
        assertEquals(new Long(3), version3.getSequence());
        assertEquals(new Long(4), version4.getSequence());

        List<Version> currentVersions, expectedVersions, alteredVersions;

        currentVersions = Arrays.asList(version1, version2, version3, version4);

        // move pos 1 to last using null (modifies all versions as it needs to shift everything forward by 1)
        // v1 ---+   v2
        // v2    | = v3
        // v3    |   v4
        // v4    |
        //    <--+   v1
        expectedVersions = Arrays.asList(version2, version3, version4, version1);
        alteredVersions = Arrays.asList(version2, version3, version4, version1);
        currentVersions = assertMoveVersionAfter(alteredVersions, currentVersions, version1, null, expectedVersions);

        // move pos 2 to last using null (modifies 3, 4 and 1)
        // v2        v2
        // v3 ---+ = v4
        // v4    |   v1
        // v1    |
        //    <--+   v3
        expectedVersions = Arrays.asList(version2, version4, version1, version3);
        alteredVersions = Arrays.asList(version4, version1, version3);
        resetAndInitVersionStore();
        currentVersions = assertMoveVersionAfter(alteredVersions, currentVersions, version3, null, expectedVersions);

        // move pos 3 to last using null (modifies 1 and 3)
        // v2        v2
        // v4      = v4
        // v1 ---+   v3
        // v3    |
        //    <--+   v1
        expectedVersions = Arrays.asList(version2, version4, version3, version1);
        alteredVersions = Arrays.asList(version3, version1);
        resetAndInitVersionStore();
        currentVersions = assertMoveVersionAfter(alteredVersions, currentVersions, version1, null, expectedVersions);

        // move pos 4 to last using null (should do nothing)
        // v2        v2
        // v4      = v4
        // v3        v3
        // v1 ---+   v1
        //    <--+
        expectedVersions = Arrays.asList(version2, version4, version3, version1);
        alteredVersions = null;
        resetAndInitVersionStore();
        currentVersions = assertMoveVersionAfter(alteredVersions, currentVersions, version1, null, expectedVersions);

        // move pos 4 to first using -1 (modifies all versions as it needs to shift everything back by 1)
        //    <--+   v1
        // v2    |
        // v4    | = v2
        // v3    |   v4
        // v1 ---+   v3
        expectedVersions = Arrays.asList(version1, version2, version4, version3);
        alteredVersions = Arrays.<Version>asList(version1, version2, version4, version3);
        resetAndInitVersionStore();
        currentVersions = assertMoveVersionAfter(alteredVersions, currentVersions, version1, new Long(-1), expectedVersions);

        // move pos 3 to first using -1 (modifies 1, 2 and 4)
        //    <--+   v4
        // v1    |
        // v2    | = v1
        // v4 ---+   v2
        // v3        v3
        expectedVersions = Arrays.asList(version4, version1, version2, version3);
        alteredVersions = Arrays.<Version>asList(version4, version1, version2);
        resetAndInitVersionStore();
        currentVersions = assertMoveVersionAfter(alteredVersions, currentVersions, version4, new Long(-1), expectedVersions);

        // move pos 2 to first using -1 (modifies 1 and 4)
        //    <--+   v1
        // v4    |
        // v1 ---+   v4
        // v2      = v2
        // v3        v3
        expectedVersions = Arrays.asList(version1, version4, version2, version3);
        alteredVersions = Arrays.<Version>asList(version1, version4);
        resetAndInitVersionStore();
        currentVersions = assertMoveVersionAfter(alteredVersions, currentVersions, version1, new Long(-1), expectedVersions);

        // move pos 1 to first using -1 (should do nothing)
        //    <--+   v1
        // v1 ---+
        // v4        v4
        // v2      = v2
        // v3        v3
        expectedVersions = Arrays.asList(version1, version4, version2, version3);
        alteredVersions = null;
        resetAndInitVersionStore();
        assertMoveVersionAfter(alteredVersions, currentVersions, version1, new Long(-1), expectedVersions);
    }

    @Test
    public void testMoveVersionAfterOnePosition()
    {
        // assert the intial sequence order (1, 2, 3 and 4)
        assertEquals(new Long(1), version1.getSequence());
        assertEquals(new Long(2), version2.getSequence());
        assertEquals(new Long(3), version3.getSequence());
        assertEquals(new Long(4), version4.getSequence());

        List<Version> currentVersions, expectedVersions, alteredVersions;

        currentVersions = Arrays.asList(version1, version2, version3, version4);

        // move v1 to second
        // v1 ---+   v2
        // v2 <--+ = v1
        // v3        v3
        // v4        v4
        expectedVersions = Arrays.asList(version2, version1, version3, version4);
        alteredVersions = Arrays.<Version>asList(version2, version1);
        currentVersions = assertMoveVersionAfter(alteredVersions, currentVersions, version1, version2.getId(), expectedVersions);

        // move v1 to 3rd position
        // v2        v2
        // v1 ---+ = v3
        // v3 <--+   v1
        // v4        v4
        resetAndInitVersionStore();
        expectedVersions = Arrays.asList(version2, version3, version1, version4);
        alteredVersions = Arrays.<Version>asList(version3, version1);
        currentVersions = assertMoveVersionAfter(alteredVersions, currentVersions, version1, version3.getId(), expectedVersions);

        // move v1 to 4th position
        // v2        v2
        // v3        v3
        // v1 ---+ = v4
        // v4 <--+   v1
        resetAndInitVersionStore();
        expectedVersions = Arrays.asList(version2, version3, version4, version1);
        alteredVersions = Arrays.<Version>asList(version4, version1);
        currentVersions = assertMoveVersionAfter(alteredVersions, currentVersions, version1, version4.getId(), expectedVersions);

        // move v1 to 3rd position
        // v2        v2
        // v3        v3
        // v1 <--+ = v4
        // v4 ---+   v1
        resetAndInitVersionStore();
        expectedVersions = Arrays.asList(version2, version3, version4, version1);
        alteredVersions = null;
        currentVersions = assertMoveVersionAfter(alteredVersions, currentVersions, version1, version4.getId(), expectedVersions);

        // move v1 to 2nd position
        // v2        v2
        // v3 <--+ = v3
        // v4 ---+   v4
        // v1        v1
        resetAndInitVersionStore();
        expectedVersions = Arrays.asList(version2, version3, version4, version1);
        alteredVersions = null;
        currentVersions = assertMoveVersionAfter(alteredVersions, currentVersions, version4, version3.getId(), expectedVersions);

        // move v1 to 2nd position
        // v2 <--+ = v2
        // v3 ---+   v3
        // v4        v4
        // v1        v1
        resetAndInitVersionStore();
        expectedVersions = Arrays.asList(version2, version3, version4, version1);
        alteredVersions = null;
        currentVersions = assertMoveVersionAfter(alteredVersions, currentVersions, version3, version2.getId(), expectedVersions);

        // v2 ---+   v3
        // v3    | = v4
        // v4    |   v1
        // v1 <--+   v2
        resetAndInitVersionStore();
        expectedVersions = Arrays.asList(version3, version4, version1, version2);
        alteredVersions = Arrays.<Version>asList(version3, version4, version1, version2);
        currentVersions = assertMoveVersionAfter(alteredVersions, currentVersions, version2, version1.getId(), expectedVersions);

        // v3        v3
        // v4 ---+ = v1
        // v1    |   v2
        // v2 <--+   v4
        resetAndInitVersionStore();
        expectedVersions = Arrays.asList(version3, version1, version2, version4);
        alteredVersions = Arrays.<Version>asList(version1, version2, version4);
        currentVersions = assertMoveVersionAfter(alteredVersions, currentVersions, version4, version2.getId(), expectedVersions);

        // v3        v3
        // v1 <--+ = v1
        // v2    |   v4
        // v4 ---+   v2
        resetAndInitVersionStore();
        expectedVersions = Arrays.asList(version3, version1, version4, version2);
        alteredVersions = Arrays.<Version>asList(version4, version2);
        currentVersions = assertMoveVersionAfter(alteredVersions, currentVersions, version4, version1.getId(), expectedVersions);

        // v3 ---+ = v1
        // v1    |   v4
        // v4 <--+   v3
        // v2        v2
        resetAndInitVersionStore();
        expectedVersions = Arrays.asList(version1, version4, version3, version2);
        alteredVersions = Arrays.<Version>asList(version1, version3, version4);
        currentVersions = assertMoveVersionAfter(alteredVersions, currentVersions, version3, version4.getId(), expectedVersions);

        // v1 <--+ = v1
        // v4    |   v3
        // v3 ---+   v4
        // v2        v2
        resetAndInitVersionStore();
        expectedVersions = Arrays.asList(version1, version3, version4, version2);
        alteredVersions = Arrays.<Version>asList(version3, version4);
        currentVersions = assertMoveVersionAfter(alteredVersions, currentVersions, version3, version1.getId(), expectedVersions);

        // v1 <--+   v1
        // v3    | = v2
        // v4    |   v3
        // v2 ---+   v4
        resetAndInitVersionStore();
        expectedVersions = Arrays.asList(version1, version2, version3, version4);
        alteredVersions = Arrays.<Version>asList(version2, version3, version4);
        currentVersions = assertMoveVersionAfter(alteredVersions, currentVersions, version2, version1.getId(), expectedVersions);
    }

    // ---- Release Version Tests ----
    @Test
    public void testReleaseVersion() throws GenericEntityException
    {
        versionManager.releaseVersions(Arrays.asList(version1), true);
        assertTrue(version1.isReleased());
    }

    // // ---- Version Archive Tests ----
    @Test
    public void testArchiveVersion() throws Exception
    {
        when(versionStore.getVersion(version1.getId())).thenReturn(version1.getGenericValue());
        final String[] idsToArchive = { version1.getString("id") };
        final String[] idsToUnArchive = { };
        versionManager.archiveVersions(idsToArchive, idsToUnArchive);
        verify(versionStore).storeVersions(Collections.singletonList(version1));
    }

    @Test
    public void testGetUnarchivedVersions() throws Exception
    {
        final List<GenericValue> unarchivedVersions = Arrays.asList(version1.getGenericValue(), version2.getGenericValue(),
                version3.getGenericValue());
        when(versionStore.getVersionsByProject(project.getId())).thenReturn(unarchivedVersions);

        final Collection<Version> unarchived = versionManager.getVersionsUnarchived(project.getId());
        assertEquals(Arrays.asList(version1, version2, version3), unarchived);
    }

    @Test
    public void testGetArchived() throws GenericEntityException
    {
        final List<GenericValue> unarchivedVersions = Arrays.asList(version4.getGenericValue(), version5.getGenericValue(),
                version6.getGenericValue());
        when(versionStore.getVersionsByProject(project.getId())).thenReturn(unarchivedVersions);

        final Collection<Version> archived = versionManager.getVersionsArchived(project);

        assertEquals(Arrays.asList(version4, version5, version6), archived);
    }

    @Test
    public void testGetAllVersions() throws Exception
    {
        final List<GenericValue> allVersionsIn = Arrays.asList(versionOne.getGenericValue(), version1.getGenericValue(),
                version2.getGenericValue(), version3.getGenericValue(), version4.getGenericValue(), version5.getGenericValue(),
                version6.getGenericValue());
        when(versionStore.getAllVersions()).thenReturn(allVersionsIn);

        final Collection<Version> allVersions = versionManager.getAllVersions();
        assertEquals(Arrays.asList(versionOne, version1, version2, version3, version4, version5, version6), allVersions);
    }

    @Test
    public void testGetAllVersionsReleased() throws Exception
    {
        final List<GenericValue> allVersionsIn = Arrays.asList(versionOne.getGenericValue(), version1.getGenericValue(),
                version2.getGenericValue(), version3.getGenericValue(), version4.getGenericValue(), version5.getGenericValue(),
                version6.getGenericValue(), version7.getGenericValue());
        when(versionStore.getAllVersions()).thenReturn(allVersionsIn);

        final Collection<Version> releasedVersions = versionManager.getAllVersionsReleased(true);
        assertEquals(Arrays.asList(version4, version5, version6, version7), releasedVersions);

        final Collection<Version> releasedVersionsNotArchived = versionManager.getAllVersionsReleased(false);
        assertEquals(Arrays.asList(version7), releasedVersionsNotArchived);
    }

    @Test
    public void testGetAllVersionsUnreleased() throws Exception
    {
        final List<GenericValue> allVersionsIn = Arrays.asList(version1.getGenericValue(), version2.getGenericValue(),
                version3.getGenericValue(), version4.getGenericValue(), version5.getGenericValue(), version6.getGenericValue(),
                version8.getGenericValue());
        when(versionStore.getAllVersions()).thenReturn(allVersionsIn);

        final Collection<Version> unreleasedVersions = versionManager.getAllVersionsUnreleased(true);
        assertEquals(Arrays.asList(version1, version2, version3, version8), unreleasedVersions);

        final Collection<Version> unreleasedVersionsNotArchived = versionManager.getAllVersionsUnreleased(false);
        assertEquals(Arrays.asList(version1, version2, version3), unreleasedVersionsNotArchived);
    }

    @Test
    public void testIsVersionOverdue()
    {
        final Calendar tomorrow = Calendar.getInstance();
        tomorrow.setTimeInMillis(System.currentTimeMillis());
        tomorrow.add(Calendar.DATE, 1);

        final Version vTomorrow = new VersionImpl(projectManager, new MockGenericValue("Version", ImmutableMap.builder()
                .put("name", "Version Tomorrow")
                .put("id", Long.valueOf(22222))
                .put("sequence", Long.valueOf(2))
                .put("project", project.getId())
                .put("releasedate", new Timestamp(tomorrow.getTimeInMillis()))
                .put("description", "The description")
                .build()));
        assertFalse("Future day was marked incorrectly overdue", versionManager.isVersionOverDue(vTomorrow));

        final Calendar yesterday = Calendar.getInstance();
        yesterday.setTimeInMillis(System.currentTimeMillis());
        yesterday.add(Calendar.DATE, -1);

        final Version vOverdue = new VersionImpl(projectManager, new MockGenericValue("Version", ImmutableMap.builder()
                .put("name", "Version Yesterday")
                .put("id", Long.valueOf(33333))
                .put("sequence", Long.valueOf(2))
                .put("project", project.getId())
                .put("releasedate", new Timestamp(yesterday.getTimeInMillis()))
                .put("description", "The description")
                .build()));
        assertTrue("Past date was marked as not overdue", versionManager.isVersionOverDue(vOverdue));

        final Version vToday = new VersionImpl(projectManager, new MockGenericValue("Version", ImmutableMap.builder()
                .put("name", "Version Today")
                .put("id", Long.valueOf(11111))
                .put("sequence", Long.valueOf(2))
                .put("project", project.getId())
                .put("releasedate", new Timestamp(System.currentTimeMillis()))
                .put("description", "The description")
                .build()));
        assertFalse("Today was marked incorrectly overdue", versionManager.isVersionOverDue(vToday));
    }

    @Test
    public void testSwapWithNoAffectedIssues()
    {
        when(nodeAssociationStore.getSourceIdsFromSink(NodeAssocationType.ISSUE_TO_AFFECTS_VERISON, version1.getId())).thenReturn(Collections.<Long>emptyList());
        when(nodeAssociationStore.getSourceIdsFromSink(NodeAssocationType.ISSUE_TO_FIX_VERISON, version1.getId())).thenReturn(Collections.<Long>emptyList());

        versionManager.swapVersionForRelatedIssues(mockUser, version1, Option.option(version2), Option.<Version>none());

        verifyZeroInteractions(issueManager, issueIndexManager, eventPublisher);
    }

    @Test
    public void testSwapVersionRemoveAffectedIssues() throws IndexException
    {
        MockIssue mockIssue = new MockIssue(10034L);
        mockIssue.setAffectedVersions(newHashSet());
        mockIssue.setFixVersions(newHashSet(version1, version2));

        when(nodeAssociationStore.getSourceIdsFromSink(NodeAssocationType.ISSUE_TO_AFFECTS_VERISON, version1.getId())).thenReturn(Collections.<Long>emptyList());
        when(nodeAssociationStore.getSourceIdsFromSink(NodeAssocationType.ISSUE_TO_FIX_VERISON, version1.getId())).thenReturn(Lists.newArrayList(10034L));

        when(issueManager.getIssueObject(10034L)).thenReturn(mockIssue);
        when(issueManager.updateIssue(eq(mockUser), eq(mockIssue), eq(updateIssueRequest))).thenReturn(mockIssue);

        //lets simulate swapping fix version with nothing (i.e. simply remove the fix version that matches).
        versionManager.swapVersionForRelatedIssues(mockUser, version1, Option.option(version2), Option.<Version>none());

        assertTrue(mockIssue.getAffectedVersions().isEmpty());
        assertEquals(newHashSet(version2), mockIssue.getFixVersions());

        verify(issueManager).updateIssue(eq(mockUser), eq(mockIssue), eq(updateIssueRequest));
        verify(issueIndexManager).reIndex(eq(mockIssue));
        verify(eventPublisher).publish(Mockito.any(VersionMergeEvent.class));
    }

    @Test
    public void testSwapVersionsWithNoExistingVersionOnIssue() throws IndexException
    {
        MockIssue mockIssue = new MockIssue(10034L);
        mockIssue.setAffectedVersions(newHashSet());
        mockIssue.setFixVersions(newHashSet());

        when(nodeAssociationStore.getSourceIdsFromSink(NodeAssocationType.ISSUE_TO_AFFECTS_VERISON, version1.getId())).thenReturn(Collections.<Long>emptyList());
        when(nodeAssociationStore.getSourceIdsFromSink(NodeAssocationType.ISSUE_TO_FIX_VERISON, version1.getId())).thenReturn(Lists.newArrayList(10034L));

        when(issueManager.getIssueObject(10034L)).thenReturn(mockIssue);
        when(issueManager.updateIssue(eq(mockUser), eq(mockIssue), eq(updateIssueRequest))).thenReturn(mockIssue);

        //lets simulate swapping fix version.  Nothing should happen since the issue currently has no fix versions set.
        versionManager.swapVersionForRelatedIssues(mockUser, version1, Option.<Version>none(), Option.option(version3));

        assertTrue(mockIssue.getAffectedVersions().isEmpty());
        assertTrue(mockIssue.getFixVersions().isEmpty());

        verify(issueManager).updateIssue(eq(mockUser), eq(mockIssue), eq(updateIssueRequest));
        verify(issueIndexManager).reIndex(eq(mockIssue));
        verify(eventPublisher).publish(Mockito.any(VersionMergeEvent.class));
    }

    @Test
    public void testSwapVersionSwapAffectedIssues() throws IndexException
    {
        MockIssue mockIssue = new MockIssue(10034L);
        mockIssue.setAffectedVersions(newHashSet(version4, version1));
        mockIssue.setFixVersions(newHashSet(version1, version5, version4));

        when(nodeAssociationStore.getSourceIdsFromSink(NodeAssocationType.ISSUE_TO_AFFECTS_VERISON, version1.getId())).thenReturn(Lists.newArrayList(10034L));
        when(nodeAssociationStore.getSourceIdsFromSink(NodeAssocationType.ISSUE_TO_FIX_VERISON, version1.getId())).thenReturn(Lists.newArrayList(10034L));

        when(issueManager.getIssueObject(10034L)).thenReturn(mockIssue);
        when(issueManager.updateIssue(eq(mockUser), eq(mockIssue), eq(updateIssueRequest))).thenReturn(mockIssue);

        //lets simulate swapping fix version.  Nothing should happen since the issue currently has no fix versions set.
        versionManager.swapVersionForRelatedIssues(mockUser, version1, Option.option(version7), Option.option(version3));

        assertEquals(newHashSet(version4, version7), mockIssue.getAffectedVersions());
        assertEquals(newHashSet(version3, version5, version4), mockIssue.getFixVersions());

        verify(issueManager).updateIssue(eq(mockUser), eq(mockIssue), eq(updateIssueRequest));
        verify(issueIndexManager).reIndex(eq(mockIssue));
        verify(eventPublisher).publish(Mockito.any(VersionMergeEvent.class));
    }

    @Test
    public void testSwapVersionMultipleAffectedIssues() throws IndexException
    {
        MockIssue mockIssue = new MockIssue(10034L);
        mockIssue.setAffectedVersions(newHashSet(version4, version1));
        mockIssue.setFixVersions(newHashSet(version1, version5, version4));

        MockIssue mockIssue2 = new MockIssue(2000L);
        mockIssue2.setAffectedVersions(newHashSet(version7, version1, version3));
        mockIssue2.setFixVersions(newHashSet());

        when(nodeAssociationStore.getSourceIdsFromSink(NodeAssocationType.ISSUE_TO_AFFECTS_VERISON, version1.getId())).thenReturn(Lists.newArrayList(2000L));
        when(nodeAssociationStore.getSourceIdsFromSink(NodeAssocationType.ISSUE_TO_FIX_VERISON, version1.getId())).thenReturn(Lists.newArrayList(10034L));

        when(issueManager.getIssueObject(10034L)).thenReturn(mockIssue);
        when(issueManager.updateIssue(eq(mockUser), eq(mockIssue), eq(updateIssueRequest))).thenReturn(mockIssue);
        when(issueManager.getIssueObject(2000L)).thenReturn(mockIssue2);
        when(issueManager.updateIssue(eq(mockUser), eq(mockIssue2), eq(updateIssueRequest))).thenReturn(mockIssue2);

        //lets simulate swapping fix version.  Nothing should happen since the issue currently has no fix versions set.
        versionManager.swapVersionForRelatedIssues(mockUser, version1, Option.option(version2), Option.option(version2));

        assertEquals(newHashSet(version4, version2), mockIssue.getAffectedVersions());
        assertEquals(newHashSet(version2, version5, version4), mockIssue.getFixVersions());

        assertEquals(newHashSet(version7, version2, version3), mockIssue2.getAffectedVersions());
        assertEquals(newHashSet(), mockIssue2.getFixVersions());

        verify(issueManager).updateIssue(eq(mockUser), eq(mockIssue), eq(updateIssueRequest));
        verify(issueManager).updateIssue(eq(mockUser), eq(mockIssue2), eq(updateIssueRequest));
        verify(issueIndexManager).reIndex(eq(mockIssue));
        verify(issueIndexManager).reIndex(eq(mockIssue2));

        //event should only be published once, since only one issue had fix versions replaced.
        verify(eventPublisher, times(1)).publish(Mockito.any(VersionMergeEvent.class));
    }


    private List<Version> assertMoveVersionAfter(final List<Version> expectedStoredVersions, final List<Version> currentVersions,
            final Version toMove, final Long scheduleAfterVersionId, final List<Version> expectedVersions)
    {
        when(versionStore.getVersionsByProject(project.getId())).thenReturn(
                Lists.transform(currentVersions, new Function<Version, GenericValue>()
                {

                    @Override
                    public GenericValue apply(final Version input)
                    {
                        return input.getGenericValue();
                    }

                }));

        versionManager.moveVersionAfter(toMove, scheduleAfterVersionId);

        assertSequenceOrder(expectedVersions);
        assertStoredVersions(expectedStoredVersions);

        return expectedVersions;
    }

    private void assertSequenceOrder(final List<Version> versions)
    {
        final List<Long> expectedSequence = new LinkedList<Long>();
        final List<Long> actualSequence = new LinkedList<Long>();

        for (int i = 0; i < versions.size(); i++)
        {
            expectedSequence.add(Long.valueOf(i + 1));
            actualSequence.add(versions.get(i).getSequence());
        }

        assertEquals(expectedSequence, actualSequence);
    }

    private void assertStoredVersions(final List<Version> versions)
    {
        verify(versionStore, Mockito.never()).storeVersion(Mockito.<Version>any());
        if (versions == null)
        {
            verify(versionStore, Mockito.never()).storeVersions(Mockito.argThat(new ArgumentMatcher<Collection<Version>>()
            {

                @Override
                public boolean matches(final Object argument)
                {
                    @SuppressWarnings ("unchecked")
                    final Collection<Version> versions = (Collection<Version>) argument;
                    return !versions.isEmpty();
                }

            }));
        }
        else
        {
            verify(versionStore, Mockito.atLeastOnce()).storeVersions(Mockito.argThat(new ArgumentMatcher<List<Version>>()
            {

                @SuppressWarnings ("unchecked")
                @Override
                public boolean matches(final Object argument)
                {
                    final List<Version> versionsArgument = (List<Version>) argument;
                    return versions.containsAll(versionsArgument) && versions.size() == versionsArgument.size();
                }

                @Override
                public void describeTo(final Description description)
                {
                    description.appendValue(versions).appendText(" in any order");
                }

            }));
        }
    }

    private void resetAndInitVersionStore()
    {
        reset(versionStore);
        when(versionStore.createVersion(Mockito.<Map<String, Object>>any())).thenAnswer(new Answer<GenericValue>()
        {

            @SuppressWarnings ("unchecked")
            @Override
            public GenericValue answer(final InvocationOnMock invocation) throws Throwable
            {
                return new MockGenericValue(OfBizDelegator.VERSION, (Map<String, Object>) invocation.getArguments()[0]);
            }

        });
        whenGetVersionByVersionId(versionOne, version1, version2, version3, version4, version5, version6, version7, version8);
    }

    private void whenGetVersionByVersionId(final Version... versions)
    {
        for (final Version version : versions)
        {
            when(versionStore.getVersion(version.getId())).thenReturn(version.getGenericValue());
        }
    }

}
