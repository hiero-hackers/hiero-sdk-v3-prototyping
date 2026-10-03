namespace a

// An old type.
@@deprecated
Old {
    // The name. Deprecated, use label instead.
    @@deprecated @@immutable name: string
    @@deprecated @@immutable label: string

    // Renders the value.
    @@deprecated string render()
}

enum Kind {
    @@deprecated LEGACY
    // Deprecated: never used.
    @@deprecated UNUSED
}
