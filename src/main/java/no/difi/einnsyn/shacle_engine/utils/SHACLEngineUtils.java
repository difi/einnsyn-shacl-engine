package no.difi.einnsyn.shacle_engine.utils;

import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.query.QueryResults;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.memory.model.MemStatementList;
import no.difi.einnsyn.shacle_engine.vocabulary.SHACL;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by veronika on 4/28/16.
 *
 * Helper methods for parsing data.
 *
 */
public class SHACLEngineUtils {

    public static RepositoryConnection parseRDFGraph(String RDFGraph, RDFFormat format) throws IOException {

        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();
        final RepositoryConnection connection = repository.getConnection();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream stream = classLoader.getResourceAsStream(RDFGraph);

        RDFParser rdfParser = Rio.createParser(format);

        rdfParser.setRDFHandler(new StatementCollector(){
            @Override
            public void handleStatement(Statement st) {
                connection.add(st);
            }
        });
        rdfParser.parse(stream, RDFGraph);

        return connection;

    }

    public static void displayRepositoryContent(RepositoryConnection connection) {
        StringWriter stringWriter = new StringWriter();
        RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, stringWriter);

        writer.startRDF();
        RepositoryResult<Statement> statements = connection.getStatements(null, null, null);

        while(statements.hasNext()){
            writer.handleStatement(statements.next());
        }

        writer.endRDF();

        System.out.println(stringWriter.toString());
    }

    public static Model repositoryConnectionToModel(RepositoryConnection connection) {
        RepositoryResult<Statement> result = connection.getStatements(null, null, null);
        return QueryResults.asModel(result);
    }

    /**
     *
     * @param properties MemStatementList from the blank SHACL nodes
     * @return a list of Properties corresponding to the MemStatList
     */
    public static List<Property> createProperties(List<MemStatementList> properties) {

        List<Property> propertyList = new ArrayList<>();

        for (MemStatementList m : properties) {

            Property property = new Property();
            Map<IRI, Value> constraints = new HashMap<>();

            for (int i = 0; i < m.size(); i++) {
                Statement s = m.get(i);
                if (s.getPredicate().equals(SHACL.predicate)) {
                    property.setPredicate(s.getObject());
                }
                else {
                    constraints.put(s.getPredicate(), s.getObject());
                }
                property.setConstraints(constraints);
            }
            propertyList.add(property);
        }
        return propertyList;
    }
}
