<%@ taglib uri="webwork" prefix="ww" %>
<ww:bean name="'com.atlassian.core.util.StringUtils'" id="stringUtils" /> <%-- this is used by issuedisplayer --%>
<ww:bean name="com.google.common.collect.Iterables" id="iterables" />
<table class="aui">
    <ww:iterator value=".">
        <ww:if test="/selectMode == 'multiple'">
            <tr class="issue-picker-row" data-row-for="<ww:property value="key" />">
                <td data-cell-type="checkbox" class="nowrap"><input type="checkbox" name="issuekey" value="<ww:property value="key" />" onclick="processCBClick(event, this);"></td>
        </ww:if>
        <ww:else>
            <tr onClick="populateForm('<ww:property value="key" />')" class="issue-picker-row" data-row-for="<ww:property value="key" />">
        </ww:else>
            <td data-cell-type="issue-key" class="nowrap">
                <a data-label="<ww:property value="key" /> - <ww:property value="summary" />" rel="<ww:property value="key" />" href="#" title="<ww:property value="summary" />" onClick="populateForm('<ww:property value="key" />')"><ww:property value="key" /></a>
            </td>
            <td><a href="#" title="<ww:property value="summary" />" onClick="populateForm('<ww:property value="key" />')"><ww:property value="@stringUtils/crop(summary, 80, '...')" /></td>
            <td class="nowrap cell-type-icon"><%@ include file="/includes/icons/priority.jsp" %></td>
            <td class="nowrap cell-type-icon"><%@ include file="/includes/icons/status.jsp" %></td>
        </tr>
    </ww:iterator>
</table>
<ww:if test="/singleSelectOnly == 'false'">
    <ww:if test="/selectMode == 'multiple'">
        <a class="aui-button" href="#" onClick="populateFormMultiple(this)"><ww:text name="'issuedisplayer.select.issues'" /></a>
    </ww:if>
</ww:if>