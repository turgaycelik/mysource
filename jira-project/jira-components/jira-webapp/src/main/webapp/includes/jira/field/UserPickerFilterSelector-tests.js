AJS.test.require("jira.webresources:user-picker-filter-configuration-resources");

module("JIRA.Admin.CustomFields.UserPickerFilter", {
    setup : function () {
        this.sandbox = sinon.sandbox.create();

        var fixture = jQuery("#qunit-fixture");

        this.$selectorPanel = jQuery('<div id="selector-panel">').appendTo(fixture);
    },

    teardown : function () {
        this.sandbox.restore();
    },

    _createProjectRole: function(id, name, description) {
        return {id: id, name: name, description: description};
    },

    _createGroup: function(name) {
        return {name: name};
    },

    initPanelData: function(userFilter) {
        JIRA.Admin.CustomFields.UserPickerFilter.SelectorPanel._initData(userFilter, [this._createProjectRole(10000, "prj1", "desc 1")]);
        return JIRA.Admin.CustomFields.UserPickerFilter.SelectorPanel;
    },

    initPanel: function(userFilter) {
        JIRA.Admin.CustomFields.UserPickerFilter.SelectorPanel.initialize(this.$selectorPanel, userFilter, this.getDefaultGroups(), this.getDefaultProjectRoles());
        return JIRA.Admin.CustomFields.UserPickerFilter.SelectorPanel;
    },

    getDefaultProjectRoles: function() {
        return [
            this._createProjectRole(10001, "prj role 1", "desc 1"),
            this._createProjectRole(10002, "prj role 2", "desc 2"),
            this._createProjectRole(10003, "prj role 3", "desc 3")
        ];
    },

    getDefaultGroups: function() {
        return [
            this._createGroup('group 1'),
            this._createGroup('group 2'),
            this._createGroup('group 3')
        ];
    },

    filter: function(enabled, roleIds, groups) {
        return _.extend({ enabled: enabled},
                _.isArray(groups) ? { groups: groups} : {},
                _.isArray(roleIds) ? { roleIds: roleIds} : {}
        );
    },

    selectAndAdd: function(panel, type, value) {
        panel._getTypeSelector().val(type).change();
        if (type === 'group') {
            panel._getGroupSelector().val(value).change();
        } else {
            panel._getRoleSelector().val(value).change();
        }
        panel._getAddFilterIcon().click();
    },

    addGroup: function(panel, groupName) {
        this.selectAndAdd(panel, 'group', groupName);
    },

    addRole: function(panel, roleId) {
        this.selectAndAdd(panel, 'role', roleId);
    },

    remove: function(panel, type, value) {
        var deleteIcon = this.$selectorPanel.find(".filter-entry[data-type='" + type + "'][data-value='" + value +"']").find('.delete-filter');
        deleteIcon.click();
    },

    removeGroup: function(panel, groupName) {
        this.remove(panel, 'group', groupName);
    },

    removeRole: function(panel, roleId) {
        this.remove(panel, 'role', roleId);
    }

});

test("json updater adds new role and returns true", function() {
    var panel = this.initPanelData({enabled: true});
    ok(panel._updateJson('role', 'add', '123'), 'adding new role id returns true');
    var userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.roleIds, 123), 'new role id is added into user filter');
});

test("json updater adds existing role and returns false", function() {
    var panel = this.initPanelData({enabled: true, roleIds: [123]});
    ok(!panel._updateJson('role', 'add', '123'), 'adding existing role id returns false');
    var userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.roleIds, 123), 'existing role id is still in user filter');
});

test("json updater adds non integer role id should return false", function() {
    var panel = this.initPanelData({enabled: true, roleIds: []});
    ok(!panel._updateJson('role', 'add', 'a123b'), 'adding invalid role id returns false');
    var userFilter = panel.getUserFilter();
    equal(_.size(userFilter.roleIds), 0, 'no new role id is added');
});

test("json updater removes existing role and returns true", function() {
    var panel = this.initPanelData({enabled: true, roleIds: [123]});
    ok(panel._updateJson('role', 'remove', '123'), 'removing existing role id returns true');
    var userFilter = panel.getUserFilter();
    ok(!_.contains(userFilter.roleIds, 123), 'old role id is removed into user filter');
});

test("json updater removes non-existing role and returns false", function() {
    var panel = this.initPanelData({enabled: true, roleIds: [456]});
    ok(!panel._updateJson('role', 'remove', '123'), 'removing non-existing role id returns false');
    var userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.roleIds, 456), 'existing role id is still in user filter');
});

test("json updater adds new group and returns true", function() {
    var panel = this.initPanelData({enabled: true});
    ok(panel._updateJson('group', 'add', 'g1'), 'adding new group returns true');
    var userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.groups, 'g1'), 'new group is added into user filter');
});

test("json updater adds existing group and returns false", function() {
    var panel = this.initPanelData({enabled: true, groups: ['g1']});
    ok(!panel._updateJson('group', 'add', 'g1'), 'adding existing group returns false');
    var userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.groups, 'g1'), 'existing group is still in user filter');
});

test("json updater removes existing group and returns true", function() {
    var panel = this.initPanelData({enabled: true, groups: ['g1']});
    ok(panel._updateJson('group', 'remove', 'g1'), 'removing existing group returns true');
    var userFilter = panel.getUserFilter();
    ok(!_.contains(userFilter.groups, 'g1'), 'old group is removed into user filter');
});

test("json updater removes non-existing group and returns false", function() {
    var panel = this.initPanelData({enabled: true, groups: ['g2']});
    ok(!panel._updateJson('group', 'remove', 'g1'), 'removing non-existing group returns false');
    var userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.groups, 'g2'), 'existing group is still in user filter');
});


test("copy filter with disabled", function() {
    var testData = [
            [{enabled: false}, this.filter(false, [], []), 'disabled filter'],
            [{enabled: false, roleIds:[1], groups:['1']}, this.filter(false, [], []), 'disabled filter reset other fields'],
            [{enabled: true, roleIds:[1], groups:['1']}, this.filter(true, [1], ['1']), 'enabled filter'],
            [{enabled: true, groups:['1', '2', '3']}, this.filter(true, [], ['1', '2', '3']), 'enabled filter without roles'],
            [{enabled: true, roleIds:[1, 2, 3]}, this.filter(true, [1,2,3], []), 'enabled filter without groups'],
            [{enabled: true, groups:['1','2','3'], roleIds:[1, 2, 3]}, this.filter(true, [1,2,3], ['1','2','3']), 'enabled filter without groups']
    ];
    var panel = JIRA.Admin.CustomFields.UserPickerFilter.SelectorPanel;
    _.each(testData, function(testItem) {
        var original = testItem[0];
        var copied = panel._copyUserFilter(original);
        var expected = testItem[1];
        deepEqual(copied, expected, testItem[2]);
        original.enabled = !original.enabled;
        if (_.size(original.roleIds) > 0) {
            original.roleIds[0] = 12345;
        }
        if (_.size(original.groups) > 0) {
            original.groups[0] = '12345';
        }
        deepEqual(copied, expected, testItem[2] + ' copied not changed');
    });
});




test("adds groups from UI", function() {
    var panel = this.initPanel({enabled: true, groups: []});
    this.addGroup(panel, 'group 1');
    var userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.groups, 'group 1'), 'group 1 is added into empty groups in filter successfully');
    equal(_.size(userFilter.roleIds), 0, 'roleIds not changed');
    this.addGroup(panel, 'group 2');
    userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.groups, 'group 2'), 'group 2 is added into groups in filter successfully');
    equal(_.size(userFilter.roleIds), 0, 'roleIds not changed');

    this.addGroup(panel, 'group 1');
    userFilter = panel.getUserFilter();
    equal(_.size(userFilter.groups), 2, 'size of groups in filter is not changed after adding duplicated group');
    ok(_.contains(userFilter.groups, 'group 1'), 'existing groups 1 are still in filter');
    ok(_.contains(userFilter.groups, 'group 2'), 'existing groups 2 are still in filter');
    equal(_.size(userFilter.roleIds), 0, 'roleIds not changed');

    this.addGroup(panel, 'group 3');
    userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.groups, 'group 3'), 'group 3 is added into groups in filter successfully');
    equal(_.size(userFilter.roleIds), 0, 'roleIds not changed');
});

test("adds roles from UI", function() {
    var panel = this.initPanel({enabled: true, groups: []});
    this.addRole(panel, 10001);
    var userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.roleIds, 10001), 'role 10001 is added into empty roleIds in filter successfully');
    equal(_.size(userFilter.groups), 0, 'groups not changed');
    this.addRole(panel, 10002);
    userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.roleIds, 10002), 'role 10002 is added into roleIds in filter successfully');
    equal(_.size(userFilter.groups), 0, 'groups not changed');

    this.addRole(panel, 10001);
    userFilter = panel.getUserFilter();
    equal(_.size(userFilter.roleIds), 2, 'size of roles in filter is not changed after adding duplicated roleId');
    ok(_.contains(userFilter.roleIds, 10001), 'existing roles 10001 are still in filter');
    ok(_.contains(userFilter.roleIds, 10002), 'existing roles 10002 are still in filter');
    equal(_.size(userFilter.groups), 0, 'groups not changed');

    this.addRole(panel, 10003);
    userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.roleIds, 10003), 'role 10003 is added into roleIds in filter successfully');
    equal(_.size(userFilter.groups), 0, 'groups not changed');
});

test("adds roles and groups from UI", function() {
    var panel = this.initPanel({enabled: true, groups: [], roleIds: []});
    this.addRole(panel, 10001);
    var userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.roleIds, 10001), 'role 10001 is added into empty roleIds in filter successfully');
    equal(_.size(userFilter.groups), 0, 'groups not changed');

    this.addGroup(panel, 'group 2');
    userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.roleIds, 10001), 'role 10001 is still in filter');
    ok(_.contains(userFilter.groups, 'group 2'), 'group 2 is added into filter successfully');

    this.addGroup(panel, 'group 1');
    userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.roleIds, 10001), 'role 10001 is still in filter');
    ok(_.contains(userFilter.groups, 'group 1'), 'group 1 is added into filter successfully');
    ok(_.contains(userFilter.groups, 'group 2'), 'group 2 is still in filter');

    this.addRole(panel, 10003);
    userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.roleIds, 10003), 'role 10003 is added into filter successfully');
    ok(_.contains(userFilter.roleIds, 10001), 'role 10001 is still in filter');
    equal(_.size(userFilter.groups), 2, 'groups not changed');

    this.addRole(panel, 10002);
    userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.roleIds, 10002), 'role 10002 is added into filter successfully');
    ok(_.contains(userFilter.roleIds, 10001), 'role 10001 is still in filter');
    ok(_.contains(userFilter.roleIds, 10003), 'role 10003 is still in filter');
    equal(_.size(userFilter.groups), 2, 'groups not changed');
});

test("remove groups from UI", function() {
    var panel = this.initPanel({enabled: true, groups: ['group 1', 'group 2']});
    this.removeGroup(panel, 'group 1');
    var userFilter = panel.getUserFilter();
    ok(!_.contains(userFilter.groups, 'group 1'), 'group 1 is removed from filter successfully');
    equal(_.size(userFilter.roleIds), 0, 'roleIds not changed');

    this.removeGroup(panel, 'group 1');
    userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.groups, 'group 2'), 'group 2 is not affected and still in filter');
    ok(!_.contains(userFilter.groups, 'group 1'), 'group 1 is not in filter');
    equal(_.size(userFilter.roleIds), 0, 'roleIds not changed');

    this.addGroup(panel, 'group 3');
    userFilter = panel.getUserFilter();
    equal(_.size(userFilter.groups), 2, 'size of groups in filter is increased after adding a new group');
    ok(_.contains(userFilter.groups, 'group 3'), 'groups 3 is added into filter successfully');
    ok(_.contains(userFilter.groups, 'group 2'), 'existing groups 2 are still in filter');
    equal(_.size(userFilter.roleIds), 0, 'roleIds not changed');

    this.addGroup(panel, 'group 1');
    userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.groups, 'group 1'), 'group 1 is added back filter successfully');
    equal(_.size(userFilter.roleIds), 0, 'roleIds not changed');
});

test("remove roles from UI", function() {
    var panel = this.initPanel({enabled: true, roleIds: [10001, 10002]});
    this.removeRole(panel, 10001);
    var userFilter = panel.getUserFilter();
    ok(!_.contains(userFilter.roleIds, 10001), 'role 10001 is removed from filter successfully');
    equal(_.size(userFilter.groups), 0, 'groups not changed');

    this.removeRole(panel, 10001);
    userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.roleIds, 10002), 'role 10002 is not affected and still in filter');
    ok(!_.contains(userFilter.roleIds, 10001), 'role 10001 is not in filter');
    equal(_.size(userFilter.groups), 0, 'groups not changed');

    this.addRole(panel, 10003);
    userFilter = panel.getUserFilter();
    equal(_.size(userFilter.roleIds), 2, 'size of role in filter is increased after adding a new role');
    ok(_.contains(userFilter.roleIds, 10003), 'role 10003 is added into filter successfully');
    ok(_.contains(userFilter.roleIds, 10002), 'existing role 10002 are still in filter');
    equal(_.size(userFilter.groups), 0, 'groups not changed');

    this.addRole(panel, 10001);
    userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.roleIds, 10001), 'role 10001 is added back filter successfully');
    equal(_.size(userFilter.groups), 0, 'groups not changed');
});

test("remove groups and roles from UI", function() {
    var panel = this.initPanel({enabled: true, groups: ['group 1', 'group 2'], roleIds: [10001, 10002]});
    this.removeRole(panel, 10001);
    var userFilter = panel.getUserFilter();
    ok(!_.contains(userFilter.roleIds, 10001), 'role 10001 is removed from filter successfully');
    equal(_.size(userFilter.groups), 2, 'groups not changed');

    this.removeGroup(panel, 'group 1');
    userFilter = panel.getUserFilter();
    ok(!_.contains(userFilter.groups, 'group 1'), 'group 1 is removed from filter successfully');
    equal(_.size(userFilter.roleIds), 1, 'roleIds not changed');

    this.removeGroup(panel, 'group 2');
    userFilter = panel.getUserFilter();
    ok(!_.contains(userFilter.groups, 'group 2'), 'group 2 is removed from filter successfully');
    ok(!_.contains(userFilter.groups, 'group 1'), 'group 1 is not in filter');
    equal(_.size(userFilter.roleIds), 1, 'roleIds not changed');

    this.removeRole(panel, 10002);
    userFilter = panel.getUserFilter();
    ok(!_.contains(userFilter.roleIds, 10002), 'role 10002 is removed from filter successfully');
    ok(!_.contains(userFilter.roleIds, 10001), 'role 10001 is not in filter');
    equal(_.size(userFilter.groups), 0, 'groups not changed');

    this.addGroup(panel, 'group 3');
    userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.groups, 'group 3'), 'groups 3 is added into filter successfully');
    equal(_.size(userFilter.roleIds), 0, 'roleIds not changed');

    this.addRole(panel, 10003);
    userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.roleIds, 10003), 'role 10003 is added into filter successfully');
    equal(_.size(userFilter.groups), 1, 'groups not changed');

    this.addRole(panel, 10001);
    userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.roleIds, 10001), 'role 10001 is added back filter successfully');
    equal(_.size(userFilter.groups), 1, 'groups not changed');

    this.addGroup(panel, 'group 1');
    userFilter = panel.getUserFilter();
    ok(_.contains(userFilter.groups, 'group 1'), 'group 1 is added back filter successfully');
    equal(_.size(userFilter.roleIds), 2, 'roleIds not changed');
});

