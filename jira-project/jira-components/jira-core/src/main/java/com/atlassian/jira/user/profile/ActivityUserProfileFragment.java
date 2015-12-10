package com.atlassian.jira.user.profile;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetRequestContextFactory;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.view.GadgetViewFactory;
import com.atlassian.gadgets.view.ModuleId;
import com.atlassian.gadgets.view.View;
import com.atlassian.gadgets.view.ViewType;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.plugin.PluginAccessor;
import webwork.action.ActionContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;

/**
 * Creates a fragment containing the User's activity stream
 *
 * @since v4.1
 */
public class ActivityUserProfileFragment implements UserProfileFragment
{
    private static final String ACTIVTY_STREAM_GADGET_MODULE_KEY = "com.atlassian.streams.streams-jira-plugin:activitystream-gadget";
    private static final String PREF_IS_CONFIGURED = "isConfigured";
    private static final String GADGET_URI = "rest/gadgets/1.0/g/com.atlassian.streams.streams-jira-plugin/gadgets/activitystream-gadget.xml";
    private static final String PREF_NUMOFENTRIES = "numofentries";
    private static final String NUMOFENTRIES = "10";
    private static final String PREFS_IS_CONFIGURABLE = "isConfigurable";
    private static final String PREF_TITLE_REQUIRED = "titleRequired";
    private static final String PREF_USERNAME = "username";

    private final PluginAccessor pluginAccessor;
    private final GadgetRequestContextFactory gadgetRequestContextFactory;
    private final I18nBean.BeanFactory i18nFactory;

    public ActivityUserProfileFragment(PluginAccessor pluginAccessor, final GadgetRequestContextFactory gadgetRequestContextFactory, I18nBean.BeanFactory i18nFactory)
    {
        this.pluginAccessor = pluginAccessor;
        this.gadgetRequestContextFactory = gadgetRequestContextFactory;
        this.i18nFactory = i18nFactory;
    }

    /**
     * We only display this is both the Activity Stream Plugin and Gadget Plugin are installed
     *
     * @param profileUser The user whose profile the current user is looking at
     * @param currentUser The current user
     * @return true if both the Activity Stream Plugin and Gadget Plugin are installed, otherwise false
     * @since v4.3
     */
    public boolean showFragment(User profileUser, User currentUser)
    {
        //need to both have the gadget plugin as well as the activity stream gadget available!
        final GadgetViewFactory viewFactory = ComponentAccessor.getOSGiComponentInstanceOfType(GadgetViewFactory.class);
        return viewFactory != null && pluginAccessor.isPluginModuleEnabled(ACTIVTY_STREAM_GADGET_MODULE_KEY);
    }

    /**
     * Get the HTML Fragement.
     * @param profileUser The user whose profile the current user is looking at
     * @param currentUser The current user
     * @return html
     * @since v4.3
     */
    public String getFragmentHtml(User profileUser, User currentUser)
    {
        final GadgetViewFactory viewFactory = ComponentAccessor.getOSGiComponentInstanceOfType(GadgetViewFactory.class);

        final MapBuilder<String, String> prefsBuilder = MapBuilder.newBuilder();
        prefsBuilder.add(PREF_IS_CONFIGURED, Boolean.TRUE.toString());
        prefsBuilder.add(PREFS_IS_CONFIGURABLE, Boolean.FALSE.toString());
        prefsBuilder.add(PREF_NUMOFENTRIES, NUMOFENTRIES);
        prefsBuilder.add(PREF_TITLE_REQUIRED, Boolean.FALSE.toString());
        prefsBuilder.add(PREF_USERNAME, escapeArgument(profileUser.getName()));

        final GadgetState gadget = GadgetState.gadget(GadgetId.valueOf("1")).specUri(URI.create(GADGET_URI)).userPrefs(prefsBuilder.toMap()).build();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final Writer gadgetWriter = new OutputStreamWriter(baos);
        final GadgetRequestContext requestContext = gadgetRequestContextFactory.get(ActionContext.getRequest());
        final View settings = new View.Builder().viewType(ViewType.DEFAULT).writable(false).build();
        try
        {
            viewFactory.createGadgetView(gadget, ModuleId.valueOf(1L), settings, requestContext).writeTo(gadgetWriter);
            gadgetWriter.flush();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        final I18nHelper i18n = i18nFactory.getInstance(currentUser);
        return "<div class=\"mod-header\"><h3>" + i18n.getText("common.concepts.activity.stream") + "</h3></div><div class=\"mod-content\">" + baos.toString() + "</div>";
    }

    public String getId()
    {
        return "activity-profile-fragment";
    }

    /**
     * Escapes the given argument as necessary.
     *
     * @param argument the argument to be escaped
     * @return the escaped argument
     */
    private static String escapeArgument(String argument)
    {
        return argument.replace("_", "\\_").replace(' ', '_');
    }

}
