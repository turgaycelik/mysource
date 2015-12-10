package com.atlassian.jira.plugin.customfield;

import java.util.HashSet;

import com.atlassian.jira.issue.customfields.CustomFieldTypeCategory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.impl.StaticPlugin;
import com.atlassian.plugin.module.ModuleFactory;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;

public class TestCustomFieldTypeModuleDescriptorImpl
{
    private final String noCategoryXML = "<customfield-type key=\"foo\" name=\"Foo\"\n" +
            "        class=\"com.atlassian.jira.issue.customfields.impl.RenderableTextCFType\">\n" +
            "    </customfield-type>";

    private final String declaredCategoryXML = "<customfield-type key=\"foo\" name=\"Foo\"\n" +
            "        class=\"com.atlassian.jira.issue.customfields.impl.RenderableTextCFType\">\n" +
            "    <category>ALL</category>\n" +
            "    <category>STANDARD</category>\n" +
            "</customfield-type>";

    private CustomFieldTypeModuleDescriptorImpl descriptor;
    private Plugin plugin;

    @Before
    public void setUp()
    {
        this.descriptor = new CustomFieldTypeModuleDescriptorImpl(null, null, ModuleFactory.LEGACY_MODULE_FACTORY, null);
        this.plugin = new StaticPlugin();
    }

    @Test
    public void testNoCategoriesFromXML() throws PluginParseException, DocumentException
    {
        final Document document = DocumentHelper.parseText(noCategoryXML);
        descriptor.init(plugin, document.getRootElement());
        assertEquals(newHashSet(CustomFieldTypeCategory.ALL, CustomFieldTypeCategory.ADVANCED),
                descriptor.getCategories());
    }

    @Test
    public void testDeclaredCategoriesFromXML() throws PluginParseException, DocumentException
    {
        final Document document = DocumentHelper.parseText(declaredCategoryXML);
        descriptor.init(plugin, document.getRootElement());
        assertEquals(newHashSet(CustomFieldTypeCategory.ALL, CustomFieldTypeCategory.STANDARD),
                descriptor.getCategories());
    }

    @Test
    public void testDeduceCategories()
    {
        // [ALL] -> [ALL, ADVANCED]
        assertEquals(newHashSet(CustomFieldTypeCategory.ALL, CustomFieldTypeCategory.ADVANCED),
                descriptor.deduceCategories(newHashSet("ALL")));

        // [ALL, ADVANCED] -> [ALL, ADVANCED]
        assertEquals(newHashSet(CustomFieldTypeCategory.ALL, CustomFieldTypeCategory.ADVANCED),
                descriptor.deduceCategories(newHashSet("ALL", "ADVANCED")));

        // [ADVANCED] -> [ALL, ADVANCED]
        assertEquals(newHashSet(CustomFieldTypeCategory.ALL, CustomFieldTypeCategory.ADVANCED),
                descriptor.deduceCategories(newHashSet("ADVANCED")));

        // [] -> [ALL, ADVANCED]
        assertEquals(newHashSet(CustomFieldTypeCategory.ALL, CustomFieldTypeCategory.ADVANCED),
                descriptor.deduceCategories(new HashSet<String>()));

        // [STANDARD] -> [ALL, STANDARD]
        assertEquals(newHashSet(CustomFieldTypeCategory.ALL, CustomFieldTypeCategory.STANDARD),
                descriptor.deduceCategories(newHashSet("STANDARD")));

        // [ADVANCED, STANDARD] -> [ALL, STANDARD, ADVANCED]
        assertEquals(newHashSet(CustomFieldTypeCategory.ALL, CustomFieldTypeCategory.STANDARD, CustomFieldTypeCategory.ADVANCED),
                descriptor.deduceCategories(newHashSet("STANDARD", "ADVANCED")));
    }

}
