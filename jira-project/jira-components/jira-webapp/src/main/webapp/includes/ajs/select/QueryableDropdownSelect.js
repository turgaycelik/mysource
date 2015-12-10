define('jira/ajs/select/queryable-dropdown-select', [
    'jira/ajs/control',
    'jira/ajs/select/suggestions/default-suggest-handler',
    'jira/ajs/layer/inline-layer-factory',
    'jira/ajs/list/list',
    'jira/util/navigator',
    'jquery'
], function(
    Control,
    DefaultSuggestHandler,
    InlineLayerFactory,
    List,
    Navigator,
    jQuery
) {
    /**
     * A dropdown that can be queried and it's links selected via keyboard. Dropdown contents retrieved via AJAX.
     *
     * @class QueryableDropdownSelect
     * @extends Control
     */
    return Control.extend({

        /**
         *  A request will not be fired and suggestions will not reset if any of these keys are inputted.
         *
         * @property {Array} INVALID_KEYS
         */
        INVALID_KEYS: {
            "Shift": true,
            "Esc": true,
            "Right": true
        },

        /**
         * Overrides default options with user options. Inserts an input field before dropdown.
         *
         * @param {Object} options
         * @param {jQuery | HTMLElement} options.element
         * @param {SuggestHandler} options.suggestionsHandler
         */
        init: function (options) {
            this.suggestionsVisible = false;
            this._setOptions(options);
            this._createFurniture();
            this._createDropdownController();
            this._createSuggestionsController();
            this._createListController();
            this._assignEventsToFurniture();

            if (this.options.width) {
                this.setFieldWidth(this.options.width);
            }
            if(this.options.loadOnInit) {
                // eagerly get suggestions
                this.requestSuggestions(true);
            }
        },

        /**
         * Creates dropdown controller
         * @private
         */
        _createDropdownController: function () {
            var instance = this;
            if (this.options.dropdownController) {
                this.dropdownController = this.options.dropdownController;
            } else {
                this.dropdownController = InlineLayerFactory.createInlineLayers({
                    offsetTarget: this.$field,
                    width: this.$field.innerWidth(),
                    content: this.options.element
                });
            }
            this.dropdownController.onhide(function () {
                instance.hideSuggestions();
            });
        },

        /**
         * Creates suggestions controller
         * @private
         */
        _createSuggestionsController: function () {
            this.suggestionsHandler = this.options.suggestionsHandler ?
                new this.options.suggestionsHandler(this.options) :
                new DefaultSuggestHandler(this.options);
        },

        /**
         * Creates list controller
         * @private
         */
        _createListController: function () {
            var instance = this;
            this.listController = new List({
                containerSelector: this.options.element,
                groupSelector: "ul.aui-list-section",
                matchingStrategy: this.options.matchingStrategy,
                eventTarget: this.$field,
                selectionHandler: function () {
                    // prevent form field from being dirty
                    instance.$field.val(AJS.I18n.getText("common.concepts.loading")).css("color", "#999");
                    instance.hideSuggestions();
                    return true;
                }
            });
        },

        /**
         * Sets field width
         *
         * @param {Number} width - field width
         */
        setFieldWidth: function (width) {
            this.$container.css({
                width: width,
                minWidth: width
            });
        },

        /**
         * Show an error message near this field
         *
         * @param {String} value (optional) - The user input text responsible for the error
         */
        showErrorMessage: function (value) {

            var $container = this.$container.parent(".field-group"); // aui container

            this.hideErrorMessage(); // remove old

            this.$errorMessage.text(AJS.format(this.options.errorMessage, value || this.getQueryVal()));

            if ($container.length === 1) {
                $container.append(this.$errorMessage);
                return;
            }

            if ($container.length === 0) {
                $container = this.$container.parent(".frother-control-renderer"); // not in aui but JIRA renderer
            }

            if ($container.length === 1) {
                this.$errorMessage.prependTo($container);
                return;
            }

            if ($container.length === 0) {
                this.$container.parent().append(this.$errorMessage);
            }
        },

        /**
         * @method hideErrorMessage - Hide the error message-
         */
        hideErrorMessage: function() {
            if (this.$errorMessage) {
                this.$errorMessage.remove();
            }
            this.$container.parent().find(".error").remove(); // remove all error message from server also
        },

        /**
         * Gets default options
         *
         * @method _getDefaultOptions
         * @private
         * @return {Object}
         */
        _getDefaultOptions: function () {
            return {
                id: "default",
                // keyInputPeriod: expected milliseconds between consecutive keystrokes
                // If this user types faster than this, no requests will be issued until they slow down.
                keyInputPeriod: 75,
                // localListLiveUpdateLimit: Won't search for new options if there are more options than this value
                localListLiveUpdateLimit: 25,
                // Only search for new options locally after this delay.
                localListLiveUpdateDelay: 150
            };
        },

        /**
         * Appends furniture around specified dropdown element. This includes:
         *
         * <ul>
         *  <li>Field - text field used fro querying</li>
         *  <li>Container - Wrapper used to contain all furniture</li>
         *  <li>Dropdown Icon - Button in right of field used to open dropdown via mouse</li>
         * </ul>
         *
         * @method _createFurniture
         * @private
         */
        _createFurniture: function () {
            this.$container = this._render("container").insertBefore(this.options.element);
            this.$field = this._render("field").appendTo(this.$container);
            this.$dropDownIcon = this._render("dropdownAndLoadingIcon", this._hasDropdownButton()).appendTo(this.$container);
            if (this.options.overlabel) {
                this.$overlabel = this._render("overlabel").insertBefore(this.$field);
                this.$overlabel.overlabel();
            }
        },

        /**
         * Whether or not to display dropdown icon/button
         *
         * @method _hasDropdownButton
         * @protected
         * @return {Boolean}
         */
        _hasDropdownButton: function () {
            return this.options.showDropdownButton || this.options.ajaxOptions && this.options.ajaxOptions.minQueryLength === 0;
        },

        /**
         * Assigns events to DOM nodes
         *
         * @method _assignEventsToFurniture
         * @protected
         */
        _assignEventsToFurniture: function () {

            var instance = this;

            this._assignEvents("ignoreBlurElement", this.dropdownController.$layer);
            this._assignEvents("container", this.$container);

            if (this._hasDropdownButton()) {
                this._assignEvents("ignoreBlurElement", this.$dropDownIcon);
                this._assignEvents("dropdownAndLoadingIcon", this.$dropDownIcon);
            }

            // if this control is created as the result of a keydown event then we do no want to catch keyup or keypress for a moment
            setTimeout(function() {
                instance._assignEvents("field", instance.$field);
                instance._assignEvents("keys", instance.$field);
            }, 15);
        },

        /**
         * Requests JSON formatted suggestions from specified resource. Resource is sepecified in the ajaxOptions object
         * passed to the constructed during initialization.
         *
         * If the query option of ajaxOptions is set to true, an ajax request will be made for every keypress. Otherwise
         * ajax request will be made only the first time the dropdown is shown.
         *
         * @method _requestThenResetSuggestions
         * @private
         * @param {Boolean} force - flag to specify that gating by keyInputPeriod should be circumvented
         */
        requestSuggestions: function (force) {
            var instance = this,
                deferred = jQuery.Deferred();

            this.outstandingRequest = this.suggestionsHandler.execute(this.getQueryVal(), force).done(function (descriptors, query) {
                if (query === instance.getQueryVal()) {
                    deferred.resolve(descriptors, query);
                }
            });
            if (this.outstandingRequest.state() !== "resolved") {
                window.clearTimeout(this.loadingWait); // clear existing wait
                // wait 150ms until we should throbber to avoid flickering while typing
                this.loadingWait = window.setTimeout(function () {
                    if (instance.outstandingRequest.state() === "pending") {
                        instance.showLoading();
                    }
                }, 150);

                this.outstandingRequest.always(function () {
                    instance.hideLoading(); // make sure we always remove throbber
                });
            }
            return deferred;
        },

        /**
         * Show the loading indicator
         * @return {*} this
         */
        showLoading: function() {
            this.$dropDownIcon.addClass("loading").removeClass("noloading");
            return this;
        },

        /**
         * Hide the loading indicator
         * @return {*} this
         */
        hideLoading: function() {
            this.$dropDownIcon.removeClass("loading").addClass("noloading");
            return this;
        },
        /**
         *
         * Sets suggestions and shows them
         *
         * @method _setSuggestions
         * @param {Array} Descriptors
         */
        _setSuggestions: function (data) {

            // JRADEV-2053: If the field is no longer focused (i.e. the user has already tabbed away) don't set
            // suggestions as it will bring focus back to this field.
            this.suggestionsVisible = true;

            if (data) {
                this.listController.generateListFromJSON(data, this.getQueryVal());
                this.dropdownController.show();
                this.dropdownController.setWidth(this.$field.innerWidth());
                this.dropdownController.setPosition();
                this.listController.enable();

            } else {
                this.hideSuggestions();
            }

            // Makes WebDriver wait for the correct suggestions
            this.$container.attr("data-query", this.getQueryVal());
        },

        /**
         * Fades out & disables interactions with field
         */
        disable: function () {
            if (!this.disabled) {
                this.$container.addClass("aui-disabled");
                // The disabledBlanket is necessary to prevent clicks on other elements positioned over the field.
                this.$disabledBlanket = this._render("disabledBlanket").appendTo(this.$container);
                this.$field.attr('disabled', true);
                this.dropdownController.hide();
                this.disabled = true;
            }
        },

        /**
         * Enables interactions with field
         */
        enable: function () {
            if (this.disabled) {
                this.$container.removeClass("aui-disabled");
                this.$disabledBlanket.remove();
                this.$field.attr('disabled', false);
                this.disabled = false;
            }
        },

        /**
         * Gets input field value
         *
         * @return {String}
         */
        getQueryVal: function () {
            return jQuery.trim(this.$field.val());
        },

        _isValidInput: function (event) {
            return this.$field.is(":visible") && !(event.type === "aui:keydown" && this.INVALID_KEYS[event.key]);
        },

        /**
         * Hides list if the is no value in input, otherwise shows and resets suggestions in dropdown
         *
         * @method _handleCharacterInput
         * @param {Boolean} force - flag to specify that gating by keyInputPeriod (via requestSuggestions) should be circumvented
         * @private
         */
        _handleCharacterInput: function (force) {
            var queryLength = this.getQueryVal().length;
            if (queryLength >= 1 || force) {
                this.requestSuggestions(force).done(_.bind(function (suggestions) {
                    this._setSuggestions(suggestions)
                }, this));
            } else {
                this.hideSuggestions();
            }
        },

        /**
         * Handles down key
         *
         * @method _handleDown
         * @param {Event} e
         */
        _handleDown: function(e) {
            if (!this.suggestionsVisible) {
                this.listController._latestQuery = ""; // JRADEV-9009 Resetting query value
                this._handleCharacterInput(true);
            }
            e.preventDefault();
        },

        /**
         * Cancels and pending or outstanding requests
         *
         * @method _rejectPendingRequests
         * @protected
         */
        _rejectPendingRequests: function () {
            if (this.outstandingRequest) {
                this.outstandingRequest.reject();
            }
        },

        /**
         * Hides suggestions
         *
         * @method hideSuggestions
         */
        hideSuggestions: function () {
            if (!this.suggestionsVisible) {
                return;
            }
            this._rejectPendingRequests();
            this.suggestionsVisible = false;
            this.$dropDownIcon.addClass("noloading");
            this.dropdownController.hide();
            this.listController.disable();
        },

        _deactivate: function () {
            this.hideSuggestions();
        },

        /**
         * Handles Escape key
         *
         * @method _handleEscape
         * @param {Event} e
         */
        _handleEscape: function (e) {
            if (this.suggestionsVisible) {
                e.stopPropagation();
                if (e.type === "keyup") {
                    this.hideSuggestions();
                    if (Navigator.isIE() && Navigator.majorVersion() < 12) {
                        // IE - field has already received the event and lost focus (default browser behaviour)
                        this.$field.focus();
                    }
                }
            }
        },

        /**
         * Selects currently focused suggestion, if there is one
         */
        acceptFocusedSuggestion: function () {
            var focused = this.listController.getFocused();
            if (focused.length !== 0 && focused.is(":visible")) {
                this.listController._acceptSuggestion(focused)
            }
        },

        keys: {
            "Down": function (e) {
                if (this._hasDropdownButton()) {
                    this._handleDown(e);
                }
            },
            "Up": function (e) {
                e.preventDefault();
            },
            "Return": function (e) {
                e.preventDefault();
            }
        },

        onEdit: function() {
            this._handleCharacterInput();
        },

        _events: {

            dropdownAndLoadingIcon: {
                click: function (e) {
                    if (this.suggestionsVisible) {
                        this.hideSuggestions();
                    } else {
                        this._handleDown(e);
                        this.$field.focus();
                    }
                    e.stopPropagation();
                }
            },

            container: {
                disable : function () {
                    this.disable();
                },
                enable: function () {
                    this.enable();
                }
            },

            field: {
                blur: function () {
                    if (!this.ignoreBlurEvent) {
                        this._deactivate();
                    } else {
                        this.ignoreBlurEvent = false;
                    }
                },
                click: function (e) {
                    e.stopPropagation();
                },
                "keydown keyup": function (e) {
                    if (e.keyCode === 27) {
                        this._handleEscape(e);
                    }
                }
            },

            keys: {
                "aui:keydown input": function (event) {
                    this._handleKeyEvent(event);
                }
            },

            ignoreBlurElement: {
                mousedown: function (e) {
                    if (Navigator.isIE() && Navigator.majorVersion() < 12) {
                        // JRA-27685. IE fires blur events when user clicks on the scrollbar inside autocomplete suggestion list.
                        // In that case we don't deactivate the input field by setting a flag and checking it in field:blur.
                        var targetIsDropdownController = (jQuery(e.target)[0] == jQuery(this.dropdownController.$layer)[0]);

                        if (targetIsDropdownController) {
                            this.ignoreBlurEvent = true;
                        }

                        if (typeof document.addEventListener === "undefined") { // IE8
                            // Scrollbar freezes in IE8 if we 'preventDefault' when user clicked on the scrollbar.
                            if (!targetIsDropdownController) {
                                var field = this.$field.get(0);

                                /**
                                 * Performs an IE specific "preventDefault"
                                 */
                                function onbeforedeactivate(event) {
                                    event.returnValue = false;
                                }

                                // Preventing the default action of "mousedown" events stops the
                                // activeElement losing focus in all non-IE. We need to use a funky
                                // workaround for IE which allows the field to lose focus briefly
                                // so that UI controls like scrollbars are still interactive.
                                field.attachEvent("onbeforedeactivate", onbeforedeactivate);
                                setTimeout(function() {
                                    field.detachEvent("onbeforedeactivate", onbeforedeactivate);
                                }, 0);
                            }
                            return;
                        }
                    }

                    // IE9 and other browsers
                    e.preventDefault()
                }
            }
        },

        _renders: {

            disabledBlanket: function () {
                return jQuery("<div class='aui-disabled-blanket' />").height(this.$field.outerHeight());
            },
            overlabel: function () {
                return jQuery("<span id='" + this.options.id + "-overlabel' data-target='" + this.options.id + "-field' class='overlabel'>" + this.options.overlabel + "</span>" );
            },
            field: function () {
                // Create <input> element in a way that is compatible with IE8 "input" event shim.
                // @see jquery.inputevent.js -- Note #2
                return jQuery("<input>").attr({
                    "autocomplete": "off",
                    "class": "text",
                    "id": this.options.id + "-field",
                    "type": "text"
                });
            },
            container: function () {
                return jQuery("<div class='queryable-select' id='" + this.options.id + "-queryable-container' />");
            },
            dropdownAndLoadingIcon: function (showDropdown) {
                var $element = jQuery('<span class="icon noloading"><span>More</span></span>');
                if  (showDropdown) {
                    $element.addClass("drop-menu");
                }
                return $element;
            },
            suggestionsContainer : function () {
                return jQuery("<div class='aui-list' id='" + this.options.id + "' tabindex='-1'></div>");
            }
        }
    });
});

/** Preserve legacy namespace
    @deprecated AJS.QueryableDropdown */
AJS.namespace('AJS.QueryableDropdown', null, require('jira/ajs/select/queryable-dropdown-select'));
AJS.namespace('AJS.QueryableDropdownSelect', null, require('jira/ajs/select/queryable-dropdown-select'));