define('jira/project/project-edit-key', [
    'jira/lib/class',
    'jquery'
], function(
    Class,
    jQuery
) {
    /**
     * @class ProjectEditKey
     * @extends Class
     */
    return Class.extend({
        init: function($form){
            this.editKeyText = AJS.I18n.getText('admin.projects.edit.project.link.toggle.edit.key');
            this.revertText = AJS.I18n.getText('admin.projects.edit.project.link.toggle.revert');

            this.$link = $form.find("#edit-project-key-toggle");
            this.$input = $form.find("#project-edit-key");
            this.initialKey = $form.find('#edit-project-original-key').val();
            this.$keyEdited = $form.find('#edit-project-key-edited');
            this.$warning = $form.find('#edit-project-warning-message');

            if (this.$keyEdited.val()==="true") {
                this.$warning.show();
            }
        },
        toggle: function(){
            if (this.$keyEdited.val()==="true") {
                this.$keyEdited.val("false");
                this.$link.html(this.editKeyText);
                this.$input.attr("disabled", "disabled");
                this.$warning.hide();

                this.$input.val(this.initialKey);
                this.$input.siblings('.error').remove();
            } else {
                this.$keyEdited.val("true");
                this.$link.html(this.revertText);
                this.$input.removeAttr("disabled");
                this.$warning.show();
            }
        }
    });
});

AJS.namespace('ProjectEditKey', null, require('jira/project/project-edit-key'));
