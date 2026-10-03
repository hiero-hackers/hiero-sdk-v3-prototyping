# Shop

## Description

Small spec that uses every element of the linked model.

## API Schema

```
namespace shop
requires {Money} from shop.money

// Something with an id.
abstraction Entity<$$Id> {
    @@immutable @@nullable id: $$Id
    @@finalMethod bool sameAs<$$O>(other: Entity<$$O>)
}

// A product.
@@finalType
Product extends Entity<int64> {
    @@immutable @@override id: int64
    @@immutable @@maxLength(40) name: string
    @@immutable price: Money
    @@async @@throws(not-found-error) list<Product> related(@@min(1) limit: int32)
}

// Something with a numeric code.
abstraction Coded {
    @@immutable code: int8
}

// Something with a price.
abstraction Priced {
    @@immutable price: Money

    // Total including tax.
    @@throws(pricing-error, timeout-error) Money gross(@@min(0) taxRate: double)
}

// Something with a label that can change.
abstraction Labelled<$$Self extends Labelled<$$Self>> {
    @@nullable label: string   // the label, if any
}

// A shape; the set of shapes is closed.
@@sealed(Circle, Square)
abstraction Shape {
    @@immutable @@nullable size: int32

    @@static Shape unit()
}

// A circle always has a size.
Circle extends Shape {
    @@immutable @@override size: int32
}

// A square with a price.
Square extends Shape, Priced {
}

// A product category.
enum Category(code: int8, @@nullable label: string) extends Coded {
    FOOD(1, "Food") // things to eat
    OTHER(2, null)
    @@deprecated LEGACY(3, "Legacy \"old\"")
    // The former catch-all category.
    //
    // Deprecated because it was never assigned; use `OTHER` instead.
    @@deprecated MISC(4, "Misc")

    // Whether this category is about food.
    bool isFood()

    @@static @@throws(not-found-error) Category byCode(code: int8)
}

enum Flag(default: bool) {
    ON(true)
    OFF(false)
}

// A postal address.
Address {
    @@immutable @@minLength(1) @@maxLength(80) street: string   // street and house number
    @@immutable @@nullable @@pattern("^[0-9]{5}$") zip: string
    @@immutable @@nullable @@urlPattern website: string
    @@immutable @@urlPattern map: string
}

// A shopping cart.
Cart<$$Item> {
    // the items in the cart;
    // never more than 100
    @@immutable @@minSize(1) @@maxSize(100) items: list<$$Item>
    @@immutable @@default([]) coupons: set<string>
    @@immutable notes: map<string, string>
    @@immutable @@default(1) @@min(1) @@max(10) quantity: int32
    @@immutable @@nullable @@min(0) discount: int32
    @@immutable @@deprecated legacy: bool
    @@immutable category: Category
    @@immutable total: Money

    // Total price of the cart.
    Money sum()

    @@static Cart<string> empty()
}

// A file attachment.
Attachment {
    @@immutable @@minSize(1) content: bytes
    @@immutable @@nullable checksum: bytes
    @@immutable weight: double
    @@immutable @@nullable count: int32
}

// A hash; prints itself.
Hash {
    @@immutable value: bytes

    // Hex representation.
    string toString()
}

// Not a record: the attribute is mutable.
Counter {
    value: int32
}

// Not a record: no attributes.
Printer {
    void print()
}

// Neither a record nor its subtype: records cannot be extended.
Base {
    @@immutable a: int32
}

Derived extends Base {
    @@immutable b: int32
}

// Deferred: refers to an abstraction.
Listing {
    @@immutable entity: Entity<string>
}

constant MAX_PRODUCTS: int32 = 1_000

@@static Product create(name: string, tags: string...)
```

## Testing

Nothing to test.

## Questions & Comments
