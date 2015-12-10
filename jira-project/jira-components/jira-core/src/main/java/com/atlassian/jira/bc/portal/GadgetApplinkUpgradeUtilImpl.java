package com.atlassian.jira.bc.portal;

import com.atlassian.gadgets.directory.spi.ExternalGadgetSpec;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecStore;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility that helps determine if there are any external gadget specs that need to have applinks created for them!
 *
 * @since v4.3
 */
public class GadgetApplinkUpgradeUtilImpl implements GadgetApplinkUpgradeUtil
{
    private final ExternalGadgetSpecStore externalGadgetSpecStore;

    public GadgetApplinkUpgradeUtilImpl(final ExternalGadgetSpecStore externalGadgetSpecStore)
    {
        this.externalGadgetSpecStore = externalGadgetSpecStore;
    }

    /**
     * Returns a mapping of a baseurl that doesn't have an applink configured yet to a list of gadget specs starting
     * with that base url
     *
     * @return Map of baseurl -> List of gadget specs starting with that baseurl
     */
    @Override
    public Map<URI, List<ExternalGadgetSpec>> getExternalGadgetsRequiringUpgrade()
    {
        final Map<URI, List<ExternalGadgetSpec>> ret = new LinkedHashMap<URI, List<ExternalGadgetSpec>>();

        for (ExternalGadgetSpec spec : externalGadgetSpecStore.entries())
        {
            final URI specUri = spec.getSpecUri();
            final URI host = URI.create(specUri.getScheme() + "://" + specUri.getAuthority());
            if (!ret.containsKey(host))
            {
                ret.put(host, new ArrayList<ExternalGadgetSpec>());
            }
            ret.get(host).add(spec);
        }
        return ret;
    }
}
