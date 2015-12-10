define('jira/ajs/list/item-descriptor', [
    'jira/ajs/descriptor',
    'jquery'
], function(
    Descriptor,
    jQuery
) {
    /**
     * The item descriptor is used in {@see QueryableDropdownSelect} to define characteristics and
     * display of items added to suggestions dropdown and in the case of {@see QueryableDropdownSelect}
     * and {@see SelectModel} also.
     *
     * @class ItemDescriptor
     * @extends Descriptor
     */
    return Descriptor.extend({

        init: function (options) {
            if (options && typeof options.meta === "object") {
                // normalise
                options.meta = JSON.stringify(options.meta);
            }
            this._super(options);
        },

        /**
         * Defines properties required during invocation to form a valid descriptor.
         *
         * @property {Object} REQUIRED_PROPERTIES
         */
        REQUIRED_PROPERTIES: {
            label: true
        },

        /**
         * Defines default properties
         *
         * @method _getDefaultOptions
         * @return {Object};
         */
        _getDefaultOptions: function () {
            return {
                showLabel: true
            };
        },

        /**
         * Gets styleClass, in the case of {@link QueryableDropdownSelect} these are the classNames that will be applied to the
         * &lt;a&gt; surrounding suggestion.
         *
         * @method styleClass
         * @return {String}
         */
        styleClass: function () {
            return this.properties.styleClass;
        },

        /**
         * Misc data useful to rendering etc stored here. We store it as a string so that i can be pulled from data attributes.
         *
         * @return {JSON}
         */
        meta: function () {
            if (this.properties.meta) {
                return JSON.parse(this.properties.meta);
            }
        },

        /**
         * Gets value, in the case of a {@link QueryableDropdownSelect} this will be the value set on the &lt;option&gt;
         *
         * @method value
         * @return {String}
         */
        value: function () {
            return this.properties.value;
        },

        /**
         * Gets invalid, in the case of a {@link QueryableDropdownSelect}, a span will be added to label as a hover over the invalid
         * icon;
         *
         * @method invalid
         * @return {String}
         */
        invalid: function () {
            return this.properties.invalid;
        },

        /**
         * Gets label suffix, in the case of {@link QueryableDropdownSelect} where we mirror the users input as a suggestion we
         * use this to append some help text such as (Add Label).
         *
         * @deprecated not i18n friendly, use *fieldText* and *label* instead
         * @method labelSuffix
         * @return {String}
         */
        labelSuffix: function (value) {
            if (typeof value !== "undefined") {
                this.properties.labelSuffix = value;
            }
            return this.properties.labelSuffix;
        },

        /**
         * Gets title, in the case of {@link MultiSelect} we use this as the title tag applied to {@link MultiSelect.Lozenge} element
         *
         * @method title
         * @return {String}
         */
        title: function () {
            return this.properties.title;
        },

        /**
         * Gets label, in the case of {@link QueryableDropdownSelect} this is the label displayed in the suggestion items, unless
         * the html property of this descriptor has been set.
         *
         * @method label
         * @return {String}
         */
        label: function () {
            return this.properties.label;
        },


        disabled: function (disabled) {
            if (disabled) {
                this.properties.disabled = disabled;
            } else {
                return this.properties.disabled;
            }
        },

        /**
         * Asks whether or not to allow duplicates of this descriptor. This is used in {@link QueryableDropdownSelect} where there
         * is a suggetion appended that mirrors user input. In this case if we have another suggestion in the list that is
         * the same as this one, we do not want to show this one.
         *
         * @method allowDuplicate
         */
        allowDuplicate: function () {
            return this.properties.allowDuplicate;
        },


        /**
         * @method removeOnUnSelect
         * @param {Boolean} value
         * @return {Boolean}
         */
        removeOnUnSelect: function (value) {
            if (typeof value !== "undefined") {
                this.properties.removeOnUnSelect = value;
            }
            return this.properties.removeOnUnSelect;
        },

        /**
         * Gets icon url
         *
         * @method icon
         * @return {String}
         */
        icon: function () {
            return this.properties.icon;
        },

        /**
         * Gets the fallback icon url
         *
         * @method icon
         * @return {String}
         */
        fallbackIcon: function () {
            return this.properties.fallbackIcon;
        },

        /**
         * Gets or sets selected state.
         *
         * @method selected
         * @param {Boolean} value
         * @return {Boolean}
         */
        selected: function (value) {
            if (typeof value !== "undefined") {
                this.properties.selected = value;
            }
            return this.properties.selected;
        },

        /**
         * Gets or sets model. The model in the case of {@link QueryableDropdownSelect} is the jQuery wrapped <option> element
         *
         * @param $model
         */
        model: function ($model) {
            if ($model) {
                this.properties.model = $model;
            } else {
                return this.properties.model;
            }
        },

        /**
         * Gets the keywords attribute
         *
         * @method keywords
         * @return {String}
         */
        keywords: function () {
            return this.properties.keywords;
        },

        /**
         * Gets the href attribute
         *
         * @method href
         * @return {String}
         */
        href: function () {
            return this.properties.href;
        },

        /**
         * {@link List} looks at this to determine if it should do the highlighting/filtering or it has already been done
         * @return {Boolean}
         */
        highlighted: function (value) {
            if (value === false) {
                if (this.properties.html) {
                    this.properties.label = jQuery("<div>" + this.properties.html + "</div>").text();
                    this.properties.html = null;
                }
                this.properties.highlighted = false;
            }
            return this.properties.highlighted && this.properties.html;
        },

        /**
         * Gets html, in the case of {@link QueryableDropdownSelect} this html will be shown as the suggestion item instead of [label]
         *
         * @method html
         * @return {String}
         */
        html: function () {
            return this.properties.html;
        },

        /**
         * @return {String} the text displayed in field when this item is selected
         */
        fieldText: function () {
            return this.properties.fieldText;
        },

        /**
         * @return true if this {@link ItemDescriptor} should not be selected in the dropdown, even if it's an exact text match for a query.
         */
        noExactMatch: function () {
            return this.properties.noExactMatch;
        }

    });
});

AJS.namespace('AJS.ItemDescriptor', null, require('jira/ajs/list/item-descriptor'));