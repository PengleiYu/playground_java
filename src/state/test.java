package state;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yupenglei on 17/5/18.
 */
public class test {
    public static void main(String[] args) {
        Map<String,String> map=new HashMap<>();
        String hello = map.remove("hello");
        System.out.println(hello);
    }
}
