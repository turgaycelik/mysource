<%@ taglib uri="webwork" prefix="ww" %>
<table id="global_perms" class="aui aui-table-rowhover">
    <thead>
        <ww:if test="key == 23">
            <ww:if test="allowGlobalPerms == true">
            <tr>
                <th>
                    <ww:text name="'admin.globalpermissions.title'"/>
                </th>
                <th style="width: 240px;">
                    <ww:text name="'admin.common.words.users.groups'"/>
                </th>
            </tr>
            </ww:if>
        </ww:if>
        <ww:else>
            <tr>
                <th>
                    <ww:property value="project/string('name')" /> <ww:text name="'admin.common.words.permissions'"/>
                </th>
                <th style="width: 240px;">
                    <ww:text name="'admin.common.words.users.groups'"/>
                </th>
            </tr>
        </ww:else>
    </thead>
    <tbody>
        <ww:iterator value="globalPermTypes">
            <tr>
                <td>
                    <strong><ww:property value="text(value)"/></strong>
                    <div class="secondary-text">
                        <p><ww:property value="/description(key)" escape="false"/></p>
                        <%-- special case for 'USE' pemission --%>
                        <ww:if test="key == 'USE'">
                            <p>
                                <ww:text name="'admin.globalpermissions.use.note'">
                                    <ww:param name="'value0'"><strong></ww:param>
                                    <ww:param name="'value3'"></strong></ww:param>
                                </ww:text>
                            </p>
                        </ww:if>
                        <%-- special case for 'ADMINISTER' or 'SYSTEM_ADMIN' permissions --%>
                        <ww:if test="key == 'ADMINISTER' || key == 'SYSTEM_ADMIN'">
                            <p>
                                <ww:text name="'admin.globalpermissions.admins.note'">
                                    <ww:param name="'value0'"><strong></ww:param>
                                    <ww:param name="'value3'"></strong></ww:param>
                                </ww:text>
                            </p>
                        </ww:if>
                    </div>
                </td>
                <td>
                    <ww:property value="/permissionGroups(key)">
                        <ww:if test=". != null && size > 0">
                            <ul>
                                <ww:iterator value=".">
                                    <li>
                                        <ww:if test="group">
                                            <ww:property value="group" />
                                        </ww:if>
                                        <ww:else>
                                            <ww:text name="'admin.common.words.anyone'"/>
                                        </ww:else>
                                        <ul class="operations-list" style="display: block;">
                                            <li><a href="<%= request.getContextPath() %>/secure/admin/user/UserBrowser.jspa?group=<ww:property value="group" />"><ww:text name="'admin.globalpermissions.view.users'"/></a></li>
                                            <li><a id="del_<ww:property value="../key" />_<ww:property value="group" />" href="<ww:url page="GlobalPermissions.jspa">
                                                <ww:param name="'action'">confirm</ww:param>
                                                <ww:param name="'globalPermType'" value="../key" />
                                                <ww:param name="'groupName'" value="group"/><%-- if no group - then don't show it --%>
                                                <ww:param name="'pid'" value="pid"/>
                                                </ww:url>"><ww:text name="'common.words.delete'"/></a>
                                            </li>
                                        </ul>
                                    </li>
                                </ww:iterator>
                            </ul>
                        </ww:if>
                        <ww:else>
                            &nbsp;
                        </ww:else>
                    </ww:property>
                </td>
            </tr>
        </ww:iterator>
    </tbody>
</table>
