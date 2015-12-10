package com.atlassian.jira.upgrade.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.atlassian.crowd.directory.DelegatedAuthenticationDirectory;
import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.directory.loader.LDAPDirectoryInstanceLoader;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.exception.DirectoryInstantiationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.propertyset.CachingOfBizPropertyEntryStore;
import com.atlassian.jira.propertyset.OfBizPropertyEntryStore;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.upgrade.tasks.util.Sequences;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.user.util.UserUtilImpl;
import com.atlassian.jira.web.util.HelpUtil;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericDelegator;

/**
 * This task migrates OS_users and Groups to Crowd Embedded users and Groups.
 *
 * This is also a setup task to convert the initial administrator and mandatory groups created in the setup process.
 *
 * @since v4.3
 */
public class UpgradeTask_Build602 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build602.class);
    private final GenericDelegator genericDelegator;
    private final CrowdDirectoryService crowdDirectoryService;
    private final LDAPDirectoryInstanceLoader ldapDirectoryInstanceLoader;
    private final Sequences sequences;

    private Long internalDirectoryID;
    private boolean internalDirectoryExists;
    private boolean delegatingDirectoryExists;
    private List<RemoteDirectory> remoteDirectories = new ArrayList<RemoteDirectory>();

    private final String upgradeGuideUrl;
    private final String upgradeGuideTitle;

    private int maxExternalEntityId = 0;
    private int nextGroupId = 10000;

    private static final String LOGIN_INFO_ATTRIBUTES = " 'login.lastLoginMillis', 'login.previousLoginMillis', 'login.lastFailedLoginMillis', 'login.count', 'login.currentFailedCount', 'login.totalFailedCount' ";

    public UpgradeTask_Build602(final GenericDelegator genericDelegator, final CrowdDirectoryService crowdDirectoryService,
            final LDAPDirectoryInstanceLoader ldapDirectoryInstanceLoader, final Sequences sequences)
    {
        super(false);
        this.genericDelegator = genericDelegator;
        this.crowdDirectoryService = crowdDirectoryService;
        this.ldapDirectoryInstanceLoader = ldapDirectoryInstanceLoader;
        this.sequences = sequences;

        HelpUtil.HelpPath helpPath = HelpUtil.getInstance().getHelpPath("upgrading");
        upgradeGuideUrl = helpPath.getUrl();
        upgradeGuideTitle = helpPath.getTitle();
    }

    private boolean useSavePoints;

    public String getBuildNumber()
    {
        return "602";
    }

    public String getShortDescription()
    {
        return "Converting Users and Groups to new structure for Crowd Embedded.";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        Connection connection = getDatabaseConnection();

        // Oracle 10g and MS SqlServer only half supports savepoints and we really only need them for a couple of badly
        // behaved open source DBs, So if Oracle or MS SqlServer we also don't use them.
        useSavePoints =  connection.getMetaData().supportsSavepoints() && !isORACLE() && !isMSSQL();

        boolean committed = false;
        try
        {
            connection.setAutoCommit(false);

            if (crowdAlreadyInitialised(connection))
            {
                log.warn("The crowd embedded tables already contain data.  No conversion will be performed.");
                return;
            }

            getDirectoryInfo(connection);
            boolean ok = true;
            if (ok)
            {
                ok &= migrateGroups(connection);
            }
            if (ok)
            {
                ok &= migrateGroupPropertySets(connection);
            }
            if (ok)
            {
                ok &= migrateUsers(connection);
            }
            if (ok)
            {
                ok &= migrateUserProperties(connection);
            }
            if (ok)
            {
                ok &= migrateMemberships(connection);
            }
            if (ok)
            {
                cleanOldTables(connection);
                connection.commit();
                committed = true;
            }
        }
        finally
        {
            if (!committed)
            {
                connection.rollback();
            }
            connection.close();
        }
        // Purge the user caches
        flushUserCaches();

        /** Refreshes the ID sequencer clearing all cached bank values. */
        genericDelegator.refreshSequencer();
    }

    private void getDirectoryInfo(final Connection connection) throws SQLException, DirectoryInstantiationException
    {
        // Set a default of 1 if there is nothing else.
        internalDirectoryID = 1l;
        internalDirectoryExists = false;
        delegatingDirectoryExists = false;

        // Get the first configured or default directoy in the database
        String selectSql = "select id, directory_type from " + convertToSchemaTableName("cwd_directory") + " order by directory_position";
        PreparedStatement selectStmt = connection.prepareStatement(selectSql);

        ResultSet rs = selectStmt.executeQuery();
        while (rs.next())
        {
            String directoryType = rs.getString("directory_type");
            if (directoryType.equals(DirectoryType.INTERNAL.toString()))
            {
                internalDirectoryExists = true;
                internalDirectoryID = rs.getLong("id");
            }
            else if (directoryType.equals(DirectoryType.DELEGATING.toString()))
            {
                delegatingDirectoryExists = true;

                Directory delegatingDirectory = crowdDirectoryService.findDirectoryById(rs.getLong("id"));
                Directory ldap = getLdapVersionOfDirectory(delegatingDirectory);
                remoteDirectories.add(ldapDirectoryInstanceLoader.getDirectory(ldap));
            }
        }
    }

    public static void flushUserCaches()
    {
        UserUtil userUtil = ComponentAccessor.getUserUtil();
        if (userUtil instanceof UserUtilImpl)
        {
            ((UserUtilImpl) userUtil).flushUserCaches();
        }
        else
        {
            log.error("Expected to find a UserUtilImpl, but got " + userUtil.getClass().getName());
        }
        OfBizPropertyEntryStore store = ComponentAccessor.getComponent(OfBizPropertyEntryStore.class);
        if (store instanceof CachingOfBizPropertyEntryStore)
        {
            ((CachingOfBizPropertyEntryStore)store).onClearCache(null);
        }
        else
        {
            final String name = (store != null) ? store.getClass().getName() : "null";
            log.error("Expected to find a CachingOfBizPropertyEntryStore, but got " + name);
        }
    }

    private void cleanOldTables(Connection connection) throws SQLException
    {
        Statement stmt = connection.createStatement();
        stmt.execute("delete from " + convertToSchemaTableName("membershipbase"));
        stmt.execute("delete from " + convertToSchemaTableName("groupbase"));
        stmt.execute("delete from " + convertToSchemaTableName("userbase"));


        stmt.execute("delete from " + convertToSchemaTableName("propertystring") + " where id in (select id from " + convertToSchemaTableName("propertyentry") + " where entity_name = 'OSUser')");
        stmt.execute("delete from " + convertToSchemaTableName("propertytext") + " where id in (select id from " + convertToSchemaTableName("propertyentry") + " where entity_name = 'OSUser')");
        stmt.execute("delete from " + convertToSchemaTableName("propertydate") + " where id in (select id from " + convertToSchemaTableName("propertyentry") + " where entity_name = 'OSUser')");
        stmt.execute("delete from " + convertToSchemaTableName("propertydecimal") + " where id in (select id from " + convertToSchemaTableName("propertyentry") + " where entity_name = 'OSUser')");
        stmt.execute("delete from " + convertToSchemaTableName("propertynumber") + " where id in (select id from " + convertToSchemaTableName("propertyentry") + " where entity_name = 'OSUser')");
        stmt.execute("delete from " + convertToSchemaTableName("propertyentry") + " where entity_name = 'OSUser'");
    }


    /**
     * Migrate the userbase table to the cwd_users table. This also migrates some core properties that were previously
     * stored in property sets.
     *
     * @param connection Database Connection
     * @throws SQLException SQL exception
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING", justification="Non-constant but safe.")        
    private boolean migrateUsers(final Connection connection) throws SQLException, OperationFailedException
    {
        String selectSql = "select ub.id as id, ub.username as username, psname.propertyvalue as fullname, psmail.propertyvalue as email, ub.password_hash as credential "
                + " from " + convertToSchemaTableName("userbase") + " ub "
                + " left outer join " + convertToSchemaTableName("propertyentry") + " pename on (ub.id = pename.entity_id) and (pename.entity_name = 'OSUser' and pename.property_key = 'fullName') "
                + " left outer join " + convertToSchemaTableName("propertystring") + " psname ON psname.id = pename.id "
                + " left outer join " + convertToSchemaTableName("propertyentry") + " pemail on (ub.id = pemail.entity_id) and (pemail.entity_name = 'OSUser' and pemail.property_key = 'email') "
                + " left outer join " + convertToSchemaTableName("propertystring") + " psmail ON psmail.id = pemail.id ";

        String insertSql = "insert into " + convertToSchemaTableName("cwd_user") + " (id, directory_id, user_name, lower_user_name, active, created_date, updated_date, first_name, lower_first_name, "
                + "  last_name, lower_last_name, display_name, lower_display_name, email_address, lower_email_address,  credential)"
                + "  values (?, ?, ?, ?, 1, current_timestamp, current_timestamp, '', '', '', '', ?, ?, ?, ?, ?)";

        PreparedStatement selectStmt = connection.prepareStatement(selectSql);
        PreparedStatement insertStmt = connection.prepareStatement(insertSql);

        ResultSet rs = selectStmt.executeQuery();
        while (rs.next())
        {
            int id = rs.getInt("id");
            String userName = rs.getString("username");
            String fullName = rs.getString("fullname");
            String email = rs.getString("email");
            String credential = rs.getString("credential");
            long directoryId = getDirectoryIdForUser(userName);

            insertStmt.setLong(1, id);
            insertStmt.setLong(2, directoryId);
            insertStmt.setString(3, userName);
            insertStmt.setString(4, userName.toLowerCase());
            insertStmt.setString(5, fullName);
            insertStmt.setString(6, fullName == null ? null : fullName.toLowerCase());
            insertStmt.setString(7, email);
            insertStmt.setString(8, email == null ? null : email.toLowerCase());
            insertStmt.setString(9, credential);
            Savepoint savePoint = null;
            try
            {
                savePoint = setSavepoint(connection);  //Name required for HSSQL
                insertStmt.execute();
                releaseSavepoint(connection, savePoint);
                migrateUserLoginAttributes(connection, id, directoryId);
            }
            catch (SQLException sqlex)
            {
                log.warn("User not migrated. User: " + userName + ". Error: " + sqlex.getMessage());
                rollBackSavePoint(connection, savePoint);
            }
        }
        rs.close();
        selectStmt.close();
        insertStmt.close();

        // Update the sequence table
        sequences.update(connection, "User", "cwd_user");
        sequences.update(connection, "UserAttribute", "cwd_user_attributes");

        return true;
    }

    private long getDirectoryIdForUser(String userName) throws OperationFailedException
    {
        // Users can be created in the local internal directory or in the local delegating LDAP directory
        
        // If we have  delegating LDAP directory check if the user is actually in the LDAP directory.
        // If he is we will create the user in the delegating directory otherwise in the internal directory.
        for (RemoteDirectory remoteDirectory : remoteDirectories)
        {
            try
            {
                remoteDirectory.findUserByName(userName);
                return remoteDirectory.getDirectoryId();
            }
            catch (UserNotFoundException e)
            {
                // next
            }
        }
        return internalDirectoryID;
    }

    private Directory getLdapVersionOfDirectory(Directory directory)
    {
        DirectoryImpl ldap = new DirectoryImpl(directory);

        String ldapClass = directory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_LDAP_DIRECTORY_CLASS);
        ldap.setImplementationClass(ldapClass);

        return ldap;
    }

    /**
     * Migrate the user properties of internal users.
     * Some core properties are separately moved to the cwd_user table.
     *
     * Properties will be moved using a priority system, depending upon the directory configuration previously installed
     * by the preceeding upgrade task.
     *
     * If the first directory is an Internal Directory or Delgated LDAP directory then we
     * <ul>
     *   <li>Assume that the actual users being migrated are actually live users and have precedenc of any external entity of the same name.</li>
     *   <li>Move all properties associated with OSUser entities to properties associated to ExternalEntity.</li>
     *   <li>A new ExternalEntity will be created for the username if it does not exist.</li>
     *   <li>Any existing property with the same key for an ExternalEntity will be <b>overwritten<b>.</li>
     * </ul>
     *
     * If the first directory is a Crowd or Ldap directory then we
     * <ul>
     *   <li>Assume that the any users being migrated are not being used or only being used as a fallback mechanism.  External entities have precedence.</li>
     *   <li>Move all properties associated with OSUser entities to properties associated to ExternalEntity.</li>
     *   <li>A new ExternalEntity will be created for the username if it does not exist.</li>
     *   <li>Any existing property with the same key for an ExternalEntity will be <b>preserved</b>.</li>
     * </ul>
     *
     *
     * @param connection Database Connection
     * @throws SQLException SQL exception
     */
    private boolean migrateUserProperties(final Connection connection) throws SQLException
    {
        // Initialise the field max ExternalEntityId with the current highest used id in the table.
        PreparedStatement stmt = connection.prepareStatement("select max(id) from " + convertToSchemaTableName("external_entities"));
        ResultSet rs = stmt.executeQuery();
        if (rs.next())
        {
            maxExternalEntityId = rs.getInt(1);
        }
        rs.close();
        stmt.close();


        String selectSql = "select id, entity_id, property_key "
                + "       from " + convertToSchemaTableName("propertyentry") + " pe "
                + "      where pe.entity_name = 'OSUser' "
                + "        and pe.property_key not in ('fullName', 'email') "
                + "        and pe.property_key not in (" + LOGIN_INFO_ATTRIBUTES + ") ";

        String updateSql = "update " + convertToSchemaTableName("propertyentry") + " set entity_id = ?, entity_name = 'ExternalEntity' "
                + "  where id = ?";

        String checkDupSql = "select id from " + convertToSchemaTableName("propertyentry") + "  where entity_name = 'ExternalEntity' and entity_id = ? and property_key= ?";
        String deleteSql = "delete from " + convertToSchemaTableName("propertyentry") + "  where id = ?";

        PreparedStatement selectStmt = connection.prepareStatement(selectSql);
        PreparedStatement updateStmt = connection.prepareStatement(updateSql);
        PreparedStatement checkDupStmt = connection.prepareStatement(checkDupSql);
        PreparedStatement deleteStmt = connection.prepareStatement(deleteSql);

        rs = selectStmt.executeQuery();
        while (rs.next())
        {
            int id = rs.getInt("id");
            int crowdUserId = rs.getInt("entity_id");
            String key = rs.getString("property_key");

            int entityId = 0;
            try
            {
                entityId = getExternalEntityId(connection, crowdUserId);
            }
            catch (UserNotFoundException e)
            {
                log.warn("User property not migrated. User Id : " + entityId + ", property key : " + key + ". user noes not exist.");
            }

            // Check for already existing key
            checkDupStmt.setInt(1, crowdUserId);
            checkDupStmt.setString(2, key);
            ResultSet rsChk = checkDupStmt.executeQuery();
            try
            {
                if (rsChk.next())
                {
                    if (internalDirectoryExists)
                    {

                        // remove any previous attribute with the same key
                        deleteStmt.setInt(1, rsChk.getInt("id"));
                        deleteStmt.execute();
                    }
                    else
                    {
                        // skip adding the new value and leave the old one in place.
                        continue;
                    }
                }
            }
            finally
            {
                rsChk.close();
            }

            updateStmt.setInt(1, entityId);
            updateStmt.setInt(2, id);
            Savepoint savePoint = null;
            try {
                savePoint = setSavepoint(connection);  //Name required for HSSQL
                updateStmt.execute();
                releaseSavepoint(connection, savePoint);
            }
            catch(SQLException sqlex)
            {
                log.warn("Attribute not migrated. User Id : " + entityId + ", attribute key : " + key + ". Error: " + sqlex.getMessage());
                rollBackSavePoint(connection, savePoint);
            }
        }
        rs.close();

        selectStmt.close();
        updateStmt.close();
        checkDupStmt.close();
        deleteStmt.close();

        // Update the sequence table
        sequences.update(connection, "ExternalEntity", "external_entities");
        return true;
    }

    /**
     * Migrate the login properties to be Crowd Attributes.
     *
     * @param connection Database connection
     * @param userId User's id to migrate attributes
     * @param directoryId Directory where user is being created.
     *
     * @throws SQLException Database error.
     */
    private boolean migrateUserLoginAttributes(final Connection connection, final long userId, long directoryId) throws SQLException
    {
        String selectSql = "select pe.id as id, pe.entity_id as user_id, pe.property_key as property_key, ps.propertyvalue as property_value"
                + "       from " + convertToSchemaTableName("propertyentry") + " pe join " + convertToSchemaTableName("propertystring") + " ps ON ps.id = pe.id "
                + "      where pe.entity_name = 'OSUser' and pe.property_key in (" + LOGIN_INFO_ATTRIBUTES + ") and pe.entity_id = ?";

        String insertSql = "insert into " + convertToSchemaTableName("cwd_user_attributes") + " (id, user_id, directory_id, attribute_name, attribute_value, lower_attribute_value) "
                + "  values (?,?,?,?,?,?)";

        PreparedStatement selectStmt = connection.prepareStatement(selectSql);
        PreparedStatement insertStmt = connection.prepareStatement(insertSql);

        selectStmt.setLong(1, userId);
        ResultSet rs = selectStmt.executeQuery();
        while (rs.next())
        {
            long id = rs.getLong("id");
            long entityId = rs.getLong("user_id");
            String key = rs.getString("property_key");
            String propertyValue = rs.getString("property_value");
            insertStmt.setLong(1, id);
            insertStmt.setLong(2, entityId);
            insertStmt.setLong(3, directoryId);
            insertStmt.setString(4, key);
            insertStmt.setString(5, propertyValue);
            insertStmt.setString(6, propertyValue == null ? null : propertyValue.toLowerCase());
            Savepoint savePoint = null;
            try
            {
                savePoint = setSavepoint(connection);  //Name required for HSSQL
                insertStmt.execute();
                releaseSavepoint(connection, savePoint);
            }
            catch (SQLException sqlex)
            {
                log.warn("Attribute not migrated. User Id : " + entityId + ", attribute key : " + key + ". Error: " + sqlex.getMessage());
                rollBackSavePoint(connection, savePoint);
            }
        }
        rs.close();
        selectStmt.close();
        insertStmt.close();
        return true;
    }

    /**
     * Find or create if required an External Entity for the Crowd User
     * @param crowdUserId User ID for the User being migrated.  This is preserved from userbase table during the migration.
     * @param connection Database connection
     *
     * @throws SQLException Database error.
     * @return The new or existing ExternalEntityId for this user.
     */
    private int getExternalEntityId(final Connection connection, final int crowdUserId)
            throws SQLException, UserNotFoundException
    {
        String selectCrowdUser = "select user_name from " + convertToSchemaTableName("cwd_user") + " where id = ?";
        String selectExternalEntity = "select id from " + convertToSchemaTableName("external_entities") + " where name = ? and entitytype = 'com.atlassian.jira.user.OfbizExternalEntityStore'";
        String insertExternalEntity = "insert into " + convertToSchemaTableName("external_entities") + " (id, name, entitytype) values(?, ?, 'com.atlassian.jira.user.OfbizExternalEntityStore')";

        PreparedStatement stmtCrowdUser = connection.prepareStatement(selectCrowdUser);
        PreparedStatement stmtExternalEntity = connection.prepareStatement(selectExternalEntity);
        PreparedStatement stmtInsertExternalEntity = connection.prepareStatement(insertExternalEntity);

        try
        {
// Get the user's name from the crowd user table
            String name;
            stmtCrowdUser.setInt(1, crowdUserId);
            ResultSet rs = stmtCrowdUser.executeQuery();
            try
            {
                if (rs.next())
                    {
                        name = rs.getString("user_name");
                    }
                    else
                    {
                        throw new UserNotFoundException(String.valueOf(crowdUserId));
                    }
            }
            finally
            {
                rs.close();
            }

            // Try and find an External Entity for this user
            stmtExternalEntity.setString(1, name);
            ResultSet rs2 = stmtExternalEntity.executeQuery();
            try
            {
                if (rs2.next())
                {
                    return rs2.getInt("id");
                }
                else
                {
                    maxExternalEntityId++;
                    stmtInsertExternalEntity.setInt(1, maxExternalEntityId);
                    stmtInsertExternalEntity.setString(2, name);
                    stmtInsertExternalEntity.execute();
                }
                return maxExternalEntityId;
            }
            finally
            {
                rs2.close();
            }
        }
        finally
        {
            stmtCrowdUser.close();
            stmtExternalEntity.close();
            stmtInsertExternalEntity.close();
        }
    }

    /**
     * Migrate the groupbase table to the cwd_group table.
     * We duplicate the groups across the internal and any delegating ldap directories
     *
     * @param connection Database Connection
     * @throws SQLException SQL exception
     */
    private boolean migrateGroups(final Connection connection) throws SQLException
    {
        // Migrate internal directory
        boolean ok =  true;
        if (internalDirectoryExists)
        {
            ok = migrateGroups(connection, internalDirectoryID);
        }
        // Migrate internal directory
        if (ok && delegatingDirectoryExists)
        {
            for (RemoteDirectory remoteDirectory : remoteDirectories)
            {
                ok &= migrateGroups(connection, remoteDirectory.getDirectoryId());
            }
        }
        // Update the sequence table
        sequences.update(connection, "Group", "cwd_group");
        return ok;
    }

    /**
     * Migrate the groupbase table to the cwd_group table
     *
     *
     * @param connection Database Connection
     * @param directoryId
     * @throws SQLException SQL exception
     */
    private boolean migrateGroups(final Connection connection, long directoryId) throws SQLException
    {
        String selectSql = " select id, groupname from " + convertToSchemaTableName("groupbase");

        String insertSql = "insert into " + convertToSchemaTableName("cwd_group") + " (id, group_name, lower_group_name, active, local, created_date, updated_date, description, group_type, directory_id) "
                + "  values (?, ?, ?, 1, 0, current_timestamp, current_timestamp, '', 'GROUP' ,?)";

        PreparedStatement selectStmt = connection.prepareStatement(selectSql);
        PreparedStatement insertStmt = connection.prepareStatement(insertSql);

        ResultSet rs = selectStmt.executeQuery();
        boolean ok = true;
        while (rs.next())
        {
            int id = nextGroupId++;
            String groupname = rs.getString("groupname");
            insertStmt.setInt(1, id);
            insertStmt.setString(2, groupname);
            insertStmt.setString(3, groupname.toLowerCase());
            insertStmt.setLong(4, directoryId);
            Savepoint savePoint = null;
            try
            {
                savePoint = setSavepoint(connection);  //Name required for HSSQL
                insertStmt.execute();
                releaseSavepoint(connection, savePoint);
            }
            catch (SQLException sqlex)
            {
                log.warn("Group not migrated. Group: " + groupname + ". Error: " + sqlex.getMessage());
                rollBackSavePoint(connection, savePoint);
                if (sqlex.getSQLState() != null && sqlex.getSQLState().startsWith("23"))
                {
                    addError(getI18nBean().getText("admin.errors.upgrade.602.duplicate.group", groupname, upgradeGuideUrl, upgradeGuideTitle));
                }
                else
                {
                    addError(getI18nBean().getText("admin.errors.upgrade.602.bad.group", groupname, sqlex.getMessage()));
                }
                ok = false;
            }
        }
        rs.close();
        selectStmt.close();
        insertStmt.close();

        return ok;
    }

    /**
     * Migrate the property entries on OSGroup over to Group.
     * This is done for "Group Display Names". See TestGroupSelectorPermissions, GroupSelectorUtils and groupnames.jsp.
     *
     *
     * @param connection Database Connection
     * @throws SQLException SQL exception
     */
    private boolean migrateGroupPropertySets(final Connection connection) throws SQLException
    {
        String sql = "update " + convertToSchemaTableName("propertyentry")
                     + " set entity_name = 'Group'"
                     + " where entity_name = 'OSGroup'";

        Statement stmt = connection.createStatement();
        stmt.execute(sql);
        stmt.close();
        return true;
    }

    /**
     * Migrate the membershipbase table to the cwd_membership table.
     *
     * @param connection Database Connection
     * @throws SQLException SQL exception
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING", justification="There is no risk of external SQL injection")
    private boolean migrateMemberships(final Connection connection) throws SQLException
    {
        String selectSql = "select mb.id as id, cg.id as groupid, cu.id as userid, cu.user_name as username, cu.directory_id as directoryid, cg.group_name as groupname, cu.user_name as username "
                + "  from " + convertToSchemaTableName("membershipbase") + " mb, " + convertToSchemaTableName("cwd_user") + " cu, " + convertToSchemaTableName("cwd_group") + " cg "
                + "  where mb.group_name = cg.group_name and mb.user_name = cu.user_name and cu.directory_id = cg.directory_id";

        String insertSql = "insert into " + convertToSchemaTableName("cwd_membership") + " (id, parent_id, child_id, membership_type, group_type, parent_name, lower_parent_name, child_name, lower_child_name, directory_id) "
                + "  values (?, ?, ?, 'GROUP_USER', NULL, ?, ?, ?, ?, ?)";

        PreparedStatement selectStmt = connection.prepareStatement(selectSql);
        PreparedStatement insertStmt = connection.prepareStatement(insertSql);

        ResultSet rs = selectStmt.executeQuery();
        while (rs.next())
        {
            int id = rs.getInt("id");
            int groupId = rs.getInt("groupid");
            int userId = rs.getInt("userid");
            String userName = rs.getString("username");
            int directoryId = rs.getInt("directoryid");
            String groupName = rs.getString("groupname");
            insertStmt.setInt(1, id);
            insertStmt.setInt(2, groupId);
            insertStmt.setInt(3, userId);
            insertStmt.setString(4, groupName);
            insertStmt.setString(5, groupName.toLowerCase());
            insertStmt.setString(6, userName);
            insertStmt.setString(7, userName.toLowerCase());
            insertStmt.setInt(8, directoryId);
            Savepoint savePoint = null;
            try
            {
                connection.getHoldability();
                savePoint = setSavepoint(connection);  //Name required for HSSQL
                insertStmt.execute();
                releaseSavepoint(connection, savePoint);
            }
            catch (SQLException sqlex)
            {
                log.warn("Membership not migrated. Group: " + groupName + " User: " + userName + ". Error: " + sqlex.getMessage());
                rollBackSavePoint(connection, savePoint);
            }
        }
        rs.close();
        selectStmt.close();
        insertStmt.close();

        sequences.update(connection, "Membership", "cwd_membership");
        return true;
    }

    /**
     * Check if we already have any crowd users or groups or memberships.
     *
     * @param connection Database Connection
     * @return true if any rows exist in any of the crowd tables we are going to populate.
     * @throws SQLException SQL exception
     */
    private boolean crowdAlreadyInitialised(Connection connection) throws SQLException
    {
        return !isTableEmpty(connection, "cwd_user")
                || !isTableEmpty(connection, "cwd_group")
                || !isTableEmpty(connection, "cwd_membership");
    }

    /**
     * Checks if the table has zero rows.
     *
     * @param connection Database Connection
     * @param tableName Table to check
     * @return True if the table is empty.
     * @throws SQLException SQL exception
     */
    private boolean isTableEmpty(final Connection connection, final String tableName) throws SQLException
    {
        Statement stmt = connection.createStatement();
        String sql = "select count(*) from " + convertToSchemaTableName(tableName);
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        int count = rs.getInt(1);

        rs.close();
        stmt.close();
        return (count == 0);
    }

    private void checkForTruncatedData(final Connection connection, String tableName) throws SQLException
    {
        Statement stmt = connection.createStatement();

        final String condition;
        if (isMSSQL())
        {
            condition = "LEN(cast(propertyvalue as varchar(max))) > 255";
        }
        else if (isMYSQL())
        {
            condition = "CHAR_LENGTH(propertyvalue) > 255";
        }
        else
        {
            condition = "LENGTH(propertyvalue) > 255";
        }
        String sql = "select count(*) from " + convertToSchemaTableName(tableName) + " where " + condition;

        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        stmt.close();


        if (count > 0)
        {
            log.warn(String.valueOf(count) + " instances of user attribute data have been truncated at 255 characters. The first " + Math.min(5, count) + " samples are listed below");

            stmt = connection.createStatement();
            sql = "select propertyvalue from " + convertToSchemaTableName(tableName) + " where " +condition;
            rs = stmt.executeQuery(sql);
            for (int i = 0; i < 5 && rs.next(); i++ )
            {
                log.warn(rs.getString(1));
            }
            rs.close();
            stmt.close();
        }
    }

    private Savepoint setSavepoint(Connection connection) throws SQLException
    {
        if (useSavePoints)
        {
            return connection.setSavepoint("atlassian");
        }
        return null;
    }

    private void releaseSavepoint(Connection connection, Savepoint savePoint) throws SQLException
    {
        if (useSavePoints)
        {
            connection.releaseSavepoint(savePoint);
        }
    }

    private void rollBackSavePoint(Connection connection, Savepoint savePoint) throws SQLException
    {
        if (useSavePoints)
        {
            if (savePoint != null)
            {
                connection.rollback(savePoint);
            }
        }
    }

    private interface AttributeConverter
    {
        String convert(Object o) throws SQLException;
    }
}
