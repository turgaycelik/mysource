define('jira/ajs/select/multi-select/lozenge', [
    'jira/ajs/control',
//    'aui',
    'jquery'
], function(
    Control,
//    AJS,
    jQuery
) {
    /**
     * A lozenge represents a discrete item of user input as a <button> element that can be focused, blurred and removed.
     *
     * @class MultiSelect.Lozenge
     * @extends Control
     */
    return Control.extend({

        init: function(options) {
            this._setOptions(options);

            this.$lozenge = this._render("lozenge");
            this.$removeButton = this._render("removeButton");

            this._assignEvents("instance", this);
            this._assignEvents("lozenge", this.$lozenge);
            this._assignEvents("removeButton", this.$removeButton);

            this.$removeButton.appendTo(this.$lozenge);
            this.$lozenge.appendTo(this.options.container);
        },

        _getDefaultOptions: function() {
            return {
                label: null,
                title: null,
                container: null,
                focusClass: "focused"
            };
        },

        _renders: {
            "lozenge": function() {
                var label = AJS.escapeHtml(this.options.label);
                var title = AJS.escapeHtml(this.options.title) || "";

                return jQuery('<li class="item-row" title="' + title + '"><button type="button" tabindex="-1" class="value-item"><span><span class="value-text">' + label + '</span></span></button></li>');
            },
            "removeButton": function() {
                return jQuery('<em class="item-delete" title="' + AJS.escapeHtml(AJS.I18n.getText("admin.common.words.remove")) + '"></em>');
            }
        },

        _events: {
            "instance": {
                "focus": function() {
                    this.$lozenge.addClass(this.options.focusClass);
                },
                "blur": function() {
                    this.$lozenge.removeClass(this.options.focusClass);
                },
                "remove": function() {
                    this.$lozenge.remove();
                }
            },
            "lozenge": {
                "click": function() {
                    this.trigger("focus");
                }
            },
            "removeButton": {
                "click": function() {
                    this.trigger("remove");
                }
            }
        }
    });

});

AJS.namespace('AJS.MultiSelect.Lozenge', null, require('jira/ajs/select/multi-select/lozenge'));
