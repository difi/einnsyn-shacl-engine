package no.difi.einnsyn.shacle_engine.validation;

import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.shacle_engine.rules.Shape;
import no.difi.einnsyn.shacle_engine.utils.Property;
import no.difi.einnsyn.shacle_engine.utils.SHACLEngineUtils;
import no.difi.einnsyn.shacle_engine.violations.ConstraintViolationHandler;
import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryResults;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.memory.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by veronika on 4/28/16.
 */
public class SHACLValidator {

    List<String> violations = new ArrayList<>();

    List<Shape> shapes;

    Repository ontology;

    public SHACLValidator(Repository shaclRules, Repository ontology) {
        if(shaclRules == null){
            return;
        }

        try (RepositoryConnection shapesConnection = shaclRules.getConnection()) {
            RepositoryResult<Statement> statements = shapesConnection.getStatements(null, RDF.TYPE, SHACL.Shape);

            shapes = QueryResults.stream(statements)
                .map(statement -> new Shape(statement.getSubject(), shapesConnection))
                .collect(Collectors.toList());

        }

        this.ontology = ontology;

    }

    public boolean validate(Repository data, ConstraintViolationHandler constraintViolationHandler) {

        if (shapes == null || data == null) {
            return false;
        }

        if (ontology != null) {
            Repository inferrencedRepository = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
            inferrencedRepository.initialize();

            try (RepositoryConnection inferrencedConnection = inferrencedRepository.getConnection()) {
                try (RepositoryConnection dataConnection = data.getConnection()) {

                    inferrencedConnection.add(dataConnection.getStatements(null, null, null));
                }

                try (RepositoryConnection ontologyConnection = ontology.getConnection()) {

                    inferrencedConnection.add(ontologyConnection.getStatements(null, null, null));
                }
            }

            data = inferrencedRepository;
        }



        try (RepositoryConnection dataConnection = data.getConnection()) {

            Optional<Boolean> reduce = shapes.stream()
                .map(shape -> shape.validate(dataConnection, constraintViolationHandler))
                .reduce((b1, b2) -> b1 && b2);

            if (reduce.isPresent()) {
                return reduce.get();
            }

            return true;

//
//
//                List<MemValue> shapePredicates = getAllShapePredicates(shapesConnection);
//
//                RepositoryResult<Statement> shapesResult = dataConnection.getStatements(null, null, null);
//                Model dataGraphModel = QueryResults.asModel(shapesResult);
//
//                // TODO: This forEach checks whether the data graph is containing all predicates as the shape.
//                // TODO: That is not necessarily the point. Check on scopeClass instead.
//                dataGraphModel.forEach(dataStatement -> {
//                    if (!shapePredicates.contains(dataStatement.getPredicate())) {
//                        violations.add(dataStatement.getPredicate().toString());
//                    }
//                });
//
//                parseShapePropertyConstraints(shapesConnection, dataGraphModel);
//
//                if (violations.isEmpty()) {
//                    return true;
//                }
//                else {
//                   return false;
////                    violations.forEach(System.out::println);
//                }
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

    private void parseShapePropertyConstraints(RepositoryConnection shapesConnection, Model dataGraphModel) {

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
     * @param properties     List of property constaints from SHACL validation shape
     * @param dataGraphModel The data graph to be validated
     */
    private void validateDataGraph(List<Property> properties, Model dataGraphModel) {

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
     * Counts the number of occurrences of a given property in a data graph.
     * Data graphs validated against sh:minCount shall not have a number of that given predicate less than
     * sh:minCount states. If the minCount is 1, there shall be AT LEAST one of that predicate in the data graph.
     *
     * @param dataGraphModel data graph to be validated
     * @param SHACLProperty  the given SHACL property
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
