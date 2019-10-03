package com.jucius.util.bitmapper;

public class BitMapper {

    private final byte[] data;

    /**
     * Initialize a new data block supporting at least the specified number of
     * values
     */
    public BitMapper(int sizeHint) {
        if (sizeHint < 0) {
            throw new IllegalArgumentException("Specified sizeHint [" + sizeHint + "] must be greater than 1");
        }

        // The size hint is used to determine the size of the long array to initialize.
        // The implementation will support at least the specified size and may support
        // more bits
        if (sizeHint % Byte.SIZE == 0) {
            data = new byte[sizeHint / Byte.SIZE];
        } else {
            data = new byte[(sizeHint / Byte.SIZE) + 1];
        }
    }

    /**
     * Create a new instance and using the specified data block
     */
    public BitMapper(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Specified data value must not be null");
        }
        this.data = data;
    }

    /**
     * Return the status of the bit at the specified (0 based) index
     */
    public boolean getBit(int bitIndex) {

            int dataIndex = getDataIndex(bitIndex);
            int dataPosition = getDataPosition(bitIndex);

           return (data[dataIndex] & (1 << dataPosition)) != 0;
    }

    /**
     * Set the specified bit to the specified value
     */
    public void setBit(int bitIndex, boolean value) {
        int dataIndex = getDataIndex(bitIndex);
        int dataPosition = getDataPosition(bitIndex);

        // Do a different bitwise operation depending on the value being set
        if (value) {
            data[dataIndex] |= (1 << dataPosition);
        } else {
            data[dataIndex] &= ~(1 << dataPosition); 
        }
    }

    public void clear() {
        for (int i = 0; i < data.length; i++) {
            data[i] = 0;
        }
    }

    public int getSize() {
        return data.length * Byte.SIZE;
    }

    public byte[] getData() {
        return data;
    }

    protected int getDataIndex(int bitIndex) {
        // The index into the data array (i.e. which "long" is the bit located in)
        int dataIndex = (int) (bitIndex / Byte.SIZE);

        if (dataIndex > data.length) {
            throw new IndexOutOfBoundsException("Specified bit index [" + bitIndex
                    + "] is greater than the maximum allowed index [" + (getSize() - 1) + "]");
        }
        return dataIndex;
    }

    protected int getDataPosition(int bitIndex) {
        // The position of the bit within the long
        return bitIndex % Byte.SIZE;
    }
}