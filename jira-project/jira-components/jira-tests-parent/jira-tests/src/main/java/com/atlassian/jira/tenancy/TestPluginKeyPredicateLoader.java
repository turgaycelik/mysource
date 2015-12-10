package com.atlassian.jira.tenancy;

import com.atlassian.core.util.FileUtils;
import com.atlassian.jira.tenancy.PluginKeyPredicateLoader;
import com.atlassian.plugin.Plugin;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 *
 *
 * @since v6.3
 */
@RunWith (MockitoJUnitRunner.class)
public class TestPluginKeyPredicateLoader
{
    @Mock
    Plugin mockPlugin;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void setupMocks()
    {
        when(mockPlugin.getKey()).thenReturn("plugin.key");
    }

    @Test
    public void returnsBlankPredicateIfFileDoesNotExist()
    {
        PluginKeyPredicateLoader loader = new PluginKeyPredicateLoader();
        assertThat(loader.getPluginKeyPatternsPredicate().matches(mockPlugin), equalTo(true));
    }

    @Test
    public void predicatesFilteredSuccessfully() throws Exception
    {
        File patternFile = testFolder.newFile("patterns.txt");
        FileUtils.saveTextFile("plugin.key", patternFile);
        PluginKeyPredicateLoader loader = new PluginKeyPredicateLoader(patternFile.getPath());
        assertThat(loader.getPluginKeyPatternsPredicate().matches(mockPlugin), equalTo(true));
    }



}
