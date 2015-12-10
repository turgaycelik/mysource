package com.atlassian.jira.bc.whitelist;

import com.atlassian.jira.component.ComponentAccessor;

import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation that delegates to the WhitelistManager in jira-gadgets-plugin
 *
 * @since v4.3
 */
public class DefaultWhitelistManager implements WhitelistManager
{
    private static final InternalWhitelistManager FALLBACK_WHITELIST_MANAGER = new InternalWhitelistManager()
    {
        @Override
        public List<String> getRules()
        {
            return Collections.emptyList();
        }

        @Override
        public List<String> updateRules(List<String> rules, boolean disabled)
        {
            return Collections.emptyList();
        }

        @Override
        public boolean isAllowed(URI uri)
        {
            return false;
        }

        @Override
        public boolean isDisabled()
        {
            return false;
        }
    };

    private static InternalWhitelistManager getWhitelistManager()
    {
        InternalWhitelistManager whitelistManager = ComponentAccessor.getOSGiComponentInstanceOfType(InternalWhitelistManager.class);
        return whitelistManager != null ? whitelistManager : FALLBACK_WHITELIST_MANAGER;
    }

    @Override
    public List<String> getRules()
    {
        return getWhitelistManager().getRules();
    }

    @Override
    public boolean isDisabled()
    {
        return getWhitelistManager().isDisabled();
    }

    @Override
    public List<String> updateRules(final List<String> newRules, final boolean disabled)
    {
        return getWhitelistManager().updateRules(newRules, disabled);
    }

    @Override
    public boolean isAllowed(final URI uri)
    {
        return getWhitelistManager().isAllowed(uri);
    }
}
