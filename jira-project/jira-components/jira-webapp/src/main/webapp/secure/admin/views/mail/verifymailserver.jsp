<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<div class="form-body">
    <aui:component id="'verifyServer'" template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'"><p><ww:text name="'admin.mailserver.verify.testing'" /></p></aui:param>
    </aui:component>
    <div id="verifyMessages">
        <ww:if test="/actionName == 'VerifySmtpServerConnection' || /actionName == 'VerifyPopServerConnection'" >
            <ww:if test="/hasAnyErrors == false">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">success</aui:param>
                    <aui:param name="'messageHtml'">
                        <p><ww:text name="'admin.mailserver.verify.success'"/></p>
                    </aui:param>
                </aui:component>
                <ww:if test="/anonymous == true">
                    <aui:component template="auimessage.jsp" theme="'aui'">
                        <aui:param name="'messageType'">warning</aui:param>
                        <aui:param name="'messageHtml'">
                            <p><ww:text name="'admin.mailserver.verify.anonymous'"><ww:param name="'value0'"><ww:property value="/serverName"/></ww:param></ww:text></p>
                        </aui:param>
                    </aui:component>
                </ww:if>
            </ww:if>
            <ww:else>
                <ww:if test="/hasErrorMessages == true && /hasErrors==false">
                    <aui:component template="auimessage.jsp" theme="'aui'">
                        <aui:param name="'messageType'">error</aui:param>
                        <aui:param name="'messageHtml'">
                            <p><ww:text name="'admin.mailserver.verify.failure.connection'"/></p>
                            <ul>
                                <ww:iterator id="error" value="/errorMessages">
                                    <li><ww:property value="."/></li>
                                </ww:iterator>
                            </ul>
                        </aui:param>
                    </aui:component>
                </ww:if>
            </ww:else>
        </ww:if>
    </div>
</div>
