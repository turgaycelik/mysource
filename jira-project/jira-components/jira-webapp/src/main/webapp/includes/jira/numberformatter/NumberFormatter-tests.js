AJS.test.require("jira.webresources:jira-global");

module("NumberFormatter", {
    setup: function() {
        this.metaGetStub = sinon.stub(AJS.Meta, 'get');
    },

    teardown: function() {
        this.metaGetStub.restore();
    }
});

test("Format integers", function() {
    this.metaGetStub.withArgs('user-locale-group-separator').returns(',');
    equal(JIRA.NumberFormatter.format(1234), "1,234");
    equal(JIRA.NumberFormatter.format(12345), "12,345");
    equal(JIRA.NumberFormatter.format(123456), "123,456");
    equal(JIRA.NumberFormatter.format(1234567), "1,234,567");
    equal(JIRA.NumberFormatter.format(-1234567), "-1,234,567");

    this.metaGetStub.withArgs('user-locale-group-separator').returns('.');
    equal(JIRA.NumberFormatter.format(1234567), "1.234.567");

    this.metaGetStub.withArgs('user-locale-group-separator').returns('');
    equal(JIRA.NumberFormatter.format(1234567), "1234567");
});