package test_annotation.table;

/**
 * Created by yupenglei on 17/4/14.
 */
public @interface SQLString {
    int value() default 0;

    String name() default "";

    Constraints constraints() default @Constraints;
}
