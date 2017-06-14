package network;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.util.Arrays;

/**
 * Created by yupenglei on 17/6/8.
 */
public class TestBuffer {
    public static void main(String[] args) {
//        test1();
//        test2();
//        endians();
        testMark();
    }

    private static void testMark() {
        ByteBuffer byteBuffer = ByteBuffer.wrap("abcdef".getBytes());
        System.out.println(byteBuffer.get());
        System.out.println(byteBuffer.get());
        System.out.println(byteBuffer.position());
        byteBuffer.mark();
        System.out.println(byteBuffer.get());
        System.out.println(byteBuffer.get());
        System.out.println(byteBuffer.get());
        System.out.println(byteBuffer.position());
        byteBuffer.reset();
        System.out.println(byteBuffer.position());
        System.out.println(byteBuffer.get(0));
        System.out.println(byteBuffer.position());
    }

    private static void endians() {
        String s = "abcdef";
        ByteBuffer byteBuffer = ByteBuffer.allocate(12);
        byteBuffer.asCharBuffer().put(s);
        System.out.println(Arrays.toString(byteBuffer.array()));

        byteBuffer.clear();
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.asCharBuffer().put(s);
        System.out.println(Arrays.toString(byteBuffer.array()));

        byteBuffer.clear();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.asCharBuffer().put(s);
        System.out.println(Arrays.toString(byteBuffer.array()));
    }

    private static void test2() {
        String s = "1234 56";
        System.out.println(s.split(" "));
    }


    private static void test1() {
        ByteBuffer byteBuffer = ByteBuffer.wrap("hello world".getBytes());
        System.out.println(Arrays.toString(byteBuffer.array()));
    }

}
