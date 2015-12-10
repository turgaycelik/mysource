package com.atlassian.jira.plugin.util;

import com.atlassian.jira.mock.plugin.MockPlugin;
import com.atlassian.jira.mock.plugin.elements.MockResourceDescriptorBuilder;
import com.atlassian.jira.plugin.language.LanguageModuleDescriptor;
import com.atlassian.jira.plugin.language.LanguageModuleDescriptorImpl;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.module.ModuleFactory;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 */
public class SimplePluginsTrackerTest
{
    private MockPlugin pluginA;
    private MockPlugin pluginB;

    @Before
    public void setUp() throws Exception
    {
        PluginInformation pluginInformationA = new PluginInformation();
        pluginInformationA.setVersion("1.0");
        PluginInformation pluginInformationB = new PluginInformation();
        pluginInformationB.setVersion("1.0");
        pluginA = new MockPlugin("nameA", "keyA", pluginInformationA);
        pluginB = new MockPlugin("nameB", "keyB", pluginInformationB);
    }

    @Test
    public void testTrackInvolvedPlugin() throws Exception
    {
        SimplePluginsTracker tracker = new SimplePluginsTracker();


        assertEquals(0, tracker.getInvolvedPluginKeys().size());

        tracker.trackInvolvedPlugin(pluginA);

        assertEquals(1, tracker.getInvolvedPluginKeys().size());
        assertTrue(tracker.isPluginInvolved(pluginA));
        assertFalse(tracker.isPluginInvolved(pluginB));

        tracker.trackInvolvedPlugin(pluginB);

        assertEquals(2, tracker.getInvolvedPluginKeys().size());
        assertTrue(tracker.isPluginInvolved(pluginA));
        assertTrue(tracker.isPluginInvolved(pluginB));

        tracker.trackInvolvedPlugin(pluginA); // its a set

        assertEquals(2, tracker.getInvolvedPluginKeys().size());
        assertTrue(tracker.isPluginInvolved(pluginA));
        assertTrue(tracker.isPluginInvolved(pluginB));

        tracker.clear();
        assertEquals(0, tracker.getInvolvedPluginKeys().size());
        assertFalse(tracker.isPluginInvolved(pluginA));
        assertFalse(tracker.isPluginInvolved(pluginB));
    }

    @Test
    public void testContainsModuleDescriptor()
    {
        pluginA.addModuleDescriptor(new LanguageModuleDescriptorImpl(null, ModuleFactory.LEGACY_MODULE_FACTORY));

        SimplePluginsTracker tracker = new SimplePluginsTracker();

        assertEquals(true, tracker.isPluginWithModuleDescriptor(pluginA, LanguageModuleDescriptor.class));
        assertEquals(false, tracker.isPluginWithModuleDescriptor(pluginB, LanguageModuleDescriptor.class));
    }

    @Test
    public void testContainsResourceDescriptors()
    {
        pluginA.addResourceDescriptor(MockResourceDescriptorBuilder.i18n("name", "location"));

        SimplePluginsTracker tracker = new SimplePluginsTracker();

        assertEquals(true, tracker.isPluginWithResourceType(pluginA, "i18n"));
        assertEquals(false, tracker.isPluginWithResourceType(pluginB, "i18n"));

    }
    @Test
    public void testHashCodeStability()
    {
        SimplePluginsTracker tracker = new SimplePluginsTracker();
        String emptyHC = tracker.getStateHashCode();

        tracker.trackInvolvedPlugin(pluginA); // add A
        String justA = tracker.getStateHashCode();

        tracker.trackInvolvedPlugin(pluginA); // added twice
        assertThat(tracker.getStateHashCode(), equalTo(justA));

        tracker.trackInvolvedPlugin(pluginB); // add B
        String aAndB = tracker.getStateHashCode();

        tracker.clear(); // clear
        assertThat(tracker.getStateHashCode(), equalTo(emptyHC));

        tracker.trackInvolvedPlugin(pluginB); // add B back (different order)
        tracker.trackInvolvedPlugin(pluginA); // add A back
        assertThat(tracker.getStateHashCode(), equalTo(aAndB));

        tracker.clear(); // clear
        assertThat(tracker.getStateHashCode(), equalTo(emptyHC));

        tracker.trackInvolvedPlugin(pluginA); // add A back
        assertThat(tracker.getStateHashCode(), equalTo(justA));
    }
}
