    /**
     * A JIRA.MultiUserListPicker.Item represents an item selected in the Share Dialog & Watchers Dialog.
     * It is much like an AJS.MultiSelect.Lozenge but is rendered differently with slightly altered behaviour.
     *
     * @constructor JIRA.MultiUserListPicker.Item
     * @extends AJS.Control
     */
    JIRA.MultiUserListPicker.Item = AJS.Control.extend({

        init: function(options) {
            this._setOptions(options);

            this.$lozenge = this._render("item");
            this.$removeButton = this.$lozenge.find('.remove-recipient');

            this._assignEvents("instance", this);
            this._assignEvents("lozenge", this.$lozenge);
            this._assignEvents("removeButton", this.$removeButton);

            this.$lozenge.prependTo(this.options.container);
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
            "item": function() {
                var descriptor = this.options.descriptor;

                var data;
                if (descriptor.noExactMatch() !== true) {
                    // A User selected from the matches
                    data = {
                        escape: false,
                        username: descriptor.value(),
                        icon: descriptor.icon(),
                        displayName: AJS.escapeHtml(descriptor.label())
                    };

                    return AJS.$(JIRA.Templates.Fields.recipientUsername(data));
                } else {
                    // Just an email
                    data = {
                        email: descriptor.value(),
                        icon: AJS.Meta.get('default-avatar-url')
                    };
                    return AJS.$(JIRA.Templates.Fields.recipientEmail(data));
                }
            }
        },

        _events: {
            "instance": {
                "remove": function() {
                    this.$lozenge.remove();
                }
            },
            "removeButton": {
                "click": function(e) {
                    // We need to stop the click propagation, else by the time the InlineDialog catches the event the span
                    // will no longer be in the DOM and the click handler will think the user clicked outside of the dialog,
                    // closing it.
                    e.stopPropagation();
                    this.trigger("remove");
                }
            }
        }
    });
