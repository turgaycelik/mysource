<%@ taglib uri="webwork" prefix="ww"  %>

<ul class="operations-list">
<ww:if test="/atLevel(., 'TRACE') == false">
    <li><a href="<ww:url page="ConfigureLogging.jspa"><ww:param name="'loggerName'" value="./name"/><ww:param name="'levelName'" value="'TRACE'"/></ww:url>">TRACE</a></li>
</ww:if>

<ww:if test="/atLevel(., 'DEBUG') == false">
    <li><a href="<ww:url page="ConfigureLogging.jspa"><ww:param name="'loggerName'" value="./name"/><ww:param name="'levelName'" value="'DEBUG'"/></ww:url>">DEBUG</a></li>
</ww:if>

<ww:if test="/atLevel(., 'INFO') == false">
    <li><a href="<ww:url page="ConfigureLogging.jspa"><ww:param name="'loggerName'" value="./name"/><ww:param name="'levelName'" value="'INFO'"/></ww:url>">INFO</a></li>
</ww:if>

<ww:if test="/atLevel(., 'WARN') == false">
    <li><a href="<ww:url page="ConfigureLogging.jspa"><ww:param name="'loggerName'" value="./name"/><ww:param name="'levelName'" value="'WARN'"/></ww:url>">WARN</a></li>
</ww:if>

<ww:if test="/atLevel(., 'ERROR') == false">
    <li><a href="<ww:url page="ConfigureLogging.jspa"><ww:param name="'loggerName'" value="./name"/><ww:param name="'levelName'" value="'ERROR'"/></ww:url>">ERROR</a></li>
</ww:if>

<ww:if test="/atLevel(., 'FATAL') == false">
    <li><a href="<ww:url page="ConfigureLogging.jspa"><ww:param name="'loggerName'" value="./name"/><ww:param name="'levelName'" value="'FATAL'"/></ww:url>">FATAL</a></li>
</ww:if>

<ww:if test="/atLevel(., 'OFF') == false">
    <li><a href="<ww:url page="ConfigureLogging.jspa"><ww:param name="'loggerName'" value="./name"/><ww:param name="'levelName'" value="'OFF'"/></ww:url>">OFF</a></li>
</ww:if>
</ul>
