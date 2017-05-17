package test_annotation;

/**
 * Created by yupenglei on 17/4/11.
 */
public class PasswordUtil {
    @UseCase(id = 11, description = "this is 1")
    public boolean method1() {
        return true;
    }

    @UseCase(id = 12, description = "this is 2")
    public boolean method2() {
        return true;
    }

    @UseCase(id = 13)
    public boolean method3() {
        return true;
    }
}
