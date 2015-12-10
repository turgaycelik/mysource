<%@ taglib uri="webwork" prefix="ww" %>
<ww:if test="/json == true">
    <%-- if they ask for JSON give it to them  --%>
    <% response.setContentType("application/json"); %>
    [
    <ww:if test="/favouriteFilters/empty() == true">
        {
            "text" : "<ww:text name="'common.filters.no.favourite.filters'"/>",
            "url" : ""
        },
    </ww:if>
    <ww:else>
        <ww:iterator value="/favouriteFilters">
        {
            "text" : "<ww:property value="/jsonEscape(./name)" escape="false"/>",
            "url" : "<%= request.getContextPath()%>/secure/IssueNavigator.jspa?mode=hide&requestId=<ww:property value="./id"/>"
        },
        </ww:iterator>
    </ww:else>
    <%-- we always have a link to manage filters in the list --%>
        {
            "groupmarker" : true,
            "text" : "<ww:text name="'portlet.savedfilters.manage'"/>",
            "url" : "<%= request.getContextPath()%>/secure/ManageFilters.jspa"
        }
    ]
</ww:if>
<ww:else>
    <%-- In the spirit of progressive enhancement (what a crock! ;p), give them a landing page  --%>
    <ww:if test="/favouriteFilters/empty == true">
        <p><ww:text name="'popups.savedfilters.nosavedfilters'"/>
            <a href="<%= request.getContextPath()%>/secure/ManageFilters.jspa"><ww:text name="'portlet.savedfilters.manage'"/></a>
        </p>
    </ww:if>
    <ww:else>
        <p><ww:text name="'popups.savedfilters.choose'">
            <ww:param name="'value0'"><a href="<%= request.getContextPath()%>/secure/ManageFilters.jspa"></ww:param>
            <ww:param name="'value1'"></a></ww:param>
        </ww:text></p>

        <table id="filter_list" class="grid" width="100%">
            <tr>
                <th nowrap><ww:text name="'popups.savedfilters.filtername'"/></th>
                <th nowrap><ww:text name="'popups.savedfilters.filterdesc'"/></th>
            </tr>
            <ww:iterator value="/favouriteFilters" status="'status'">
                <tr class="<ww:if test="@status/odd == true">rowNormal</ww:if><ww:else>rowAlternate</ww:else> rowSelectable">
                    <td nowrap width="1%">
                        <a href="<%= request.getContextPath()%>/secure/IssueNavigator.jspa?mode=hide&requestId=<ww:property value="./id"/>" title="<ww:property value="./name"/>"><ww:property value="./name"/></a> &nbsp;
                    </td>
                    <td>
                        <a href="<%= request.getContextPath()%>/secure/IssueNavigator.jspa?mode=hide&requestId=<ww:property value="./id"/>" title="<ww:property value="./name"/>"><ww:property value="./description"/></a> &nbsp;
                    </td>
                </tr>
            </ww:iterator>
        </table>
    </ww:else>
</ww:else>
