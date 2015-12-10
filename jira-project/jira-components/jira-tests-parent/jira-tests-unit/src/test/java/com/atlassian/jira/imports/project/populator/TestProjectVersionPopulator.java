package com.atlassian.jira.imports.project.populator;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalVersion;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilder;
import com.atlassian.jira.imports.project.parser.ProjectVersionParser;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestProjectVersionPopulator
{

    @Test
    public void testProjectElement() throws ParseException
    {
        final Map attributes = EasyMap.build();

        final Mock mockProjectVersionParser = new Mock(ProjectVersionParser.class);
        mockProjectVersionParser.setStrict(true);
        ExternalVersion externalVersion = new ExternalVersion();
        mockProjectVersionParser.expectAndReturn("parse", P.ANY_ARGS, externalVersion);
        ProjectVersionPopulator projectVersionPopulator = new ProjectVersionPopulator()
        {

            ProjectVersionParser getProjectVersionParser()
            {
                return (ProjectVersionParser) mockProjectVersionParser.proxy();
            }
        };

        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);
        mockBackupOverviewBuilder.setStrict(true);
        mockBackupOverviewBuilder.expectVoid("addVersion", P.args(P.eq(externalVersion)));

        projectVersionPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "Version", attributes);

        mockBackupOverviewBuilder.verify();
        mockProjectVersionParser.verify();
    }

    @Test
    public void testProjectElementNotParsable()
    {
        final Mock mockProjectVersionParser = new Mock(ProjectVersionParser.class);
        mockProjectVersionParser.setStrict(true);
        mockProjectVersionParser.expectAndThrow("parse", P.ANY_ARGS, new ParseException("Hello world"));
        ProjectVersionPopulator projectVersionPopulator = new ProjectVersionPopulator()
        {
            ProjectVersionParser getProjectVersionParser()
            {
                return (ProjectVersionParser) mockProjectVersionParser.proxy();
            }
        };
        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);

        try
        {
            projectVersionPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "Version", EasyMap.build());
            fail();
        }
        catch (ParseException e)
        {
            // expected
        }
        mockProjectVersionParser.verify();
    }

    @Test
    public void testNonProjectElement() throws ParseException
    {
        ProjectVersionPopulator projectVersionPopulator = new ProjectVersionPopulator();

        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);
        mockBackupOverviewBuilder.expectNotCalled("addVersion");

        projectVersionPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "NotVersion", EasyMap.build());
        mockBackupOverviewBuilder.verify();
    }


}
