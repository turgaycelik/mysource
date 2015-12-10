define('jira/field/label-picker', [
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
     * @class LabelPicker
     * @extends MultiSelect
     */
    return MultiSelect.extend({

        _getDefaultOptions: function () {
            return jQuery.extend(true, this._super(), {
                ajaxOptions: {
                    url: contextPath + "/includes/js/ajs/layer/labeldata.js",
                    query: true,
                    formatResponse: this._formatResponse
                },
                removeDuplicates: true,
                removeOnUnSelect: true,
                userEnteredOptionsMsg: AJS.I18n.getText("label.new")
            });
        },

        isValidItem: function(itemValue) {
            return !/\s/.test(itemValue);
        },

        _handleServerSuggestions: function (data) {
            // if the suggestions brought back from the server include the original token and it doesn't match with the
            // token provided by the user disregard the suggestions
            if(data && data.token) {
                if(jQuery.trim(this.$field.val()) !== data.token) {
                    return;
                }
            }
            this._super(data);
        },

        _handleSpace: function () {
            if(jQuery.trim(this.$field.val()) !== "") {
                if(this.handleFreeInput()) {
                    this.hideSuggestions();
                }
            }
        },

        keys: {

            //if the user presses space, turn the text entered into labels.
            //if they pressed enter and the dropdown is *not* visible, then also turn text into labels.  Otherwise if the
            //dropdown is visibly enter should just select the item from the dropdown.
            "Spacebar": function (event) {
                this._handleSpace();
                event.preventDefault();
            }
        },

        _formatResponse: function (data) {

            var optgroup = new GroupDescriptor({
                label: AJS.I18n.getText("common.words.suggestions"),
                type: "optgroup",
                styleClass: 'labels-suggested'
            });

            if (data && data.suggestions) {
                jQuery.each(data.suggestions, function () {
                    optgroup.addItem(new ItemDescriptor({
                        value: this.label,
                        label: this.label,
                        html: this.html,
                        highlighted: true
                    }));
                });
            }
            return [optgroup];
        },

        handleFreeInput: function() {
            var values = jQuery.trim(this.$field.val()).match(/\S+/g);

            if (values) {
                // If there are multiple space-separated values, add them separately.
                for (var value, i = 0; value = values[i]; i++) {
                    this.addItem({ value: value, label: value });
                }
                this.model.$element.trigger("change");
            }

            this.$field.val("");
        }
    });

});

/** Preserve legacy namespace
 @deprecated AJS.LabelPicker */
AJS.namespace("AJS.LabelPicker", null, require('jira/field/label-picker'));
AJS.namespace('JIRA.LabelPicker', null, require('jira/field/label-picker'));
