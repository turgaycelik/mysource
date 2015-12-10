/**
 * Applies dirty form warnings to dialogs
 *
 * Usage:
 * var myDialog = new JIRA.FormDialog({...});
 * myDialog.dirtyFormWarning(); // bind dirty form warning behaviour
 */
JIRA.Dialog.fn.dirtyFormWarning = function () {
    var handler = function (e, popup, hideReason) {
        var dirtyMessage;
        if (!e.isDefaultPrevented() && hideReason === JIRA.Dialog.HIDE_REASON.escape) {
            dirtyMessage = JIRA.DirtyForm.getDirtyWarning();
            if(dirtyMessage && !confirm(dirtyMessage)) {
                e.preventDefault();
            }
        }
    };
    return function () {
        return this.bind("Dialog.beforeHide", handler);
    };
}();