package com.atlassian.jira.bc.portal;

import com.atlassian.gadgets.directory.spi.ExternalGadgetSpec;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Utility that helps determine if there are any external gadget specs that need to have applinks created for them!
 *
 * @since v4.3
 */
public interface GadgetApplinkUpgradeUtil
{
    Map<URI, List<ExternalGadgetSpec>> getExternalGadgetsRequiringUpgrade();
}
