<%@ taglib uri="webwork" prefix="ww" %>

<ww:if test="/json == true">
    <%-- if they ask for the JSON version then give it to them --%>
    <% response.setContentType("application/json"); %>
    [
    <ww:iterator value="/historyItems" status="'status'">
        {
            "text" : "<ww:property value="./issueKey"/> - <ww:property value="/jsonEscape(./shortSummary)" escape="false"/>",
            "url" : "<%= request.getContextPath()%>/browse/<ww:property value="./issueKey"/>"
        }<ww:if test="@status/last == false">,</ww:if>
    </ww:iterator>
    ]
</ww:if>
<ww:else>
    <%-- In the spirit of progressive enhancement (what a crock! ;p), give them a landing page  --%>
    <ww:if test="/historyItems/empty == true">
        <p><ww:text name="'popups.recenthistory.noissues'"/></p>
    </ww:if>
    <ww:else>
        <table id="recent_history_list" class="grid" width="100%">
            <ww:iterator value="/historyItems" status="'status'">
                <tr class="<ww:if test="@status/odd == true">rowNormal</ww:if><ww:else>rowAlternate</ww:else> rowSelectable">
                    <td width="1%" nowrap>
                        <a href="<%= request.getContextPath()%>/browse/<ww:property value="./issueKey"/>" title="<ww:property value="./shortSummary"/>"><ww:property value="./issueKey"/></a>
                    </td>
                    <td>
                        <a href="<%= request.getContextPath()%>/browse/<ww:property value="./issueKey"/>" title="<ww:property value="./shortSummary"/>"><ww:property value="./shortSummary"/></a>
                    </td>
                </tr>
            </ww:iterator>
        </table>
    </ww:else>
</ww:else>
