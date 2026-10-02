/*
 * Grammar of the Hiero SDK V3 API meta-language.
 *
 * Normative prose: guidelines/api-guideline.md. This grammar is the machine-readable counterpart and
 * describes the content of a spec's "## API Schema" code block.
 *
 * Design notes:
 * - The grammar is intentionally *lenient* for a small, closed set of syntax variants that occur in
 *   the existing specs but are not (yet) part of the guideline (e.g. the `type` keyword before a
 *   complex type, `...` placeholders in enums, bounds on generic arguments at the use site, methods
 *   without a return type). They are parsed into the model and reported by the validator as
 *   diagnostics, so a spec author gets a precise message instead of a syntax error.
 * - Everything that is not one of those known variants is a hard syntax error.
 * - The Java package is derived by the antlr4-maven-plugin from the directory of this file.
 * - The language is not newline-sensitive. Comments go to a hidden channel; the AST builder picks up
 *   the comments directly preceding a declaration as its documentation.
 */
grammar MetaLang;

// ---------------------------------------------------------------------------------------------
// Parser rules
// ---------------------------------------------------------------------------------------------

schema
    : namespaceDecl requiresDecl* topLevelDecl* EOF
    ;

namespaceDecl
    : NAMESPACE qualifiedName
    ;

requiresDecl
    : REQUIRES LBRACE requiresList RBRACE FROM qualifiedName
    ;

requiresList
    : STAR
    | identifier (COMMA identifier)* COMMA?
    ;

topLevelDecl
    : constantDecl
    | enumDecl
    | complexTypeDecl
    | functionDecl
    ;

// --- constants ---------------------------------------------------------------------------------

constantDecl
    : annotation* CONSTANT identifier COLON typeRef ASSIGN literal SEMI?
    ;

// --- complex types (incl. abstractions) --------------------------------------------------------

complexTypeDecl
    : annotation* ABSTRACTION? TYPE? identifier typeParameters? extendsClause? typeBody
    ;

extendsClause
    : EXTENDS typeRef (COMMA typeRef)*
    ;

typeBody
    : LBRACE member* RBRACE
    ;

member
    : fieldDecl
    | methodDecl
    ;

fieldDecl
    : annotation* identifier COLON typeRef SEMI?
    ;

methodDecl
    : returningMethodDecl                                                              # withReturnMethod
    | annotation* identifier LPAREN parameterList? RPAREN SEMI?                        # noReturnMethod
    ;

returningMethodDecl
    : annotation* typeRef identifier LPAREN parameterList? RPAREN SEMI?                 # classicMethod
    | annotation* identifier LPAREN parameterList? RPAREN COLON typeRef SEMI?          # trailingReturnMethod
    ;

// --- namespace-level functions -----------------------------------------------------------------
// `Owner.name(...)` attaches a (static) function to a type declared elsewhere in the namespace.

functionDecl
    : annotation* typeRef (owner=identifier DOT)? name=identifier LPAREN parameterList? RPAREN SEMI?
    ;

// --- enums -------------------------------------------------------------------------------------

enumDecl
    : annotation* ENUM identifier extendsClause? LBRACE enumEntry* RBRACE
    ;

// Methods inside enums must declare a return type: `bool supportsType(...)` would otherwise be
// ambiguous with the enum value `bool` followed by a method `supportsType(...)` without return type.
enumEntry
    : annotation* identifier (COMMA | SEMI)?     # enumValue
    | ELLIPSIS                                   # enumPlaceholder
    | fieldDecl                                  # enumField
    | returningMethodDecl                        # enumMethod
    ;

// --- parameters --------------------------------------------------------------------------------

parameterList
    : parameter (COMMA parameter)* COMMA?
    ;

parameter
    : annotation* identifier COLON typeRef ELLIPSIS?
    ;

// --- generics ----------------------------------------------------------------------------------

typeParameters
    : LT typeParameter (COMMA typeParameter)* GT
    ;

typeParameter
    : GENERIC_NAME (EXTENDS typeRef)?
    ;

// --- type references ---------------------------------------------------------------------------

typeRef
    : functionType
    | GENERIC_NAME
    | ANY
    | VOID
    | qualifiedName typeArguments?
    ;

typeArguments
    : LT typeArgument (COMMA typeArgument)* GT
    ;

typeArgument
    : ANY (EXTENDS typeRef)?                 # wildcardArgument
    | GENERIC_NAME EXTENDS typeRef           # boundedGenericArgument
    | typeRef                                # plainArgument
    ;

functionType
    : FUNCTION LT typeRef identifier LPAREN parameterList? RPAREN GT
    ;

// --- annotations -------------------------------------------------------------------------------

annotation
    : ANNOTATION_NAME (LPAREN (annotationArgument (COMMA annotationArgument)*)? RPAREN)?
    ;

annotationArgument
    : KEBAB_ID
    | literal
    ;

// --- literals ----------------------------------------------------------------------------------

literal
    : STRING                                                                   # stringLiteral
    | NUMBER                                                                   # numberLiteral
    | LBRACK (literal (COMMA literal)*)? RBRACK                                # listLiteral
    | qualifiedName LBRACE (structEntry (COMMA structEntry)* COMMA?)? RBRACE   # structLiteral
    | qualifiedName                                                            # nameLiteral
    ;

structEntry
    : identifier COLON literal
    ;

// --- names -------------------------------------------------------------------------------------

qualifiedName
    : identifier (DOT identifier)*
    ;

// Keywords that are only meaningful in a specific position are usable as identifiers everywhere else
// (e.g. `type` is both the optional complex-type keyword and a basic data type).
identifier
    : IDENTIFIER
    | TYPE
    | FROM
    | NAMESPACE
    | REQUIRES
    | CONSTANT
    | ENUM
    | ABSTRACTION
    | EXTENDS
    | FUNCTION
    ;

// ---------------------------------------------------------------------------------------------
// Lexer rules
// ---------------------------------------------------------------------------------------------

NAMESPACE   : 'namespace';
REQUIRES    : 'requires';
FROM        : 'from';
CONSTANT    : 'constant';
ENUM        : 'enum';
ABSTRACTION : 'abstraction';
TYPE        : 'type';
EXTENDS     : 'extends';
FUNCTION    : 'function';
ANY         : 'ANY';
VOID        : 'void';

ELLIPSIS : '...';
LBRACE   : '{';
RBRACE   : '}';
LPAREN   : '(';
RPAREN   : ')';
LBRACK   : '[';
RBRACK   : ']';
LT       : '<';
GT       : '>';
COMMA    : ',';
COLON    : ':';
SEMI     : ';';
DOT      : '.';
ASSIGN   : '=';
STAR     : '*';

ANNOTATION_NAME : '@@' [a-zA-Z] [a-zA-Z0-9]*;
GENERIC_NAME    : '$$' [a-zA-Z] [a-zA-Z0-9_]*;

// lowercase-kebab-case error identifiers used in @@throws(...); must contain at least one hyphen
KEBAB_ID   : [a-z] [a-z0-9]* ('-' [a-z0-9]+)+;
IDENTIFIER : [a-zA-Z_] [a-zA-Z0-9_]*;
NUMBER     : '-'? [0-9] [0-9_]* ('.' [0-9] [0-9_]*)?;
STRING     : '"' (~["\\\r\n] | '\\' .)* '"';

LINE_COMMENT  : '//' ~[\r\n]* -> channel(HIDDEN);
BLOCK_COMMENT : '/*' .*? '*/' -> channel(HIDDEN);
WS            : [ \t\r\n]+ -> skip;

// Any other character is reported as a lexer error instead of being silently dropped.
ERROR_CHAR : .;
