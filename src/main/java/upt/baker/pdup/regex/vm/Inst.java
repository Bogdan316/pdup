package upt.baker.pdup.regex.vm;

import com.intellij.psi.JavaTokenType;
import upt.baker.pdup.utils.KeywordMapping;


public abstract class Inst {

    private Inst() {
    }


    public static class LitInst extends Inst {
        public final int val;

        public LitInst(int val) {
            this.val = val;
        }

        public boolean isMatching(int idx) {
            // if val == IDENTIFIER we are matching any IDENTIFIER (e.g. idx >= 0)
            return (val == JavaTokenType.IDENTIFIER.getIndex() && idx >= 0) || idx == val;
        }

        @Override
        public String toString() {
            return "LitInst{" +
                    "val='" + KeywordMapping.getName(val) + '\'' +
                    '}';
        }
    }

    public static class JmpInst extends Inst {
        public int label = -1;

        @Override
        public String toString() {
            return "JmpInst{" +
                    "label=" + label +
                    '}';
        }
    }

    public static class SplitInst extends Inst {
        public int l1 = -1;
        public int l2 = -1;

        @Override
        public String toString() {
            return "SplitInst{" +
                    "l1=" + l1 +
                    ", l2=" + l2 +
                    '}';
        }
    }

    public static class AnyInst extends Inst {
        @Override
        public String toString() {
            return "AnyInst{}";
        }
    }

    public static class SaveInst extends Inst {
        public int saveIdx = -1;

        @Override
        public String toString() {
            return "SaveInst{" +
                    "saveIdx=" + saveIdx +
                    '}';
        }
    }

    public static class MatchInst extends Inst {
        @Override
        public String toString() {
            return "MatchInst{}";
        }
    }
}
