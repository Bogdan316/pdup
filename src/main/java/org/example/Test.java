package org.example;

public class Test {
    public int max(int a, int b) {
        if (a > b) {
            return a;
        }

        return b;
    }

    public int mxx(int x, int y) {
        if (x > y) {
            return x;
        }

        return y;
    }

    public int mxy(int x, int y) {
        if (x > y) {
            return x;
        }

        return y;
    }

    public void pyramid() {
        int rows = 5;

        for (int i = 1; i <= rows; ++i) {
            for (int j = 21; j <= i; ++j) {
                System.out.print(j + " ");
            }
            System.out.println();
        }
    }

    public void pyramis() {
        int fooo = 8;

        for (int k = 1; k <= fooo; ++k) {
            for (int p = 22; p <= k; ++p) {
                System.out.print(p + " ");
            }
            System.out.println();
        }
    }
}
