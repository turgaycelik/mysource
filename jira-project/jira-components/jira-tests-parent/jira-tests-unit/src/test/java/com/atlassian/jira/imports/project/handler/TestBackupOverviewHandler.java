package com.atlassian.jira.imports.project.handler;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.core.BackupOverview;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilder;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @since v3.13
 */
public class TestBackupOverviewHandler
{
    @Test
    public void testHandlerDelegatesToBuilder() throws ParseException
    {
        final Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);
        mockBackupOverviewBuilder.setStrict(true);
        mockBackupOverviewBuilder.expectVoid("populateInformationFromElement", P.args(P.eq("TestEntity"), P.eq(Collections.EMPTY_MAP)));
        BackupOverviewHandler backupOverviewHandler = new BackupOverviewHandler()
        {
            BackupOverviewBuilder getBackupOverviewBuilder()
            {
                return (BackupOverviewBuilder) mockBackupOverviewBuilder.proxy();
            }
        };

        backupOverviewHandler.handleEntity("TestEntity", Collections.EMPTY_MAP);

        mockBackupOverviewBuilder.verify();
    }

    @Test
    public void testRealData() throws ParserConfigurationException, SAXException, IOException
    {
        ChainedSaxHandler chainedSaxHandler = new ChainedSaxHandler();
        BackupOverviewHandler backupOverviewHandler = new BackupOverviewHandler();
        chainedSaxHandler.registerHandler(backupOverviewHandler);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        InputSource inputSource = new InputSource(new StringReader(TestChainedSaxHandler.XML));
        // Scan the XML
        saxParser.parse(inputSource, chainedSaxHandler);

        BackupOverview backupOverview = backupOverviewHandler.getBackupOverview();

        assertEquals(1, backupOverview.getProjects().size());
        assertNotNull(backupOverview.getProject("MKY"));
        assertEquals(1, backupOverview.getProject("MKY").getIssueIds().size());
    }
}
