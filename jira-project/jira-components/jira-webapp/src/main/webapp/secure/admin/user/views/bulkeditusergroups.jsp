<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.bulkeditgroups.title'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="group_browser"/>
</head>
<body>
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'mainContent'">
            <ul class="aui-nav aui-nav-breadcrumbs">
                <li><a href="<ww:url page="/secure/admin/user/GroupBrowser.jspa" atltoken="false"/>"><ww:text name="'admin.menu.usersandgroups.group.browser'" /></a></li>
            </ul>
            <h2><ww:text name="'admin.bulkeditgroups.title'"/></h2>
        </ui:param>
    </ui:soy>

    <page:applyDecorator id="groups-edit" name="auiform">
        <page:param name="action">BulkEditUserGroups.jspa</page:param>
        <page:param name="cssClass">top-label</page:param>

        <ww:if test="prunedUsersToAssign != null && prunedUsersToAssign/size > 0">
            <fieldset class="hidden parameters">
                <ww:iterator value="prunedUsersToAssign">
                    <input type="hidden" title="prunedUsersToAssign" class="list" value="<ww:property value="."/>"/>
                </ww:iterator>
            </fieldset>
            <div id="prunePanel" class="aui-message error hidden">
                <span class="aui-icon icon-error"></span>
                <p>
                    <ww:text name="'admin.bulkeditgroups.prune.erroneous.names'">
                        <ww:param name="'value0'"><a id="prune" href="#"></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                </p>
            </div>
        </ww:if>

        <p><ww:text name="'admin.bulkeditgroups.description'"/></p>
        <p><ww:text name="'admin.bulkeditgroups.description2'"/></p>
        <ul>
            <li>
                <ww:text name="'admin.bulkeditgroups.memberslist.description'"/>
            </li>
            <li>
                <ww:text name="'admin.bulkeditgroups.removing.description'">
                    <ww:param name="'value0'"><strong></ww:param>
                    <ww:param name="'value1'"></strong></ww:param>
                </ww:text>
            </li>
            <li>
                <ww:text name="'admin.bulkeditgroups.adding.description'">
                    <ww:param name="'value0'"><strong></ww:param>
                    <ww:param name="'value1'"></strong></ww:param>
                </ww:text>
            </li>
        </ul>
        <p>
            <ww:text name="'admin.bulkeditgroups.step.one'">
                <ww:param name="'value0'"><strong></ww:param>
                <ww:param name="'value1'"></strong></ww:param>
            </ww:text><br>
            <ww:text name="'admin.bulkeditgroups.step.two'">
                <ww:param name="'value0'"><strong></ww:param>
                <ww:param name="'value1'"></strong></ww:param>
            </ww:text>
        </p>

        <ww:if test="allVisibleGroups != null && allVisibleGroups/size > 0">
            <div class="aui-group bulk-edit-user-groups">
                <!-- Groups to pick from -->
                <div class="aui-item">
                    <div class="field-group">
                        <label for="selectedGroupsStr">
                            <ww:text name="'admin.bulkeditgroups.selected.x.of.y.groups'">
                                <ww:param name="'value0'"><ww:property value="selectedGroupsUserHasPermToSee/size"/></ww:param>
                                <ww:param name="'value1'"><ww:property value="allVisibleGroups/size"/></ww:param>
                            </ww:text>
                        </label>
                        <select id="selectedGroupsStr" name="selectedGroupsStr" class="select full-width-field" multiple size="10">
                            <ww:iterator value="allVisibleGroups">
                                <option <ww:if test="isGroupSelected(.) == 'true'">selected</ww:if> value="<ww:property value="name" />"><ww:property value="name" /></option>
                            </ww:iterator>
                        </select>
                        <div id="groupRefreshPanel" class="aui-message info hidden">
                            <span class="aui-icon icon-info"></span>
                            <p>
                                <ww:text name="'admin.bulkeditgroups.please.refresh'">
                                    <ww:param name="'value0'"><a id="refresh-dependant-fields" href="#"></ww:param>
                                    <ww:param name="'value1'"></a></ww:param>
                                </ww:text>
                            </p>
                        </div>
                    </div>
                </div>
                <!-- List of all members in the selected groups - selected ones will be removed -->
                <div class="aui-item">
                    <div class="field-group">
                        <label for="usersToUnassign">
                            <ww:if test="membersList/size == 1">
                                <ww:text name="'admin.bulkeditgroups.n.group.members'">
                                    <ww:param name="'value0'"><ww:property value="assignedUsersCount"/></ww:param>
                                </ww:text>
                            </ww:if>
                            <ww:else>
                                <ww:text name="'admin.bulkeditgroups.group.members'"/>
                            </ww:else>
                        </label>
                        <ww:if test="assignedUsersCount > 0">
                            <select id="usersToUnassign" name="usersToUnassign" class="select full-width-field" multiple size="10">
                                <ww:iterator value="membersList">
                                    <optgroup label="<ww:property value="./name"/>">
                                        <ww:iterator value="./childOptions">
                                            <option value="<ww:property value="optionValue(.)"/>">
                                                <ww:property value="./name"/>
                                            </option>
                                        </ww:iterator>
                                    </optgroup>
                                </ww:iterator>
                            </select>
                            <ww:if test="tooManyUsersListed == 'true'">
                                <aui:component template="auimessage.jsp" theme="'aui'">
                                    <aui:param name="'messageType'">warning</aui:param>
                                    <aui:param name="'messageHtml'">
                                        <p>
                                            <ww:text name="'admin.bulkeditgroups.warn.too.many.users.for.groups'">
                                                <ww:param name="'value0'"><ww:property value="prettyPrintOverloadedGroups"/></ww:param>
                                                <ww:param name="'value1'"><ww:property value="maxUsersDisplayedPerGroup"/></ww:param>
                                            </ww:text>
                                        </p>
                                    </aui:param>
                                </aui:component>
                            </ww:if>
                        </ww:if>
                        <ww:else>
                            <aui:component template="auimessage.jsp" theme="'aui'">
                                <aui:param name="'messageType'">info</aui:param>
                                <aui:param name="'messageHtml'">
                                    <p><ww:text name="'admin.bulkeditgroups.no.users.in.selection'"/></p>
                                </aui:param>
                            </aui:component>
                        </ww:else>
                    </div>
                    <input class="aui-button" id="unassign-users-from-selected-groups" name="unassign" type="submit" value="Remove selected users">
                </div>
                <!-- Multi-user picker for users to add to selected groups -->
                <div class="aui-item">
                    <div class="field-group">
                        <label for="usersToAssignStr"><ww:text name="'admin.bulkeditgroups.add.group.members'"/></label>
                        <ui:component name="'usersToAssignStr'" value="usersToAssignStr" template="multiuserpicker.jsp">
                            <ui:param name="'cssClass'" value="'textarea'"/>
                            <ui:param name="'nolabel'" value="'true'"/>
                            <ui:param name="'style'" value="''"/>
                        </ui:component>
                        <fieldset class="hidden parameters">
                            <ww:if test="hasPermission('pickusers') == true">
                                <input type="hidden" title="currentUserCanBrowseUsers" value="true">
                            </ww:if>
                            <ww:else>
                                <input type="hidden" title="currentUserCanBrowseUsers" value="false">
                            </ww:else>
                        </fieldset>
                        <div class="description"><ww:text name="'admin.project.people.find.users'" /></div>
                    </div>
                    <input class="aui-button" id="add-users-to-selected-groups" name="assign" type="submit" value="Add selected users">
                </div>
            </div>
        </ww:if>
        <ww:else>
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'admin.bulkeditgroups.no.groups'"/></p>
                </aui:param>
            </aui:component>
        </ww:else>
    </page:applyDecorator>

    <script>

        AJS.$(function() {
            var $groupsSelectList = AJS.$('#selectedGroupsStr');
            var selectedGroups = function(){ return $groupsSelectList.val(); };
            var initialSelectedGroups = selectedGroups();

            $groupsSelectList.change(function() {
                var currentlySelectedGroups = selectedGroups();
                var isGroupSelectionDifferent = (function() {
                    var diff = _.difference(currentlySelectedGroups, initialSelectedGroups);
                    return diff.length === 0;
                })();
                AJS.$('#groupRefreshPanel').toggleClass('hidden', isGroupSelectionDifferent);
            }).change();

            //
            AJS.$('#refresh-dependant-fields').click(function(){
                AJS.$('#groups-edit').submit();
            });

            var usersToAdd = AJS.params['prunedUsersToAssign'];
            if (usersToAdd && usersToAdd.length) {
                AJS.$('#prune').click(function(e) {
                    e.preventDefault();
                    var usersToRemove = [];
                    AJS.$('#usersToAssignStr').val(usersToAdd.join(", "));
                    AJS.$('#usersToAssignMultiSelect option').each(function(){
                        var $this = AJS.$(this);
                        if (AJS.$.inArray($this.val(), usersToAdd) < 0) {
                            var descriptor = new AJS.ItemDescriptor({
                                value: $this.val(),
                                label: $this.text()
                            });
                            usersToRemove.push(descriptor);
                        }
                    });
                    AJS.$.each(usersToRemove, function() {
                        AJS.$('#usersToAssignMultiSelect').trigger('removeOption', this);
                    });
                    AJS.$('#add-users-to-selected-groups').click();
                });
                AJS.$('#prunePanel').removeClass('hidden');
            }

        });
    </script>

</body>
</html>
