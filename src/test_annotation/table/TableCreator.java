package test_annotation.table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yupenglei on 17/4/14.
 */
public class TableCreator {
    public static void main(String[] args) throws ClassNotFoundException {
        if (args.length < 1) {
            System.out.println("no args");
            System.exit(0);
        }
        for (String className : args) {
            Class<?> cl = Class.forName(className);
            DBTable dbTable = cl.getAnnotation(DBTable.class);
            if (dbTable == null) {
                System.out.println("No DBTable annotation in class " + className);
                continue;
            }
            String tableName = dbTable.name();
            if (tableName.length() < 1) {
                tableName = cl.getName().toUpperCase();
            }
            List<String> columnDefs = new ArrayList<>();
            for (Field field : cl.getDeclaredFields()) {
                String columnName;
                if (field.getDeclaredAnnotations().length < 1) {
                    continue;
                }
                SQLInteger sqlInteger = field.getDeclaredAnnotation(SQLInteger.class);
                if (sqlInteger != null) {
                    if (sqlInteger.name().length() < 1) {
                        columnName = field.getName().toUpperCase();
                    } else {
                        columnName = sqlInteger.name();
                    }
                    columnDefs.add(columnName + " INT " + getConstraints(sqlInteger.constraints()));
                }
                SQLString sqlString = field.getDeclaredAnnotation(SQLString.class);
                if (sqlString != null) {
                    if (sqlString.name().length() < 1) {
                        columnName = field.getName().toUpperCase();
                    } else {
                        columnName = sqlString.name();
                    }
                    columnDefs.add(columnName + " VARCHAR(" + sqlString.value() + ")" + getConstraints(sqlString
                            .constraints()));
                }
            }
            StringBuilder createCommand = new StringBuilder("create table " + tableName + "(");
            for (String column : columnDefs) {
                createCommand.append("\n").append(column).append(",");
            }
            createCommand.deleteCharAt(createCommand.lastIndexOf(",")).append(")");
            System.out.println(createCommand);
        }
    }

    private static String getConstraints(Constraints con) {
        String constraints = " ";
        if (!con.allowNull())
            constraints += " NOT NULL";
        if (con.primaryKey())
            constraints += " PRIMARY KEY";
        if (con.unique())
            constraints += " UNIQUE";
        return constraints;
    }
}
