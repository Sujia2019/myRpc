package utils;

import java.io.*;

public class SerializableUtil {


    public static byte[] toByteArray(Object obj){
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
        try{
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        }catch (IOException ex){
            ex.printStackTrace();
        }
        return bytes;
    }

    public static Object getObject(byte[] src) {
        Object res = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(src);
        try{
            ObjectInputStream ois = new ObjectInputStream(bis);
            res = ois.readObject();
            bis.close();
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return res;
    }

}
