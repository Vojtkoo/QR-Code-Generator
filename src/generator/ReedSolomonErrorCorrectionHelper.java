package generator;

/**
 * A class containing useful methods for generating the Reed-Solomon error correction codewords
 */
public class ReedSolomonErrorCorrectionHelper {
    private static final int[] LOG = new int[256];
    private static final int[] EXP = new int[256];

    static {
        for(int exponent = 1, value = 1; exponent < 256; exponent++) {
            value = value > 127 ? ((value << 1) ^ 285) : value << 1;
            LOG[value] = exponent % 255;
            EXP[exponent % 255] = value;
        }
    }

    private static int mul(int a, int b) {
        return (a != 0 && b != 0) ? EXP[(LOG[a] + LOG[b]) % 255] : 0;
    }

    private static int div(int a, int b) {
        return EXP[(LOG[a] + LOG[b] * 254) % 255];
    }

    private static int[] polyMul(int[] poly1, int[] poly2) {
        final int[] coeffs = new int[poly1.length + poly2.length - 1];

        for(int index = 0; index < coeffs.length; index++) {
            int coeff = 0;
            for(int p1index = 0; p1index <= index; p1index++) {
                final int p2index = index - p1index;
                int c1 = p1index >= poly1.length ? 0 : poly1[p1index];
                int c2 = p2index >= poly2.length ? 0 : poly2[p2index];

                coeff ^= mul(c1, c2);
            }
            coeffs[index] = coeff;
        }

        return coeffs;
    }

    private static int[] polyRest(int[] dividend, int[] divisor) {
        final int quotientLength = dividend.length - divisor.length + 1;
        int[] rest = dividend.clone();
        for(int count = 0; count < quotientLength; count++) {
            if(rest[0] != 0) {
                final int factor = div(rest[0], divisor[0]);
                final int[] subtr = new int[rest.length];
                int[] factorPoly = new int[] {factor};
                int[] mulRes = polyMul(divisor, factorPoly);

                System.arraycopy(mulRes, 0, subtr, 0, mulRes.length);

                int[] tmp = new int[rest.length - 1];
                for(int i = 0; i < tmp.length; i++) {
                    tmp[i] = rest[i + 1] ^ subtr[i + 1];
                }

                rest = tmp;
            } else {
                int[] tmp = new int[rest.length - 1];
                System.arraycopy(rest, 1, tmp, 0, tmp.length);
                rest = tmp;
            }
        }
        return rest;
    }

    private static int[] getGeneratorPoly(int degree) {
        int[] lastPoly = new int[] {1};
        for(int index = 0; index < degree; index++) {
            lastPoly = polyMul(lastPoly, new int[] {1, EXP[index]});
        }

        return lastPoly;
    }

    /**
     * Generates the error correction codewords for the data.
     * @param data Data for generation
     * @param codewords Number of codewords for the joined array of data and error correction codewords
     * @return Array of error correction codewords
     */
    public static int[] getEDC(int[] data, int codewords) {
        final int degree = codewords - data.length;
        final int[] messagePoly = new int[codewords];
        System.arraycopy(data, 0, messagePoly, 0, data.length);

        return polyRest(messagePoly, getGeneratorPoly(degree));
    }

    /**
     * Generates the error correction codewords for the binary data.
     * @param data Binary data for generation
     * @param codewords Number of codewords for the joined array of data and error correction codewords
     * @return Array of error correction codewords represented in binary
     */
    public static boolean[] getEDC(boolean[] data, int codewords) {
        int[] numData = new int[data.length / 8];

        for(int i = 0; i < numData.length; i++) {
            int n = 0;

            for(int bit = 0; bit < 8; bit++) {
                n *= 2;
                if(data[i * 8 + bit]) {
                    n++;
                }
            }

            numData[i] = n;
        }

        int[] edcRes = getEDC(numData, codewords);

        boolean[] res = new boolean[edcRes.length * 8];

        for(int i = 0; i < edcRes.length; i++) {
            int n = edcRes[i];
            for(int j = 7; j >= 0; j--) {
                res[i * 8 + j] = (n % 2) == 1;
                n /= 2;
            }
        }

        return res;
    }
}
