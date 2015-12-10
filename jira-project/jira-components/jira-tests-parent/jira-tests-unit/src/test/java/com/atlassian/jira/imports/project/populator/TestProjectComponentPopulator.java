package com.atlassian.jira.imports.project.populator;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalComponent;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilder;
import com.atlassian.jira.imports.project.parser.ProjectComponentParser;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestProjectComponentPopulator
{
    @Test
    public void testProjectElement() throws ParseException
    {
        final Map attributes = EasyMap.build();

        final Mock mockProjectComponentParser = new Mock(ProjectComponentParser.class);
        mockProjectComponentParser.setStrict(true);
        ExternalComponent externalComponent = new ExternalComponent();
        mockProjectComponentParser.expectAndReturn("parse", P.ANY_ARGS, externalComponent);
        ProjectComponentPopulator projectComponentPopulator = new ProjectComponentPopulator()
        {

            ProjectComponentParser getProjectComponentParser()
            {
                return (ProjectComponentParser) mockProjectComponentParser.proxy();
            }
        };

        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);
        mockBackupOverviewBuilder.setStrict(true);
        mockBackupOverviewBuilder.expectVoid("addComponent", P.args(P.eq(externalComponent)));

        projectComponentPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "Component", attributes);

        mockBackupOverviewBuilder.verify();
        mockProjectComponentParser.verify();
    }

    @Test
    public void testProjectElementNotParsable()
    {
        final Mock mockProjectComponentParser = new Mock(ProjectComponentParser.class);
        mockProjectComponentParser.setStrict(true);
        mockProjectComponentParser.expectAndThrow("parse", P.ANY_ARGS, new ParseException("Hello world"));
        ProjectComponentPopulator projectComponentPopulator = new ProjectComponentPopulator()
        {
            ProjectComponentParser getProjectComponentParser()
            {
                return (ProjectComponentParser) mockProjectComponentParser.proxy();
            }
        };
        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);

        try
        {
            projectComponentPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "Component", EasyMap.build());
            fail();
        }
        catch (ParseException e)
        {
            // expected
        }
        mockProjectComponentParser.verify();
    }

    @Test
    public void testNonProjectElement() throws ParseException
    {
        ProjectComponentPopulator projectComponentPopulator = new ProjectComponentPopulator();

        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);
        mockBackupOverviewBuilder.expectNotCalled("addComponent");

        projectComponentPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "NotComponent", EasyMap.build());
        mockBackupOverviewBuilder.verify();
    }

}
