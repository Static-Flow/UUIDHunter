package test.java;


import main.java.com.staticflow.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class UtilsTest {

    public static UUID generateOneSecondFutureUUID(UUID uuid, long change) {
        byte[] uuidBytes = Utils.toByteArray(uuid);
        long timestamp = ((long) uuidBytes[0] << 56) |
                ((long) (uuidBytes[1] & 0xFF) << 48) |
                ((long) (uuidBytes[2] & 0xFF) << 40) |
                ((long) (uuidBytes[3] & 0xFF) << 32);
        System.out.printf("%X%n",timestamp);

        long lower32 = (timestamp >> 32) & 0xFFFFFFFFL;
        System.out.printf("time high: %X%n",lower32);
        lower32 += change;
        System.out.printf("time high: %X%n",lower32);
        timestamp = lower32 << 32;
        System.out.printf("%X%n",timestamp);

        uuidBytes[0] = (byte) (timestamp >> 56);
        uuidBytes[1] = (byte) (timestamp >> 48);
        uuidBytes[2] = (byte) (timestamp >> 40);
        uuidBytes[3] = (byte) (timestamp >> 32);
        return Utils.fromByteArray(uuidBytes);
    }


    @Test
    void genUUIDs() {
        UUID initUuid = UUID.fromString("95f6e264-bb00-11ec-8833-00155d01ef00");
;
        UUID pastUuid = generateOneSecondFutureUUID(initUuid,-10000000L);

        UUID futureUuid = generateOneSecondFutureUUID(initUuid,10000000L);

        System.out.println("Past UUID: "+pastUuid);
        System.out.println("Init UUID: "+initUuid);
        System.out.println("Future UUID: "+futureUuid);

        boolean foundStart = false;
        boolean foundEnd = false;
        Iterable<UUID> possibleUUIDs = Utils.genUUIDs(initUuid, 10000L, 1);
        for(UUID possibleUuid : possibleUUIDs) {
           if(possibleUuid.toString().equals(pastUuid.toString())) {
               foundStart = true;
           }
            if(possibleUuid.toString().equals(futureUuid.toString())) {
                foundEnd = true;
            }
        }
        Assertions.assertTrue(foundStart && foundEnd);
    }
}