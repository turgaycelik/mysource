<%@ taglib uri="webwork" prefix="ww" %>
<input type="text"
       name="<ww:property value="parameters['name']"/>"
      <ww:property value="parameters['size']">
         <ww:if test=".">size="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['maxlength']">
         <ww:if test=".">maxlength="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['nameValue']">
         <ww:if test=".">value="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['disabled']">
         <ww:if test="{parameters['disabled']}">DISABLED</ww:if>
      </ww:property>
      <ww:property value="parameters['readonly']">
         <ww:if test="{parameters['readonly']}">READONLY</ww:if>
      </ww:property>
      <ww:property value="parameters['onkeyup']">
         <ww:if test=".">onkeyup="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['tabindex']">
         <ww:if test=".">tabindex="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['onchange']">
         <ww:if test=".">onchange="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['style']">
         <ww:if test=".">style="<ww:property value="."/>"</ww:if>
      </ww:property>
    <ww:property value="parameters['class']">
       <ww:if test=".">class="<ww:property value="."/>"</ww:if>
    </ww:property>
/>
    <span class="subText">
    <ww:if test="parameters['imagesrc']">
        <a href="#" onclick="<ww:property value="parameters['imagefunction']"/>;return false;">
        <img title="<ww:property value="parameters['imagetitle']"/>"
            name="<ww:property value="parameters['imagename']"/>"
            src="<ww:property value="parameters['imagesrc']"/>" hspace=0 height=16 width=16 border=0 align=absmiddle>
        </a>
    </ww:if>
    <ww:else>
        [ <a href="#" class="subText" onclick="<ww:property value="parameters['imagefunction']"/>;return false;"><ww:text name="'admin.text.image.select.image'"/></a> ]
    </ww:else>
    </span>
