package state;

/**
 * Created by yupenglei on 17/5/17.
 */
public class AirConditionTest {
    private static final int STATE_OFF = 0;
    private static final int STATE_FAN_ONLY = 1;
    private static final int STATE_COOL = 2;

    private static final String[] sStates = {"STATE_OFF", "STATE_FAN_ONLY", "STATE_COOL"};

    private static class AirCondition1 {
        private int mState;

        AirCondition1() {
            mState = STATE_OFF;
        }

        void power() {
            int pre = mState;
            switch (mState) {
                case STATE_OFF:
                    mState = STATE_FAN_ONLY;
                    break;
                case STATE_FAN_ONLY:
                case STATE_COOL:
                    mState = STATE_OFF;
                    break;
            }
            printState(pre);
        }

        void cool() {
            int pre = mState;
            switch (mState) {
                case STATE_OFF:
                    break;
                case STATE_FAN_ONLY:
                    mState = STATE_COOL;
                    break;
                case STATE_COOL:
                    mState = STATE_FAN_ONLY;
                    break;
            }
            printState(pre);
        }

        private void printState(int pre) {
            System.out.println(String.format("%-15s -> %-15s", sStates[pre], sStates[mState]));
        }
    }

    public static void main(String[] args) {
        AirCondition1 airCondition = new AirCondition1();
        airCondition.power();
        airCondition.cool();
        airCondition.power();
        airCondition.cool();
        airCondition.power();
        airCondition.cool();
        airCondition.cool();
    }
}
