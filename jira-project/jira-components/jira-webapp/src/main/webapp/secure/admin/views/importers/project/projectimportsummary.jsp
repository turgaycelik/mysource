<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title>
        <ww:text name="'admin.project.import.summary.title'">
            <ww:param name="'value0'"><ww:property value="/projectName"/></ww:param>
        </ww:text>
    </title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/import_export_section"/>
    <meta name="admin.active.tab" content="project_import"/>
</head>
<body>

<page:applyDecorator name="jiraform">
    <page:param name="action">ProjectImportSummary.jspa</page:param>
    <page:param name="columns">1</page:param>
    <page:param name="cancelURI">ProjectImportSelectBackup!cancel.jspa</page:param>
    <page:param name="title"><ww:text name="'admin.project.import.summary.title'"><ww:param name="'value0'"><ww:property value="/projectName"/></ww:param></ww:text></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="autoSelectFirst">false</page:param>
    <page:param name="helpURL">restore_project</page:param>
    <page:param name="description">
        <p><ww:text name="'admin.project.import.summary.desc'"/></p>
    <ww:if test="/mappingResult != null && /canImport == true">
        <p><ww:text name="'admin.project.import.summary.johnson.desc'"/></p>
    </ww:if>
    <ww:if test="/mappingResult != null">
        <ul class="optionslist">
            <li>
                <ww:text name="'admin.project.import.summary.refresh.validation'">
                <ww:param name="'value0'"><a href="<%=request.getContextPath()%>/secure/admin/ProjectImportSummary!reMapAndValidate.jspa"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </li>
        </ul>
    </ww:if>
    </page:param>
    <ww:if test="/mappingResult != null">

        <page:param name="leftButtons">
            <input class="aui-button" id="prevButton" name="prevButton" title="<ww:property value="text('common.forms.previous')"/>" type="submit" value="<ww:property value="text('common.forms.previous')"/>"/>
        </page:param>

        <ww:if test="/canImport == true">
            <page:param name="submitId">import_submit</page:param>
            <page:param name="submitName"><ww:text name="'common.forms.import'"/></page:param>
        </ww:if>
        <ww:else>
            <page:param name="buttons">
                <input class="aui-button" id="refreshValidationButton" name="refreshValidationButton" title="<ww:property value="text('admin.project.import.summary.refresh.validation.button')"/>" type="submit" value="<ww:property value="text('admin.project.import.summary.refresh.validation.button')"/>"/>
            </page:param>
        </ww:else>

    </ww:if>
    <tr>
        <td class="jiraformbody" id="projectimport">

            <div id="summary">

                <div id="systemfields">
                    <h3 class="formtitle"><ww:text name="'admin.project.import.summary.system.fields'"/></h3>
                    <ww:if test="/mappingResult != null">
                        <ww:property value="/systemFieldsValidateMessages" id="fieldlist"/>
                        <jsp:include page="/includes/admin/importers/projectimportsummary_field_list.jsp" />
                    </ww:if>
                </div>

                <div id="customfields">
                    <h3 class="formtitle"><ww:text name="'admin.project.import.summary.custom.fields'"/></h3>
                    <ww:if test="/mappingResult != null && /customFieldsValidateMessages/size() == 0">
                        <ul>
                            <li class="unprocessed">
                                <ww:text name="'admin.project.import.summary.no.custom.fields'"/>
                            </li>
                        </ul>
                    </ww:if>
                    <ww:elseIf test="/mappingResult != null">
                        <ww:property value="/customFieldsValidateMessages" id="fieldlist"/>
                        <jsp:include page="/includes/admin/importers/projectimportsummary_field_list.jsp" />
                    </ww:elseIf>
                </div>

            </div>

        </td>
    </tr>
</page:applyDecorator>


</body>
</html>
