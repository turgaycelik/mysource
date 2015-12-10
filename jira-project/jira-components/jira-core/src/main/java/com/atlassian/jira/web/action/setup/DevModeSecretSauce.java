package com.atlassian.jira.web.action.setup;

import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import org.apache.commons.lang.StringUtils;
import webwork.action.Action;
import webwork.action.ActionContext;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * This is ONLY ever called in dev mode.  Its here to save us time but still leave JIRA alone enough so
 * that we can test it "fully".  All you have to do is remove your secretsauce
 * <p/>
 * This is ONLY here to make developers more productive and not in ANY way there for customers.  Its
 * so we can get versions of JIRA out quicker to customers.
 *
 * You have to place specific files into specific places and run in specific modes to get this to work. So
 * its not a security hole nor is it detracting from testability of JIRA.
 *
 * Its is however aimed at increasing the velocity of the JIRA developers.
 *
 * @since v4.4
 */
public class DevModeSecretSauce
{
    private final boolean boneFide;
    private final Properties properties;

    public DevModeSecretSauce(final JiraProperties jiraSystemProperties)
    {
        final File secretSauce = new File(jiraSystemProperties.getProperty("user.home") + "/.jiradev/secretsauce.properties");
        boneFide = JiraSystemProperties.isDevMode() && secretSauce.exists();
        properties = new Properties();
        if (boneFide)
        {
            try
            {
                properties.load(new FileReader(secretSauce));
            }
            catch (IOException ignored)
            {
            }
        }
    }

    /**
     * This returns true if it detects that you are a bona fide JIRA developer and want the auto setup stuff to just
     * work.
     * <p/>
     * It ONLY ever works in jira.dev.mode and ONLY if you drop the secretsauce file into  ~/.jiradev directory
     *
     * @return true if they are a JIRA developer
     */
    public boolean isBoneFideJiraDeveloper()
    {
        return boneFide;
    }

    /**
     * This is ONLY ever called in dev mode and it pre-fills the licence based in the secret sauce properties
    *
     * @return a prefilled licence or empty string
     */
    String getPrefilledLicence()
    {
        if (isBoneFideJiraDeveloper())
        {
            return StringUtils.defaultString(properties.getProperty("licence"));
        }
        return null;
    }

    /**
     * Can be called to get target harded code values if they are a JIRA developer or empty string otherwise
     *
     * @param propertyName the desired value if we are in JIRA easy setup mode
     * @return the value or empty string
     */
    public String getSecretSauceProperty(String propertyName)
    {
        return isBoneFideJiraDeveloper() ? StringUtils.defaultString(properties.getProperty(propertyName)) : null;
    }
}
