<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.editusergroups.edit.user.groups'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>
<body>
    <page:applyDecorator id="user-edit-groups" name="auiform">
        <page:param name="action">EditUserGroups.jspa</page:param>
        <page:param name="cssClass">top-label</page:param>
        <page:param name="cancelLinkURI">UserBrowser.jspa</page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.editusergroups.edit.user.groups'"/></aui:param>
        </aui:component>

        <div id="userGroupPicker" class="aui-group">
            <div class="aui-item">
                <div class="field-group">
                    <label for="groupsToJoin"><ww:text name="'admin.editusergroups.available.groups'"/></label>
                    <ww:if test="/nonMemberGroups != null && /nonMemberGroups/size > 0">
                        <select id="groupsToJoin" name="groupsToJoin" class="select full-width-field" multiple size="10">
                            <ww:iterator value="/nonMemberGroups">
                                <option value="<ww:property value="." />"><ww:property value="."/></option>
                            </ww:iterator>
                        </select>
                        <p><input id="user-edit-groups-join" class="aui-button" name="join" type="submit" value="<ww:text name="'admin.editusergroups.join.groups'"/>"/></p>
                    </ww:if>
                    <ww:else>
                        <aui:component template="auimessage.jsp" theme="'aui'">
                            <aui:param name="'messageType'">info</aui:param>
                            <aui:param name="'messageHtml'"><ww:text name="'admin.editusergroups.user.is.a.member.of.all'"/></aui:param>
                        </aui:component>
                    </ww:else>
                </div>
            </div>
            <div class="aui-item">
                <div class="field-group">
                    <label for="groupsToLeave"><ww:text name="'admin.editusergroups.groups.currently.in'"/></label>
                    <ww:if test="memberGroups != null && memberGroups/size > 0">
                        <select id="groupsToLeave" name="groupsToLeave" class="select full-width-field" multiple size="10">
                            <ww:iterator value="memberGroups">
                                <option value="<ww:property value="." />"><ww:property value="."/></option>
                            </ww:iterator>
                        </select>
                        <p><input id="user-edit-groups-leave" class="aui-button" name="leave" type="submit" value="<ww:text name="'admin.editusergroups.leave.groups'"/>"/>
                    </ww:if>
                    <ww:else>
                        <aui:component template="auimessage.jsp" theme="'aui'">
                            <aui:param name="'messageType'">info</aui:param>
                            <aui:param name="'messageHtml'"><ww:text name="'admin.editusergroups.user.is.a.member.of.no.groups'"/></aui:param>
                        </aui:component>
                    </ww:else>
                </div>
            </div>
        </div>
        <aui:component name="'name'" template="hidden.jsp" theme="'aui'"/>
    </page:applyDecorator>
</body>
</html>
