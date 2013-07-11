package com.ebremer.webid4vivo;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
    protected void addIt(HttpServletRequest request, String s) {

        try {
            VitroRequest vreq = new VitroRequest(request);
            Dataset dataset = vreq.getDataset();

            Model im = ModelFactory.createDefaultModel();

            InputStream is = null;
            try {
                is = new ByteArrayInputStream(s.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                System.out.println(ex.toString());
            }
            System.out.println("importing...");
            im.read(is, null, "TTL");
            im.write(System.out, "TTL");
            System.out.println("adding triples to kb 2...");
            Model e = dataset.getNamedModel("http://vitro.mannlib.cornell.edu/default/vitro-kb-2");
            e.add(im);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Get data from model.
     */
    protected void getIt(HttpServletRequest request, String s) {

        try {
            VitroRequest vreq = new VitroRequest(request);
            Dataset dataset = vreq.getDataset();

            Model im = ModelFactory.createDefaultModel();

            InputStream is = null;
            try {
                is = new ByteArrayInputStream(s.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                System.out.println(ex.toString());
            }
            System.out.println("importing...");
            im.read(is, null, "TTL");
            im.write(System.out, "TTL");
            System.out.println("adding triples to kb 2...");
            Model e = dataset.getNamedModel("http://vitro.mannlib.cornell.edu/default/vitro-kb-2");
            e.add(im);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }    

    /**
     * Update vivo with person's webid.
     *
     * @param request
     */
    public void updateVivoWithExternalWebid(HttpServletRequest request) {

        StringBuffer sb = new StringBuffer();
        //they probably have made provisions for their fake
        sb.append("@prefix local: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> .\n");
        //sb.append("@prefix local: <http://vivo.stonybrook.edu/local#> .\n");
        sb.append("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n");
        sb.append("<");
        sb.append(getProfileUri(request));
        sb.append("> ");
        sb.append(" local:hasWebIDAssociation _:bnode .\n");
        sb.append("_:bnode local:hasWebID \n");
        sb.append("<");
        sb.append(request.getParameter("txtWebId"));
        sb.append("> ;\n");
        sb.append("local:localHosted false ;\n");
        sb.append("local:me true ;\n");
        sb.append("rdf:label \"remote\" . \n");
        System.out.println(sb.toString());

        addIt(request, sb.toString());

    }

    /**
     * Update vivo with person's webid.
     *
     * @param request
     */
    public void updateVivoWithGeneratedWebid(HttpServletRequest request, X509Certificate cert) {

        RSAPublicKey certpublickey = (RSAPublicKey) cert.getPublicKey();
        String modulus = String.format("%0288x", certpublickey.getModulus());
        String exponent = String.valueOf(certpublickey.getPublicExponent());

        StringBuffer sb = new StringBuffer();
        String webid = request.getParameter("webid");

        sb.append("@prefix cert: <http://www.w3.org/ns/auth/cert#> .\n");
        sb.append("@prefix local: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> .\n");
        sb.append("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n");
        sb.append("<");
        sb.append(webid);
        sb.append("> ");
        sb.append(" local:hasWebIDAssociation _:bnode11 .\n");
        sb.append("_:bnode22 rdf:label \"pretend\" ;\n");
        sb.append("	a cert:RSAPublicKey ;\n");
        sb.append("	cert:exponent 123456 ;\n");
        sb.append("	cert:modulus \n");
        sb.append("\"");
        sb.append(modulus);
        sb.append("\"^^<http://www.w3.org/2001/XMLSchema#hexBinary> . \n");

        sb.append("_:bnode11 local:hasWebID \n");
        sb.append("<");
        sb.append(webid);
        sb.append("> ;\n");
        sb.append("	local:localHosted true ;\n");
        sb.append("	local:me true ;\n");
        sb.append("	rdf:label \"home\" ;\n");
        sb.append("	cert:key _:bnode22 .\n");
        System.out.println(sb.toString());

        addIt(request, sb.toString());


    }

    /**
     * User is logging in with WebID. Fetch info so I can log person in.
     *
     * @param request
     * @param webid
     * @return
     */
    public String[] getIdsForLogin(HttpServletRequest request, String webid) {

        String[] id = {"", ""};

        StringBuffer queryString = new StringBuffer();

        queryString.append("SELECT ?s ?id WHERE { ?s <http://vivo.stonybrook.edu/ns#networkId> ?id ; \n");
        queryString.append("<http://vitro.mannlib.cornell.edu/ns/vitro/authorization#hasWebIDAssociation> ?bnode . \n");
        queryString.append("?bnode <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#hasWebID> \n");

        queryString.append("<");
        queryString.append(webid);
        queryString.append("> . }\n");

        System.out.println("getIdsForLogin(): " + queryString);

        com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString.toString());

        OntModel ontModel = getOntModel(request);
        QueryExecution qe = QueryExecutionFactory.create(query, ontModel);

        ResultSet results = qe.execSelect();
        for (; results.hasNext();) {
            QuerySolution qsoln = results.nextSolution();

            Resource r = qsoln.getResource("s");
            id[0] = (String) r.getURI();

            Literal l = qsoln.getLiteral("id");
            id[1] = (String) l.getString();

        }
        qe.close();
        System.out.println("subj: " + id[0]);
        System.out.println("id: " + id[1]);

        return id;

    }

    /**
     *
     * @param request
     * @return
     */
    public ArrayList<WebIDAssociation> getWebIdList(HttpServletRequest request) {
        StringBuffer sb = new StringBuffer();

        StringBuffer queryString = new StringBuffer();
        queryString.append("SELECT ?hasWebIDAssociation ?me ?hasWebID ?localHosted ?label \n");
        queryString.append("FROM NAMED <http://vitro.mannlib.cornell.edu/default/vitro-kb-2> \n");
        queryString.append("WHERE { \n");
        queryString.append("<");
        queryString.append(getProfileUri(request));
        queryString.append(">  <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#hasWebIDAssociation> ?bnode . \n");
        queryString.append("?bnode <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#me> ?me ; \n");
        queryString.append("<http://vitro.mannlib.cornell.edu/ns/vitro/authorization#hasWebID> ?hasWebID ; \n");
        queryString.append("<http://vitro.mannlib.cornell.edu/ns/vitro/authorization#localHosted> ?localHosted ; \n");
        queryString.append("<http://www.w3.org/1999/02/22-rdf-syntax-ns#label> ?label . \n");
        queryString.append("}\n");

        System.out.println("listWebids: " + queryString);

        com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString.toString());

        OntModel ontModel = getOntModel(request);

        QueryExecution qe = QueryExecutionFactory.create(query, ontModel);

        ArrayList<WebIDAssociation> webidList = new ArrayList();
        ResultSet results = qe.execSelect();

        for (; results.hasNext();) {
            QuerySolution q = results.nextSolution();
            webidList.add(new WebIDAssociation(q.getLiteral("me").getBoolean(), q.getResource("webid").getURI(), q.getLiteral("localHosted").getBoolean(), q.getLiteral("label").getString()));

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
