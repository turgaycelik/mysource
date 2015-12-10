<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<!-- Only Global Admins or Project Admins of all projects associated with selected issues can disable bulk operation mail notifications -->
<aui:component template="auimessage.jsp" theme="'aui'">
    <aui:param name="'messageType'">warning</aui:param>
    <aui:param name="'messageHtml'">
        <p>
            <ww:if test="sendBulkNotification() == true">
                <ww:text name="'bulk.operation.send.mail.confirm.yes'" />
            </ww:if>
            <ww:else>
                <ww:text name="'bulk.operation.send.mail.confirm.no'">
                    <ww:param name="value0"><b></ww:param>
                    <ww:param name="value01"></b></ww:param>
                </ww:text>
            </ww:else>
        </p>
    </aui:param>
</aui:component>