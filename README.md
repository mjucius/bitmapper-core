# BitMapper Core

A lightweight, memory-efficient Java library for storing and manipulating boolean values as individual bits in a byte array.

## Overview

Java's `boolean[]` uses one byte per element on every common JVM, and `Boolean[]` is even more wasteful. **BitMapper** stores 8 boolean values per byte, reducing memory use by up to 8× compared to `boolean[]` (and up to 32× compared to `Boolean[]`).

## Features

- **Memory efficient**: 1 byte per 8 boolean values.
- **Zero-copy wrapping**: construct over an existing `byte[]` and mutate it in place.
- **Simple API**: get / set / clear / setAll, plus equality and serialization.
- **Bounds-checked**: every read and write throws a clear `IndexOutOfBoundsException` for negative indices and indices beyond `getSize()`.
- **No external dependencies**.

## Requirements

- **Runtime**: Java 8 or higher.
- **Build (from source)**: any JDK 17+ (required by Gradle 9.5). The published artifact targets Java 8 bytecode.

## Installation

Once published to Maven Central, add to your `build.gradle`:

```gradle
dependencies {
    implementation 'com.jucius:bitmapper-core:1.1.0'
}
```

Or in Maven:

```xml
<dependency>
    <groupId>com.jucius</groupId>
    <artifactId>bitmapper-core</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Building from source

```bash
git clone https://github.com/mjucius/bitmapper-core.git
cd bitmapper-core
./gradlew build
```

## Usage

### Creating a new BitMapper

The size is rounded up to the next multiple of 8 bits.

```java
BitMapper bitMapper = new BitMapper(1024);

bitMapper.setBit(256, true);
boolean v = bitMapper.getBit(512);

int size = bitMapper.getSize();   // 1024

bitMapper.setAll(true);            // every bit → true
bitMapper.clear();                 // every bit → false

byte[] data = bitMapper.getData(); // backing byte array (live reference)
```

### Wrapping an existing byte array

The array is **not copied**. Reads see existing bits; writes mutate the original.

```java
byte[] backing = new byte[4];
BitMapper bitMapper = new BitMapper(backing);

int size = bitMapper.getSize();    // 32 (8 bits × 4 bytes)
bitMapper.setBit(12, true);
assert bitMapper.getData() == backing;
assert backing[1] == 0x10;          // bit 12 is the 0x10 bit of byte 1
```

### Bounds checking

```java
BitMapper bitMapper = new BitMapper(new byte[4]);  // 32 bits

bitMapper.getBit(-1);   // throws IndexOutOfBoundsException
bitMapper.getBit(32);   // throws IndexOutOfBoundsException
bitMapper.setBit(48, true);  // throws IndexOutOfBoundsException
```

## API reference

### Constructors

- `BitMapper(int sizeHint)` — new BitMapper with at least the specified number of bits, rounded up to the next multiple of 8. Throws `IllegalArgumentException` if `sizeHint` is negative.
- `BitMapper(byte[] data)` — wraps the given array (zero-copy). Throws `IllegalArgumentException` if `data` is null.

### Methods

- `boolean getBit(int bitIndex)` — returns the bit at `bitIndex`.
- `void setBit(int bitIndex, boolean value)` — assigns `value` to the bit at `bitIndex`.
- `void clear()` — sets every bit to `false`.
- `void setAll(boolean value)` — sets every bit to `value`.
- `int getSize()` — total number of bits (always a multiple of 8).
- `byte[] getData()` — returns the live backing array.

`BitMapper` also implements `Serializable` and overrides `equals`, `hashCode`, and `toString` (content-equality on the underlying byte array).

## Bit ordering

Bits are packed **least-significant-bit first** within each byte. That is:

| Bit index | Byte | Mask within byte |
|----------:|-----:|------------------|
| 0         | 0    | `0x01`           |
| 7         | 0    | `0x80`           |
| 8         | 1    | `0x01`           |
| 15        | 1    | `0x80`           |

Tests pin this contract, so it is safe to rely on when persisting a BitMapper's bytes.

## Thread safety

`BitMapper` is **not thread-safe**. External synchronization is required for concurrent access; mutating one instance from multiple threads without coordination produces undefined results.

## Performance

| Operation | Complexity |
|-----------|------------|
| `getBit`, `setBit` | O(1) |
| `clear`, `setAll`  | O(n / 8) |
| Memory             | n / 8 bytes |

## Use cases

- Bit sets and flag arrays
- Memory-constrained environments
- Network protocol payload manipulation
- Persistent storage of compact boolean arrays

## License

Apache License 2.0. See [LICENSE](LICENSE).

## Contributing

Issues and pull requests welcome.

## Author

[Matthew Jucius](https://github.com/mjucius)
