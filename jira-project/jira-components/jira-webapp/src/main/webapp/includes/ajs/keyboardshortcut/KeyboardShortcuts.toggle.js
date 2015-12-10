define('jira/ajs/keyboardshortcut/keyboard-shortcut-toggle', [
    'exports'
], function(
    exports
) {
    var keyboardShortcutsDisabled = false;

    exports.disable = function() {
        keyboardShortcutsDisabled = true;
    };

    exports.enable = function() {
        keyboardShortcutsDisabled = false;
    };

    exports.areKeyboardShortcutsDisabled = function() {
        return keyboardShortcutsDisabled;
    };
});

// Legacy namespacing
(function(KST) {
    AJS.namespace('AJS.enableKeyboardShortcuts', null, KST.enable);
    AJS.namespace('AJS.disableKeyboardShortcuts', null, KST.disable);
})(require('jira/ajs/keyboardshortcut/keyboard-shortcut-toggle'));
