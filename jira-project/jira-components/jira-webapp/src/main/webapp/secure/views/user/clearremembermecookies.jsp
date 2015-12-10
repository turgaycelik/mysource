<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<page:applyDecorator id="clear-remember-me" name="auiform">
    <page:param name="action">ClearRememberMeCookies.jspa</page:param>

    <ww:if test="/remoteUser != null">
        <page:param name="submitButtonName">Clear</page:param>
        <page:param name="submitButtonText"><ww:text name="'rememberme.clear.cookies'"/></page:param>
    </ww:if>
    <page:param name="cancelLinkURI"><ww:url value="'/secure/ViewProfile.jspa'" atltoken="false"/></page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'rememberme.form.heading'"/></aui:param>
    </aui:component>

    <ww:if test="/remoteUser == null">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p>
                    <ww:text name="'rememberme.must.log.in'">
                        <ww:param name="param0"><a href="<%=request.getContextPath()%>/login.jsp?os_destination=%2Fsecure%2FViewProfile.jspa"></ww:param>
                        <ww:param name="param1"></a></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>
    </ww:if>
    <ww:else>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'rememberme.instruction.1'"/></p>
                <p><ww:text name="'rememberme.instruction.2'"/></p>
            </aui:param>
        </aui:component>
    </ww:else>

</page:applyDecorator>