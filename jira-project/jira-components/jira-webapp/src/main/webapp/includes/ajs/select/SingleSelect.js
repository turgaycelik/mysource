define('jira/ajs/select/single-select', [
    'jira/ajs/select/queryable-dropdown-select',
    'jira/ajs/select/select-helper',
    'jira/ajs/select/select-model',
    'jira/ajs/select/suggestions/select-suggest-handler',
    'jira/ajs/layer/inline-layer-factory',
    'jira/ajs/list/list',
    'jira/ajs/list/item-descriptor',
    'jira/util/navigator',
    'jquery'
], function(
    QueryableDropdownSelect,
    SelectHelper,
    SelectModel,
    SelectSuggestHandler,
    InlineLayerFactory,
    List,
    ItemDescriptor,
    Navigator,
    jQuery
) {

    /**
     * A single select list that can be queried and suggestions selected via a dropdown. Suggestions are retrieved via AJAX or Statically.
     *
     * @class SingleSelect
     * @extends QueryableDropdownSelect
     */
    return QueryableDropdownSelect.extend({

        /**
         * This constructor:
         * <ul>
         *  <li>Overrides default options with user options</li>
         *  <li>Inserts an input field before dropdown</li>
         *  <li>Displays selection in input field, if there is a selected option in the select field</li>
         * <ul>
         *
         * @param options
         */
        init: function (options) {

            if (this._setOptions(options) === this.INVALID) {
                return this.INVALID;
            }

            this._createSelectModel();

            if (this.options.disabled) {
                this._createFurniture(true);
                return this;
            }

            jQuery.extend(this, SelectHelper);

            this._createFurniture();
            this._createSuggestionsController();
            this._createDropdownController();
            this._createListController();
            this._assignEventsToFurniture();
            this._setInitState();

            if (this.options.width) {
                this.setFieldWidth(this.options.width)
            }
            if (this.$overlabel) {
                this.$overlabel.overlabel(this.$field);
            }

            this.model.$element.addClass("aui-ss-select").trigger("initialized", [this]);
        },

        _setInitState: function () {
            if (this.options.editValue) {
                this._setEditingMode();
                this.$field.val(this.options.editValue);
                // display selected, if there is one.
            } else if (this.getSelectedDescriptor()) {
                this.setSelection(this.getSelectedDescriptor());
                // otherwise turn editing on
            } else {
                this._setEditingMode();
                if (this.options.inputText) { // inputText is really placeholder text
                    this.$field.val(this.options.inputText);
                }
            }
        },

        _createSelectModel: function () {
            // Used to retrieve and set options in our <select> element via a JSON interface
            this.model = new SelectModel({
                element: this.options.element,
                removeOnUnSelect: this.options.removeOnUnSelect
            });
        },

        _createDropdownController: function () {
            this.dropdownController = InlineLayerFactory.createInlineLayers({
                alignment:AJS.LEFT,
                offsetTarget: this.$field,
                content: jQuery(".aui-list", this.$container),
                setMaxHeightToWindow: this.options.setMaxHeightToWindow,
                minHeight: this.options.minHeight
            });
        },

        _createSuggestionsController: function () {
            this.suggestionsHandler = this.options.suggestionsHandler ?
                    new this.options.suggestionsHandler(this.options, this.model) :
                    new SelectSuggestHandler(this.options, this.model);
        },

        _assignEventsToFurniture: function () {
            var instance = this;
            this._super();
            this.model.$element.bind("reset", function () {
                var selectedDescriptor = instance.getSelectedDescriptor();
                if (selectedDescriptor) {
                    instance.setSelection(instance.getSelectedDescriptor());
                }
            })
            .bind("showSuggestions", function (e) {
                instance._handleDown(e);
            })
            .bind("hideSuggestions", function () {
                instance.hideSuggestions();
            })
            .bind("set-selection-value", function (e, value) {
                instance._setDescriptorWithValue(value);
            });
        },

        /**
         * Sets field width
         *
         * @param {Number} width - field width
         */
        setFieldWidth: function (width) {
            this.$container.css({maxWidth: width});
            this.$field.css({maxWidth: width});
        },

        _createListController: function () {
            var instance = this;
            this.listController = new List({
                containerSelector: jQuery(".aui-list", this.$container),
                groupSelector: "ul.aui-list-section",
                matchingStrategy: this.options.matchingStrategy,
                maxInlineResultsDisplayed: this.options.maxInlineResultsDisplayed,
                matchItemText: this.options.matchItemText,
                hasLinks: this.options.hasLinks,
                selectionHandler: function (e) {
                    var selectedSuggestion = this.getFocused(),
                            selectedDescriptor = selectedSuggestion.data("descriptor");

                    instance.setSelection(selectedDescriptor);
                    instance.$field.select();

                    e.preventDefault();
                    return false;
                }
            });
        },

        /**
         * Returns the selected descriptor. Undefined if there is none.
         *
         * @return {ItemDescriptor}
         */
        getSelectedDescriptor: function () {
            return this.model.getDisplayableSelectedDescriptors()[0];
        },

        /**
         * Gets the value that has been configured to display to the user. Uses label by default.
         *
         * @param {ItemDescriptor} descriptor
         * @return {String}
         */
        getDisplayVal: function (descriptor) {
            return descriptor[this.options.itemAttrDisplayed || "label"]();
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
                errorMessage: AJS.I18n.getText("jira.ajax.autocomplete.error"),
                revertOnInvalid: false,
                showDropdownButton: true
            });
        },

        /**
         * Appends furniture around specified dropdown element. This includes:
         *
         * <ul>
         *  <li>errorMessage - A container for error messages is created but not appended until needed</li>
         *  <li>selectedItemsWrapper - A wrapper for selected items</li>
         *  <li>selectedItemsContainer - A container for selected items</li>
         * </ul>
         *
         * @method _createFurniture
         * @protected
         */
        _createFurniture: function (disabled) {

            var id = this.model.$element.attr("id");
            this.model.$element.data("aui-ss", true);
            this.$container = this._render("container", this.model.id);
            var containerClass = this.model.$element.data('container-class');
            if (containerClass) {
                this.$container.addClass(containerClass);
            }

            if (disabled) {
                var value = this.model.$element.val() && this.model.$element.val()[0];
                value = value || "";
                this.model.$element.replaceWith(this._render("disableSelectField", id, value));
            } else {
                var placeholder = this.model.getPlaceholder();
                this.$field = this._render("field", this.model.id, placeholder).appendTo(this.$container);
                if (placeholder && !this._placeholderSupported()) { // emulate HTML5 placeholder attribute behaviour.
                    this.options.overlabel = placeholder;
                    this.$overlabel = this._render("overlabel").insertBefore(this.$field);
                }
                this.$container.append(this._render("suggestionsContainer", this.model.id));
                this.$container.insertBefore(this.model.$element);
                this.$dropDownIcon = this._render("dropdownAndLoadingIcon", this._hasDropdownButton()).appendTo(this.$container);
                this.$errorMessage = this._render("errorMessage");
            }
        },

        _placeholderSupported: function() {
            return !(Navigator.isIE() && Navigator.majorVersion() < 10);
        },

        /**
         * If there is a selection, search for everything, otherwise use the field value.
         *
         * @return {String}
         */
        getQueryVal: function () {
            if (this.$container.hasClass("aui-ss-editing")) {
                return jQuery.trim(this.$field.val());
            } else {
                return "";
            }
        },

        /**
         * Adds supplied suggestions to dropdown and &lt;select&gt; list
         *
         * @method _setSuggestions
         * @param {Object} data - JSON representing suggestions
         * @param {Object} context - JSON representing suggestion context, including:
         * - {boolean} filter - true if the data should be filtered on any query entered
         */
        _setSuggestions: function (data, context) {
            if (data) {
                this._super(data, context);
                this.model.$element.trigger("suggestionsRefreshed", [this]);
            } else {
                this.hideSuggestions();
            }
        },

        /**
         * Sets to editing mode. Clearing the appearence of a selection.
         */
        _setEditingMode: function () {
            this.$container.addClass("aui-ss-editing")
                    .removeClass("aui-ss-has-entity-icon");
            // Workaround for IE9 form element styling bug JRADEV-6299
            this.$field.css("paddingLeft");
        },

        _hasIcon: function () {
            var icon,
                selectedDescriptor = this.getSelectedDescriptor();
            if (selectedDescriptor) {
                icon = selectedDescriptor.icon();
                return icon && icon !== "none";
            }
        },

        /**
         * Sets to readonly mode. Displaying the appearence that something is selected.
         */
        _setReadOnlyMode: function () {

            this.$container.removeClass("aui-ss-editing");

            if (this._hasIcon()) {
                this.$container.addClass("aui-ss-has-entity-icon");
                // Workaround for IE9 form element styling bug JRADEV-6299
                if (Navigator.isIE() && Navigator.majorVersion() > 8) {
                    this.$container.append(this.$field.detach());
                }
            }
        },

        /**
         * Submits form
         *
         * @method submitForm
         */
        submitForm: function () {
            if (!this.suggestionsVisible) {
                this.handleFreeInput();
                jQuery(this.$field[0].form).submit(); // submit on enter if field is empty
            }
        },

        /**
         * Allows a selection to made based on the value of an option. Useful for tests.
         *
         * @param value - internal value of an option item to select. If no matching options exists nothing happens.
         */
        selectValue: function (value) {
            this.listController.selectValue(value);
        },

        /**
         * Sets as selected in model and changes styling to demonstrate selection
         *
         * @param {ItemDescriptor} descriptor
         */
        setSelection: function (descriptor) {

            if (typeof descriptor === "string") {
                descriptor = new ItemDescriptor({
                    value: descriptor,
                    label: descriptor
                });
            }

            this.removeFreeInputVal();

            // We need to update this.$field's value *before* calling this.model.setSelected(descriptor),
            // otherwise subsequent this.getSelectedDescriptor() calls won't return the descriptor we want.
            // @see jquery.inputevent.js -- Note #1
            this.$field.val(descriptor.fieldText() || this.getDisplayVal(descriptor));
            this.$field.trigger("change");

            if (this.model.setSelected(descriptor)) {
                this.hideErrorMessage();
            }

            if (this._hasIcon()) {
                if (this.$entityIcon) {
                    this.$entityIcon.remove();
                }

                this.$entityIcon = this._render("entityIcon", descriptor.icon()).appendTo(this.$container);
            }

            this._setReadOnlyMode();
            this.hideSuggestions();

            this.lastSelection = descriptor;
            this.model.$element.trigger("selected", [descriptor, this]);
        },

        /**
         * Clears the control - selection and field text.
         */
        clear: function () {
            this.$field.val('');
            this.hideSuggestions();
            this.clearSelection();
        },

        /**
         * Clears selection and sets back to editing mode
         */
        clearSelection: function () {

            var instance = this;
            instance._setEditingMode();
            instance.model.setAllUnSelected();
            instance.model.$element.trigger("unselect", [this]);
        },

        /**
         * Removal of items in select list if we are replacing them from the server.
         *
         * @method _handleServerSuggestions
         * @override
         * @protected
         * @param {Array} data
         */
        _handleServerSuggestions: function (data) {
            this.cleanUpModel();
            this._super(data);
        },

        /**
         * If we are querying the server then the server will return the full result set to be displayed. We do not
         * want any linguring options in there.
         */
        cleanUpModel: function () {
            if (this.options.ajaxOptions.query) {
                this.model.clearUnSelected();
            }
        },

        /**
         * Handles editing of input value
         */
        onEdit: function() {
            if (this.getSelectedDescriptor()) {
                this.clearSelection();
            }
            this._super();
            this.model.$element.trigger("query");
        },

        /**
         * Handle the case where text remains unselected in the text field
         */
        handleFreeInput: function(value) {

            value = value || jQuery.trim(this.$field.val());

            if (this.options.revertOnInvalid && !this.model.getDescriptor(value)) {
                this.setSelection(this.lastSelection || "");
            } else if (this.$container.hasClass("aui-ss-editing")) {
                if (this._setDescriptorWithValue(value)) {
                    this.hideErrorMessage();
                } else if (!this.options.submitInputVal) {
                    this.showErrorMessage(value);
                }
            }
        },

        _setDescriptorWithValue: function (value) {
            var descriptor = this.model.getDescriptor(value);
            if (descriptor) {
                this.setSelection(descriptor);
                return true;
            }
            return false;
        },

        _handleCharacterInput: function (force) {
            this._super(force);
            if (this.$container.hasClass("aui-ss-editing")) {
                this.updateFreeInputVal();
            }
        },

        _deactivate: function () {
            this.handleFreeInput();
            this.hideSuggestions();
        },

        keys: {
            "Return": function (e) {
                this.submitForm();
                e.preventDefault();
             },
            "Tab" : function () {
                this.acceptFocusedSuggestion();
            }
        },

        _events: {
            field: {
                focus: function () {
                    var instance = this;
                    window.setTimeout(function () {
                        if (instance.$field.is(":focus")) {
                            instance.$field.select();
                        }
                    }, 0);
                },
                click: function () {
                    this._handleCharacterInput(true);
                }
            }
        },

        _renders: {
            label: function (label, id) {
                return jQuery("<label />").attr("for", id).text(label).addClass("overlabel");
            },
            errorMessage: function () {
                return jQuery('<div class="error" />');
            },
            entityIcon: function (url) {
                var icon = jQuery('<img class="aui-ss-entity-icon" alt=""/>');
                icon.attr("src", url);
                return  icon;
            },
            field: function (idPrefix, placeholder) {
                // Create <input> element in a way that is compatible with IE8 "input" event shim.
                // @see jquery.inputevent.js -- Note #2
                var $field = jQuery("<input>").attr({
                    "autocomplete": "off",
                    "class": "text aui-ss-field ajs-dirty-warning-exempt",
                    "id": idPrefix + "-field"
                    });
                if (placeholder) {
                    $field.attr("placeholder", placeholder);
                }
                return $field;
            },
            disableSelectField: function (id, value) {
                return jQuery("<input type='text' class='text long-field' value='" + value + "' name='" + id + "' id='" + id + "' />");
            },
            container : function (idPrefix) {
                return jQuery('<div class="aui-ss" id="' + idPrefix +'-single-select">');
            },
            suggestionsContainer : function (idPrefix) {
                return jQuery('<div class="aui-list" id="' + idPrefix + '-suggestions" tabindex="-1"></div>');
            },
            dropdownAndLoadingIcon: function (showDropdown) {
                var $element = jQuery('<span class="icon aui-ss-icon noloading"><span>More</span></span>');
                if  (showDropdown) {
                    $element.addClass("drop-menu");
                }
                return $element;
            }
        }
    });

});

AJS.namespace('AJS.SingleSelect', null, require('jira/ajs/select/single-select'));
