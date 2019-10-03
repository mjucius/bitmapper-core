package com.jucius.util.bitmapper;

import org.junit.Test;

import static org.junit.Assert.*;

public class BitMapperTest {
    @Test
    public void testBitMapper() {
        BitMapper bitMapper = new BitMapper(64);
        assertEquals(8, bitMapper.getData().length);

        // Set a bit and verify it's set
        bitMapper.setBit(12, true);
        assertTrue(bitMapper.getBit(12));

        // Set it again and verify it's still set
        bitMapper.setBit(12, true);
        assertTrue(bitMapper.getBit(12));

        // Unset a bit again and verify it's still cleared
        bitMapper.setBit(12, false);
        assertFalse(bitMapper.getBit(12));

        // Set another bit so we can verify the clear method
        bitMapper.setBit(12, true);
        assertTrue(bitMapper.getBit(12));

        // Verify clear works
        bitMapper.clear();
        assertFalse(bitMapper.getBit(12));
    }

    @Test
    public void testSingleBitSetPermutations() {
        BitMapper bitMapper = new BitMapper(1024);

        // Set each bit individually then check all the bits to make sure only
        // the correct bit was set and all others are unset
        for (int i = 0; i < bitMapper.getSize(); i++) {
            bitMapper.setBit(i, true);
            for (int j = 0; j < bitMapper.getSize(); j++) {
                assertEquals(bitMapper.getBit(j), i == j);
            }
            bitMapper.setBit(i, false);
        }
    }

    @Test
    public void testSingleBitUnsetPermutations() {
        BitMapper bitMapper = new BitMapper(1024);

        // Set all the bits initially
        for (int i = 0; i < bitMapper.getSize(); i++) {
            bitMapper.setBit(i, true);
        }

        // Unset one bit at a time and verify only that one bit was unset
        for (int i = 0; i < bitMapper.getSize(); i++) {
            bitMapper.setBit(i, false);
            for (int j = 0; j < bitMapper.getSize(); j++) {
                assertEquals(bitMapper.getBit(j), i != j);
            }
            bitMapper.setBit(i, true);
        }
    }

}
