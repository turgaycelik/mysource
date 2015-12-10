package com.atlassian.jira.plugin.aboutpagepanel;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.jira.config.util.EncodingConfiguration;
import com.atlassian.jira.license.thirdparty.BomParser;
import com.atlassian.jira.plugin.webfragment.descriptors.JiraAbstractWebFragmentModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.soy.SoyTemplateRendererProvider;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebItemModuleDescriptor;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang.StringUtils.isEmpty;

public class AboutPagePanelModuleDescriptorImpl extends JiraAbstractWebFragmentModuleDescriptor implements AboutPagePanelModuleDescriptor
{
    private static final Logger log = LoggerFactory.getLogger(AboutPagePanelModuleDescriptorImpl.class);

    private static final String LOCATION_KEY = "location";
    private static final String MODULE_KEY = "module-key";
    private static final String FUNCTION_KEY = "function";
    private final EncodingConfiguration encodingConfiguration;
    private final SoyTemplateRendererProvider soyTemplateRendererProvider;
    private final BomParser bomParser;

    private volatile String introduction = "";
    private volatile String licenses = "";
    private volatile String conclusion = "";
    private volatile String introductionModule = "";
    private volatile String conclusionModule = "";

    public AboutPagePanelModuleDescriptorImpl(JiraAuthenticationContext jiraAuthenticationContext,
            final WebInterfaceManager webInterfaceManager, final EncodingConfiguration encodingConfiguration,
            final SoyTemplateRendererProvider soyTemplateRendererProvider, final BomParser bomParser)
    {
        super(jiraAuthenticationContext, new DefaultWebItemModuleDescriptor(webInterfaceManager));
        this.encodingConfiguration = encodingConfiguration;
        this.soyTemplateRendererProvider = soyTemplateRendererProvider;
        this.bomParser = bomParser;
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        super.init(plugin, element);
        Element introduction = element.element("introduction");
        Element licenses = element.element("licenses");
        Element conclusion = element.element("conclusion");

        if (introduction == null && licenses == null && conclusion == null)
            throw new PluginParseException("An introduction template, licenses file or conclusion template must be provided.");

        if (introduction != null)
        {
            this.introduction = assertFunctionPresent(introduction, "Introduction");
            this.introductionModule = assertModuleKeyPresent(introduction, "Introduction");
        }

        if (licenses != null)
        {
            this.licenses = assertLocationPresent(licenses, "Licenses");
        }
        
        if (conclusion != null)
        {
            this.conclusion = assertFunctionPresent(conclusion, "Conclusion");
            this.conclusionModule = assertModuleKeyPresent(conclusion, "Conclusion");
        }

    }

    private String assertModuleKeyPresent(final Element introduction, String field)
    {
        String location = introduction.attributeValue(MODULE_KEY);
        if (StringUtils.isEmpty(location))
            throw new PluginParseException(field + " module key must be specified");
        return location;
    }

    private String assertLocationPresent(final Element introduction, String field)
    {
        String location = introduction.attributeValue(LOCATION_KEY);
        if (StringUtils.isEmpty(location))
            throw new PluginParseException(field + " license file must be specified");
        return location;
    }

    private String assertFunctionPresent(final Element introduction, String field)
    {
        String location = introduction.attributeValue(FUNCTION_KEY);
        if (StringUtils.isEmpty(location))
            throw new PluginParseException(field + " function must be specified");
        return location;
    }

    @Override
    public String getPluginSectionHtml()
    {
        PluginAndMaterials pluginAndMaterials = getPluginAndMaterials();

        if (pluginAndMaterials == null)
            return "";

        HashMap<String, Object> a = Maps.newHashMap();
        a.put("pluginEntry", pluginAndMaterials);
        try
        {
            return getSoyRenderer().render("jira.webresources:action-soy-templates", "JIRA.Templates.About.renderModule", a);
        }
        catch (SoyException e)
        {
            log.debug("Problem rendering {}", pluginAndMaterials, e);
            return "";
        }
    }

    @Override
    public String getHtml(final String resourceName)
    {
        return getPluginSectionHtml();
    }

    @Override
    public String getHtml(final String resourceName, final Map<String, ?> startingParams)
    {
        return getPluginSectionHtml();
    }

    @Override
    public void writeHtml(final String resourceName, final Map<String, ?> startingParams, final Writer writer)
            throws IOException
    {
        writer.write(getPluginSectionHtml());
    }

    private PluginAndMaterials getPluginAndMaterials()
    {
        final Plugin plugin = getPlugin();

        try
        {
            SoyTemplateRenderer soyTemplateRenderer = getSoyRenderer();
            List<Material> a = getMaterials(plugin);
            Map<String, Object> data = getContextProvider().getContextMap(Collections.<String, Object>emptyMap());
            String introduction = generateIntroduction(soyTemplateRenderer, data);
            String conclusion = generateConclusion(soyTemplateRenderer, data);

            return new PluginAndMaterials(plugin.getName(), plugin.getPluginInformation().getVersion(), introduction, conclusion, a);
        }
        catch (RuntimeException e)
        {
            // Failure to load one plugin's information should not cause the about box to not be displayed.
            log.info("Could not load license information for " + plugin.getName() + " " + plugin.getKey(), e);
        }
        return null;
    }

    @VisibleForTesting
    List<Material> getMaterials(final Plugin plugin)
    {
        return Lists.newArrayList(loadMaterials(plugin));
    }

    private Set<Material> loadMaterials(final Plugin plugin)
    {
        final Set<Material> materials = Sets.newTreeSet();
        if (!isEmpty(this.getLicensesLocation()))
        {
            ClassLoader classLoader = plugin.getClassLoader();

            InputStream resourceAsStream = classLoader.getResourceAsStream(this.getLicensesLocation());
            if (resourceAsStream != null)
            {
                try
                {
                    String bomContents = IOUtils.toString(resourceAsStream, encodingConfiguration.getEncoding());
                    Iterables.addAll(materials, bomParser.extractLgplMaterials(bomContents));
                }
                catch (IOException e)
                {
                    log.debug("Could not load detailed license information for " + plugin.getName() +
                            " " + plugin.getKey() + " : " + this.getName() + " at " + this.getLicensesLocation(), e);
                }
                finally
                {
                    IOUtils.closeQuietly(resourceAsStream);
                }
            }
            else
            {
                log.debug("Could not locate detailed license information for " + plugin.getName() +
                        " " + plugin.getKey() + " : " + this.getName() + " at " + this.getLicensesLocation());
            }
        }
        return materials;
    }

    @VisibleForTesting
    String generateIntroduction(SoyTemplateRenderer soyTemplateRenderer, Map<String, Object> data)
    {
        return introductionOk() ? renderItem(soyTemplateRenderer, introduction, getIntroductionModuleKey(), data) : "";
    }

    @VisibleForTesting
    String generateConclusion(SoyTemplateRenderer soyTemplateRenderer, Map<String, Object> data)
    {
        return conclusionOk() ? renderItem(soyTemplateRenderer, conclusion, getConclusionModuleKey(), data) : "";
    }

    private boolean introductionOk()
    {
        return !isEmpty(introduction) && !isEmpty(introductionModule);
    }

    private boolean conclusionOk()
    {
        return !isEmpty(conclusion) && !isEmpty(conclusionModule);
    }

    @VisibleForTesting
    String renderItem(final SoyTemplateRenderer soyTemplateRenderer, final String item, final String itemModuleKey, Map<String, Object> data)
    {
        try
        {
            return soyTemplateRenderer.render(itemModuleKey, item, data);
        }
        catch (SoyException e)
        {
            log.info("Couldn't render 'about-license' for " + itemModuleKey + ". Continuing with other modules.", e);
        }
        catch (RuntimeException e)
        {
            log.info("Couldn't render 'about-license' for " + itemModuleKey + ". Continuing with other modules.", e);
        }

        return "";
    }

    private SoyTemplateRenderer getSoyRenderer()
    {
        return soyTemplateRendererProvider.getRenderer();
    }

    public String getIntroductionModuleKey()
    {
        return introductionModule;
    }

    public String getLicensesLocation()
    {
        return licenses;
    }

    public String getConclusionModuleKey()
    {
        return conclusionModule;
    }

    /**
     * Ties a bill of materials in with its plugins
     */
    public static final class PluginAndMaterials implements Comparable<PluginAndMaterials>
    {
        private final String pluginName;
        private final String pluginVersion;
        private final String introduction;
        private final String conclusion;
        private final List<Material> materials;

        public PluginAndMaterials(final String pluginName, final String pluginVersion, final String introduction, final String conclusion, final List<Material> materials)
        {
            this.pluginName = pluginName;
            this.pluginVersion = pluginVersion;
            this.introduction = introduction;
            this.conclusion = conclusion;
            this.materials = materials;
        }

        public String getPluginName()
        {
            return pluginName;
        }

        public String getPluginVersion()
        {
            return pluginVersion;
        }

        public String getIntroductionHtml()
        {
            return introduction;
        }

        public String getConclusionHtml()
        {
            return conclusion;
        }

        public List<Material> getMaterials()
        {
            return materials;
        }

        public boolean isEntries()
        {
            return materials != null && materials.size() > 0;
        }

        @Override
        public int compareTo(@Nonnull PluginAndMaterials pluginAndMaterials)
        {
            if (pluginAndMaterials == null) return 1;

            int compare = getPluginName().compareTo(pluginAndMaterials.getPluginName());
            if(compare != 0) return compare;

            compare = getPluginVersion().compareTo(pluginAndMaterials.getPluginVersion());
            if(compare != 0) return compare;

            compare = getIntroductionHtml().compareTo(pluginAndMaterials.getIntroductionHtml());
            if(compare != 0) return compare;

            compare = getConclusionHtml().compareTo(pluginAndMaterials.getConclusionHtml());
            if(compare != 0) return compare;

            return getMaterials().size() - pluginAndMaterials.getMaterials().size();
        }

    }

    /**
     * Encapsulates data from the Bill Of Materials
     */
    public static final class Material implements Comparable<Material>
    {
        private final String libraryName;
        private final String mavenInfo;
        private final String license;
        private final String url;
        private final String artifactType;

        public Material(final String libraryName, final String mavenInfo, final String license, final String url, final String artifactType)
        {
            this.libraryName = libraryName;
            this.mavenInfo = mavenInfo;
            this.license = license;
            this.url = url;
            this.artifactType = artifactType;
        }

        public String getLibraryName()
        {
            return libraryName == null ? "" : libraryName;
        }

        public String getMavenInfo()
        {
            return mavenInfo == null ? "" : mavenInfo;
        }

        public String getLicense()
        {
            return license == null ? "" : license;
        }

        public String getUrl()
        {
            return url == null ? "" : url;
        }

        public String getArtifactType()
        {
            return artifactType == null ? "" : artifactType;
        }

        public boolean isUrlAndGav()
        {
            return !(isEmpty(mavenInfo) || isEmpty(url));
        }

        public boolean isUrlNotGav()
        {
            return isEmpty(mavenInfo) && !isEmpty(url);
        }

        public boolean isGavNotUrl()
        {
            return !isEmpty(mavenInfo) && isEmpty(url);
        }

        public String toString()
        {
            return getLibraryName() + "," + getMavenInfo() + "," + getLicense() + "," + getUrl() + "," + getArtifactType();
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            final Material material = (Material) o;

            if (artifactType != null ? !artifactType.equals(material.artifactType) : material.artifactType != null)
            { return false; }
            if (libraryName != null ? !libraryName.equals(material.libraryName) : material.libraryName != null)
            {
                return false;
            }
            if (license != null ? !license.equals(material.license) : material.license != null) { return false; }
            if (mavenInfo != null ? !mavenInfo.equals(material.mavenInfo) : material.mavenInfo != null)
            {
                return false;
            }
            if (url != null ? !url.equals(material.url) : material.url != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = libraryName != null ? libraryName.hashCode() : 0;
            result = 31 * result + (mavenInfo != null ? mavenInfo.hashCode() : 0);
            result = 31 * result + (license != null ? license.hashCode() : 0);
            result = 31 * result + (url != null ? url.hashCode() : 0);
            result = 31 * result + (artifactType != null ? artifactType.hashCode() : 0);
            return result;
        }

        @Override
        public int compareTo(@Nonnull final Material material)
        {
            int result = this.getLibraryName().compareTo(material.getLibraryName());
            if (result != 0) { return result; }

            result = this.getMavenInfo().compareTo(material.getMavenInfo());
            if (result != 0) { return result; }

            result = this.getLicense().compareTo(material.getLicense());
            if (result != 0) { return result; }

            result = this.getUrl().compareTo(material.getUrl());
            if (result != 0) { return result; }

            return this.getArtifactType().compareTo(material.getArtifactType());
        }
    }
}
