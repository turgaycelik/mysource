package com.atlassian.jira.plugin.keyboardshortcut;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.plugin.webfragment.DefaultWebFragmentContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.Users;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.webresource.WebResourceIntegration;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.jira.plugin.keyboardshortcut.CachingKeyboardShortcutManager.ShortcutCacheKey.makeCacheKey;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;

@EventComponent
public class CachingKeyboardShortcutManager implements KeyboardShortcutManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CachingKeyboardShortcutManager.class);
    private static final String REST_PREFIX = "/rest/api/1.0/shortcuts/";
    private static final String REST_RESOURCE = "/shortcuts.js";

    private static final String REQUEST_CACHE_KEY = "keyboard.shortcuts.contexts";

    private final PluginShortcuts shortcuts = new PluginShortcuts();
    private final BuildUtilsInfo buildUtilsInfo;
    private final PluginAccessor pluginAccessor;
    private final WebResourceIntegration webResourceIntegration;
    private final JiraAuthenticationContext authenticationContext;
    private final UserPreferencesManager userPreferencesManager;


    public CachingKeyboardShortcutManager(final BuildUtilsInfo buildUtilsInfo,
            final PluginAccessor pluginAccessor, final WebResourceIntegration webResourceIntegration, final JiraAuthenticationContext authenticationContext, final UserPreferencesManager userPreferencesManager)
    {
        this.buildUtilsInfo = buildUtilsInfo;
        this.pluginAccessor = pluginAccessor;
        this.webResourceIntegration = webResourceIntegration;
        this.authenticationContext = authenticationContext;
        this.userPreferencesManager = userPreferencesManager;
    }

    @EventListener
    @SuppressWarnings ("UnusedParameters")
    public void onClearCache(ClearCacheEvent event)
    {
        final List<KeyboardShortcutModuleDescriptor> descriptors =
                pluginAccessor.getEnabledModuleDescriptorsByClass(KeyboardShortcutModuleDescriptor.class);

        // re-init the shortcut list with the descriptors that are currently active
        shortcuts.reInit(descriptors);
    }

    public void registerShortcut(final String pluginModuleKey, final KeyboardShortcut shortcut)
    {
        shortcuts.register(pluginModuleKey, shortcut);
    }

    public void unregisterShortcut(final String pluginModuleKey)
    {
        shortcuts.unregister(pluginModuleKey);
    }

    @Override
    public List<KeyboardShortcut> getActiveShortcuts()
    {
        return listActiveShortcutsUniquePerContext(Collections.<String, Object>emptyMap());
    }

    @Override
    public List<KeyboardShortcut> listActiveShortcutsUniquePerContext(final Map<String, Object> userContext)
    {

        final Collection<KeyboardShortcut> filter = filter(getAllShortcuts(), new Predicate<KeyboardShortcut>()
        {
            @Override
            public boolean apply(final KeyboardShortcut keyboardShortcut)
            {
                try
                {
                    return keyboardShortcut.shouldDisplay(userContext);
                }
                catch (RuntimeException e)
                {
                    LOGGER.warn("failed to evaluate the conditions of a keyboard shortcut: " + keyboardShortcut, e);
                    return false;
                }
            }
        });
        return eliminateShadowedShortcutsPerContext(new LinkedList<KeyboardShortcut>(filter));
    }

    public List<KeyboardShortcut> getAllShortcuts()
    {
        return shortcuts.getAll();
    }

    public void requireShortcutsForContext(final Context context)
    {
        getRequiredContexts().add(context);
    }

    /**
     * Returns the given list of shortcuts minus shadowed shortcuts. A shortcut is shadowed if if shares a common prefix
     * with another shortcut that has a higher precedence (i.e. a higher "order" in the &lt;keyboard-shortcut&gt;
     * element in the plugin XML) and has the same context.
     *
     * @param keyboardShortcuts an ordered List of shortcuts
     * @return the given list of shortcuts with shadowed shortcuts removed
     */
    private List<KeyboardShortcut> eliminateShadowedShortcutsPerContext(final List<KeyboardShortcut> keyboardShortcuts)
    {
        // use a linked hash map to preserve the shortcut order
        LinkedHashMap<ShortcutCacheKey, KeyboardShortcut> activeShortcuts = Maps.newLinkedHashMap();

        // add each shortcut one by one, removing any existing (lower order) shortcuts that conflict
        for (KeyboardShortcut addingShortcut : keyboardShortcuts)
        {
            ShortcutCacheKey addingKey = makeCacheKey(addingShortcut);

            for (ShortcutCacheKey existingKeys : activeShortcuts.keySet())
            {
                KeyboardShortcut existingShortcut = activeShortcuts.get(existingKeys);
                if (addingKey.conflictsWith(existingKeys))
                {
                    activeShortcuts.remove(existingKeys);
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug(String.format("Keyboard shortcut %s ('%s') is shadowed by %s ('%s') in context %s",
                                addingShortcut.getDescriptionI18nKey(),
                                addingShortcut.getShortcuts(),
                                existingShortcut.getDescriptionI18nKey(),
                                existingShortcut.getShortcuts(),
                                existingShortcut.getContext())
                        );
                    }

                    // there's at most 1 conflicting so don't bother checking the rest of the set
                    break;
                }
            }

            activeShortcuts.put(addingKey, addingShortcut);
        }

        // the values are still sorted because we use a LinkedHashMap
        return Lists.newArrayList(activeShortcuts.values());
    }

    private Set<Context> getRequiredContexts()
    {
        final Map<String, Object> requestCache = webResourceIntegration.getRequestCache();
        @SuppressWarnings ("unchecked")
        Set<Context> requiredContexts = (Set<Context>) requestCache.get(REQUEST_CACHE_KEY);
        if (requiredContexts == null)
        {
            requiredContexts = new LinkedHashSet<Context>();
            requestCache.put(REQUEST_CACHE_KEY, requiredContexts);
        }
        return requiredContexts;
    }

    public String includeShortcuts()
    {
        UrlBuilder url = new UrlBuilder(createUrl());

        for (Context context : getRequiredContexts())
        {
            url.addParameterUnsafe("context", context.toString());
        }
        return url.asUrlString();
    }

    private String createUrl()
    {
        MessageDigest messageDigest = getMessageDigest("MD5");
        if (messageDigest == null)
        {
            messageDigest = getMessageDigest("SHA");
        }
        if (messageDigest == null)
        {
            throw new RuntimeException("Unable to retrieve a valid message digest!");
        }
        messageDigest.update(getValueForHash().getBytes());
        final byte[] digest = messageDigest.digest();
        final BigInteger bigInt = new BigInteger(1, digest);
        final String hash = bigInt.toString(16);
        //include the buildnumber as well as the hash of the keyboard shortcuts, to ensure that when JIRA is upgraded
        //a new keyboard shortcut js file will be served regardless of if the hashcode has changed.
        return REST_PREFIX + buildUtilsInfo.getCurrentBuildNumber() + "/" + hash + REST_RESOURCE;
    }

    private MessageDigest getMessageDigest(String digestName)
    {
        try
        {
            return MessageDigest.getInstance(digestName);
        }
        catch (NoSuchAlgorithmException e)
        {
            return null;
        }
    }

    private String getValueForHash()
    {
        List<KeyboardShortcut> shortcuts = getAllShortcuts();

        Map<String, Object> context = getWebFragmentContext();

        StringBuilder sb = new StringBuilder();
        for (KeyboardShortcut shortcut : shortcuts)
        {
            sb.append(shortcut).append(shortcut.shouldDisplay(context));
        }

        return sb.toString();
    }

    Map<String, Object> getWebFragmentContext()
    {
        return DefaultWebFragmentContext.get();
    }

    public boolean isKeyboardShortcutsEnabled()
    {
        ApplicationUser user = authenticationContext.getUser();
        if (Users.isAnonymous(user))
        {
            return true;
        }

        Preferences userPrefs = userPreferencesManager.getExtendedPreferences(user);
        return !userPrefs.getBoolean(PreferenceKeys.USER_KEYBOARD_SHORTCUTS_DISABLED);
    }

    final static class ShortcutCacheKey
    {
        public static ShortcutCacheKey makeCacheKey(@Nonnull KeyboardShortcut shortcut)
        {
            notNull("shortcut", shortcut);
            return new ShortcutCacheKey(shortcut.getContext(), shortcut.getShortcuts());
        }

        final Context context;
        final ImmutableSet<ImmutableList<String>> shortcuts;

        private ShortcutCacheKey(Context context, Set<List<String>> shortcuts)
        {
            this.context = context;
            this.shortcuts = makeImmutable(shortcuts);
        }

        public boolean conflictsWith(ShortcutCacheKey that)
        {
            boolean contextEquals = Objects.equal(this.context, that.context);
            if (contextEquals)
            {
                for (ImmutableList<String> thisShortcut : this.shortcuts)
                {
                    String thisKeys = StringUtils.join(thisShortcut, "");
                    for (ImmutableList<String> thatShortcut : that.shortcuts)
                    {
                        String thatKeys = StringUtils.join(thatShortcut, "");
                        if (thisKeys.startsWith(thatKeys) || thatKeys.startsWith(thisKeys))
                        {
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        @Override
        @SuppressWarnings ("RedundantIfStatement")
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            ShortcutCacheKey that = (ShortcutCacheKey) o;

            if (context != that.context) { return false; }
            if (!shortcuts.equals(that.shortcuts)) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = context.hashCode();
            result = 31 * result + shortcuts.hashCode();
            return result;
        }

        private ImmutableSet<ImmutableList<String>> makeImmutable(Set<List<String>> keys)
        {
            return ImmutableSet.copyOf(transform(keys, new Function<List<String>, ImmutableList<String>>()
            {
                @Override
                public ImmutableList<String> apply(@Nullable List<String> input)
                {
                    return ImmutableList.copyOf(input);
                }
            }));
        }
    }
}
