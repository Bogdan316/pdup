import com.intellij.psi.JavaTokenType;
import org.junit.Assert;
import org.junit.Test;
import upt.baker.pdup.duplications.Pdup;
import upt.baker.pdup.duplications.PdupToken;

import java.util.*;


public class PdupTest {

    @Test
    public void testPrev() {
        // prev(ubvbubv$) == 0b0b4b4$
        int c = -JavaTokenType.IF_KEYWORD.getIndex();
        int e = Integer.MIN_VALUE;
        var u = new PdupToken(0, 0, 0);
        var b = new PdupToken(c, 0, 0);
        var v = new PdupToken(1, 0, 0);
        var $ = new PdupToken(e, 0, 0);
        var string = new ArrayList<>(List.of(u, b, v, b, u, b, v, $));
        Assert.assertArrayEquals(new int[]{0, c, 0, c, 4, c, 4, e}, Pdup.prev(string));
    }

    @Test
    public void tetRev() {
        int c = -JavaTokenType.IF_KEYWORD.getIndex();
        int e = Integer.MIN_VALUE;
        var u = new PdupToken(0, 0, 0);
        var b = new PdupToken(c, 0, 0);
        var v = new PdupToken(1, 0, 0);
        var $ = new PdupToken(e, 0, 0);
        var string = new ArrayList<>(List.of(u, b, v, b, u, b, v, $));
        Assert.assertArrayEquals(new int[]{4, c, 4, c, 0, c, 0, e}, Pdup.rev(Pdup.prev(string)));
    }

    private void depth(Pdup.Node n, List<Integer> path, PriorityQueue<List<Integer>> paths) {
        if (n.isLeaf()) {
            paths.add(path.subList(0, path.size()));
            return;
        }
        for (var c : n.getChildren()) {
            var p = new ArrayList<>(path);
            p.addAll(c.value.getPsub());
            depth(c.value, p, paths);
        }
    }

    private Map<Integer, List<Integer>> getPSuffixes(List<PdupToken> string) {
        var prev = Pdup.prev(string);
        var m = new HashMap<Integer, List<Integer>>();
        for (int i = 0; i < prev.length; i++) {
            var l = new ArrayList<Integer>();
            for (int j = 0; j < prev.length - i; j++) {
                l.add(Pdup.f(prev[i + j], j));
            }
            m.put(l.size(), l);
        }

        return m;
    }

    @Test
    public void testBuild() {
        int c = -JavaTokenType.IF_KEYWORD.getIndex();
        int e = Integer.MIN_VALUE;
        var u = new PdupToken(0, 0, 0);
        var b = new PdupToken(c, 0, 0);
        var v = new PdupToken(1, 0, 0);
        var $ = new PdupToken(e, 0, 0);
        var string = new ArrayList<>(List.of(u, b, v, b, u, b, v, $));

        var dup = new Pdup(5, string);
        dup.build();
        var r = dup.getTree();
        var paths = new PriorityQueue<List<Integer>>(Comparator.comparingInt(List::size));
        depth(r, new ArrayList<>(), paths);

        var suffs = getPSuffixes(string);

        // check that we have all the p-suffixes
        Assert.assertEquals(paths.size(), suffs.size());

        // check that all the p-suffixes from the tree match the expected p-suffixes
        while (!paths.isEmpty()) {
            var p = paths.poll();
            Assert.assertEquals(p, suffs.get(p.size()));
        }
    }

    @Test
    public void testPdup() {
        int c = -JavaTokenType.IF_KEYWORD.getIndex();
        int e = Integer.MIN_VALUE;
        var u = new PdupToken(0, 0, 0);
        var b = new PdupToken(c, 0, 0);
        var v = new PdupToken(1, 0, 0);
        var $ = new PdupToken(e, 0, 0);
        var string = new ArrayList<>(List.of(u, b, v, b, u, b, v, $));

        var dup = new Pdup(1, string);
        var ranges = dup.pdup();
        for (var r : ranges) {
            Assert.assertArrayEquals(Pdup.prev(string.subList(r.lftStartOff(), r.lftEndOff())), Pdup.prev(string.subList(r.rhtStartOff(), r.rhtEndOff())));
        }
    }
}