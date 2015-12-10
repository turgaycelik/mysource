<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title>
        <ww:text name="'admin.deleteuser.delete.user.confirm.heading'">
            <ww:param name="'value0'"><ww:property value="name"/></ww:param>
        </ww:text>
    </title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>
<body>
    <ww:if test="user">
        <ww:if test="deleteable == 'true'">
            <page:applyDecorator id="delete_user_confirm" name="auiform">
                <page:param name="action">DeleteUser.jspa</page:param>
                <page:param name="submitButtonText"><ww:text name="'common.words.delete'"/></page:param>
                <page:param name="submitButtonName">Delete</page:param>
                <page:param name="cancelLinkURI"><ww:url value="'UserBrowser.jspa'" /></page:param>
                <page:param name="cssClass">top-label</page:param>

                <aui:component template="formHeading.jsp" theme="'aui'">
                    <aui:param name="'text'">
                        <ww:text name="'admin.deleteuser.delete.user.confirm.heading'">
                            <ww:param name="'value0'"><ww:property value="name" escape="false"/></ww:param>
                        </ww:text>
                     </aui:param>
                </aui:component>

                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">warning</aui:param>
                    <aui:param name="'messageHtml'">
                        <ww:if test="userState/inMultipleDirectories == 'true'">
                            <p>
                                <ww:text name="'admin.deleteuser.user.exists.in.multiple'">
                                    <ww:param name="'value0'"><ww:property value="name"/></ww:param>
                                </ww:text>
                            </p>
                        </ww:if>
                        <ww:else>
                            <p>
                                <ww:text name="'admin.deleteuser.users.may.be.deleted'">
                                    <ww:param name="'value0'"><ww:property value="name"/></ww:param>
                                </ww:text>
                            </p>
                            <ww:if test="componentsUserLeadsWarning/size() > 0">
                                <p><ww:text name="'admin.deleteuser.components.lead.desc'"/></p>
                            </ww:if>
                        </ww:else>
                    </aui:param>
                </aui:component>

                <jsp:include page="/secure/admin/user/views/assignedreported.jsp"/>

                <ui:component name="'name'" template="hidden.jsp" theme="'aui'"  />
                <ui:component name="'confirm'" value="'true'" template="hidden.jsp" theme="'aui'"  />
            </page:applyDecorator>
        </ww:if>
        <ww:else>
            <page:applyDecorator id="delete_user_confirm" name="auiform">
                <page:param name="action">DeleteUser.jspa</page:param>
                <page:param name="cancelLinkURI"><ww:url value="'UserBrowser.jspa'" /></page:param>
                <page:param name="cancelLinkText">Close</page:param>
                <page:param name="cssClass">top-label</page:param>

                <aui:component template="formHeading.jsp" theme="'aui'">
                    <aui:param name="'text'">
                        <ww:text name="'admin.deleteuser.delete.user.confirm.heading'">
                            <ww:param name="'value0'"><ww:property value="name" escape="false"/></ww:param>
                        </ww:text>
                    </aui:param>
                </aui:component>

                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">error</aui:param>
                    <aui:param name="'messageHtml'">
                        <p id="user-cannot-be-deleted"><ww:text name="'admin.deleteuser.cannot.be.deleted'"><ww:param name="'value0'"><ww:property value="name"/></ww:param></ww:text></p>
                        <ww:if test="/selfDestruct == 'true'">
                            <p id="user-delete-self-error"><ww:text name="'admin.errors.users.cannot.delete.currently.logged.in'"/></p>
                        </ww:if>
                        <ww:if test="/nonSysAdminAttemptingToDeleteSysAdmin == 'true'">
                            <p id="user-nonadmin-error"><ww:text name="'admin.errors.users.cannot.delete.due.to.sysadmin'"/></p>
                        </ww:if>
                    </aui:param>
                </aui:component>

                <jsp:include page="/secure/admin/user/views/assignedreported.jsp"/>
            </page:applyDecorator>
        </ww:else>
    </ww:if>
    <ww:else>
        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'">
                <ww:text name="'admin.deleteuser.delete.user.confirm.heading'">
                    <ww:param name="'value0'"><ww:property value="name" escape="false"/></ww:param>
                </ww:text>
            </aui:param>
        </aui:component>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.deleteuser.user.does.not.exist'">
                    <ww:param name="'value0'"><b><ww:property value="name" /></b></ww:param>
                    <ww:param name="'value1'"><a href="<ww:url page="UserBrowser.jspa"/>"></ww:param>
                    <ww:param name="'value2'"></a></ww:param>
                </ww:text></p>
            </aui:param>
        </aui:component>
    </ww:else>
</body>
</html>
