;define("jira/admin/licenseroles",
["jquery", "underscore", "backbone", "jira/ajs/ajax/smart-ajax/web-sudo", "jira/dialog/error-dialog"],
function ($, _, Backbone, WebSudo, ErrorDialog) {
    "use strict";

    var templates = JIRA.Templates.Admin.LicenseRoles;
    var licenseroles = {};

    /**
     * Compares the passed two strings in a case-insensitive manner.
     *
     * @param {String} a left-hand side of the comparison.
     * @param {String} b right-hand side of the comparison.
     * @returns {number} Returns < 0 when a < b, > 0 when a > b or 0 when a == b.
     */
    var localeCompare = function (a, b) {
        a = a || "";
        b = b || "";
        return a.localeCompare(b);
    };

    /**
     * Generic error handler.
     */
    var errorHandler = function (xhr) {
        ErrorDialog.openErrorDialogForXHR(xhr);
    };

    licenseroles.Model = AJS.RestfulTable.EntryModel.extend({

        initialize: function (options) {
            this.io = options.io || new licenseroles.IO();
        },
        save: function (attributes, options) {
            var instance = this, putRole = this.io.putRole(this.id, attributes.groups || []);
            putRole.done(function (data) {
                instance.set(data);
            });
            putRole.fail(function (xhr) {
                if (!options.error || xhr.status !== 400) {
                    errorHandler(xhr);
                } else {
                    var data;
                    try {
                        data = xhr.responseText && JSON.parse(xhr.responseText) || {};
                    } catch (e) {
                        data = {};
                    }
                    options.error.call(instance, instance, data, xhr);
                }
            });
            if (options.success) {
                putRole.done(options.success);
            }
        }
    });

    /**
     * Renders the read only view of a list of groups.
     *
     * @constructor
     */
    licenseroles.GroupView = AJS.RestfulTable.CustomReadView.extend({
        render: function () {
            return templates.groups({
                groups: this.model.get('groups')
            });
        }
    });

    /**
     * Renders the edit view for the list of groups.
     */
    licenseroles.GroupEditView = AJS.RestfulTable.CustomEditView.extend({
        render: function () {
            return templates.editGroups({
                groups: this.model.get('groups')
            });
        }
    });

    /**
     * Abstraction around all AJAX calls to JIRA to do with license roles.
     *
     * @constructor
     */
    licenseroles.IO = function (options) {
        options = options || {};
        this.operations = {
            ajax: options.ajax || WebSudo.makeWebSudoRequest.bind(WebSudo)
        }
    };

    _.extend(licenseroles.IO.prototype, {
        /**
         * Returns a JSON representation of all the license roles in JIRA.
         *
         * @returns {jQuery.Promise} A promise that will complete with the role data. Errors are also reported
         *  through the returned promise.
         */
        getRoles: function () {
            return this.operations.ajax({
                url: this._makeUrl(),
                dataType: "json"
            }).pipe(function (data) {
                _.each(data, function (role) {
                    role.groups.sort(localeCompare);
                });
                return data.sort(function (a, b) {
                    return localeCompare(a.name, b.name);
                });
            });
        },
        putRole: function(roleName, groups) {
            return this.operations.ajax({
                url: this._makeUrl(roleName),
                dataType: "json",
                type: "PUT",
                contentType: "application/json",
                data: JSON.stringify({
                    groups: _.toArray(groups)
                })
            }).pipe(function (data) {
                data.groups.sort(localeCompare);
                return data;
            });
        },
        /**
         * Create a URL for the passed ROLE.
         *
         * @param {string} [role] the role to create the URL for.
         * @returns {string} the URL for the passed ROLE.
         * @private
         */
        _makeUrl: function (role) {
            var url = contextPath + "/rest/api/2/licenserole";
            if (role) {
                url += "/" + role;
            }
            return url;
        }
    });

    licenseroles.RoleEditorView = Backbone.Marionette.ItemView.extend({

        /**
         * The object that renders and manages UI for License Roles viewing/editing.
         *
         * @param {object} options.io object that can be used to retrieve the list of current roles. It needs to
         *  have a `getRoles` method.
         *
         * @constructor
         */
        initialize: function (options) {
            this.io = options.io;
        },

        template: templates.table,
        ui: {
            table: ".license-role-table"
        },
        onShow: function () {
            var io = this.io;
            this.table = new AJS.RestfulTable({
                model: licenseroles.Model,
                allowEdit: true,
                allowCreate: false,
                allowDelete: false,
                el: this.ui.table,
                resources: {
                    all: function (callback) {
                        io.getRoles().done(callback).fail(errorHandler);
                    }
                },
                columns: [
                    {
                        id: "name",
                        styleClass: "license-role-name",
                        header: AJS.I18n.getText("licenserole.role.concept"),
                        allowEdit: false
                    },
                    {
                        id: "groups",
                        styleClass: "license-role-groups",
                        header: AJS.I18n.getText("common.words.groups"),
                        emptyText: AJS.I18n.getText('admin.usersandgroups.add.group'),
                        readView: licenseroles.GroupView,
                        editView: licenseroles.GroupEditView,
                        fieldName: "license-role-groups-select-textarea"
                    }
                ],
                noEntriesMsg: AJS.I18n.getText("licenserole.no.roles.installed")
            });
        }
    });

    /**
     * An editor for the license role configuration in JIRA.
     *
     * @param {element|String|jQuery} options.el the element to add the editor to. Its contents will be overwritten.
     * @constructor
     */
    licenseroles.RoleEditor = function (options) {
        if (!options.el) {
            return;
        }
        var $el = $(options.el);
        if (!$el.length) {
            return;
        }
        var region = new Marionette.Region({
            el: options.el
        });
        var view = options.view || licenseroles.RoleEditorView;
        region.show(new view({
            io: new licenseroles.IO()
        }));
    };

    return licenseroles;
});
