AJS.test.require("jira.webresources:key-commands");

(function() {

    module("Sequenced Shortcut Events", {
        teardown: function() {
            jQuery(document).unbind("shortcut");
        }
    });

    test('"shortcut" event fires for single character shortcut', function() {
        var counter = new ShortcutCounter("x");
        keypress("x");
        equal(counter.valueOf(), 1, 'Shortcut "x" event fires for input stream [x]');
    });

    test('"shortcut" event fires for multi-character shortcut sequence', function() {
        var counter = new ShortcutCounter("xyz");
        keypress("x");
        keypress("y");
        keypress("z");
        equal(counter.valueOf(), 1, 'Shortcut "xyz" event fires for input stream [x,y,z]');
    });

    test('"shortcut" event does not fire for non-matching character sequences', function() {
        var counter = new ShortcutCounter("xyz");
        keypress("z");
        keypress("x");
        keypress("z");
        keypress("x");
        keypress("y");
        keypress("y");
        keypress("z");
        equal(counter.valueOf(), 0, 'Shortcut "xyz" event does not fire for input stream [z,x,z,x,y,y,z]');
    });

    test("Conflicting shortcut sequences do not throw errors", function() {
        jQuery(document).bind("shortcut", "xyz", jQuery.noop);
        jQuery(document).bind("shortcut", "xy", jQuery.noop);
        jQuery(document).bind("shortcut", "y", jQuery.noop);
        jQuery(document).bind("shortcut", "xyzy", jQuery.noop);
        expect(0);
    });

    test('"shortcut" event only fires for longest matching sequence', function() {
        var counter0 = new ShortcutCounter("yz");
        var counter1 = new ShortcutCounter("xyz");
        keypress("x");
        keypress("y");
        keypress("z");
        equal(counter0.valueOf(), 0, 'Shortcut "yz" event does not fire for input stream [x,y,z]');
        equal(counter1.valueOf(), 1, 'Shortcut "xyz" event fires for input stream [x,y,z]');
    });

    test("Input stream resets after a completed shortcut", function() {
        var counter0 = new ShortcutCounter("zy");
        var counter1 = new ShortcutCounter("xyz");
        keypress("z");
        keypress("x");
        keypress("y");
        keypress("z");
        keypress("y");
        equal(counter0.valueOf(), 0, 'Shortcut "zy" does not fire for input stream [z,x,y,z,y]');
        equal(counter1.valueOf(), 1, 'Shortcut "xyz" fires for input stream [z,x,y,z,y]');
    });

    test("Input stream resets after a non-character key is pressed", function() {
        var counter0 = new ShortcutCounter("xy");
        keypress("x");
        keydown(17);
        keydown(89);
        keypress("y");
        equal(counter0.valueOf(), 0, "CTRL key resets input stream");
        var counter1 = new ShortcutCounter("xz");
        keypress("x");
        keydown(16);
        keydown(90);
        keypress("z");
        equal(counter1.valueOf(), 1, "SHIFT key does not reset input stream");
    });

    test('Key events targeted at form element do not fire "shortcut" events', function() {
        var counter = new ShortcutCounter("xy");
        var textarea = document.getElementById("qunit-fixture").appendChild(document.createElement("textarea"));
        keypress("x", document);
        keypress("y", textarea);
        keypress("x", textarea);
        keypress("y", textarea);
        equal(counter.valueOf(), 0, 'Key events targeted at <textarea> do not fire "shortcut" event');
        keypress("x", textarea);
        keypress("y", document);
        equal(counter.valueOf(), 0, "Key events targeted at <textarea> reset the character input stream");
    });

    function keydown(keyCode, target) {
        jQuery(target || document).trigger({
            type: "keydown",
            keyCode: keyCode,
            which: keyCode
        });
    }

    function keypress(character, target) {
        jQuery(target || document).trigger({
            type:  "keypress",
            which: character.charCodeAt(0)
        });
    }

    function ShortcutCounter(shortcut) {
        var i = 0;
        this.valueOf = function() {
            return i;
        };
        jQuery(document).bind("shortcut", shortcut, function() {
            i++;
        });
    }

})();
