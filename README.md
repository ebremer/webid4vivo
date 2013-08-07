webid4vivo
==========

webid4vivo <br>
add webid authentication and provisioning capabilities to VIVO (http://www.vivoweb.org)

**Developers:**<br>
Erich Bremer
Tammy DiPrima

=================

**HOW TO IMPLEMENT:**

**Move java files to:** [vivo-install-dir]/src/edu/stonybrook/ai/webid4vivo

**Add the servlets & servlet mappings to productMods/WEB-INF/web.xml:**<br>
<pre><code>
    &lt;servlet&gt;
        &lt;servlet-name&gt;signIn&lt;/servlet-name&gt;
        &lt;servlet-class&gt;edu.stonybrook.ai.webid4vivo.auth&lt;/servlet-class&gt;
    &lt;/servlet&gt;
    &lt;servlet&gt;
        &lt;servlet-name&gt;webidMgt&lt;/servlet-name&gt;
        &lt;servlet-class&gt;edu.stonybrook.ai.webid4vivo.WebidController&lt;/servlet-class&gt;
    &lt;/servlet&gt;
    &lt;servlet&gt;
        &lt;servlet-name&gt;webidGen&lt;/servlet-name&gt;
        &lt;servlet-class&gt;edu.stonybrook.ai.webid4vivo.WebidGenerator&lt;/servlet-class&gt;
    &lt;/servlet&gt;
    &lt;servlet-mapping&gt;
        &lt;servlet-name&gt;signIn&lt;/servlet-name&gt;
        &lt;url-pattern&gt;/signIn&lt;/url-pattern&gt;
    &lt;/servlet-mapping&gt;
    &lt;servlet-mapping&gt;
        &lt;servlet-name&gt;webidMgt&lt;/servlet-name&gt;
        &lt;url-pattern&gt;/webidMgt&lt;/url-pattern&gt;
    &lt;/servlet-mapping&gt;
    &lt;servlet-mapping&gt;
        &lt;servlet-name&gt;webidGen&lt;/servlet-name&gt;
        &lt;url-pattern&gt;/webidGen&lt;/url-pattern&gt;
    &lt;/servlet-mapping&gt;
</code></pre>

**Add the following to /vitro-core/webapp/web/templates/freemarker/widgets/widget-login.ftl:**
<pre><code>
&lt;p class="external-auth"&gt;&lt;a class="blue button" href="/signIn" title="webid"&gt;WebID&lt;/a&gt;&lt;/p&gt;
&lt;p class="or-auth"&gt;or&lt;/p&gt;
</code></pre>
**Suggestion -- if you are also using external authentication, make that button green:**
<pre><code>
&lt;p class="external-auth"&gt;&lt;a class="green button" href="${externalAuthUrl}" title="external authentication name"&gt;${externalAuthName}&lt;/a&gt;&lt;/p&gt;
</code></pre>
**Add the following to productMods/templates/freemarker/body/individual/individual--foaf-person.ftl**:
<p>*Where it says:*
<pre><code>&lt;section id="individual-info" ${infoClass!} role="region"&gt; &lt;br&gt;
</code></pre>
*Add this underneath:*
<pre><code>
        &lt;#if user.loggedIn&gt;
         &lt;div align="right"&gt;&lt;a href="/webidMgt?2"&gt;My WebIDs&lt;/a&gt;&lt;/div&gt;
    	&lt;/#if&gt;
</code></pre>
</p>
**Stop tomcat<br>
Redeploy vivo<br>**

**Modify /etc/httpd/conf/httpd.conf:**
<pre><code>
&#35;Listen 12.34.56.78:80
Listen 443
&#35; LoadModule foo_module modules/mod_foo.so
LoadModule ssl_module modules/mod_ssl.so
&#35;LoadModule include_module modules/mod_include.so
&#35;&lt;VirtualHost *:80&gt;	
&lt;Location /signIn&gt;
SSLVerifyDepth 0
SSLVerifyClient optional_no_ca
&lt;/Location&gt;
</code></pre>
**Restart apache**

