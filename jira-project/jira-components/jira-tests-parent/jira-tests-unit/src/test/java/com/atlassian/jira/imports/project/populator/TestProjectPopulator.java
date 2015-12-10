package com.atlassian.jira.imports.project.populator;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilder;
import com.atlassian.jira.imports.project.parser.ProjectParser;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Tests ProjectPopulator
 *
 * @since v3.13
 */
public class TestProjectPopulator
{
    @Test
    public void testProjectElement() throws ParseException
    {
        final Map attributes = EasyMap.build();

        final Mock mockProjectParser = new Mock(ProjectParser.class);
        mockProjectParser.setStrict(true);
        ExternalProject externalProject = new ExternalProject();
        mockProjectParser.expectAndReturn("parseProject", P.ANY_ARGS, externalProject);
        ProjectPopulator projectPopulator = new ProjectPopulator()
        {
            ProjectParser getProjectParser()
            {
                return (ProjectParser) mockProjectParser.proxy();
            }
        };

        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);
        mockBackupOverviewBuilder.setStrict(true);
        mockBackupOverviewBuilder.expectVoid("addProject", P.args(P.eq(externalProject)));

        projectPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "Project", attributes);

        mockBackupOverviewBuilder.verify();
        mockProjectParser.verify();
    }

    @Test
    public void testProjectElementNotParsable()
    {
        final Mock mockProjectParser = new Mock(ProjectParser.class);
        mockProjectParser.setStrict(true);
        mockProjectParser.expectAndThrow("parseProject", P.ANY_ARGS, new ParseException("Hello world"));
        ProjectPopulator projectPopulator = new ProjectPopulator()
        {
            ProjectParser getProjectParser()
            {
                return (ProjectParser) mockProjectParser.proxy();
            }
        };
        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);

        try
        {
            projectPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "Project", EasyMap.build());
            fail();
        }
        catch (ParseException e)
        {
            // expected
        }
        mockProjectParser.verify();
    }

    @Test
    public void testNonProjectElement() throws ParseException
    {
        ProjectPopulator projectPopulator = new ProjectPopulator();
        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);
        mockBackupOverviewBuilder.expectNotCalled("addProject");

        projectPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "NotProject", EasyMap.build("id", "10", "key", "MNK"));
        mockBackupOverviewBuilder.verify();
    }
}
