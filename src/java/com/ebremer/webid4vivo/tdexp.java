/**
 * READIN' WRITIN' TEST-BED.
 *
 * REMINDERS: TBox = ontology ABox = Instance data
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
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateAction;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.String;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author tammydiprima
 */
public class tdexp extends HttpServlet {

    private VitroRequest vreq;
    //private OntModel ontModel;
    private static final String PUBLIC_GRAPH = "http://vitro.mannlib.cornell.edu/default/vitro-kb-2";
    private static final String PRIVATE_GRAPH = "<http://vitro.mannlib.cornell.edu/default/vitro-kb-userAccounts>";
    private static final String BASE = "http://vivo.stonybrook.edu/individual/";
    private static final String ERICH = "n1559";
    private static final String TAMMY = "n1431";

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        this.vreq = new VitroRequest(request);
        //this.ontModel = this.vreq.getJenaOntModel();

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet tdexp</title>");
        out.println("</head>");
        out.println("<body>");

        delete1(out);

        out.println("</body>");
        out.println("</html>");
        out.close();

    }

    protected void delete1(PrintWriter out) throws ServletException, IOException {

        StringBuffer queryString = new StringBuffer();
        
        queryString.append("PREFIX  loc:  <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> \n");
        queryString.append("PREFIX cert: <http://www.w3.org/ns/auth/cert#> \n");
        queryString.append("DELETE \n");
        queryString.append("{ <http://vivo.stonybrook.edu/individual/u2315> loc:hasWebIDAssociation ?assoc . \n");
        queryString.append("    ?assoc ?p ?o . \n");
        queryString.append("    ?assoc cert:key ?akey . \n");
        queryString.append("    ?akey ?pp ?oo . \n");
        queryString.append("}\n");        
        queryString.append("WHERE \n");
        queryString.append("{ <http://vivo.stonybrook.edu/individual/u2315> loc:hasWebIDAssociation ?assoc . \n");
        queryString.append("    ?assoc ?p ?o . \n");
        queryString.append(" OPTIONAL { ?assoc cert:key ?akey . \n"); 
        queryString.append("    ?akey ?pp ?oo . } \n");
        queryString.append("}\n");         

        // deleted too much!
        /*
         queryString.append("PREFIX  loc:  <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> \n");
         queryString.append("PREFIX cert: <http://www.w3.org/ns/auth/cert#> \n");
         queryString.append("DELETE \n");
         queryString.append("WHERE \n");
         queryString.append("  { <http://vivo.stonybrook.edu/individual/u2315> loc:hasWebIDAssociation ?assoc . \n");
         //queryString.append("    ?assoc loc:hasUUID ?uuid .\n");
         queryString.append("    ?assoc ?p ?o . \n");
         queryString.append("    ?assoc cert:key ?key . \n");
         queryString.append("    ?key ?pp ?oo . \n");
         queryString.append("  } \n");*/

        /*
        queryString.append("PREFIX  loc:  <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> ");
        queryString.append("PREFIX cert: <http://www.w3.org/ns/auth/cert#> ");
        queryString.append("DELETE ");
        queryString.append("WHERE ");
        queryString.append("{ <http://vivo.stonybrook.edu/individual/u2315> loc:hasWebIDAssociation ?assoc . ");
        queryString.append("    ?assoc ?p ?o . ");
        queryString.append("    ?assoc cert:key ?akey . ");
        queryString.append("    ?akey ?pp ?oo . ");
        queryString.append("}");*/

        // This deletes everything up to, but not including, the public key:
        /*
         queryString.append("PREFIX  loc:  <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> \n");
         queryString.append("PREFIX cert: <http://www.w3.org/ns/auth/cert#> \n");
         queryString.append("DELETE \n");
         queryString.append("WHERE \n");
         queryString.append("  { <http://vivo.stonybrook.edu/individual/u2315> loc:hasWebIDAssociation ?bnode1 . \n");
         queryString.append("    ?bnode1 ?p ?o .\n");
         queryString.append("    ?bnode1 cert:key ?bnode2 . \n");
         queryString.append("    ?bnode2 ?pp ?oo .\n");
         queryString.append("  } \n");
         */

        // you can't use OPTIONAL with DELETE WHERE
        /*
         queryString.append("PREFIX  loc:  <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> \n");
         queryString.append("PREFIX cert: <http://www.w3.org/ns/auth/cert#> \n");
         queryString.append("DELETE { \n");
         queryString.append("GRAPH ?graph { ");
         queryString.append("  <http://vivo.stonybrook.edu/individual/u2315> loc:hasWebIDAssociation ?bnode . \n");
         queryString.append("    ?bnode ?p ?o .\n");
         queryString.append("    ?bnode cert:key ?key . \n");
         queryString.append("    ?key ?ppp ?ooo . \n");
         queryString.append("  } }\n");        
         queryString.append("WHERE { \n");
         queryString.append("GRAPH ?graph { ");
         queryString.append("  <http://vivo.stonybrook.edu/individual/u2315> loc:hasWebIDAssociation ?bnode . \n");
         queryString.append("    ?bnode ?p ?o .\n");
         queryString.append("    optional { ?bnode cert:key ?key . \n");
         queryString.append("    { ?key ?ppp ?ooo . } }  \n");
         queryString.append("  } } \n");
         */


        out.println("<pre>" + queryString.toString() + "</pre>");

        OntModelSelector ontModelSelector = ModelContext.getOntModelSelector(getServletContext());

        OntModel userAccts = ontModelSelector.getUserAccountsModel();
        //OntModel aboxModel = ontModelSelector.getABoxModel();

        userAccts.enterCriticalSection(Lock.WRITE);
        try {
            UpdateAction.parseExecute(queryString.toString(), userAccts);

        } catch (Exception ex) {
            out.println("delete1(): " + ex.toString());
        } finally {
            userAccts.leaveCriticalSection();
        }


    }

    /**
     * Need to use graph store. Getting the user accounts OntModel (or
     * userAccts.getGraph()) = error.
     *
     * @param request
     * @param out
     */
    protected void delete(HttpServletRequest request, PrintWriter out) {
        StringBuffer queryString = new StringBuffer();
        queryString.append("PREFIX  loc:  <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> \n");
        queryString.append("CONSTRUCT \n");
        queryString.append("  { <http://vivo.stonybrook.edu/individual/u2315> loc:hasWebidAssociation ?ass . \n");
        queryString.append("    ?ass ?pp ?oo \n");
        queryString.append("  } \n");
        queryString.append("WHERE \n");
        queryString.append("  { <http://vivo.stonybrook.edu/individual/u2315> loc:hasWebidAssociation ?ass . \n");
        queryString.append("    ?ass ?pp ?oo \n");
        queryString.append("  } \n");

        out.println(new java.util.Date() + " DELETE");
        out.println("<p>");
        out.println(queryString);
        out.println("</p>");

        RDFService rdfService = this.vreq.getRDFService();
        Model toRemove = ModelFactory.createDefaultModel();
        String fmQuery = queryString.toString();
        try {
            toRemove.read(rdfService.sparqlConstructQuery(fmQuery, RDFService.ModelSerializationFormat.RDFXML), null);
        } catch (RDFServiceException ex) {
            out.println("<p>");
            out.println(ex.toString());
            out.println("</p>");

        }

        ByteArrayOutputStream outRemove = new ByteArrayOutputStream();
        toRemove.write(outRemove);
        InputStream inRemove = new ByteArrayInputStream(outRemove.toByteArray());
        ChangeSet removeChangeSet = rdfService.manufactureChangeSet();
        removeChangeSet.addRemoval(inRemove, RDFService.ModelSerializationFormat.RDFXML, JenaDataSourceSetupBase.JENA_USER_ACCOUNTS_MODEL);//.JENA_DB_MODEL);
        try {
            rdfService.changeSetUpdate(removeChangeSet);
        } catch (RDFServiceException ex) {
            out.println("<p>");
            out.println(ex.toString());
            out.println("</p>");
        }


    }

    protected void writeAndRead(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws ServletException, IOException {
        //HttpSession session = ((HttpServletRequest) request).getSession(false);
        //ServletContext ctx = session.getServletContext();
        //OntModel userAccountsModel = ontModelFromContextAttribute(ctx, "userAccountsOntModel");
        //OntModel userAccts = (OntModel) ctx.getAttribute("userAccountsOntModel");
        //OntModelSelectorImpl baseOms = new OntModelSelectorImpl();
        //baseOms.setUserAccountsModel(userAccountsModel);

        StringBuffer sb = new StringBuffer();
        sb.append("@prefix cert: <http://www.w3.org/ns/auth/cert#> .\n");
        sb.append("@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n");
        sb.append("@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n");
        sb.append("@prefix rdfs: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n");
        sb.append("<http://larry4.example/profile#me> a foaf:Person .\n");
        sb.append("<http://larry4.example/profile#me> foaf:name \"larry4\" .\n");
        sb.append("<http://larry4.example/profile#me> cert:key _:key .\n");
        sb.append("_:key a cert:RSAPublicKey .\n");
        sb.append("_:key rdfs:label \"tired and we want to go home\" .\n");
        sb.append("_:key cert:exponent 65537 .\n");
        out.println("<pre>" + sb.toString() + "</pre>");


        OntModelSelector ontModelSelector = ModelContext.getOntModelSelector(getServletContext());
        OntModel userAccts = ontModelSelector.getUserAccountsModel();
        userAccts.enterCriticalSection(Lock.WRITE);
        try {
            /* avoiding blank node issue for now.... */
            InputStream is = null;
            try {
                is = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                out.println(ex.toString());
            }
            Model im = ModelFactory.createDefaultModel();
            im.read(is, null, "TTL");
            im.write(out, "TTL");

            userAccts.add(im);
        } catch (Exception ex) {
            System.out.println("vivoInsert(): " + ex.toString());
        } finally {
            //aboxModel.getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),false,individualURI));
            userAccts.leaveCriticalSection();
        }

        //ctx.setAttribute("userAccountsOntModel", userAccts);


        //////////

        try {

            StringBuffer queryString = new StringBuffer();

            queryString.append("PREFIX  rdfs: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
            queryString.append("PREFIX  cert: <http://www.w3.org/ns/auth/cert#> \n");
            queryString.append("PREFIX  foaf: <http://xmlns.com/foaf/0.1/> \n");
            queryString.append("PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#> \n");

            queryString.append("SELECT ?name ?alabel ?anex \n");
            queryString.append("WHERE \n");
            queryString.append("{ <http://larry4.example/profile#me> rdfs:type foaf:Person . \n");
            queryString.append("<http://larry4.example/profile#me> foaf:name ?name . \n");
            queryString.append("<http://larry4.example/profile#me> cert:key ?thing . \n");
            queryString.append("?thing rdfs:type cert:RSAPublicKey . \n");
            queryString.append("?thing rdfs:label ?alabel . \n");
            queryString.append("?thing cert:exponent ?anex \n");
            queryString.append("}");


            out.println("<pre>" + sb.toString() + "</pre>");

            com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString.toString());

            QueryExecution qe = QueryExecutionFactory.create(query, userAccts);

            ResultSet results = qe.execSelect();
            for (; results.hasNext();) {
                out.println("HERE.");
                QuerySolution qsoln = results.nextSolution();

                Literal l = qsoln.getLiteral("alabel");
                out.println((String) l.getString());

                l = qsoln.getLiteral("name");
                out.println((String) l.getString());

            }
            qe.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void addIt() {

        try {
            Dataset dataset = vreq.getDataset();

            //Model im = vreq.getJenaOntModel();
            Model im = ModelFactory.createDefaultModel();
            Iterator i = dataset.listNames();
            System.out.println("The Dataset contains the following graphs...");
            //OntModel mm = vreq.getJenaOntModel();
            // System.out.println("before : "+mm.size());
            while (i.hasNext()) {
                System.out.println((String) i.next());
            }
            GraphStore graphStore = GraphStoreFactory.create(dataset);
            /* Something bizzare going on with VIVO/Jena/SDB and blank nodes, circle back later.... -eb 
             * StringBuffer sb = new StringBuffer();
             sb.append("PREFIX cert: <http://www.w3.org/ns/auth/cert#>\n");
             sb.append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n");
             sb.append("PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n");
             sb.append("PREFIX rdfs: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
             sb.append("INSERT DATA INTO <" + PUBLIC_GRAPH + ">\n");
             sb.append("{ \n");
             sb.append("<http://larry.example/profile#me> a foaf:Person;\n");
             sb.append("foaf:name \"Larry\";\n");
             sb.append("cert:key <http://larry.example/node1> .\n");

             sb.append("<http://larry.example/node1> a cert:RSAPublicKey;\n");
             sb.append("rdfs:label \"tired and we want to go home\";\n");
             sb.append("cert:exponent 65537 .\n");
             sb.append("} \n");
             */
            StringBuffer sb = new StringBuffer();
            sb.append("@prefix cert: <http://www.w3.org/ns/auth/cert#> .\n");
            sb.append("@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n");
            sb.append("@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n");
            sb.append("@prefix rdfs: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n");
            sb.append("<http://larry2.example/profile#me> a foaf:Person .\n");
            sb.append("<http://larry2.example/profile#me> foaf:name \"Larry2\" .\n");
            sb.append("<http://larry2.example/profile#me> cert:key _:key .\n");
            sb.append("_:key a cert:RSAPublicKey .\n");
            sb.append("_:key rdfs:label \"tired and we want to go home\" .\n");
            sb.append("_:key cert:exponent 65537 .\n");
            System.out.println(sb.toString());
            /* avoiding blank node issue for now.... */
            InputStream is = null;
            try {
                is = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                System.out.println(ex.toString());
            }
            System.out.println("importing...");
            //im.read(is,"" + PUBLIC_GRAPH + "", "TTL");
            im.read(is, null, "TTL");
            im.write(System.out, "TTL");
            System.out.println("adding triples to kb 2...");
            Model e = dataset.getNamedModel(PUBLIC_GRAPH);
            e.add(im);
            //UpdateAction.parseExecute(sb.toString(), this.ontModel);
            // UpdateAction.parseExecute(sb.toString(), graphStore);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Prove that we can read from, and write to, the database.
     *
     * @param request
     * @param response
     * @param out
     */
    protected void readAndWrite(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        String uri = ERICH;
        String uri1 = TAMMY;
        String name = "";

        m_insert1(request);

        boolean result_of_ASK;
        String ask = "ASK  { ?x <http://xmlns.com/foaf/0.1/knows>  <" + BASE + uri + "> }";
        out.println("<code>" + ask + "</code>");
        out.println("<br>");
        try {
            // Ask
            result_of_ASK = m_search(ask);
            out.println("<p>");
            out.println("ASK: Does anybody know " + uri + "??<br>");
            out.println(result_of_ASK);
            out.println("</p>");
        } catch (Exception b) {
            out.println("b: " + b.toString());
            out.println("<br>");
        }

        testInsertMethods(request, response, out);

        m_insert(request);

        try {
            String queryString = "SELECT ?name WHERE { <" + BASE + uri + "> <http://xmlns.com/foaf/0.1/name>  ?name }";
            out.println("<code>" + queryString + "</code><br>");
            name = m_query(queryString);
            out.println("<p>");
            out.println("Did the insert methods work?<br>");
            out.println(name);
            out.println("</p>");
        } catch (Exception a) {
            out.println("zyzz: " + a.toString());
            out.println("<br>");
        }


        try {
            // Query
            String queryString = "SELECT ?cert WHERE { <" + BASE + uri1 + "> <http://www.w3.org/ns/auth/cert#X509Certificate>  ?cert }";
            out.println("<code>" + queryString + "</code><br>");
            name = m_query1(queryString);
            out.println("<p>");
            out.println("Did we insert a X509 Certificate?<br>");
            out.println(name);
            out.println("</p>");
        } catch (Exception a) {
            out.println("zyzz: " + a.toString());
            out.println("<br>");
        }

    }

    /**
     * Test methods of insertion, figure out what works.
     *
     * @param request
     * @param response
     * @param out
     */
    protected void testInsertMethods(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        String uri = ERICH;
        String uri1 = TAMMY;

        // Insert
        out.println("<p>");

        // TEST SET 1:
        try {
            out.println("Test alpha<br>");
            m_insert(uri);
        } catch (Exception c) {
            System.out.println("alpha: " + c.toString());
        }

        // TEST SET 2:
        try {
            out.println("Test beta<br>");
            addTripleToDefaultGraph(uri);
        } catch (Exception d) {
            System.out.println("beta: " + d.toString());
        }

        // TEST SET 3:
        try {
            out.println("Test gamma<br>");
            vivoInsert(uri, uri1);
        } catch (Exception e) {
            System.out.println("gamma: " + e.toString());
        }

        try {
            // TEST SET 4:
            out.println("Test delta<br>");
            arqUpdate();

            out.println("</p>");
        } catch (Exception f) {
            System.out.println("delta: " + f.toString());
        }

    }

    /**
     * USE GRAPHSTORE TO UPDATE DATA TO NAMED GRAPH.
     * http://jena.apache.org/documentation/query/update.html
     */
    protected void arqUpdate() {

        try {
            Dataset dataset = this.vreq.getDataset();
            GraphStore graphStore = GraphStoreFactory.create(dataset);

            StringBuffer sb = new StringBuffer();
            sb.append("INSERT DATA ");
            sb.append("{ GRAPH <" + PUBLIC_GRAPH + "> ");
            //sb.append("{ <" + BASE + "n1559> <http://xmlns.com/foaf/0.1/knows>  <" + BASE + "n1431> . }");
            sb.append("{ <" + BASE + "n1559> <http://xmlns.com/foaf/0.1/name>  \"delta\"^^<http://www.w3.org/2001/XMLSchema#string> . }");
            sb.append("}");

            System.out.println(sb.toString());
            UpdateAction.parseExecute(sb.toString(), graphStore);
        } catch (Exception ex) {
            System.out.println("arqUpdate(): " + ex.toString());
        }

    }

    protected void arqDelete(PrintWriter out) {

        Dataset dataset = this.vreq.getDataset();
        GraphStore graphStore = GraphStoreFactory.create(dataset);

        StringBuffer sb = new StringBuffer();

        //

        // WORKS ON MAIN GRAPH
            /*
         sb.append("DELETE { \n");
         sb.append("  GRAPH <" + PUBLIC_GRAPH + "> { \n");
         sb.append("    <http://vivo.stonybrook.edu/individual/n1431> <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#hasWebIDAssociation> <http://vivo.stonybrook.edu/individual/n884392184> . \n");
         sb.append("  } \n");
         sb.append("} \n");
         sb.append("USING <" + PUBLIC_GRAPH + "> \n");
         sb.append("WHERE \n");
         sb.append("  { <http://vivo.stonybrook.edu/individual/n1431> <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#hasWebIDAssociation> <http://vivo.stonybrook.edu/individual/n884392184> } \n");
         */

        // USER ACCOUNTS GRAPH DID NOT WORK
            /*
         sb.append("DELETE { \n");
         sb.append("  GRAPH <http://vitro.mannlib.cornell.edu/default/vitro-kb-userAccounts> { \n");
         sb.append("    <http://vivo.stonybrook.edu/individual/u2315> <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#hasWebIDAssociation> ?var . \n");
         sb.append("  } \n");
         sb.append("} \n");
         sb.append("USING <http://vitro.mannlib.cornell.edu/default/vitro-kb-userAccounts> \n");
         sb.append("WHERE \n");
         sb.append("  { <http://vivo.stonybrook.edu/individual/u2315> <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#hasWebIDAssociation> ?var } \n");
         */

        // DELETE & INSERT DIDN'T WORK:
        // "Graph selection from the dataset not supported - ignored"
        // DELETE { GRAPH <g1> { a b c } } INSERT { GRAPH <g1> { x y z } } USING <g1> WHERE { ... }

        /*
         sb.append("prefix xsd: <http://www.w3.org/2001/XMLSchema#string> \n");
         sb.append("prefix loc: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> \n");
         sb.append("PREFIX g:  <http://vitro.mannlib.cornell.edu/default/> \n");
         sb.append("DELETE { \n");
         sb.append("GRAPH g:vitro-kb-userAccounts { \n");
         sb.append("<http://vivo.stonybrook.edu/individual/u2315> loc:emailAddress \"tammy.diprima@stonybrook.edu\"^^xsd:string  } } \n");

         sb.append("INSERT { \n");
         sb.append("GRAPH g:vitro-kb-userAccounts { \n");
         sb.append("<http://vivo.stonybrook.edu/individual/u2315> loc:emailAddress \"tammy@yabbadabbadoo.edu\"^^xsd:string  } } \n");

         sb.append("USING g:vitro-kb-userAccounts \n");

         sb.append("WHERE { \n");
         sb.append("<http://vivo.stonybrook.edu/individual/u2315> loc:emailAddress \"tammy.diprima@stonybrook.edu\"^^xsd:string } \n");


         out.println("<p>Using vitro-kb-userAccounts.<br>");
         out.println(sb.toString());
         out.println("</p><p>");
         out.println(graphStore.getDefaultGraph().toString());
         out.println("</p>");

         UpdateAction.parseExecute(sb.toString(), graphStore);
         */


        // THIS JUST MADE VIVO CRAP OUT.
            /*
         sb.append("@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> . \n");
         sb.append("@prefix cert:    <http://www.w3.org/ns/auth/cert#> . \n");
         sb.append("@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> . \n");
         sb.append("<http://vivo.stonybrook.edu/individual/u2315> \n");
         sb.append("      <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#hasWebIDAssociation> \n");
         sb.append("              [ rdfs:label \"home\" ; \n");
         sb.append("                <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#hasWebID> \n");
         sb.append("                        <http://vivo.stonybrook.edu/individual/n1431> ; \n");
         sb.append("                <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#localHosted> \n");
         sb.append("                        \"true\"^^xsd:boolean ; \n");
         sb.append("                <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#me> \n");
         sb.append("                        \"true\"^^xsd:boolean ; \n");
         sb.append("                cert:key \n");
         sb.append("                        [ a       cert:RSAPublicKey ; \n");
         sb.append("                          cert:exponent 65537 ; \n");
         sb.append("                          cert:modulus \"cb84bb1d884348cc315c177602752874a9e6286077f4dfa3ed3f8344b01598eb203b233a456dedfbb14bbc92bcf22bdf568d912dc86f7adf9b85d8060d07f4ce797753d54a9a954c2cca12d464f5076acd05bcc989bda8f914c1b6c9030702a7d21db6efb199910843bd19373ab1abc2de634cbeee4b0d2ca4e421b6adb461bfdd6f9ce5be7e31de6f6e2fc515e4c51de53467de84237bd803bacea676c2fee8afabc5025815268cd16db2f7fbf42bdb888e95a9afa0c10855cd7bf5e212e8bbfaba943747f3aa4e7aa5ec93242cbdd680e6707588472cc1a36ac365c5cb10a3cbc1c920f6c3ac2c513cee15a41210ef21284ac3116da7a1ea5fda4734559353\"^^xsd:hexBinary \n");
         sb.append("                        ] \n");
         sb.append("              ] . \n");

         */

        OntModelSelector ontModelSelector = ModelContext.getOntModelSelector(getServletContext());
        OntModel userAccts = ontModelSelector.getUserAccountsModel();
        List blah = userAccts.getSubGraphs();
        out.println(blah.toString());
        out.println("<br>");
        out.println(blah.toArray());



        /*
         userAccts.enterCriticalSection(Lock.WRITE);
         try {
         InputStream is = null;
         try {
         is = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
         } catch (UnsupportedEncodingException ex) {
         out.println(ex.toString());
         }
         Model im = ModelFactory.createDefaultModel();
         im.read(is, null, "TTL");
         im.write(out, "TTL");

         userAccts.remove(im);

         } catch (Exception ex) {
         out.println("<br>arqDelete(): " + ex.toString());
         out.println("<br>");
         ex.printStackTrace(out);
         }*/

    }

    /**
     * GET RESOURCE. ADD RESOURCE-PREDICATE-LITERAL TO MODEL.
     *
     * References: IndividualDaoJena.getExternalIds()
     * IndividualDaoJena.addVClass() RDFUploadController
     *
     * Reminders: TBox = ontology ABox = Instance data
     */
    protected void vivoInsert(String uri, String uri1) {
        String individualURI = BASE + uri;

        OntModelSelector ontModelSelector = ModelContext.getOntModelSelector(getServletContext());

        OntModel aboxModel = ontModelSelector.getABoxModel();
        aboxModel.enterCriticalSection(Lock.WRITE);
        //ontModel.getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),true,individualURI));
        try {
            Resource indRes = aboxModel.getResource(individualURI);
            //aboxModel.add(indRes, FOAF.knows, aboxModel.getResource(individualURI1));
            aboxModel.add(indRes, FOAF.name, "gamma");
            //aboxModel.commit(); //<== java.lang.UnsupportedOperationException: this model does not support transactions
            //System.out.println("commit failed... again."); // PROBABLY EXPLAINS WHY I NEVER SEE VIVO "COMMIT()"
            //updatePropertyDateTimeValue(indRes, MODTIME, Calendar.getInstance().getTime(),ontModel);
        } catch (Exception ex) {
            System.out.println("vivoInsert(): " + ex.toString());
        } finally {
            //aboxModel.getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),false,individualURI));
            aboxModel.leaveCriticalSection();
            //aboxModel.close();
        }

    }

    /**
     * Insert "tammy foaf:knows erich". WORKS.
     *
     * @param request
     */
    protected void m_insert1(HttpServletRequest request) {

        System.out.println("Insert a record...");
        OntModelSelector ontModelSelector = ModelContext.getOntModelSelector(getServletContext());
        OntModel model = ontModelSelector.getABoxModel();

        model.enterCriticalSection(Lock.WRITE);

        try {

            Resource erich = model.getResource(BASE + "n1559");
            Resource tammy = model.getResource(BASE + "n1431");

            // Create statements
            Statement statement = model.createStatement(tammy, FOAF.knows, erich);

            // add the created statement to the model
            model.add(statement);

        } finally {
            model.leaveCriticalSection();
            //model.close();
        }

    }

    /**
     * Insert resource from auth ontology.
     *
     * @param request
     */
    protected void m_insert(HttpServletRequest request) {

        System.out.println("Insert a record...");
        OntModelSelector ontModelSelector = ModelContext.getOntModelSelector(getServletContext());
        OntModel model = ontModelSelector.getABoxModel();

        model.enterCriticalSection(Lock.WRITE);

        try {

            Resource erich = model.getResource(BASE + "n1559");
            Resource tammy = model.getResource(BASE + "n1431");

            // Create properties for the different types of relationships to represent
            Property hasCert = model.createProperty("http://www.w3.org/ns/auth/cert#", "X509Certificate");

            // Create a Resource for each thing, identified by their URI
            Resource certResource = model.createResource("http://yabbadabbadoo1/foaf.me");

            // Create statements
            Statement statement = model.createStatement(tammy, hasCert, certResource);

            // add the created statement to the model
            model.add(statement);

        } finally {
            model.leaveCriticalSection();
            //model.close();
        }

    }

    /**
     * GET RESOURCE FROM MODEL. ADD DATA TO RESOURCE (THUS UPDATING THE MODEL).
     * http://jena.sourceforge.net/tutorial/RDF_API/
     */
    protected void m_insert(String uri) {

        String personURI = BASE + uri;
        OntModelSelector ontModelSelector = ModelContext.getOntModelSelector(getServletContext());
        OntModel aboxModel = ontModelSelector.getABoxModel();

        Model model = ModelFactory.createDefaultModel();
        System.out.println("m_insert()");
        System.out.println("b4 - model.size(): " + model.size());


        aboxModel.enterCriticalSection(Lock.WRITE);

        try {


            Resource person = model.getResource(personURI);
            person.addProperty(FOAF.name, "alpha");

            System.out.println("aft - model.size(): " + model.size());

            //model.write(System.out);
            //aboxModel.add(model);


        } catch (Exception idk) {
            System.out.println("m_insert(): " + idk.toString());
        } finally {
            aboxModel.leaveCriticalSection();
            //aboxModel.close();
        }

    }

    /**
     * USING DATASET.
     * http://pic.dhe.ibm.com/infocenter/db2luw/v10r1/topic/com.ibm.swg.im.dbclient.rdf.doc/doc/c0060617.html.
     */
    public void addTripleToDefaultGraph(String personURI) {

        try {
            Dataset ds = this.vreq.getDataset();
            Model m = ds.getNamedModel(PUBLIC_GRAPH);//ds.getDefaultModel();
            m.enterCriticalSection(Lock.WRITE);

            try {

                if (m == null) {
                    System.out.println("model is null");
                }

                String fullName = "beta";
                Resource johnSmith = m.createResource(BASE + personURI); // create (existing) resource works!
                //Resource johnSmith = ontModel.createResource(personURI);
                johnSmith.addProperty(FOAF.name, fullName);

            } finally {
                m.leaveCriticalSection();
            }

        } catch (Exception ex) {
            System.out.println("addTripleToDefaultGraph(): " + ex.toString());
        }
    }

    /**
     * SELECT queries. do a simple query find a triple in this person's profile
     * http://jena.apache.org/documentation/query/app_api.html
     */
    protected String m_query(String queryString) {
        com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString);
        OntModelSelector ontModelSelector = ModelContext.getOntModelSelector(getServletContext());

        OntModel aboxModel = ontModelSelector.getABoxModel();

        // Enter a critical section. The application must call leaveCriticialSection.
        aboxModel.enterCriticalSection(Lock.READ);

        String name = "";

        try {
            QueryExecution qe = QueryExecutionFactory.create(query, aboxModel);

            ResultSet results = qe.execSelect();
            for (; results.hasNext();) {
                QuerySolution qsoln = results.nextSolution();
                Literal really = qsoln.getLiteral("name");
                name = name + " " + really.getString();
            }
            qe.close();
        } finally {
            // Releases the lock from the matching enterCriticalSection.
            aboxModel.leaveCriticalSection();
            //aboxModel.close();
        }

        return name;

    }

    /**
     * SELECT queries. do a simple query find a triple in this person's profile
     * http://jena.apache.org/documentation/query/app_api.html
     */
    protected String m_query1(String queryString) {
        com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString);

        String name = "";
        OntModelSelector ontModelSelector = ModelContext.getOntModelSelector(getServletContext());
        OntModel aboxModel = ontModelSelector.getABoxModel();
        aboxModel.enterCriticalSection(Lock.READ);
        try {
            QueryExecution qe = QueryExecutionFactory.create(query, aboxModel);

            ResultSet results = qe.execSelect();
            for (; results.hasNext();) {
                QuerySolution q = results.nextSolution();
                Resource r = q.getResource("cert");
                name = r.getURI();
            }
            qe.close();
        } finally {
            aboxModel.leaveCriticalSection();
            //aboxModel.close();
        }


        return name;

    }

    /**
     * ASK Queries. search the entire database ask and see if it returns true
     * http://jena.apache.org/documentation/query/app_api.html
     */
    protected boolean m_search(String queryString) {
        System.out.println(queryString);
        OntModelSelector ontModelSelector = ModelContext.getOntModelSelector(getServletContext());
        OntModel aboxModel = ontModelSelector.getABoxModel();
        aboxModel.enterCriticalSection(Lock.READ);
        boolean result = false;
        try {
            com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString);

            QueryExecution qe = QueryExecutionFactory.create(query, aboxModel);
            result = qe.execAsk();
            qe.close();
        } catch (Exception ex) {
            System.out.println("ask failed");
            System.out.println(ex.toString());
        } finally {
            aboxModel.leaveCriticalSection();
            //aboxModel.close();           
        }

        return result;

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}