# BitMapper Core

A lightweight, memory-efficient Java library for storing and manipulating boolean values as individual bits in a byte array.

## Overview

Java's native `boolean[]` array is not memory-efficient. The underlying JVM implementation can use a byte or even an int array internally to store a single bit value, wasting significant memory. **BitMapper** provides a memory-efficient alternative by storing bits directly in a byte array (8 bits per byte), reducing memory usage by up to 8x compared to boolean arrays.

## Features

- **Memory Efficient**: Stores 8 boolean values per byte instead of 1-4 bytes per boolean
- **Simple API**: Clean, intuitive methods for getting and setting individual bits
- **Flexible**: Create new bit arrays or wrap existing byte arrays
- **Zero-copy**: When wrapping byte arrays, no data is copied
- **Safe**: Bounds checking with clear exception messages
- **Lightweight**: No external dependencies, minimal footprint

## Requirements

- Java 8 or higher
- Gradle 5.4+ (for building from source)

## Installation

### Using Gradle

Add the dependency to your `build.gradle`:

```gradle
dependencies {
    implementation 'com.jucius.util:bitmapper-core:1.0.0'
}
```

### Building from Source

```bash
git clone https://github.com/mjucius/bitmapper-core.git
cd bitmapper-core
./gradlew build
```

## Usage

### Creating a New BitMapper

Create a new BitMapper with a specified number of bits. All bits default to `false`:

```java
// Create a new BitMapper with 1024 bits
BitMapper bitMapper = new BitMapper(1024);

// Set the 256th bit to true
bitMapper.setBit(256, true);

// Get whether the 512th bit is set (i.e. true) or not
boolean bitVal = bitMapper.getBit(512);

// Get the size (number of bits)
int size = bitMapper.getSize();  // Returns 1024

// Clear all bits (set to false)
bitMapper.clear();

// Get the underlying byte array data
byte[] data = bitMapper.getData();
```

### Wrapping an Existing Byte Array

Overlay an existing byte array to manipulate it at the bit level:

```java
// Create a BitMapper wrapping an existing byte array
byte[] myData = new byte[4];
BitMapper bitMapper = new BitMapper(myData);

// Size is 32 (8 bits per byte * 4 bytes)
int bitMapperSize = bitMapper.getSize();

// Get the value of the 24th bit in the underlying myData
boolean bitValue = bitMapper.getBit(24);

// Set the 12th bit in the underlying data to true
bitMapper.setBit(12, true);

// The BitMapper wraps the underlying data - any changes via the BitMapper
// modify the underlying array. BitMapper.getData() returns the same object
// passed into the constructor. Data is NOT copied.
assert bitMapper.getData() == myData;
```

### Handling Out of Bounds Access

```java
byte[] myData = new byte[4];  // 32 bits
BitMapper bitMapper = new BitMapper(myData);

// Throws IndexOutOfBoundsException because 48th bit is out of range
try {
    bitMapper.getBit(48);
} catch (IndexOutOfBoundsException e) {
    // Handle error
}
```

## API Reference

### Constructors

- `BitMapper(int sizeHint)` - Creates a new BitMapper supporting at least the specified number of bits
- `BitMapper(byte[] data)` - Creates a BitMapper wrapping an existing byte array (zero-copy)

### Methods

- `boolean getBit(int bitIndex)` - Returns the value of the bit at the specified index
- `void setBit(int bitIndex, boolean value)` - Sets the bit at the specified index to the given value
- `void clear()` - Sets all bits to false
- `int getSize()` - Returns the total number of bits supported
- `byte[] getData()` - Returns the underlying byte array

## Performance Characteristics

- **Memory**: O(n/8) - Uses 1 byte per 8 boolean values
- **Get/Set**: O(1) - Constant time bit access with bitwise operations
- **Clear**: O(n/8) - Linear in the number of bytes

## Motivation

This library was created for use in memory-constrained environments where storing large numbers of boolean flags efficiently is critical. Rather than dealing with manual bit shifting and masking operations, BitMapper encapsulates this complexity in a clean API.

**Design Philosophy**: This implementation prioritizes memory minimization over computational efficiency. While bitwise operations add a small computational overhead compared to direct array access, the memory savings (up to 8x) make it ideal for:

- Bloom filters
- Bit sets and flags
- Memory-constrained embedded systems
- Large-scale data processing with boolean markers
- Network protocol implementations

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

## Author

Created and maintained by [mjucius](https://github.com/mjucius)
