webid4vivo
==========

webid4vivo add webid authentication and provisioning capabilities to VIVO (http://www.vivoweb.org)

Developers:
Erich Bremer
Tammy DiPrima

=================

HOW TO IMPLEMENT:

Move java files to [vivo-install-dir]/src/edu/stonybrook/ai/webid4vivo

Add the servlets & servlet mappings to productMods/WEB-INF/web.xml:
    <servlet>
        <servlet-name>signIn</servlet-name>
        <servlet-class>edu.stonybrook.ai.webid4vivo.auth</servlet-class>
    </servlet>
    <servlet>
        <servlet-name>webidMgt</servlet-name>
        <servlet-class>edu.stonybrook.ai.webid4vivo.WebidController</servlet-class>
    </servlet>
    <servlet>
        <servlet-name>webidGen</servlet-name>
        <servlet-class>edu.stonybrook.ai.webid4vivo.WebidGenerator</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>signIn</servlet-name>
        <url-pattern>/signIn</url-pattern>
    </servlet-mapping>
    <servlet-mapping>
        <servlet-name>webidMgt</servlet-name>
        <url-pattern>/webidMgt</url-pattern>
    </servlet-mapping>
    <servlet-mapping>
        <servlet-name>webidGen</servlet-name>
        <url-pattern>/webidGen</url-pattern>
    </servlet-mapping>

Add the following to /vitro-core/webapp/web/templates/freemarker/widgets/widget-login.ftl:
                <p class="external-auth"><a class="blue button" href="/signIn" title="webid">WebID</a></p>
                <p class="or-auth">or</p>

Suggestion -- if you are also using external authentication, make that button green:
                <p class="external-auth"><a class="green button" href="${externalAuthUrl}" title="external authentication name">${externalAuthName}</a></p>


Add the following to productMods/templates/freemarker/body/individual/individual--foaf-person.ftl
Where it says:    <section id="individual-info" ${infoClass!} role="region"> 
Add this underneath:
        <#if user.loggedIn>
         <div align="right"><a href="/webidMgt?2">My WebIDs</a></div>
    	</#if>


Stop tomcat
Redeploy vivo

Modify /etc/httpd/conf/httpd.conf:
#Listen 12.34.56.78:80
Listen 443
# LoadModule foo_module modules/mod_foo.so
LoadModule ssl_module modules/mod_ssl.so
#LoadModule include_module modules/mod_include.so
#<VirtualHost *:80>	
    <Location /signIn>
	SSLVerifyDepth 0
	SSLVerifyClient optional_no_ca
    </Location>

Restart apache
