<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="webwork" prefix="iterator" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'movesubtask.title'"/>: <ww:property value="issue/string('key')" /></title>
</head>
<body>
    <page:applyDecorator name="bulkops-general">
        <page:param name="pageTitle"><ww:text name="'movesubtask.title'"/></page:param>
        <page:param name="navContentJsp">/secure/views/issue/movetaskpane.jsp</page:param>

            <ww:if test="subTaskTypes/size > 1">
                <page:applyDecorator name="jiraform">
                    <page:param name="title"><ww:text name="'movesubtask.title'"/>: <ww:property value="issue/string('key')" /></page:param>
                    <page:param name="description">
                        <ww:text name="'movesubtask.step1.desc'"/>
                    </page:param>
                    <page:param name="width">100%</page:param>
                    <page:param name="action">MoveSubTaskType.jspa</page:param>
                    <page:param name="autoSelectFirst">false</page:param>
                    <page:param name="cancelURI"><ww:url value="/issuePath" atltoken="false"/></page:param>
                    <page:param name="submitId">next_submit</page:param>
                    <page:param name="submitName"><ww:text name="'common.forms.next'"/> &gt;&gt;</page:param>

                    <ww:property value="/fieldHtml('issuetype')" escape="'false'" />

                    <ui:component name="'id'" template="hidden.jsp" />
                </page:applyDecorator>
            </ww:if>
<%-- This will only be displayed if there are no other possible sub-task issue types available for this project. This
 should not happen because in the previous screen we check for that and disable the option. But still people could hand
 craft the URL or an Administrator may remove an issue type from the scheme while someone is changing the sub-tasks
 issue type. --%>
            <ww:else>
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">warning</aui:param>
                    <aui:param name="'messageHtml'">
                        <p><ww:text name="'movesubtask.nosubtasktypes'"/></p>
                    </aui:param>
                </aui:component>
            </ww:else>

    </page:applyDecorator>
</body>
</html>
