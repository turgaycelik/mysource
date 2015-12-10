define('jira/dialog/edit-profile-dialog', [
    'jira/dialog/user-profile-dialog',
    'jquery'
], function(
    UserProfileDialog,
    jQuery
) {
    /**
     * @class EditProfileDialog
     * @extends UserProfileDialog
     */
    return UserProfileDialog.extend({
        _handleSubmitResponse: function (data, xhr, smartAjaxResult) {
            if (this.serverIsDone) {
                this._updateName();
                this._updateMail();
                this._super(data, xhr, smartAjaxResult);
            }
        },
        _updateName: function () {
            var oldName = jQuery("#up-d-fullname").text();
            var name = jQuery("#edit-profile-fullname").val();
            jQuery("#up-d-fullname").text(name);
            jQuery("#up-user-title-name").text(name);
            jQuery("a[href*='ViewProfile.jspa']").each(function() {
                var $el = jQuery(this);
                if ($el.text().indexOf(oldName) >= 0) $el.text(name);
            });
            if (window.frames['gadget-1'] && window.frames['gadget-1'].AJS){
                window.frames['gadget-1'].jQuery("a[href*='ViewProfile.jspa']").each(function() {
                    var $el = jQuery(this);
                    if ($el.text().indexOf(oldName) >= 0) $el.text(name);
                });
            }
        },
        _updateMail: function () {
            var email = jQuery("#edit-profile-email").val();
            var emailDiv = jQuery("#up-d-email");
            if (emailDiv.find("a").length === 0){
                if (/\sat\s.*\sdot\s/.test(emailDiv.text())){
                    emailDiv.text(email.replace(/@/g, " at ").replace(/\./g, " dot "));
                } else {
                    emailDiv.text(email);
                }
            } else {
                emailDiv.find("a").attr("href", "mailto:" + email).text(email);
            }
        }
    });

});

AJS.namespace('JIRA.EditProfileDialog', null, require('jira/dialog/edit-profile-dialog'));
