package com.ebremer.webid4vivo;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Random;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author tammydiprima
 */
public class WebidHelper {

    /**
     * Hand back THE ontology model.
     *
     * @param request
     * @return
     */
    public OntModel getOntModel(HttpServletRequest request) {
        return new VitroRequest(request).getJenaOntModel();
    }

    /**
     * Current user account.
     *
     * @param request
     * @return
     */
    public UserAccount getCurrentUserAccount(HttpServletRequest request) {
        VitroRequest vreq = new VitroRequest(request);

        // EDIT
        return LoginStatusBean.getCurrentUser(vreq);


        /*
         edu.cornell.mannlib.vitro.webapp.beans.UserAccount a = LoginStatusBean.getCurrentUser(vreq);
         com.ebremer.webid4vivo.UserAccount b = (com.ebremer.webid4vivo.UserAccount) a;
         b.setWebidLinkToProfile(a.getExternalAuthId());
        
         getUserAccountsDao(vreq).updateUserAccount(b);
        
         return b;
         */
    }

    /**
     * Get a reference to the UserAccountsDao, or null.
     */
    private UserAccountsDao getUserAccountsDao(VitroRequest vreq) {
        WebappDaoFactory wadf = vreq.getWebappDaoFactory();
        if (wadf == null) {
            return null;
        }

        UserAccountsDao userAccountsDao = wadf.getUserAccountsDao();
        if (userAccountsDao == null) {
            System.out.println("getUserAccountsDao: no UserAccountsDao");
        }

        return userAccountsDao;
    }

    /**
     * User is logging in with WebID.
     *
     * @param request
     * @return
     */
    protected UserAccount getUserAccount(HttpServletRequest request, String webid) {
        StringBuffer queryString = new StringBuffer();
        queryString.append("PREFIX  auth:  <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> \n");
        queryString.append("SELECT ?s WHERE { \n");
        queryString.append("?s auth:hasWebIDAssociation ?bnode . \n");
        queryString.append("?bnode auth:hasWebID ");
        queryString.append("<");
        queryString.append(webid);
        queryString.append("> . }");

        System.out.println(new java.util.Date() + " GET USER ACCOUNT");
        System.out.println(queryString);
        String userAcctUri = "";

        com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString.toString());

        HttpSession session = ((HttpServletRequest) request).getSession(false);
        ServletContext ctx = session.getServletContext();
        OntModelSelector ontModelSelector = ModelContext.getOntModelSelector(ctx);
        OntModel userAccts = ontModelSelector.getUserAccountsModel();
        // Enter a critical section. The application must call leaveCriticialSection.
        userAccts.enterCriticalSection(Lock.READ);

        try {
            QueryExecution qe = QueryExecutionFactory.create(query, userAccts);

            ResultSet results = qe.execSelect();
            for (; results.hasNext();) {
                QuerySolution qsoln = results.nextSolution();

                Resource r = qsoln.getResource("s");
                userAcctUri = (String) r.getURI();

            }
            qe.close();
        } finally {
            // Releases the lock from the matching enterCriticalSection.
            userAccts.leaveCriticalSection();
            //aboxModel.close();
        }

        if (!userAcctUri.isEmpty()) {
            return getUserAccountByUri(request, userAcctUri);

        } else {
            return null;
        }

    }

    /**
     *
     * @param request
     * @param user
     * @throws
     * edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator.LoginNotPermitted
     */
    protected void recordLogin(HttpServletRequest request, UserAccount user) throws Authenticator.LoginNotPermitted {
        Authenticator.getInstance(request).recordLoginAgainstUserAccount(user, LoginStatusBean.AuthenticationSource.EXTERNAL);
    }

    /**
     * Add data to model.
     */
    protected void addIt(HttpServletRequest request, String s, boolean acctsModel) {

        HttpSession session = ((HttpServletRequest) request).getSession(false);
        ServletContext ctx = session.getServletContext();
        OntModelSelector ontModelSelector = ModelContext.getOntModelSelector(ctx);
        OntModel model = null;
        
        if (acctsModel)
            model = ontModelSelector.getUserAccountsModel();
        else
            model = ontModelSelector.getABoxModel();
        
        model.enterCriticalSection(Lock.WRITE);
        try {
            /* avoiding blank node issue for now.... */
            InputStream is = null;
            try {
                is = new ByteArrayInputStream(s.getBytes("UTF-8"));
            } catch (Exception ex) {
                System.out.println(ex.toString());
            }
            Model im = ModelFactory.createDefaultModel();
            im.read(is, null, "TTL");
            
            String whichModel = "";
            
            if (acctsModel)
                whichModel = "Accounts Model";
            else
                whichModel = "Public ABox Model";
            
            System.out.println(new java.util.Date() + " adding record to " + whichModel);
            im.write(System.out, "TTL");
            model.add(im);
            
        } catch (Exception ex) {
            System.out.println("addIt(): " + ex.toString());
        } finally {
            //aboxModel.getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),false,individualURI));
            model.leaveCriticalSection();
        }
    }

    /**
     * Update vivo with person's webid.
     *
     * @param request
     */
    public void updateVivoWithExternalWebid(HttpServletRequest request) {

        UserAccount userAccount = getCurrentUserAccount(request);

        StringBuffer sb = new StringBuffer();
        //they probably have made provisions for their fake
        sb.append("@prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> .\n");
        sb.append("@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n");
        sb.append("<");
        //sb.append(getProfileUri(request));
        sb.append(userAccount.getUri());
        sb.append("> ");
        sb.append(" auth:hasWebIDAssociation _:bnode .\n");
        sb.append("_:bnode auth:hasWebID \n");
        sb.append("<");
        sb.append(request.getParameter("txtWebId"));
        sb.append("> ;\n");
        sb.append("auth:localHosted false ;\n");
        sb.append("auth:me true ;\n");
        sb.append("rdfs:label \"remote\" . \n");
        
        addIt(request, sb.toString(), true);
        
        StringBuffer str = new StringBuffer();
        String uri = request.getParameter("txtWebId");
        
        Random random = new Random(System.currentTimeMillis());
        int blah = Math.abs(random.nextInt());
        //System.out.println(String.valueOf(blah));
                
        //they probably have made provisions for their fake
        str.append("@prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> .\n");
        str.append("@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n");
        str.append("<");
        str.append(getProfileUri(request));
        str.append("> ");
        str.append(" auth:hasWebIDAssociation <http://vivo.stonybrook.edu/individual/n" + blah + "> .\n");
        str.append("<http://vivo.stonybrook.edu/individual/n" + blah + "> auth:hasWebID \n");
        str.append("<");
        str.append(uri);
        str.append("> ;\n");
        str.append("auth:localHosted false ;\n");
        str.append("auth:me true ;\n");
        str.append("rdfs:label \"remote\" . \n");        
        
        addIt(request, str.toString(), false);

    }

    /**
     * Update vivo with person's webid.
     *
     * @param request
     */
    public void updateVivoWithGeneratedWebid(HttpServletRequest request, X509Certificate cert) {

        UserAccount userAccount = getCurrentUserAccount(request);

        RSAPublicKey certpublickey = (RSAPublicKey) cert.getPublicKey();
        String modulus = String.format("%0288x", certpublickey.getModulus());
        String exponent = String.valueOf(certpublickey.getPublicExponent());
        String label = request.getParameter("label");

        StringBuffer sb = new StringBuffer();
        String webid = request.getParameter("webid");

        sb.append("@prefix cert: <http://www.w3.org/ns/auth/cert#> .\n");
        sb.append("@prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> .\n");
        sb.append("@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n");
        sb.append("<");
        sb.append(userAccount.getUri());
        sb.append("> ");
        sb.append(" auth:hasWebIDAssociation _:bnode11 .\n");
        sb.append("_:bnode11 auth:hasWebID \n");
        sb.append("<");
        sb.append(webid);
        sb.append("> ;\n");
        sb.append("	auth:localHosted true ;\n");
        sb.append("	auth:me true ;\n");
        sb.append("	rdfs:label \"home\" ;\n");
        sb.append("	cert:key _:bnode22 .\n");
        sb.append("_:bnode22 a cert:RSAPublicKey ;\n");
        sb.append("     cert:exponent ");
        sb.append(exponent);
        sb.append(";    cert:modulus \n");
        sb.append("\"");
        sb.append(modulus);
        sb.append("\"^^<http://www.w3.org/2001/XMLSchema#hexBinary> . \n");


        addIt(request, sb.toString(), true);
        
        StringBuffer str = new StringBuffer();
        Random random = new Random(System.currentTimeMillis());
        int blah = Math.abs(random.nextInt());  
        
        int blah1 = Math.abs(random.nextInt()+1);
        
        str.append("@prefix cert: <http://www.w3.org/ns/auth/cert#> .\n");
        str.append("@prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> .\n");
        str.append("@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n");
        str.append("<");
        str.append(webid);
        str.append("> ");
        str.append(" auth:hasWebIDAssociation <http://vivo.stonybrook.edu/individual/n" + blah + "> .\n");
        str.append("<http://vivo.stonybrook.edu/individual/n" + blah + "> auth:hasWebID \n");
        str.append("<");
        str.append(webid);
        str.append("> ;\n");
        str.append("	auth:localHosted true ;\n");
        str.append("	auth:me true ;\n");
        str.append("	rdfs:label \"home\" ;\n");
        str.append("	cert:key <http://vivo.stonybrook.edu/individual/n" + blah1 + "> .\n");
        str.append("<http://vivo.stonybrook.edu/individual/n" + blah1 + "> a cert:RSAPublicKey ;\n");
        str.append("     cert:exponent ");
        str.append(exponent);
        str.append(";    cert:modulus \n");
        str.append("\"");
        str.append(modulus);
        str.append("\"^^<http://www.w3.org/2001/XMLSchema#hexBinary> . \n");

        addIt(request, str.toString(), false);

    }

    /**
     *
     * @param request
     * @return
     */
    public ArrayList<WebIDAssociation> getWebIdList(HttpServletRequest request) {

        StringBuffer queryString = new StringBuffer();

        queryString.append("PREFIX  auth:  <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> \n");
        queryString.append("PREFIX  rdfs:  <http://www.w3.org/2000/01/rdf-schema#> \n");

        queryString.append("SELECT ?hasWebIDAssociation ?me ?webid ?localHosted ?label \n");
        queryString.append("WHERE { \n");
        queryString.append("<");
        //queryString.append(getProfileUri(request));
        queryString.append(this.getCurrentUserAccount(request).getUri());
        queryString.append(">\n");

        queryString.append("auth:hasWebIDAssociation ?bnode . \n");
        queryString.append("?bnode auth:me ?me ; \n");
        queryString.append("auth:hasWebID ?webid ; \n");
        queryString.append("auth:localHosted ?localHosted ; \n");
        queryString.append("rdfs:label ?label . \n");
        queryString.append("}");

        System.out.println(new java.util.Date() + " LIST WEBIDS");
        System.out.println(queryString);

        ArrayList<WebIDAssociation> webidList = new ArrayList();
        
        com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString.toString());
        
        HttpSession session = ((HttpServletRequest) request).getSession(false);
        ServletContext ctx = session.getServletContext();
        OntModelSelector ontModelSelector = ModelContext.getOntModelSelector(ctx);
        OntModel userAccts = ontModelSelector.getUserAccountsModel();

        // Enter a critical section. The application must call leaveCriticialSection.
        userAccts.enterCriticalSection(Lock.READ);

        try {
            QueryExecution qe = QueryExecutionFactory.create(query, userAccts);

            ResultSet results = qe.execSelect();
            for (; results.hasNext();) {
                
                QuerySolution q = results.nextSolution();
                boolean me = q.getLiteral("me").getBoolean();
                String webid = (String) q.getResource("webid").getURI();
                boolean localHosted = q.getLiteral("localHosted").getBoolean();
                String label = q.getLiteral("label").getString();
                
                webidList.add(new WebIDAssociation(me, webid, localHosted, label));

                // TODO: ADD ROLE.
                //out.println("<tr><td>" + really.getString() + "</td><td>Self-Edit</td></tr>");
            }
            qe.close();
        } 
        finally {
            // Releases the lock from the matching enterCriticalSection.
            userAccts.leaveCriticalSection();
            //aboxModel.close();
        }


        return webidList;

    }

    /**
     * Get Default Namespace.
     *
     * @param request
     * @return
     */
    public String getNamespace(HttpServletRequest request) {
        return new VitroRequest(request).getWebappDaoFactory().getDefaultNamespace();
    }

    /**
     * I have a User Account. Now get Profile URI.
     *
     * @param request
     * @param response
     * @return
     */
    public String getProfileUri(HttpServletRequest request) {
        VitroRequest vreq = new VitroRequest(request);
        UserAccount userAccount = LoginStatusBean.getCurrentUser(vreq);
        String defaultNamespace = vreq.getWebappDaoFactory().getDefaultNamespace();
        //System.out.println(defaultNamespace);
        String profileURI = "";

        if (userAccount != null) {
            StringBuffer queryString = new StringBuffer();

            // Fetch Profile URI, by network ID.
            queryString.append("SELECT ?uri WHERE { ?uri <http://vivo.stonybrook.edu/ns#networkId> \"");
            queryString.append(userAccount.getExternalAuthId());
            queryString.append("\" . }");

            System.out.println("getProfileUri(): " + queryString);

            com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString.toString());

            OntModel ontModel = getOntModel(request);
            QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
            int i = 0;

            ResultSet results = qe.execSelect();
            for (; results.hasNext();) {
                QuerySolution qsoln = results.nextSolution();
                Resource r = qsoln.getResource("uri");
                profileURI = r.getURI();
                //System.out.println(++i + " " + profileURI);

                if (profileURI.startsWith(defaultNamespace)) {
                    break;
                }
            }
            qe.close();
        } else {
            // Shouldn't happen, but...
            System.out.println("PROFILE IS NULL");
        }

        return profileURI;
    }

    /**
     * Code for: Close this window, and refresh parent window.
     *
     * @return the closeAndRefresh
     */
    public String getCloseAndRefresh() {
        StringBuffer sb = new StringBuffer();
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<script language=\"javascript\">");
        sb.append("function closeAndRefresh(){");
        sb.append("  opener.location.reload();");
        sb.append("  window.close();");
        sb.append("}");
        sb.append("</script>");
        sb.append("</head>");
        sb.append("<body onload=\"closeAndRefresh()\">");
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }

    /**
     * This is override of LoginStatusBean.getCurrentUser().
     *
     * @param session
     * @param userEmail
     * @return
     */
    public static UserAccount getUserAccountByUri(HttpServletRequest request, String s) {

        ServletContext ctx = request.getSession().getServletContext();
        WebappDaoFactory wadf = (WebappDaoFactory) ctx.getAttribute("webappDaoFactory");
        if (wadf == null) {
            System.out.println("No WebappDaoFactory");
            return null;
        }

        UserAccountsDao userAccountsDao = wadf.getUserAccountsDao();
        if (userAccountsDao == null) {
            System.out.println("No UserAccountsDao");
            return null;
        }

        //  return Authenticator.getInstance(request).getAccountForExternalAuth(webidAuthID);
        //  return userAccountsDao.getUserAccountByEmail(userEmail);

        return userAccountsDao.getUserAccountByUri(s);

    }
}
