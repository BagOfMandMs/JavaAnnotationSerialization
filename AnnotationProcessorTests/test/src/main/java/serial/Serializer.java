package serial;


import SerializationRegistry.SerializationRegistry;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Serializer {
    private static final Map<Class<?>, Class<?>> WRAPPER_TYPE_MAP;
    private static int idGen = 1;
    static {
        WRAPPER_TYPE_MAP = new HashMap<Class<?>, Class<?>>(16);
        WRAPPER_TYPE_MAP.put(Integer.class, int.class);
        WRAPPER_TYPE_MAP.put(Byte.class, byte.class);
        WRAPPER_TYPE_MAP.put(Character.class, char.class);
        WRAPPER_TYPE_MAP.put(Boolean.class, boolean.class);
        WRAPPER_TYPE_MAP.put(Double.class, double.class);
        WRAPPER_TYPE_MAP.put(Float.class, float.class);
        WRAPPER_TYPE_MAP.put(Long.class, long.class);
        WRAPPER_TYPE_MAP.put(Short.class, short.class);
        WRAPPER_TYPE_MAP.put(Void.class, void.class);
    }

    public static boolean isPrimitiveWrapper(Class<?> source) {
        return WRAPPER_TYPE_MAP.containsKey(source);
    }

    public static Class<?> wrapperToPrimitive(Class<?> source){
        return WRAPPER_TYPE_MAP.get(source);
    }

    /*private static void serializePrimitiveField(Object o, Field f, Class<?> ft, SerializingOutputStream out) throws IllegalAccessException {
        if (ft == Integer.TYPE) {
            out.writeInt(f.getInt(o));
        } else if (ft == Boolean.TYPE) {
            out.write(f.getBoolean(o) ? 1 : 0);
        } else if (ft == Float.TYPE) {
            out.writeFloat(f.getFloat(o));
        } else if (ft == Double.TYPE) {
            out.writeDouble(f.getDouble(o));
        } else if (ft == Long.TYPE) {
            out.writeLong(f.getLong(o));
        } else if (ft == Character.TYPE) {
            out.write(f.getChar(o));
        } else if (ft == Byte.TYPE) {
            out.write(f.getByte(o));
        } else {
            LOGGING.logE("Serializer", "Unsupported primitive type " + ft);
        }
    }*/

    private static void serializePrimitive(Object o, Class<?> t, SerializingOutputStream out) {
        if (t == Integer.TYPE) {
            out.writeInt((int)o);
        } else if (t == Boolean.TYPE) {
            out.write(((boolean)o) ? 1 : 0);
        } else if (t == Float.TYPE) {
            out.writeFloat((float)o);
        } else if (t == Double.TYPE) {
            out.writeDouble((double)o);
        } else if (t == Long.TYPE) {
            out.writeLong((long)o);
        } else if (t == Character.TYPE) {
            out.write((char)o);
        } else if (t == Byte.TYPE) {
            out.write((byte)o);
        } else {
            System.out.println("Unsupported primitive type during serialization: " + t);
            //LOGGING.logE("Serializer", "Unsupported primitive type " + t);
        }
    }

    private static void serializePrimitiveWrapper(Object o, Class<?> t, SerializingOutputStream out) {
        if (t == Integer.class) {
            out.writeInt((Integer) o);
        } else if (t == Boolean.class) {
            out.write(((Boolean)o) ? 1 : 0);
        } else if (t == Float.class) {
            out.writeFloat((Float)o);
        } else if (t == Double.class) {
            out.writeDouble((Double)o);
        } else if (t == Long.class) {
            out.writeLong((Long)o);
        } else if (t == Character.class) {
            out.write((Character)o);
        } else if (t == Byte.class) {
            out.write((Byte)o);
        } else {
            System.out.println("Unsupported primitive type (wrapper) during serialization: " + t);
            //LOGGING.logE("Serializer", "Unsupported primitive wrapper type " + t);
        }
    }


    private static boolean initialized = false;
    public static void checkInitialized(){
        if(!initialized){
            SerializationRegistry.BuildSerializationMap();
            initialized = true;
        }
    }

    private static void Serialize(Object o, SerializingOutputStream out, HashMap<Object, Integer> sharedObjectMap) throws NonSerializableObjectException, IllegalAccessException {
        Class<?> oc = o.getClass();
        // Serializable class
        if(oc.isAnnotationPresent(Serial.class)){
            if(sharedObjectMap.containsKey(o)){
                out.writeInt(sharedObjectMap.get(o));
            }else{
                out.writeInt(-idGen);
                sharedObjectMap.put(o, idGen);
                idGen++;
                out.writeString(oc.getName());

                //System.out.println(oc.getName());
                for(Field f : oc.getFields()) {
                    // Deal with each field
                    if (f.isAnnotationPresent(Serial.class)) {
                        Serialize(f.get(o), out, sharedObjectMap);
                    }
                }
            }
        // Primitive wrapper
        } else if(isPrimitiveWrapper(oc)) {
            serializePrimitiveWrapper(o, oc, out);
        // Primitive
        } else if(oc.isPrimitive()){
            serializePrimitive(o, oc, out);
        // Array
        } else if(oc.isArray()) {
            out.writeInt(Array.getLength(o));
            for(int i = 0; i < Array.getLength(o); i++){
                Object otarget = Array.get(o, i);
                Serialize(otarget, out, sharedObjectMap);
            }
        // String
        } else if(oc == String.class) {
            out.writeString(String.valueOf(o));
        } else {
            throw new NonSerializableObjectException("Class " + o.getClass().toString() + " is not Serializable!");
        }
    }

    public static byte[] Serialize(Object o) throws NonSerializableObjectException, IllegalAccessException {
        checkInitialized();
        SerializingOutputStream out = new SerializingOutputStream();
        HashMap<Object, Integer> sharedObjectMap = new HashMap<>();
        Serialize(o, out, sharedObjectMap);
        return out.toByteArray();
    }

    public static <T> T Deserialize(byte[] data, Class<T> type) throws NonSerializableObjectException, SerializingInputStream.InvalidStreamLengthException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        checkInitialized();
        HashMap<Integer, Object> sharedObjectMap = new HashMap<>();
        return Deserialize(type, new SerializingInputStream(data), sharedObjectMap);
    }

    public static <T> T Deserialize(Class<T> type, SerializingInputStream in, HashMap<Integer, Object> sharedObjectMap) throws NonSerializableObjectException, SerializingInputStream.InvalidStreamLengthException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if(type.isAnnotationPresent(Serial.class)){
            int id = in.readInt();
            if(id < 0) {
                String typeName = in.readString();
                Class<T> trueType = (Class<T>) SerializationRegistry.classMap.get(typeName);
                Constructor<?> constructor = trueType.getConstructor();
                Object o = constructor.newInstance();
                for (Field f : trueType.getFields()) {
                    // Deal with each field
                    if (f.isAnnotationPresent(Serial.class)) {
                        Object e = Deserialize(f.getType(), in, sharedObjectMap);
                        f.set(o, e);
                    }
                }
                System.out.println("id: " + o.getClass());
                sharedObjectMap.put(-id, o);
                return (T) o;
            }else{
                return (T)sharedObjectMap.get(id);
            }
            // Primitive wrapper
        } else if(isPrimitiveWrapper(type)) {
            return (T) deserializePrimitiveWrapper(type, in);
            // Primitive
        } else if(type.isPrimitive()){
            return (T) deserializePrimitive(type, in);
            // Array
        } else if(type.isArray()) {
            int len = in.readInt();
            Object result = Array.newInstance(type.getComponentType(), len);
            for(int i = 0; i < len; i++){
                Object element = Deserialize(type.getComponentType(), in, sharedObjectMap);
                Array.set(result, i, element);
            }
            return (T) result;
            // String
        } else if(type == String.class) {
            return (T) in.readString();
        } else {
            throw new NonSerializableObjectException("Class " + type.toString() + " is not Serializable!");
        }
    }

    private static Object deserializePrimitive(Class<?> t, SerializingInputStream in) throws SerializingInputStream.InvalidStreamLengthException {
        if (t == Integer.TYPE) {
            return in.readInt();
        } else if (t == Boolean.TYPE) {
            return in.read() != 0;
        } else if (t == Float.TYPE) {
            return in.readFloat();
        } else if (t == Double.TYPE) {
            return in.readDouble();
        } else if (t == Long.TYPE) {
            return in.readLong();
        } else if (t == Character.TYPE) {
            return (char)in.read();
        } else if (t == Byte.TYPE) {
            return in.read();
        } else {
            System.out.println("Unsupported primitive type during serialization: " + t);
            //LOGGING.logE("Serializer", "Unsupported primitive type " + t);
        }
        return null;
    }

    private static Object deserializePrimitiveWrapper(Class<?> t, SerializingInputStream in) throws SerializingInputStream.InvalidStreamLengthException {
        if (t == Integer.class) {
            return in.readInt();
        } else if (t == Boolean.class) {
            return in.read() != 0;
        } else if (t == Float.class) {
            return in.readFloat();
        } else if (t == Double.class) {
            return in.readDouble();
        } else if (t == Long.class) {
            return in.readLong();
        } else if (t == Character.class) {
            return (char)in.read();
        } else if (t == Byte.class) {
            return in.read();
        } else {
            System.out.println("Unsupported primitive type during serialization: " + t);
            //LOGGING.logE("Serializer", "Unsupported primitive type " + t);
        }
        return null;
    }

    public static class NonSerializableObjectException extends Exception{
        NonSerializableObjectException(String msg){
            super(msg);
        }
    }

}
