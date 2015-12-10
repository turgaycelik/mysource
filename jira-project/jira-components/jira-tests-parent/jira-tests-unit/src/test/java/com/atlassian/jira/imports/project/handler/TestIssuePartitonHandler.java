package com.atlassian.jira.imports.project.handler;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.core.BackupProject;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.model.MockModelEntity;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v3.13
 */
public class TestIssuePartitonHandler
{
    private static final String NL = System.getProperty("line.separator");

    @Mock private DelegatorInterface mockDelegatorInterface;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockDelegatorInterface.getDelegatorName()).thenReturn("default");
    }

    @Test
    public void testPartionFile() throws ParseException
    {
        final MockModelEntity mockModelEntity = new MockModelEntity("Issue");
        mockModelEntity.setFieldNames(asList("id", "key", "desc"));

        final StringWriter writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);

        final BackupProject mockBackupProject = mock(BackupProject.class);
        when(mockBackupProject.containsIssue("10")).thenReturn(false);
        when(mockBackupProject.containsIssue("12")).thenReturn(true);
        when(mockBackupProject.containsIssue("14")).thenReturn(true);
        when(mockBackupProject.containsIssue("16")).thenReturn(false);
        
        final IssuePartitonHandler issuePartitonHandler = new IssuePartitonHandler(
                mockBackupProject, printWriter, mockModelEntity, "UTF-8", mockDelegatorInterface);

        // Invoke
        issuePartitonHandler.startDocument();
        issuePartitonHandler.handleEntity("Issue", ImmutableMap.of("id", "10", "key", "HSP-1", "desc", "Sheet happened."));
        issuePartitonHandler.handleEntity("Issue", ImmutableMap.of("id", "12", "key", "MNK-16", "desc", "Stuff happened."));
        issuePartitonHandler.handleEntity("Issue", ImmutableMap.of(
                "id", "14", "key", "MNK-19", "desc", "Some really bad gear happened." + NL + "Yes it did."));
        issuePartitonHandler.handleEntity("Issue", ImmutableMap.of("id", "16", "key", "HSP-13", "desc", "More stuff happened."));
        issuePartitonHandler.handleEntity("NotIssue", ImmutableMap.of("id", "16", "key", "HSP-13", "desc", "More stuff happened."));
        issuePartitonHandler.endDocument();
        printWriter.close();

        // Check
        String xml = writer.toString();
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
                     + "<entity-engine-xml>" + NL
                     + "    <Issue id=\"12\" key=\"MNK-16\" desc=\"Stuff happened.\"/>" + NL
                     + "    <Issue id=\"14\" key=\"MNK-19\">" + NL
                     + "        <desc><![CDATA[Some really bad gear happened." + NL
                     + "Yes it did.]]></desc>" + NL
                     + "    </Issue>" + NL
                     + "</entity-engine-xml>", xml);

        assertEquals(2, issuePartitonHandler.getEntityCount());
    }
}
