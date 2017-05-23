package state;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yupenglei on 17/5/18.
 */
public class VoteManager {
    private Map<String, String> mVoteMap = new HashMap<>();
    private Map<String, Integer> mVoteCountMap = new HashMap<>();


    public interface VoteState {
        void vote(String user, String voteItem, VoteManager manager);
    }

    public class NormalVoteState implements VoteState {
        @Override
        public void vote(String user, String voteItem, VoteManager manager) {
            manager.mVoteMap.put(user, voteItem);
            System.out.println("投票成功");
        }
    }

    public class RepeatVoteState implements VoteState {
        @Override
        public void vote(String user, String voteItem, VoteManager manager) {
            System.out.println("请勿重复投票");
        }
    }

    public class SpiteVoteState implements VoteState {
        @Override
        public void vote(String user, String voteItem, VoteManager manager) {
            manager.mVoteMap.remove(user);
            System.out.println("恶意投票,取消投票资格");
        }
    }

    public class BlackVoteState implements VoteState {
        @Override
        public void vote(String user, String voteItem, VoteManager manager) {
            System.out.println("已进入黑名单, 禁止登录和使用");
        }
    }

    public void vote(String user, String voteItem) {
        Integer count = mVoteCountMap.get(user);
        if (count == null) {
            count = 0;
        }
        mVoteCountMap.put(user, ++count);

        VoteState voteState;
        if (count == 1) {
            voteState = new NormalVoteState();
        } else if (count > 1 && count < 5) {
            voteState = new RepeatVoteState();
        } else if (count >= 5 && count < 8) {
            voteState = new SpiteVoteState();
        } else {
            voteState = new BlackVoteState();
        }
        voteState.vote(user, voteItem, this);
    }

    public static void main(String[] args) {
        VoteManager voteManager = new VoteManager();
        for (int i = 0; i < 10; i++) {
            System.out.println(String.format("第%d次投票", (i + 1)));
            voteManager.vote("Tom", "A");
        }
    }

}
