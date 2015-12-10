(function(KeyboardShortcut) {
    /**
     * We're overriding AUI's implementation for some reason... hooray!
     *
     * AJS.whenIType -- factory for creating new KeyboardShortcut instances
     *
     * Example usage:
     *
     * AJS.whenIType("gh").or("gd").goTo("/secure/Dashboard.jspa");
     * AJS.whenIType("c").click("#create_link");
     *
     * @param {string} shortcut
     * @returns {KeyboardShortcut}
     */
    AJS.whenIType = function(shortcut, ctx) {
        return new KeyboardShortcut(shortcut, ctx);
    };
    AJS.whenIType.fromJSON = KeyboardShortcut.fromJSON;

})(require('jira/ajs/keyboardshortcut/keyboard-shortcut'));
