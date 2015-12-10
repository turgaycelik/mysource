define('jira/ajs/select/security-level-select', [
    'jira/ajs/select/dropdown-select',
//    'aui',
    'jquery'
], function(
    DropdownSelect,
//    AJS,
    jQuery
) {
    /**
     * Provides a menu specifically for the comment security level.
     * @class SecurityLevelSelect
     * @extends DropdownSelect
     */
    return DropdownSelect.extend({

        _createFurniture: function () {
            AJS.populateParameters();

            this._super();
        },

        _selectionHandler: function (selected) {

            var descriptor = selected.data("descriptor");
            var $triggerIcon = this.$trigger.find("span:first");
            var $currentLevel = this.$container.parent().find(".current-level");
            var commentIsViewableByAllUsers = descriptor && !descriptor.value();

            if(commentIsViewableByAllUsers) {
                $triggerIcon.removeClass("icon-locked").addClass("icon-unlocked");

                var securityLevelViewableByAll = AJS.I18n.getText("security.level.viewable.by.all");
                $triggerIcon.text(AJS.I18n.getText("security.level.explanation", securityLevelViewableByAll));
                $currentLevel.text(securityLevelViewableByAll);
            } else {
                $triggerIcon.removeClass("icon-unlocked").addClass("icon-locked");

                var htmlEscapedLabel = jQuery("<div/>").text(descriptor.label()).html();
                var specificSecurityLevelAsHtmlFormat = AJS.format(AJS.I18n.getText("security.level.restricted.to"), htmlEscapedLabel);
                var stripHtmlSecurityLevelLabel = jQuery("<div/>").html(specificSecurityLevelAsHtmlFormat).text();
                $triggerIcon.text(AJS.I18n.getText("security.level.explanation", stripHtmlSecurityLevelLabel));
                $currentLevel.html(specificSecurityLevelAsHtmlFormat);
            }

            this._super(selected);
        },

        _handleDownKey: function(e) {
            //if the dropdown isn't open yet, pressing down should open it!
            if(e.keyCode === jQuery.ui.keyCode.DOWN && !this.dropdownController.isVisible()) {
                e.preventDefault();
                e.stopPropagation();
                this.show();
            }
        },

        _events: {
            trigger:  {
                keydown: function (e) {
                    this._handleDownKey(e);
                },
                keypress: function (e) {
                    this._handleDownKey(e);
                }
            }
        }
    });

});

/** Preserve legacy namespace
    @deprecated AJS.SecurityLevel*/
AJS.namespace("AJS.SecurityLevel", null, require('jira/ajs/select/security-level-select'));
AJS.namespace('AJS.SecurityLevelSelect', null, require('jira/ajs/select/security-level-select'));
