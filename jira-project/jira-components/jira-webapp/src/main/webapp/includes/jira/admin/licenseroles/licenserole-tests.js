;AJS.test.require("jira.webresources:license-roles");

require(["jquery", "underscore", "backbone", "jira/admin/licenseroles"],
function ($, _, Backbone, Roles) {
    "use strict";

    var GroupViewList = function ($el) {
        this.$el = $el;
    };

    _.extend (GroupViewList.prototype, {
        groups: function () {
            return this.$el.find(".license-role-group-list li").map(function () {
                return $.trim($(this).text());
            }).get();
        }
    });

    var GroupEditList = function ($el) {
        this.$el = $el;
    };

    _.extend (GroupEditList.prototype, {
        groups: function () {
            return this.$el.find("#license-role-groups-select option").map(function () {
                return $.trim($(this).text());
            }).get();
        }
    });

    module("JIRA.Admin.LicenseRoles.GroupView", {
        setup: function () {
            this.$fixture = $("#qunit-fixture");
        }
    });

    test("Renders groups correctly", function () {
        var view = new Roles.GroupView({
            model: new Backbone.Model({groups: ["abc", "def"]})
        });
        this.$fixture.html(view.render());
        var groupList = new GroupViewList(this.$fixture);
        deepEqual(groupList.groups(), ["abc", "def"], "Groups 'abc', 'def' rendered correctly.");
    });

    module("JIRA.Admin.LicenseRoles.GroupEditView", {
        setup: function () {
            this.$fixture = $("#qunit-fixture");
        }
    });

    test("Renders edit groups correctly", function () {
        var view = new Roles.GroupEditView({
            model: new Backbone.Model({groups: ["abc", "def"]})
        });
        this.$fixture.html(view.render());
        var groupList = new GroupEditList(this.$fixture);
        deepEqual(groupList.groups(), ["abc", "def"], "Groups 'abc', 'def' rendered correctly.");
    });

    module("Roles.IO");

    test("Get Roles makes correct REST call.", function () {

        var promise = $.Deferred();
        promise.resolve([
            {name: "z", groups: []},
            {name: "a", groups: ["z", "a"]}
        ]);

        var getFunc = sinon.stub();
        getFunc.returns(promise);

        var io = new Roles.IO({
            ajax: getFunc
        });

        var success = sinon.spy(), fail = sinon.spy();

        io.getRoles().done(success).fail(fail);

        ok(getFunc.calledWith({
            url: contextPath + "/rest/api/2/licenserole",
            dataType: "json"
        }), "Called correct REST endpoint.");

        ok(!fail.called, "Fail should not be called.");
        ok(success.called, "Success called.");

        var expected = [
            {name: "a", groups: ["a", "z"]},
            {name: "z", groups: []}
        ];

        deepEqual(success.firstCall.args[0], expected, "Sorted data?");
    });

    test("Get Roles reports errors.", function () {

        var promise = $.Deferred();
        promise.reject("errors");

        var getFunc = sinon.stub();
        getFunc.returns(promise);

        var io = new Roles.IO({
            ajax: getFunc
        });

        var success = sinon.spy(), fail = sinon.spy();

        io.getRoles().done(success).fail(fail);

        ok(fail.calledWith("errors"), "Fail should be called.");
        ok(!success.called, "Success not called.");
    });

    test("Put Roles makes correct REST call.", function () {

        var promise = $.Deferred();
        promise.resolve({name: "a", groups: ["z", "a"]});

        var postFunc = sinon.stub();
        postFunc.returns(promise);

        var io = new Roles.IO({
            ajax: postFunc
        });

        var success = sinon.spy(), fail = sinon.spy();

        io.putRole("a", ["z", "a"]).done(success).fail(fail);

        ok(postFunc.called, "AJAX method called.");
        var expectedAjax = {
            url: contextPath + "/rest/api/2/licenserole/a",
                    dataType: "json",
                type: "PUT",
                contentType: "application/json",
                data: JSON.stringify({groups: ["z", "a"]})
        };
        deepEqual(postFunc.firstCall.args[0], expectedAjax, "AJAX method called with correct argument.");
        ok(postFunc.calledWith(), "Called correct REST endpoint.");

        ok(!fail.called, "Fail should not be called.");
        ok(success.called, "Success called.");

        var expected = {name: "a", groups: ["a", "z"]};
        deepEqual(success.firstCall.args[0], expected, "Sorted data?");
    });

    test("Put Roles reports errors.", function () {

        var promise = $.Deferred();
        promise.reject("errors");

        var putFunc = sinon.stub();
        putFunc.returns(promise);

        var io = new Roles.IO({
            ajax: putFunc
        });

        var success = sinon.spy(), fail = sinon.spy();

        io.putRole("me").done(success).fail(fail);

        ok(fail.calledWith("errors"), "Fail should be called.");
        ok(!success.called, "Success not called.");
    });


    module("Roles.RoleEditorView", {
        setup: function () {
            this.sandbox = sinon.sandbox.create();
            this.errorHandler = this.sandbox.stub(JIRA.ErrorDialog, "openErrorDialogForXHR");
            this.table = this.sandbox.stub(AJS, "RestfulTable");
            this.fixture = new Backbone.Marionette.Region({
                el: $("#qunit-fixture")
            });
        },
        teardown: function () {
            this.sandbox.restore();
        }
    });

    test("onShow creates Restful table.", function () {

        var view = new Roles.RoleEditorView({
            io: {
                getRoles: function () {
                    return $.Deferred().resolve("good");
                }
            }
        });

        this.fixture.show(view);

        ok(this.table.calledOnce, "Created RestTable");
        var options = this.table.firstCall.args[0];

        ok(options.allowEdit, "The table should be editable.");
        ok(!options.allowCreate, "Can't create.");
        ok(!options.allowDelete, "Can't delete.");

        ok(this.fixture.el.find("table.license-role-table").length, "Table added");

        var columns = options.columns;
        equal(columns[0].id, "name", "First column is name.");
        equal(columns[0].styleClass, "license-role-name", "First column has name class.");
        equal(columns[0].header, "licenserole.role.concept", "First column has correct header.");
        ok(!columns[0].allowEdit, "First column cannot be edited.");

        equal(columns[1].id, "groups", "Second column is groups.");
        equal(columns[1].styleClass, "license-role-groups", "Second column has group class.");
        equal(columns[1].header, "common.words.groups", "Second has correct header.");
        equal(columns[1].emptyText, "admin.usersandgroups.add.group", "Second column has correct empty text.");
        ok(columns[1].readView === Roles.GroupView, "Second column rendered by right view.");
        ok(columns[1].editView === Roles.GroupEditView, "Second column edited by right view.");
        ok(columns[1].fieldName === "license-role-groups-select-textarea", "Field name correctly set for second column.");
    });

    test("Get all pushes results to table", function () {

        var view = new Roles.RoleEditorView({
            io: {
                getRoles: function () {
                    return $.Deferred().resolve("good");
                }
            }
        });

        this.fixture.show(view);

        ok(this.table.calledOnce, "Created RestTable");
        var options = this.table.firstCall.args[0];

        var callback = sinon.spy();
        options.resources.all(callback);
        ok(callback.calledWith("good"), "Call method to fill table.");
        ok(!this.errorHandler.called, "Error handler not called.");
    });

    test("Get all error reports error.", function () {

        var view = new Roles.RoleEditorView({
            io: {
                getRoles: function () {
                    return $.Deferred().reject("bad");
                }
            }
        });

        this.fixture.show(view);

        ok(this.table.calledOnce, "Created RestTable");
        var options = this.table.firstCall.args[0];

        var callback = sinon.spy();
        options.resources.all(callback);
        ok(!callback.called, "Table not filled.");
        ok(this.errorHandler.calledWith("bad"), "Error handler called.");
    });

    module("Roles.RoleEditor", {
        setup: function () {
            this.sandbox = sinon.sandbox.create();
            this.$fixture = $("#qunit-fixture");

            this.init = sinon.spy();
            this.render = sinon.spy();
            this.show = sinon.spy();

            this.View = Backbone.Marionette.ItemView.extend({
                initialise: this.init,
                template: "<div>Hello World!</div>",
                onRender: this.render,
                onShow: this.show
            });

        },
        teardown: function () {
            this.sandbox.restore();
        }
    });

    test("roleEditor addsEditorView to UI ", function () {

        new Roles.RoleEditor({
            el: this.$fixture,
            view: this.View
        });

        ok(this.render.called, "Render called");
        ok(this.show.called, "Show called");
    });

    test("roleEditor does not add view to UI when not el passed", function () {

        new Roles.RoleEditor({
            view: this.View
        });

        ok(!this.render.called, "Render not called");
        ok(!this.show.called, "Show not called");
    });

    test("roleEditor does not add view to UI when el cannot be found", function () {

        new Roles.RoleEditor({
            el: "#dontFindMe",
            view: this.View
        });

        ok(!this.render.called, "Render not called");
        ok(!this.show.called, "Show not called");
    });

    module("Roles.Model", {
        setup: function () {
            this.sandbox = sinon.sandbox.create();
            this.id = "role";
            this.result = $.Deferred();
            this.put = sinon.stub().returns(this.result);
            this.role = new Roles.Model({
                io: {
                    putRole: this.put
                }
            });
            this.role.set({id: this.id, groups: ["a", "b"]});
            this.errorHandler = this.sandbox.stub(JIRA.ErrorDialog, "openErrorDialogForXHR");
        },
        teardown: function () {
            this.sandbox.restore();
        }
    });

    test("Save sets the model using result from server.", function () {
        this.result.resolve({groups: ["z"]});
        this.role.save({groups: ["b", "c"]}, {});
        deepEqual(this.role.get("groups"), ["z"], "Updated to server reported state.");
    });

    test("Save calls success method of passed arguments.", function () {
        this.result.resolve({groups: ["z"]});

        var success = sinon.stub(), error = sinon.stub();
        this.role.save({groups: ["b", "c"]}, {success: success, error: error});

        ok(success.calledWith({groups: ["z"]}), "Success calls success handler.");
        ok(!error.called, "Error not called.");
    });

    test("Save calls putRole with correct arguments", function () {
        this.result.resolve({groups: ["z"]});

        this.role.save({groups: ["b", "c"]}, {});

        ok(this.put.calledWith(this.id, ["b", "c"]), "Put called with correct arguments.");
    });

    test("Save calls putRole with correct arguments when no groups provided.", function () {
        this.result.resolve({groups: ["z"]});

        this.role.save({}, {});

        ok(this.put.calledWith(this.id, []), "Put called with empty groups.");
    });

    test("Calls global error handler when no error callback provided.", function () {
        var xhr = {
            status: 400
        };
        this.result.reject(xhr);
        this.role.save({}, {});

        ok(this.errorHandler.calledWith(xhr), "Error handler called.");
    });

    test("Calls global error handler when non-400 error generated.", function () {
        var xhr = {
            status: 404
        };

        this.result.reject(xhr);

        var success = sinon.stub(), error = sinon.stub();
        this.role.save({}, {success: success, error: error});

        ok(this.errorHandler.calledWith(xhr), "Error handler called.");
        ok(!success.called, "Success handler should not be called.");
        ok(!error.called, "Error handler should not be called.");
    });

    var assertErrorHandlerCalled = function(text) {
        var xhr = {
            status: 400,
            responseText: text
        };

        this.result.reject(xhr);

        var success = sinon.stub(), error = sinon.stub();
        this.role.save({}, {success: success, error: error});

        ok(!this.errorHandler.called, "Global error handler not called.");
        ok(!success.called, "Success handler should not be called.");

        ok(error.called, "Error called");
        var args = error.firstCall.args;

        var expectedErrors;
        try {
            expectedErrors = text && JSON.parse(text) || {};
        } catch (e) {
            expectedErrors = {};
        }

        ok(args[0] === this.role, "Error handler takes model as first argument.");
        deepEqual(args[1], expectedErrors, "Error handler takes errors as second argument.");
        deepEqual(args[2], xhr, "Error handler takes xhr as third argument.");
    };

    test("Calls error handler on 400 error with error response empty.", function () {
        assertErrorHandlerCalled.call(this);
    });

    test("Calls error handler on 400 error with error response invalid JSON.", function () {
        assertErrorHandlerCalled.call(this, "<div>Error</div>");
    });

    test("Calls error handler on 400 error with valid JSON error response.", function () {
        assertErrorHandlerCalled.call(this, JSON.stringify({value: true}));
    });
});