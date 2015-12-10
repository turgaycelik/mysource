package com.atlassian.jira.admin;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.plugin.renderer.JiraRendererModuleDescriptor;
import com.atlassian.velocity.VelocityManager;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.velocity.exception.VelocityException;

import javax.annotation.Nullable;
import java.util.Map;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

/**
 * Implementation of RenderableProperty whose behaviour is driven by the {@link com.atlassian.jira.config.CoreFeatures#ON_DEMAND ON_DEMAND}
 * flag.
 *
 * @since v5.0.7
 */
@Internal
public final class RenderablePropertyImpl implements RenderableProperty
{
    private static final String EDIT_VM = "renderable-property-edit.vm";

    private final String templateDirectory = getClass().getPackage().getName().replace('.', '/');
    private final ApplicationProperties applicationProperties;
    private final PropertyPersister propertyPersister;
    private final PropertyDescriptions propertyDescription;
    private final FeatureManager featureManager;
    private final VelocityManager velocityManager;
    private final RendererManager rendererManager;

    /**
     * Creates a new RenderableApplicationProperty that persists the property value using the given {@link
     * PropertyPersister} and {@link PropertyDescriptions}.
     *
     * @param applicationProperties an ApplicationProperties
     * @param featureManager a FeatureManager
     * @param velocityManager a VelocityManager
     * @param rendererManager a RendererManager
     * @param propertyPersister a PropertyPersister
     * @param propertyDescriptions an option PropertyDescriptions
     */
    RenderablePropertyImpl(ApplicationProperties applicationProperties, FeatureManager featureManager, VelocityManager velocityManager, RendererManager rendererManager, PropertyPersister propertyPersister, @Nullable PropertyDescriptions propertyDescriptions)
    {
        this.applicationProperties = applicationProperties;
        this.featureManager = featureManager;
        this.velocityManager = velocityManager;
        this.rendererManager = rendererManager;
        this.propertyPersister = propertyPersister;
        this.propertyDescription = propertyDescriptions != null ? propertyDescriptions : new EmptyDescriptions();
    }

    /**
     * Returns the view HTML for this renderable application property.
     *
     * @return a String containing the view HTML
     */
    @Override
    public final String getViewHtml()
    {
        if (isOnDemand())
        {
            return rendererManager.getRenderedContent(AtlassianWikiRenderer.RENDERER_TYPE, getValue(), null);
        }

        // no OnDemand == no encoding
        return getValue();
    }

    /**
     * Returns the edit HTML for this renderable application property.
     *
     * @param fieldName the field name to use in the generated HTML
     * @return a String containing the edit HTML
     */
    @Override
    public final String getEditHtml(String fieldName)
    {
        if (isOnDemand())
        {
            JiraRendererModuleDescriptor rendererDescriptor = rendererManager.getRendererForType(AtlassianWikiRenderer.RENDERER_TYPE).getDescriptor();
            Map<String, String> renderParams = Maps.newHashMap(ImmutableMap.of(
                    "rows", "10",
                    "cols", "60",
                    "wrap", "virtual",
                    "class", "long-field"
            ));

            return rendererDescriptor.getEditVM(getValue(), null, AtlassianWikiRenderer.RENDERER_TYPE, fieldName, fieldName, renderParams, false);
        }

        return renderTemplate(EDIT_VM, new Context(fieldName));
    }

    /**
     * Returns the description HTML for this property.
     *
     * @return a String containing the description HTML
     */
    @Override
    public final String getDescriptionHtml()
    {
        if (isOnDemand())
        {
            return propertyDescription.getOnDemandDescriptionHtml();
        }

        return propertyDescription.getBtfDescriptionHtml();
    }

    /**
     * Returns the property value in raw format.
     *
     * @return the property value in raw format
     */
    public final String getValue()
    {
        return propertyPersister.load();
    }

    /**
     * Sets the property value.
     *
     * @param value a String containing the property value
     */
    public final void setValue(String value)
    {
        propertyPersister.save(value);
    }

    /**
     * Returns a boolean indicating whether JIRA is running in OnDemand.
     *
     * @return a boolean indicating whether JIRA is running in OnDemand
     * @see #isBTF()
     */
    protected final boolean isOnDemand()
    {
        return featureManager.isOnDemand();
    }

    /**
     * Returns a boolean indicating whether JIRA is running in BTF.
     *
     * @return a boolean indicating whether JIRA is running in BTF
     * @see #isOnDemand()
     */
    protected final boolean isBTF()
    {
        return !isOnDemand();
    }

    private String renderTemplate(String template, Context context)
    {
        String templatePath = templateDirectory + "/" + template;
        try
        {
            return velocityManager.getEncodedBody(templatePath, "", applicationProperties.getEncoding(), ImmutableMap.<String,Object>of("property", context));
        }
        catch (VelocityException e)
        {
            throw new RuntimeException("Error rendering " + templatePath, e);
        }
    }

    public class Context
    {
        private final String fieldName;

        public Context(String fieldName)
        {
            this.fieldName = fieldName;
        }

        public String getFieldName()
        {
            return fieldName;
        }

        public String getValueHtml()
        {
            return escapeHtml(getValue());
        }
    }

    /**
     * Default PropertyDescription implementation.
     */
    private static class EmptyDescriptions implements PropertyDescriptions
    {
        @Override
        public String getOnDemandDescriptionHtml()
        {
            return "";
        }

        @Override
        public String getBtfDescriptionHtml()
        {
            return "";
        }
    }
}
