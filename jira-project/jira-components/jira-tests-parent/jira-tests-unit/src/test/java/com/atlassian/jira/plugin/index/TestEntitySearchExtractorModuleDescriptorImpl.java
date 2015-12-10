package com.atlassian.jira.plugin.index;

import com.atlassian.jira.index.EntitySearchExtractor;
import com.atlassian.jira.index.SearchExtractorRegistrationManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginException;
import com.atlassian.plugin.module.ModuleFactory;

import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestEntitySearchExtractorModuleDescriptorImpl
{
    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    public ModuleFactory moduleFactory;
    @Mock
    public JiraAuthenticationContext jiraAuthenticationContext;
    @AvailableInContainer @Mock
    public SearchExtractorRegistrationManager extractorRegistrationManager;

    private EntitySearchExtractorModuleDescriptorImpl moduleDescriptor;

    @Before
    public void setUp() throws Exception
    {
        this.moduleDescriptor = new EntitySearchExtractorModuleDescriptorImpl(jiraAuthenticationContext, moduleFactory)
        {
            @Override
            public Class<EntitySearchExtractor<?>> getModuleClass()
            {
                try
                {
                    // noinspection unchecked
                    return (Class<EntitySearchExtractor<?>>) Class.forName(getModuleClassName());
                }
                catch (ClassNotFoundException e)
                {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Test
    public void moduleWithValidImplementationOfEntitySearchExtractor()
    {
        when(moduleFactory.createModule(eq(CommentSearchExtractorImpl.class.getCanonicalName()), any(ModuleDescriptor.class)))
                .thenReturn(new CommentSearchExtractorImpl());
        moduleDescriptor.init(mock(Plugin.class), buildModuleDescriptor(CommentSearchExtractorImpl.class));
        moduleDescriptor.enabled();

        EntitySearchExtractorModuleDescriptorImpl.RegistrationStrategy<?> registrationStrategy = moduleDescriptor.getRegistrationStrategy();
        assertThat(registrationStrategy.getEntityType(), Matchers.<Class<?>>is(Comment.class));
    }

    @Test
    public void enablingValidModule()
    {
        CommentSearchExtractorImpl extractor = new CommentSearchExtractorImpl();
        when(moduleFactory.createModule(eq(CommentSearchExtractorImpl.class.getCanonicalName()), any(ModuleDescriptor.class)))
                .thenReturn(extractor);
        moduleDescriptor.init(mock(Plugin.class), buildModuleDescriptor(CommentSearchExtractorImpl.class));
        moduleDescriptor.enabled();

        verify(extractorRegistrationManager).register(eq(extractor), eq(Comment.class));
    }

    @Test
    public void disablingValidModule()
    {
        CommentSearchExtractorImpl extractor = new CommentSearchExtractorImpl();
        when(moduleFactory.createModule(eq(CommentSearchExtractorImpl.class.getCanonicalName()), any(ModuleDescriptor.class)))
                .thenReturn(extractor);
        moduleDescriptor.init(mock(Plugin.class), buildModuleDescriptor(CommentSearchExtractorImpl.class));
        moduleDescriptor.enabled();

        moduleDescriptor.disabled();
        verify(extractorRegistrationManager).unregister(eq(extractor), eq(Comment.class));

    }

    @Test
    public void providingImplementationOfInvalidClassResultsInPluginParseException()
    {
        moduleDescriptor.init(mock(Plugin.class), buildModuleDescriptor(InvalidEntitySearchExtractor.class));
        exception.expect(PluginException.class);
        moduleDescriptor.enabled();
    }

    private Element buildModuleDescriptor(Class<?> clazz)
    {
        return new DOMElement("entity-search-extractor")
                .addAttribute("class", clazz.getCanonicalName())
                .addAttribute("key", "module-key");

    }

}
