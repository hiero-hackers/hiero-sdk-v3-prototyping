package org.hiero.sdk.v3.metalang.semantic;

import java.util.Objects;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;

/**
 * The result of resolving a named type reference.
 */
public sealed interface ResolvedType {

    /**
     * A basic data type.
     *
     * @param type the builtin type
     */
    record Builtin(BuiltinType type) implements ResolvedType {
        /**
         * Creates a builtin resolution.
         *
         * @param type the builtin type
         */
        public Builtin {
            Objects.requireNonNull(type, "type must not be null");
        }
    }

    /**
     * A type declared in a spec.
     *
     * @param namespace   the namespace that declares the type
     * @param declaration the declaration
     */
    record Declared(String namespace, Declaration.TypeDeclaration declaration) implements ResolvedType {
        /**
         * Creates a declared-type resolution.
         *
         * @param namespace   the declaring namespace
         * @param declaration the declaration
         */
        public Declared {
            Objects.requireNonNull(namespace, "namespace must not be null");
            Objects.requireNonNull(declaration, "declaration must not be null");
        }
    }

    /**
     * The reference could not be resolved.
     *
     * @param rule    the rule describing the problem
     * @param message the detailed message
     */
    record Unresolved(Rule rule, String message) implements ResolvedType {
        /**
         * Creates an unresolved result.
         *
         * @param rule    the rule
         * @param message the message
         */
        public Unresolved {
            Objects.requireNonNull(rule, "rule must not be null");
            Objects.requireNonNull(message, "message must not be null");
        }
    }
}
