package com.atlassian.jira.functest.framework.admin;

/**
 * Actions to be performed on the permission schemes in JIRA's administration.
 *
 * @since v4.0
 */
public interface PermissionSchemes
{
    /**
     * Navigates to the Default Permission Scheme.
     * @return the Default Permission Scheme to operate on.
     */
    PermissionScheme defaultScheme();

    /**
     * Navigates to the scheme with the specified name.
     * @param schemeName the permission scheme name.
     * @return the Permission Scheme with the given name.
     */
    PermissionScheme scheme(String schemeName);

    class Type
    {
        public static final int CREATE_ISSUES = 11;
        public static final int EDIT_ISSUES = 12;
    }

    /**
     * Represents a permission scheme that actions can be carried out on
     */
    interface PermissionScheme
    {
        /**
         * @deprecated Use {@link #grantPermissionToGroup(String, String)}
         */
        @Deprecated
        void grantPermissionToGroup(int permission, String groupName);

        void grantPermissionToGroup(String permission, String groupName);

        /**
         * @deprecated Use {@link #grantPermissionToSingleUser(String, String)}
         */
        @Deprecated
        void grantPermissionToSingleUser(int permission, String username);

        void grantPermissionToSingleUser(String permission, String username);

        /**
         * @deprecated Use {@link #grantPermissionToReporter(String)}
         */
        @Deprecated
        void grantPermissionToReporter(int permission);

        void grantPermissionToReporter(String permission);

        /**
         * @deprecated Use {@link #grantPermissionToProjectLead(String)}
         */
        @Deprecated
        void grantPermissionToProjectLead(int permission);

        void grantPermissionToProjectLead(String permission);

        /**
         * @deprecated Use {@link #grantPermissionToCurrentAssignee(String)}
         */
        @Deprecated
        void grantPermissionToCurrentAssignee(int permission);

        void grantPermissionToCurrentAssignee(String permission);

        /**
         * @deprecated Use {@link #grantPermissionToUserCustomFieldValue(String, String)}
         */
        @Deprecated
        void grantPermissionToUserCustomFieldValue(int permission, String customFieldId);

        void grantPermissionToUserCustomFieldValue(String permission, String customFieldId);

        /**
         * @deprecated Use {@link #grantPermissionToGroupCustomFieldValue(String, String)}
         */
        @Deprecated
        void grantPermissionToGroupCustomFieldValue(int permission, String customFieldId);

        void grantPermissionToGroupCustomFieldValue(String permission, String customFieldId);

        /**
         * @deprecated Use {@link #grantPermissionToProjectRole(String, String)}
         */
        @Deprecated
        void grantPermissionToProjectRole(int permission, String projectRoleId);

        void grantPermissionToProjectRole(String permission, String projectRoleId);

        /**
         * Remove the given permission setting.
         *
         * @param permissionType the permission type. See {@link Type} for constants.
         * @param permissionParam the permission parameter. eg group-name for group based permissions, ID for project roles
         * @deprecated Use {@link #removePermission(String, String)}
         */
        @Deprecated
        void removePermission(int permissionType, String permissionParam);

        /**
         * Remove the given permission setting.
         *
         * @param permissionType the permission type. See {@link Type} for constants.
         * @param permissionParam the permission parameter. eg group-name for group based permissions, ID for project roles
         */
        void removePermission(String permissionType, String permissionParam);
    }
}

