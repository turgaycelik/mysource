package com.atlassian.jira.upgrade.tasks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;

import static com.atlassian.jira.upgrade.tasks.UpgradeTask_Build705.sameColor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v5.0
 */
public class TestUpgradeTask_Build705
{
    @Test
    public void testSameColor() throws Exception
    {
        assertTrue(sameColor(null, null));
        assertTrue(sameColor("", ""));
        assertTrue(sameColor(" ", "\t"));
        assertTrue(sameColor("#ff00cc", "#ff00cc"));
        assertTrue(sameColor("#ff00cc", "ff00cc"));
        assertTrue(sameColor("#ff00cc", "ff00cc"));
        assertTrue(sameColor("ff00cc", "ff00cc"));
        assertTrue(sameColor("ff00cc", "#ff00cc"));
        assertTrue(sameColor("ff00cc ", "#ff00cc"));
        assertTrue(sameColor("ff00cc ", " #ff00cc"));

        assertFalse(sameColor("#fff", "#ffffff"));
        assertFalse(sameColor("ff#", "#ff"));
        assertFalse(sameColor(null, "#ff"));
        assertFalse(sameColor("", "#ff"));
        assertFalse(sameColor("", "#f f"));
        assertFalse(sameColor("", "#f f "));
        assertFalse(sameColor("#fc fc", "#fcfc"));
    }

    @Test
    public void testUpgradeWithDefaults() throws Exception
    {
        MockApplicationProperties properties = new MockApplicationProperties();
        //Don't change this because its customised.
        properties.map.put(APKeys.JIRA_LF_MENU_BGCOLOUR, "something");
        //Remove this colour because it is the OLD default and as such is not necessary.
        properties.map.put(APKeys.JIRA_LF_TEXT_ACTIVE_LINKCOLOUR, "#3c78b5");

        UpgradeTask_Build705 build705 = new UpgradeTask_Build705(properties);
        build705.doUpgrade(false);

        assertEquals("something", properties.map.get(APKeys.JIRA_LF_MENU_BGCOLOUR));
        assertFalse(properties.map.containsKey(APKeys.JIRA_LF_TEXT_ACTIVE_LINKCOLOUR));
        assertFalse(properties.map.containsKey(APKeys.JIRA_LF_TOP_SEPARATOR_BGCOLOR));
    }

    private static class MockApplicationProperties implements ApplicationProperties
    {
        private final Map<String, String> map = new HashMap<String, String>();

        @Override
        public String getText(String name)
        {
            throw new NotImplementedException();
        }

        @Override
        public String getDefaultBackedText(String name)
        {
            throw new NotImplementedException();
        }

        @Override
        public void setText(String name, String value)
        {
            throw new NotImplementedException();
        }

        @Override
        public String getString(String name)
        {
            return map.get(name);
        }

        @Override
        public Collection<String> getDefaultKeys()
        {
            throw new NotImplementedException();
        }

        @Override
        public String getDefaultBackedString(String name)
        {
            throw new NotImplementedException();
        }

        @Override
        public String getDefaultString(String name)
        {
            throw new NotImplementedException();
        }

        @Override
        public void setString(String name, String value)
        {
            if (value == null)
            {
                map.remove(name);
            }
            else
            {
                map.put(name, value);
            }
        }

        @Override
        public boolean getOption(String key)
        {
            throw new NotImplementedException();
        }

        @Override
        public Collection<String> getKeys()
        {
            throw new NotImplementedException();
        }

        @Override
        public void setOption(String key, boolean value)
        {
            throw new NotImplementedException();
        }

        @Override
        public String getEncoding()
        {
            throw new NotImplementedException();
        }

        @Override
        public String getMailEncoding()
        {
            throw new NotImplementedException();
        }

        @Override
        public String getContentType()
        {
            throw new NotImplementedException();
        }

        @Override
        public void refresh()
        {
            throw new NotImplementedException();
        }

        @Override
        public Locale getDefaultLocale()
        {
            throw new NotImplementedException();
        }

        @Override
        public Collection<String> getStringsWithPrefix(String prefix)
        {
            throw new NotImplementedException();
        }

        @Override
        public Map<String, Object> asMap()
        {
            return new HashMap<String, Object>(map);
        }
    }

}
