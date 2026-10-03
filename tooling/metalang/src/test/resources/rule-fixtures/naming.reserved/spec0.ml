namespace a

X {
    @@immutable hashCode: int32
    @@immutable name: string
    void wait()
    string toString()
}

enum E(name: string) {
    A("a")

    int32 ordinal()
    bool equals(other: E)
}
