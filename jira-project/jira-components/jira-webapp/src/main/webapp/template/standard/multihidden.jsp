<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib prefix="jira" uri="jiratags" %>

<%--<ww:generator id="hiddenFields" val="parameters['fields']" separator="','"/>--%>
<ww:property value="parameters['fields']">
    <ww:if test=".">
        <jira:split value="." separator="," />
        <ww:iterator value=".">
            <ui:component name="." template="hidden.jsp" theme="'single'" />
            <%--    <ui:textfield label="." name="." />--%>
        </ww:iterator>
    </ww:if>
</ww:property>

<%--<ww:generator id="hiddenFields" val="parameters['multifields']" separator="','"/>--%>
<ww:property value="parameters['multifields']">
    <ww:if test=".">
        <jira:split value="." separator=","/>
        <ww:iterator value=".">
            <ui:component name="." template="arrayhidden.jsp"/>
        </ww:iterator>
    </ww:if>
</ww:property>


