package state.approve.base;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Created by yupenglei on 17/5/19.
 */
@Data
@Accessors(prefix = "m", chain = true)
public class LeaveRequestModel {
    @ToString
    public enum RESULT {
        OK("OK"), NO("NO");

        private String mString;

        RESULT(String s) {
            mString = s;
        }
    }

    private String mUser;
    private String mBeginDate;
    private int mLeaveDays;
    private RESULT mResult;
}
