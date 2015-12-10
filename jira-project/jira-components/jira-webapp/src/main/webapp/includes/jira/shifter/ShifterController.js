define('jira/shifter/shifter-controller', [
    'jira/lib/class',
    'jira/shifter/shifter-dialog',
    'underscore'
], function(
    Class,
    ShifterDialog,
    _
) {
    /**
     * @class ShifterController
     * @classdesc Registers/unregisters {@see ShifterGroup}s and shows/hides a {@see ShifterDialog}
     */
    return Class.extend({

        MAX_RESULTS_PER_GROUP: 5,

        init: function(id) {
            this.id = id;
            this.groupFactories = []
        },

        /**
         * @param {Function} groupFactory - a function that returns a ShifterGroup, an Array of ShifterGroup, or null.
         * @returns groupFactory
         */
        register: function(groupFactory) {
            this.groupFactories.push(groupFactory);
            return groupFactory;
        },

        /**
         * @param {Function} groupFactory - a reference to an already registered group factory
         * @returns groupFactory
         */
        unregister: function(groupFactory) {
            this.groupFactories = _.without(this.groupFactories, groupFactory);
            return groupFactory;
        },

        /**
         * Creates and shows a ShifterDialog if one is not already visible.
         * If one is visible, simple focus the query field.
         */
        show: function() {
            if (!this.dialog || this.dialog.destroyed()) {
                var groups = _.chain(this.groupFactories)
                .map(function(factory) {
                    return factory();
                })
                .compact()
                .flatten()
                .value()
                .sort(function(a, b) {
                    return a.weight - b.weight;
                });
                this.dialog = new ShifterDialog(this.id, groups, {
                    maxResultsDisplayedPerGroup: this.MAX_RESULTS_PER_GROUP
                });
            }
            this.dialog.focus();
        },

        /**
         * Hides and destroys the currently visible ShifterDialog.
         * Does nothing if a ShifterDialog is not currently shown.
         */
        hide: function() {
            this.dialog && this.dialog.destroy();
        }

    });
});

AJS.namespace('JIRA.ShifterComponent.ShifterController', window, require('jira/shifter/shifter-controller'));
