define('jira/mention/mention-group', [
    'jira/ajs/group'
], function(
    Group
) {
    return Group.extend({
        _makeSelection: function(e) {
            if (this.items && this.items[this.index]) {
                this.items[this.index].trigger("accept");
                e.preventDefault();
            }
        },

        keys: {
            "Up": function(e) {
                this.shiftFocus(-1);
                e.preventDefault();
            },
            "Down": function(e) {
                this.shiftFocus(1);
                e.preventDefault();
            },
            "Return": function(e) {
                this._makeSelection(e);
            },
            "Tab": function(e) {
                this._makeSelection(e);
            }
        }
    });
});

AJS.namespace('JIRA.MentionGroup', null, require('jira/mention/mention-group'));
