package org.hiero.mirrornode.common;


/// The order in which a list query returns its items, by the attribute the query is sorted by (an id, a consensus
/// timestamp, ...). When a query is called without an order, the mirror node uses the default of the endpoint.
public enum Order {

    /// ascending
    ASC,
    /// descending
    DESC;
}
