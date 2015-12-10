define('jira/ajs/list/item-descriptor-factory', [
    'jira/ajs/list/item-descriptor',
    'jira/ajs/list/message-descriptor'
], function(
    ItemDescriptor,
    MessageDescriptor
) {

    /**
     * Factory method.
     *
     * This method knows how to create a {@link ItemDescriptor} and descendant classes (delegating the creation to their
     * factories)
     * @param suggestion
     * @param groupIndex
     * @return {ItemDescriptor}
     */
    function createItemDescriptor(suggestion, groupIndex) {
        if (suggestion.isMessage) {
            return createMessageDescriptor(suggestion, groupIndex);
        } else {
            return new ItemDescriptor({
                label: suggestion.label,
                styleClass: suggestion.styleClass,
                value: suggestion.value,
                keywords: suggestion.keywords,
                meta: { groupIndex: groupIndex }
            })
        }
    }

    /**
     * Factory method.
     *
     * This method knows how to create a {@link MessageDescriptor} and descendant classes (delegating the creation to their
     * factories)
     * @param suggestion
     * @param groupIndex
     * @return {MessageDescriptor}
     */
    function createMessageDescriptor(suggestion, groupIndex) {
        return new MessageDescriptor({
            label: suggestion.label,
            styleClass: suggestion.styleClass,
            useAUI: suggestion.useAUI,
            messageID: suggestion.messageID,
            value: suggestion.value,
            keywords: suggestion.keywords,
            meta: { groupIndex: groupIndex }
        })
    }

    return {
        createItemDescriptor: createItemDescriptor,
        createMessageDescriptor: createMessageDescriptor
    };
});

(function(Factory) {
    AJS.namespace('AJS.ItemDescriptor.create', null, Factory.createItemDescriptor);
    AJS.namespace('AJS.MessageDescriptor.create', null, Factory.createMessageDescriptor);
})(require('jira/ajs/list/item-descriptor-factory'));
