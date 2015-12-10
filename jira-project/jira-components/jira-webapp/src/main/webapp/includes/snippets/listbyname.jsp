<%@ taglib uri="webwork" prefix="ww" %>
<%
// This is used to render Components & Versions on the ViewIssue action
//
// TODO: when ViewIssue.getComponents() and ViewIssue.getFixVersions() return value objects and not GenericValues, refactor this to not use GenericValue field accessors
%>
<ww:if test=". != null && size > 0">
    <ww:iterator status="'liststatus'">
        <ww:property value="string('name')" /><ww:if test="@liststatus/last == false">, </ww:if>
    </ww:iterator>
</ww:if>
<ww:else>
    <ww:text name="'common.words.none'"/>
</ww:else>
