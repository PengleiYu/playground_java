package state.approve;

import state.approve.base.LeaveRequestContext;
import state.approve.base.LeaveRequestModel;

import java.util.Scanner;

/**
 * Created by yupenglei on 17/5/19.
 */
public class Client {
    public static void main(String[] args) {

        LeaveRequestModel model =
                new LeaveRequestModel()
                        .setUser("Tome")
                        .setBeginDate("2017.5.5");
        readLeaveDays(model);

        new LeaveRequestContext()
                .setBusinessVO(model)
                .setState(new ProjectManagerState())
                .doWork();
    }

    private static void readLeaveDays(LeaveRequestModel model) {
        System.out.println("请输入请假天数:");
        Scanner scanner = new Scanner(System.in);
        if (scanner.hasNext()) {
            int a = scanner.nextInt();
            model.setLeaveDays(a);
        }
    }
}
