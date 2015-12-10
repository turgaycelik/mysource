define('jira/util/users/logged-in-user', [
    'jira/util/data/meta'
], function(
    meta
) {
    /**
     * A namespace containing functions to handle users,
     * groups and roles in JIRA.
     */
    var User = {};

    /**
     * Retrieves the user name the user that is currently logged in JIRA.
     *
     * @return A {String} containing the user name.
     */
    User.username = function() {
        return meta.get("remote-user");
    };

    /**
     * Retrieves the full name the user that is currently logged in JIRA.
     *
     * @returns {String} containing the full name of the user.
     */
    User.fullName = function() {
        return meta.get('remote-user-fullname');
    };

    /**
     * Whether the user that is currently logged in JIRA is anonymous or not.
     *
     * @return {Boolean} true if the currently logged in user is anonymous; otherwise false.
     */
    User.isAnonymous = function() {
        return meta.get("remote-user") === "";
    };

    /**
     * Determine whether the current user is a sysadmin.
     *
     * @returns {boolean}
     */
    User.isSysadmin = function () {
        return !!meta.getBoolean("is-sysadmin");
    };

    User.isAdmin = function() {
        return !!meta.getBoolean("is-admin");
    };

    return User;
});
