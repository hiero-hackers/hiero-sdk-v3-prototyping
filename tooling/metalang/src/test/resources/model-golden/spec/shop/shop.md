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

// A product category.
enum Category(code: int8, @@nullable label: string) {
    FOOD(1, "Food") // things to eat
    OTHER(2, null)
    @@deprecated LEGACY(3, "Legacy \"old\"")

    // Whether this category is about food.
    bool isFood()

    @@static @@throws(not-found-error) Category byCode(code: int8)
}

enum Flag(default: bool) {
    ON(true)
    OFF(false)
}

constant MAX_PRODUCTS: int32 = 1_000

@@static Product create(name: string, tags: string...)
```

## Testing

Nothing to test.

## Questions & Comments
