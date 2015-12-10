<%@ taglib uri="webwork" prefix="ww" %>
<%
// This is used to render Components & Versions on the ViewIssue action
//
// TODO: when ViewIssue.getComponents() and ViewIssue.getFixVersions() return value objects and not GenericValues, refactor this to not use GenericValue field accessors
%>
<ww:if test=". != null && size > 0">
    <ww:iterator status="'liststatus'">
        <a href="<ww:url value="'/browse/' + /selectedProject/string('key') + '/' + $navfield + '/' + ./long('id')"/>" title="<ww:property value="./string('name')" /> <ww:property value="./string('description')" ><ww:if test=". && !./equals('')"> - <ww:property value="." /></ww:if></ww:property>"><ww:property value="string('name')"/></a><ww:if test="@liststatus/last == false">, </ww:if>
    </ww:iterator>
</ww:if>
<ww:else>
    <ww:text name="'common.words.none'"/>
</ww:else>
