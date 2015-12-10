package com.atlassian.jira.user.profile;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.VelocityParamFactory;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;

import java.util.Map;

import static com.atlassian.jira.template.TemplateSources.file;

/**
 * <p>Abstract class for {@link com.atlassian.jira.user.profile.UserProfileFragment} that helps writing fragments that
 * are rendered by velocity.</p>
 *
 * <p>It simply renders the template based of the fragment id and the implementing class can also
 * override {@link #createVelocityParams(User, User)}</p>
 *
 * @since v4.1
 */
public abstract class AbstractUserProfileFragment implements UserProfileFragment
{
    private static final Logger log = Logger.getLogger(AbstractUserProfileFragment.class);

    protected final JiraAuthenticationContext jiraAuthenticationContext;
    private final VelocityTemplatingEngine templatingEngine;
    private final VelocityParamFactory velocityParamFactory;

    public AbstractUserProfileFragment(final JiraAuthenticationContext jiraAuthenticationContext,
            final VelocityTemplatingEngine templatingEngine, final VelocityParamFactory velocityParamFactory)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.templatingEngine = templatingEngine;
        this.velocityParamFactory = velocityParamFactory;
    }

    /**
     * Whether or not we display this fragment. By default we do.
     *
     * @param profileUser The user whose profile the current user is looking at
     * @param currentUser The current user
     * @return true if we should disply this fragment, otherwise false
     */
    public boolean showFragment(final User profileUser, final User currentUser)
    {
        return true;
    }

    /**
     * Creates the HTML for this fragment.
     * <p/>
     * This implementation renders the template based off of the fragment id - {@link #getId()}
     *
     * @param profileUser The user whose profile the current user is looking at
     * @param currentUser The current user
     * @return The HTML of this fragment
     */
    public String getFragmentHtml(final User profileUser, final User currentUser)
    {
        final String template = getId() + ".vm";
        try
        {
            final Map<String, Object> velocityParams = createVelocityParams(profileUser, currentUser);

            return templatingEngine.render
                    (
                            file("templates/plugins/userprofile/" + template)
                    ).
                    applying(velocityParams).
                    asHtml();
        }
        catch (VelocityException e)
        {
            log.error("Error occurred while rendering velocity template for '" + "templates/plugins/userprofile/" + template + "'.", e);
            return "";
        }
    }

    /**
     * Creates the parameters passed to the velocity template.
     * <p/>
     * By default this contains "fragId", "profileUser", "currentUser"
     * <p/>
     * Implmentors of this abstract class can override this method to provide their own params.
     *
     * @param profileUser The user whose profile the current user is looking at
     * @param currentUser The current user
     * @return a map of the params passed to the velocity template.
     * @since v4.3
     */
    protected Map<String, Object> createVelocityParams(final User profileUser, final User currentUser)
    {
        final Map<String, Object> velocityParams = velocityParamFactory.getDefaultVelocityParams(jiraAuthenticationContext);
        velocityParams.put("fragid", getId());
        velocityParams.put("profileUser", profileUser);
        velocityParams.put("currentUser", currentUser);
        velocityParams.put("i18n", jiraAuthenticationContext.getI18nHelper());
        return velocityParams;
    }
}
