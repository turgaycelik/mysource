package com.atlassian.jira.plugin.language;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.impl.StaticPlugin;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.util.validation.ValidationException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @since v4.3
 */
public class TestLanguageModuleDesciptor
{
    String goodLanguageModuleXML =
            "<language name=\"Polish\" key=\"pl_PL\" language=\"pl\" country=\"PL\">\n" +
            "    <resource name=\"pl_PL.gif\" type=\"download\" location=\"templates/languages/pl_PL/pl_PL.gif\">\n" +
            "        <property key=\"content-type\" value=\"image/gif\"/>\n" +
            "    </resource>\n" +
            "</language>";

    String moduleWithoutCountryXML =
            "<language name=\"Polish\" key=\"pl_PL\" language=\"pl\">\n" +
            "</language>";

    String moduleWithoutLanguageParameterXML =
            "<language invalid=\"aaa\">\n" +
            "</language>";

    private Document document;
    private LanguageModuleDescriptor md = new LanguageModuleDescriptorImpl(null, ModuleFactory.LEGACY_MODULE_FACTORY);
    private Plugin plugin = new StaticPlugin();

    @Test
    public void testGoodDescriptor() throws DocumentException
    {
        document = DocumentHelper.parseText(goodLanguageModuleXML);
        md.init(plugin, document.getRootElement());
        assertEquals("pl", md.getModule().getLanguage());
        assertEquals("PL", md.getModule().getCountry());
        assertEquals("/download/resources/null:pl_PL/pl_PL.gif", md.getModule().getFlagUrl());
    }

    @Test
    public void testModuleWithoutCountryExplicitlySet() throws DocumentException
    {
        document = DocumentHelper.parseText(moduleWithoutCountryXML);
        md.init(plugin, document.getRootElement());
        assertEquals("pl", md.getModule().getLanguage());
        assertEquals("", md.getModule().getCountry());
    }

    @Test
    public void testDescriptorWithoutLanguageParam() throws DocumentException
    {
        document = DocumentHelper.parseText(moduleWithoutLanguageParameterXML);
        try
        {
            md.init(plugin, document.getRootElement());
        }
        catch (ValidationException e)
        {
            return;
        }
        fail("Language modules without \"language\" parameter should cause an exception to be thrown");
    }
}
