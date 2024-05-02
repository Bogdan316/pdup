
package upt.baker.pdup.regex.parser;

import java.util.List;

public interface AstNode {
    void postOrder(List<ReToken> order);

    record BinaryExp(ReToken op, AstNode left, AstNode right) implements AstNode {
        @Override
        public String toString() {
            return "BinaryExp{" +
                    "op=" + op +
                    ", left=" + left +
                    ", right=" + right +
                    '}';
        }

        @Override
        public void postOrder(List<ReToken> order) {
            left.postOrder(order);
            right.postOrder(order);
            order.add(op);
        }
    }

    record UnaryExp(ReToken op, AstNode exp) implements AstNode {
        @Override
        public String toString() {
            return "UnaryExp{" +
                    "op=" + op +
                    ", exp=" + exp +
                    '}';
        }

        @Override
        public void postOrder(List<ReToken> order) {
            exp.postOrder(order);
            order.add(op);
        }
    }

    record Group(AstNode exp) implements AstNode {
        @Override
        public void postOrder(List<ReToken> order) {
            exp.postOrder(order);
        }

        @Override
        public String toString() {
            return "Group{" +
                    "exp=" + exp +
                    '}';
        }
    }

    record Term(ReToken term) implements AstNode {
        @Override
        public void postOrder(List<ReToken> order) {
            order.add(term);
        }

        @Override
        public String toString() {
            return "Term{" +
                    "term=" + term +
                    '}';
        }
    }
}
