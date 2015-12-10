define('jira/field/assignee-picker', [
    'jira/ajs/select/single-select',
    'jira/ajs/select/suggestions/assignee-suggest-handler',
    'jira/ajs/list/group-descriptor',
    'jira/ajs/list/item-descriptor',
    'aui/params',
    'jquery'
], function(
    SingleSelect,
    AssigneeSuggestHandler,
    GroupDescriptor,
    ItemDescriptor,
    params,
    $
) {
    /**
     * A single-select list for selecting Assignees. Assignees in the list are in two groups:
     *
     * - Suggestions: local data, consisting of recent assignees for the issue and current
     *                user, plus the reporter
     * - Search: AJAX data, found from all user assignable for the current project
     *
     * @class AssigneePicker
     * @extends SingleSelect
     */
    return SingleSelect.extend({

        init: function (options) {

            var element = options.element;

            // Returns the data sent to the server for the AJAX search
            function data(query) {
                params.actionDescriptorId = undefined;

                AJS.populateParameters();
                return {
                    username: query,
                    projectKeys: params.projectKeys,
                    issueKey: params.assigneeEditIssueKey,
                    actionDescriptorId: params.actionDescriptorId,
                    maxResults:1000
                };
            }

            function formatResponse(response) {
                var ret = [];
                if (response.length) {
                    // Search results
                    var groupDescriptor = new GroupDescriptor({
                        weight: 1,          // index of group in dropdown
                        id: "assignee-group-search",
                        uniqueItemScope: 'container',
                        replace: true,     // Allow subsequent calls to replace model items
                        label: AJS.I18n.getText("assignee.picker.group.search")
                    });

                    for (var i = 0, len = response.length; i < len; i++) {
                        var user = response[i];

                        var username = user.name;
                        var displayName = user.displayName;
                        var emailAddress = user.emailAddress;
                        var label = displayName + ' - ' + emailAddress + ' (' + username + ')';

                        groupDescriptor.addItem(new ItemDescriptor({
                            value: username,
                            fieldText: displayName,
                            label: label,
                            allowDuplicate: false,
                            icon: user.avatarUrls['16x16']
                        }));
                    }
                    ret.push(groupDescriptor);
                }
                return ret;
            }

            $.extend(options, {
                submitInputVal: true,
                showDropdownButton: !!element.data('show-dropdown-button'),
                errorMessage: AJS.I18n.getText("assignee.picker.invalid.user"),
                localDataGroupId: 'assignee-group-suggested',
                content: "mixed",
                removeDuplicates: true,
                ajaxOptions: {
                    url: function() {
                        //reset the assigneeEditIssueKey param, so that when we go from an quickedit dialog to a quick create dialog for
                        //example the value isn't set!
                        params.assigneeEditIssueKey = undefined;
                        AJS.populateParameters();

                        var path = params.assigneeEditIssueKey ? 'search' : 'multiProjectSearch';
                        return contextPath + "/rest/api/latest/user/assignable/" + path;
                    },
                    query: true,                // keep going back to the server for each keystroke
                    data: data,
                    formatResponse: formatResponse
                }
            });

            if(options.editValue == "-1") {
                // override value in "data-editValue" for SingleSelect._setOptions -> getOptionsFromAttributes()
                // so on error the "Automatic" is selected instead of verbatim "-1" string in SingleSelect._setInitState
                options.editValue = false;
                element.val("-1");
            }

            this._super(options);
            this.suggestionsHandler = new AssigneeSuggestHandler(this.options, this.model);
        },

        /**
         * Handle the case where the entry was deleted - on blur, set the Assignee to 'Automatic'.
         */
        handleFreeInput: function(value) {
            if ("" === $.trim(value || this.$field.val())) {
                this.setSelection(this.model.getDescriptor("-1"));
            } else {
                this._super(value);
            }
        },

        /**
         * Assignee Picker is a special case as we have <option>s that are prepopulated and ones that are requested from the
         * server. Our super class will remove all of our <option>s whenever we make a new request as it is expecting that
         * because we go to the server all our <option>s will be populated from the server also. This is not the case
         * overriding this method fixes this issue. If we do not override we get JRADEV-8626.
         */
        cleanUpModel: function () {}

    });
});

AJS.namespace('JIRA.AssigneePicker', null, require('jira/field/assignee-picker'));