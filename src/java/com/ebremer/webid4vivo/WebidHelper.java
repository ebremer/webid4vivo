package com.ebremer.webid4vivo;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateAction;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author tammydiprima
 */
public class WebidHelper {

    private String closeAndRefresh;

    public WebidHelper() {
        setCloseAndRefresh();
    }

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
     * Get User's Account information
     *
     * @param person
     * @return
     */
    protected UserAccount getUserAccount(HttpServletRequest request, String webidAuthID) {
        return Authenticator.getInstance(request).getAccountForExternalAuth(webidAuthID);
    }

    /**
     * Update vivo with person's webid.
     *
     * @param request
     */
    public void updateVivoWithExternalWebid(HttpServletRequest request) {
        VitroRequest vreq = new VitroRequest(request);
        String webid = request.getParameter("txtWebId");

        try {
            Dataset dataset = vreq.getDataset();
            GraphStore graphStore = GraphStoreFactory.create(dataset);

            StringBuffer sb = new StringBuffer();

            sb.append("INSERT DATA ");
            sb.append("{ GRAPH <http://vitro.mannlib.cornell.edu/default/vitro-kb-2> ");
            sb.append("{ ");

            sb.append("<");
            sb.append(getProfileUri(request));
            sb.append("> ");

            sb.append("<http://www.w3.org/2002/07/owl#sameAs> ");

            sb.append("<");
            sb.append(webid);
            sb.append("> ");

            sb.append(" . ");
            sb.append("}");
            sb.append("}");

            System.out.println(sb.toString());
            UpdateAction.parseExecute(sb.toString(), graphStore);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Update vivo with person's webid.
     *
     * @param request
     */
    public void updateVivoWithGeneratedWebid(HttpServletRequest request, X509Certificate cert) {
        VitroRequest vreq = new VitroRequest(request);

        // EDIT
        // UserAccount currentUser = LoginStatusBean.getCurrentUser(vreq);

        try {
            Dataset dataset = vreq.getDataset();
            GraphStore graphStore = GraphStoreFactory.create(dataset);

            RSAPublicKey certpublickey = (RSAPublicKey) cert.getPublicKey();
            String modulus = String.format("%0288x", certpublickey.getModulus());
            String exponent = String.valueOf(certpublickey.getPublicExponent());

            StringBuffer sb = new StringBuffer();

            sb.append("INSERT DATA ");
            sb.append("{ GRAPH <http://vitro.mannlib.cornell.edu/default/vitro-kb-2> ");
            sb.append("{ ");
            // THE KEY (BLANK NODE)
            sb.append("<");
            sb.append(request.getParameter("webid"));
            sb.append("> ");
            sb.append(" <http://www.w3.org/ns/auth/cert#key> _:bnode . ");
            // PUBLIC KEY
            sb.append("_:bnode <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/auth/cert#RSAPublicKey> . ");
            // MODULUS
            sb.append("_:bnode <http://www.w3.org/ns/auth/cert#modulus> ");
            sb.append("\"");
            sb.append(modulus);
            sb.append("\"^^<http://www.w3.org/2001/XMLSchema#hexBinary> . ");
            // EXPONENT
            sb.append("_:bnode <http://www.w3.org/ns/auth/cert#exponent> ");
            sb.append(exponent);
            sb.append(" . ");
            sb.append("}");
            sb.append("}");

            System.out.println(sb.toString());
            UpdateAction.parseExecute(sb.toString(), graphStore);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * You're logging in with WebID. I need your ID so I can find your
     * UserAccount and log you in.
     *
     * @param request
     * @param webid
     * @return
     */
    public String getNetworkIdForLogin(HttpServletRequest request, String webid) {

        String id = "";

        StringBuffer queryString = new StringBuffer();
        queryString.append("SELECT ?id WHERE { <");
        queryString.append(webid);
        queryString.append("> <http://www.w3.org/2002/07/owl#sameAs> ?b . ");
        queryString.append("?b <http://vivo.stonybrook.edu/ns#networkId> ?id . }"); // WEBID == PERSON URI (INTERNAL).
        System.out.println("getNetworkIdForLogin(): " + queryString);

        com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString.toString());

        OntModel ontModel = getOntModel(request);
        QueryExecution qe = QueryExecutionFactory.create(query, ontModel);

        ResultSet results = qe.execSelect();
        for (; results.hasNext();) {
            QuerySolution qsoln = results.nextSolution();

            Literal really = qsoln.getLiteral("id");
            id = really.getString();
        }
        qe.close();
        System.out.println("id: " + id);

        return id;

    }

    /**
     *
     * @param request
     * @return
     */
    public ArrayList getWebIds(HttpServletRequest request) {
        StringBuffer sb = new StringBuffer();

        StringBuffer queryString = new StringBuffer();
        queryString.append("SELECT ?webid WHERE { ");
        queryString.append("<");
        queryString.append(getProfileUri(request));
        queryString.append(">  <http://www.w3.org/2002/07/owl#sameAs> ?webid . }");
        System.out.println("listWebids: " + queryString);

        com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString.toString());

        OntModel ontModel = getOntModel(request);

        QueryExecution qe = QueryExecutionFactory.create(query, ontModel);

        ArrayList webidList = new ArrayList();
        ResultSet results = qe.execSelect();

        for (; results.hasNext();) {
            QuerySolution qsoln = results.nextSolution();

            //Literal l = qsoln.getLiteral("webid");
            //webidList.add((String) l.getString());

            Resource r = qsoln.getResource("webid");
            webidList.add((String) r.getURI());

            // TODO: ADD ROLE.
            //out.println("<tr><td>" + really.getString() + "</td><td>Self-Edit</td></tr>");
        }
        qe.close();

        return webidList;

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

            // Fetch profile URI, by email address.
            //String email = userAccount.getEmailAddress();
            //queryString.append("SELECT ?s WHERE { ?s <http://vivoweb.org/ontology/core#primaryEmail> \"");
            //queryString.append(email);
            //queryString.append("\" . }");            

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
        return closeAndRefresh;
    }

    /**
     * Code for: Close this window, and refresh parent window.
     *
     * @param closeAndRefresh the closeAndRefresh to set
     */
    public void setCloseAndRefresh() {
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
        this.closeAndRefresh = sb.toString();
    }

    /**
     * NOT USED. This is override of LoginStatusBean.getCurrentUser().
     *
     * @param session
     * @param userEmail
     * @return
     */
    public static UserAccount getUserAccountByEmail(HttpSession session, String userEmail) {
        if (session == null) {
            return null;
        }

        ServletContext ctx = session.getServletContext();
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

        // a different way, by external auth id:
        //    return Authenticator.getInstance(request).getAccountForExternalAuth(webidAuthID);

        return userAccountsDao.getUserAccountByEmail(userEmail);
    }

    /**
     * NOT USED. You're logging in with WebID. I need your email so I can find
     * your UserAccount and log you in. TODO: EMAIL NEEDS TO BE MANDATORY.
     *
     * @param request
     * @param webid
     * @return
     */
    public String getEmail(HttpServletRequest request, String webid) {

        String email = "";

        StringBuffer queryString = new StringBuffer();
        queryString.append("SELECT ?email WHERE { <");
        queryString.append(webid.trim()); // WEBID == PERSON URI.
        queryString.append("> <http://vivoweb.org/ontology/core#primaryEmail> ?email . }");
        System.out.println("getEmail(): " + queryString);

        com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString.toString());

        OntModel ontModel = getOntModel(request);
        QueryExecution qe = QueryExecutionFactory.create(query, ontModel);

        ResultSet results = qe.execSelect();
        for (; results.hasNext();) {
            QuerySolution qsoln = results.nextSolution();

            Literal really = qsoln.getLiteral("email");
            email = really.getString();
        }
        qe.close();
        System.out.println("email: " + email);

        return email;

    }
}
