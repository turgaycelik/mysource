define('jira/shifter/shifter-dialog', [
    'jira/lib/class',
    'jira/data/session-storage',
    'jira/shifter/shifter-select',
    'jira/ajs/list/group-descriptor',
    'jira/ajs/list/item-descriptor',
    'jquery',
    'underscore'
], function(
    Class,
    sessionStorage,
    ShifterSelect,
    GroupDescriptor,
    ItemDescriptor,
    jQuery,
    _
) {
    /**
     * @class ShifterDialog
     */
    return Class.extend({
        BLUR_DELAY: 50,

        /**
         * @constructor
         * @param {String} id - unique id for this dialog
         * @param {ShifterGroup[]} groups
         * @param {Object} options
         * @param {Number} options.maxResultsDisplayedPerGroup
         */
        init: function(id, groups, options) {
            this.id = id;
            this.groups = groups;
            this.options = options;
            this._render();
            this._destroyOnBlur();
            this._preventFocusOnNonInputElements();
            jQuery(document).on('mousedown.shifterdialog.' + id, _.bind(this._destroyOnMousedownOutside, this));
        },

        focus: function() {
            if (this.$dialog) {
                this.$dialog.find('input').focus();
            }
        },

        destroy: function() {
            var $dialog = this.$dialog;
            if ($dialog) {
                $dialog.stop().animate({
                    top: -1 * $dialog.height()
                }, 100, function() {
                    $dialog.remove();
                });
                this.$dialog = null;
                jQuery(document).off('mousedown.shifterdialog.' + this.id);
            }
        },

        destroyed: function() {
            return !this.$dialog;
        },

        saveLastQuery: function(query) {
            sessionStorage.setItem('JIRA.Shifter.lastQuery', query);
        },

        getLastQuery: function() {
            return sessionStorage.getItem('JIRA.Shifter.lastQuery');
        },

        enterLoadingState: function() {
            var $dialog = this.$dialog;
            $dialog.addClass('loading-action');
            $dialog.find('.aui-list').slideUp(200);
        },

        _render: function() {
            var html = JIRA.Templates.Shifter.dialog({
                id: this.id
            });
            var $dialog = this.$dialog = jQuery(html).appendTo('body');

            var shifterSelect = this.shifterSelect = new ShifterSelect({
                id: this.id,
                element: $dialog.find('.aui-list'),
                groups: this.groups,
                suggestionsHandler: this._makeSuggestionsHandler(),
                onSelection: _.bind(this._onSelection, this),
                maxResultsDisplayedPerGroup: this.options.maxResultsDisplayedPerGroup
            });

            if (this.getLastQuery()) {
                shifterSelect.$field.val(this.getLastQuery()).select();
                shifterSelect.onEdit();
            }

            shifterSelect.$field.on('keyup', _.bind(function(e) {
                if (e.which === jQuery.ui.keyCode.ESCAPE) {
                    e.stopPropagation();
                    this.destroy();
                }
            }, this));

            //JRADEV-19747 - Hide any layer to avoid conflicts with the keyboard handling (up, down, return...)
            if (AJS.currentLayerItem && AJS.currentLayerItem.hide) {
                AJS.currentLayerItem.hide()
            }

            $dialog.css('top', -1 * $dialog.height()).animate({
                top: 0
            }, 100);
        },

        _destroyOnBlur: function() {
            this.$dialog.find('input').blur(_.bind(function() {
                setTimeout(_.bind(function() {
                    if (this.$dialog && !jQuery.contains(this.$dialog[0], document.activeElement)) {
                        this.destroy();
                    }
                }, this), this.BLUR_DELAY);
            }, this));
        },

        /**
         * Preventing focus when clicking on elements inside the dialog doesn't completely work in IE8
         */
        _destroyOnMousedownOutside: function(e) {
            if (this.$dialog.find(e.target).length === 0 && e.target !== this.$dialog) {
                this.destroy();
            }
        },

        _preventFocusOnNonInputElements: function() {
            var $inputs = this.$dialog.find('input');
            this.$dialog.mousedown(function(e) {
                if (jQuery.inArray(e.target, $inputs) === -1) {
                    e.preventDefault();
                }
            });
        },

        _makeSuggestionsHandler: function() {
            var groups = this.groups;
            return Class.extend({
                execute: function(query) {
                    var suggestions = [];
                    var masterDeferred = jQuery.Deferred();
                    var deferredsRemaining = groups.length;
                    _.map(groups, function(group, groupIndex) {
                        return group.getSuggestions(query).done(function(groupSuggestions) {
                            if (!_.isArray(groupSuggestions)) {
                                return;
                            }
                            suggestions.push(new GroupDescriptor({
                                label: group.name,
                                description: group.context,
                                weight: group.weight,
                                items: _.map(groupSuggestions, function(suggestion) {
                                    return ItemDescriptor.create(suggestion, groupIndex);
                                })
                            }));
                            // Keep the groups sorted
                            suggestions.sort(function(a,b){ return a.weight() - b.weight(); });
                        }).always(function() {
                            deferredsRemaining--;
                            if (deferredsRemaining === 0) {
                                masterDeferred.resolve(suggestions, query);
                            }
                        });
                    });
                    return masterDeferred;
                }
            });
        },

        _onSelection: function(group, value, label) {
            this.saveLastQuery(label);
            var ret = group.onSelection(value);
            if (ret && _.isFunction(ret.always)) {
                this.enterLoadingState();
                ret.always(_.bind(this.destroy, this));
            } else {
                this.destroy();
            }
        }
    });
});

AJS.namespace('JIRA.ShifterComponent.ShifterDialog', null, require('jira/shifter/shifter-dialog'));
