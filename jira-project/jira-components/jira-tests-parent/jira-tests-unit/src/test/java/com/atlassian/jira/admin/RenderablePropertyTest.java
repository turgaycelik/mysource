package com.atlassian.jira.admin;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.plugin.renderer.JiraRendererModuleDescriptor;
import com.atlassian.velocity.VelocityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith (ListeningMockitoRunner.class)
public class RenderablePropertyTest
{
    private static final String PROP_NAME = "prop_1";
    private static final String PROP_RAW_VIEW = "value_1_view";
    private static final String PROP_RAW_EDIT = "value_1_edit";
    private static final String PROP_WIKI_VIEW = "prop_1_wiki";
    private static final String PROP_WIKI_EDIT = "prop_1_wikiedit";

    private final PropertyPersister persister = new PropertyPersister()
    {
        @Override
        public String load()
        {
            return PROP_RAW_VIEW;
        }

        @Override
        public void save(String value)
        {
            // do nothing
        }
    };

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private FeatureManager featureManager;

    @Mock
    private VelocityManager velocityManager;

    @Mock
    private RendererManager rendererManager;

    @Mock
    private JiraRendererPlugin wikiRenderer;

    @Mock
    private JiraRendererModuleDescriptor wikiRendererDescriptor;

    @Before
    public void setUp() throws Exception
    {
        when(applicationProperties.getText(PROP_NAME)).thenReturn(PROP_RAW_VIEW);

        // set up wiki renderer mocks
        when(rendererManager.getRenderedContent(anyString(), anyString(), any(IssueRenderContext.class))).thenReturn(PROP_WIKI_VIEW);
        when(rendererManager.getRendererForType(AtlassianWikiRenderer.RENDERER_TYPE)).thenReturn(wikiRenderer);
        when(wikiRenderer.getDescriptor()).thenReturn(wikiRendererDescriptor);
        when(wikiRendererDescriptor.getEditVM(eq(PROP_RAW_VIEW), eq((String) null), eq(AtlassianWikiRenderer.RENDERER_TYPE), eq("prop_1"), eq("prop_1"), anyMap(), eq(false))).thenReturn(PROP_WIKI_EDIT);

        // set up Velocity mocks
        when(velocityManager.getEncodedBody(eq(RenderablePropertyImpl.class.getPackage().getName().replace(".", "/") + "/renderable-property-edit.vm"), eq(""), anyString(), anyMap())).thenReturn(PROP_RAW_EDIT);
    }

    @Test
    public void propertyViewShouldRenderAsHtmlInJiraBehindTheFirewall() throws Exception
    {
        setUpOnDemandFeature(false);

        RenderableProperty prop = new RenderablePropertyImpl(applicationProperties, featureManager, velocityManager, rendererManager, persister, null);
        assertThat(prop.getViewHtml(), equalTo(PROP_RAW_VIEW));
    }

    @Test
    public void propertyEditShouldBeRenderedFromVelocityTemplateInJiraBehindTheFirewall() throws Exception
    {
        setUpOnDemandFeature(false);

        RenderableProperty prop = new RenderablePropertyImpl(applicationProperties, featureManager, velocityManager, rendererManager, persister, null);
        assertThat(prop.getEditHtml("name"), equalTo(PROP_RAW_EDIT));
    }

    @Test
    public void propertyViewShouldRenderAsWikiMarkupInJiraOnDemand() throws Exception
    {
        setUpOnDemandFeature(true);

        RenderableProperty prop = new RenderablePropertyImpl(applicationProperties, featureManager, velocityManager, rendererManager, persister, null);
        assertThat(prop.getViewHtml(), equalTo(PROP_WIKI_VIEW));
    }

    @Test
    public void propertyEditShouldBeRenderedByRendererManagerInJiraOnDemand() throws Exception
    {
        setUpOnDemandFeature(true);

        RenderableProperty prop = new RenderablePropertyImpl(applicationProperties, featureManager, velocityManager, rendererManager, persister, null);
        assertThat(prop.getEditHtml(PROP_NAME), equalTo(PROP_WIKI_EDIT));
    }

    private void setUpOnDemandFeature(boolean onDemand)
    {
        when(featureManager.isOnDemand()).thenReturn(onDemand);
    }
}
