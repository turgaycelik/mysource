(function($) {


    /**
     * A multi-select list for selecting recipients to Share an issue or filter with. Shares are to 2 types of recipients:
     * - Users: selected from a dropdown list, and
     * - Email: addresses typed out in full
     *
     * @constructor JIRA.MultiUserListPicker
     * @extends AJS.MultiSelect
     */
    JIRA.MultiUserListPicker = AJS.MultiSelect.extend({

        init: function (options) {

            var restPath = "/rest/api/1.0/users/picker";

            function formatResponse(response) {

                var ret = [];

                $(response).each(function(i, suggestions) {

                    var groupDescriptor = new AJS.GroupDescriptor({
                        weight: i, // order or groups in suggestions dropdown
                        label: suggestions.footer // Heading of group
                    });

                    $(suggestions.users).each(function(){
                        groupDescriptor.addItem(new AJS.ItemDescriptor({
                            value: this.name, // value of item added to select
                            label: this.displayName, // title of lozenge
                            html: this.html,
                            icon: this.avatarUrl,
                            allowDuplicate: false,
                            highlighted: true
                        }));
                    });

                    ret.push(groupDescriptor);
                });

                return ret;
            }

            $.extend(options, {
                itemAttrDisplayed: "label",
                userEnteredOptionsMsg: AJS.I18n.getText("common.form.email.label.suffix"),
                showDropdownButton: false,
                removeOnUnSelect: true,
                ajaxOptions: {
                    url: contextPath + restPath,
                    query: true,                // keep going back to the server for each keystroke
                    data: { showAvatar: true },
                    formatResponse: formatResponse
                },
                suggestionsHandler: AJS.UserListSuggestHandler,
                itemGroup: new AJS.Group(),
                itemBuilder: function (descriptor) {
                    return new JIRA.MultiUserListPicker.Item({
                        descriptor: descriptor,
                        container: this.$selectedItemsContainer
                    });
                }
            });

            this._super(options);
        },

        _createFurniture: function (disabled) {
            this._super(disabled);
            if (this.options.description) {
                this._render("description", this.options.description).insertAfter(this.$field);
            }
        },

        /**
         * The share textarea has no lozenges inside it and no need for cursor and indent nonsense.
         * It could even be a plain text field.
         */
        updateItemsIndent: $.noop,

        _renders: {
            selectedItemsWrapper: function () {
                return $('<div class="recipients"></div>');
            },
            selectedItemsContainer: function () {
                return $('<ol />');
            },
            description: function (description) {
                return $("<div />").addClass("description").text(description);
            }
        }

    });

}(AJS.$));