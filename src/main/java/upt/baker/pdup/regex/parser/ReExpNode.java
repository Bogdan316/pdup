
package upt.baker.pdup.regex.parser;


public interface ReExpNode {
    <T> void accept(ReVoidAstVisitor<T> v, T arg);

    record StarHookExp(ReExpNode exp) implements ReExpNode {
        @Override
        public <T> void accept(ReVoidAstVisitor<T> v, T arg) {
            v.visit(this, arg);
        }

        @Override
        public String toString() {
            return "StarHook{" +
                    "exp=" + exp +
                    '}';
        }
    }

    record StarExp(ReExpNode exp) implements ReExpNode {
        @Override
        public String toString() {
            return "StarExp{" +
                    "exp=" + exp +
                    '}';
        }

        @Override
        public <T> void accept(ReVoidAstVisitor<T> v, T arg) {
            v.visit(this, arg);
        }
    }

    record PlusExp(ReExpNode exp) implements ReExpNode {
        @Override
        public String toString() {
            return "PlusExp{" +
                    "exp=" + exp +
                    '}';
        }

        @Override
        public <T> void accept(ReVoidAstVisitor<T> v, T arg) {
            v.visit(this, arg);
        }
    }

    record HookExp(ReExpNode exp) implements ReExpNode {
        @Override
        public String toString() {
            return "HookExp{" +
                    "exp=" + exp +
                    '}';
        }

        @Override
        public <T> void accept(ReVoidAstVisitor<T> v, T arg) {
            v.visit(this, arg);
        }
    }

    record ConcatExp(ReExpNode left, ReExpNode right) implements ReExpNode {
        @Override
        public String toString() {
            return "AndExp{" +
                    "left=" + left +
                    ", right=" + right +
                    '}';
        }

        @Override
        public <T> void accept(ReVoidAstVisitor<T> v, T arg) {
            v.visit(this, arg);
        }
    }

    record AltExp(ReExpNode left, ReExpNode right) implements ReExpNode {
        @Override
        public String toString() {
            return "AltExp{" +
                    "left=" + left +
                    ", right=" + right +
                    '}';
        }

        @Override
        public <T> void accept(ReVoidAstVisitor<T> v, T arg) {
            v.visit(this, arg);
        }
    }

    record GroupExp(ReExpNode exp, int groupIdx) implements ReExpNode {

        @Override
        public <T> void accept(ReVoidAstVisitor<T> v, T arg) {
            v.visit(this, arg);
        }

        @Override
        public String toString() {
            return "GroupExp{" +
                    "exp=" + exp +
                    ", groupIdx=" + groupIdx +
                    '}';
        }
    }

    class AnyExp implements ReExpNode {
        @Override
        public String toString() {
            return "Any{}";
        }

        @Override
        public <T> void accept(ReVoidAstVisitor<T> v, T arg) {
            v.visit(this, arg);
        }
    }

    record LiteralExp(int value) implements ReExpNode {
        @Override
        public String toString() {
            return "Literal{" +
                    "value='" + value + '\'' +
                    '}';
        }

        @Override
        public <T> void accept(ReVoidAstVisitor<T> v, T arg) {
            v.visit(this, arg);
        }
    }
}
