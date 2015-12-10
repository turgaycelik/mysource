package com.atlassian.jira.plugin.profile;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;

/**
 * Defines a "panel" of content that will be displayed on the view profile page, in the center. These panels will
 * be groups by named "tabs".
 *
 * @since v3.12
 */
@PublicSpi
public interface ViewProfilePanel
{
    /**
     * The default velocity template name that is used to render the view.  
     */
    public static final String VIEW_TEMPLATE = "view";

    /**
     * This method is called on plugin initialization and provides the module with a reference to the parent
     * module descriptor.
     * 
     * @param moduleDescriptor the controlling class that doles out this module.
     */
    void init(ViewProfilePanelModuleDescriptor moduleDescriptor);

    /**
     * Renders the html to be used in this profile panel.
     *
     * @param profileUser The user whose profile is being viewed.  May be null.
     * @return the html content.
     */
    String getHtml(User profileUser);
}
