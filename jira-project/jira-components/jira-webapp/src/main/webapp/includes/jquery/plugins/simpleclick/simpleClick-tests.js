AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:common");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:sinon");

module("SimpleClick", {
    setup: function() {
        this.$container = jQuery('<div class="container"><a href="blah">Blah</a></div>');
        this.$anchor = this.$container.find('a');
    },

    clickEvent: function() {
        return jQuery.Event("click");
    },

    shiftClickEvent: function () {
        return jQuery.Event("click", {
            shiftKey: true
        });
    },

    metaClickEvent: function() {
        return jQuery.Event("click", {
            metaKey: true
        });
    }
});

test("Event bound on the anchor is triggered by a normal click", function() {
    var simpleClickSpy = sinon.spy();
    this.$anchor.on('simpleClick', simpleClickSpy);
    this.$anchor.trigger(this.clickEvent());
    equal(simpleClickSpy.callCount, 1, "simpleClick handler is called once");
});

test("Event bound on the anchor is not triggered by a meta click", function() {
    var simpleClickSpy = sinon.spy();
    this.$anchor.on('simpleClick', simpleClickSpy);
    this.$anchor.trigger(this.metaClickEvent());
    equal(simpleClickSpy.callCount, 0, "simpleClick handler is not called");
});

test("Event bound on the anchor is not triggered by a shift click", function() {
    var simpleClickSpy = sinon.spy();
    this.$anchor.on('simpleClick', simpleClickSpy);
    this.$anchor.trigger(this.shiftClickEvent());
    equal(simpleClickSpy.callCount, 0, "simpleClick handler is not called");
});

test("preventDefault() in a simpleClick event handler acts on the original click event", function() {
    this.$anchor.on('simpleClick', function(e) {
        e.preventDefault();
    });
    var clickEvent = this.clickEvent();
    this.$anchor.trigger(clickEvent);
    equal(clickEvent.isDefaultPrevented(), true, "Click event's default is prevented");
});

test("Event delegation still works", function() {
    var simpleClickSpy = sinon.spy();
    this.$container.on('simpleClick', 'a', simpleClickSpy);
    this.$anchor.trigger(this.clickEvent());
    equal(simpleClickSpy.callCount, 1, "Delegated handler on the container is called once");
});

test("Removing the handler works", function() {
    var simpleClickSpy = sinon.spy();
    this.$anchor.on('simpleClick', simpleClickSpy);
    this.$anchor.off('simpleClick');
    this.$anchor.trigger(this.clickEvent());
    equal(simpleClickSpy.callCount, 0, "Event handler is not called after being removed");
});