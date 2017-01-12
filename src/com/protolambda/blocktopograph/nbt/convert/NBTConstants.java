package com.protolambda.blocktopograph.nbt.convert;

import com.protolambda.blocktopograph.nbt.tags.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NBTConstants {

    public enum NBTType {

        END(0, EndTag.class, "EndTag"),
        BYTE(1, ByteTag.class, "ByteTag"),
        SHORT(2, ShortTag.class, "ShortTag"),
        INT(3, IntTag.class, "IntTag"),
        LONG(4, LongTag.class, "LongTag"),
        FLOAT(5, FloatTag.class, "FloatTag"),
        DOUBLE(6, DoubleTag.class, "DoubleTag"),
        BYTE_ARRAY(7, ByteArrayTag.class, "ByteArrayTag"),
        STRING(8, StringTag.class, "StringTag"),
        LIST(9, ListTag.class, "ListTag"),
        COMPOUND(10, CompoundTag.class, "CompoundTag"),
        INT_ARRAY(11, IntArrayTag.class, "IntArrayTag"),
        //Is this one even used?!? Maybe used in mods?
        SHORT_ARRAY(100, ShortArrayTag.class, "ShortArrayTag");

        public final int id;
        public final Class<? extends Tag> tagClazz;
        public final String displayName;

        static public Map<Integer, NBTType> typesByID = new HashMap<>();
        static public Map<Class<? extends Tag>, NBTType> typesByClazz = new HashMap<>();

        NBTType(int id, Class<? extends Tag> tagClazz, String displayName) {
            this.id = id;
            this.tagClazz = tagClazz;
            this.displayName = displayName;
        }

        //not all NBT types are meant to be created in an editor, the END tag for example.
        public static String[] editorOptions_asString;
        public static NBTType[] editorOptions_asType = new NBTType[]{
            BYTE,
            SHORT,
            INT,
            LONG,
            FLOAT,
            DOUBLE,
            BYTE_ARRAY,
            STRING,
            LIST,
            COMPOUND,
            INT_ARRAY
        //SHORT_ARRAY // Short Array isn't an official tag and shouldn't be supported
        };

        static {

            int len = editorOptions_asType.length;
            editorOptions_asString = new String[len];
            for (int i = 0; i < len; i++) {
                editorOptions_asString[i] = editorOptions_asType[i].displayName;
            }

            //fill maps
            for (NBTType type : NBTType.values()) {
                typesByID.put(type.id, type);
                typesByClazz.put(type.tagClazz, type);
            }
        }

        public static Tag newInstance(String tagName, NBTType type) {
            switch (type) {
                case END:
                    return new EndTag();
                case BYTE:
                    return new ByteTag(tagName, (byte) 0);
                case SHORT:
                    return new ShortTag(tagName, (short) 0);
                case INT:
                    return new IntTag(tagName, 0);
                case LONG:
                    return new LongTag(tagName, 0L);
                case FLOAT:
                    return new FloatTag(tagName, 0f);
                case DOUBLE:
                    return new DoubleTag(tagName, 0.0);
                case BYTE_ARRAY:
                    return new ByteArrayTag(tagName, new byte[]{0});
                case STRING:
                    return new StringTag(tagName, "");
                case LIST:
                    return new ListTag(tagName, new ArrayList<Tag>(), NBTType.BYTE);
                case COMPOUND:
                    return new CompoundTag(tagName, new ArrayList<Tag>());
                case INT_ARRAY:
                    return new IntArrayTag(tagName, new int[]{0});
                case SHORT_ARRAY:
                    return new ShortArrayTag(tagName, new short[]{0});
                default:
                    return null;
            }
        }

        public static Object getDefaultValue(NBTType type) {
            switch (type) {
                case END:
                    return null;
                case BYTE:
                    return (byte) 0;
                case SHORT:
                    return (short) 0;
                case INT:
                    return 0;
                case LONG:
                    return 0L;
                case FLOAT:
                    return 0f;
                case DOUBLE:
                    return 0.0;
                case BYTE_ARRAY:
                    return new byte[]{0};
                case STRING:
                    return "";
                case LIST:
                    return new ArrayList<Tag>();
                case COMPOUND:
                    return new ArrayList<Tag>();
                case INT_ARRAY:
                    return new int[]{0};
                case SHORT_ARRAY:
                    return new short[]{0};
                default:
                    return null;
            }
        }

        private static byte[] parseByteArray(String array) {
            String text = array.replaceAll("[\\[\\]]", "");
            String[] arrayText = text.split(",");
            byte[] arrayData = new byte[arrayText.length];

            for (int i = 0; i < arrayData.length; i++) {
                arrayData[i] = Byte.parseByte(arrayText[i].trim());
            }

            return arrayData;
        }

        private static int[] parseIntArray(String array) {
            String text = array.replaceAll("[\\[\\]]", "");
            String[] arrayText = text.split(",");
            int[] arrayData = new int[arrayText.length];

            for (int i = 0; i < arrayData.length; i++) {
                arrayData[i] = Integer.parseInt(arrayText[i].trim());
            }

            return arrayData;
        }

        private static short[] parseShortArray(String array) {
            String text = array.replaceAll("[[\\[\\]]", "");
            String[] arrayText = text.split(",");
            short[] arrayData = new short[arrayText.length];

            for (int i = 0; i < arrayData.length; i++) {
                arrayData[i] = Short.parseShort(arrayText[i].trim());
            }

            return arrayData;
        }

        public static Object parseValue(String value, NBTType type) {
            value = value.trim();
            switch (type) {
                case END:
                    return null;
                case BYTE:
                    return Byte.parseByte(value);
                case SHORT:
                    return Short.parseShort(value);
                case INT:
                    return Integer.parseInt(value);
                case LONG:
                    return Long.parseLong(value);
                case FLOAT:
                    return Float.parseFloat(value);
                case DOUBLE:
                    return Double.parseDouble(value);
                case BYTE_ARRAY:
                    return parseByteArray(value);
                case STRING:
                    return value;
                case LIST:
                    return new ArrayList<Tag>(); // Ignore
                case COMPOUND:
                    return new ArrayList<Tag>(); // Ignore
                case INT_ARRAY:
                    return parseIntArray(value);
                case SHORT_ARRAY:
                    return parseShortArray(value);
                default:
                    return null;
            }
        }
    }

    public static final Charset CHARSET = Charset.forName("UTF-8");
}
