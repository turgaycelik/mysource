;(function() {
    var KeyboardShortcut = require('jira/ajs/keyboardshortcut/keyboard-shortcut');
    var KeyboardShortcutToggle = require('jira/ajs/keyboardshortcut/keyboard-shortcut-toggle');
    var Meta = require('jira/util/data/meta');
    var JiraDialog = require('jira/dialog/dialog');
    var AuiDropdown = require('aui/dropdown');
    var AuiPopup = require('aui/popup');
    var jQuery = require('jquery');

    /**
     * Ignore keyboard shortcuts when we have a dialog, dropdwon etc shown.
     */
    KeyboardShortcut.addIgnoreCondition(function () {
        return AuiPopup.current || AuiDropdown.current || JiraDialog.current || KeyboardShortcutToggle.areKeyboardShortcutsDisabled();
    });

    jQuery(function() {
        if (!!Meta.get("keyboard-shortcuts-enabled")) {
            KeyboardShortcutToggle.enable();
        } else {
            KeyboardShortcutToggle.disable();
        }

        if (AJS.keys) {
            AJS.activeShortcuts = KeyboardShortcut.fromJSON(AJS.keys.shortcuts);
        }
    });

})();
