package state;

/**
 * Created by yupenglei on 17/5/17.
 */
public class AirConditionTest {
    private static final int STATE_OFF = 0;
    private static final int STATE_FAN_ONLY = 1;
    private static final int STATE_COOL = 2;

    private static final String[] sStates = {"STATE_OFF", "STATE_FAN_ONLY", "STATE_COOL"};

    private static class AirCondition {
        private int mState;

        AirCondition() {
            mState = STATE_OFF;
            printState();
        }

        void power() {
            switch (mState) {
                case STATE_OFF:
                    mState = STATE_FAN_ONLY;
                    break;
                case STATE_FAN_ONLY:
                case STATE_COOL:
                    mState = STATE_OFF;
                    break;
            }
            printState();
        }

        void cool() {
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
            printState();
        }

        private void printState() {
            System.out.println(sStates[mState]);
        }
    }

    public static void main(String[] args) {
        AirCondition airCondition = new AirCondition();
        airCondition.power();
        airCondition.cool();
        airCondition.power();
        airCondition.cool();
        airCondition.power();
        airCondition.cool();
        airCondition.cool();
    }
}
