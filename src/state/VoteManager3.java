package state;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yupenglei on 17/5/19.
 */
@Accessors(prefix = "m")
public class VoteManager3 {
    @Getter
    private Map<String, String> mVoteMap = new HashMap<>();
    @Getter
    private Map<String, VoteState3> mVoteStateMap = new HashMap<>();

    public void vote(String user, String item) {
        VoteState3 voteState = mVoteStateMap.get(user);
        if (voteState == null) {
            voteState = new NormalVoteState3(user, item, this);
        }
        voteState.vote(user, item, this);
    }

    public static void main(String[] args) {
        VoteManager3 voteManager = new VoteManager3();
        for (int i = 0; i < 10; i++) {
            voteManager.vote("Tom", "A");
        }
    }
}

abstract class VoteState3 {
    public VoteState3(String user, String item, VoteManager3 voteManager, int count) {
        mCount = count;
    }

    protected int mCount = 0;

    public void vote(String user, String item, VoteManager3 voteManager) {
        System.out.println(String.format("count=%d,this=%s", mCount, this));
        enter(user, item, voteManager);
        if (process(user, item, voteManager))
            voteManager.getVoteStateMap().put(user, getNextState(user, item, voteManager));
    }

    abstract void enter(String user, String item, VoteManager3 voteManager);

    abstract boolean process(String user, String item, VoteManager3 voteManager);

    abstract VoteState3 getNextState(String user, String item, VoteManager3 voteManager);
}

class NormalVoteState3 extends VoteState3 {
    public NormalVoteState3(String user, String item, VoteManager3 voteManager) {
        super(user, item, voteManager, 0);
        voteManager.getVoteStateMap().put(user, this);
    }

    @Override
    void enter(String user, String item, VoteManager3 voteManager) {
        voteManager.getVoteMap().put(user, item);
        System.out.println("投票成功");
    }

    @Override
    boolean process(String user, String item, VoteManager3 voteManager) {
        return ++mCount > 1;
    }

    @Override
    VoteState3 getNextState(String user, String item, VoteManager3 voteManager) {
        return new RepeatVoteState3(user, item, voteManager, mCount);
    }
}

class RepeatVoteState3 extends VoteState3 {
    public RepeatVoteState3(String user, String item, VoteManager3 voteManager, int count) {
        super(user, item, voteManager, count);
    }

    @Override
    void enter(String user, String item, VoteManager3 voteManager) {
        System.out.println("请勿重复投票");
    }

    @Override
    boolean process(String user, String item, VoteManager3 voteManager) {
        return ++mCount >= 5;
    }

    @Override
    VoteState3 getNextState(String user, String item, VoteManager3 voteManager) {
        return new SpiteVoteState3(user, item, voteManager, mCount);
    }
}

class SpiteVoteState3 extends VoteState3 {
    public SpiteVoteState3(String user, String item, VoteManager3 voteManager, int count) {
        super(user, item, voteManager, count);
    }

    @Override
    void enter(String user, String item, VoteManager3 voteManager) {
        voteManager.getVoteMap().remove(user);
        System.out.println("恶意刷票,取消投票资格");
    }

    @Override
    boolean process(String user, String item, VoteManager3 voteManager) {
        return ++mCount >= 8;
    }

    @Override
    VoteState3 getNextState(String user, String item, VoteManager3 voteManager) {
        return new BlackVoteState3(user, item, voteManager, mCount);
    }
}

class BlackVoteState3 extends VoteState3 {
    public BlackVoteState3(String user, String item, VoteManager3 voteManager, int count) {
        super(user, item, voteManager, count);
    }

    @Override
    void enter(String user, String item, VoteManager3 voteManager) {
        System.out.println("已进入黑名单,禁止登录和使用");
    }

    @Override
    boolean process(String user, String item, VoteManager3 voteManager) {
        mCount++;
        return false;
    }

    @Override
    VoteState3 getNextState(String user, String item, VoteManager3 voteManager) {
        return null;
    }
}

