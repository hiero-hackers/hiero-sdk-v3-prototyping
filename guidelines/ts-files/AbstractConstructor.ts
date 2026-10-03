/**
 * The class of a type, also of an abstract class (the mapping of the meta-language type `type<T>`), e.g. to select the
 * type of a result: `client.getResponse(id, AccountCreateTransaction)`.
 */
export type AbstractConstructor<T> = abstract new (...args: never[]) => T;
