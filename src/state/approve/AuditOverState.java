package state.approve;

import state.approve.base.LeaveRequestModel;
import state.approve.base.LeaveRequestState;
import state.approve.base.StateMachine;

/**
 * Created by yupenglei on 17/5/19.
 */
public class AuditOverState implements LeaveRequestState {
    @Override
    public void doWork(StateMachine context) {
        //获取业务对象
        LeaveRequestModel model = (LeaveRequestModel) context.getBusinessVO();
        //业务处理,将结果保存到数据库,并记录整个流程结束
        System.out.println(String.format("%s, 你的请假审批已结束,结果是: %s", model.getUser(), model.getResult()));
    }
}
