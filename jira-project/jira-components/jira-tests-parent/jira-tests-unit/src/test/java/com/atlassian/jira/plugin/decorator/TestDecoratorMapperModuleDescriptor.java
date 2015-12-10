package com.atlassian.jira.plugin.decorator;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.impl.StaticPlugin;
import com.atlassian.plugin.module.ModuleFactory;

import com.opensymphony.module.sitemesh.Config;
import com.opensymphony.module.sitemesh.Decorator;
import com.opensymphony.module.sitemesh.DecoratorMapper;
import com.opensymphony.module.sitemesh.Page;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TestDecoratorMapperModuleDescriptor
{
    private DecoratorMapperModuleDescriptor desc;
    private DecoratorMapper mockDecoratorMapper;

    @Before
    public void setUp() throws Exception
    {
        mockDecoratorMapper = mock(DecoratorMapper.class);
        desc = new DecoratorMapperModuleDescriptor(ModuleFactory.LEGACY_MODULE_FACTORY)
        {
            @Override
            DecoratorMapper createDecoratorMapper()
            {
                return mockDecoratorMapper;
            }
        };
    }

    @Test
    public void testParseDecoratorMapper() throws Exception
    {
        parse("<decorator-mapper key='key' class='com.atlassian.jira.plugin.decorator.TestDecoratorMapperModuleDescriptor$TestDecoratorMapper'></decorator-mapper>");
        desc.enabled();

        assertEquals(TestDecoratorMapper.class, desc.getModuleClass());
    }

    @Test
    public void testParseDecoratorNoClass() throws Exception
    {
        try
        {
            parse("<decorator-mapper key='key'></decorator-mapper>");
            fail("Exception not thrown when no class set");
        }
        catch (PluginParseException ppe)
        {
//            ppe.printStackTrace();
        }
    }

    @Test
    public void testParseDecoratorClassNotExist() throws Exception
    {
        try
        {
            parse(
                "<decorator-mapper key='key' class='com.atlassian.ThisClassNoExist'></decorator-mapper>");
            desc.enabled();
            fail("Exception not thrown when non existant class set");
        }
        catch (PluginParseException ppe)
        {
//            ppe.printStackTrace();
        }
    }

    @Test
    public void testDecoratorIsInitialised() throws InstantiationException
    {
        Config mockConfig = mock(Config.class);
        DecoratorMapper mockParent = mock(DecoratorMapper.class);
        DecoratorMapper decoratorMapper = desc.getDecoratorMapper(mockConfig, mockParent);

        verify(decoratorMapper).init(same(mockConfig), eq(new Properties()), same(mockParent));
    }

    private void parse(String xml) throws Exception
    {
        Document document = DocumentHelper.parseText(xml);
        desc.init(new StaticPlugin(), document.getRootElement());
    }

    public class TestDecoratorMapper implements DecoratorMapper
    {
        public void init(Config config, Properties properties, DecoratorMapper decoratorMapper)
            throws InstantiationException
        {
        }

        public Decorator getDecorator(HttpServletRequest httpServletRequest, Page page)
        {
            return null;
        }

        public Decorator getNamedDecorator(HttpServletRequest httpServletRequest, String s)
        {
            return null;
        }
    }

}
