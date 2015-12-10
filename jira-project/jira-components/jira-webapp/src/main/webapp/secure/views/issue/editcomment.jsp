<%@ page import="com.atlassian.jira.util.JiraUtils"%>
<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<html>
<head>
    <ww:if test="issueValid == true">
        <title><ww:text name="'viewissue.comment.edit.title'"/></title>
        <meta name="decorator" content="issueaction" />
        <%
             KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
             keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
         %>
        <link rel="index" href="<ww:url value="/issuePath" atltoken="false" />" />
    </ww:if>
    <ww:else>
        <title><ww:text name="'common.words.error'"/></title>
        <meta name="decorator" content="message" />
    </ww:else>
</head>
<body>
<ww:if test="issueValid == true">
    <page:applyDecorator id="comment-edit" name="auiform">
        <page:param name="action">EditComment.jspa</page:param>
        <page:param name="showHint">true</page:param>
        <ww:property value="/hint('comment')">
            <ww:if test=". != null">
                <page:param name="hint"><ww:property value="./text" escape="false" /></page:param>
                <page:param name="hintTooltip"><ww:property value="./tooltip" escape="false" /></page:param>
            </ww:if>
        </ww:property>
        <page:param name="submitButtonName">Save</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.words.save'"/></page:param>
        <page:param name="cancelLinkURI"><ww:url value="/issuePath" atltoken="false" /></page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'viewissue.comment.edit.title'"/></aui:param>
        </aui:component>

        <aui:component name="'commentId'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'id'" template="hidden.jsp" theme="'aui'" />

        <page:applyDecorator name="auifieldset">
            <page:param name="legend"><ww:text name="'comment.details.legend'" /></page:param>

            <page:applyDecorator name="auifieldgroup">
                <aui:component id="'comment-author'" label="text('viewissue.comment.author')" name="'comment-author'" template="formFieldValue.jsp" theme="'aui'">
                    <aui:param name="'texthtml'"><jira:formatuser userKey="/commentAuthorKey" type="'profileLink'" id="'comment_summary'" /></aui:param>
                </aui:component>
            </page:applyDecorator>
            <page:applyDecorator name="auifieldgroup">
                <aui:component id="'comment-created'" label="text('viewissue.comment.created')" name="'comment-created'" template="formFieldValue.jsp" theme="'aui'">
                    <aui:param name="'texthtml'"><ww:property value="/dmyDateFormatter/format(/commentObject/created)" /></aui:param>
                </aui:component>
            </page:applyDecorator>
            <ww:if test="/commentObject/created/equals(/commentObject/updated) == false">
                <page:applyDecorator name="auifieldgroup">
                    <aui:component id="'comment-update-author'" label="text('viewissue.comment.update.author')" name="'comment-update-author'" template="formFieldValue.jsp" theme="'aui'">
                        <aui:param name="'texthtml'"><jira:formatuser userKey="/commentUpdateAuthorKey" type="'profileLink'" id="'comment_summary_updated'" /></aui:param>
                    </aui:component>
                </page:applyDecorator>
                <page:applyDecorator name="auifieldgroup">
                    <aui:component id="'comment-updated'" label="text('viewissue.comment.updated')" name="'comment-updated'" template="formFieldValue.jsp" theme="'aui'">
                        <aui:param name="'texthtml'"><ww:property value="/dmyDateFormatter/format(/commentObject/updated)" /></aui:param>
                    </aui:component>
                </page:applyDecorator>
            </ww:if>
            <ww:if test="/commentObject/level != null">
                <page:applyDecorator name="auifieldgroup">
                    <aui:component id="'comment-visibleby'" label="text('viewissue.comment.visibleby')" name="'comment-visibleby'" template="formFieldValue.jsp" theme="'aui'">
                        <aui:param name="'texthtml'"><ww:property value="/commentObject/level" /></aui:param>
                    </aui:component>
                </page:applyDecorator>
            </ww:if>
            <ww:property escape="'false'" value="/fieldScreenRendererLayoutItemForField(/field('comment'))/fieldLayoutItem/orderableField/editHtml(/fieldScreenRendererLayoutItemForField(/field('comment'))/fieldLayoutItem, /, /, /issueObject, /displayParams)" />
        </page:applyDecorator>

    </page:applyDecorator>
</ww:if>
<ww:else>
    <div class="form-body">
        <header>
            <h1><ww:text name="'common.words.error'"/></h1>
        </header>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'messageHtml'">
                <ww:if test="remoteUser == null">
                    <p><ww:text name="'addcomment.notloggedin'"/></p>
                    <p>
                        <ww:text name="'addcomment.mustfirstlogin'">
                            <ww:param name="'value0'"><jira:loginlink><ww:text name="'common.words.login'"/></jira:loginlink></ww:param>
                            <ww:param name="'value1'"></ww:param>
                        </ww:text>
                        <ww:if test="extUserManagement != true">
                            <% if (JiraUtils.isPublicMode()) { %>
                                <ww:text name="'noprojects.signup'">
                                    <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/Signup!default.jspa"></ww:param>
                                    <ww:param name="'value1'"></a></ww:param>
                                </ww:text>
                            <% } %>
                        </ww:if>
                    </p>
                </ww:if>
                <ww:else>
                    <ww:iterator value="flushedErrorMessages"><p><ww:property /></p></ww:iterator>
                    <p><ww:text name="'comment.add.error.permission'"/></p>
                </ww:else>
            </aui:param>
        </aui:component>
        <% if (TextUtils.stringSet(request.getParameter("comment"))) { %>
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'titleText'"><ww:text name="'comment.add.error.recover.comment'"/></aui:param>
                <aui:param name="'messageHtml'">
                    <jira:text2html><%= request.getParameter("comment") %></jira:text2html>
                </aui:param>
            </aui:component>
        <% } %>
    </div>
</ww:else>
</body>
</html>
