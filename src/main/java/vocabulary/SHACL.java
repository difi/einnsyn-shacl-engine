package vocabulary;

import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * Created by veronika on 4/21/16.
 *
 * The SHACL vocabulary. Note that the specification of SHACL is changing, therefore may this
 * class be outdated. https://www.w3.org/TR/shacl/
 *
 * TODO: USE THE SHACL VOCABULARY LOCATED IN LIBRARIES/VOCABULARIES INSTEAD!
 *
 */
public class SHACL {

    public final static String BASE_URI = "http://www.w3.org/ns/shacl";
    public final static String NS = BASE_URI + "#";
    public final static String PREFIX = "sh";

    static ValueFactory factory = SimpleValueFactory.getInstance();

    /**
     * OBJECT TYPES
     */
    public final static IRI Shape = factory.createIRI(NS + "Shape");

    // Scopes
    public final static IRI PropertyScope = factory.createIRI(NS + "PropertyScope");
    public final static IRI InversePropertyScope = factory.createIRI(NS + "InversePropertyScope");
    public final static IRI AllSubjectsScope = factory.createIRI(NS + "AllSubjectsScope");
    public final static IRI AllObjectsScope = factory.createIRI(NS + "AllObjectsScope");

    public final static IRI PropertyGroup = factory.createIRI(NS + "PropertyGroup");
    public final static IRI Closed = factory.createIRI(NS + "Closed");
    public final static IRI NodeKind = factory.createIRI(NS + "NodeKind");

    // Object types
    public final static IRI IRI = factory.createIRI(NS + "IRI");
    public final static IRI Literal = factory.createIRI(NS + "Literal");
    public final static IRI BlankNode = factory.createIRI(NS + "BlankNode");

    // Constraints
    public final static IRI Constraint = factory.createIRI(NS + "Constraint");
    public final static IRI ConstraintTemplate = factory.createIRI(NS + "ConstraintTemplate");
    public final static IRI PropertyConstraint = factory.createIRI(NS + "PropertyConstraint");
    public final static IRI NodeConstraint = factory.createIRI(NS + "NodeConstraint");

    // Validation classes
    public final static IRI Violation = factory.createIRI(NS + "Violation");
    public final static IRI Info = factory.createIRI(NS + "Info");
    public final static IRI Warning = factory.createIRI(NS + "Warning");
    public final static IRI ValidationResult = factory.createIRI(NS + "ValidationResult");
    public final static IRI AbstractResult = factory.createIRI(NS + "AbstractResult");

    public final static IRI NativeConstraint = factory.createIRI(NS + "NativeConstraint");

    // Template and Argument
    public final static IRI Template = factory.createIRI(NS + "Template");
    public final static IRI TemplateConstraint = factory.createIRI(NS + "TemplateConstraint");
    public final static IRI ScopeTemplate = factory.createIRI(NS + "ScopeTemplate");
    public final static IRI Argument = factory.createIRI(NS + "Argument");
    public final static IRI PropertyValueConstraintTemplate = factory.createIRI(NS + "PropertyValueConstraintTemplate");
    public final static IRI InversePropertyValueConstraintTemplate = factory.createIRI(NS + "InversePropertyValueConstraintTemplate");
    public final static IRI AbstractDatatypePropertyConstraint = factory.createIRI(NS + "AbstractDatatypePropertyConstraint");
    public final static IRI DerivedValuesTemplate = factory.createIRI(NS + "DerivedValuesTemplate");

    // Scopes
    public final static IRI SPARQLScope = factory.createIRI(NS + "SPARQLScope");
    public final static IRI NativeScope = factory.createIRI(NS + "NativeScope");
    public final static IRI TemplateScope = factory.createIRI(NS + "TemplateScope");

    // Functions
    public final static IRI Function = factory.createIRI(NS + "Function");
    public final static IRI ValidationFunctions = factory.createIRI(NS + "ValidationFunctions");
    public final static IRI DefaultValueTypeRule = factory.createIRI(NS + "DefaultValueTypeRule");


    /**
     * PREDICATE TYPES
     */
    public final static IRI scope = factory.createIRI(NS + "scope");
    public final static IRI scopeClass = factory.createIRI(NS + "scopeClass");
    public final static IRI scopeNode = factory.createIRI(NS + "scopeNode");

    public final static IRI filterShape = factory.createIRI(NS + "filterShape");
    public final static IRI hasShape = factory.createIRI(NS + "hasShape");

    public final static IRI shapesGraph = factory.createIRI(NS + "shapesGraph");
    public final static IRI property = factory.createIRI(NS + "property");
    public final static IRI inverseProperty = factory.createIRI(NS + "inverseProperty");
    public final static IRI group = factory.createIRI(NS + "group");
    public final static IRI defaultValue = factory.createIRI(NS + "defaultValue");

    // Core constraints
    public final static IRI class_property = factory.createIRI(NS + "class");
    public final static IRI classIn = factory.createIRI(NS + "classIn");
    public final static IRI directType = factory.createIRI(NS + "directType");
    public final static IRI datatype = factory.createIRI(NS + "datatype");
    public final static IRI datatypeIn = factory.createIRI(NS + "datatypeIn");
    public final static IRI equals = factory.createIRI(NS + "equals");
    public final static IRI hasValue = factory.createIRI(NS + "hasValue");
    public final static IRI in = factory.createIRI(NS + "in");
    public final static IRI lessThan = factory.createIRI(NS + "lessThan");
    public final static IRI lessThanOrEquals = factory.createIRI(NS + "lessThanOrEquals");
    public final static IRI minCount = factory.createIRI(NS + "minCount");
    public final static IRI maxCount = factory.createIRI(NS + "maxCount");
    public final static IRI minLength = factory.createIRI(NS + "minLength");
    public final static IRI maxLength = factory.createIRI(NS + "maxLength");
    public final static IRI maxExclusive = factory.createIRI(NS + "maxExclusive");
    public final static IRI maxInclusive = factory.createIRI(NS + "maxInclusive");
    public final static IRI minExclusive = factory.createIRI(NS + "minExclusive");
    public final static IRI minInclusive = factory.createIRI(NS + "minInclusive");
    public final static IRI nodeKind = factory.createIRI(NS + "nodeKind");
    public final static IRI notEquals = factory.createIRI(NS + "notEquals");
    public final static IRI pattern = factory.createIRI(NS + "pattern");
    public final static IRI flags = factory.createIRI(NS + "flags");
    public final static IRI uniqueLang = factory.createIRI(NS + "uniqueLang");
    public final static IRI valueShape = factory.createIRI(NS + "valueShape");
    public final static IRI qualifiedValueShape = factory.createIRI(NS + "qualifiedValueShape");
    public final static IRI qualifiedMinCount = factory.createIRI(NS + "qualifiedMinCount");
    public final static IRI qualifiedMaxCount = factory.createIRI(NS + "qualifiedMaxCount");

    public final static IRI not = factory.createIRI(NS + "not");
    public final static IRI and = factory.createIRI(NS + "and");
    public final static IRI or = factory.createIRI(NS + "or");
    public final static IRI closed = factory.createIRI(NS + "closed");
    public final static IRI ignoredProperties = factory.createIRI(NS + "ignoredProperties");

    // Non-validating constraint characteristics
    public final static IRI name = factory.createIRI(NS + "name");
    public final static IRI description = factory.createIRI(NS + "description");
    public final static IRI order = factory.createIRI(NS + "order");

    // Validation results
    public final static IRI severity = factory.createIRI(NS + "severity");
    public final static IRI focusNode = factory.createIRI(NS + "focusNode");
    public final static IRI subject = factory.createIRI(NS + "subject");
    public final static IRI predicate = factory.createIRI(NS + "predicate");
    public final static IRI object = factory.createIRI(NS + "object");
    public final static IRI message = factory.createIRI(NS + "message");
    public final static IRI detail = factory.createIRI(NS + "detail");
    public final static IRI sourceConstraint = factory.createIRI(NS + "sourceConstraint");
    public final static IRI sourceShape = factory.createIRI(NS + "sourceShape");

    // Annotation properties
    public final static IRI resultAnnotation = factory.createIRI(NS + "resultAnnotation");
    public final static IRI annotationVarName = factory.createIRI(NS + "annotationVarName");
    public final static IRI annotationProperty = factory.createIRI(NS + "annotationProperty");
    public final static IRI annotationValue = factory.createIRI(NS + "annotationValue");


    // Native constraints
    public final static IRI sparql = factory.createIRI(NS + "sparql");
    public final static IRI constraint = factory.createIRI(NS + "constraint");

    public final static IRI argument = factory.createIRI(NS + "argument");
    public final static IRI optional = factory.createIRI(NS + "optional");
    public final static IRI labelTemplate = factory.createIRI(NS + "labelTemplate");

    public final static IRI validationFunction = factory.createIRI(NS + "validationFunction");
    public final static IRI value = factory.createIRI(NS + "value");

    // Functions
    public final static IRI returnType = factory.createIRI(NS + "returnType");
    public final static IRI arg1 = factory.createIRI(NS + "arg1");
    public final static IRI arg2 = factory.createIRI(NS + "arg2");
    // For more arguments, add public final static IRI argX = factory.createIRI(NS + "argX");

    // Derived values constraints
    public final static IRI derivedValues = factory.createIRI(NS + "derivedValues");
    public final static IRI defaultValueType = factory.createIRI(NS + "defaultValueType");

    public final static IRI entailment = factory.createIRI(NS + "entailment");

    public static String withPrefix(IRI iri) {
        return PREFIX + ":" + iri.getLocalName();
    }
}
