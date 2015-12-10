package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalLink;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.mock.issue.MockIssue;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v3.13
 */
public class TestIssueLinkTransformerImpl
{
    /**
     * In this test, the source issue id does not have an Issue key in the backup file - this would be invalid data,
     * but we want to safely ignore orphaned data like this.
     */
    @Test
    public void testTransformOrphanedLink()
    {
        // Mock BackupSystemInformation
        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("101");
        mockBackupSystemInformationControl.setReturnValue(null);
        mockBackupSystemInformationControl.replay();

        // Mock IssueManager
        final MockControl mockIssueManagerControl = MockControl.createStrictControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
//        mockIssueManager.();
//        mockIssueManagerControl.setReturnValue(null);
        mockIssueManagerControl.replay();

        IssueLinkTransformerImpl issueLinkTransformer = new IssueLinkTransformerImpl(mockIssueManager, mockBackupSystemInformation);
        final ExternalLink oldIssueLink = new ExternalLink();
        oldIssueLink.setLinkType("12");
        oldIssueLink.setSourceId("101");
        oldIssueLink.setDestinationId("201");
        oldIssueLink.setSequence("3");

        ExternalLink newIssueLink = issueLinkTransformer.transform(new ProjectImportMapperImpl(null, null), oldIssueLink);
        assertNull(newIssueLink);

        // Verify Mock BackupSystemInformation
        mockBackupSystemInformationControl.verify();
        // Verify Mock IssueManager
        mockIssueManagerControl.verify();
    }

    /**
     * In this test, the source issue key is not found in the current JIRA.
     * The link cannot be created.
     */
    @Test
    public void testTransformSourceNotInCurrentSystem()
    {
        // Mock BackupSystemInformation
        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("101");
        mockBackupSystemInformationControl.setReturnValue("RAT-1");
        mockBackupSystemInformationControl.replay();

        // Mock IssueManager
        final MockControl mockIssueManagerControl = MockControl.createStrictControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManager.getIssueObject("RAT-1");
        mockIssueManagerControl.setReturnValue(null);
        mockIssueManagerControl.replay();

        IssueLinkTransformerImpl issueLinkTransformer = new IssueLinkTransformerImpl(mockIssueManager, mockBackupSystemInformation);
        final ExternalLink oldIssueLink = new ExternalLink();
        oldIssueLink.setLinkType("12");
        oldIssueLink.setSourceId("101");
        oldIssueLink.setDestinationId("201");
        oldIssueLink.setSequence("3");

        ExternalLink newIssueLink = issueLinkTransformer.transform(new ProjectImportMapperImpl(null, null), oldIssueLink);
        assertNull(newIssueLink);

        // Verify Mock BackupSystemInformation
        mockBackupSystemInformationControl.verify();
        // Verify Mock IssueManager
        mockIssueManagerControl.verify();
    }

    /**
     * In this test, the Destination issue key is not found in the current JIRA.
     * The link cannot be created.
     */
    @Test
    public void testTransformDestinationNotInCurrentSystem()
    {
        // Mock BackupSystemInformation
        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("201");
        mockBackupSystemInformationControl.setReturnValue("COW-1");
        mockBackupSystemInformationControl.replay();

        // Mock IssueManager
        final MockControl mockIssueManagerControl = MockControl.createStrictControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManager.getIssueObject("COW-1");
        mockIssueManagerControl.setReturnValue(null);
        mockIssueManagerControl.replay();

        IssueLinkTransformerImpl issueLinkTransformer = new IssueLinkTransformerImpl(mockIssueManager, mockBackupSystemInformation);
        final ExternalLink oldIssueLink = new ExternalLink();
        oldIssueLink.setLinkType("12");
        oldIssueLink.setSourceId("101");
        oldIssueLink.setDestinationId("201");
        oldIssueLink.setSequence("3");

        ProjectImportMapperImpl projectImportMapper = new ProjectImportMapperImpl(null, null);
        // Let the source belong to imported Project - therefore it is mapped in the Issue Mapper.
        projectImportMapper.getIssueMapper().mapValue("101", "56");
        ExternalLink newIssueLink = issueLinkTransformer.transform(projectImportMapper, oldIssueLink);
        assertNull(newIssueLink);

        // Verify Mock BackupSystemInformation
        mockBackupSystemInformationControl.verify();
        // Verify Mock IssueManager
        mockIssueManagerControl.verify();
    }

    @Test
    public void testTransformDestinationHappyPath()
    {
        // Mock BackupSystemInformation
        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("201");
        mockBackupSystemInformationControl.setReturnValue("COW-1");
        mockBackupSystemInformationControl.replay();

        // Mock IssueManager
        final MockControl mockIssueManagerControl = MockControl.createStrictControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManager.getIssueObject("COW-1");
        mockIssueManagerControl.setReturnValue(new MockIssue(67));
        mockIssueManagerControl.replay();

        // oldIssueLink
        final ExternalLink oldIssueLink = new ExternalLink();
        oldIssueLink.setLinkType("12");
        oldIssueLink.setSourceId("101");
        oldIssueLink.setDestinationId("201");
        oldIssueLink.setSequence("3");

        // ProjectImportMapper
        ProjectImportMapperImpl projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getIssueLinkTypeMapper().mapValue("12", "34");
        // Let the source belong to imported Project - therefore it is mapped in the Issue Mapper.
        projectImportMapper.getIssueMapper().mapValue("101", "56");

        // Finally do the transform
        IssueLinkTransformerImpl issueLinkTransformer = new IssueLinkTransformerImpl(mockIssueManager, mockBackupSystemInformation);
        ExternalLink newIssueLink = issueLinkTransformer.transform(projectImportMapper, oldIssueLink);
        assertEquals("34", newIssueLink.getLinkType());
        assertEquals("56", newIssueLink.getSourceId());
        assertEquals("67", newIssueLink.getDestinationId());
        assertEquals("3", newIssueLink.getSequence());

        // Verify Mock BackupSystemInformation
        mockBackupSystemInformationControl.verify();
        // Verify Mock IssueManager
        mockIssueManagerControl.verify();
    }
}
