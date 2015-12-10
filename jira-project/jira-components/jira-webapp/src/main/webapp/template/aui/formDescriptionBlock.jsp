<%--

Required Parameters:
    * messageHtml                      - Unescaped text of the description (should be in <p> tags)

Notes:
    See http://confluence.atlassian.com/display/AUI/Forms for more information

--%>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<aui:component template="auimessage.jsp" theme="'aui'">
    <aui:param name="'messageType'">info</aui:param>
    <aui:param name="'messageHtml'">
        <ww:property value="parameters['messageHtml']" escape="false" />
    </aui:param>
</aui:component>