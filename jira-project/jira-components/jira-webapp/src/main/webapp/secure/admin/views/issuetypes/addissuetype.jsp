<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<ww:property value="/manageableOption" >
<html>
<head>
    <title>
        <ww:text name="'admin.issuesettings.issuetypes.add.dialog.title'"/>
    </title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/issue_types_section"/>
    <meta name="admin.active.tab" content="issue_types"/>
</head>
<body>

<page:applyDecorator id="add-issue-type-form" name="auiform">
    <page:param name="action"><ww:property value="/submitUrl"/></page:param>
    <page:param name="method">post</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.forms.add'"/></page:param>
    <page:param name="submitButtonName">Add</page:param>
    <page:param name="cancelLinkURI"><ww:property value="/cancelUrl"/></page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'">
            <ww:text name="'admin.issuesettings.issuetypes.add.dialog.title'"/>
        </aui:param>
    </aui:component>

    <ww:iterator value="/hiddenFields">
        <aui:component template="hidden.jsp" theme="'aui'" name="first()" value="second()"/>
    </ww:iterator>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield label="text('common.words.name')" name="'name'" mandatory="true" maxlength="60" theme="'aui'"/>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:textarea label="text('common.words.description')" name="'description'" rows="'4'" theme="'aui'"/>
    </page:applyDecorator>

    <ww:if test="typeEnabled == true">
        <page:applyDecorator name="auifieldset">
            <page:param name="type">group</page:param>
            <page:param name="legend"><ww:text name="'admin.issuesettings.issuetypes.type'"/></page:param>
            <aui:radio id="'issue-type-style'" label="text('admin.issuesettings.issuetypes.type')" theme="'aui'" name="'style'" list="typesList"
                       listKey="'id'" listValue="'name'" value="/style"/>
        </page:applyDecorator>
    </ww:if>

</page:applyDecorator>

</body>
</ww:property>
</html>