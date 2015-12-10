<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<table class="aui" id="issue-types-table">
    <thead>
        <tr>
            <th>
                <ww:text name="'common.words.name'"/>
            </th>
            <ww:if test="typeEnabled == true">
                <th>
                    <ww:text name="'admin.common.words.type'"/>
                </th>
            </ww:if>
            <ww:if test="/schemes/size() > 1">
                <th>
                    <ww:text name="'admin.issuesettings.issuetypes.related.schemes'"/>
                </th>
            </ww:if>
            <th>
                <ww:text name="'common.words.operations'"/>
            </th>
        </tr>
    </thead>
    <tbody>
    <ww:iterator value="allOptions" status="'status'">
    <tr>
        <td class="cell-type-key">
            <ww:if test="../iconEnabled == true">
                <ww:component name="../fieldId" template="constanticon.jsp">
                  <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                  <ww:param name="'iconurl'" value="iconUrl" />
                  <ww:param name="'alt'"><ww:property value="name" /></ww:param>
                </ww:component>
            </ww:if>
            <strong data-issue-type-field="name"><ww:property value="name"/></strong>
            <div class="description secondary-text"><ww:property value="description"/></div>
        </td>
        <ww:if test="../typeEnabled == true">
        <td data-type="<ww:if test="subTask == true">subtask</ww:if><ww:else>issuetype</ww:else>">
            <ww:property value="type" />
        </td>
        </ww:if>
        <ww:if test="/schemes/size() > 1">
        <td>
            <ww:property value="/allRelatedSchemes(id)">
                <ww:if test="./size() > 0">
                    <ul>
                    <ww:iterator value="." status="'status'">
                        <li><a href="<ww:url value="'ManageIssueTypeSchemes!default.jspa'" ><ww:param name="'actionedSchemeId'" value="./id" /></ww:url>"><ww:property value="./name" /></a></li>
                    </ww:iterator>
                    </ul>
                </ww:if>
                <ww:else>
                    <ww:text name="'admin.issuesettings.issuetypes.no.associated.schemes'"/>
                </ww:else>
            </ww:property>

        </td>
        </ww:if>
        <td class="cell-type-collapsed">
            <ul class="operations-list">
                <li><a href="Edit<ww:property value="../actionPrefix" />!default.jspa?id=<ww:property value="id"/>"><ww:text name="'common.words.edit'"/></a></li>
            <%-- At least one constant  must exist - check that there is more than one constant --%>
            <ww:if test="../allOptions/size > 1">
                <li><a href="Delete<ww:property value="../actionPrefix" />!default.jspa?id=<ww:property value="id"/>"><ww:text name="'common.words.delete'"/></a></li>
            </ww:if>
            <ww:if test="/translatable == true">
                <li>
                    <ww:declare id="constantType"><ww:if test="subTask == true">subTask</ww:if><ww:else><ww:property value="fieldId" /></ww:else></ww:declare>
                    <a href="ViewTranslations!default.jspa?issueConstantType=<ww:property value="@constantType" />" id="translate_link"  >
                        <ww:text name="'admin.issuesettings.issuetypes.translate'"/>
                    </a>
                </li>
            </ww:if>
            </ul>
        </td>
    </tr>
    </ww:iterator>
    </tbody>
</table>