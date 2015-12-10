<%@ page import="com.atlassian.jira.ComponentManager"%>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <ww:if test="/issueValid == true">
        <title>[#<ww:property value="/issueObject/key" />] <ww:property value="/issueObject/summary" /></title>
        <meta name="decorator" content="issueaction" />
        <%
            KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
            keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);

            request.setAttribute("contextPath", request.getContextPath());
        %>
        <link rel="index" href="<ww:url value="/issuePath" atltoken="false" />" />
    </ww:if>
    <ww:else>
        <title><ww:text name="'common.words.error'"/></title>
        <meta name="decorator" content="message" />
    </ww:else>
</head>
<body>
<ww:if test="/issueValid == true">
    <page:applyDecorator name="auiform" id="manage-attachments">
        <ww:if test="/inlineDialogMode == true">
            <page:param name="cancelLinkText"><ww:text name="'common.words.close'"/></page:param>
            <page:param name="cancelLinkURI"><ww:url value="/issuePath" atltoken="false" /></page:param>
        </ww:if>
        <ww:else>
            <div class="aui-toolbar2">
                <div class="aui-toolbar2-inner">
                    <div class="aui-toolbar2-primary">
                        <div class="aui-buttons">
                            <a id="back-lnk" class="aui-button" href="<%= request.getContextPath() %>/browse/<ww:property value="/issueObject/key" />"><span class="icon icon-back"><span><ww:text name="'opsbar.back.to.issue'"/></span></span><ww:text name="'opsbar.back.to.issue'"/></a>
                        </div>
                        <ww:if test="/attachable == true || /screenshotAttachable == true || /zipSupport == true">
                            <div class="aui-buttons">
                                <ww:if test="/attachable == true">
                                    <a id="attach-more-files-link" class="aui-button" href="<ww:url page="AttachFile!default.jspa"><ww:param name="'id'" value="/issueObject/id" /><ww:param name="'returnUrl'" value="'ManageAttachments.jspa?id=' + /issueObject/id" /></ww:url>"><ww:text name="'manageattachments.attach.more.files'"/></a>
                                </ww:if>
                                <ww:if test="/zipSupport == true">
                                    <a id="aszipbutton" class="aui-button" href="<ww:property value="@contextPath"/>/secure/attachmentzip/<ww:property value="/issueObject/id"/>.zip" title="<ww:text name="'common.concepts.attachments.as.a.zip'"/>"><ww:text name="'common.concepts.attachments.as.a.zip.short'"/></a>
                                </ww:if>
                                <ww:if test="/screenshotAttachable == true">
                                    <a class="aui-button issueaction-attach-screenshot" href="<ww:url value="'AttachScreenshot!default.jspa'"  >
                                        <ww:param name="'id'" value="/issueObject/id" />
                                        </ww:url>">
                                        <ww:text name="'manageattachments.attach.another.screenshot'"/>
                                    </a>
                                </ww:if>
                            </div>
                        </ww:if>
                    </div>
                </div>
            </div>
        </ww:else>

        <aui:component template="issueFormHeading.jsp" theme="'aui/dialog'">
            <aui:param name="'title'"><ww:text name="'manageattachments.title'"/></aui:param>
            <aui:param name="'issueKey'"><ww:property value="/issueObject/key" escape="false"/></aui:param>
            <aui:param name="'issueSummary'"><ww:property value="/issueObject/summary" escape="false"/></aui:param>
            <aui:param name="'cameFromSelf'" value="/cameFromIssue"/>
            <aui:param name="'cameFromParent'" value="/cameFromParent"/>
        </aui:component>

        <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
            <aui:param name="'messageHtml'"><ww:text name="'manageattachments.description'"/></aui:param>
        </aui:component>

        <ww:property value="/issueObject">
            <ww:if test="attachments != null && attachments/empty == false">
                <table id="issue-attachments-table" class="aui" border="0">
                    <thead>
                        <tr>
                            <th>&nbsp;</th>
                            <th>&nbsp;</th>
                            <th><ww:text name="'manageattachments.file.name'"/></th>
                            <th><ww:text name="'manageattachments.size'"/></th>
                            <th><ww:text name="'manageattachments.mime.type'"/></th>
                            <th><ww:text name="'manageattachments.date.attached'"/></th>
                            <th><ww:text name="'manageattachments.author'"/></th>
                            <th>&nbsp;</th>
                        </tr>
                    </thead>
                    <tbody>
                        <ww:bean name="'com.atlassian.core.util.FileSize'" id="sizeFormatter" />
                        <ww:iterator value="attachments" status="'status'">
                            <tr>
                                <td><ww:property value="@status/count"/></td>
                                <td><ww:fragment template="attachment-icon.jsp"><ww:param name="'filename'" value="filename"/><ww:param name="'mimetype'" value="mimetype"/></ww:fragment></td>
                                <td><a href="<ww:property value="@contextPath"/>/secure/attachment/<ww:property value="id" />/<ww:property value="urlEncoded(filename)" />"><ww:property value="filename" /></a></td>
                                <td><ww:property value="@sizeFormatter/format(filesize)"/></td>
                                <td><ww:property value="mimetype"/></td>
                                <td class="attachment-date"><time datetime="<ww:property value="/iso8601Formatter/format(created)"/>"><ww:property value="/dateTimeFormatter/format(created)"/></time></td>
                                <td><ww:if test="authorObject != null"><ww:property value="authorObject/displayName"/></ww:if><ww:else><span title="<ww:text name="'admin.viewuser.user.does.not.exist.title'" />"><ww:property value="authorKey"/></span></ww:else></td>
                                <td class="icon"><ww:if test="/hasDeleteAttachmentPermission(./id) == true"> <a title="<ww:text name="'attachment.delete.tooltip'"/>" href="<ww:url page="/secure/DeleteAttachment!default.jspa"><ww:param name="'id'" value="/issueObject/id" /><ww:param name="'deleteAttachmentId'" value="./id" /></ww:url>" id="del_<ww:property value="./id" />" class="icon icon-delete delete"><span><ww:text name="'common.words.delete'"/></span></a></ww:if></td>
                            </tr>
                        </ww:iterator>
                    </tbody>
                </table>
            </ww:if>
            <ww:else>
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'messageHtml'">
                        <p><ww:text name="'manageattachments.no.attachments.notification'"/></p>
                    </aui:param>
                </aui:component>
            </ww:else>
        </ww:property>
    </page:applyDecorator>
</ww:if>
<ww:else>
    <div class="form-body">
        <header>
            <h1><ww:text name="'common.words.error'"/></h1>
        </header>
        <%@ include file="/includes/issue/generic-errors.jsp" %>
    </div>
</ww:else>
</body>
</html>
