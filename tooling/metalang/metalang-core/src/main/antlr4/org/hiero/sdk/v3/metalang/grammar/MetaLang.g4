/*
 * Grammar of the Hiero SDK V3 API meta-language.
 *
 * Normative prose: guidelines/api-guideline.md and guidelines/test-language.md. This grammar is the
 * machine-readable counterpart and describes the content of a spec's "## API Schema" code block (start rule
 * `schema`), of its "## Default Instances for Testing" block (`instances`) and of its `tests` blocks (`tests`).
 *
 * Design notes:
 * - The grammar is intentionally *lenient* for a small, closed set of syntax variants that occur in
 *   the existing specs but are not (yet) part of the guideline (e.g. the `type` keyword before a
 *   complex type, bounds on generic arguments at the use site, methods
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

// --- default instances -------------------------------------------------------------------------
// The content of a spec's "## Default Instances for Testing" code block: for a type, the standard way to obtain an instance
// through the API. It uses the namespace and the imports of the file's API schema.

// It also holds the named test values of the spec: `values NAME: type = expression`, a value (typically a list of
// inputs) that the tests of the same spec use by its name.

instances
    : (instanceDecl | valuesDecl)* EOF
    ;

instanceDecl
    : INSTANCE typeRef ASSIGN expression SEMI?
    ;

valuesDecl
    : VALUES identifier COLON typeRef ASSIGN expression SEMI?
    ;

// Expressions of default instances and tests. The grammar accepts the operators everywhere; a default instance
// that uses one is reported by the instance resolver. The alternatives are ordered by precedence, the highest
// first (see "Operators" in guidelines/test-language.md); binary operators are left-associative.
expression
    : expression DOT identifier LPAREN argumentList? RPAREN         # methodCallExpression
    | expression DOT identifier                                      # accessExpression
    | expression LBRACK expression RBRACK                            # indexExpression
    | MINUS expression                                               # negateExpression
    | expression op=(STAR | SLASH | PERCENT) expression              # multiplicativeExpression
    | expression op=(PLUS | MINUS) expression                        # additiveExpression
    | expression op=(LT | LE | GT | GE) expression                   # comparisonExpression
    | expression IS NOT? typeRef                                     # isExpression
    | expression op=(EQ | NEQ) expression                            # equalityExpression
    | expression AND expression                                      # andExpression
    | expression OR expression                                       # orExpression
    | DEFAULT (LPAREN typeRef RPAREN)?                               # defaultExpression
    | qualifiedName typeArguments? LBRACE argumentList? RBRACE       # constructExpression
    | qualifiedName LPAREN argumentList? RPAREN                      # callExpression
    | LBRACK (expression (COMMA expression)* COMMA?)? RBRACK         # listExpression
    | LBRACE (mapEntry (COMMA mapEntry)* COMMA?)? RBRACE             # mapExpression
    | LPAREN expression RPAREN                                       # parenthesizedExpression
    | STRING                                                         # stringExpression
    | NUMBER                                                         # numberExpression
    | qualifiedName                                                  # nameExpression
    ;

mapEntry
    : expression COLON expression
    ;

argumentList
    : argument (COMMA argument)* COMMA?
    ;

argument
    : identifier COLON expression
    ;

// --- tests -----------------------------------------------------------------------------------
// The content of a `tests` code block of a spec's "## Testing" section. Like the default instances it uses the
// namespace and the imports of the file's API schema. Statements are not separated by a token; a `;` is allowed.

tests
    : testDecl* EOF
    ;

testDecl
    : annotation* TEST STRING block
    ;

block
    : LBRACE statement* RBRACE
    ;

statement
    : letStatement
    | ifStatement
    | forStatement
    | expressionStatement
    ;

letStatement
    : LET identifier (COLON typeRef)? ASSIGN expression SEMI?
    ;

ifStatement
    : IF expression block (ELSE (ifStatement | block))?
    ;

forStatement
    : FOR identifier IN expression block
    ;

// a call whose result is not used, e.g. an assertion; only calls are allowed (checked by the validator)
expressionStatement
    : annotation* expression SEMI?
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
    | annotation* identifier typeParameters? LPAREN parameterList? RPAREN SEMI?        # noReturnMethod
    ;

returningMethodDecl
    : annotation* typeRef identifier typeParameters? LPAREN parameterList? RPAREN SEMI?                 # classicMethod
    | annotation* identifier typeParameters? LPAREN parameterList? RPAREN COLON typeRef SEMI?          # trailingReturnMethod
    ;

// --- namespace-level functions -----------------------------------------------------------------
// Attaching a function to a type from outside (`Owner.name(...)`) is deliberately not part of the
// language: methods of a type are declared inside the type.

functionDecl
    : annotation* typeRef identifier typeParameters? LPAREN parameterList? RPAREN SEMI?
    ;

// --- enums -------------------------------------------------------------------------------------

// `enum Name(attr: Type, ...)`: the attribute list works like a table header; every value assigns
// the attributes positionally: `VALUE("a", 1)`. A value with parentheses needs at least one argument:
// `int8 code()` must stay a method and must not be read as the two values `int8` and `code()`.
enumDecl
    : annotation* ENUM identifier (LPAREN parameterList? RPAREN)? extendsClause? LBRACE enumEntry* RBRACE
    ;

// Methods inside enums must declare a return type: `bool supportsType(...)` would otherwise be
// ambiguous with the enum value `bool` followed by a method `supportsType(...)` without return type.
enumEntry
    : annotation* identifier (LPAREN literal (COMMA literal)* RPAREN)? (COMMA | SEMI)?     # enumValue
    | fieldDecl                                  # enumField      // not allowed; reported by the validator
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
    | MINUS? NUMBER                                                            # numberLiteral
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
    | INSTANCE
    | VALUES
    | DEFAULT
    | TEST
    | LET
    | IF
    | ELSE
    | FOR
    | IN
    | IS
    | NOT
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
INSTANCE    : 'instance';
VALUES      : 'values';
DEFAULT     : 'DEFAULT';
TEST        : 'test';
LET         : 'let';
IF          : 'if';
ELSE        : 'else';
FOR         : 'for';
IN          : 'in';
IS          : 'is';
NOT         : 'not';
ANY         : 'ANY';
VOID        : 'void';

ELLIPSIS : '...';
EQ       : '==';
NEQ      : '!=';
LE       : '<=';
GE       : '>=';
AND      : '&&';
OR       : '||';
PLUS     : '+';
MINUS    : '-';
SLASH    : '/';
PERCENT  : '%';
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
// (in a test, `n-1` is therefore one token and no subtraction: a binary minus needs spaces around it)
KEBAB_ID   : [a-z] [a-z0-9]* ('-' [a-z0-9]+)+;
IDENTIFIER : [a-zA-Z_] [a-zA-Z0-9_]*;
// unsigned: a sign is the MINUS token, so that `a -1` is a subtraction
NUMBER     : [0-9] [0-9_]* ('.' [0-9] [0-9_]*)?;
STRING     : '"' (~["\\\r\n] | '\\' .)* '"';

LINE_COMMENT  : '//' ~[\r\n]* -> channel(HIDDEN);
BLOCK_COMMENT : '/*' .*? '*/' -> channel(HIDDEN);
WS            : [ \t\r\n]+ -> skip;

// Any other character is reported as a lexer error instead of being silently dropped.
ERROR_CHAR : .;
