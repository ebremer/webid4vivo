package com.ebremer.webid4vivo;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.Lock;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.String;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author tammydiprima
 */
public class tdexp extends HttpServlet {

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

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet tdexp</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Servlet tdexp</h1>");

        /*
        OntModel userAccts = ontModelSelector.getUserAccountsModel();

        OntModel userAccountsModel = ontModelFromContextAttribute(ctx, "userAccountsOntModel");

        OntModelSelectorImpl baseOms = new OntModelSelectorImpl();
        baseOms.setUserAccountsModel(userAccountsModel);
        */

        HttpSession session = ((HttpServletRequest) request).getSession(false);
        ServletContext ctx = session.getServletContext();
        OntModel userAccts = (OntModel) ctx.getAttribute("userAccountsOntModel");
        
        userAccts.enterCriticalSection(Lock.WRITE);

        StringBuffer sb = new StringBuffer();
        sb.append("@prefix cert: <http://www.w3.org/ns/auth/cert#> .\n");
        sb.append("@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n");
        sb.append("@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n");
        sb.append("@prefix rdfs: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n");
        sb.append("<http://larry3.example/profile#me> a foaf:Person .\n");
        sb.append("<http://larry3.example/profile#me> foaf:name \"Larry3\" .\n");
        sb.append("<http://larry3.example/profile#me> cert:key _:key .\n");
        sb.append("_:key a cert:RSAPublicKey .\n");
        sb.append("_:key rdfs:label \"tired and we want to go home\" .\n");
        sb.append("_:key cert:exponent 65537 .\n");
        out.println(sb.toString());
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
        userAccts.leaveCriticalSection();

        ctx.setAttribute("userAccountsOntModel", userAccts);
        
        
        //////////
        
        try {

            StringBuffer queryString = new StringBuffer();

            queryString.append("PREFIX  rdfs: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
            queryString.append("PREFIX  cert: <http://www.w3.org/ns/auth/cert#> \n");
            queryString.append("PREFIX  foaf: <http://xmlns.com/foaf/0.1/> \n");
            queryString.append("PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#> \n");

            queryString.append("SELECT ?name ?alabel ?anex \n");
            queryString.append("WHERE \n");
            queryString.append("{ <http://larry3.example/profile#me> rdfs:type foaf:Person . \n");
            queryString.append("<http://larry3.example/profile#me> foaf:name ?name . \n");
            queryString.append("<http://larry3.example/profile#me> cert:key ?thing . \n");
            queryString.append("?thing rdfs:type cert:RSAPublicKey . \n");
            queryString.append("?thing rdfs:label ?alabel . \n");
            queryString.append("?thing cert:exponent ?anex \n");
            queryString.append("}");


            out.println(queryString);

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
        
                
        out.println("</body>");
        out.println("</html>");
        out.close();

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