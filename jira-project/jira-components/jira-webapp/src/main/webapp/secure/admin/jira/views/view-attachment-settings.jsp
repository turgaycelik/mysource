<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.globalsettings.attachment.settings'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="attachments"/>
</head>
<body>
    <%-- error messages --%>
    <ww:if test="hasErrorMessages == 'true'">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'titleText'"><ww:text name="'admin.common.words.errors'"/></aui:param>
            <aui:param name="'messageHtml'">
                <ul>
                    <ww:iterator value="errorMessages">
                        <li><ww:property /></li>
                    </ww:iterator>
                </ul>
            </aui:param>
        </aui:component>
    </ww:if>
    <header class="aui-page-header">
        <div class="aui-page-header-inner">
            <div class="aui-page-header-main">
                <h2><ww:text name="'admin.menu.globalsettings.attachments'"/></h2>
            </div>
            <ww:if test="/systemAdministrator == true || applicationProperties/option('jira.option.allowattachments') == true">
            <div class="aui-page-header-actions">
                <div class="aui-buttons">
                    <a id="edit-attachments" class="aui-button trigger-dialog" href="EditAttachmentSettings!default.jspa">
                        <span class="icon jira-icon-edit"></span>
                        <ww:text name="'admin.common.phrases.edit.settings'"/>
                    </a>
                </div>
                <aui:component name="'attachments'" template="help.jsp" theme="'aui'" />
            </div>
            </ww:if>
        </div>
    </header>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'id'">AttachmentSettings</aui:param>
        <aui:param name="'contentHtml'">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <p>
                        <ww:text name="'admin.attachmentsettings.instruction1'">
                            <ww:param name="'value0'"><strong></ww:param>
                            <ww:param name="'value1'"><ww:text name="'admin.permissions.CREATE_ATTACHMENT'"/></ww:param>
                            <ww:param name="'value2'"></strong></ww:param>
                        </ww:text>
                    </p>
                </aui:param>
            </aui:component>
            <ww:if test="/systemAdministrator == false && applicationProperties/option('jira.option.allowattachments') == false">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">warning</aui:param>
                    <aui:param name="'messageHtml'">
                        <p><ww:text name="'admin.attachmentsettings.disabled.contact.sys.admin'">
                          <ww:param name="'value0'"> </ww:param>
                          <ww:param name="'value1'"> </ww:param>
                        </ww:text></p>
                    </aui:param>
                </aui:component>
            </ww:if>
            <table class="aui aui-table-rowhover" id="table-AttachmentSettings">
                <tbody>
                    <tr data-attachment-setting="allow-attachment">
                        <td width="40%" data-cell-type="label">
                            <strong><ww:text name="'admin.attachmentsettings.allow.attachments'"/></strong>
                        </td>
                        <td width="60%" data-cell-type="value">
                            <ww:if test="applicationProperties/option('jira.option.allowattachments') == true">
                                <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                            </ww:if>
                            <ww:else>
                                <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                            </ww:else>
                        </td>
                    </tr>
                    <tr data-attachment-setting="attachment-path">
                        <td data-cell-type="label">
                            <strong><ww:text name="'admin.attachmentsettings.attachment.path'"/></strong>
                            <div class="secondary-text">
                                <ww:text name="'admin.attachmentsettings.attachment.path.explanation'" />
                            </div>
                        </td>
                        <td data-cell-type="value">
                            <ww:property value="attachmentPath" />
                        </td>
                    </tr>
                    <tr data-attachment-setting="attachment-size">
                        <td data-cell-type="label">
                            <strong><ww:text name="'admin.attachmentsettings.attachment.size'"/></strong>
                            <div class="secondary-text">
                                <ww:text name="'admin.attachmentsettings.attachment.size.explanation'"/>
                            </div>
                        </td>
                        <td data-cell-type="value">
                            <ww:property value="prettyAttachmentSize"/>
                        </td>
                    </tr>
                    <tr data-attachment-setting="allow-thumbnails">
                        <td data-cell-type="label">
                            <strong><ww:text name="'admin.attachmentsettings.enable.thumbnails'"/></strong>
                            <div class="secondary-text">
                                <ww:text name="'admin.attachmentsettings.enable.thumbnails.explanation'"/>
                            </div>
                        </td>
                        <td data-cell-type="value">
                            <ww:if test="applicationProperties/option('jira.option.allowthumbnails') == true">
                                <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                            </ww:if>
                            <ww:else>
                                <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                            </ww:else>
                        </td>
                    </tr>
                    <tr data-attachment-setting="zipsupport">
                        <td data-cell-type="label">
                            <strong><ww:text name="'admin.attachmentsettings.enable.zipsupport'"/></strong>
                            <div class="secondary-text">
                                <ww:text name="'admin.attachmentsettings.enable.zip.support.explanation'"/>
                            </div>
                        </td>
                        <td data-cell-type="value">
                            <ww:if test="/zipSupport == true">
                                <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                            </ww:if>
                            <ww:else>
                                <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                            </ww:else>
                        </td>
                    </tr>
                </tbody>
            </table>
        </aui:param>
    </aui:component>
</body>
</html>
