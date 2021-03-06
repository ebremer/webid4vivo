## WebID4VIVO

Purpose: To add WebID authentication and provisioning capabilities to VIVO (http://www.vivoweb.org)

**Developers:**<br>
Erich Bremer http://www.ebremer.com<br>
Tammy DiPrima

=================

### HOW TO IMPLEMENT:

#### Required Jars

**Get [Bouncy Castle](http://www.bouncycastle.org/latest_releases.html) Crypto APIs:** 

* bcpkix-jdk15on-149.jar 
* bcprov-jdk15on-149.jar

**Put the jars here:** [vivo-install-dir]/src

**For your local development environment, you'll also need:**

* The [Apache Commons Logging](http://commons.apache.org/proper/commons-logging/download_logging.cgi) jar
  * commons-logging
* The [Apache Jena](http://www.apache.org/dist/jena/binaries/) jars:
  * jena-core
  * jena-arq  
    * Note: Download the apache-jena* file.  When you uncompress it, you will find both "core" and "arq" files in the "lib" folder.
* The VIVO class files:
  * If you haven't already, download [VIVO](http://vivoweb.org/download)
  * Once you deploy the application, you'll find class files here: WEB-INF/classes/edu.
  * Package them into a jar.
* Finally, of course, you'll need the Java and Java Servlet APIs


#### Source Code

**Create directory, and move java files to:** [vivo-install-dir]/src/edu/stonybrook/hsai/webid4vivo

**Add the servlets & servlet mappings to:** [vivo-install-dir]productMods/WEB-INF/web.xml<br>

    <servlet>
        <servlet-name>signIn</servlet-name>
        <servlet-class>edu.stonybrook.hsai.webid4vivo.auth</servlet-class>
    </servlet>
    <servlet>
        <servlet-name>webidMgt</servlet-name>
        <servlet-class>edu.stonybrook.hsai.webid4vivo.WebidController</servlet-class>
    </servlet>
    <servlet>
        <servlet-name>webidGen</servlet-name>
        <servlet-class>edu.stonybrook.hsai.webid4vivo.WebidGenerator</servlet-class>
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


#### Add button and link to user interface

**Add the following to:** [vivo-install-dir]/vitro-core/webapp/web/templates/freemarker/widgets/widget-login.ftl

    <p class="external-auth"><a class="blue button" href="/signIn" title="webid">WebID</a></p>
    <p class="or-auth">or</p>
    
Notice that the button color is <font color="#398aac">**blue**</font>.

**Suggestion:** If you are also using external authentication, make the external-auth button <font color="#749a02">**green**</font>.

    <p class="external-auth"><a class="green button" href="${externalAuthUrl}" title="external authentication name">${externalAuthName}</a></p>

**Add the following to:** [vivo-install-dir]/productMods/templates/freemarker/body/individual/individual--foaf-person.ftl:

*Where it says:*

    <section id="individual-info" ${infoClass!} role="region"> <br>

*Add this underneath:*

    <#if user.loggedIn>    
        <div align="right"><a href="/webidMgt?2">My WebIDs</a></div>
    </#if>


**Stop Tomcat<br>
Redeploy VIVO<br>**

#### Apache HTTP Server

**Add commands to /etc/httpd/conf/httpd.conf:**

    #Listen 12.34.56.78:80
    Listen 443
    
    #LoadModule foo_module modules/mod_foo.so
    LoadModule ssl_module modules/mod_ssl.so
    
    #<VirtualHost *:80>    
    <Location /signIn>
        SSLVerifyDepth 0
        SSLVerifyClient optional_no_ca
    </Location>

**Restart Apache**
