package com.atlassian.jira.web.sitemesh;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.plugin.decorator.DecoratorMapperModuleDescriptor;
import com.atlassian.jira.plugin.decorator.DecoratorModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.module.ModuleFactory;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.servlet.MockHttpServletRequest;
import com.opensymphony.module.sitemesh.Config;
import com.opensymphony.module.sitemesh.Decorator;
import com.opensymphony.module.sitemesh.DecoratorMapper;
import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.module.sitemesh.mapper.DefaultDecorator;

import org.easymock.ArgumentsMatcher;
import org.easymock.MockControl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestPluginDecoratorMapper
{
    private MockControl mockPluginAccessorControl;
    private PluginAccessor mockPluginAccessor;
    private PluginDecoratorMapper mapper;
    private MockControl mockParentMapperControl;
    private DecoratorMapper mockParentMapper;
    private MockControl mockPluginMapperControl;
    private DecoratorMapper mockPluginMapper;
    private DecoratorMapperModuleDescriptor mockMapperDescriptor;

    @Before
    public void setUp() throws Exception
    {
        mockPluginAccessorControl = MockControl.createControl(PluginAccessor.class);
        mockPluginAccessor = (PluginAccessor) mockPluginAccessorControl.getMock();
        mapper = new PluginDecoratorMapper()
        {
            PluginAccessor getPluginAccessor()
            {
                return mockPluginAccessor;
            }
        };
        mockParentMapperControl = MockControl.createControl(DecoratorMapper.class);
        mockParentMapper = (DecoratorMapper) mockParentMapperControl.getMock();
        mapper.init(null, null, mockParentMapper);

        mockPluginMapperControl = MockControl.createControl(DecoratorMapper.class);
        mockPluginMapper = (DecoratorMapper) mockPluginMapperControl.getMock();
        mockMapperDescriptor = new DecoratorMapperModuleDescriptor(ModuleFactory.LEGACY_MODULE_FACTORY)
        {
            public DecoratorMapper getDecoratorMapper(Config config, DecoratorMapper parent)
            {
                return mockPluginMapper;
            }

            public String getPluginKey()
            {
                return "plugin.key";
            }
        };

    }

    protected void verify()
    {
        mockPluginAccessorControl.verify();
        mockParentMapperControl.verify();
        mockPluginMapperControl.verify();
    }

    protected void replay()
    {
        mockPluginAccessorControl.replay();
        mockParentMapperControl.replay();
        mockPluginMapperControl.replay();
    }

    @Test
    public void testNoPlugins()
    {
        setupPluginAccessorExpectation(DecoratorMapperModuleDescriptor.class, Collections.EMPTY_LIST);
        setupPluginAccessorExpectation(DecoratorModuleDescriptor.class, Collections.EMPTY_LIST);
        Decorator decorator = new DefaultDecorator("decorator", "decorator", null);
        setupParentMapperInvocation(decorator);

        replay();

        assertEquals(decorator, invokeGetDecorator());

        verify();
    }

    @Test
    public void testMapperPluginMatches()
    {
        setupPluginAccessorExpectation(DecoratorMapperModuleDescriptor.class, EasyList.build(mockMapperDescriptor));
        Decorator decorator = new DefaultDecorator("decorator", "decorator", null);
        setupPluginMapperInvocation(decorator);

        replay();

        assertEquals(decorator, invokeGetDecorator());

        verify();
    }

    @Test
    public void testMapperPluginDoesntMatch()
    {
        setupPluginAccessorExpectation(DecoratorMapperModuleDescriptor.class, EasyList.build(mockMapperDescriptor));
        setupPluginAccessorExpectation(DecoratorModuleDescriptor.class, Collections.EMPTY_LIST);
        setupPluginMapperInvocation(null);

        Decorator decorator = new DefaultDecorator("decorator", "decorator", null);
        setupParentMapperInvocation(decorator);

        replay();

        assertEquals(decorator, invokeGetDecorator());

        verify();
    }

    @Test
    public void testMapperPluginNull()
    {
        setupPluginAccessorExpectation(DecoratorMapperModuleDescriptor.class, EasyList.build(new DecoratorMapperModuleDescriptor(ModuleFactory.LEGACY_MODULE_FACTORY)
        {
            public DecoratorMapper getDecoratorMapper(Config config, DecoratorMapper parent)
            {
                return null;
            }
            public String getPluginKey()
            {
                return "plugin.key";
            }
        }));
        setupPluginAccessorExpectation(DecoratorModuleDescriptor.class, Collections.EMPTY_LIST);
        Decorator decorator = new DefaultDecorator("decorator", "decorator", null);
        setupParentMapperInvocation(decorator);

        replay();

        assertEquals(decorator, invokeGetDecorator());

        verify();
    }

    @Test
    public void testDecoratorPluginMatches()
    {
        setupPluginAccessorExpectation(DecoratorMapperModuleDescriptor.class, Collections.EMPTY_LIST);
        setupPluginAccessorExpectation(DecoratorModuleDescriptor.class, EasyList.build(createModuleDescriptor("page",
            "p.th")));
        replay();

        assertEquals("page", invokeGetDecorator().getPage());

        verify();
    }

    @Test
    public void testDecoratorPluginDoesntMatch()
    {
        setupPluginAccessorExpectation(DecoratorMapperModuleDescriptor.class, Collections.EMPTY_LIST);
        setupPluginAccessorExpectation(DecoratorModuleDescriptor.class, EasyList.build(createModuleDescriptor("page",
            "psth")));
        Decorator decorator = new DefaultDecorator("decorator", "decorator", null);
        setupParentMapperInvocation(decorator);

        replay();

        assertEquals(decorator, invokeGetDecorator());

        verify();
    }

    @Test
    public void testNamedNoPlugins()
    {
        setupPluginAccessorExpectation(DecoratorMapperModuleDescriptor.class, Collections.EMPTY_LIST);
        setupPluginAccessorExpectation(DecoratorModuleDescriptor.class, Collections.EMPTY_LIST);
        Decorator decorator = new DefaultDecorator("decorator", "decorator", null);
        setupNamedParentMapperInvocation(decorator);

        replay();

        assertEquals(decorator, invokeGetNamedDecorator());

        verify();
    }

    @Test
    public void testNamedMapperPluginMatches()
    {
        setupPluginAccessorExpectation(DecoratorMapperModuleDescriptor.class, EasyList.build(mockMapperDescriptor));
        Decorator decorator = new DefaultDecorator("decorator", "decorator", null);
        setupNamedPluginMapperInvocation(decorator);

        replay();

        assertEquals(decorator, invokeGetNamedDecorator());

        verify();
    }

    @Test
    public void testNamedMapperPluginDoesntMatch()
    {
        setupPluginAccessorExpectation(DecoratorMapperModuleDescriptor.class, EasyList.build(mockMapperDescriptor));
        setupPluginAccessorExpectation(DecoratorModuleDescriptor.class, Collections.EMPTY_LIST);
        setupNamedPluginMapperInvocation(null);

        Decorator decorator = new DefaultDecorator("decorator", "decorator", null);
        setupNamedParentMapperInvocation(decorator);

        replay();

        assertEquals(decorator, invokeGetNamedDecorator());

        verify();
    }

    @Test
    public void testNamedMapperPluginNull()
    {
        setupPluginAccessorExpectation(DecoratorMapperModuleDescriptor.class, EasyList.build(new DecoratorMapperModuleDescriptor(ModuleFactory.LEGACY_MODULE_FACTORY)
        {
            public DecoratorMapper getDecoratorMapper(Config config, DecoratorMapper parent)
            {
                return null;
            }
            public String getPluginKey()
            {
                return "plugin.key";
            }
        }));
        setupPluginAccessorExpectation(DecoratorModuleDescriptor.class, Collections.EMPTY_LIST);
        Decorator decorator = new DefaultDecorator("decorator", "decorator", null);
        setupNamedParentMapperInvocation(decorator);

        replay();

        assertEquals(decorator, invokeGetNamedDecorator());

        verify();
    }

    @Test
    public void testNamedDecoratorPluginMatches()
    {
        setupPluginAccessorExpectation(DecoratorMapperModuleDescriptor.class, Collections.EMPTY_LIST);
        setupPluginAccessorExpectation(DecoratorModuleDescriptor.class, EasyList.build(createModuleDescriptor("page",
            "name")));
        replay();

        assertEquals("page", invokeGetNamedDecorator().getPage());

        verify();
    }

    @Test
    public void testNamedDecoratorPluginDoesntMatch()
    {
        setupPluginAccessorExpectation(DecoratorMapperModuleDescriptor.class, Collections.EMPTY_LIST);
        setupPluginAccessorExpectation(DecoratorModuleDescriptor.class, EasyList.build(createModuleDescriptor("page",
            "blah")));
        Decorator decorator = new DefaultDecorator("decorator", "decorator", null);
        setupNamedParentMapperInvocation(decorator);

        replay();

        assertEquals(decorator, invokeGetNamedDecorator());

        verify();
    }



    private void setupPluginAccessorExpectation(Class clazz, List result)
    {
        mockPluginAccessor.getEnabledModuleDescriptorsByClass(clazz);
        mockPluginAccessorControl.setReturnValue(result);
    }

    private void setupParentMapperInvocation(Decorator decorator)
    {
        mockParentMapper.getDecorator(null, null);
        mockParentMapperControl.setMatcher(ANY_MATCHER);
        mockParentMapperControl.setReturnValue(decorator);
    }

    private void setupPluginMapperInvocation(Decorator decorator)
    {
        mockPluginMapper.getDecorator(null, null);
        mockPluginMapperControl.setMatcher(ANY_MATCHER);
        mockPluginMapperControl.setReturnValue(decorator);
    }

    private void setupNamedParentMapperInvocation(Decorator decorator)
    {
        mockParentMapper.getNamedDecorator(null, null);
        mockParentMapperControl.setMatcher(ANY_MATCHER);
        mockParentMapperControl.setReturnValue(decorator);
    }

    private void setupNamedPluginMapperInvocation(Decorator decorator)
    {
        mockPluginMapper.getNamedDecorator(null, null);
        mockPluginMapperControl.setMatcher(ANY_MATCHER);
        mockPluginMapperControl.setReturnValue(decorator);
    }

    private static final ArgumentsMatcher ANY_MATCHER = new ArgumentsMatcher()
    {
        public boolean matches(Object[] objects, Object[] objects1)
        {
            return true;
        }

        public String toString(Object[] objects)
        {
            return null;
        }
    };

    private Decorator invokeGetDecorator()
    {
        MockHttpServletRequest request = new MockHttpServletRequest()
        {
            public String getServletPath()
            {
                return "path";
            }

            public String getRequestURI()
            {
                return "path";
            }

            public String getContextPath()
            {
                return "";
            }
        };
        // This runs the actual test
        return mapper.getDecorator(request, (Page) new Mock(Page.class).proxy());
    }

    private Decorator invokeGetNamedDecorator()
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        // This runs the actual test
        return mapper.getNamedDecorator(request, "name");
    }

    private DecoratorModuleDescriptor createModuleDescriptor(final String page, final String pattern)
    {
        return new DecoratorModuleDescriptor(ModuleFactory.LEGACY_MODULE_FACTORY)
        {
            public Pattern getPattern()
            {
                return Pattern.compile(pattern);
            }

            public String getPage()
            {
                return page;
            }

            public String getPluginKey()
            {
                return "plugin.key";
            }

            public String getName()
            {
                return pattern;
            }
        };
    }

}
