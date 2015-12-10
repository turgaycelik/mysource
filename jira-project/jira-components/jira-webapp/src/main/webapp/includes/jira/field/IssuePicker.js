define('jira/field/issue-picker', [
    'jira/ajs/select/multi-select',
    'jira/ajs/list/group-descriptor',
    'jira/ajs/list/item-descriptor',
    'jquery'
], function(
    MultiSelect,
    GroupDescriptor,
    ItemDescriptor,
    jQuery
) {
    /**
     * A multiselect list for querying and selecting issues. Issues can also be selected via a popup.
     *
     * @class IssuePicker
     * @extends MultiSelect
     */
    var IssuePicker = MultiSelect.extend({

        /**
         *
         * Note: We could probably have the server return in a format that can be digested by appendOptionsFromJSON, but
         * we currently have a legacy issue picker that uses the same end point.
         *
         * @method _formatResponse
         * @param {Object} response
         */
        _formatResponse: function (response) {
            var ret = [],
                canonicalBaseUrl = (function(){
                    var uri = parseUri(window.location);
                    return uri.protocol + "://" + uri.authority;
                })();

            if (response && response.sections) {

                jQuery(response.sections).each(function(i, section) {

                    var groupDescriptor = new GroupDescriptor({
                        weight: i, // order or groups in suggestions dropdown
                        label: section.label, // Heading of group
                        description: section.sub // description for the group heading
                    });

                    if (section.issues && section.issues.length > 0){

                        jQuery(section.issues).each(function(){

                            groupDescriptor.addItem(new ItemDescriptor({
                                highlighted: true,
                                value: this.key, // value of item added to select
                                label: this.key + " - " + this.summaryText, // title of lozenge
                                icon: this.img ? canonicalBaseUrl + contextPath + this.img : null, // Need to have the canonicalBaseUrl for IE7 to avoid mixed content warnings when viewing the issuepicker over https
                                html: this.keyHtml + " - " + this.summary // html used in suggestion
                            }));
                        });
                    }

                    ret.push(groupDescriptor);

                });
            }

            return ret;
        },


        /**
         * Gets default options
         *
         * @method _getDefaultOptions
         * @protected
         * @return {Object}
         */
        _getDefaultOptions: function () {
            return jQuery.extend(true, this._super(), {
                ajaxOptions: {
                    formatResponse: this._formatResponse
                }
            });
        },

        /**
         * Launches a popup window, where issues can be fixed based on filter/history and current search. Installs
         * a callback in the current window that can be used by the popup window to add items to the control.
         *
         * @method _launchPopup
         * @override
         */
        _launchPopup: function () {

            function getWithDefault(value, def) {
                if(typeof value == "undefined" || value == null){
                    return def;
                } else {
                    return value;
                }
            }

            var url, urlParam, vWinUsers, options, instance = this;

            IssuePicker.callback = function (items) {
                if (typeof items === "string") {
                    items = JSON.parse(items);
                }
                instance._addMultipleItems(items, true);
                instance.$field.focus();
            };

            options = this.options.ajaxOptions.data;
            url = contextPath + '/secure/popups/IssuePicker.jspa?';
            urlParam = {
                singleSelectOnly: "false",
                decorator: "popup",
                currentIssue: options.currentIssueKey || "",
                showSubTasks: getWithDefault(options.showSubTasks, false),
                /* Note the slightly different option name here showSubTasksParent vs. showSubTaskParent */
                showSubTasksParent: getWithDefault(options.showSubTaskParent, false)
            };

            if (options.currentProjectId) {
                urlParam["currentProjectId"] = options.currentProjectId;
            }

            url += jQuery.param(urlParam);

            vWinUsers = window.open(url, 'IssueSelectorPopup', 'status=no,resizable=yes,top=100,left=200,width=' + this.options.popupWidth + ',height=' + this.options.popupHeight + ',scrollbars=yes,resizable');
            vWinUsers.opener = self;
            vWinUsers.focus();
        },

        /**
         * Adds popup link next to picker and assigns event to open popup window
         *
         * @param {Boolean} disabled - Adds a standard text box instead of ajax picker if set to true
         * @override
         */
        _createFurniture: function (disabled) {
            var $popupLink;

            this._super(disabled);

            $popupLink = this._render("popupLink");

            this._assignEvents("popupLink", $popupLink);
            this.$container.addClass('jira-issue-picker');
            this.$container.addClass('hasIcon');
            this.$container.after($popupLink);

        },

        handleFreeInput: function() {
            var values = this.$field.val().toUpperCase().match(/\S+/g);

            if (values) {
                this._addMultipleItems(jQuery.map(values, function(value) {
                    return { value: value, label: value };
                }));
            }

            this.$field.val("");
        },

        _events: {
            popupLink: {
                click: function (e) {
                    this._launchPopup();
                    e.preventDefault();
                }
            }
        },

        _renders: {
            popupLink: function () {
                return jQuery("<a class='issue-picker-popup' />")
                        .attr({
                            href: "#",
                            title: this.options.popupLinkMessage
                        })
                        .text("" + this.options.popupLinkMessage + "");
            }
        }

    });

    IssuePicker.callback = null;

    return IssuePicker;
});

/** @deprecated */
AJS.namespace("jira.issuepicker", null, require('jira/field/issue-picker'));
/** @deprecated */
AJS.namespace("AJS.IssuePicker", null, require('jira/field/issue-picker'));
AJS.namespace('JIRA.IssuePicker', null, require('jira/field/issue-picker'));
