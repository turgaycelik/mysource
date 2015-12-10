package com.atlassian.jira.plugin.aboutpagepanel;

import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.config.util.EncodingConfiguration;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.license.thirdparty.BomParser;
import com.atlassian.jira.mock.plugin.MockPlugin;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.soy.SoyTemplateRendererProvider;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.ContextProvider;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AboutPagePanelModuleDescriptorImplTest
{
    @Rule
    public TestRule rule = new InitMockitoMocks(this);

    @Mock private JiraAuthenticationContext authenticationContext;
    @Mock private ModuleFactory legacyModuleFactory;
    @Mock private EncodingConfiguration encodingConfiguration;
    @Mock private SoyTemplateRendererProvider soyTemplateRendererProvider;
    @Mock private SoyTemplateRenderer soyTemplateRenderer;
    @Mock private WebInterfaceManager webInterfaceManager;
    @Mock private WebFragmentHelper webFragmentHelper;
    @Mock private ContextProvider contextProvider;
    @Mock private BomParser bomParser;
    private Plugin plugin = new MockPlugin("example.key")
            .setClassLoader(AboutPagePanelModuleDescriptorImplTest.class.getClassLoader());

    private AboutPagePanelModuleDescriptorImpl descriptor;

    @Before
    public void onTestInit() throws Exception
    {
        when(webInterfaceManager.getWebFragmentHelper()).thenReturn(webFragmentHelper);
        when(webFragmentHelper.loadContextProvider(eq("mockContextProvider"), any(Plugin.class))).thenReturn(contextProvider);

        descriptor = new AboutPagePanelModuleDescriptorImpl(authenticationContext, webInterfaceManager, encodingConfiguration,
                soyTemplateRendererProvider, bomParser);
    }

    @Test
    public void getIntroduction() throws Exception
    {
        descriptor.init(plugin, readResource("licenseModuleWithIntroAndConclusion.xml"));

        descriptor.generateIntroduction(soyTemplateRenderer, Collections.<String, Object> emptyMap());
        verify(soyTemplateRenderer, times(1)).render(any(String.class), any(String.class), any(Map.class));
    }

    @Test
    public void getConclusion() throws Exception
    {
        descriptor.init(plugin, readResource("licenseModuleWithIntroAndConclusion.xml"));

        descriptor.generateConclusion(soyTemplateRenderer, Collections.<String, Object> emptyMap());
        verify(soyTemplateRenderer, times(1)).render(matches("exampleModuleKey"), matches("conclusion"), any(Map.class));
    }

    @Test
    public void getContextProvider() throws Exception
    {
        Map<String, Object> data = ImmutableMap.<String, Object> of("parameterName", "parameterValue");
        when(soyTemplateRendererProvider.getRenderer()).thenReturn(soyTemplateRenderer);
        when(contextProvider.getContextMap(any(Map.class))).thenReturn(data);
        descriptor.init(plugin, readResource("aboutPagePanelWithContextProvider.xml"));
        descriptor.enabled();
        descriptor.getPluginSectionHtml();
        verify(contextProvider, times(1)).getContextMap(any(Map.class));
        verify(soyTemplateRenderer, times(2)).render(any(String.class), any(String.class), any(Map.class));
    }

    @Test
    public void verifyConstructor() throws Exception
    {
        expectExceptionInit("<about-page-panel key=\"key\"/>");
        expectExceptionInit("<about-page-panel key=\"key\"><introduction/></about-page-panel>");
        expectExceptionInit("<about-page-panel key=\"key\"><introduction function='introduction'/></about-page-panel>");
        expectOkInit(
                "<about-page-panel key=\"key\">"
                    + "<introduction function='introduction' module-key='exampleModuleKey'/>"
                + "</about-page-panel>");

        expectExceptionInit("<about-page-panel key=\"key\">><licenses/></about-page-panel>");
        expectOkInit("<about-page-panel key=\"key\">><licenses location='exampleFile'/></about-page-panel>");

        expectExceptionInit(
                "<about-page-panel key=\"key\">"
                    + "<introduction function='introduction' module-key='exampleModuleKey'/>"
                    + "<licenses location='exampleFile'/>"
                    + "<conclusion/>"
                + "</about-page-panel>");

        expectExceptionInit(
                "<about-page-panel key=\"key\">"
                    + "<introduction function='introduction' module-key='exampleModuleKey'/>"
                    + "<licenses location='exampleFile'/>"
                    + "<conclusion function='exampleConclusion'/>"
                + "</about-page-panel>");

        expectOkInit(
                "<about-page-panel key=\"key\">"
                    + "<introduction function='introduction' module-key='exampleModuleKey'/>"
                    + "<licenses location='exampleFile'/>"
                    + "<conclusion function='exampleConclusion' module-key='exampleModuleKey'/>"
                + "</about-page-panel>");

        expectOkInit(
                "<about-page-panel key=\"key\">"
                    + "<context-provider class=\"mockContextProvider\"></context-provider>"
                    + "<introduction function='introduction' module-key='exampleModuleKey'/>"
                    + "<licenses location='exampleFile'/>"
                    + "<conclusion function='exampleConclusion' module-key='exampleModuleKey'/>"
                + "</about-page-panel>");
    }

    @Test
    public void loadsBomFromPlugin() throws SoyException
    {
        final AboutPagePanelModuleDescriptorImpl.Material material = createMaterial("one");

        when(bomParser.extractLgplMaterials("bomContents"))
                .thenReturn(ImmutableList.of(material));

        descriptor.init(plugin, readResource("licenseModuleWithBom.xml"));

        final List<AboutPagePanelModuleDescriptorImpl.Material> result = descriptor.getMaterials(plugin);
        assertThat(result, Matchers.contains(material));
    }

    @Test
    public void ignoresBadBomFromPlugin() throws SoyException
    {
        descriptor.init(plugin, readResource("licenseModuleWithBadBom.xml"));

        final List<AboutPagePanelModuleDescriptorImpl.Material> result = descriptor.getMaterials(plugin);
        assertThat(result.isEmpty(), Matchers.equalTo(true));
    }

    @Test
    public void renderItemReturnsEmptyOnException() throws Exception
    {
        when(soyTemplateRenderer.render(anyString(), anyString(), any(Map.class))).thenThrow(new SoyException(""));
        String s = descriptor.renderItem(soyTemplateRenderer, "a", "b", Collections.<String, Object> emptyMap());
        assertEquals("", s);
    }

    private AboutPagePanelModuleDescriptorImpl.Material createMaterial(String name)
    {
        return new AboutPagePanelModuleDescriptorImpl.Material(name, name, name, name, name);
    }

    private Element readResource(final String resource)
    {
        SAXReader reader  = new SAXReader();
        try
        {
            return reader.read(getClass().getResourceAsStream(resource)).getRootElement();
        }
        catch (DocumentException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Element readInline(String string)
    {
        SAXReader reader  = new SAXReader();
        try
        {
            return reader.read(new StringReader(string)).getRootElement();
        }
        catch (DocumentException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void expectExceptionInit(String element)
    {
        try
        {
            descriptor.init(plugin, readInline(element));
            descriptor.enabled();
            fail("expected PluginParseException");
        }
        catch(PluginParseException ignored){}
    }

    private void expectOkInit(String element)
    {
        try
        {
            descriptor.init(plugin, readInline(element));
            descriptor.enabled();
        }
        catch(PluginParseException ignored)
        {
            fail("Did not expect plugin parse exception");
        }
    }
}
