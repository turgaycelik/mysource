package com.atlassian.jira.gadgets.whitelist;

import com.atlassian.gadgets.directory.spi.ExternalGadgetSpec;
import com.atlassian.jira.bc.portal.GadgetApplinkUpgradeUtil;
import com.atlassian.jira.bc.whitelist.WhitelistManager;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Adds whitelist entries for all external gadgets.
 *
 * @since v4.3
 */
public class UpgradeTask_Build611 implements PluginUpgradeTask
{
    private final GadgetApplinkUpgradeUtil gadgetApplinkUpgradeUtil;
    private final WhitelistManager whitelistManager;


    public UpgradeTask_Build611(final GadgetApplinkUpgradeUtil gadgetApplinkUpgradeUtil, final WhitelistManager whitelistManager)
    {
        this.gadgetApplinkUpgradeUtil = gadgetApplinkUpgradeUtil;
        this.whitelistManager = whitelistManager;
    }

    @Override
    public Collection<Message> doUpgrade()
    {
        if (whitelistManager.getRules().isEmpty())
        {
            final Map<URI,List<ExternalGadgetSpec>> externalGadgets = gadgetApplinkUpgradeUtil.getExternalGadgetsRequiringUpgrade();
            final List<String> rules = new ArrayList<String>();
            //always add WAC to ensure we get the marketing gadget
            rules.add("http://www.atlassian.com/*");
            if(!externalGadgets.isEmpty())
            {
                for (URI uri : externalGadgets.keySet())
                {
                    rules.add(uri.normalize().toASCIIString().toLowerCase() + "/*");
                }
            }
            updateWhitelist(rules);
        }
        return Collections.emptySet();
    }

    @Override
    public String getShortDescription()
    {
        return "Configuring whitelist entries for all external gadgets";
    }

    @Override
    public int getBuildNumber()
    {
        return 1;
    }

    @Override
    public String getPluginKey()
    {
        return "com.atlassian.jira.gadgets";
    }

    private void updateWhitelist(final List<String> rules)
    {
        try
        {
            ImportUtils.setSubvertSecurityScheme(true);
            whitelistManager.updateRules(rules, false);
        }
        finally
        {
            ImportUtils.setSubvertSecurityScheme(false);
        }
    }
}