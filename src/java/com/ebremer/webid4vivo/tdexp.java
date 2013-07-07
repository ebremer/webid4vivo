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
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.String;
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
    private OntModel ontModel;
    private static final String NAMED_GRAPH = "http://vitro.mannlib.cornell.edu/default/vitro-kb-2";
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
        this.ontModel = this.vreq.getJenaOntModel();

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet tdexp</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Servlet tdexp</h1>");

        arqUpdate1();
        //readAndWrite(request, response, out);
        
        //out.println(getUserAccount(request, ""));

        out.println("</body>");
        out.println("</html>");
        out.close();

    }

    protected void arqUpdate1() {

        try {
            Dataset dataset = this.vreq.getDataset();
            GraphStore graphStore = GraphStoreFactory.create(dataset);

            StringBuffer sb = new StringBuffer();
            sb.append("INSERT DATA ");
            sb.append("{ GRAPH <http://vitro.mannlib.cornell.edu/default/vitro-kb-2> ");
            sb.append("{ ");
            
            // PUBLIC KEY
            sb.append("_:bnode153153856 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/auth/cert#RSAPublicKey>. ");
            // MODULUS
            sb.append("_:bnode153153856 <http://www.w3.org/ns/auth/cert#modulus> ");
            sb.append("\"C9A83116B7465E246622F0DEE7F8752886DE54D59B739DEA2FBE89B0A5AAEAD8B1FC7A58B7BE5248C4CFC6291AF7110C576E1931A50A29884F647735692966345475B15FFE529E24A912D37F2D101AB37EE201E35F38D05EC66B1A8D4B5C0043C05979AE532");
            sb.append("22D4C153905E1B63DCCC17893DE7E5FC66669A8983FE9A8E1B0E57916793E8E22C9C700D07A9B51A729A1852CFFBFE51F8E9A7E7CDD0C054531C3960C669290E93AAE529145E981B14F0E77CB5FC3D985E43DD6642802CF4F7DDE25EA9EFDD255CAB3786BA2C");
            sb.append("2263FA3D6D6F86BB48E9149F4D300ECF2661D05C2E19DE89FE6E7D8057232CBEE4746E9D705795905A8D0EBC99043C6261A36493B\"^^<http://www.w3.org/2001/XMLSchema#hexBinary>. ");
            // EXPONENT
            sb.append("_:bnode153153856 <http://www.w3.org/ns/auth/cert#exponent> \"65537\"^^<http://www.w3.org/2001/XMLSchema#integer>. ");
            // THE KEY (BLANK NODE)
            sb.append("<http://vivo.stonybrook.edu/individual/n1431> <http://www.w3.org/ns/auth/cert#key> _:bnode153153856. ");
            
            sb.append("}");
            sb.append("}");

            System.out.println(sb.toString());
            UpdateAction.parseExecute(sb.toString(), graphStore);
        } catch (Exception ex) {
            System.out.println("arqUpdate(): " + ex.toString());
        }


        /*
         // USE REQUEST OBJECT
        
         UpdateRequest request = UpdateFactory.create();
        
         request.add("DROP ALL")
         .add("CREATE GRAPH <http://example/g2>")
         .add("LOAD <file:etc/update-data.ttl> INTO <http://example/g2>");

         // And perform the operations.
         UpdateAction.execute(request, graphStore);
                 
         */

    }

    
    /**
     * Testing.
     * @param request
     * @return 
     */
    protected UserAccount getUserAccount(HttpServletRequest request, String somebody)
    {
        return Authenticator.getInstance(request).getAccountForExternalAuth(somebody);
    }
    
    /**
     * Prove that we can read from, and write to, the database.
     * @param request
     * @param response
     * @param out 
     */
    protected void readAndWrite(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
    {
        String uri = ERICH;
        String uri1 = TAMMY;
        String name = "";

        try {
            // Query
            name = m_query(uri);
            out.println("<p>");
            out.println("Simple query: Who is " + uri + "?<br>");
            out.println(name);
            out.println("</p>");
        } catch (Exception a) {
            System.out.println("a: " + a.toString());
        }


        boolean result_of_ASK;
        try {
            // Ask
            result_of_ASK = m_search(uri);
            out.println("<p>");
            out.println("ASK: Does anybody know " + name + "??<br>");
            out.println(result_of_ASK);
            out.println("</p>");
        } catch (Exception b) {
            System.out.println("b: " + b.toString());
        }

        // One of these methods gotta work!
        testInsertMethods(request, response, out);
        
        try {
            // Ask again to see if a record was entered
            // TODO: THIS RETURNS FALSE. WHY?
            result_of_ASK = m_search(uri);
            out.println("<p>");
            out.println("NOW, does anybody know Erich??<br>");
            out.println(result_of_ASK);
            out.println("</p>");
        } catch (Exception g) {
            System.out.println("g: " + g.toString());
        }        
        
        
    }

    /**
     * Test methods of insertion, figure out what works.
     * @param request
     * @param response
     * @param out 
     */
    protected void testInsertMethods(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
    {
        String uri = ERICH;
        String uri1 = TAMMY;
        
        // Insert
        out.println("<p>");
        //out.println("Inserting foaf:knows triples for Erich & Tammy<br>");
        out.println("Inserting foaf:name triples for Erich<br>");

        // TEST SET 1:
        try {
            out.println("Test alpha<br>");
            m_insert(uri, uri1);
        } catch (Exception c) {
            System.out.println("c: " + c.toString());
        }

        // TEST SET 2:
        try {
            out.println("Test beta<br>");
            addTripleToDefaultGraph(uri);
        } catch (Exception d) {
            System.out.println("d: " + d.toString());
        }

        // TEST SET 3:
        try {
            out.println("Test gamma<br>");
            vivoInsert(uri, uri1);
        } catch (Exception e) {
            System.out.println("e: " + e.toString());
        }

        try {
            // TEST SET 4:
            out.println("Test delta<br>");
            arqUpdate();

            out.println("</p>");
        } catch (Exception f) {
            System.out.println("f: " + f.toString());
        }

    }

    /**
     * DELTA WORKED.
     * ARQ - SPARQL Update.
     * http://jena.apache.org/documentation/query/update.html
     */
    protected void arqUpdate() {

        try {
            Dataset dataset = this.vreq.getDataset();
            GraphStore graphStore = GraphStoreFactory.create(dataset);

            // READ FROM FILE
            //UpdateAction.readExecute("update.ru", graphStore);

            // REQUEST AS STRING
            //UpdateAction.parseExecute("[DON'T] DROP ALL", graphStore);

            StringBuffer sb = new StringBuffer();
            sb.append("INSERT DATA ");
            sb.append("{ GRAPH <http://vitro.mannlib.cornell.edu/default/vitro-kb-2> ");
            //sb.append("{ <http://vivo.stonybrook.edu/individual/n1559> <http://xmlns.com/foaf/0.1/knows>  <http://vivo.stonybrook.edu/individual/n1431> . }");
            sb.append("{ <http://vivo.stonybrook.edu/individual/n1559> <http://xmlns.com/foaf/0.1/name>  \"delta\"^^<http://www.w3.org/2001/XMLSchema#string> . }");
            sb.append("}");

            System.out.println(sb.toString());
            UpdateAction.parseExecute(sb.toString(), graphStore);
        } catch (Exception ex) {
            System.out.println("arqUpdate(): " + ex.toString());
        }


        /*
         // USE REQUEST OBJECT
        
         UpdateRequest request = UpdateFactory.create();
        
         request.add("DROP ALL")
         .add("CREATE GRAPH <http://example/g2>")
         .add("LOAD <file:etc/update-data.ttl> INTO <http://example/g2>");

         // And perform the operations.
         UpdateAction.execute(request, graphStore);
                 
         */

    }

    /**
     * GAMMA WORKED.
     *
     * References: IndividualDaoJena.getExternalIds()
     * IndividualDaoJena.addVClass() RDFUploadController
     *
     * Reminders: TBox = ontology ABox = Instance data
     */
    protected void vivoInsert(String uri, String uri1) {
        String individualURI = BASE + uri;
        String individualURI1 = BASE + uri1;

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
        }

    }

    /**
     * Insert "tammy foaf:knows erich".
     * WORKS.
     * @param request 
     */
    protected void m_insert(HttpServletRequest request) {
        
        System.out.println("Insert a record...");
        
        
        try {

            // SO FAR WE HAVE ERICH FOAF:KNOWS TAMMY.
            // LET'S PUT TAMMY FOAF:KNOWS ERICH.
            
            // Using this model:
            OntModelSelector ontModelSelector = ModelContext.getOntModelSelector(getServletContext());
            OntModel model = ontModelSelector.getABoxModel();
            
            Resource erich = model.getResource("http://vivo.stonybrook.edu/individual/n1559");
            Resource tammy = model.getResource("http://vivo.stonybrook.edu/individual/n1431");

            // Create properties for the different types of relationships to represent
            Property hasCert = model.createProperty("http://www.w3.org/ns/auth/cert#","X509Certificate");

            // Create a Resource for each thing, identified by their URI
            Resource certResource = model.createResource("http://yabbadabbadoo1/foaf.me");
            
            // Create statements
            //Statement statement = model.createStatement(tammy,FOAF.knows,erich);
            Statement statement = model.createStatement(tammy,hasCert,certResource);

            // add the created statement to the model
            model.add(statement);            

        } catch (Exception idk) {
            System.out.println("m_insert(): " + idk.toString());
        }

    }

    /**
     * Prove: I can insert data. Insert "erich foaf:knows tammy".
     * http://jena.sourceforge.net/tutorial/RDF_API/
     */
    protected void m_insert(String uri, String uri1) {

        String personURI = BASE + uri;
        String personURI1 = BASE + uri1;

        try {
            Model model = ModelFactory.createDefaultModel();

            Resource person = model.createResource(personURI);
            person.addProperty(FOAF.name, "alpha");
            //Resource person1 = model.createResource(personURI1);

            //person.addProperty(FOAF.knows, person1);
            //person1.addProperty(FOAF.knows, person);

            model.write(System.out);

            ontModel.add(model);

        } catch (Exception idk) {
            System.out.println("m_insert(): " + idk.toString());
        }

    }

    
    /**
     * Insert Attempt #2.
     * http://pic.dhe.ibm.com/infocenter/db2luw/v10r1/topic/com.ibm.swg.im.dbclient.rdf.doc/doc/c0060617.html.
     */
    public void addTripleToDefaultGraph(String personURI) {

        try {
            System.out.println("addTripleToDefaultGraph");

            Dataset ds = this.vreq.getDataset();
            Model m = ds.getDefaultModel();

            // Adding via model
            m.begin();

            String fullName = "beta";
            Resource johnSmith = m.createResource(BASE + personURI);
            //Resource johnSmith = ontModel.createResource(personURI);
            johnSmith.addProperty(FOAF.name, fullName);

            m.commit();
            m.close();


        } catch (Exception ex) {
            System.out.println("addTripleToDefaultGraph(): " + ex.toString());
        }
    }

    /**
     * SELECT queries. do a simple query find a triple in this person's profile
     * http://jena.apache.org/documentation/query/app_api.html
     */
    protected String m_query(String uri) {
        String queryString = "SELECT ?name WHERE { <" + BASE + uri + "> <http://xmlns.com/foaf/0.1/firstName>  ?name }";
        com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString);

        String name = "";

        QueryExecution qe = QueryExecutionFactory.create(query, ontModel);

        ResultSet results = qe.execSelect();
        for (; results.hasNext();) {
            QuerySolution qsoln = results.nextSolution();
            Literal really = qsoln.getLiteral("name");
            name = really.getString();
        }
        qe.close();

        return name;

    }

    /**
     * ASK Queries. search the entire database ask and see if it returns true
     * http://jena.apache.org/documentation/query/app_api.html
     */
    protected boolean m_search(String uri) {
        // Ask, "Does anybody know [this person]??"
        StringBuffer sb = new StringBuffer();
        sb.append("PREFIX foaf:    <http://xmlns.com/foaf/0.1/> ");
        sb.append("ASK  { ?x foaf:knows  <" + BASE + uri + "> }");

        String queryString = sb.toString();
        System.out.println(queryString);

        com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString);

        QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
        boolean result = qe.execAsk();
        qe.close();

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