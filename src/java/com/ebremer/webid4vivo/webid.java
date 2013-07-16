package com.ebremer.webid4vivo;

/**
 *
 * @author Erich Bremer
 * @author Tammy DiPrima
 */
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class webid {

    private X509Certificate cert = null;
    private ArrayList<String> alts = new ArrayList();
    private String uri = null;
    private String modulus = null;
    private String exponent = null;
    private Model model = null;

    webid(X509Certificate cert) {
        this.cert = cert;
        RSAPublicKey certpublickey = (RSAPublicKey) cert.getPublicKey();
        modulus = String.format("%0288x", certpublickey.getModulus());
        exponent = String.valueOf(certpublickey.getPublicExponent());

        Collection altnames = null;
        try {
            altnames = cert.getSubjectAlternativeNames();
        } catch (CertificateParsingException ex) {
            System.out.println(ex.toString());
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

    public String getSparqlQuery() {
        // ASK {?webid :key [ :modulus ?mod; :exponent ?exp; ] .}

        StringBuffer sp = new StringBuffer();

        /*
         sp.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ");
         sp.append("PREFIX cert: <http://www.w3.org/ns/auth/cert#> ");
         sp.append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> ");
         sp.append("PREFIX auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> ");
         sp.append("ASK {<");
         sp.append(uri);
         sp.append("> ");
         sp.append("cert:key [ cert:modulus \"");
         sp.append(modulus);
         sp.append("\"^^xsd:hexBinary;");
         sp.append("cert:exponent ");
         sp.append(exponent);
         sp.append("; ] .}");
         */

        sp.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ");
        sp.append("PREFIX cert: <http://www.w3.org/ns/auth/cert#> ");
        sp.append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> ");
        sp.append("PREFIX auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> ");
        sp.append("ASK { ?s auth:hasWebIDAssociation [ auth:hasWebID \n");
        sp.append("<");
        sp.append(uri);
        sp.append("> ; ");
        sp.append("cert:key [ cert:modulus \"");
        sp.append(modulus);
        sp.append("\"^^xsd:hexBinary;");
        sp.append("cert:exponent ");
        sp.append(exponent);
        sp.append("; ] ] .}");

        return sp.toString();

    }

    public boolean verified() {
        model = ModelFactory.createDefaultModel();
        model.read(uri, "RDF/XML");

        com.hp.hpl.jena.query.Query query = QueryFactory.create(getSparqlQuery());

        QueryExecution qe = QueryExecutionFactory.create(query, model);
        if (qe.execAsk()) {
            return true;
        } else {
            return false;
        }
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
