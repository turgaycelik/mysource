package com.atlassian.jira.config.webwork;

import java.util.HashSet;
import java.util.Set;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.MockApplicationProperties;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 */
public class ApplicationPropertiesConfigurationTest
{
    // test code should not depend on ther code under test hence the repetition
    private static Set<String> WHITELIST_OF_KEYS = new HashSet<String>();
    static
    {
        WHITELIST_OF_KEYS.add("webwork.multipart.maxSize");
        WHITELIST_OF_KEYS.add("webwork.i18n.encoding");
    }

    @Test
    public void testGetImpl() throws Exception
    {
        final MockApplicationProperties applicationProperties = new MockApplicationProperties() {
            @Override
            public String getDefaultBackedString(String name)
            {
                return name; // echo
            }
        };

        ApplicationPropertiesConfiguration config = new ApplicationPropertiesConfiguration()
        {
            @Override
            ApplicationProperties getApplicationProperties()
            {
                return applicationProperties;
            }
        };

        attempt(config, null, false);
        attempt(config, "", false);
        attempt(config, "some.name", false);
        attempt(config, "webwork.name", false);
        for (String key : WHITELIST_OF_KEYS)
        {
            attempt(config,key,true);
        }
    }

    private void attempt(ApplicationPropertiesConfiguration config, final String propertyName, final boolean expectedToSucceed) {
        try
        {
            Object echoedPropertyName = config.getImpl(propertyName);
            assertEquals(propertyName,echoedPropertyName);
            if (! expectedToSucceed) {
                fail(propertyName + " access was expected to fail");
            }
        }
        catch (IllegalArgumentException e)
        {
            if (expectedToSucceed) {
                fail(propertyName + " access was expected to succeed");
            }
        }
    }
}
