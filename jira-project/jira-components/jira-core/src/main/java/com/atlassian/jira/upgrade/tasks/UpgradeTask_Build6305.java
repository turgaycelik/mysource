package com.atlassian.jira.upgrade.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.jira.database.DatabaseUtil.closeQuietly;

/**
 * This upgrade task changes two Gravatar settings in JIRA:
 * <ol>
 *
 * <li>If the "Allow Gravatar" settings is set to "default" then explicitly set Gravatar to OFF. We do this because
 *     the default is changing from OFF to ON and we don't want to change the behaviour for existing JIRA instances.
 *
 * <li>If the "Allow Gravatar" setting is set to "ON" then we reset every user's avatar to the JIRA default. With
 *     JRA-33596 implemented this means that JIRA will display the Gravatar for every user as before.
 *
 * </ol>
 *
 * @since JIRA 6.3
 */
public class UpgradeTask_Build6305 extends AbstractUpgradeTask
{
    /**
     * Logger for com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6305.
     */
    private static final Logger logger = LoggerFactory.getLogger(UpgradeTask_Build6305.class);

    /**
     * Property name of the "Allow Gravatars" application property as at JIRA 6.3.
     *
     * @see com.atlassian.jira.avatar.GravatarSettings
     */
    private static final String GRAVATARS_PROPERTY = "jira.user.avatar.gravatar.enabled";

    /**
     * The default value for "Allow Gravatars" in previous versions of JIRA.
     */
    private static final boolean GRAVATARS_PRE63_DEFAULT = false;

    /**
     * Used to read/write application properties.
     */
    private final PropertiesManager propertiesManager;

    public UpgradeTask_Build6305(final PropertiesManager propertiesManager)
    {
        super(false);
        this.propertiesManager = propertiesManager;
    }

    @Override
    public String getBuildNumber()
    {
        return "6305";
    }

    @Override
    public String getShortDescription()
    {
        return "Migrate avatar settings";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        boolean allowGravatars = isAllowGravatars();
        if (allowGravatars)
        {
            // we want Gravatar users to keep seeing the same avatar after upgrading so we need to reset every user to
            // the JIRA default avatar. in JIRA this has the effect of displaying the Gravatar for every user.
            resetAllUsersToDefaultAvatar();
        }

        // make the current Gravatar setting explicit in the database (may have previous relied on the "default"
        // JIRA setting if the property was not set)
        setAllowGravatars(allowGravatars);
    }

    /**
     * Resets every JIRA user to the default avatar using a single SELECT statement.
     */
    private void resetAllUsersToDefaultAvatar() throws SQLException
    {
        final Stopwatch watch = new Stopwatch().start();

        final Optional<Long> defaultAvatarId = getDefaultUserAvatarId();
        if (!defaultAvatarId.isPresent())
        {
            logger.info("Unable to determine default avatar id. Skipping upgrade task...");
            return;
        }

        logger.info("Resetting all users to default avatar {}...", defaultAvatarId);

        Connection conn = null;
        PreparedStatement stmt = null;
        try
        {
            conn = getDatabaseConnection();

            //noinspection SpellCheckingInspection
            stmt = conn.prepareStatement("UPDATE " + convertToSchemaTableName("propertynumber") +
                                         "   SET propertyvalue=?" +
                                         " WHERE id IN (SELECT id" +
                                                       "  FROM "+ convertToSchemaTableName("propertyentry") +
                                                       " WHERE entity_name='ApplicationUser'" +
                                                       "   AND property_key='user.avatar.id')");

            stmt.setLong(1, defaultAvatarId.get());
            int updated = stmt.executeUpdate();

            logger.info("Reset {} user avatars in {}ms", updated, watch.elapsedMillis());
        }
        finally
        {
            closeQuietly(stmt);
            closeQuietly(conn);
        }
    }

    /**
     * @return true if the instance is configured to allow Gravatars
     */
    private boolean isAllowGravatars() throws SQLException
    {
        if (applicationProperties().exists(GRAVATARS_PROPERTY))
        {
            return applicationProperties().getBoolean(GRAVATARS_PROPERTY);
        }

        return GRAVATARS_PRE63_DEFAULT;
    }

    /**
     * Sets the Allow Gravatars option.
     *
     * @param allowGravatars whether to allow Gravatars
     */
    private void setAllowGravatars(final boolean allowGravatars) throws SQLException
    {
        logger.info("Setting application property {} to {}", GRAVATARS_PROPERTY, allowGravatars);

        applicationProperties().setBoolean(GRAVATARS_PROPERTY, allowGravatars);
    }

    /**
     * @return the an optional avatar id for the JIRA default avatar
     */
    private Optional<Long> getDefaultUserAvatarId() throws SQLException
    {
        try
        {
            return Optional.of(Long.valueOf(applicationProperties().getString("jira.avatar.user.default.id")));
        }
        catch (NumberFormatException e)
        {
            return Optional.absent();
        }
        catch (PropertyException e)
        {
            return Optional.absent();
        }
    }

    private PropertySet applicationProperties()
    {
        return propertiesManager.getPropertySet();
    }
}
