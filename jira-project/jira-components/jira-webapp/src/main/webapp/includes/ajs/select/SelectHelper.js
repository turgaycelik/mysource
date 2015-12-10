define('jira/ajs/select/select-helper', function() {
    return {
        updateFreeInputVal: function () {
            if (this.options.submitInputVal) {
                this.model.updateFreeInput(this.$field.val());
            }
        },

        removeFreeInputVal: function () {
            if (this.options.submitInputVal) {
                this.model.removeFreeInputVal();
            }
        }
    };
});

AJS.namespace('AJS.SelectHelper', null, require('jira/ajs/select/select-helper'));
