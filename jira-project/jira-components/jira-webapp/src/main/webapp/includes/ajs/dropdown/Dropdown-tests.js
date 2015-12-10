AJS.test.require("jira.webresources:dropdown");
AJS.test.require("jira.webresources:key-commands");


(function($) {
    module("Dropdown Menus", {
        teardown: function() {
            console.log("Tearing");
            $(".ajs-layer").remove();
        }
    });

    function pressKey(keyCode) {
        $(document).trigger({
            type: "keydown",
            keyCode: keyCode,
            which: keyCode
        });
    }

    test("An arrow keypress does not select hidden list entries", function() {

        var dropDown = $("<div id='dropDown' class='aui-list'>"
                         + "<ul id='list_id'>"
                           + "<li id='visible-list-entry'>"
                             + "<a id='test-link-1' href='/test/path/1'>Test 1</a>"
                           + "</li>"
                           + "<li id='hidden-list-entry' class='hidden'>"
                             + "<a id='test-link-2' href='/test/path/2'>Test 2</a>"
                           + "</li>"
                         + "</ul>"
                       + "</div>"),

            trigger = $("<a id='trigger' href='#'>Trigger</a>");

        AJS.Dropdown.create({
            content: dropDown,
            trigger: trigger
        });

        trigger.appendTo("#qunit-fixture");

        trigger.click();

        pressKey(40);

        ok($('#visible-list-entry').hasClass("active"), "First LI has active class when down pressed once.");

        pressKey(40);

        ok(!$('#hidden-list-entry').hasClass("active"), "Hidden LI doesn't have active class when down pressed twice.");
    });

    test("An arrow keypress does not select nested list entries", function() {

        var dropDown = $("<div id='dropDown'>"
                         + "<ul id='nested_list_id'>"
                           + "<li id='first-level-list-entry-1'>"
                             + "<ul><li id='nested-list-entry'><a id='test-link-1' href='/test/path/1'>Test 1</a></li></ul>"
                           + "</li>"
                           + "<li id='first-level-list-entry-2'><a id='test-link-2' href='/test/path/2'>Test 2</a></li>"
                         + "</ul>"
                       + "</div>"),
            trigger = $("<a id='trigger' href='#'>Trigger</a>");

        AJS.Dropdown.create({
            content: dropDown,
            trigger: trigger
        });

        trigger.appendTo("#qunit-fixture");

        trigger.click();

        pressKey(40);

        ok(!$('#nested-list-entry').hasClass("active"), "Nested LI does not have active class when down pressed once.");
        ok($('#first-level-list-entry-1').hasClass("active"), "Top level LI has active class when down pressed once.");

        pressKey(40);

        ok($('#first-level-list-entry-2').hasClass("active"), "Second top level LI has active class when down pressed twice.");
    });

    test("An arrow keypress does not select an list entry with no anchor", function() {

        var dropDown = $("<div id='dropDown'>"
                         + "<ul id='nested_list_id'>"
                           + "<li id='empty-first-level-list-entry'>"
                             + "<ul></ul>"
                           + "</li>"
                           + "<li id='first-level-list-entry-1'><a id='test-link-1' href='/test/path/1'>Test 1</a></li>"
                         + "</ul>"
                       + "</div>"),
            trigger = $("<a id='trigger' href='#'>Trigger</a>");

        AJS.Dropdown.create({
            content: dropDown,
            trigger: trigger
        });

        trigger.appendTo("#qunit-fixture");

        trigger.click();

        pressKey(40);

        ok(!$('#empty-first-level-list-entry').hasClass("active"), "Empty LI does not have active class when down pressed.");
        ok($('#first-level-list-entry-1').hasClass("active"), "Top level LI has active class when down pressed once.");
    });

})(AJS.$);
