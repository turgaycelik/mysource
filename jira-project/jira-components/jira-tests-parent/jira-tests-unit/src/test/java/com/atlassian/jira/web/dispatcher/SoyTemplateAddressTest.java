package com.atlassian.jira.web.dispatcher;

import com.google.common.collect.Iterables;

import org.junit.Assert;

import webwork.config.util.ActionInfo;
import webwork.config.util.ActionInfoImpl;

import static com.atlassian.jira.web.dispatcher.SoyTemplateAddress.address;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

/**
 */
public class SoyTemplateAddressTest
{
    @org.junit.Before
    public void setUp() throws Exception
    {

    }

    @org.junit.Test
    public void testGoodAddresses() throws Exception
    {

        String sourceOfConfig;

        sourceOfConfig = "pluginKey:someOtherModule";

        assertAddress("fully qualified address", "fully:qualified", "soyTemplate",
                address(
                        mockViewInfo("fully:qualified/soyTemplate", sourceOfConfig)
                )
        );

        assertAddress("partial plugin key", "pluginKey:mixedModuleKey", "soyTemplate",
                address(
                        mockViewInfo(":mixedModuleKey/soyTemplate", sourceOfConfig)
                )
        );

        assertAddress("partial plugin key with whitespace", "pluginKey:mixedModuleKey", "soyTemplate",
                address(
                        mockViewInfo(" \n:\t mixedModuleKey / soyTemplate", sourceOfConfig)
                )
        );

        assertAddress("partial plugin key with dot", "pluginKey:mixedModuleKey", "soyTemplate",
                address(
                        mockViewInfo(".:mixedModuleKey/soyTemplate", sourceOfConfig)
                )
        );

        assertAddress("partial plugin key with dot and spaces", "pluginKey:mixedModuleKey", "soyTemplate",
                address(
                        mockViewInfo(" .:mixedModuleKey/soyTemplate", sourceOfConfig)
                )
        );

        assertAddress("partial plugin key with dot and whitespace", "pluginKey:mixedModuleKey", "soyTemplate",
                address(
                        mockViewInfo("\n\t.\n\t:mixedModuleKey/soyTemplate", sourceOfConfig)
                )
        );


        //
        // JIRA core support
        //
        sourceOfConfig = "/path/WEB-INF/classes/actions.xml";

        assertAddress("fully qualified address", "fully:qualified", "soyTemplate",
                address(
                        mockViewInfo("fully:qualified/soyTemplate", sourceOfConfig)
                )
        );

        assertAddress("partial plugin key", "jira.webresources:mixedModuleKey", "soyTemplate",
                address(
                        mockViewInfo(":mixedModuleKey/soyTemplate", sourceOfConfig)
                )
        );

        assertAddress("partial plugin key with whitespace", "jira.webresources:mixedModuleKey", "soyTemplate",
                address(
                        mockViewInfo(" \n:\t mixedModuleKey / soyTemplate", sourceOfConfig)
                )
        );

        assertAddress("partial plugin key with dot", "jira.webresources:mixedModuleKey", "soyTemplate",
                address(
                        mockViewInfo(".:mixedModuleKey/soyTemplate", sourceOfConfig)
                )
        );

        assertAddress("partial plugin key with dot and spaces", "jira.webresources:mixedModuleKey", "soyTemplate",
                address(
                        mockViewInfo(" .:mixedModuleKey/soyTemplate", sourceOfConfig)
                )
        );

        assertAddress("partial plugin key with dot and whitespace", "jira.webresources:mixedModuleKey", "soyTemplate",
                address(
                        mockViewInfo("\n\t.\n\t:mixedModuleKey/soyTemplate", sourceOfConfig)
                )
        );

    }


    @org.junit.Test
    public void testBadAddresses() throws Exception
    {
        String defaultCompleteModuleKey = "com.plugin.key:moduleKey";

        assertBadAddress("no template at all", "", defaultCompleteModuleKey);

        assertBadAddress("no template at all with all whitespace", " \t\n ", defaultCompleteModuleKey);

        assertBadAddress("no slash to indicate the template name", "com.plugin.key:moduleKey", defaultCompleteModuleKey);

        assertBadAddress("no colon to indicate the module name", "moduleKey/templateName", defaultCompleteModuleKey);

        assertBadAddress("too many colons", "com.plugin.key:moduleKey:templateName", defaultCompleteModuleKey);

        assertBadAddress("too many colons we well", "com.plugin.key:moduleKey/templateName:someOtherColon", defaultCompleteModuleKey);

        assertBadAddress("no template name", "com.plugin.key:moduleKey/", defaultCompleteModuleKey);

        assertBadAddress("no template name without plugin key", ":moduleKey/", defaultCompleteModuleKey);

        assertBadAddress("no template name without plugin key and module", ":/", defaultCompleteModuleKey);

        assertBadAddress("no template name without plugin key and module and template", ":", defaultCompleteModuleKey);

        assertBadAddress("no template name without plugin key and module and just bad structure in general", "::/", defaultCompleteModuleKey);

        assertBadAddress("no template name with whitespace", "com.plugin.key:moduleKey/ \n\t", defaultCompleteModuleKey);
    }

    private void assertBadAddress(final String reason, final String templateNameValue, final String defaultCompleteModuleKey)
    {
        try
        {
            address(
                    mockViewInfo(templateNameValue, defaultCompleteModuleKey)
            );
            fail(reason);
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    private void assertAddress(final String reason, final String completeKey, final String templateName, final SoyTemplateAddress address)
    {
        Assert.assertThat(reason + " completeKey=", address.getCompleteKey(), equalTo(completeKey));
        Assert.assertThat(reason + " templateName=", address.getTemplateName(), equalTo(templateName));
    }

    private ActionInfo.ViewInfo mockViewInfo(final String soyAddress, final String sourceOfConfig)
    {
        ActionInfo actionInfo = ActionInfoImpl.builder("a", "b").setSource(sourceOfConfig).startView("viewName", soyAddress).endView().build();
        return Iterables.get(actionInfo.getViews(), 0);
    }
}
