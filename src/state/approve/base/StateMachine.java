package state.approve.base;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by yupenglei on 17/5/19.
 */
@Data
@Accessors(prefix = "m", chain = true)
public class StateMachine {

    private State mState = null;
    private Object mBusinessVO = null;


    public void doWork() {
        mState.doWork(this);
    }
}
