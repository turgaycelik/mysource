define('jira/ajs/select/suggestions/suggest-helper', [
    'jira/ajs/list/group-descriptor',
    'jira/ajs/list/item-descriptor',
    'jira/ajs/select/fetchers/mixed-descriptor-fetcher',
    'jira/ajs/select/fetchers/ajax-descriptor-fetcher',
    'jira/ajs/select/fetchers/func-descriptor-fetcher',
    'jira/ajs/select/fetchers/static-descriptor-fetcher',
    'underscore'
], function(
    GroupDescriptor,
    ItemDescriptor,
    MixedDescriptorFetcher,
    AjaxDescriptorFetcher,
    FuncDescriptorFetcher,
    StaticDescriptorFetcher,
    _
) {
    /**
     * A utility object to manipulate/create suggestions
     * @class SuggestHelper
     */
    return {

        /**
         * Factory method to create descriptor fetcher based on user optiosn
         *
         * @param options
         * @param {SelectModel} model
         */
        createDescriptorFetcher:function (options, model) {
            if (options.ajaxOptions && options.ajaxOptions.url) {
                if (model && options.content === "mixed") {
                    return new MixedDescriptorFetcher(options, model);
                } else {
                    return new AjaxDescriptorFetcher(options.ajaxOptions);
                }
            } else if (options.suggestions) {
                return new FuncDescriptorFetcher(options);
            } else if (model) {
                return new StaticDescriptorFetcher(options, model);
            }
        },

        /**
         * Extract all item descriptors within an array of group descriptors.
         *
         * @param descriptors {GroupDescriptor[]} The group descriptors.
         * @return {ItemDescriptor[]} All item descriptors within.
         */
        extractItems: function (descriptors) {
            return _.flatten(_.map(descriptors, function(descriptor) {
                if (descriptor instanceof GroupDescriptor) {
                    return descriptor.items();
                } else {
                    return [descriptor];
                }
            }));
        },
        /**
         * Creates a descriptor group that mirrors the inputted query
         * @param {String} query
         * @param {String} label
         * @param {Boolean} uppercaseValue
         * @return {GroupDescriptor}
         */
        mirrorQuery: function (query, label, uppercaseValue) {
            var value = uppercaseValue ? query.toUpperCase() : query;
            return new GroupDescriptor({
                label: "user inputted option",
                showLabel: false,
                replace: true
            }).addItem(new ItemDescriptor({
                value: value,
                label: value,
                labelSuffix: " (" + label + ")",
                title: value,
                allowDuplicate: false,
                noExactMatch: true          // this item doesn't count as an exact query match for selthis.ection purposes
            }));
        },
        /**
         * Does the item descriptor match any of the selected values
         * @param {ItemDescriptor} itemDescriptor
         * @param {String[]} selectedVals
         * @return {Boolean}
         */
        isSelected: function (itemDescriptor, selectedVals) {
            return _.any(selectedVals, function (descriptor) {
                return itemDescriptor.value() === descriptor.value();
            });
        },
        /**
         * Removes duplicate descriptors
         *
         * @param descriptors
         * @param vals
         * @return {Array}
         */
        removeDuplicates: function (descriptors, vals) {
            vals = vals || [];
            return _.filter(descriptors, _.bind(function (descriptor) {
                if (descriptor instanceof GroupDescriptor) {
                    descriptor.items(this.removeDuplicates(descriptor.items(), vals));
                    return true;
                } else if (!_.include(vals, descriptor.value())) {
                    if (descriptor.value()) {
                        vals.push(descriptor.value());
                    }
                    return true;
                }
            }, this));
        },
        /**
         * Loop over all descriptors and remove descriptors that match selected vals. Usually if the user has already
         * selected a suggestion, we don't want to show it.
         * @param {GroupDescriptor[], ItemDescriptor[]} descriptors
         * @param {String[]} selectedValues
         * @return {GroupDescriptor[], ItemDescriptor[]} descriptors
         * @private
         */
        removeSelected: function (descriptors, selectedValues) {
            return _.filter(descriptors, _.bind(function (descriptor) {
                if ((descriptor instanceof ItemDescriptor) && this.isSelected(descriptor, selectedValues)) {
                    return false;
                }
                if (descriptor instanceof GroupDescriptor) {
                    descriptor.items(this.removeSelected(descriptor.items(), selectedValues));
                }
                return true;
            }, this));
        }
    };

});

define('jira/ajs/select/suggestions/default-suggest-handler', [
    'jira/lib/class',
    'jira/ajs/select/suggestions/suggest-helper',
    'jquery'
], function(
    Class,
    SuggestHelper,
    jQuery
) {
    /**
     * A default suggestion handler. Used for autocomplete without a backing <select>
     * @class SuggestHandler
     * @class DefaultSuggestHandler
     */
    return Class.extend({
        /**
         * @constructor
         * @param options
         */
        init: function (options) {
            this.options = options;
            this.descriptorFetcher = SuggestHelper.createDescriptorFetcher(options);
        },
        /**
         * Check if we should mirror input as a suggestion
         * @param {String} query
         * @return {Boolean}
         */
        validateMirroring: function (query) {
            return this.options.userEnteredOptionsMsg && query.length > 0
        },
        /**
         * Applies default formatting
         *
         * @param {Array} descriptors
         * @param {String} query
         * @return {*}
         */
        formatSuggestions: function (descriptors, query) {
            if (this.validateMirroring(query)) {

                descriptors.push(SuggestHelper.mirrorQuery(query, this.options.userEnteredOptionsMsg, this.options.uppercaseUserEnteredOnSelect));
            }
            return descriptors;
        },

        /**
         * Requests descriptors then formats them
         * @param {String} query
         * @param {Boolean} force
         * @return {*}
         */
        execute: function (query, force) {
            var deferred = jQuery.Deferred();
            var fetcherDef = this.descriptorFetcher.execute(query, force).done(_.bind(function (descriptors) {
                if (descriptors) {
                    descriptors = this.formatSuggestions(descriptors, query)
                }
                deferred.resolve(descriptors, query);
            }, this));
            deferred.fail(function () {
                fetcherDef.reject();
            });
            return deferred;
        }
    });

});

define('jira/ajs/select/suggestions/select-suggest-handler', [
    'jira/ajs/select/suggestions/default-suggest-handler',
    'jira/ajs/select/suggestions/suggest-helper'
], function(
    DefaultSuggestHandler,
    SuggestHelper
) {
    /**
     * A suggestion handler that removes suggestions that have already been selected in <select>
     * @class SelectSuggestHandler
     * @extends DefaultSuggestHandler
     */
    return DefaultSuggestHandler.extend({
        /**
         * @constructor
         * @param {Object} options
         * @param {SelectModel} model
         */
        init: function (options, model) {
            this.descriptorFetcher = SuggestHelper.createDescriptorFetcher(options, model);
            this.options = options;
            this.model = model;
        },
        /**
         * Formats suggestions removing already selected descriptors
         * @param descriptors
         * @param query
         * @return {GroupDescriptor[]}*/
        formatSuggestions: function (descriptors, query) {
            var suggestions = this._super(descriptors, query);
            var selectedDescriptors = this.model.getDisplayableSelectedDescriptors();
            if (this.options.removeDuplicates) {
                suggestions = SuggestHelper.removeDuplicates(descriptors);
            }
            return SuggestHelper.removeSelected(suggestions, selectedDescriptors);
        }
    });

});

define('jira/ajs/select/suggestions/assignee-suggest-handler', [
    'jira/ajs/select/suggestions/select-suggest-handler'
], function(
    SelectSuggestHandler
) {
    /**
     * Special handler for assignee picker that appends some footer text prompting user to start typing for more options.
     * @class AssigneeSuggestHandler
     * @extends SelectSuggestHandler
     */
    return SelectSuggestHandler.extend({
        /**
         * Formats suggestions removing already selected descriptors
         * @param descriptors
         * @param query
         * @return {GroupDescriptor[]}*/
        formatSuggestions: function (descriptors, query) {
            var descriptors = this._super(descriptors, query);
            if (query.length === 0) {
                descriptors[0].footerText(AJS.I18n.getText("user.picker.ajax.short.desc"));
            }
            return descriptors;
        }
    });


});

define('jira/ajs/select/suggestions/checkbox-multi-select-suggest-handler', [
    'jira/ajs/select/suggestions/select-suggest-handler',
    'jira/ajs/select/suggestions/suggest-helper',
    'jira/ajs/list/group-descriptor'
], function(
    SelectSuggestHandler,
    SuggestHelper,
    GroupDescriptor
) {
    /**
     * A suggestion handler that without a query, shows selected items at the top followed by unselected items in their groups.
     * When querying selected and unselected items are munged together and sorted in alphabetical order.
     * @class CheckboxMultiSelectSuggestHandler
     * @extends SelectSuggestHandler
     */
    return SelectSuggestHandler.extend({

        /**
         * Creates html string for clear all
         * @return {String}
         */
        createClearAll: function () {
            return "<li class='check-list-group-actions'><a class='clear-all' href='#'>" + AJS.I18n.getText("jira.ajax.autocomplete.clear.all") + "</a></li>";
        },

        /**
         * Formats descriptors for display in checkbox multiselect
         *
         * @param descriptors
         * @param query
         * @return {Array} formatted descriptors
         */
        formatSuggestions: function (descriptors, query) {

            var selectedItems = SuggestHelper.removeDuplicates(this.model.getDisplayableSelectedDescriptors());
            var selectedGroup = new GroupDescriptor({
                styleClass: "selected-group",
                items: selectedItems,
                actionBarHtml: selectedItems.length > 1 ? this.createClearAll()  : null
            });
            descriptors.splice(0, 0, selectedGroup);
            if (query.length > 0) {
                descriptors = SuggestHelper.removeDuplicates(descriptors);
                // Extract all items from the descriptors and sort them by label.
                var items = SuggestHelper.extractItems(descriptors).sort(function(a, b) {
                    a = a.label().toLowerCase();
                    b = b.label().toLowerCase();
                    return a.localeCompare(b);
                });
                descriptors = [new GroupDescriptor({items: items})];
            }
            return descriptors;
        }
    });

});

define('jira/ajs/select/suggestions/user-list-suggest-handler', [
    'jira/ajs/select/suggestions/select-suggest-handler'
], function(
    SelectSuggestHandler
) {
    /**
     * Special handler for share dialog pickers.
     * @class UserListSuggestHandler
     * @extends SelectSuggestHandler
     */
    return SelectSuggestHandler.extend({
        /**
         * Tests valid email address
         */
        emailExpression: /^([a-zA-Z0-9_\.\-\+])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/,
        /**
         * Only mirror user input if it is a valid email address
         * @param {String} query
         * @return {Boolean}
         */
        validateMirroring: function (query) {
            return this.options.freeEmailInput && query.length > 0 && this.emailExpression.test(query)
        }
    });
});

define('jira/ajs/select/suggestions/only-new-items-suggest-handler', [
    'jira/ajs/select/suggestions/select-suggest-handler'
], function(
        SelectSuggestHandler
        ) {
    /**
     * Special handler that will only allow new items to be mirrored.
     * @class OnlyNewItemsSuggestHandler
     * @extends SelectSuggestHandler
     */
    return SelectSuggestHandler.extend({
        /**
         * Only mirror user input if it doesn't exist yet in the list of options based on label match.
         * @param {String} query
         * @return {Boolean}
         */
        validateMirroring:function(query) {
            var allowMirroring = this._super(query);
            if(allowMirroring) {
                var allExistingDescriptors = this.model.getDisplayableSelectedDescriptors().concat(this.model.getDisplayableUnSelectedDescriptors());
                var existingItem = _.some(allExistingDescriptors, function(descriptor) {
                    return descriptor.label() === query;
                });
                return !existingItem;
            }
            return false;
        }
    });
});

define('jira/ajs/select/fetchers/static-descriptor-fetcher', [
    'jira/lib/class',
    'jquery'
], function(
    Class,
    jQuery
) {
    /**
     * Gets unselected <option>s from <select> as suggestions
     * @class StaticDescriptorFetcher
     */
    return Class.extend({
        /**
         * @param {Object} options - empty in this case
         * @param {SelectModel} model - a wrapper around <select> element
         */
        init: function (options, model) {
            this.model = model;
            this.model.$element.data("static-suggestions", true);
        },
        /**
         * @return {jQuery.Deferred}
         */
        execute: function (query) {
            var deferred = jQuery.Deferred();
            deferred.resolve(this.model.getUnSelectedDescriptors(), query);
            return deferred;
        }
    });
});

define('jira/ajs/select/fetchers/ajax-descriptor-fetcher', [
    'jira/lib/class',
    'jira/ajs/ajax/smart-ajax',
    'jquery',
    'underscore'
], function(
    Class,
    SmartAjax,
    jQuery,
    _
) {
    /**
     * Retrieves json from server and converts it into descriptors using formatSuggestions function supplied by user.
     * @class AjaxDescriptorFetcher
     */
    return Class.extend({

        /**
         * @constructor
         * @param options
         */
        init: function (options) {
            this.options = _.extend({
                keyInputPeriod: 75, // Wait this long between key strokes before going to server
                minQueryLength: 1, // Need these many characters before we go to server
                data: {},
                dataType: "json"
            }, options);
        },

        // Actually make the request and notify those interested
        makeRequest: function(deferred, ajaxOptions, query) {
            ajaxOptions.complete = _.bind(function () {
                this.outstandingRequest = null;
            }, this);
            ajaxOptions.success = _.bind(function (data) {
                if (ajaxOptions.query) {
                    deferred.resolve(ajaxOptions.formatResponse(data, query));
                } else {
                    this.lastResponse = ajaxOptions.formatResponse(data, query);
                    deferred.resolve(this.lastResponse);
                }
            }, this);
            var originalError = ajaxOptions.error;
            ajaxOptions.error = function (xhr, textStatus, msg, smartAjaxResult) {
                if (!smartAjaxResult.aborted) {
                    if (originalError) {
                        originalError.apply(this, arguments);
                    } else {
                        alert(SmartAjax.buildSimpleErrorContent(smartAjaxResult,{ alert : true }));
                    }
                }
            };

            this.outstandingRequest = SmartAjax.makeRequest(ajaxOptions); // issue requestcle
        },

        /**
         * Prepare the data and prevent throttling of server
         * @param {jQuery.Deferred} deferred
         * @param {Object} ajaxOptions - standard jQuery ajax options
         * @param {String} query - in most cases this is the user input
         * @param {Boolean} force - ignore request buffers. I want my request dispatched NOW.
         */
        incubateRequest: function(deferred, ajaxOptions, query, force) {

            clearTimeout(this.queuedRequest); // cancel any queued requests

            if (force && this.outstandingRequest) {
                this.outstandingRequest.abort();
                this.outstandingRequest = null;
            }

            if (!ajaxOptions.query && this.lastResponse) {
                deferred.resolve(this.lastResponse);
            } else if (!this.outstandingRequest) {
                if (typeof ajaxOptions.data === 'function') {
                    ajaxOptions.data = ajaxOptions.data(query);
                } else {
                    ajaxOptions.data.query = query;
                }

                if (typeof ajaxOptions.url === 'function') {
                    ajaxOptions.url = ajaxOptions.url();
                }

                if ((query.length >= parseInt(ajaxOptions.minQueryLength, 10)) || force) {
                    this.makeRequest(deferred, ajaxOptions, query);
                } else {
                    deferred.resolve();
                }
            } else {
                this.queuedRequest = setTimeout(_.bind(function () {
                    this.incubateRequest(deferred, ajaxOptions, query, true);
                }, this), ajaxOptions.keyInputPeriod);
            }

            return deferred;
        },
        /**
         * Sets up a request
         * @param {Function} query - lazily evaluated value of input field.
         * @param {Boolean} force - Piss off all buffers etc. Make request now!
         * @return {jQuery.Deferred}
         */
        execute: function (query, force) {
            var deferred = jQuery.Deferred();
            deferred.fail(_.bind(function () {
                clearTimeout(this.queuedRequest);
                if (this.outstandingRequest) {
                    this.outstandingRequest.abort();
                }
            }, this));
            this.incubateRequest(deferred, _.extend({}, this.options), query, force);
            return deferred;
        }
    });

});

define('jira/ajs/select/fetchers/mixed-descriptor-fetcher', [
    'jira/lib/class',
    'jira/ajs/select/fetchers/ajax-descriptor-fetcher',
    'jquery',
    'underscore'
], function(
    Class,
    AjaxDescriptorFetcher,
    jQuery,
    _
) {
    /**
     * Gets suggestions from unselected <option>s in <select> as well as going to the server upon character for more
     * results on input.
     *
     * @class MixedDescriptorFetcher
     */
    return Class.extend({
        /**
         *
         * @param {Object} options - jQuery ajax options object. With additional:
         * @param {function} options.formatResponse - function for creating descriptors out of server response
         * @param {number} options.minQueryLength - min input length before a request is made
         * @param {Object} options.ajaxOptions
         * @param {SelectModel} model - a wrapper around <select> element
         */
        init: function (options, model) {
            this.ajaxFetcher = new AjaxDescriptorFetcher(options.ajaxOptions);
            this.options = options;
            this.model = model;
        },
        /**
         * @param query
         * @param force
         * @return {jQuery.Deferred}
         */
        execute: function (query, force) {
            var deferred = jQuery.Deferred();
            // This needs to come after the return statement...
            if (query.length >= 1) {
                var ajaxDeferred = this.ajaxFetcher.execute(query, force).done(_.bind(function (suggestions) {
                    // JRADEV-21004
                    // Put suggestions at the front to avoid them being removed by removeDuplicates() method.
                    // After that, we sort the descriptors based on a label, so this change won't affect the
                    // final result
                    var descriptors = [].concat(suggestions).concat(this.model.getAllDescriptors());
                    deferred.resolve(descriptors, query);
                }, this));
                deferred.fail(function () {
                    ajaxDeferred.reject();
                });
            } else {
                deferred.resolve(this.model.getUnSelectedDescriptors(), query);
            }
            return deferred;
        }
    });
});

define('jira/ajs/select/fetchers/func-descriptor-fetcher', [
    'jira/lib/class',
    'jquery'
], function(
    Class,
    jQuery
) {
    /**
     * A single fetcher that will just return the result of calling supplied function
     *
     * @class FuncDescriptorFetcher
     */
    return Class.extend({
        /**
         * @constructor
         * @param options
         */
        init: function (options) {
             this.options = options;
        },
        /**
         * Gets result of function
         * @param query

         */
        execute: function (query) {
            var deferred = jQuery.Deferred();
            deferred.resolve(this.options.suggestions(query), query);
            return deferred;
        }
    });
});

AJS.namespace('AJS.SuggestHelper', null, require('jira/ajs/select/suggestions/suggest-helper'));
AJS.namespace('AJS.DefaultSuggestHandler', null, require('jira/ajs/select/suggestions/default-suggest-handler'));
AJS.namespace('AJS.SelectSuggestHandler', null, require('jira/ajs/select/suggestions/select-suggest-handler'));
AJS.namespace('AJS.OnlyNewItemsSuggestHandler', null, require('jira/ajs/select/suggestions/only-new-items-suggest-handler'));
AJS.namespace('AJS.CheckboxMultiSelectSuggestHandler', null, require('jira/ajs/select/suggestions/checkbox-multi-select-suggest-handler'));
AJS.namespace('JIRA.AssigneeSuggestHandler', null, require('jira/ajs/select/suggestions/assignee-suggest-handler'));
AJS.namespace('AJS.UserListSuggestHandler', null, require('jira/ajs/select/suggestions/user-list-suggest-handler'));

AJS.namespace('AJS.StaticDescriptorFetcher', null, require('jira/ajs/select/fetchers/static-descriptor-fetcher'));
AJS.namespace('AJS.AjaxDescriptorFetcher', null, require('jira/ajs/select/fetchers/ajax-descriptor-fetcher'));
AJS.namespace('AJS.MixedDescriptorFetcher', null, require('jira/ajs/select/fetchers/mixed-descriptor-fetcher'));
AJS.namespace('AJS.FuncDescriptorFetcher', null, require('jira/ajs/select/fetchers/func-descriptor-fetcher'));
