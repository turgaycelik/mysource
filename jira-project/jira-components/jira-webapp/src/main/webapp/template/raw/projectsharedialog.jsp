<%@ taglib uri="webwork" prefix="ww" %>

<ww:property value="parameters['projects']">
    <ww:if test="empty == false">
        <div id="project-share-info" class="shared-item-target">
            <div class="shared-item-content">
                <h3><ww:property value="parameters['title']"/>:</h3>
                <ul class="shared-project-list">
                    <ww:iterator value=".">
                    <li><a class="shared-project-name" href="<ww:url value="'/plugins/servlet/project-config/'+ key" atltoken="false" />"><img class="shared-project-icon" width="16" height="16" alt="" src="<ww:url atltoken="false" value="'/secure/projectavatar'"><ww:param name="'pid'" value="id"/><ww:param name="'avatarId'" value="avatar/avatarId"/></ww:url>"><ww:property value="name"/></a></li>
                    </ww:iterator>
                </ul>
            </div>
        </div>
    </ww:if>
</ww:property>