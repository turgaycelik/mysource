(function (AJS, $, JIRA, _) {
    AJS.namespace("JIRA.Admin.CustomFields.UserPickerFilter");

    var templateNamespace = JIRA.Templates.Admin.CustomFields.UserPickerFilter;

    /**
     * The selector panel that provides the UI and logic for configuring user picker filters.
     *
     * It does not include codes/logic for getting the required data to render the panel, e.g., user filter, groups, project roles,
     * nor does it include codes/logic for submitting the configured filter back to the server.
     *
     * Need to be changed to Backbone MVC
     */
    JIRA.Admin.CustomFields.UserPickerFilter.SelectorPanel = {
        /**
         * render selector in the panel.
         * also store _userFilter and populate _projectRolesIdMap.
         *
         * and then setup the required event handlers.
         *
         * @param $selectorPanel the jquery panel element to draw the selector
         * @param userFilter the original user filter
         * @param groups {{name:string}[]} all groups in the system for selection
         * @param projectRoles {{id:number,name:string,description:string}[]} all project roles in the system for selection
         */
        initialize: function ($selectorPanel, userFilter, groups, projectRoles) {
            this.$selectorPanel = $selectorPanel;
            this._initData(userFilter, projectRoles);
            this._renderSelectorPanel(this._userFilter, groups, projectRoles);
            this._setupEventHandlers();
        },

        getUserFilter : function () {
            return this._copyUserFilter(this._userFilter);
        },

        isUserFilterEnabled : function () {
            return this._userFilter.enabled;
        },

        getFilterCheckbox: function () {
            return this._getElement('#filter-checkbox');
        },

        _userFilter : {},
        _projectRolesIdMap : {}, // mapping from role id to {name, description} for display
        _numOfEntries : 0,

        // renderer functions that accept the value (group name or role id) and render the filter entry
        // return the rendered html
        _entryRenderers : {
            'group' : function (value) {
                return templateNamespace.showGroupFilterEntry({
                    name : value, seq : this._numOfEntries ++
                });
            },
            'role' : function (value) {
                var roleData = this._projectRolesIdMap[value];
                if (roleData && roleData.name) {
                    return templateNamespace.showRoleFilterEntry({
                        name : roleData.name, seq : this._numOfEntries ++, roleId : value
                    });
                } else {
                    return "";
                }
            }
        },

        // updater functions that update the user filter, with add and remove operations
        // return true if added/removed, false if the value already exists (for add) or not exists (for remove) in the user filter
        _jsonUpdaters : {
            'add' : {
                'group' : function(newGroup) {
                    if ($.inArray(newGroup, this._userFilter.groups) > -1) {
                        return false;
                    } else {
                        this._userFilter.groups.push(newGroup);
                        return true;
                    }
                },
                'role' : function(newRoleId) {
                    var newRoleIdAsInt = parseInt(newRoleId, 10); // when reading from option.val, it's a string
                    if (isNaN(newRoleIdAsInt) || $.inArray(newRoleIdAsInt, this._userFilter.roleIds) > -1) {
                        return false;
                    } else {
                        this._userFilter.roleIds.push(newRoleIdAsInt);
                        return true;
                    }
                }
            },
            'remove' : {
                'group' : function(oldGroup) {
                    var index = $.inArray(oldGroup, this._userFilter.groups);
                    if (index > -1) {
                        this._userFilter.groups.splice(index, 1);
                        return true;
                    } else {
                        return false;
                    }
                },
                'role' : function(oldRoleId) {
                    // when reading from data(), it's auto-converted to integer. but we never know whether it might be called from other places
                    var oldRoleIdAsInt = parseInt(oldRoleId, 10);
                    if (isNaN(oldRoleIdAsInt)) {
                        return false;
                    }
                    var index = $.inArray(oldRoleIdAsInt, this._userFilter.roleIds);
                    if (index > -1) {
                        this._userFilter.roleIds.splice(index, 1);
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        },

        _getElement: function(selector) {
            return this.$selectorPanel.find(selector);
        },

        _initData: function (userFilter, projectRoles) {
            this._userFilter = this._copyUserFilter(_.extend(this._getDefaultUserFilter(), userFilter));
            this._projectRolesIdMap = this._populateRoleDetailsMap(projectRoles);
        },

        /**
         * render the panel in the #filter-selector-panel div.
         * also store _userFilter and populate _projectRolesIdMap.
         * @param userFilter
         * @param groups
         * @param projectRoles
         */
        _renderSelectorPanel: function (userFilter, groups, projectRoles) {
            // render the filter selector panel
            this.$selectorPanel.append(templateNamespace.renderSelectorPanel({
                'groups' : groups, 'projectRoles' : projectRoles, "userFilter" : userFilter
            }));

            // render the initial filters from _userFilter
            this._renderInitEntries();
        },

        /**
         * setup event handlers for the selector panel.
         */
        _setupEventHandlers: function() {
            var instance = this;

            this.getFilterCheckbox().change(function() {
                var el = $(this);
                instance._userFilter.enabled = el.is(':checked');
                instance._updateUIForEnabledChange();
            });

            this._getTypeSelector().change(function () {
                var selectedSelector = instance._getCurrentSelector();
                instance._getElement('.filter-selector').not(selectedSelector).hide();
                instance._getElement(selectedSelector).show();
            });

            this._getAddFilterIcon().click(function() {
                var selectedType = instance._getSelectedType(),
                        selectedValue = instance._getSelectedValue();
                if (instance._updateJson(selectedType, 'add', selectedValue)) {
                    instance._renderEntry(selectedType, selectedValue);
                } else {
                    // do something to inform the user
                    // or maybe disable the selected ones so that user could not even select them
                }
            });

            // bind to container div so that it works for new entries added later
            this._getDisplayDiv().on('click', '.delete-filter', function() {
                var el = $(this),
                        entryDiv = el.closest('.filter-entry'),
                        selectedType = entryDiv.data('type'),
                        selectedValue = entryDiv.data('value');
                if (instance._updateJson(selectedType, 'remove', selectedValue)) {
                    entryDiv.remove();
                    if (--instance._numOfEntries == 0) {
                        instance._updateUIForNoFilterEntriesMsg();
                    }
                }
            });
        },

        _copyUserFilter : function (userFilter) {
            var userFilterCopy = { enabled : userFilter.enabled, groups : [], roleIds : [] };
            if (userFilterCopy.enabled) {
                if (userFilter.groups) {
                    userFilterCopy.groups = userFilter.groups.slice();
                }
                if (userFilter.roleIds) {
                    userFilterCopy.roleIds = userFilter.roleIds.slice();
                }
            }
            return userFilterCopy;
        },

        _updateJson : function (type, operation, value) {
            var updatersByOp = this._jsonUpdaters[operation];
            if (updatersByOp) {
                var updater = updatersByOp[type];
                if (updater && typeof updater === 'function') {
                    return updater.call(this, value);
                }
            }
            return false;
        },

        _getDefaultUserFilter : function () {
            return {
                enabled: false,
                groups: [],
                roleIds: []
            };
        },

        _getDisplayDiv : function() {
            return this._getElement('#filter-display-div');
        },

        _addToDisplay : function(displayHtml) {
            if (displayHtml) {
                this._getDisplayDiv().append(displayHtml);
            }
        },

        _getNoFilterEntryDiv : function() {
            return this._getElement('#filter-entry-div-none');
        },

        _getNoFilterWarningDiv : function() {
            return this._getElement('#filter-none-msg');
        },

        _renderEntry : function (type, value) {
            var renderer = this._entryRenderers[type];
            if (renderer && typeof renderer === 'function') {
                this._addToDisplay(renderer.call(this, value));
                if (this._numOfEntries === 1) {
                    this._updateUIForNoFilterEntriesMsg(); // remove the no entries msg
                }
            }
        },
        /**
         * create a map from project role id to project role object for easy lookup of project role names.
         * @param projectRoles
         * @private
         */
        _populateRoleDetailsMap: function (projectRoles) {
            var detailsMap = {};
            $.each(projectRoles, function(index, projectRole) {
                detailsMap[projectRole.id] = projectRole;
            });
            return detailsMap;
        },

        _renderInitEntries: function () {
            var instance = this;
            if (this._userFilter.enabled) {
                if (this._userFilter.groups) {
                    $.each(this._userFilter.groups, function(index, elem) {
                        instance._renderEntry('group', elem);
                    });
                }
                if (this._userFilter.roleIds) {
                    $.each(this._userFilter.roleIds, function(index, elem) {
                        instance._renderEntry('role', elem);
                    });
                }
            } else {
                // just to make sure that the data is clean
                this._userFilter = this._getDefaultUserFilter();
            }
            this._updateUIForEnabledChange();
        },

        _updateUIForEnabledChange : function() {
            var isEnabled = this._userFilter.enabled,
                    selectPanel = this._getElement('#filter-selector-selection-panel'),
                    disabledMsg = this._getElement('#filter-disabled-msg');
            if (isEnabled) {
                selectPanel.show();
                disabledMsg.hide();
            } else {
                selectPanel.hide();
                disabledMsg.show();
            }
            this._updateUIForNoFilterEntriesMsg();
        },

        _updateUIForNoFilterEntriesMsg : function() {
            if (this._numOfEntries === 0 && this._userFilter.enabled) {
                this._getNoFilterEntryDiv().show();
                this._getNoFilterWarningDiv().show();
            } else {
                this._getNoFilterEntryDiv().hide();
                this._getNoFilterWarningDiv().hide();
            }
        },

        _getTypeSelector: function () {
            return this._getElement('#filter-selector-type');
        },

        _getGroupSelector: function() {
            return this._getElement('#filter-selector-group');
        },

        _getRoleSelector: function() {
            return this._getElement('#filter-selector-role');
        },

        _getAddFilterIcon : function() {
            return this._getElement('#add-filter');
        },

        /**
         * return whether we are selecting group or role
         * @private
         */
        _getSelectedType : function () {
            return this._getTypeSelector().find('option:selected').val();
        },

        /**
         * return the group or role that's selected
         * @private
         */
        _getSelectedValue: function () {
            return this._getElement(this._getCurrentSelector()).find('option:selected').val();
        },

        _getCurrentSelector : function () {
            return '#filter-selector-' + this._getSelectedType();
        }
    };
})(AJS, AJS.$, JIRA, _);