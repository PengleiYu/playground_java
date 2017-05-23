package state.approve;

import state.approve.base.LeaveRequestModel;
import state.approve.base.LeaveRequestState;
import state.approve.base.StateMachine;

import java.util.Scanner;

/**
 * Created by yupenglei on 17/5/19.
 */
public class ProjectManagerState implements LeaveRequestState {
    @Override
    public void doWork(StateMachine context) {
        //获取业务对象
        LeaveRequestModel model = (LeaveRequestModel) context.getBusinessVO();
        //业务处理,将结果保存到数据库
        projectProcess(model);

        //根据结果设置下一步
        if (LeaveRequestModel.RESULT.OK.equals(model.getResult())) {
            if (model.getLeaveDays() > 3) {
                //项目经理同意,且请假天数大于3,转入部门经理审批状态
                context.setState(new DepManagerState());
                context.doWork();
            } else {
                //请假天数小于3,转入审批结束状态
                context.setState(new AuditOverState());
                context.doWork();
            }
        } else {
            //项目经理未同意,转入审批结束状态
            context.setState(new AuditOverState());
            context.doWork();
        }
    }

    private void projectProcess(LeaveRequestModel model) {
        System.out.println("项目经理审批中...");
        System.out.println(String.format("%s 申请从 %s 开始请假%d天, 请项目经理审核（1为同意,2为不同意）:",
                model.getUser(), model.getBeginDate(), model.getLeaveDays()));

        Scanner scanner = new Scanner(System.in);
        if (scanner.hasNext()) {
            int a = scanner.nextInt();
            model.setResult(a == 1 ? LeaveRequestModel.RESULT.OK : LeaveRequestModel.RESULT.NO);
        }
    }
}
