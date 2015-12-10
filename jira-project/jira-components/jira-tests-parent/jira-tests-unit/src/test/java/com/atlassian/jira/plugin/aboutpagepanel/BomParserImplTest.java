package com.atlassian.jira.plugin.aboutpagepanel;

import java.util.List;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class BomParserImplTest
{
    @Test
    public void bomParseNewlines()
    {
        BomParserImpl aboutPage = new BomParserImpl();
        List<AboutPagePanelModuleDescriptorImpl.Material> materials = aboutPage.extractLgplMaterials("amf-serializer,com.exadel.flamingo.flex:amf-serializer:jar:2.2.0,GNU Lesser General Public License 2.1,,binary\r\natlassian-image-consumer,com.atlassian.image:atlassian-image-consumer:jar:1.0.1,GNU Lesser General Public License 2.1,http://www.atlassian.com/atlassian-public-pom/atlassian-image-consumer/,binary");
        assertEquals(2, materials.size());
        assertEquals(materials.get(0), new AboutPagePanelModuleDescriptorImpl.Material("amf-serializer", "com.exadel.flamingo.flex:amf-serializer:jar:2.2.0", "GNU Lesser General Public License 2.1", "", "binary"));
        assertEquals(materials.get(1), new AboutPagePanelModuleDescriptorImpl.Material("atlassian-image-consumer", "com.atlassian.image:atlassian-image-consumer:jar:1.0.1", "GNU Lesser General Public License 2.1", "http://www.atlassian.com/atlassian-public-pom/atlassian-image-consumer/", "binary"));
    }

    @Test
    public void bomParseComments()
    {
        BomParserImpl aboutPage = new BomParserImpl();
        List<AboutPagePanelModuleDescriptorImpl.Material> materials = aboutPage.extractLgplMaterials(
                "PluginA,,GNU Lesser General Public License 2.1,,\n" +
                        "  #PluginB,,GNU Lesser General Public License 2.1,,\n" +
                        "Plugin#,,GNU Lesser General Public License 2.1,,\n" +
                        "#PluginD,,GNU Lesser General Public License 2.1,,");
        assertEquals(2, materials.size());
        assertEquals(materials.get(0), new AboutPagePanelModuleDescriptorImpl.Material("PluginA", "", "GNU Lesser General Public License 2.1", "", ""));
        assertEquals(materials.get(1), new AboutPagePanelModuleDescriptorImpl.Material("Plugin#", "", "GNU Lesser General Public License 2.1", "", ""));
    }

    @Test
    public void bomParseEmpty()
    {

        BomParserImpl aboutPage = new BomParserImpl();
        List<AboutPagePanelModuleDescriptorImpl.Material> materials = aboutPage.extractLgplMaterials("");
        assertEquals(0, materials.size());
    }

    @Test
    public void bomParseWhitespaceStrings()
    {
        BomParserImpl aboutPage = new BomParserImpl();
        List<AboutPagePanelModuleDescriptorImpl.Material> materials = aboutPage.extractLgplMaterials(" PluginA ,\t, GNU Lesser General Public License 2.1 ,  \t , \n");
        assertEquals(1, materials.size());
        assertEquals(materials.get(0), new AboutPagePanelModuleDescriptorImpl.Material("PluginA", "", "GNU Lesser General Public License 2.1", "", ""));
    }

    @Test
    public void bomParseRequireSomeDetails()
    {
        BomParserImpl aboutPage = new BomParserImpl();
        List<AboutPagePanelModuleDescriptorImpl.Material> materials = aboutPage.extractLgplMaterials(",,GNU Lesser General Public License 2.1,,");
        assertEquals(0, materials.size());
    }
}
