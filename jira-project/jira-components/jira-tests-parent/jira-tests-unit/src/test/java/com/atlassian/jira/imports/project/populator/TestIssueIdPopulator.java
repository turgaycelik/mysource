package com.atlassian.jira.imports.project.populator;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalIssue;
import com.atlassian.jira.external.beans.ExternalIssueImpl;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilder;
import com.atlassian.jira.imports.project.parser.IssueParser;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestIssueIdPopulator
{
    @Test
    public void testIssueidElement() throws ParseException
    {
        final Map attributes = EasyMap.build();

        final Mock mockIssueParser = new Mock(IssueParser.class);
        mockIssueParser.setStrict(true);
        ExternalIssue externalIssue = new ExternalIssueImpl(null);
        externalIssue.setId("12345");
        externalIssue.setProject("54321");
        mockIssueParser.expectAndReturn("parse", P.ANY_ARGS, externalIssue);
        IssueIdPopulator issueidPopulator = new IssueIdPopulator()
        {
            IssueParser getIssueParser()
            {
                return (IssueParser) mockIssueParser.proxy();
            }
        };

        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);
        mockBackupOverviewBuilder.setStrict(true);
        mockBackupOverviewBuilder.expectVoid("addIssue", P.args(P.eq(externalIssue)));

        issueidPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "Issue", attributes);

        mockBackupOverviewBuilder.verify();
        mockIssueParser.verify();
    }

    @Test
    public void testIssueidElementNotParsable()
    {
        final Mock mockIssueParser = new Mock(IssueParser.class);
        mockIssueParser.setStrict(true);
        mockIssueParser.expectAndThrow("parse", P.ANY_ARGS, new ParseException("Hello world"));
        IssueIdPopulator issueidPopulator = new IssueIdPopulator()
        {
            IssueParser getIssueParser()
            {
                return (IssueParser) mockIssueParser.proxy();
            }
        };
        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);

        try
        {
            issueidPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "Issue", EasyMap.build());
            fail();
        }
        catch (ParseException e)
        {
            // expected
        }
        mockIssueParser.verify();
    }

    @Test
    public void testNonIssueidElement() throws ParseException
    {
        IssueIdPopulator issueidPopulator = new IssueIdPopulator();
        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);
        mockBackupOverviewBuilder.expectNotCalled("addIssue");

        issueidPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "NotIssue", EasyMap.build("id", "10", "key", "MNK"));
        mockBackupOverviewBuilder.verify();
    }

}
