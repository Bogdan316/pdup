package upt.baker.pdup.regex.vm;

import java.util.ArrayList;
import java.util.List;

public class MatchGroups {
    public record Group(int start, int len) {
        @Override
        public String toString() {
            return "Group{" +
                    "start=" + start +
                    ", len=" + len +
                    '}';
        }
    }

    private final List<Group> groups;

    public MatchGroups(int[] groupsIdx) {
        groups = buildGroups(groupsIdx);
    }

    private List<Group> buildGroups(int[] groupsIdx) {
        var grs = new ArrayList<Group>();
        for (int i = 0; i < (groupsIdx.length - 1) / 2; i++) {
            int len = groupsIdx[2 * i + 1] - groupsIdx[2 * i];
            if (len != 0) {
                grs.add(new Group(groupsIdx[2 * i], len));
            }
        }
        return grs;
    }

    public List<Group> getGroups() {
        return groups;
    }
}
