# bitmapper-core

## Overview
The java boolean[] does not provide a memory-efficient means of storing boolean values.  The underlying implementation is JVM dependent and can use a byte or even an int array internally to store a single bit value.  This package contains a more memory-efficient means of storing the equivalent of a boolean array.
The package implements this functionality on top of a boolean array allowing individual bits to be retrieved and manipulated.

## Motivation
As someone who needed to store and manipulate a series of bit values in a memory constrained environment, encapsulating the messiness of the actual bit shifting was important.  This class prioritizes memory minimization over computational efficiency.  The implementation is effectively an overlay over a simple byte array encapsulating the complexity of the bit shifting.

## Usage

Simple usage creating a new bit map to support the suggested size.  All bits default to false:
```java
// Create a new BitMapper with 1024 bits
BitMapper bitMapper = new BitMapper(1024);

// Set the 256th bit to true
bitMapper.setBit(256, true);

// Get whether the 512th bit is set (i.e. true) or not
boolean bitVal = bitMapper.getBit(512);

// Get the underlying byte array data
byte[] data = bitMapper.getData();
```

Alternate use case overlaying an existing byte array
```java
// Cerate a BitMapper wrapping an existing byte array
byte[] myData = new byte[4];
....
BitMapper bitMapper = new BitMapper(myData);

// Size is 32 (8 bits per byte * 4 bytes)
int bitMapperSize = bitMapper.size();

// Get the value of the 24th bit in the underlying myData
int bitValue = bitMapper.get(24);

// Throws an IndexOutOfBoundsException because 48th bit is out of range
// of the underlying data
bitMapper.get(48);

// Ensures the 12th bit in the underlying data is set to true.
// Will do nothing if the bit is already set to true
bitMapper.set(12, true);

// The BitMapper wraps the underlying data and any data changed via the BitMapper
// changes the underying data.  BitMapper.getData() returns the same object passed
// into the constructor.  Data is NOT copied.
bitMapper.getData() == myData
```
