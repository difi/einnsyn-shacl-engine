package validation;

import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryResults;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import utils.Property;
import utils.SHACLEngineUtils;
import vocabulary.SHACL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by veronika on 4/28/16.
 *
 *
 */
public class SHACLValidator {

    static List<String> violations = new ArrayList<>();

    public static void parseShapePropertyConstraints(String shapesGraphLocation, String dataGraphLocation) throws IOException {

        RepositoryConnection shapesConnection = SHACLEngineUtils.parseRDFGraph(shapesGraphLocation, RDFFormat.TURTLE);
        RepositoryConnection dataConnection = SHACLEngineUtils.parseRDFGraph(dataGraphLocation, RDFFormat.TURTLE);

        List<MemValue> shapePredicates = getAllShapePredicates(shapesConnection);

        RepositoryResult<Statement> shapesResult = dataConnection.getStatements(null, null, null);
        Model dataGraphModel = QueryResults.asModel(shapesResult);

        // TODO: This forEach checks whether the data graph is containing all predicates as the shape.
        // TODO: That is not necessarily the point. Check on scopeClass instead.
        dataGraphModel.forEach(dataStatement -> {
            if (!shapePredicates.contains(dataStatement.getPredicate())) {
                violations.add(dataStatement.getPredicate().toString());
            }
        });

        parseShapePropertyConstraints(shapesConnection, dataGraphModel);

        if (violations.isEmpty()) {
            System.out.println("No violations detected. Nice work!");
        }
        else {
            System.out.println("** Violations detected! **\n");
            violations.forEach(System.out::println);
        }
    }


    private static List<MemValue> getAllShapePredicates(RepositoryConnection connection) {

        List<MemValue> predicates = new ArrayList<>();

        Model shapesModel = SHACLEngineUtils.repositoryConnectionToModel(connection);

        for (Statement s : shapesModel) {

            // If a scopeClass is present, then RDF.TYPE is added to the predicates list.
            // Every shape needs to contain a scopeClass, telling what kind of resource we're talking about.
            if (s.getPredicate().equals(SHACL.scopeClass)) {
                MemValue type = new MemIRI(null, RDF.NAMESPACE, RDF.TYPE.getLocalName());
                predicates.add(type);
            }

            if (s.getPredicate().equals(SHACL.property)) {

                MemBNode propertyNode = (MemBNode) s.getObject();
                MemStatementList list = propertyNode.getSubjectStatementList();

                for (int i = 0; i < list.size(); i++) {
                    MemStatement st = list.get(i);
                    if (st.getPredicate().equals(SHACL.predicate)) {
                        predicates.add(st.getObject());
                    }
                }
            }
        }
        return predicates;
    }

    private static void parseShapePropertyConstraints(RepositoryConnection shapesConnection, Model dataGraphModel) {

        Model shapesModel = SHACLEngineUtils.repositoryConnectionToModel(shapesConnection);

        List<MemStatementList> memStatementLists = new ArrayList<>();

        shapesModel.stream()
                .filter(s -> s.getPredicate().equals(SHACL.property))
                .forEach(s -> {
                    MemBNode propertyNode = (MemBNode) s.getObject();
                    MemStatementList list = propertyNode.getSubjectStatementList();
                    memStatementLists.add(list);
        });

        List<Property> properties = SHACLEngineUtils.createProperties(memStatementLists);
        validateDataGraph(properties, dataGraphModel);
    }

    /**
     *
     * @param properties List of property constaints from SHACL validation shape
     * @param dataGraphModel The data graph to be validated
     */
    private static void validateDataGraph(List<Property> properties, Model dataGraphModel) {

        // Validate sh:minCount
        for (Statement s : dataGraphModel) {
            properties.stream()
                    .filter(p -> p.getPredicate().equals(s.getPredicate()))
                    .filter(p -> p.getMinCount() == 1)
                    .filter(p -> !validateMinCount(dataGraphModel, p))
                    .forEach(p -> violations.add(
                            String.join("\n",
                                    "Object value: " + s.getObject().stringValue(),
                                    "Message: Data graph may only contain minimum one " + s.getPredicate().getLocalName() + "! ",
                                    "Severity: " + p.getSeverity(),
                                    "Focus node: " + s.getSubject() + ".\n"
                                    )
                    ));
        }

        // Validating sh:maxCount
        for (Statement s : dataGraphModel) {
            violations.addAll(properties.stream()
                    .filter(p -> p.getPredicate().equals(s.getPredicate()))
                    .filter(p -> p.getMaxCount() == 1)
                    .filter(p -> !validateMaxCount(dataGraphModel, p))
                    .map(p -> String.join("\n",
                            "Object value: " + s.getObject().stringValue(),
                            "Message: Data graph may only contain maximum one " + s.getPredicate().getLocalName() + "! ",
                            "Severity: " + p.getSeverity(),
                            "Focus node: " + s.getSubject() + ".\n"
                    )).collect(Collectors.toList()));
        }
    }

    /**
     *
     * Counts the number of occurrences of a given property in a data graph.
     * Data graphs validated against sh:minCount shall not have a number of that given predicate less than
     * sh:minCount states. If the minCount is 1, there shall be AT LEAST one of that predicate in the data graph.
     *
     * @param dataGraphModel data graph to be validated
     * @param SHACLProperty the given SHACL property
     * @return true if predicate occurrences is more than one
     */
    private static boolean validateMinCount(Model dataGraphModel, Property SHACLProperty) {

        List<IRI> predicates = dataGraphModel.stream()
                .filter(s -> s.getPredicate().equals(SHACLProperty.getPredicate()))
                .map(Statement::getPredicate).collect(Collectors.toList());

        return predicates.size() <= 1;
    }

    private static boolean validateMaxCount(Model dataGraphModel, Property SHACLProperty) {

        List<IRI> predicates = new ArrayList<>();

        dataGraphModel.stream()
                .filter(s -> s.getPredicate().equals(SHACLProperty.getPredicate()))
                .forEach(s -> predicates.add(s.getPredicate()));

        return predicates.size() <= 1;
    }
}
