package state.approve;

import state.approve.base.LeaveRequestModel;
import state.approve.base.LeaveRequestState;
import state.approve.base.StateMachine;

import java.util.Scanner;

/**
 * Created by yupenglei on 17/5/19.
 */
public class DepManagerState implements LeaveRequestState {
    @Override
    public void doWork(StateMachine context) {
        //获取业务对象
        LeaveRequestModel model = (LeaveRequestModel) context.getBusinessVO();
        //业务处理,将结果保存到数据库
        depProcess(model);

        //部门经理审批完成,转入审批结束状态
        context.setState(new AuditOverState());
        context.doWork();
    }

    private void depProcess(LeaveRequestModel model) {
        System.out.println("部门经理审核中...");
        System.out.println(String.format("%s 申请从 %s 开始请假%d天,请部门经理审核（1为同意,2为不同意）:",
                model.getUser(), model.getBeginDate(), model.getLeaveDays()));

        Scanner scanner = new Scanner(System.in);
        if (scanner.hasNext()) {
            int a = scanner.nextInt();
            model.setResult(a == 1 ? LeaveRequestModel.RESULT.OK : LeaveRequestModel.RESULT.NO);
        }
    }
}
