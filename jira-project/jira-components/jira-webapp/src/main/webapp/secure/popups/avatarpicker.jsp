<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<dl id="avatar-prefs" class="hidden">
    <dt>pickerFieldID</dt>
    <dd><ww:property value="/avatarField"/></dd>
    <dt>title</dt>
    <dd><ww:text name="'avatarpicker.title'"/></dd>
    <dt>ownerId</dt>
    <dd><ww:property value="/ownerId"/></dd>
    <dt>noCustom</dt>
    <dd><ww:text name="'avatarpicker.nocustom'"/></dd>
    <dt>uploadAction</dt>
    <dd><%= request.getContextPath() %>/secure/project/AvatarPicker!upload.jspa</dd>
    <dt>avatarType</dt>
    <dd><ww:property value="/avatarType"/></dd>
    <dt>updateUrl</dt>
    <dd><ww:property value="/updateUrl"/></dd>
    <dt>defaultAvatarId</dt>
    <dd><ww:property value="/defaultAvatarId"/></dd>
    <dt>selectedId</dt>
    <dd><ww:property value="/selectedAvatar"/></dd>
</dl>

<div id="avatar-panel">

    <div class="panel" id="avatar-all">
        <h2><ww:text name="'avatarpicker.all'"/></h2>
    </div>

    <!-- first the built-in palette -->
    <div class="panel" id="avatar-builtin">
        <h2><ww:text name="'avatarpicker.builtin'"/></h2>
        <ul class="avatars">
            <ww:iterator value="/systemAvatars">
                <li class="avatar" data-avatarId="<ww:property value="./id" />" <ww:if test="./id == /avatar/id"> id="selected-avatar"</ww:if>>
                <img alt="<ww:text name="'avatarpicker.tooltip'"/>" height="48" id="<ww:property value="./id"/>" src="<ww:property value="/url(.)"/>" width="48" /></li>
            </ww:iterator>
        </ul>
    </div>

    <!--  then the icons uploaded for the project if any -->
    <div class="panel" id="avatar-uploaded">
        <h2><ww:text name="'avatarpicker.custom'"/></h2>
        <ul class="avatars">
            <ww:iterator value="/uploadedAvatars">
                <li class="avatar custom" data-avatarId="<ww:property value="./id" />" <ww:if test="./id == /avatar/id">id="selected-avatar"</ww:if>>
                    <img alt="<ww:text name="'avatarpicker.tooltip'" />" height="48" id="<ww:property value="./id"/>" src="<ww:property value="/url(.)"/>" width="48" />
                    <a class="del" href="<%= request.getContextPath()%>/secure/project/DeleteAvatar!default.jspa?avatarId=<ww:property value="./id"/>&amp;ownerId=<ww:property value="/ownerId"/>&amp;avatarType=<ww:property value="/avatarType"/>">x</a>
                </li>
            </ww:iterator>
            <ww:if test="/temporaryAvatarExistent == true">
                <li class="avatar custom" id="selected-avatar"><img alt="" height="48" src="<ww:property value="/temporaryAvatarUrl"/>" width="48" /></li>
            </ww:if>
        </ul>
    </div>

    <div class="module error message">
        <p class="retryprogress"><ww:text name="'avatarpicker.error'"/> <strong class="retry">(<ww:text name="'avatarpicker.retry'"/>)</strong></p>
        <p class="retrycomplete"><ww:text name="'avatarpicker.retrycomplete'"/></p>
        <button class="tryagain"><ww:text name="'avatarpicker.tryagain'"/></button>
        <button type="button" class="aui-button aui-button-link cancel"><ww:text name="'admin.common.words.cancel'"/></button>
    </div>

</div>

