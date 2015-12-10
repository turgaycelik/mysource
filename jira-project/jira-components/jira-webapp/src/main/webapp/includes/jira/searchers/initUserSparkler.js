JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, $ctx) {
    // Creates group/item descriptors from an AJAX response.
    function _formatUserGroupResponse (response) {
        var users = _formatUserResponse(response.users, true);
        var groups = _formatGroupResponse(response.groups, true);
        var items = [].concat(users).concat(groups);
        return [new AJS.GroupDescriptor({items: items})];
    }

    function _formatUserResponse(response, prefix) {
        return _.map(response.users, function (item) {
            return new AJS.ItemDescriptor({
                highlighted: true,
                html: item.html,
                icon: item.avatarUrl,
                label: item.displayName,
                value: (prefix ? "user:" : "") + item.name
            });
        });
    }

    function _formatGroupResponse(response, prefix) {
        return _.map(response.groups, function (item) {
            return new AJS.ItemDescriptor({
                highlighted: true,
                html: item.html,
                icon: AJS.contextPath() + "/images/icons/icon_groups_16.png",
                label: item.name,
                value: (prefix ? "group:" : "")+ item.name
            });
        });
    }

    jQuery(".js-usergroup-checkboxmultiselect", $ctx).each(function () {
        var ajaxData = {};
        // grab additional parameters from fieldset
        AJS.$("fieldset.user-group-searcher-params", $ctx).each(function(){
            ajaxData = JIRA.parseOptionsFromFieldset(AJS.$(this))
        });
        ajaxData.showAvatar = true;
        new AJS.CheckboxMultiSelect({
            element: this,
            maxInlineResultsDisplayed: 10,
            content: "mixed",
            ajaxOptions: {
                url: AJS.contextPath() + "/rest/api/latest/groupuserpicker",
                data: ajaxData,
                query: true,
                formatResponse: _formatUserGroupResponse
            }
        });
    });

    jQuery(".js-user-checkboxmultiselect", $ctx).each(function () {
        new AJS.CheckboxMultiSelect({
            element: this,
            maxInlineResultsDisplayed: 5,
            content: "mixed",
            ajaxOptions: {
                url: AJS.contextPath() + "/rest/api/latest/user/picker",
                data: {
                    showAvatar: true
                },
                query: true,
                formatResponse: function (items) {
                    return _formatUserResponse(items, false);
                }
            }
        });
    });

    jQuery(".js-group-checkboxmultiselect", $ctx).each(function () {
        new AJS.CheckboxMultiSelect({
            element: this,
            maxInlineResultsDisplayed: 5,
            content: "mixed",
            ajaxOptions: {
                url: AJS.contextPath() + "/rest/api/latest/groups/picker",
                data: {
                    showAvatar: true
                },
                query: true,
                formatResponse: function (items) {
                    return _formatGroupResponse(items, false);
                }
            }
        });
    });

});