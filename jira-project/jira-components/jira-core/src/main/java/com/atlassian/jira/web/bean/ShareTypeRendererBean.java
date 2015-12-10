package com.atlassian.jira.web.bean;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.sharing.type.ShareTypeRenderer;
import com.atlassian.jira.sharing.type.ShareTypeRenderer.RenderMode;
import com.atlassian.jira.util.dbc.Assertions;

import java.util.Map;

/**
 * Helper class that is used to render share types.
 * 
 * @since v3.13
 */
public class ShareTypeRendererBean
{
    private final JiraAuthenticationContext authenticationContext;
    private final ShareTypeRenderer renderer;
    private final ShareType.Name shareTypeName;
    private final RenderMode renderMode;
    private final TypeDescriptor typeDescriptor;

    public ShareTypeRendererBean(final ShareType shareType, final JiraAuthenticationContext authenticationContext, final com.atlassian.jira.sharing.type.ShareTypeRenderer.RenderMode renderMode, final TypeDescriptor typeDescriptor)
    {
        Assertions.notNull("shareType", shareType);
        Assertions.notNull("authenticationContext", authenticationContext);
        Assertions.notNull("renderMode", renderMode);
        Assertions.notNull("typeDescriptor", typeDescriptor);

        this.typeDescriptor = typeDescriptor;
        this.authenticationContext = authenticationContext;
        this.renderMode = renderMode;
        this.shareTypeName = shareType.getType();
        this.renderer = shareType.getRenderer();
    }

    public String getShareType()
    {
        return shareTypeName.get();
    }

    /**
     * Return the HTML of the component that is used to configure the ShareType when necessary.
     * 
     * @return The HTML component to render the ShareType.
     */

    public String getShareTypeEditor()
    {
        return renderer.getShareTypeEditor(authenticationContext);
    }

    /**
     * Return the HTML of the component that is used to select the ShareType instance when necessary.
     * 
     * @return The HTML component to render the ShareType selector.
     */
    public String getShareTypeSelector()
    {
        return renderer.getShareTypeEditor(authenticationContext);
    }

    /**
     * Return whether or not the component needs a button.
     * 
     * @return true if the HTML component needs a button or false otherwise.
     */

    public boolean isAddButtonNeeded()
    {
        return renderer.isAddButtonNeeded(authenticationContext);
    }

    /**
     * Return a string that can be used to differentiate the associated ShareType. This string is used in the web component that selects a particular
     * ShareType.
     * 
     * @return A ShareType description.
     */
    public String getShareTypeLabel()
    {
        return renderer.getShareTypeLabel(authenticationContext);
    }

    /**
     * Return a map of key-> message templates for inclusion in the Javascript that renders the shares.
     *
     * @return map of key->message for inclusion in the Javascript that renders the shares.
     */
    public Map /*<String, String>*/ getTranslatedTemplates()
    {
        return renderer.getTranslatedTemplates(authenticationContext, typeDescriptor, renderMode);
    }
}
