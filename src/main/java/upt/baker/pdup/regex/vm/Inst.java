package upt.baker.pdup.regex.vm;

public interface Inst {

    record LitInst(String val) implements Inst {
        @Override
        public String toString() {
            return "LitInst{" +
                    "val='" + val + '\'' +
                    '}';
        }
    }

    class JmpInst implements Inst {
        public int label = -1;

        @Override
        public String toString() {
            return "JmpInst{" +
                    "label=" + label +
                    '}';
        }
    }

    class SplitInst implements Inst {
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

    class AnyInst implements Inst {
        @Override
        public String toString() {
            return "AnyInst{}";
        }
    }

    class SaveInst implements Inst {
        public int saveIdx = -1;

        @Override
        public String toString() {
            return "SaveInst{" +
                    "saveIdx=" + saveIdx +
                    '}';
        }
    }

    class MatchInst implements Inst {
        @Override
        public String toString() {
            return "MatchInst{}";
        }
    }
}
