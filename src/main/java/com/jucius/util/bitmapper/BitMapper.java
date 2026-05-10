package com.jucius.util.bitmapper;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A memory-efficient bit set backed by a {@code byte[]}.
 *
 * <p>Each bit is stored as a single bit in the underlying byte array, packing
 * 8 boolean values per byte. Bit ordering within each byte is
 * least-significant-bit first: bit index {@code 0} corresponds to the
 * {@code 0x01} bit of byte {@code 0}, bit index {@code 7} to the {@code 0x80}
 * bit of byte {@code 0}, bit index {@code 8} to the {@code 0x01} bit of byte
 * {@code 1}, and so on.
 *
 * <p>This class is <strong>not thread-safe</strong>. External synchronization
 * is required for concurrent access.
 */
public final class BitMapper implements Serializable {

    /**
     * The serial form is just the {@code data} field. If a future version
     * changes the field shape, custom {@code readObject}/{@code writeObject}
     * methods will be required to remain backward-compatible with this form.
     */
    private static final long serialVersionUID = 1L;

    /** Maximum {@code sizeHint} that can be rounded up to a multiple of 8 without overflow. */
    private static final int MAX_SIZE_HINT = Integer.MAX_VALUE - (Byte.SIZE - 1);

    /** Maximum number of bytes shown in {@link #toString()} before truncating. */
    private static final int TO_STRING_MAX_BYTES = 32;

    /** Backing storage; one bit per logical bit, packed LSB-first per byte. */
    private final byte[] data;

    /**
     * Creates a new BitMapper supporting at least the specified number of bits.
     * The backing array length is rounded up to the next whole byte, so the
     * actual {@link #getSize()} is rounded up to the next multiple of 8.
     *
     * @param sizeHint the minimum number of bits; must be non-negative and small
     *        enough that the rounded-up byte count fits in an {@code int}
     * @throws IllegalArgumentException if {@code sizeHint} is negative or so
     *         large that rounding it up to the next multiple of 8 would
     *         overflow
     */
    public BitMapper(int sizeHint) {
        if (sizeHint < 0) {
            throw new IllegalArgumentException(
                    "Specified sizeHint [" + sizeHint + "] must be non-negative");
        }
        if (sizeHint > MAX_SIZE_HINT) {
            throw new IllegalArgumentException(
                    "Specified sizeHint [" + sizeHint + "] exceeds maximum supported size [" + MAX_SIZE_HINT + "]");
        }
        this.data = new byte[(sizeHint + Byte.SIZE - 1) / Byte.SIZE];
    }

    /**
     * Creates a BitMapper that wraps the given byte array. The array is not
     * copied: mutations made through this BitMapper are visible in the original
     * array, and vice versa.
     *
     * @param data the backing byte array
     * @throws IllegalArgumentException if {@code data} is {@code null}
     */
    public BitMapper(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Specified data value must not be null");
        }
        this.data = data;
    }

    /**
     * Returns the value of the bit at the specified (0-based) index.
     *
     * @param bitIndex the 0-based bit index to read
     * @return {@code true} if the bit is set, {@code false} otherwise
     * @throws IndexOutOfBoundsException if {@code bitIndex} is negative or
     *         greater than or equal to {@link #getSize()}
     */
    public boolean getBit(int bitIndex) {
        checkBitIndex(bitIndex);
        return (data[bitIndex / Byte.SIZE] & (1 << (bitIndex % Byte.SIZE))) != 0;
    }

    /**
     * Sets the bit at the specified (0-based) index to the given value.
     *
     * @param bitIndex the 0-based bit index to write
     * @param value the new value of the bit
     * @throws IndexOutOfBoundsException if {@code bitIndex} is negative or
     *         greater than or equal to {@link #getSize()}
     */
    public void setBit(int bitIndex, boolean value) {
        checkBitIndex(bitIndex);
        int byteIndex = bitIndex / Byte.SIZE;
        int bitMask = 1 << (bitIndex % Byte.SIZE);
        if (value) {
            data[byteIndex] |= bitMask;
        } else {
            data[byteIndex] &= ~bitMask;
        }
    }

    /**
     * Sets every bit to {@code false}. Operates on the underlying array in
     * place; for wrapped arrays, the original reference is preserved.
     */
    public void clear() {
        Arrays.fill(data, (byte) 0);
    }

    /**
     * Sets every bit to the given value. Operates on the underlying array in
     * place; for wrapped arrays, the original reference is preserved.
     *
     * @param value the value to assign to every bit
     */
    public void setAll(boolean value) {
        Arrays.fill(data, value ? (byte) 0xFF : (byte) 0);
    }

    /**
     * Returns the total number of bits, equal to 8 times the underlying byte
     * array length.
     *
     * @return the total number of bits supported by this BitMapper
     */
    public int getSize() {
        return data.length * Byte.SIZE;
    }

    /**
     * Returns the underlying byte array. The returned reference aliases the
     * internal state: mutations to the array are visible through this
     * BitMapper, and vice versa. This is intentional for zero-copy use cases.
     * Callers wanting a defensive copy can use
     * {@code Arrays.copyOf(getData(), getData().length)}.
     *
     * @return the live backing byte array
     */
    public byte[] getData() {
        return data;
    }

    private void checkBitIndex(int bitIndex) {
        if (bitIndex < 0 || bitIndex >= getSize()) {
            throw new IndexOutOfBoundsException(
                    "Bit index [" + bitIndex + "] is out of range [0, "
                            + getSize() + ")");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BitMapper)) {
            return false;
        }
        return Arrays.equals(this.data, ((BitMapper) o).data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BitMapper{size=").append(getSize()).append(", data=[");
        int shown = Math.min(data.length, TO_STRING_MAX_BYTES);
        for (int i = 0; i < shown; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(String.format("0x%02X", data[i] & 0xFF));
        }
        if (data.length > shown) {
            sb.append(", ... (").append(data.length - shown).append(" more bytes)");
        }
        sb.append("]}");
        return sb.toString();
    }
}
