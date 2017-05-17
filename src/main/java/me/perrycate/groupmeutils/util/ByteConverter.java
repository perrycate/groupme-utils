package me.perrycate.groupmeutils.util;

import java.io.*;

public class ByteConverter {

    public static byte[] objectToBytes(Object o) {
        // Mostly from
        // http://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] output = new byte[0];
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(o);
            out.flush();
            output = bos.toByteArray();
            bos.close();
        } catch (IOException e) {
            System.err.println(e);
        }

        return output;
    }

    public static Object bytesToObject(byte[] b) {
        // Mostly from
        // http://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
        ByteArrayInputStream bis = new ByteArrayInputStream(b);
        ObjectInput in = null;
        Object output = null;
        try {
            in = new ObjectInputStream(bis);
            output = in.readObject();
            in.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return output;
    }
}
