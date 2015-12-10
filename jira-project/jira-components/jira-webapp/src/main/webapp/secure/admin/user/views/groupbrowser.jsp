<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.menu.usersandgroups.group.browser'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="group_browser"/>
</head>
<body>
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'mainContent'">
            <h2><ww:text name="'admin.menu.usersandgroups.group.browser'"/></h2>
        </ui:param>
        <ui:param name="'actionsContent'">
            <div class="aui-buttons">
                <ww:if test="/hasGroupWritableDirectory == true">
                    <a class="aui-button"  id="bulk_edit_groups" href="BulkEditUserGroups!default.jspa"><ww:text name="'admin.bulkeditgroups.title'"/></a>
                    <ww:if test="/nestedGroupsEnabledForAnyDirectory == true">
                        <a class="aui-button"  id="edit_nested_groups" href="EditNestedGroups!default.jspa"><ww:text name="'admin.editnestedgroups.title'"/></a>
                    </ww:if>
                </ww:if>
            </div>
        </ui:param>
        <ui:param name="'helpContent'">
            <aui:component name="'groups'" template="help.jsp" theme="'aui'" />
        </ui:param>
    </ui:soy>
    <p><ww:text name="'admin.usersandgroups.group.browser.description.1'"/></p>
    <ww:if test="/hasGroupWritableDirectory == true">
        <p><ww:text name="'admin.usersandgroups.group.browser.description.2'"/></p>
        <p><ww:text name="'admin.usersandgroups.group.browser.description.3'"/></p>
    </ww:if>

    <div class="aui-group">
        <div class="aui-item">
            <page:applyDecorator id="groups-filter" name="auiform">
                <page:param name="action">GroupBrowser.jspa</page:param>
                <page:param name="submitButtonText"><ww:text name="'admin.usersandgroups.filter'"/></page:param>
                <page:param name="submitButtonName">filter</page:param>
                <page:param name="cancelLinkURI">GroupBrowser.jspa?nameFilter=</page:param>
                <page:param name="cancelLinkText"><ww:text name="'admin.usersandgroups.reset.filter'"/></page:param>
                <page:param name="cssClass">top-label filter-form</page:param>

                <aui:component template="formSubHeading.jsp" theme="'aui'">
                    <aui:param name="'text'"><ww:text name="'admin.usersandgroups.filter.group'"/></aui:param>
                </aui:component>

                <ww:property value="filter">
                    <div class="aui-group">
                        <div class="aui-item">
                            <aui:textfield label="text('admin.usersandgroups.name.contains')"  name="'nameFilter'" size="20" theme="'aui'">
                                <aui:param name="'cssClass'">full-width-field</aui:param>
                            </aui:textfield>
                        </div>
                        <div class="aui-item">
                            <aui:select label="text('admin.usersandgroups.groups.per.page')" name="'max'" list="/maxValues" listKey="'.'" listValue="'.'" theme="'aui'">
                                <aui:param name="'headerrow'" value="''" />
                                <aui:param name="'cssClass'">full-width-field</aui:param>
                            </aui:select>
                        </div>
                    </div>
                </ww:property>
            </page:applyDecorator>
        </div>
        <ww:if test="/hasGroupWritableDirectory == true">
            <%-- TODO: Restful table the Add Group form or put it in a dialog --%>
            <div class="aui-item">
                <form action="GroupBrowser!add.jspa" method="post" id="add-group" class="aui add-group top-label">
                    <ww:component name="'atl_token'" value="/xsrfToken" template="hidden.jsp"/>
                    <div class="form-body">
                        <h3><ww:text name="'admin.usersandgroups.add.group'"/></h3>
                        <div class="field-group">
                            <aui:textfield label="text('common.words.name')" name="'addName'" theme="'aui'">
                                <aui:param name="'cssClass'">full-width-field</aui:param>
                            </aui:textfield>
                        </div>
                    </div>
                    <div class="buttons-container form-footer">
                        <div class="buttons">
                            <input type="submit" class="aui-button" name="add_group" value="<ww:text name="'admin.usersandgroups.add.group'"/>">
                        </div>
                    </div>
                </form>
            </div>
        </ww:if>
    </div>
    <div class="aui-group count-pagination">
        <div class="results-count aui-item">
            <ww:text name="'admin.usersandgroups.displaying.x.to.y.of.z'">
                <ww:param name="'value0'"><span class="results-count-start"><ww:property value="niceStart" /></span></ww:param>
                <ww:param name="'value1'"><span class="results-count-end"><ww:property value="niceEnd" /></span></ww:param>
                <ww:param name="'value2'"><span class="results-count-total"><ww:property value="browsableItems/size" /></span></ww:param>
            </ww:text>
        </div>
        <ww:if test="pager/pages(/browsableItems)/size > 1">
            <div class="pagination aui-item">
                <ww:if test="filter/start > 0">
                    <a class="icon icon-previous" title="<ww:text name="'common.forms.previous'"/>" href="<ww:url page="GroupBrowser.jspa"><ww:param name="'start'" value="filter/previousStart" /><ww:param name="'max'" value="filter/max" /><ww:param name="'nameFilter'" value="filter/nameFilter"/></ww:url>"><span>&lt&lt; <ww:text name="'common.forms.previous'"/></span></a>
                </ww:if>
                <ww:property value = "pager/pages(/browsableItems)">
                    <ww:if test="size > 1">
                        <ww:iterator value="." status="'pagerStatus'">
                            <ww:if test="currentPage == true"><strong><ww:property value="pageNumber" /></strong></ww:if>
                            <ww:else>
                                <a href="<ww:url page="GroupBrowser.jspa"><ww:param name="'start'" value="start" /><ww:param name="'max'" value="filter/max" /><ww:param name="'nameFilter'" value="filter/nameFilter"/></ww:url>"><ww:property value="pageNumber" /></a>
                            </ww:else>
                        </ww:iterator>
                    </ww:if>
                </ww:property>
                <ww:if test="filter/end < browsableItems/size">
                    <a class="icon icon-next" title="<ww:text name="'common.forms.next'"/>" href="<ww:url page="GroupBrowser.jspa"><ww:param name="'start'" value="filter/nextStart" /><ww:param name="'max'" value="filter/max" /><ww:param name="'nameFilter'" value="filter/nameFilter"/></ww:url>"><span><ww:text name="'common.forms.next'"/> &gt;&gt;</span></a>
                </ww:if>
            </div>
        </ww:if>
    </div>
    <table id="group_browser_table" class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th width="40%">
                    <ww:text name="'admin.usersandgroups.group.name'"/>
                </th>
                <th width="10%">
                    <ww:text name="'admin.common.words.users'"/>
                </th>
                <th width="30%">
                    <ww:text name="'admin.menu.schemes.permission.schemes'"/>
                </th>
                <ww:if test="/hasGroupWritableDirectory == true">
                    <th width="20%">
                        <ww:text name="'common.words.operations'"/>
                    </th>
                </ww:if>
            </tr>
        </thead>
        <tbody>
            <ww:iterator value="currentPage" status="'status'">
                <tr>
                    <td>
                        <a href="<ww:url page="ViewGroup.jspa">
                        <ww:param name="'name'" value="name"/></ww:url>"><ww:property value="name"/></a>
                    </td>
                    <td>
                        <a href="<ww:url value="'UserBrowser.jspa'" ><ww:param name="'group'" value="name" /><ww:param name="'emailFilter'" value="''" /></ww:url>"><ww:property value="/usersForGroup(.)/size"/></a>
                    </td>
                    <td>
                        <ww:if test="/permissionSchemes(./name)/empty == false">
                            <ul>
                            <ww:iterator value="/permissionSchemes(./name)">
                               <li><a href="<%= request.getContextPath() %>/secure/admin/EditPermissions!default.jspa?schemeId=<ww:property value="./long('id')"/>"><ww:property value="./string('name')" /></a></li>
                            </ww:iterator>
                            </ul>
                        </ww:if>
                        <ww:else>
                            &nbsp;
                        </ww:else>
                    </td>
                    <ww:if test="/hasGroupWritableDirectory == true && /userAbleToDeleteGroup(name) == true">
                        <td>
                            <ul class="operations-list">
                                <li><a id="del_<ww:property value="name"/>" href="<ww:url value="'DeleteGroup!default.jspa'" ><ww:param name="'name'" value="name" /></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                                <li><a id="edit_members_of_<ww:property value="name"/>" href="<ww:url value="'BulkEditUserGroups!default.jspa'" ><ww:param name="'selectedGroupsStr'" value="name" /></ww:url>"><ww:text name="'admin.usersandgroups.edit.members'"/></a></li>
                            </ul>
                        </td>
                    </ww:if>
                </tr>
            </ww:iterator>
        </tbody>
    </table>
    <ww:if test="pager/pages(/browsableItems)/size > 1">
        <div class="aui-group count-pagination">
            <div class="pagination aui-item">
                <ww:if test="filter/start > 0">
                    <a class="icon icon-previous" title="<ww:text name="'common.forms.previous'"/>" href="<ww:url page="GroupBrowser.jspa"><ww:param name="'start'" value="filter/previousStart" /><ww:param name="'max'" value="filter/max" /><ww:param name="'nameFilter'" value="filter/nameFilter"/></ww:url>"><span>&lt;&lt; <ww:text name="'common.forms.previous'"/></span></a>
                </ww:if>
                <ww:property value = "pager/pages(/browsableItems)">
                    <ww:if test="size > 1">
                        <ww:iterator value="." status="'pagerStatus'">
                            <ww:if test="currentPage == true"><strong><ww:property value="pageNumber" /></strong></ww:if>
                            <ww:else>
                                <a href="<ww:url page="GroupBrowser.jspa"><ww:param name="'start'" value="start" /><ww:param name="'max'" value="filter/max" /><ww:param name="'nameFilter'" value="filter/nameFilter"/></ww:url>"><ww:property value="pageNumber" /></a>
                            </ww:else>
                        </ww:iterator>
                    </ww:if>
                </ww:property>
                <ww:if test="filter/end < browsableItems/size">
                    <a class="icon icon-next" title="<ww:text name="'common.forms.next'"/>" href="<ww:url page="GroupBrowser.jspa"><ww:param name="'start'" value="filter/nextStart" /><ww:param name="'max'" value="filter/max" /><ww:param name="'nameFilter'" value="filter/nameFilter"/></ww:url>"><span><ww:text name="'common.forms.next'"/> &gt;&gt;</span></a>
                </ww:if>
            </div>
        </div>
    </ww:if>
</body>
</html>
