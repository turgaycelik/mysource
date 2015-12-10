<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title><ww:property value="/title"/></title>
</head>

<body>

<page:applyDecorator name="jirapanel">
    <page:param name="buttons">
        <a id="back-link" href="<%= request.getContextPath() %>/secure/admin/ProjectImportSummary!default.jspa" ><ww:text name="'admin.common.words.back'"/></a>
    </page:param>
    <page:param name="title"><ww:property value="/title"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">restore_project</page:param>
    <page:param name="description">
        <p><ww:property value="/description" escape="false"/></p>
        <p><ww:property value="/userCountLimitMessage" escape="false"/></p>
        <ul class="optionslist">
            <li><a href='<ww:property value="/actionName" />!xmlExport.jspa'><ww:text name="'admin.project.import.users.xml.export'"/></a></li>
        </ul>
    </page:param>

    <table id="usersdonotexist" class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th><ww:text name="'common.words.username'"/></th>
                <th><ww:text name="'common.words.fullname'"/></th>
                <th><ww:text name="'common.words.email'"/></th>
            </tr>
        </thead>
        <tbody>
            <ww:iterator value="/limitedUsers">
                <tr>
                    <td><ww:property value="./name"/></td>
                    <td><ww:property value="./fullname"/></td>
                    <td><ww:property value="./email"/></td>
                </tr>
            </ww:iterator>
        </tbody>
    </table>

</page:applyDecorator>

</body>
</html>
