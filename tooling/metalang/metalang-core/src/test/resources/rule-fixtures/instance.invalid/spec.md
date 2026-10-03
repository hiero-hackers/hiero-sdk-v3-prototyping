# Title

## Description

Text.

## API Schema

```
namespace a
abstraction Shape { void draw() }
enum Color { RED }
Point { @@immutable x: int32 }
@@static Point origin()
```

## Default Instances

```
// an unknown attribute, a missing attribute
instance Point = Point{y: 1}

// a value of the wrong type
instance Shape = Point{x: 1}

// no declared type
instance string = "x"

// an unknown enum constant
instance Color = Color.BLUE
```

## Testing

Nothing.

## Questions & Comments
