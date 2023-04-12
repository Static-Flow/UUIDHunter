package main.java.com.staticflow;

import com.eatthepath.uuid.FastUUID;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Utility class containing helper methods used throughout the Extension
 */
public final class Utils {

    private Utils(){}

    /**
     * Return a {@link UUID} if the supplied {@code value} is a valid V1 UUID
     * @param value A String representation of a UUID
     * @return A V1 {@link UUID} or null if {@code value} is not a V1 UUID
     */
    public static UUID parseUuidString(String value) {
        try {
            UUID uuid = FastUUID.parseUUID(value);
            if( uuid.version() == 1) {
                return uuid;
            } else {
                return null;
            }
        }catch (IllegalArgumentException ignored){
            return null;
        }
    }

    /**
     * Extract the timestamp from the UUID and convert it to yyyy-MM-dd HH:mm:ss.SSSSSS format
     * @param uuid The UUID to extract the timestamp from
     * @return The timestamp of the UUID in yyyy-MM-dd HH:mm:ss.SSSSSS format
     */
    public static String getUuidTime(UUID uuid) {
        LocalDateTime dtZero = LocalDateTime.of(1582, 10, 15, 0, 0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        return formatter.format(dtZero.plus(uuid.timestamp()/10L, ChronoUnit.MICROS));
    }

    /**
     * Extract the MAC address from the UUID
     * @param uuid The UUID to extract the MAC address from
     * @return the MAC address from the UUID
     */
    public static String getUuidMac(UUID uuid) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int shift = (5 - i) * 8;
            int octet = (int) ((uuid.node() >> shift) & 0xff);
            sb.append(String.format("%02x", octet));
            if (i < 5) {
                sb.append(":");
            }
        }
        return sb.toString();
    }

    /**
     * Convert a byte buffer into a UUID
     * @param bytes An array of bytes representing a UUID
     * @return A {@link UUID} created from the input byte array
     */
    public static UUID fromByteArray(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long msb = buffer.getLong();
        long lsb = buffer.getLong();
        return new UUID(msb, lsb);
    }

    /**
     * Convert a {@link UUID} into a byte array
     * @param uuid A {@link UUID} to be converted into a byte array
     * @return A byte array representation of a {@link UUID}
     */
    public static byte[] toByteArray(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    /**
     * Generates an iterable sequence of UUIDs starting from {@code seconds} before the {@code providedUuid} to {@code seconds} after.
     * @param providedUuid the UUID from which to generate the sequence
     * @param precision the increment value to use when generating the sequence
     * @param seconds the number of seconds before and after the timestamp of {@code providedUuid} to start and stop the sequence
     * @return an iterable sequence of {@link UUID UUIDs}
     *
     */
    public static Iterable<UUID> genUUIDs(UUID providedUuid, long precision, int seconds) {


        return () -> new Iterator<>() {
            //Convert the initial UUID into a byte array
            final byte[] pastUuidBytes = Utils.toByteArray(providedUuid);
            //Extract the lower 32 bits of the timestamp
            final long timestamp = (((long) pastUuidBytes[0] << 56) |
                    ((long) (pastUuidBytes[1] & 0xFF) << 48) |
                    ((long) (pastUuidBytes[2] & 0xFF) << 40) |
                    ((long) (pastUuidBytes[3] & 0xFF) << 32));
            //Shift the bits to make it easily manageable
            final long lowerTimestamp = (timestamp >> 32) & 0xFFFFFFFFL;
            //create the boundaries of the sequence
            final long end =  lowerTimestamp + 10000000L*seconds;
            long start = lowerTimestamp - 10000000L*seconds;

            @Override
            public boolean hasNext() {
                return start <= end;
            }

            @Override
            public UUID next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                //update the lower 32 bits of the timestamp with the next sequence value
                pastUuidBytes[0] = (byte) (start >> 24);
                pastUuidBytes[1] = (byte) (start >> 16);
                pastUuidBytes[2] = (byte) (start >> 8);
                pastUuidBytes[3] = (byte) (start);
                //convert the byte array back to a UUID
                UUID uuid = Utils.fromByteArray(pastUuidBytes);
                start += precision;
                return uuid;
            }
        };
    }
}
