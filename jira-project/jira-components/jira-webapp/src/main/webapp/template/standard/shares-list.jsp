<%@ taglib uri="webwork" prefix="ww" %>

<ww:property value="parameters['sharesview']">
<ww:if test="./private == true">
    <ww:property value="parameters['privatemessage']">
    <ul class="shareList">
        <li class="private" title="<ww:text name="'common.sharing.shared.display.private.desc'"/>"><ww:property value="."/></li>
    </ul>
    </ww:property>
</ww:if>
<ww:else>
    <ww:property value="./sharePermissions">
        <ww:if test="./size > 2">
            <ul class="shareList" id="share_list_summary_<ww:property value="../id"/>" onclick="JIRA.Share.toggleElements('share_list_complete_<ww:property value="../id"/>', 'share_list_summary_<ww:property value="../id"/>')">
                <li class="public" title="<ww:property value="parameters['sharedmessage']"/>">
                    <ww:text name="'common.sharing.shared.share.count'">
                        <ww:param name="'value0'"><strong></ww:param>
                        <ww:param name="'value1'"><ww:property value="./size"/></ww:param>
                        <ww:param name="'value2'"></strong></ww:param>
                    </ww:text>
                    <span class="switch"><ww:text name="'common.concepts.show'"/></span>
                </li>
            </ul>
            <ul class="shareList" id="share_list_complete_<ww:property value="../id"/>" style="display: none;" onclick="JIRA.Share.toggleElements('share_list_summary_<ww:property value="../id"/>', 'share_list_complete_<ww:property value="../id"/>')">
        </ww:if>
        <ww:else>
            <ul class="shareList">
        </ww:else>
            <ww:iterator value=".">
                <li class="public" title="<ww:property value="../simpleDescription(.)" escape="false"/>">
                    <ww:property value="../shareView(.)" escape="false"/>
                </li>
            </ww:iterator>
            <ww:if test="./size > 2">
                <li><span class="switch"><ww:text name="'common.concepts.hide'"/></span></li>
            </ww:if>
            </ul>
    </ww:property>
</ww:else>
</ww:property>
