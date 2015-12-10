define('jira/ajs/list/group-descriptor', [
    'jira/ajs/descriptor',
    'jira/ajs/list/item-descriptor'
], function(
    Descriptor,
    ItemDescriptor
) {
    /**
     * The group descriptor is used in {@link QueryableDropdownSelect} to define characteristics and display
     * of groups of items added to suggestions dropdown and in the case of {@link QueryableDropdownSelect} and
     * {@link SelectModel} also.
     *
     * @class GroupDescriptor
     * @extends Descriptor
     */
    return Descriptor.extend({

        /**
         * Defines default properties
         *
         * @method _getDefaultOptions
         * @return {Object}
         */
        _getDefaultOptions: function () {
            return {
                showLabel: true,
                label: "",
                items: []
            };
        },

        placement: function () {
            return this.properties.placement;
        },

        /**
         * Gets styleClass, in the case of {@link QueryableDropdownSelect} these are the classNames that will be applied to the
         * &lt;div&gt; surrounding a group of suggestions.
         *
         * @method styleClass
         * @return {String}
         */
        styleClass: function () {
            return this.properties.styleClass;
        },

        /**
         * Gets weight, in the case of {@link QueryableDropdownSelect} this defines the order in which the group is appended in
         * the &lt;optgroup&gt; and as a result displayed in the suggestions.
         *
         * @method weight
         * @return {Number}
         */
        weight: function () {
            return this.properties.weight;
        },

        /**
         * Gets label, in the case of {@link QueryableDropdownSelect} this is the heading that is displayed in the suggestions
         *
         * @method label
         * @return {String}
         */
        label: function () {
            return this.properties.label;
        },

        /**
         * Unselectable Li appended to bottom of list
         * @return {String}
         */
        footerText: function (footerText) {
            if (footerText) {
                this.properties.footerText = footerText;
            } else {
                return this.properties.footerText;
            }
        },

        /**
         * Unselectable Li appended to bottom of list
         * @return {String}
         */
        footerHtml: function (footerHtml) {
            if (footerHtml) {
                this.properties.footerHtml = footerHtml;
            } else {
                return this.properties.footerHtml;
            }
        },

        /**
         * Prepended to group list; used for "Clear All" link
         * @param {string=} actionBarHtml
         * @return {string}
         */
        actionBarHtml: function (actionBarHtml) {
            if (actionBarHtml) {
                this.properties.actionBarHtml = actionBarHtml;
            }
            return this.properties.actionBarHtml;
        },

        /**
         * Determines if the label should be shown or not, in the case of {@link QueryableDropdownSelect} this is used when we have
         * a suggestion that mirrors that of the user input. It sits in a seperate group but we do not want a heading for it.
         *
         * @method showLabel
         * @return {Boolean}
         */
        showLabel: function () {
            return this.properties.showLabel;
        },

        /**
         * Gets items, in the case of {@link QueryableDropdownSelect} and subclasses these are instances of {@link ItemDescriptor}.
         * These items are used to describe the elements built as &lt;option&gt;'s in {@link SelectModel} and suggestion
         * items built in {@link List}
         *
         * @method items
         * @return {ItemDescriptor[]}
         */
        items: function (items) {
            if (items) {
                this.properties.items = items;
                return this;
            } else {
                return this.properties.items;
            }
        },

        /**
         * Adds item to the items array.
         *
         * @method addItem
         * @param {ItemDescriptor} item
         */
        addItem: function (item) {
            this.properties.items.push(item);
            return this;
        },

        /**
         * @return a unique id
         */
        id: function () {
            return this.properties.id;
        },


        /**
         * Sets model, in the
         *
         * @param {jQuery} $model
         */
        setModel: function ($model) {
            this.properties.model = $model;
        },


        replace: function () {
            return this.properties.replace;
        },

        /**
         * Defines a scope within which items in this Group must be unique; allowed values are:
         *
         * - 'group':     (default) the item must be unique in this group
         * - 'container': the item must be unique in this Group *and* its container
         * - 'none':      the item does not need to be unique.
         *
         * The setting here may be overridden by the {@link ItemDescriptor#allowDuplicate} property.
         */
        uniqueItemScope: function () {
            return this.properties.uniqueItemScope;
        },


        description: function () {
            return this.properties.description;
        },

        /**
         * Gets or sets model, in the case of {@link SelectModel} gets jQuery wrapped &lt;optgroup&gt; element
         *
         * @method model
         *
         * @return {jQuery}
         */
        model: function($model) {
            if ($model) {
                this.properties.model = $model;
            }
            else {
                return this.properties.model;
            }
        }
    });
});

AJS.namespace('AJS.GroupDescriptor', null, require('jira/ajs/list/group-descriptor'));
