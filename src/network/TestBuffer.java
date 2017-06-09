package network;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

/**
 * Created by yupenglei on 17/6/8.
 */
public class TestBuffer {
    public static void main(String[] args) {
//        test1();
        test2();
    }
    private static void test2(){
        String s="1234 56";
        System.out.println(s.split(" "));
    }


    private static void test1() {
        ByteBuffer byteBuffer = ByteBuffer.wrap("hello world".getBytes());
        System.out.println(Arrays.toString(byteBuffer.array()));
    }

}
