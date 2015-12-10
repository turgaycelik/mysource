<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.deletegroup.title'"><ww:param name="'value0'"><ww:property value="name" /></ww:param></ww:text></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="group_browser"/>
</head>
<body>
    <page:applyDecorator id="group-delete" name="auiform">
        <page:param name="action">DeleteGroup.jspa</page:param>
        <page:param name="cssClass">top-label</page:param>
        <page:param name="cancelLinkURI">GroupBrowser.jspa</page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.deletegroup.title'"><ww:param name="'value0'"><ww:property value="name" /></ww:param></ww:text></aui:param>
        </aui:component>

        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.deletegroup.users.must.be.removed.first'"/></p>
                <p><ww:text name="'admin.deletegroup.will.not.delete.users'"/></p>
            </aui:param>
        </aui:component>

        <ww:if test="hasSubscriptions == true">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">warning</aui:param>
                <aui:param name="'iconText'"><ww:text name="'admin.common.words.warning'"/></aui:param>
                <aui:param name="'titleText'"><ww:text name="'admin.deletegroup.subscriptions.header'"/></aui:param>
                <aui:param name="'messageHtml'">
                    <ul>
                        <ww:iterator value="subscriptions">
                            <li><ww:property /></li>
                        </ww:iterator>
                    </ul>
                    <p><ww:text name="'admin.deletegroup.subscriptions.footer'"/></p>
                </aui:param>
            </aui:component>
        </ww:if>
        <!-- Only add delete controls if user is allowed to delete the selected group -->
        <ww:if test="/hasAnyErrors() == false">
            <page:param name="submitButtonText"><ww:text name="'common.words.delete'"/></page:param>
            <page:param name="submitButtonName">Delete</page:param>

            <ww:if test="matchingCommentsAndWorklogsCount > 0">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">warning</aui:param>
                    <aui:param name="'iconText'"><ww:text name="'admin.common.words.warning'"/></aui:param>
                    <aui:param name="'titleText'"><ww:text name="'admin.deletegroup.invisible.comments'" /></aui:param>
                    <aui:param name="'messageHtml'">
                        <p><ww:text name="'admin.deletegroup.invisible.comments.invisible'" /></p>
                    </aui:param>
                </aui:component>

                <page:applyDecorator name="auifieldgroup">
                    <label for="swapGroup"><ww:text name="'admin.deletegroup.move.comments.to.be.seen.by'"/></label>
                    <select id="swapGroup" name="swapGroup" class="select">
                        <ww:iterator value="otherGroups">
                            <option value="<ww:property value="." />" selected>
                                <ww:property value="." />
                            </option>
                        </ww:iterator>
                    </select>
                </page:applyDecorator>
            </ww:if>
            <ui:component name="'name'" template="hidden.jsp"  theme="'aui'" />
            <ui:component name="'confirm'" value="'true'" template="hidden.jsp" theme="'aui'"  />
        </ww:if>

  </page:applyDecorator>
</body>
</html>
