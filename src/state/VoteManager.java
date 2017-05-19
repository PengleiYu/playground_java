package state;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yupenglei on 17/5/18.
 */
public class VoteManager {
    private Map<String, String> mVoteMap = new HashMap<>();
    private Map<String, Integer> mVoteCountMap = new HashMap<>();
    private VoteState mVoteState = null;


    public interface VoteState {
        void vote(String user, String voteItem, VoteManager manager);
    }

    public class NomalVoteState implements VoteState {
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

    public class SpliteVoteState implements VoteState {
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

        if (count == 1) {
            mVoteState = new NomalVoteState();
        } else if (count > 1 && count < 5) {
            mVoteState = new RepeatVoteState();
        } else if (count >= 5 && count < 8) {
            mVoteState = new SpliteVoteState();
        } else {
            mVoteState = new BlackVoteState();
        }
        mVoteState.vote(user, voteItem, this);
    }

    public static void main(String[] args) {
        VoteManager voteManager = new VoteManager();
        for (int i = 0; i < 10; i++) {
            System.out.println(String.format("第%d次投票", (i + 1)));
            voteManager.vote("Tom", "A");
        }
    }

}
