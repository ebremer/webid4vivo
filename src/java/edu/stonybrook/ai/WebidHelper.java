package edu.stonybrook.ai;

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
import com.hp.hpl.jena.update.UpdateAction;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handle back-end communications.
 * 
 * @author Erich Bremer
 * @author Tammy DiPrima
 */
public class WebidHelper {

    private static final Log log = LogFactory.getLog(WebidHelper.class);

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
        return LoginStatusBean.getCurrentUser(vreq);
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

        if (acctsModel) {
            model = ontModelSelector.getUserAccountsModel();
        } else {
            model = ontModelSelector.getABoxModel();
        }

        model.enterCriticalSection(Lock.WRITE);
        try {
            /* avoiding blank node issue for now.... */
            InputStream is = null;
            try {
                is = new ByteArrayInputStream(s.getBytes("UTF-8"));
            } catch (Exception ex) {
                log.error(ex);
            }
            Model im = ModelFactory.createDefaultModel();
            im.read(is, null, "TTL");

            String whichModel;

            if (acctsModel) {
                whichModel = "Accounts Model";
            } else {
                whichModel = "Public ABox Model";
            }

            //im.write(System.out, "TTL");
            model.add(im);

        } catch (Exception ex) {
            log.error(ex);
        } finally {
            model.leaveCriticalSection();
        }
    }

    /**
     * Update vivo with person's webid.
     *
     * @param request
     */
    public void updateVivoWithExternalWebid(HttpServletRequest request) {

        String namespace = this.getNamespace(request);

        UUID rand = UUID.randomUUID();
        String webidAssocURI = namespace + "n" + rand;

        rand = UUID.randomUUID();
        String uuid = namespace + "n" + rand;

        UserAccount userAccount = getCurrentUserAccount(request);

        StringBuffer sb = new StringBuffer();
        sb.append("@prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> .\n");
        sb.append("@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n");
        sb.append("<");
        sb.append(userAccount.getUri());
        sb.append("> ");
        sb.append(" auth:hasWebIDAssociation ");
        sb.append("<");
        sb.append(webidAssocURI);
        sb.append("> .\n");
        sb.append("<");
        sb.append(webidAssocURI);
        sb.append("> ");
        sb.append(" auth:hasWebID ");
        sb.append("<");
        sb.append(request.getParameter("txtWebId"));
        sb.append("> ;\n");
        sb.append(" auth:hasUUID ");
        sb.append("<");
        sb.append(uuid);
        sb.append("> ;\n");
        sb.append("auth:localHosted false ;\n");
        sb.append("auth:me true ;\n");
        sb.append("rdfs:label \"remote\" . \n");

        addIt(request, sb.toString(), true);

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
        String label = request.getParameter("label");

        String webid = request.getParameter("webid");

        String namespace = this.getNamespace(request);

        UUID rand = UUID.randomUUID();
        String webidAssocURI = namespace + "n" + rand;

        rand = UUID.randomUUID();
        String uuid = namespace + "n" + rand;

        rand = UUID.randomUUID();
        String key = namespace + "n" + rand;

        UserAccount userAccount = getCurrentUserAccount(request);

        StringBuffer sb = new StringBuffer();
        sb.append("@prefix cert: <http://www.w3.org/ns/auth/cert#> .\n");
        sb.append("@prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> .\n");
        sb.append("@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n");
        sb.append("<");
        sb.append(userAccount.getUri());
        sb.append("> ");
        sb.append(" auth:hasWebIDAssociation ");
        sb.append("<");
        sb.append(webidAssocURI);
        sb.append("> .\n");
        sb.append("<");
        sb.append(webidAssocURI);
        sb.append("> ");
        sb.append(" auth:hasWebID ");
        sb.append("<");
        sb.append(webid);
        sb.append("> ;\n");
        sb.append(" auth:localHosted true ;\n");
        sb.append(" auth:me true ;\n");
        sb.append(" rdfs:label \"");
        sb.append(label);
        sb.append("\" ;\n");
        sb.append(" auth:hasUUID ");
        sb.append("<");
        sb.append(uuid);
        sb.append("> ;\n");
        sb.append(" cert:key ");
        sb.append("<");
        sb.append(key);
        sb.append("> .\n");
        sb.append("<");
        sb.append(key);
        sb.append("> ");
        sb.append(" a cert:RSAPublicKey ;\n");
        sb.append(" cert:exponent ");
        sb.append(exponent);
        sb.append(";\n cert:modulus ");
        sb.append("\"");
        sb.append(modulus);
        sb.append("\"^^<http://www.w3.org/2001/XMLSchema#hexBinary> . \n");


        addIt(request, sb.toString(), true);

        StringBuffer str = new StringBuffer();


        str.append("@prefix cert: <http://www.w3.org/ns/auth/cert#> .\n");
        str.append("@prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> .\n");
        str.append("@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n");
        str.append("<");
        str.append(webid);
        str.append("> ");
        str.append(" cert:key ");
        str.append("<");
        str.append(key);
        str.append("> .\n");
        str.append("<");
        str.append(key);
        str.append("> ");
        str.append(" a cert:RSAPublicKey ;\n");
        str.append(" cert:exponent ");
        str.append(exponent);
        str.append(";\n cert:modulus ");
        str.append("\"");
        str.append(modulus);
        str.append("\"^^<http://www.w3.org/2001/XMLSchema#hexBinary> . \n");

        addIt(request, str.toString(), false);

    }

    /**
     * Update vivo with person's webid.
     *
     * @param request
     */
    public void updateVivoWith_BLANKNODE(HttpServletRequest request, X509Certificate cert) {

        RSAPublicKey certpublickey = (RSAPublicKey) cert.getPublicKey();
        String modulus = String.format("%0288x", certpublickey.getModulus());
        String exponent = String.valueOf(certpublickey.getPublicExponent());
        String label = request.getParameter("label");

        String webid = request.getParameter("webid");

        String namespace = this.getNamespace(request);
        UUID rand = UUID.randomUUID();
        String uuid = namespace + "n" + rand;

        UserAccount userAccount = getCurrentUserAccount(request);
        StringBuffer sb = new StringBuffer();
        sb.append("@prefix cert: <http://www.w3.org/ns/auth/cert#> .\n");
        sb.append("@prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> .\n");
        sb.append("@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n");
        sb.append("<");
        sb.append(userAccount.getUri());
        sb.append("> ");
        sb.append(" auth:hasWebIDAssociation ");
        sb.append("_:bnode .\n");
        sb.append("_:bnode ");
        sb.append(" auth:hasWebID ");
        sb.append("<");
        sb.append(webid);
        sb.append("> ;\n");
        sb.append(" auth:localHosted true ;\n");
        sb.append(" auth:me true ;\n");
        sb.append(" rdfs:label \"");
        sb.append(label);
        sb.append("\" ;\n");
        sb.append(" auth:hasUUID ");
        sb.append("<");
        sb.append(uuid);
        sb.append("> ;\n");
        sb.append(" cert:key ");
        sb.append("_:bnode1 .\n");
        sb.append("_:bnode1 ");
        sb.append(" a cert:RSAPublicKey ;\n");
        sb.append(" cert:exponent ");
        sb.append(exponent);
        sb.append(";\n cert:modulus ");
        sb.append("\"");
        sb.append(modulus);
        sb.append("\"^^<http://www.w3.org/2001/XMLSchema#hexBinary> . \n");


        addIt(request, sb.toString(), true);

        StringBuffer str = new StringBuffer();


        str.append("@prefix cert: <http://www.w3.org/ns/auth/cert#> .\n");
        str.append("@prefix auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> .\n");
        str.append("@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n");
        str.append("<");
        str.append(webid);
        str.append("> ");
        str.append(" cert:key ");
        str.append("_:bnode .\n");
        str.append("_:bnode ");
        str.append(" a cert:RSAPublicKey ;\n");
        str.append(" cert:exponent ");
        str.append(exponent);
        str.append(";\n cert:modulus ");
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
    public ArrayList<WebidAssociation> getWebIdList(HttpServletRequest request) {

        StringBuffer queryString = new StringBuffer();

        queryString.append("PREFIX  auth:  <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> \n");
        queryString.append("PREFIX  rdfs:  <http://www.w3.org/2000/01/rdf-schema#> \n");

        queryString.append("SELECT ?hasWebIDAssociation ?me ?webid ?localHosted ?uuid ?label \n");
        queryString.append("WHERE { \n");
        queryString.append("<");
        //queryString.append(getProfileUri(request));
        queryString.append(this.getCurrentUserAccount(request).getUri());
        queryString.append(">\n");

        queryString.append("auth:hasWebIDAssociation ?bnode . \n");
        queryString.append("?bnode auth:me ?me ; \n");
        queryString.append("auth:hasWebID ?webid ; \n");
        queryString.append("auth:localHosted ?localHosted ; \n");
        queryString.append("auth:hasUUID ?uuid ; \n");
        queryString.append("rdfs:label ?label . \n");
        queryString.append("}");

        ArrayList<WebidAssociation> webidList = new ArrayList();

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
                String uuid = (String) q.getResource("uuid").getURI();

                webidList.add(new WebidAssociation(me, webid, localHosted, label, uuid));

                // TODO: ADD ROLE.
                //out.println("<tr><td>" + really.getString() + "</td><td>Self-Edit</td></tr>");
            }
            qe.close();
        } finally {
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
        String profileURI = "";

        if (userAccount != null) {
            StringBuffer queryString = new StringBuffer();

            // Fetch Profile URI, by network ID.
            queryString.append("SELECT ?uri WHERE { ?uri <http://vivo.stonybrook.edu/ns#networkId> \"");
            queryString.append(userAccount.getExternalAuthId());
            queryString.append("\" . }");

            com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString.toString());

            OntModel ontModel = getOntModel(request);
            QueryExecution qe = QueryExecutionFactory.create(query, ontModel);

            ResultSet results = qe.execSelect();
            for (; results.hasNext();) {
                QuerySolution qsoln = results.nextSolution();
                Resource r = qsoln.getResource("uri");
                profileURI = r.getURI();

                if (profileURI.startsWith(defaultNamespace)) {
                    break;
                }
            }
            qe.close();
        } else {
            // Shouldn't happen, but...
            Logger.getLogger(WebidHelper.class.getName()).log(Level.SEVERE, "Null Profile.");
        }

        return profileURI;
    }


    /**
     * Delete selected webids.
     *
     * @param request
     * @throws IOException
     */
    protected void delete(HttpServletRequest request) throws IOException {

        VitroRequest vreq = new VitroRequest(request);
        UserAccount userAccount = LoginStatusBean.getCurrentUser(vreq);
        String userUri = userAccount.getUri();

        HttpSession session = ((HttpServletRequest) request).getSession(false);
        ServletContext ctx = session.getServletContext();
        OntModelSelector ontModelSelector = ModelContext.getOntModelSelector(ctx);

        OntModel userAccts = ontModelSelector.getUserAccountsModel();
        OntModel aboxModel = ontModelSelector.getABoxModel();

        // Get the IDs to delete
        String toDelete[] = request.getParameterValues("id");
        if (toDelete != null) {

            // Loop through and delete them
            for (int i = 0; i < toDelete.length; i++) {

                StringBuffer queryString = new StringBuffer();
                queryString.append("PREFIX  loc:  <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> \n");
                queryString.append("PREFIX cert: <http://www.w3.org/ns/auth/cert#> \n");
                queryString.append("DELETE \n");
                queryString.append("{ <");
                queryString.append(userUri);
                queryString.append("> loc:hasWebIDAssociation ?assoc . \n");
                queryString.append("    ?assoc loc:hasUUID <");
                queryString.append(toDelete[i]);
                queryString.append("> . \n");
                queryString.append("    ?assoc ?p ?o . \n");
                queryString.append("    ?assoc cert:key ?akey . \n");
                queryString.append("    ?akey ?pp ?oo . \n");
                queryString.append("}\n");
                queryString.append("WHERE \n");
                queryString.append("{ <");
                queryString.append(userUri);
                queryString.append("> loc:hasWebIDAssociation ?assoc . \n");
                queryString.append("    ?assoc loc:hasUUID <");
                queryString.append(toDelete[i]);
                queryString.append("> . \n");
                queryString.append("    ?assoc ?p ?o . \n");
                queryString.append(" OPTIONAL { ?assoc cert:key ?akey . \n");
                queryString.append("    ?akey ?pp ?oo . } \n");
                queryString.append("}\n");

                userAccts.enterCriticalSection(Lock.WRITE);
                try {
                    UpdateAction.parseExecute(queryString.toString(), userAccts);

                } catch (Exception ex) {
                    log.error(ex);
                } finally {
                    userAccts.leaveCriticalSection();
                }

                queryString = new StringBuffer();
                queryString.append("PREFIX cert: <http://www.w3.org/ns/auth/cert#> \n");
                queryString.append("DELETE {");
                queryString.append("?s cert:key <");
                queryString.append(toDelete[i]);
                queryString.append("> . \n<");
                queryString.append(toDelete[i]);
                queryString.append("> ?p ?o . \n");
                queryString.append("} \n");
                queryString.append("WHERE { \n");
                queryString.append("?s cert:key <");
                queryString.append(toDelete[i]);
                queryString.append("> . \n<");
                queryString.append(toDelete[i]);
                queryString.append("> ?p ?o . \n");
                queryString.append("} \n");

                aboxModel.enterCriticalSection(Lock.WRITE);
                try {
                    UpdateAction.parseExecute(queryString.toString(), aboxModel);

                } catch (Exception ex) {
                    log.error(ex);
                } finally {
                    aboxModel.leaveCriticalSection();
                }

            }
        }

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
            Logger.getLogger(WebidHelper.class.getName()).log(Level.SEVERE, "No WebappDaoFactory.");
            return null;
        }

        UserAccountsDao userAccountsDao = wadf.getUserAccountsDao();
        if (userAccountsDao == null) {
            Logger.getLogger(WebidHelper.class.getName()).log(Level.SEVERE, "No UserAccountsDao.");
            return null;
        }

        return userAccountsDao.getUserAccountByUri(s);

    }
}
