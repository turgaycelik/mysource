package com.atlassian.jira.dev.reference.plugin.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.user.PreDeleteUserErrors;
import com.atlassian.jira.plugin.user.WebErrorMessage;
import com.atlassian.jira.plugin.user.WebErrorMessageImpl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides basic example of the pre-delete-user-errors plugin point
 *
 * @since 6.0
 */
public class ReferencePreDeleteUserErrors implements PreDeleteUserErrors
{
    private static final String DESCRIPTION = "Reference User should never be removed - this has been provided by the reference plugin";
    private static final String SNIPPET = "Entity: 17 entities - this has been provided by the reference plugin";
    private URI furtherInformationURI = null;

    public ReferencePreDeleteUserErrors()
    {
        try
        {
            this.furtherInformationURI = new URI("http://www.atlassian.com");
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public List<WebErrorMessage> getPreDeleteUserErrors(final User user)
    {
        List<WebErrorMessage> webErrorMessages = new ArrayList<WebErrorMessage>();
        if (user.getName().equals("predeleteuser"))
        {
            webErrorMessages.add(new WebErrorMessageImpl(DESCRIPTION, SNIPPET, furtherInformationURI));
        }
        return webErrorMessages;
    }
}
