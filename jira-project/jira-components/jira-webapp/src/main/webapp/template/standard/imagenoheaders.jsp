<%@ taglib uri="webwork" prefix="ww" %>
<span style="visibility: hidden; position: absolute;"><input type="text"
       name="<ww:property value="parameters['name']"/>"
      <ww:property value="parameters['hiddenvalue']">
         <ww:if test=".">value="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['hiddenid']">
         <ww:if test=".">id="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['nameValue']">
         <ww:if test=".">value="<ww:property value="."/>"</ww:if>
      </ww:property>
/></span>
<span class="subText">
    <a
        <ww:if test="parameters['linkid']">id="<ww:property value="parameters['linkid']"/>"</ww:if>
        <ww:if test="parameters['linkclass']">class="<ww:property value="parameters['linkclass']"/>"</ww:if>
        href="<ww:property value="parameters['url']" escape="false"/>"
        <ww:if test="parameters['onclick']">onclick="<ww:property value="parameters['onclick']" escape="false"/>;return false;"</ww:if>
    >
    <img
        <ww:if test="parameters['src']">
            title="<ww:property value="parameters['title']"/>"
            alt="<ww:property value="parameters['title']"/>"
            src="<ww:property value="parameters['src']" escape="false"/>"
        </ww:if>
        <ww:else>
            src="<%= request.getContextPath()%>/images/border/spacer.gif"
        </ww:else>
        name="<ww:property value="parameters['imagename']"/>"
        id="<ww:property value="parameters['id']"/>"
        <ww:if test="parameters['width']">
            width="<ww:property value="parameters['width']"/>"
        </ww:if>
        <ww:if test="parameters['height']">
            height="<ww:property value="parameters['height']"/>"
        </ww:if>
        <ww:if test="parameters['class']">class="<ww:property value="parameters['class']"/>"</ww:if>
        border="0"/>
    </a>
    <span id="<ww:property value="parameters['textid']"/>">
    <ww:if test="!parameters['src']">
        [ <a href="#" class="subText" onclick="<ww:property value="parameters['onclick']" escape="false"/>;return false;"><ww:text name="'admin.text.image.select.image'"/></a> ]
    </ww:if>
    </span>
</span>
