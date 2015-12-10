AJS.$(function(){
    var quickLinks = function () {
        var $quicklinks = AJS.$("#quicklinks");
        AJS.Dropdown.create({
            trigger: $quicklinks.find(".aui-dd-link"),
            content: $quicklinks.find(".aui-list"),
            alignment: AJS.RIGHT
        });
        return arguments.callee;
    }();

    JIRA.VersionBlocks.init();

    new JIRA.EditProfileDialog({
        trigger: "#edit_profile_lnk",
        autoClose: true
    });

    new JIRA.UserProfileDialog({
        trigger: "#view_change_password",
        autoClose: true
    });

    new JIRA.UserProfileDialog({
        trigger: "#view_clear_rememberme",
        autoClose: true
    });

    new JIRA.EditPreferencesDialog({
        trigger: "#edit_prefs_lnk",
        autoClose: true
    });
});
