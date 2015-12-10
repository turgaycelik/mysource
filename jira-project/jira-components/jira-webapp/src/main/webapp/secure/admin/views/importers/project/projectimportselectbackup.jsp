<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.project.import.select.backup.title'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/import_export_section"/>
    <meta name="admin.active.tab" content="project_import"/>
</head>
<body>
<page:applyDecorator id="project-import" name="auiform">
    <page:param name="action">ProjectImportSelectBackup.jspa</page:param>
    <page:param name="submitButtonName">Add</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.forms.next'"/></page:param>
    <page:param name="helpURL">restore_project</page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'admin.project.import.select.backup.title'"/></aui:param>
        <aui:param name="'helpURL'">restore_project</aui:param>
    </aui:component>

    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'admin.project.import.select.backup.desc.overview'" /></p>
        </aui:param>
    </aui:component>

    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'messageHtml'">
            <p>
                <ww:text name="'admin.project.import.select.backup.desc.test.first'">
                    <ww:param name="'value0'"><a href="<ww:property value="/docsLink"/>"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </p>
            <p>
                <ww:text name="'admin.project.import.select.backup.desc.same.version'">
                    <ww:param name="'value0'"><ww:property value="/version"/></ww:param>
                </ww:text>
            </p>
            <p>
                <ww:text name="'admin.project.import.select.backup.desc.process.availability'" />
            </p>
            <p>
                <ww:text name="'admin.project.import.select.backup.desc.backup.first'">
                    <ww:param name="'value0'"><a href="<%=request.getContextPath()%>/secure/admin/XmlBackup!default.jspa"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </p>
        </aui:param>
    </aui:component>

    <ww:if test="/showResumeLinkStep2 == true">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <p>
                    <ww:text name="'admin.project.import.select.backup.resume.step2'">
                        <ww:param name="'value0'"><a href='<%=request.getContextPath()%>/secure/admin/ProjectImportSelectProject!default.jspa'></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>
    </ww:if>
    <ww:if test="/showResumeLinkStep3 == true">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <p>
                    <ww:text name="'admin.project.import.select.backup.resume.step3'">
                        <ww:param name="'value0'"><ww:property value="/selectedProjectName"/></ww:param>
                        <ww:param name="'value1'"><a href='<%=request.getContextPath()%>/secure/admin/ProjectImportSummary!reMapAndValidate.jspa'></ww:param>
                        <ww:param name="'value2'"></a></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>
    </ww:if>

    <page:applyDecorator name="auifieldgroup">
        <page:param name="description"><ww:text name="'admin.project.import.select.backup.filename.desc'"/> <ww:property value="/defaultImportPath"/></page:param>
        <aui:textfield label="text('admin.project.import.select.backup.filename.label')" name="'backupXmlPath'" mandatory="'true'" theme="'aui'" />
    </page:applyDecorator>

    <ww:if test="/defaultImportAttachmentsPath != null">
        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="'admin.project.import.select.backup.attachment.desc'"/></page:param>
            <aui:component template="formFieldValue.jsp" label="text('admin.project.import.select.backup.attachment.label')" name="'defaultImportAttachmentsPath'" theme="'aui'" />
        </page:applyDecorator>
    </ww:if>

</page:applyDecorator>
</body>
</html>
