define('jira/ajs/select/checkbox-multi-select', [
    'jira/ajs/select/queryable-dropdown-select',
    'jira/ajs/select/select-helper',
    'jira/ajs/select/select-model',
    'jira/ajs/select/suggestions/checkbox-multi-select-suggest-handler',
    'jira/ajs/list/list',
    'jira/ajs/list/item-descriptor',
    'jira/util/events',
    'jira/util/events/types',
    'jquery'
], function(
    QueryableDropdownSelect,
    SelectHelper,
    SelectModel,
    SelectSuggestHandler,
    List,
    ItemDescriptor,
    Events,
    Types,
    jQuery
) {
    /**
     * A multiselect list that can be queried, selected options appear as checkboxes below the queryfield.
     * https://extranet.atlassian.com/download/attachments/1991213117/Sparkler+-+Phased+Approach.png
     *
     * @class CheckboxMultiSelect
     * @extends QueryableDropdownSelect
     */
    return QueryableDropdownSelect.extend({

        /**
         * @constructor
         * @param {Object} options
         */
        init: function (options) {

            var instance = this;

            // mixin
            jQuery.extend(this, SelectHelper);

            // munge default options with user specified options. Will put result at this.options.
            this._setOptions(options);

            var element = jQuery(this.options.element);
            if (!element.attr("multiple")) {
                throw "Cannot create CheckboxMultiSelect without multiple-select select element.";
            }

            this.options.element = jQuery(this.options.element).hide();

            // Gives a JSON interface to a &lt;select list&gt;. Allowing you to add elements via JSON descriptors. It also
            // provides utility methods to retrieve collections of elements as JSON, for example selected options.
            this.model = new SelectModel({
                element: this.options.element,
                removeOnUnSelect: this.options.removeOnUnSelect
            });

            this.suggestionsHandler = this.options.suggestionsHandler ?
                                      new this.options.suggestionsHandler(this.options, this.model) :
                                      new SelectSuggestHandler(this.options, this.model);

            // Some convienience events for devs to add remove <option>'s to <select> and it be reflected in UI
            this.options.element.bind("updateOptions", function () {
                instance._setOptions(options);
            }).bind("selectOption", function (e, descriptor) {
                instance.selectItem(descriptor);
            }).bind("removeOption", function (e, descriptor) {
                instance.unselectItem(descriptor);
            }).bind("clear", function() {
                instance.clear();
            });

            // Add the visual representation
            this._createFurniture();

            // QueryableDropdownSelect which we extend relies on dropdownController being there to manage the showing and
            // hiding of the dropdown when querying suggestions. As our suggestions are always shown we can just make these
            // methods empty.
            this.dropdownController = {
                show: jQuery.noop,
                setWidth: jQuery.noop,
                setPosition: jQuery.noop,
                hide: jQuery.noop
            };

            this.listController = new List({
                stallEventBind: this.options.stallEventBind,
                containerSelector: jQuery(".aui-list", this.$container),
                scrollContainer: ".aui-list-scroll",
                selectionEvent: "change",
                delegateTarget: this.$field,
                hasLinks: false,
                itemSelector: ".check-list-item",
                groupSelector: "ul.aui-list-section",
                matchingStrategy: this.options.matchingStrategy,
                maxInlineResultsDisplayed: this.options.maxInlineResultsDisplayed,
                expandAllResults: this.options.expandAllResults,
                renderers: this._getCustomRenders(),
                selectionHandler: function (e) {
                    var focusedItem;
                    if (e.type === "change") {
                        focusedItem = jQuery(e.target).closest(this.options.itemSelector);
                    } else {
                        focusedItem = this.getFocused();
                    }
                    instance._selectionHandler(focusedItem, e);
                    return false;
                }
            });

            this._assignEventsToFurniture();

            // Render suggestions in correct state from <select> (checked or not)
            this.render();
            this.model.$element.addClass("check-list-select-select").trigger("initialized", [this]);
            Events.trigger(Types.CHECKBOXMULITSELECT_READY, [this.model.$element, this]);
            return this;
        },

        /**
         * Returns custom renders used by list controller
         *
         * @return {Object}
         * @private
         */
        _getCustomRenders: function() {
            return {
                suggestion: this._renders.suggestionItem, // override default suggestion renderer
                suggestionItemElement: this._renders.suggestionItemElement,
                suggestionItemResolver: this._renders.suggestionItemResolver
            };
        },

        /**
         * Hides list if the is no value in input, otherwise shows and resets suggestions in dropdown
         *
         * @method _handleCharacterInput
         * @param {Boolean} force
         * @private
         */
        _handleCharacterInput: function (force) {
            // tipsy() only works for the first item in the set. We want to hide the tipsy() for all the
            // invalid items, hence the each() call. Related to JRADEV-16120
            jQuery.each(this.listController.$container.find(".invalid-item"), function() {
                jQuery(this).tipsy("hide");
            });
            this.requestSuggestions(force).done(_.bind(function (suggestions) {
                this._setSuggestions(suggestions)
            }, this));
            this.$dropDownIcon.toggleClass('clear-field', !!this.getQueryVal());
            this.listController.moveToFirst();
        },

        /**
         * Gets default options
         * @return {Object}
         */
        _getDefaultOptions: function () {
            return jQuery.extend(true, this._super(), {
                errorMessage: AJS.I18n.getText("jira.ajax.autocomplete.error"),
                stallEventBind: true
            });
        },

        /**
         * Appends container dom element required to render check-list
         */
        _createFurniture: function () {
            var id = this.model.$element.attr("id");
            this.$container = this._render("container", id);
            this.$fieldContainer = this._render("fieldContainer").appendTo(this.$container);
            this.$field = this._render("field", id, this._getPlaceholderText()).appendTo(this.$fieldContainer);
            this.$container.append(this._render("suggestionsContainer", id));
            this.$container.insertBefore(this.model.$element);
            this.$dropDownIcon = this._render("dropdownAndLoadingIcon").appendTo(this.$fieldContainer);
        },

        _getPlaceholderText: function() {
            var placeholderText = jQuery.trim(this.model.$element.data("placeholder-text"));
            return (placeholderText && placeholderText !== "") ? placeholderText : AJS.I18n.getText("common.concepts.search");
        },

        /**
         * Assigns events furniture
         */
        _assignEventsToFurniture: function () {
            var instance = this;
            this._assignEvents("body", document);
            // if this control is created as the result of a keydown event then we do no want to catch keyup or keypress for a moment

            if (this.options.stallEventBind) {
                window.setTimeout(function () {
                    instance._assignEvents("field", instance.$field)
                            ._assignEvents("keys", instance.$field)
                            ._assignEvents("container", instance.$container)
                            ._assignEvents("fieldIcon", instance.$dropDownIcon);
                }, 0);
            } else {
                instance._assignEvents("field", instance.$field)
                        ._assignEvents("keys", instance.$field)
                        ._assignEvents("fieldIcon", instance.$dropDownIcon);
            }

            this.listController.$container.delegate(".clear-all", "click", function(event) {
                event.preventDefault();
                var $clearAll = jQuery(event.target);
                if ($clearAll.hasClass('disabled')) {
                    return;
                }
                $clearAll.parent().remove();
                instance.clear();
            });
        },

        /**
         * Clears the control - selection(s) and field text.
         */
        clear: function () {
            var instance = this;
            var selectedDescriptors = this.model.getDisplayableSelectedDescriptors();
            this.model.setAllUnSelected();
            if (this.$field.val().length === 0) {
                this.$field.val("");
                this.listController.$container.find(":checkbox").removeAttr("checked");
            } else {
                this.clearQueryField();
                this.listController.moveToFirst();
            }
            this._toggleClearButton();
            jQuery.each(selectedDescriptors, function() {
                instance.model.$element.trigger("unselect", [this, instance, true]);
            });
        },

        clearQueryField: function() {
            this.$field.val("");
            this._handleCharacterInput(true);
            this.$field.focus();
        },

        /**
         * Unselects item in model
         * @param {ItemDescriptor} descriptor
         */
        unselectItem: function (descriptor) {
            this.model.setUnSelected(descriptor);
            this.model.$element.trigger("unselect", [descriptor, this, false]);
            this.$container.find(".aui-list input[type=checkbox]").each(function() {
                if (this.value === descriptor.value()) {
                    this.checked = false;
                }
            });
        },

        _handleEscape: function(e) {
            var $field = this.$field;
            if (e.type === "keydown" && $field.val() !== "") {
                e.stopPropagation();
                $field.val("");
                $field.on("keyup", handleEscKeyUp);
                this._handleCharacterInput(true);
            }
            // Some controls listen to keyup for ESC, others to keydown.
            // We want to stopPropagation of *either* key event iff ESC was pressed when the field is not empty
            function handleEscKeyUp(event) {
                if (event.keyCode === 27) { // ESC key
                    event.stopPropagation();
                    $field.off("keyup", handleEscKeyUp);
                }
            }
        },

        /**
         * Adds selected suggestion to the selected items, and marks it as selected in the model.
         * @param {Object} descriptor - JSON describing suggestion/option
         * @param {Boolean} initialize
         */
        selectItem: function(descriptor, initialize) {
            this.model.setSelected(descriptor);
            if (!initialize) {
                this.model.$element.trigger("selected", [descriptor, this]);
            }
            this.$container.find(".aui-list input[type=checkbox]").each(function() {
                if (this.value === descriptor.value()) {
                    this.checked = true;
                }
            });
        },

        /**
         * Handle when a suggestion is selected/unselected
         * @param {jQuery} selected
         * @param {Event} event
         */
        _selectionHandler: function (selected, event) {
            var instance = this;
            selected.each(function () {
                var descriptor = jQuery.data(this, "descriptor"),
                    $input = jQuery(this).find(":input");

                if (instance._directCheckboxClick || event.shiftKey) {
                    descriptor.properties.fromCheckbox = true;
                }

                instance._setDescriptorSelection(descriptor, $input);
            });
            this._toggleClearButton();
        },

        _toggleClearButton: function() {
            var hasSelection = this.model.getSelectedValues().length > 0;
            this.listController.$container.find('.clear-all')
                .attr('tabindex', hasSelection ? null : -1)
                .closest('.check-list-group-actions')
                    .toggleClass('hidden', !hasSelection);
        },

        /**
         * Set the selection state of a descriptor and its associated input.
         *
         * Called by _selectionHandler.
         *
         * @param {ItemDescriptor} descriptor The Item Descriptor.
         * @param {jQuery} $input The descriptor's input.
         */
        _setDescriptorSelection: function(descriptor, $input) {
            if (!descriptor.selected()) {
                this.selectItem(descriptor);
                $input.attr("checked", "checked");
            } else {
                this.unselectItem(descriptor);
                $input.removeAttr("checked");
            }
        },

        render: function () {
            this._handleCharacterInput(true);
        },

        _events: {
            field: {
                "keydown": function(event) {
                    if (event.keyCode === 13) {
                        event.preventDefault();
                        // TODO: The following is a trial to test keyboard behaviour. @see JRADEV-14866
                        var instance = this;
                        this.model.$element.bind("unselect selected", handleSelected);
                        setTimeout(function() {
                            instance.model.$element.unbind("unselect selected", handleSelected);
                        }, 0);
                    }
                    function handleSelected() {
                        if (jQuery.trim(instance.$field.val()) !== "") {
                            // Reset the results after selecting an item in autocomplete mode with RETURN key.
                            instance.$field.val("");
                            instance._handleCharacterInput(true);
                        }
                    }
                }
            },
            container: {
                /**
                 * Refocus the input when a suggestion is clicked.
                 *
                 * This prevents the need to handle mousemove and scroll events
                 * when the field is unfocused, yet the dialog is still visible.
                 *
                 * In IE, if a field is selected with the Return key, the form was
                 * prematurely submitted.
                 *
                 * In Chrome (since checkboxes are not focused when clicked) the
                 * whole page would move when pressing the Up or Down keys causing
                 * a confusing situation where the suggestion under where the mouse was
                 * (as it is hidden when key-navigating), would be highlighted but not
                 * selected when the Return key was pressed.
                 */
                mousedown: function(event) {
                    var instance = this;
                    function onmouseup(event) {
                        if (event.type === "mouseup") {
                            instance._directCheckboxClick = true;
                            setTimeout(function() {
                                instance._directCheckboxClick = false;
                            }, 40);
                        }
                        jQuery(document).unbind("mouseup mouseleave", onmouseup);
                    }
                    if (jQuery(event.target).is("input[type=checkbox]")) {
                        // "click" events are always dispatched when a checkbox value is
                        // changed. This is how we determine if it was actually clicked.
                        jQuery(document)
                            .unbind("mouseup mouseleave", onmouseup)
                            .bind("mouseup mouseleave", onmouseup);
                    }
                    if (event.target !== this.$field.get(0)) {
                        event.preventDefault();
                    }
                },
                click: function () {
                    if (this.$field.get(0) !== document.activeElement) {
                        this.$field.focus();
                    }
                }
            },
            fieldIcon: {
                // For clearing the query field
                click: function(e) {
                    if (jQuery(e.target).hasClass('clear-field')) {
                        this.clearQueryField();
                    }
                }
            }
        },

        _renders: {
            errorMessage: function (idPrefix) {
                return jQuery('<div class="error" />').attr('id', idPrefix + "-error");
            },
            fieldContainer: function () {
                return jQuery("<div class='check-list-field-container' />");
            },
            field: function (idPrefix, placeholderText) {
                // Create <input> element in a way that is compatible with IE8 "input" event shim.
                // @see jquery.inputevent.js -- Note #2
                return jQuery("<input>").attr({
                    "autocomplete": "off",
                    "placeholder": placeholderText,
                    "class": "aui-field check-list-field",
                    "id": idPrefix + "-input",
                    "wrap": "off"
                });
            },
            disableSelectField: function (id) {
                return jQuery("<input type='text' class='long-field' name='" + id + "' id='" + id + "' />");
            },
            container : function (idPrefix) {
                return jQuery('<div class="check-list-select" id="' + idPrefix +'-multi-select">');
            },
            suggestionsContainer : function (idPrefix) {
                return jQuery('<div class="aui-list" id="' + idPrefix + '-suggestions"></div>');
            },
            suggestionItemElement: function(descriptor, replacementText) {
                //adding the label as a class for testing.
                var $checkbox = jQuery("<input type='checkbox' tabindex='-1' />").val(descriptor.value()),
                        $listElem = jQuery('<li class="check-list-item">'),
                        $label = jQuery("<label class='item-label' />"),
                        $img;

                if (descriptor.styleClass()) {
                    $listElem.addClass(descriptor.styleClass());
                }

                if (replacementText) {
                    $label.html(replacementText);
                } else if (descriptor.html()) {
                    $label.html(descriptor.html());
                } else {
                    $label.text(descriptor.label());
                }

                if (descriptor.selected()) {
                    $checkbox.attr("checked", "checked");
                }

                if (descriptor.icon() && descriptor.icon() !== "none") {
                    $img = jQuery("<img src='" + descriptor.icon() + "' height='16' width='16' align='absmiddle' />");
                    if (descriptor.fallbackIcon() && descriptor.fallbackIcon() !== "none") {
                        $img.one('error', function(){ this.src=descriptor.fallbackIcon()});
                    }
                    $label.prepend($img);
                }

                if (descriptor.title()) {
                    $label.attr("title", descriptor.title());
                    $label.attr("data-descriptor-title", descriptor.title()); // Used by KickAss' page object for finding values in the dropdown!
                }

                if (descriptor.disabled()) {
                    $listElem.addClass("disabled");
                    $checkbox.attr("disabled", "disabled");
                }

                $label.prepend($checkbox);
                $listElem.append($label);

                return $listElem;
            },
            suggestionItemResolver: function(descriptor, replacementText) {
                return this.suggestionItemElement(descriptor, replacementText);
            },
            suggestionItem: function(descriptor, replacementText) {
                var $listElem = this._renders.suggestionItemResolver(descriptor, replacementText),
                        $label = $listElem.find("label"),
                        $tipsyTarget;

                if (descriptor.invalid() || descriptor.disabled()) {

                    $listElem.addClass("has-invalid-item");

                    $label.append("<span class='invalid-item'></span>");
                    $tipsyTarget = $label.find(".invalid-item");

                    //Need to remove the title
                    //Otherwise both tipsy and html tooltip will show
                    _.defer(function() {
                        $listElem.attr("original-title", $listElem.attr("title"));
                        $listElem.removeAttr("title");
                    });

                    var title;
                    //If the searcher is nice enough to put an error message
                    //then use that instead
                    //otherwise use the generic one
                    if (descriptor.title()) {
                        title = descriptor.title();

                        $label.attr("original-title", title);
                        $label.removeAttr("title");
                    } else {
                        title = AJS.I18n.getText("jira.search.context.invalid.generic", AJS.I18n.getText("common.concepts.value"), descriptor.label());
                    }

                    $tipsyTarget.tipsy({
                        title: function(){
                            return title;
                        },
                        className: "tipsy-front",
                        trigger: "manual"
                    });

                    $tipsyTarget.hoverIntent({
                        interval: 200,
                        over: function() {
                            $tipsyTarget.tipsy("show");
                        },
                        out: function() {
                            $tipsyTarget.tipsy("hide");
                        }
                    });
                }

                return $listElem.data("descriptor", descriptor);
            }
        },

        // QueryableDropdownSelect requires these but we don't
        handleFreeInput: jQuery.noop,
        hideSuggestions: jQuery.noop,
        showErrorMessage: jQuery.noop,
        _deactivate: jQuery.noop
    });
});

AJS.namespace('AJS.CheckboxMultiSelect', null, require('jira/ajs/select/checkbox-multi-select'));
