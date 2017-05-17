package test_annotation;

/**
 * Created by yupenglei on 17/4/11.
 */
public class Testable {
    public void execute() {
        System.out.println("Executing ...");
    }

    @Test
    public void testExecute() {
        execute();
    }
}
