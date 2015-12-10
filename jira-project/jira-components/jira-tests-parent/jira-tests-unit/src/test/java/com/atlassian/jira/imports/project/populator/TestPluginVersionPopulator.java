package com.atlassian.jira.imports.project.populator;

import java.util.Date;
import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilder;
import com.atlassian.jira.imports.project.parser.PluginVersionParser;
import com.atlassian.jira.plugin.PluginVersion;
import com.atlassian.jira.plugin.PluginVersionImpl;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestPluginVersionPopulator
{
    @Test
    public void testProjectElement() throws ParseException
    {
        final Map attributes = EasyMap.build();

        final Mock mockPluginVersionParser = new Mock(PluginVersionParser.class);
        mockPluginVersionParser.setStrict(true);
        PluginVersion pluginVersion = new PluginVersionImpl("key", "name", "version", new Date());
        mockPluginVersionParser.expectAndReturn("parse", P.ANY_ARGS, pluginVersion);
        PluginVersionPopulator pluginVersionPopulator = new PluginVersionPopulator()
        {

            PluginVersionParser getPluginVersionParser()
            {
                return (PluginVersionParser) mockPluginVersionParser.proxy();
            }
        };

        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);
        mockBackupOverviewBuilder.setStrict(true);
        mockBackupOverviewBuilder.expectVoid("addPluginVersion", P.args(P.eq(pluginVersion)));

        pluginVersionPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "PluginVersion", attributes);

        mockBackupOverviewBuilder.verify();
        mockPluginVersionParser.verify();
    }

    @Test
    public void testProjectElementNotParsable()
    {
        final Mock mockPluginVersionParser = new Mock(PluginVersionParser.class);
        mockPluginVersionParser.setStrict(true);
        mockPluginVersionParser.expectAndThrow("parse", P.ANY_ARGS, new ParseException("Hello world"));
        PluginVersionPopulator pluginVersionPopulator = new PluginVersionPopulator()
        {
            PluginVersionParser getPluginVersionParser()
            {
                return (PluginVersionParser) mockPluginVersionParser.proxy();
            }
        };
        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);

        try
        {
            pluginVersionPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "PluginVersion", EasyMap.build());
            fail();
        }
        catch (ParseException e)
        {
            // expected
        }
        mockPluginVersionParser.verify();
    }

    @Test
    public void testNonProjectElement() throws ParseException
    {
        PluginVersionPopulator pluginVersionPopulator = new PluginVersionPopulator();

        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);
        mockBackupOverviewBuilder.expectNotCalled("addPluginVersion");

        pluginVersionPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "NotPluginVersion", EasyMap.build());
        mockBackupOverviewBuilder.verify();
    }

}
