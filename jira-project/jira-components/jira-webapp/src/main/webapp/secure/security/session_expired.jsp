<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'xsrf.error.session.expired.title'"/></title>
    <meta name="decorator" content="message" />
</head>
<body>
<%--here so func tests work and attach file dialog. DO NOT DELETE! --%>
<!-- SecurityTokenMissing -->
    <div class="form-body" id="xsrf-error">
        <header class="xsrf-session-expired">
                <h1><ww:text name="'xsrf.error.session.expired.title'"/></h1>
        </header>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p>
                    <ww:text name="'xsrf.info.session.expired.line.2'">
                        <ww:param name="'value0'"><ww:property value="/sessionTimeoutDuration"/></ww:param>
                        <ww:param name="'value0'"><ww:property value="/sessionTimeoutUnit"/></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'xsrf.info.session.expired.userememberme'"/></p>
            </aui:param>
        </aui:component>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageHtml'">
                <p>
                    <ww:text name="'xsrf.error.offending.action'"/>:
                    <ww:property value="/requestURL"/>
                </p>
                <h4><ww:text name="'xsrf.notlogin.captured.params'"/></h4>
                <ul class="item-details request-parameters">
                    <ww:iterator value="/requestParameters">
                        <ww:iterator value="./value">
                            <%--dont use the original atl_token--%>
                            <ww:if test="../key != 'atl_token' && ../key/toLowerCase()/contains('password') != true">
                                <li>
                                    <dl>
                                        <dt><ww:property value="../key"/></dt>
                                        <dd><ww:property value="."/></dd>
                                    </dl>
                                </li>
                            </ww:if>
                        </ww:iterator>
                    </ww:iterator>
                </ul>
                <p>
                    <ww:text name="'xsrf.login.text'">
                        <ww:if test="/hasRedirectUrl == true">
                            <ww:param name="'value0'"><a id="xsrf-login-link" href="<ww:property value="@contextpath"/>/login.jsp?os_destination=<ww:property value="/encodedRedirectUrl"/>"></ww:param>
                        </ww:if>
                        <ww:else>
                            <ww:param name="'value0'"><a id="xsrf-login-link" href="<ww:property value="@contextpath"/>/login.jsp"></ww:param>
                        </ww:else>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>
    </div>
</body>
</html>
