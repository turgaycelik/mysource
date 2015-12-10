package com.atlassian.jira.action;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.util.profiling.UtilTimerStack;
import com.atlassian.velocity.htmlsafe.HtmlSafe;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericDelegator;

import webwork.action.ActionSupport;
import webwork.action.CommandDriven;
import webwork.dispatcher.ActionResult;
import webwork.util.editor.PropertyEditorException;

/**
 * This class sits just above the base Webwork {@link ActionSupport} class and provides JIRA specific code to all our
 * actions.
 * <p/>
 * Its main capabilities are extra error handling and I18n support
 * @deprecated since 6.1 Please use subclass instead {@link com.atlassian.jira.web.action.JiraWebActionSupport}. This class is subject of removal.
 */
@PublicSpi
@Deprecated
public abstract class JiraActionSupport extends ActionSupport implements CommandDriven, I18nHelper
{
    protected final Logger log = Logger.getLogger(this.getClass());

    private GenericDelegator delegator;
    private OfBizDelegator ofBizDelegator;
    private Preferences userPrefs;
    private I18nHelper i18nHelperDelegate;

    protected JiraActionSupport()
    {
    }

    /**
     * @return The logged in user.
     * @deprecated since 6.1 use {@link com.atlassian.jira.web.action.JiraWebActionSupport#getLoggedInApplicationUser()} instead.
     */
    @Deprecated
    public abstract User getLoggedInUser();

    /**
     * @return The logged in user.
     * @deprecated Use {@link #getLoggedInUser()} instead. Since v4.3. To be deleted in v6.0
     */
    public User getRemoteUser()
    {
        return getLoggedInUser();
    }

    /**
     * This can be called to get a component from the {@link ComponentAccessor}.  Override this if you
     * wish to change this behaviour say in unit tests.
     *
     * @param clazz the component class in question
     * @return the component instance
     */
    protected <T> T getComponentInstanceOfType(final Class<T> clazz)
    {
        return ComponentAccessor.getComponentOfType(clazz);
    }

    @Override
    public String execute() throws Exception
    {
        final String logLine = getActionName() + ".execute()";
        UtilTimerStack.push(logLine);
        try
        {
            return super.execute();
        }
        finally
        {
            UtilTimerStack.pop(logLine);
        }
    }

    public OfBizDelegator getOfBizDelegator()
    {
        if (ofBizDelegator == null)
        {
            ofBizDelegator = ComponentAccessor.getComponent(OfBizDelegator.class);
        }

        return ofBizDelegator;
    }

    /**
     * Gets GenericDelegator.
     * @return GenericDelegator
     *
     * @deprecated Use {@link #getOfBizDelegator()} instead. Since v5.0.
     */
    public GenericDelegator getDelegator()
    {
        if (delegator == null)
        {
            delegator = (GenericDelegator) ComponentAccessor.getComponent(DelegatorInterface.class);
        }

        return delegator;
    }

    public ApplicationProperties getApplicationProperties()
    {
        return getComponentInstanceOfType(ApplicationProperties.class);
    }

    /**
     * @deprecated Get this injected into your action instead. Since v6.0.
     */
    public WatcherManager getWatcherManager()
    {
        return getComponentInstanceOfType(WatcherManager.class);
    }

    /**
     * @return The name of this action - the unqualified class name.
     */
    @Override
    public final String getActionName()
    {
        final String classname = getClass().getName();
        return classname.substring(classname.lastIndexOf('.') + 1);
    }

    /**
     * Get a definitive result. Returns {@link webwork.action.Action#ERROR} if there are error messages, otherwise
     * {@link webwork.action.Action#SUCCESS}.
     *
     * @return {@link webwork.action.Action#ERROR} or {@link webwork.action.Action#SUCCESS}
     */
    public String getResult()
    {
        return invalidInput() ? ERROR : SUCCESS;
    }

    public void addErrorMessages(final Collection<String> errorMessages)
    {
        if (errorMessages == null)
        {
            return;
        }
        for (final String errorMessage : errorMessages)
        {
            addErrorMessage(errorMessage);
        }
    }

    public void addErrors(final Map<String, String> errors)
    {
        if (errors == null)
        {
            return;
        }
        for (final Map.Entry<String, String> mapEntry : errors.entrySet())
        {
            final String name = mapEntry.getKey();
            final String error = mapEntry.getValue();
            addError(name, error);
        }
    }

    public boolean hasAnyErrors()
    {
        return !getErrors().isEmpty() || !getErrorMessages().isEmpty();
    }

    public void addErrorMessages(final ActionResult aResult)
    {
        if (!SUCCESS.equals(aResult.getResult()))
        {
            final ActionSupport actionSupport = (ActionSupport) aResult.getFirstAction();
            //noinspection unchecked
            addErrorMessages(actionSupport.getErrorMessages());
        }
    }

    public boolean isIndexing()
    {
        return true;
    }

    /**
     * Override this method from ActionSupport.  Body is copied from there, with the exception of a clause that prevents
     * JRA-7245
     */
    @Override
    public void addIllegalArgumentException(final String fieldName, final IllegalArgumentException e)
    {
        String msg = e.getMessage();
        if (e instanceof PropertyEditorException)
        {
            msg = getPropertyEditorMessage(fieldName, (PropertyEditorException) e);
        }
        if ((msg != null) && msg.startsWith("missing matching end quote"))
        {
            //ignore this message - it is most likely because of JRA-7245 / WW-801
        }
        else
        {
            addError(fieldName, msg);
        }
    }

    public Preferences getUserPreferences()
    {
        if (userPrefs == null)
        {
            userPrefs = getComponentInstanceOfType(UserPreferencesManager.class).getPreferences(getLoggedInUser());
        }
        return userPrefs;
    }

    /**
     * Checks if descriptorParams contains key and removes it, otherwise adds the error message with the given message
     * key.
     *
     * @param params the map of parameters
     * @param key the param key to remove.
     * @param messageKey the error.
     */
    protected void removeKeyOrAddError(final Map params, final String key, final String messageKey)
    {
        if (params.containsKey(key))
        {
            params.remove(key);
        }
        else
        {
            addErrorMessage(getText(messageKey));
        }

    }

    /**
     * ===================================================================================
     * <p/>
     * I18nHelper related code
     * <p/>
     * ===================================================================================
     */

    /**
     * @return the {@link I18nHelper} associated with this action
     */
    protected I18nHelper getI18nHelper()
    {
        if (i18nHelperDelegate == null)
        {
            i18nHelperDelegate = getComponentInstanceOfType(JiraAuthenticationContext.class).getI18nHelper();
        }
        return i18nHelperDelegate;
    }

    @Override
    public Set<String> getKeysForPrefix(String prefix)
    {
        throw new UnsupportedOperationException("This method should only be called via the I18nBean and is only required for SAL.");
    }

    @Override
    public ResourceBundle getDefaultResourceBundle()
    {
        return getI18nHelper().getDefaultResourceBundle();
    }

    @Override
    public ResourceBundle getResourceBundle()
    {
        return getI18nHelper().getResourceBundle();
    }

    @Override
    public Locale getLocale()
    {
        return getI18nHelper().getLocale();
    }

    @Override
    public String getUnescapedText(String key)
    {
        return getI18nHelper().getUnescapedText(key);
    }

    @Override
    public String getUntransformedRawText(String key)
    {
        return getI18nHelper().getUntransformedRawText(key);
    }

    @Override
    public boolean isKeyDefined(String key)
    {
        return getI18nHelper().isKeyDefined(key);
    }

    @Override
    @HtmlSafe
    public String getText(String key)
    {
        return getI18nHelper().getText(key);
    }

    @Override
    @HtmlSafe
    public String getText(String key, String value1)
    {
        return getI18nHelper().getText(key, value1);
    }

    @Override
    @HtmlSafe
    public String getText(String key, String value1, String value2)
    {
        return getI18nHelper().getText(key, value1, value2);
    }

    @Override
    @HtmlSafe
    public String getText(String key, String value1, String value2, String value3)
    {
        return getI18nHelper().getText(key, value1, value2, value3);
    }

    @Override
    @HtmlSafe
    public String getText(String key, String value1, String value2, String value3, String value4)
    {
        return getI18nHelper().getText(key, value1, value2, value3, value4);
    }

    @Override
    @HtmlSafe
    public String getText(String key, Object value1, Object value2, Object value3)
    {
        return getI18nHelper().getText(key, value1, value2, value3);
    }

    @Override
    @HtmlSafe
    public String getText(String key, Object value1, Object value2, Object value3, Object value4)
    {
        return getI18nHelper().getText(key, value1, value2, value3, value4);
    }

    @Override
    @HtmlSafe
    public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5)
    {
        return getI18nHelper().getText(key, value1, value2, value3, value4, value5);
    }

    @Override
    @HtmlSafe
    public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6)
    {
        return getI18nHelper().getText(key, value1, value2, value3, value4, value5, value6);
    }

    @Override
    @HtmlSafe
    public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6, Object value7)
    {
        return getI18nHelper().getText(key, value1, value2, value3, value4, value5, value6, value7);
    }

    @Override
    @HtmlSafe
    public String getText(String key, String value1, String value2, String value3, String value4, String value5, String value6, String value7)
    {
        return getI18nHelper().getText(key, value1, value2, value3, value4, value5, value6, value7);
    }

    @Override
    @HtmlSafe
    public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6, Object value7, Object value8)
    {
        return getI18nHelper().getText(key, value1, value2, value3, value4, value5, value6, value7, value8);
    }

    @Override
    @HtmlSafe
    public String getText(String key, String value1, String value2, String value3, String value4, String value5, String value6, String value7, String value8, String value9)
    {
        return getI18nHelper().getText(key, value1, value2, value3, value4, value5, value6, value7, value8, value9);
    }

    @Override
    @HtmlSafe
    public String getText(String key, Object parameters)
    {
        return getI18nHelper().getText(key, parameters);
    }
}
