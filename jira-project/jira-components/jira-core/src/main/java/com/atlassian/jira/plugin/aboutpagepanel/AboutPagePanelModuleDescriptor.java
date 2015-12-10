package com.atlassian.jira.plugin.aboutpagepanel;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;

/**
 * Defines a plugin point for displaying information on the application About page. At least one of introduction,
 * licenses-location, and conclusion must be defined; multiple entries will be displayed in that order.
 * <h3>Example</h3>
 *
 * <pre>
 * <web-resource key="about-introduction">
 *   <resource type="soy" name="about-introduction.soy" location="about-introduction.soy"/>
 * </web-resource>
 *
 * <web-resource key="about-conclusion">
 *   <resource type="soy" name="about-conclusion.soy" location="about-conclusion.soy"/>
 * </web-resource>
 *
 * <about-page-panel name="about-page-section-myplugin" key="about-page-section-myplugin">
 *   <introduction module-key="${project.groupId}.${project.artifactId}:about-introduction" function="about.introduction"/>
 *   <licenses location="bom.csv"/>
 *   <conclusion module-key="${project.groupId}.${project.artifactId}:about-conclusion" function="about.conclusion"/>
 * </about-page-panel>
 * </pre>
 *
 * <p>The two web resources define the locations of the templates for the <em>introduction</em> and <em>conclusion</em>
 * sections. In most cases these are <em>necessary</em> and should give a path to a soy file in your plugin.</p>
 * <p>The <em>module-key</em>s in the <em>introduction</em> and <em>conclusion</em> elements define the modules from
 * which to load the templates. These are typically the plugin key, followed by a colon (:), then followed by the key of
 * module defining the templates to use.</p>
 * <p>The <em>function</em> defines the function that is to be called from the template file located at the
 * <em>module-key.</em></p>
 * <p>The <em>licenses location</em> element is a path to the bom.csv file inside the plugin.</p>
 * @since 6.0.2
 */
@ExperimentalApi
public interface AboutPagePanelModuleDescriptor extends JiraResourcedModuleDescriptor<Void>
{

    /**
     * Gets the rendered HTML for this plugin, to be displayed on the About page.
     *
     * @return the rendered HTML for this plugin
     */
    String getPluginSectionHtml();
}
