package test_annotation;

import java.lang.reflect.Method;

/**
 * Created by yupenglei on 17/4/11.
 */
public class UseCaseTracker {
    private static void trackUseCases(Class<?> cl) {
        for (Method method : cl.getDeclaredMethods()) {
//            System.out.println(method.getName());
            UseCase useCase = method.getAnnotation(UseCase.class);
            if (useCase == null) {
                System.out.println("use case is null");
                continue;
            }
            System.out.println(String.format("id=%s, description=%s", useCase.id(), useCase.description()));
        }
    }

    public static void main(String[] args) {
        trackUseCases(PasswordUtil.class);
    }
}
