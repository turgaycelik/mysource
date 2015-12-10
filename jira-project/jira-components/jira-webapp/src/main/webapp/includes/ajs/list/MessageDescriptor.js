define('jira/ajs/list/message-descriptor', [
    'jira/ajs/list/item-descriptor',
    'jquery'
], function(
    ItemDescriptor,
    $
) {
    /**
     * The message descriptor is used in {@link QueryableDropdownSelect} to define characteristics and
     * display of items added to suggestions dropdown and in the case of {@link QueryableDropdownSelect}
     * and {@link SelectModel} also.
     *
     * It displays an AUI message instead a regular item
     *
     * @class MessageDescriptor
     * @extends ItemDescriptor
     */
    return ItemDescriptor.extend({
        /**
         * Gets the useAUI attribute
         *
         * @method useAUI
         * @return {Boolean}
         */
        useAUI: function () {
            return this.properties.useAUI;
        },

        /**
         * Gets message ID, used for the DOM Element
         *
         * @method domID
         * @return {String}
         */
        messageID: function () {
            return this.properties.messageID;
        },

        _getDefaultOptions: function() {
            return $.extend(this._super(), {
                useAUI: true
            });
        }
    });

});

AJS.namespace('AJS.MessageDescriptor', null, require('jira/ajs/list/message-descriptor'));