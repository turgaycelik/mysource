define('jira/shifter/shifter-select', [
    'jira/ajs/select/queryable-dropdown-select',
    'jira/ajs/list/list-with-messages',
    'jira/ajs/list/message-descriptor',
    'jquery',
    'underscore'
], function(
    QueryableDropdownSelect,
    ListWithMessages,
    MessageDescriptor,
    jQuery,
    _
) {
        return QueryableDropdownSelect.extend({
        /**
         * @param options.id
         * @param options.element
         * @param {ShifterGroup[]} options.groups
         * @param {Class} options.suggestionsHandler
         * @param {Function} options.onSelection - called with (group, value) when a suggestion is selected
         * @param {Number} options.maxSuggestionsPerGroup
         */
        init: function(options) {
            _.extend(options, {
                dropdownController: {
                    setWidth: jQuery.noop,
                    setPosition: jQuery.noop,
                    onhide: jQuery.noop,
                    hide: jQuery.noop,
                    show: jQuery.noop
                }
            });

            this._super(options);

            this.$field.attr('placeholder', AJS.I18n.getText('shifter.placeholder'));
            this.$dropDownIcon.click(_.bind(this.clear, this));
            this._handleCharacterInput();
        },

        onEdit: function() {
            this.toggleClearButton();
            this._handleCharacterInput();
        },

        clear: function() {
            this.$field.val('').focus();
            this.onEdit();
        },

        toggleClearButton: function() {
            this.$dropDownIcon.toggleClass('aui-iconfont-remove', !!this.$field.val());
        },

        showLoading: function() {
            this._super();
            this.$dropDownIcon.removeClass('aui-iconfont-remove');
        },

        hideLoading: function() {
            this._super();
            this.toggleClearButton();
        },

        _handleCharacterInput: function() {
            this._super(true);
            if (this.getQueryVal().length === 0) {
                this.listController.moveToFirst();
            }
        },

        _createListController: function () {
            var instance = this;
            this.listController = new ListWithMessages({
                containerSelector: this.options.element,
                groupSelector: "ul.aui-list-section",
                scrollContainer: ".aui-list-scroll",
                matchingStrategy: this.options.matchingStrategy,
                maxResultsDisplayedPerGroup: this.options.maxResultsDisplayedPerGroup,
                eventTarget: this.$field,
                hasLinks: false,
                renderers: {
                    suggestionGroupHeading: this._renders.suggestionGroupHeading
                },
                selectionHandler: function(e) {
                    var targetData = jQuery(e.currentTarget).data();
                    if (targetData && targetData.descriptor instanceof MessageDescriptor ) {
                        //If selected element is a message, do nothing
                        return false;
                    }
                    var selectedDescriptor = this.getFocused().data("descriptor");
                    var groupIndex = selectedDescriptor.meta().groupIndex;
                    var group = instance.options.groups[groupIndex];
                    var value = selectedDescriptor.value();
                    var label = selectedDescriptor.label();
                    instance.$dropDownIcon.removeClass('aui-iconfont-remove');
                    instance.$field.val(AJS.I18n.getText('common.concepts.loading')).prop('disabled', true);
                    instance.options.onSelection(group, value, label);
                }
            });
        },

        hideSuggestions: jQuery.noop,

        _renders: {
            suggestionGroupHeading: function (descriptor) {
                return jQuery(JIRA.Templates.Shifter.groupHeading({
                    groupName: descriptor.label(),
                    groupContext: descriptor.description()
                })).data('descriptor', descriptor);
            },
            dropdownAndLoadingIcon: function (showDropdown) {
                return jQuery('<span class="icon noloading aui-icon aui-icon-small"><span>More</span></span>');
            }
        }

    });

});

AJS.namespace('JIRA.ShifterComponent.ShifterSelect', null, require('jira/shifter/shifter-select'));
