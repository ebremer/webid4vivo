/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import javax.servlet.http.HttpServletRequest;

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
    public UserAccount getUserAccount(HttpServletRequest request) {
        VitroRequest vreq = new VitroRequest(request);
        return LoginStatusBean.getCurrentUser(vreq);
    }

    /**
     * Update vivo with person's webid.
     *
     * @param request
     */
    public void updateVivo(HttpServletRequest request, String webid) {
        VitroRequest vreq = new VitroRequest(request);

        UserAccount userAccount = LoginStatusBean.getCurrentUser(vreq);
        String email = userAccount.getEmailAddress();

        String profileURI = getProfileUri(request);

        try {
            Dataset dataset = vreq.getDataset();
            GraphStore graphStore = GraphStoreFactory.create(dataset);

            StringBuffer sb = new StringBuffer();
            sb.append("INSERT DATA ");
            sb.append("{ GRAPH <http://vitro.mannlib.cornell.edu/default/vitro-kb-2> ");
            sb.append("{ ");
            sb.append("<");
            sb.append(profileURI.trim());
            sb.append("> ");
            sb.append("<http://vivo.stonybrook.edu/ontology/vivo-sbu/webid>  \"");
            sb.append(webid.trim());
            sb.append("\"^^<http://www.w3.org/2001/XMLSchema#string> . }");
            sb.append(". }");

            System.out.println("INSERT DATA: " + sb);

            UpdateAction.parseExecute(sb.toString(), graphStore);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * You're logging in with WebID. I need your email so I can find your
     * UserAccount and log you in. TODO: EMAIL NEEDS TO BE MANDATORY.
     *
     * @param request
     * @param webid
     * @return
     */
    public String getEmail(HttpServletRequest request, String webid) {

        String email = "";

        // NOTE: FOR NOW WEBID IS STRING. LATER -- URI.
        StringBuffer queryString = new StringBuffer();
        queryString.append("SELECT ?s ?email WHERE { ");
        queryString.append("?s <http://vivo.stonybrook.edu/ontology/vivo-sbu/webid>  \"");
        queryString.append(webid.trim());
        queryString.append("\"^^<http://www.w3.org/2001/XMLSchema#string>; <http://vivoweb.org/ontology/core#primaryEmail> ?email . }");
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

    /**
     * I have a User Account. Now get Profile URI, by email address.
     *
     * @param request
     * @param response
     * @return
     */
    public String getProfileUri(HttpServletRequest request) {
        VitroRequest vreq = new VitroRequest(request);
        UserAccount userAccount = LoginStatusBean.getCurrentUser(vreq);
        String profileURI = "";

        if (userAccount != null) {
            String email = userAccount.getEmailAddress();

            StringBuffer queryString = new StringBuffer();
            queryString.append("SELECT ?s WHERE { ?s <http://vivoweb.org/ontology/core#primaryEmail> \"");
            queryString.append(email);
            queryString.append("\" . }");
            System.out.println("getProfileUri(): " + queryString);

            com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString.toString());

            OntModel ontModel = getOntModel(request);
            QueryExecution qe = QueryExecutionFactory.create(query, ontModel);

            ResultSet results = qe.execSelect();
            for (; results.hasNext();) {
                QuerySolution qsoln = results.nextSolution();
                Resource really = qsoln.getResource("s");
                profileURI = really.getURI();
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
}
