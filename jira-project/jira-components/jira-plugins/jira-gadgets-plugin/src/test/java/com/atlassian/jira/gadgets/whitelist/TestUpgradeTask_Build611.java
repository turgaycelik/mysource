package com.atlassian.jira.gadgets.whitelist;

import com.atlassian.gadgets.directory.spi.ExternalGadgetSpec;
import com.atlassian.jira.bc.portal.GadgetApplinkUpgradeUtil;
import com.atlassian.jira.bc.whitelist.WhitelistManager;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestUpgradeTask_Build611
{
    @Mock
    private GadgetApplinkUpgradeUtil mockGadgetApplinkUpgradeUtil;

    @Mock
    private WhitelistManager mockWhitelistManager;
    
    @Test
    public void testDoUpgrade() throws Exception
    {
        when(mockGadgetApplinkUpgradeUtil.getExternalGadgetsRequiringUpgrade()).thenReturn(Collections.<URI, List<ExternalGadgetSpec>>emptyMap());
        when(mockWhitelistManager.updateRules(Matchers.<List<String>>any(), anyBoolean())).thenReturn(Arrays.asList("http://www.atlassian.com/*"));
        when(mockWhitelistManager.getRules()).thenReturn(Collections.<String>emptyList());

        UpgradeTask_Build611 upgradeTask = new UpgradeTask_Build611(mockGadgetApplinkUpgradeUtil, mockWhitelistManager);
        upgradeTask.doUpgrade();
    }

    @Test
    public void testDoUpgradeWithData() throws Exception
    {
        when(mockGadgetApplinkUpgradeUtil.getExternalGadgetsRequiringUpgrade()).thenReturn(ImmutableMap.<URI, List<ExternalGadgetSpec>>of(URI.create("http://extranet.atlassian.com"), Collections.<ExternalGadgetSpec>emptyList(), URI.create("http://www.google.com"), Collections.<ExternalGadgetSpec>emptyList(), URI.create("https://www.cows.com"), Collections.<ExternalGadgetSpec>emptyList()));
        when(mockWhitelistManager.updateRules(Matchers.<List<String>>any(), anyBoolean())).thenReturn(Arrays.asList("http://www.atlassian.com/*", "http://extranet.atlassian.com/*", "http://www.google.com/*", "https://www.cows.com/*"));

        UpgradeTask_Build611 upgradeTask = new UpgradeTask_Build611(mockGadgetApplinkUpgradeUtil, mockWhitelistManager);
        upgradeTask.doUpgrade();
    }
}
