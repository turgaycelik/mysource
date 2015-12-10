<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<!-- Only Global Admins or Project Admins of all projects associated with selected issues can disable bulk operation mail notifications -->
<ww:if test="/canDisableMailNotifications() == true && /bulkEditBean/hasMailServer == true">
    <div class="checkbox">
        <input class="checkbox" type="checkbox" id="sendBulkNotificationCB" name="sendBulkNotification" value="true" checked />
        <label for="sendBulkNotificationCB"><ww:text name="'bulk.operation.send.mail'"/></label>
        <div class="description"><ww:text name="'bulk.operation.send.mail.desc'"/></div>
    </div>
</ww:if>




