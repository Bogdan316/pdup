package upt.baker.pdup.regex.engine;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface State {
    void addState(Set<State> states, String s);

    class Any implements State {
        public final Transition out = new Transition();

        @Override
        public void addState(Set<State> states, String s) {
            states.add(out.dst);
        }
    }

    class Literal implements State {
        public final String val;
        public final Transition out = new Transition();

        public Literal(String val) {
            this.val = val;
        }

        @Override
        public void addState(Set<State> states, String s) {
            if (val.equals(s)) {
                states.add(out.dst);
            }
        }
    }

    class Split implements State {
        public final Transition left = new Transition();
        public final Transition right = new Transition();

        public Split(@Nullable State left, @Nullable State right) {
            this.left.setDst(left);
            this.right.setDst(right);
        }

        @Override
        public void addState(Set<State> states, String s) {
            if (left.dst != null) {
                left.dst.addState(states, s);
            }
            if (right.dst != null) {
                right.dst.addState(states, s);
            }
        }
    }

    class Match implements State {
        @Override
        public void addState(Set<State> states, String s) {
            states.add(this);
        }
    }
}
