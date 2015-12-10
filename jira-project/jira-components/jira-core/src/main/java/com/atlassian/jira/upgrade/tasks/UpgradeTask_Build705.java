package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.stripToEmpty;

/**
 * Removing old LF colors in the databse if they are no differen from the current default colours. We do this because
 * old upgrade tasks wrote the default colours to the database.
 */
public class UpgradeTask_Build705 extends AbstractUpgradeTask
{
    final ApplicationProperties applicationProperties;

    //
    /// These are package level so the test can use them
    static final Map<String, String> OLD_COLORS;

    static
    {
        //
        // old colors
        //
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.put(APKeys.JIRA_LF_MENU_BGCOLOUR, "#3c78b5");
        builder.put(APKeys.JIRA_LF_TEXT_ACTIVE_LINKCOLOUR, "#3c78b5");
        builder.put(APKeys.JIRA_LF_TEXT_LINKCOLOUR, "#3c78b5");
        builder.put(APKeys.JIRA_LF_TEXT_HEADINGCOLOUR, "#3c78b5");
        builder.put(APKeys.JIRA_LF_TOP_BGCOLOUR, "#114070");
        builder.put(APKeys.JIRA_LF_TOP_HIGHLIGHTCOLOR, "#325c82");
        builder.put(APKeys.JIRA_LF_TOP_SEPARATOR_BGCOLOR, "#114070");

        OLD_COLORS = builder.build();
    }

    public UpgradeTask_Build705(final ApplicationProperties applicationProperties)
    {
        super(false);
        this.applicationProperties = applicationProperties;
    }

    public String getBuildNumber()
    {
        return "705";
    }

    public String getShortDescription()
    {
        return "Migrating the old JIRA UI colors to the new JIRA 5.0 colors";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        for (final Map.Entry<String, String> oldEntry : OLD_COLORS.entrySet())
        {
            final String currentColor = applicationProperties.getString(oldEntry.getKey());
            if (currentColor != null)
            {
                if (sameColor(oldEntry.getValue(), currentColor))
                {
                    applicationProperties.setString(oldEntry.getKey(), null);
                }
            }
        }
    }

    /**
     * Compares colors for the same value.  It turns out that colors might be missing the # at the front
     * so we do it a  bit mroe smartly
     *
     * @param color1 color to compare
     * @param color2 coor to compare
     * @return true if they are the same
     */
    static boolean sameColor(String color1, String color2)
    {
        color1 = stripToEmpty(color1);
        color2 = stripToEmpty(color2);

        if ((color1.length() > 1) && color1.startsWith("#"))
        {
            color1 = color1.substring(1);
        }
        if ((color2.length() > 1) && color2.startsWith("#"))
        {
            color2 = color2.substring(1);
        }
        return color1.equalsIgnoreCase(color2);
    }
}