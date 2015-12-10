<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<table class="aui aui-table-rowhover" id="summary_table">
    <thead>
        <tr>
            <th><ww:text name="'bulk.migrate.overview.project'"/></th>
            <th><ww:text name="'bulk.migrate.overview.issuetype'"/></th>
            <th><ww:text name="'bulk.migrate.overview.affected'"/></th>
        </tr>
    </thead>
    <tbody>
    <ww:iterator value="/multiBulkMoveBean/issuesInContext" status="'status'">
        <tr>
            <td>
                <ww:property value="./key/project/string('name')" /><br />
                <span class="smallGrey"><ww:property value="./key/project/string('description')" /></span>
            </td>
            <td>
                <ul class="imagebacked">
                    <li>
                        <img class="icon jira-icon-image" src="<ww:url value="./key/issueTypeObject/iconUrl" atltoken="false" />" alt="" />
                        <ww:property value="./key/issueTypeObject/nameTranslation" /><br />
                        <span class="smallGrey"><ww:property value="./key/issueTypeObject/descTranslation" /></span>
                    </li>
                </ul>
            </td>
            <td>
                <ww:property value="./value/size()" />
            </td>
        </tr>
    </ww:iterator>
    </tbody>
</table>
