package state;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yupenglei on 17/5/19.
 */
@Accessors(prefix = "m")
public class VoteManager2 {
    @Getter
    private Map<String, String> mVoteMap = new HashMap<>();
    @Getter
    private Map<String, VoteState> mVoteStateMap = new HashMap<>();
    @Getter
    private Map<String, Integer> mVoteCountMap = new HashMap<>();

    public void vote(String user, String item) {
        VoteState voteState = mVoteStateMap.get(user);
        if (voteState == null) {
            voteState = new NormalVoteState();
        }
        voteState.vote(user, item, this);
    }

    public static void main(String[] args) {
        VoteManager2 voteManager = new VoteManager2();
        for (int i = 0; i < 10; i++) {
            voteManager.vote("Tome", "B");
        }
    }

}

interface VoteState {
    void vote(String user, String item, VoteManager2 voteManager);
}

class NormalVoteState implements VoteState {
    @Override
    public void vote(String user, String item, VoteManager2 voteManager) {
        voteManager.getVoteCountMap().put(user, 1);
        voteManager.getVoteMap().put(user, item);
        voteManager.getVoteStateMap().put(user, new RepeatVoteState());
        System.out.println("恭喜,投票成功");
    }
}

class RepeatVoteState implements VoteState {
    @Override
    public void vote(String user, String item, VoteManager2 voteManager) {
        System.out.println("请勿重复投票");
        Integer count = voteManager.getVoteCountMap().get(user);
        voteManager.getVoteCountMap().put(user, ++count);
        if (count >= 4) {
            voteManager.getVoteStateMap().put(user, new SpiteVoteState());
        }
    }
}

class SpiteVoteState implements VoteState {
    @Override
    public void vote(String user, String item, VoteManager2 voteManager) {
        System.out.println("恶意刷票,取消投票资格");
        voteManager.getVoteMap().remove(user);
        Integer count = voteManager.getVoteCountMap().get(user);
        voteManager.getVoteCountMap().put(user, ++count);
        if (count >= 7) {
            voteManager.getVoteStateMap().put(user, new BlackVoteState());
        }
    }
}

class BlackVoteState implements VoteState {
    @Override
    public void vote(String user, String item, VoteManager2 voteManager) {
        Integer count = voteManager.getVoteCountMap().get(user);
        voteManager.getVoteCountMap().put(user, ++count);
        System.out.println("已进入黑名单,进制登录和使用");
    }
}
