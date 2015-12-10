package com.atlassian.jira.sharing.type;

import com.atlassian.jira.config.util.EncodingConfiguration;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.dbc.Assertions;
import org.apache.velocity.exception.VelocityException;

import java.util.Map;

import static com.atlassian.jira.template.TemplateSources.file;

/**
 * A partial implementation of {@link com.atlassian.jira.sharing.type.ShareTypeRenderer} that can be used to generate
 * HTML using Velocity templates.
 *
 * @since v3.13
 */

public abstract class VelocityShareTypeRenderer implements ShareTypeRenderer
{
    private static final String I18N_KEY = "i18n";
    private static final String RENDERER_KEY = "renderer";
    private static final String TEMPLATE_DIRECTORY_PATH = "templates/jira/sharing/";

    private final VelocityTemplatingEngine templatingEngine;

    public VelocityShareTypeRenderer(final EncodingConfiguration encoding, final VelocityTemplatingEngine templatingEngine)
    {
        Assertions.notNull("encoding", encoding);
        Assertions.notNull("velocityManager", templatingEngine);

        this.templatingEngine = templatingEngine;
    }

    /**
     * Get the text returned by running a velocity template. This can be used to create the view/editor of a ShareType from a Velocity template.
     *
     * @param template the filename of the velocity template to run. It is assumed to be located under 'templates/jira/sharing/'.
     * @param params the parameters that should be passed to the velocity template.
     * @param authenticationContext the authentication context the template should run under.
     * @return the text of the velocity template.
     */
    protected final String renderVelocity(final String template, final Map<String, Object> params, final JiraAuthenticationContext authenticationContext)
    {
        @SuppressWarnings("unchecked")
        final Map<String, Object> velocityParams = (Map<String, Object>) addDefaultVelocityParameters(params, authenticationContext);
        velocityParams.put(VelocityShareTypeRenderer.RENDERER_KEY, this);
        if (!velocityParams.containsKey(VelocityShareTypeRenderer.I18N_KEY))
        {
            velocityParams.put(VelocityShareTypeRenderer.I18N_KEY, authenticationContext.getI18nHelper());
        }
        try
        {
            return templatingEngine.render(file(TEMPLATE_DIRECTORY_PATH + template)).applying(velocityParams).asHtml();
        }
        catch (final VelocityException e)
        {
            throw new RuntimeException(e);
        }
    }

    Map<String, ?> addDefaultVelocityParameters(final Map<String, Object> params, final JiraAuthenticationContext authCtx)
    {
        return JiraVelocityUtils.getDefaultVelocityParams(params, authCtx);
    }
}
