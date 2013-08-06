package edu.stonybrook.ai.webid4vivo;

/**
 * WebID bean.
 * 
 * @author Erich Bremer
 * @author Tammy DiPrima
 */
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.Lock;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class webid {

    private X509Certificate cert = null;
    private ArrayList<String> alts = new ArrayList();
    private String uri = null;
    private String modulus = null;
    private String exponent = null;
    private Model model = null;
    private static final Log log = LogFactory.getLog(webid.class);
    

    webid(X509Certificate cert) {
        this.cert = cert;
        RSAPublicKey certpublickey = (RSAPublicKey) cert.getPublicKey();
        modulus = String.format("%0288x", certpublickey.getModulus());
        exponent = String.valueOf(certpublickey.getPublicExponent());

        Collection altnames = null;
        try {
            altnames = cert.getSubjectAlternativeNames();
        } catch (CertificateParsingException ex) {
            log.error(ex);
        }

        Iterator itAltNames = altnames.iterator();
        while (itAltNames.hasNext()) {
            List extensionEntry = (List) itAltNames.next();
            Integer nameType = (Integer) extensionEntry.get(0);
            if (nameType.intValue() == 6) {
                String aname = (String) extensionEntry.get(1);
                alts.add(aname);
            }
        }

        if (alts.size() == 1) {
            uri = (String) alts.get(0);
        }
    }

    public boolean verified(HttpServletRequest request) {

        boolean result = false;
        String namespace = new WebidHelper().getNamespace(request);
        StringBuffer sb = new StringBuffer();
        sb.append("PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#> \n");
        sb.append("PREFIX cert: <http://www.w3.org/ns/auth/cert#> \n");
        sb.append("PREFIX auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> \n");

        if (uri.contains(namespace)) {
            sb.append("ASK { ?s auth:hasWebIDAssociation \n");
            sb.append("[ auth:hasWebID \n");
            sb.append("<");
            sb.append(uri);
            sb.append("> ; \n");
            sb.append("cert:key \n");
            sb.append("[ cert:modulus \"");
            sb.append(modulus);
            sb.append("\"^^xsd:hexBinary; \n");
            sb.append("cert:exponent ");
            sb.append(exponent);
            sb.append("; ] ] . }");
            
            HttpSession session = ((HttpServletRequest) request).getSession(false);
            ServletContext ctx = session.getServletContext();
            OntModelSelector ontModelSelector = ModelContext.getOntModelSelector(ctx);
            OntModel userAccts = ontModelSelector.getUserAccountsModel();
            
            // Enter a critical section. The application must call leaveCriticialSection.
            userAccts.enterCriticalSection(Lock.READ);
            
            try {
                com.hp.hpl.jena.query.Query query = QueryFactory.create(sb.toString());

                QueryExecution qe = QueryExecutionFactory.create(query, userAccts);
                result = qe.execAsk();
                qe.close();
            } catch (Exception ex) {
                log.error(ex);
            } finally {
                userAccts.leaveCriticalSection();
            }

        } else {
            sb.append("ASK { <");
            sb.append(uri);
            sb.append("> cert:key \n");
            sb.append("[ cert:modulus \"");
            sb.append(modulus);
            sb.append("\"^^xsd:hexBinary ; \n");
            sb.append("cert:exponent ");
            sb.append(exponent);
            sb.append("; ] . }");

            model = ModelFactory.createDefaultModel();
            model.read(uri, "RDF/XML");

            com.hp.hpl.jena.query.Query query = QueryFactory.create(sb.toString());

            QueryExecution qe = QueryExecutionFactory.create(query, model);
            result = qe.execAsk();
            qe.close();

        }

        return result;
    }

    public X509Certificate getCert() {
        return cert;
    }

    public Model getFOAF() {
        return model;
    }

    public String getURI() {
        return uri;
    }
}
