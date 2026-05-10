package com.jucius.util.bitmapper;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BitMapperTest {

    // ---------- Constructor: BitMapper(int) ----------

    @Test
    void intConstructor_negativeSize_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new BitMapper(-1));
        assertTrue(ex.getMessage().contains("-1"));
        assertTrue(ex.getMessage().contains("non-negative"));
    }

    @Test
    void intConstructor_zeroSize_succeedsWithEmptyMap() {
        BitMapper bitMapper = new BitMapper(0);
        assertEquals(0, bitMapper.getSize());
        assertEquals(0, bitMapper.getData().length);
    }

    @Test
    void intConstructor_roundsUpToNextMultipleOfEight() {
        assertEquals(8, new BitMapper(1).getSize());
        assertEquals(8, new BitMapper(7).getSize());
        assertEquals(8, new BitMapper(8).getSize());
        assertEquals(16, new BitMapper(9).getSize());
        assertEquals(1024, new BitMapper(1023).getSize());
        assertEquals(1024, new BitMapper(1024).getSize());
        assertEquals(1032, new BitMapper(1025).getSize());
    }

    @Test
    void intConstructor_overflowingSize_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new BitMapper(Integer.MAX_VALUE));
        assertTrue(ex.getMessage().contains("exceeds maximum"));
    }

    // ---------- Constructor: BitMapper(byte[]) ----------

    @Test
    void byteArrayConstructor_null_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new BitMapper((byte[]) null));
        assertTrue(ex.getMessage().contains("must not be null"));
    }

    @Test
    void byteArrayConstructor_empty_succeedsWithZeroSize() {
        BitMapper bitMapper = new BitMapper(new byte[0]);
        assertEquals(0, bitMapper.getSize());
    }

    @Test
    void byteArrayConstructor_prePopulatedArray_readsExistingBits() {
        // 0x05 = 0b00000101 → bits 0 and 2 set
        byte[] backing = new byte[] { (byte) 0x05, (byte) 0x80 };
        BitMapper bitMapper = new BitMapper(backing);
        assertTrue(bitMapper.getBit(0));
        assertFalse(bitMapper.getBit(1));
        assertTrue(bitMapper.getBit(2));
        assertFalse(bitMapper.getBit(7));
        // 0x80 in byte 1 → high bit, which is bit index 8 + 7 = 15
        assertTrue(bitMapper.getBit(15));
    }

    // ---------- Zero-copy wrapping ----------

    @Test
    void getData_returnsSameInstanceAsConstructorArgument() {
        byte[] backing = new byte[4];
        BitMapper bitMapper = new BitMapper(backing);
        assertSame(backing, bitMapper.getData());
    }

    @Test
    void wrap_setBit_mutatesOriginalArray() {
        byte[] backing = new byte[2];
        BitMapper bitMapper = new BitMapper(backing);
        bitMapper.setBit(3, true);
        assertEquals((byte) 0x08, backing[0]);
    }

    @Test
    void wrap_clear_preservesArrayReference() {
        byte[] backing = new byte[] { (byte) 0xFF, (byte) 0xFF };
        BitMapper bitMapper = new BitMapper(backing);
        bitMapper.clear();
        assertSame(backing, bitMapper.getData());
        assertArrayEquals(new byte[] { 0, 0 }, backing);
    }

    // ---------- Bounds checks ----------

    @Test
    void getBit_negativeIndex_throwsIndexOutOfBounds() {
        BitMapper bitMapper = new BitMapper(64);
        assertThrows(IndexOutOfBoundsException.class, () -> bitMapper.getBit(-1));
    }

    @Test
    void getBit_indexEqualToSize_throwsIndexOutOfBounds() {
        BitMapper bitMapper = new BitMapper(64);
        IndexOutOfBoundsException ex = assertThrows(IndexOutOfBoundsException.class,
                () -> bitMapper.getBit(64));
        assertTrue(ex.getMessage().contains("64"));
    }

    @Test
    void getBit_indexFarOutOfRange_throwsIndexOutOfBounds() {
        BitMapper bitMapper = new BitMapper(64);
        assertThrows(IndexOutOfBoundsException.class, () -> bitMapper.getBit(72));
        assertThrows(IndexOutOfBoundsException.class, () -> bitMapper.getBit(1000));
    }

    @Test
    void setBit_negativeIndex_throwsIndexOutOfBounds() {
        BitMapper bitMapper = new BitMapper(64);
        assertThrows(IndexOutOfBoundsException.class, () -> bitMapper.setBit(-1, true));
        assertThrows(IndexOutOfBoundsException.class, () -> bitMapper.setBit(-1, false));
    }

    @Test
    void setBit_indexAtOrBeyondSize_throwsIndexOutOfBounds() {
        BitMapper bitMapper = new BitMapper(64);
        assertThrows(IndexOutOfBoundsException.class, () -> bitMapper.setBit(64, true));
        assertThrows(IndexOutOfBoundsException.class, () -> bitMapper.setBit(72, false));
    }

    @Test
    void emptyMap_anyAccess_throws() {
        BitMapper bitMapper = new BitMapper(0);
        assertThrows(IndexOutOfBoundsException.class, () -> bitMapper.getBit(0));
        assertThrows(IndexOutOfBoundsException.class, () -> bitMapper.setBit(0, true));
    }

    // ---------- Bit ordering (LSB-first within each byte) ----------

    @Test
    void setBit_zero_setsLowBitOfByteZero() {
        BitMapper bitMapper = new BitMapper(8);
        bitMapper.setBit(0, true);
        assertEquals((byte) 0x01, bitMapper.getData()[0]);
    }

    @Test
    void setBit_seven_setsHighBitOfByteZero() {
        BitMapper bitMapper = new BitMapper(8);
        bitMapper.setBit(7, true);
        assertEquals((byte) 0x80, bitMapper.getData()[0]);
    }

    @Test
    void setBit_eight_setsLowBitOfByteOne() {
        BitMapper bitMapper = new BitMapper(16);
        bitMapper.setBit(8, true);
        assertEquals((byte) 0x00, bitMapper.getData()[0]);
        assertEquals((byte) 0x01, bitMapper.getData()[1]);
    }

    // ---------- Core set/get/clear behavior (migrated + sharpened) ----------

    @Test
    void setBit_idempotent() {
        BitMapper bitMapper = new BitMapper(64);
        bitMapper.setBit(12, true);
        assertTrue(bitMapper.getBit(12));
        bitMapper.setBit(12, true);
        assertTrue(bitMapper.getBit(12));
    }

    @Test
    void clear_unsetsAllBits() {
        BitMapper bitMapper = new BitMapper(64);
        bitMapper.setBit(12, true);
        bitMapper.setBit(40, true);
        bitMapper.clear();
        assertFalse(bitMapper.getBit(12));
        assertFalse(bitMapper.getBit(40));
    }

    @Test
    void singleBitSetPermutations_onlyTargetIsSet() {
        BitMapper bitMapper = new BitMapper(1024);
        for (int i = 0; i < bitMapper.getSize(); i++) {
            bitMapper.setBit(i, true);
            for (int j = 0; j < bitMapper.getSize(); j++) {
                assertEquals(i == j, bitMapper.getBit(j));
            }
            bitMapper.setBit(i, false);
        }
    }

    @Test
    void singleBitUnsetPermutations_onlyTargetIsUnset() {
        BitMapper bitMapper = new BitMapper(1024);
        bitMapper.setAll(true);
        for (int i = 0; i < bitMapper.getSize(); i++) {
            bitMapper.setBit(i, false);
            for (int j = 0; j < bitMapper.getSize(); j++) {
                assertEquals(i != j, bitMapper.getBit(j));
            }
            bitMapper.setBit(i, true);
        }
    }

    // ---------- setAll ----------

    @Test
    void setAll_true_setsEveryBit() {
        BitMapper bitMapper = new BitMapper(24);
        bitMapper.setAll(true);
        for (int i = 0; i < bitMapper.getSize(); i++) {
            assertTrue(bitMapper.getBit(i), "bit " + i);
        }
        assertArrayEquals(
                new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF },
                bitMapper.getData());
    }

    @Test
    void setAll_false_unsetsEveryBit() {
        BitMapper bitMapper = new BitMapper(new byte[] { (byte) 0xAB, (byte) 0xCD });
        bitMapper.setAll(false);
        for (int i = 0; i < bitMapper.getSize(); i++) {
            assertFalse(bitMapper.getBit(i));
        }
    }

    // ---------- equals / hashCode ----------

    @Test
    void equals_sameContent_true() {
        BitMapper a = new BitMapper(new byte[] { 1, 2, 3 });
        BitMapper b = new BitMapper(new byte[] { 1, 2, 3 });
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_differentContent_false() {
        BitMapper a = new BitMapper(new byte[] { 1, 2, 3 });
        BitMapper b = new BitMapper(new byte[] { 1, 2, 4 });
        assertNotEquals(a, b);
    }

    @Test
    void equals_differentLength_false() {
        BitMapper a = new BitMapper(new byte[] { 1 });
        BitMapper b = new BitMapper(new byte[] { 1, 0 });
        assertNotEquals(a, b);
    }

    @Test
    void equals_nullAndOtherTypes_false() {
        BitMapper a = new BitMapper(8);
        assertNotEquals(a, null);
        assertNotEquals(a, "not a BitMapper");
    }

    @Test
    void equals_self_true() {
        BitMapper a = new BitMapper(8);
        assertEquals(a, a);
    }

    @Test
    void equals_twoWrappersOfSameArray_true() {
        byte[] backing = new byte[] { 1, 2, 3 };
        BitMapper a = new BitMapper(backing);
        BitMapper b = new BitMapper(backing);
        assertEquals(a, b);
    }

    // ---------- toString ----------

    @Test
    void toString_includesSizeAndHexBytes() {
        BitMapper bitMapper = new BitMapper(new byte[] { (byte) 0x01, (byte) 0xAB });
        String s = bitMapper.toString();
        assertTrue(s.contains("size=16"), s);
        assertTrue(s.contains("0x01"), s);
        assertTrue(s.contains("0xAB"), s);
    }

    @Test
    void toString_emptyMap_hasZeroSizeAndEmptyData() {
        String s = new BitMapper(0).toString();
        assertTrue(s.contains("size=0"), s);
        assertTrue(s.contains("data=[]"), s);
    }

    @Test
    void toString_largeMap_truncatesAfterMaxBytes() {
        // 40 bytes → 320 bits, exceeds the 32-byte preview threshold
        BitMapper bitMapper = new BitMapper(40 * Byte.SIZE);
        String s = bitMapper.toString();
        assertTrue(s.contains("size=320"), s);
        assertTrue(s.contains("8 more bytes"), s);
        // Reasonable upper bound: a well-truncated string is short
        assertTrue(s.length() < 500, "toString should be truncated: " + s);
    }

    // ---------- Serializable round-trip ----------

    @Test
    void serialization_roundTripPreservesBits() throws Exception {
        BitMapper original = new BitMapper(64);
        original.setBit(3, true);
        original.setBit(40, true);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bytes)) {
            oos.writeObject(original);
        }

        BitMapper deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(bytes.toByteArray()))) {
            deserialized = (BitMapper) ois.readObject();
        }

        assertEquals(original, deserialized);
        assertTrue(deserialized.getBit(3));
        assertTrue(deserialized.getBit(40));
        assertFalse(deserialized.getBit(0));
    }
}
