package proxy;

import java.io.*;

public class Serializer {

    public static byte[] saveObject(Serializable object) throws IOException {
        byte[] bytes = new byte[0];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            bytes = bos.toByteArray();
        }
        catch (Exception exception){
            System.out.println(exception.getMessage());
        }
        finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return bytes;
    }

    public static Object loadObject(byte[] bytes) throws ClassNotFoundException, IOException {
        Object o;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            o = in.readObject();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return o;
    }
}