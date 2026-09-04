package generator.data;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * A class representing a QR code
 */
public class QRCode {
    private static final int[][] alignmentPosList = new int[][] {{6, 18}, {6, 22}, {6, 26}, {6, 30}, {6, 34}, {6, 22, 38},
            {6, 24, 42}, {6, 26, 46}, {6, 28, 50}, {6, 30, 54}, {6, 32, 58}, {6, 34, 62}, {6, 26, 46, 66}, {6, 26, 48, 70},
            {6, 26, 50, 74}, {6, 30, 54, 78}, {6, 30, 56, 82}, {6, 30, 58, 86}, {6, 34, 62, 90}, {6, 28, 50, 72, 94},
            {6, 26, 50, 74, 98}, {6, 30, 54, 78, 102}, {6, 28, 54, 80, 106}, {6, 32, 58, 84, 110}, {6, 30, 58, 86, 114},
            {6, 34, 62, 90, 118}, {6, 26, 50, 74, 98, 122}, {6, 30, 54, 78, 102, 126}, {6, 26, 52, 78, 104, 130},
            {6, 30, 56, 82, 108, 134}, {6, 34, 60, 86, 112, 138}, {6, 30, 58, 86, 114, 142}, {6, 34, 62, 90, 118, 146},
            {6, 30, 54, 78, 102, 126, 150}, {6, 24, 50, 76, 102, 128, 154}, {6, 28, 54, 80, 106, 132, 158},
            {6, 32, 58, 84, 110, 136, 162}, {6, 26, 54, 82, 110, 138, 166}, {6, 30,  58, 86, 114, 142, 170}};

    private static final boolean[] FINDER_1 = new boolean[] {true, false, true, true, true, false, true, false, false, false, false};
    private static final boolean[] FINDER_2 = new boolean[] {false, false, false, false, true, false, true, true, true, false, true};

    private final int version;
    private final boolean[] data;
    private final ErrorCorrectionLevel errorCorrectionLevel;
    private final MaskType maskType;

    private boolean[][] imageMatrixInternal;
    private BufferedImage image;
    private Color lightColor;
    private Color darkColor;
    private int moduleWidth;

    public QRCode(int version, boolean[] data, ErrorCorrectionLevel errorCorrectionLevel, MaskType maskType) {
        this.version = Math.clamp(version, 1, 40);
        this.data = data;
        this.errorCorrectionLevel = errorCorrectionLevel;
        this.maskType = maskType;

        this.moduleWidth = 4;
        this.lightColor = Color.WHITE;
        this.darkColor = Color.BLACK;

        generateImageAndMatrix();
    }

    /**
     * Getter for the raw binary data representing the encoded message
     * @return Binary data of the encoded message
     */
    public boolean[] getData() {
        return data;
    }

    /**
     * Returns the version of the QR code (ranging from 1 to 40 inclusive)
     * @return Version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Returns the error correction level of the QR code
     * @return EC level
     */
    public ErrorCorrectionLevel getErrorCorrectionLevel() {
        return errorCorrectionLevel;
    }

    /**
     * Returns the mask type of the QR code
     * @return Mask type
     */
    public MaskType getMaskType() {
        return maskType;
    }

    /**
     * Returns the QR code represented by a 2-dimensional boolean array. This array does NOT include the quiet zone.
     * @return The QR code matrix
     */
    public boolean[][] getImageMatrix() {
        return imageMatrixInternal;
    }

    /**
     * Returns a {@code BufferedImage} containing the QR code, including the quiet zone
     * @return The QR code image
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Returns the amount of pixels corresponding to the width and height of one module on the image
     * @return Width and height of one module
     */
    public int getModuleWidth() {
        return moduleWidth;
    }

    /**
     * Sets the width and height of one module on the image
     * @param moduleWidth The new width and height
     */
    public void setModuleWidth(int moduleWidth) {
        this.moduleWidth = moduleWidth;
        generateImage();
    }

    /**
     * Returns the color used for the light modules on the image
     * @return The color of light modules
     */
    public Color getLightColor() {
        return lightColor;
    }

    /**
     * Sets the color used for the light modules on the image
     * @param lightColor The new color of light modules
     */
    public void setLightColor(Color lightColor) {
        this.lightColor = lightColor;
        generateImage();
    }

    /**
     * Returns the color used for the dark modules on the image
     * @return The color of dark modules
     */
    public Color getDarkColor() {
        return darkColor;
    }

    /**
     * Sets the color used for the dark modules on the image
     * @param darkColor The new color of dark modules
     */
    public void setDarkColor(Color darkColor) {
        this.darkColor = darkColor;
        generateImage();
    }

    private void drawSquare(Boolean[][] matrix, int x, int y, int sideLength, boolean val) {
        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                matrix[x + i][y + j] = val;
            }
        }
    }

    private void drawRect(Boolean[][] matrix, int x, int y, int sideLengthA, int sideLengthB, boolean val) {
        for (int i = 0; i < sideLengthA; i++) {
            for (int j = 0; j < sideLengthB; j++) {
                matrix[x + i][y + j] = val;
            }
        }
    }

    private void drawFinderPattern(Boolean[][] matrix, int x, int y) {
        drawSquare(matrix, x, y, 7, true);
        drawSquare(matrix, x + 1, y + 1, 5, false);
        drawSquare(matrix, x + 2, y + 2, 3, true);
    }

    private void drawAlignmentPattern(Boolean[][] matrix, int x, int y) {
        drawSquare(matrix, x - 2, y - 2, 5, true);
        drawSquare(matrix, x - 1, y - 1, 3, false);
        drawSquare(matrix, x, y, 1, true);
    }

    private int[][] calculateAlignmentGrid() {
        if(version < 2) {
            return new int[0][];
        }

        int[] entry = alignmentPosList[version - 2];
        int[][] res = new int[entry.length * entry.length - 3][];
        int index = 0;
        for(int i = 0; i < entry.length; i++) {
            for(int j = 0; j < entry.length; j++) {
                if(((i == 0 || i == entry.length - 1) && ((j == 0 || j == entry.length - 1))) && !(i == entry.length - 1 && j == entry.length - 1)) continue;
                res[index++] = new int[] {entry[i], entry[j]};
            }
        }

        return res;
    }

    private boolean[][] generateMatrixWithMask(MaskType maskType) {
        if(maskType == MaskType.AUTO) return new boolean[0][0];

        int size = 4 * version + 17;
        Boolean[][] imageMatrix = new Boolean[size][size];

        //Dummy format data part 1
        drawSquare(imageMatrix, 0, 0, 9, false);

        //Timing patterns
        for (int i = 0; i < size; i++) {
            boolean val = (i & 1) == 0;
            imageMatrix[i][6] = val;
            imageMatrix[6][i] = val;
        }

        //Finder patterns
        drawFinderPattern(imageMatrix, 0, 0);
        drawSquare(imageMatrix, 0, size - 8, 8, false);
        drawFinderPattern(imageMatrix, 0, size - 7);
        drawSquare(imageMatrix, size - 8, 0, 8, false);
        drawFinderPattern(imageMatrix, size - 7, 0);

        //Alignment patterns
        int[][] alignmentGrid = calculateAlignmentGrid();

        for(int[] alignment : alignmentGrid) {
            drawAlignmentPattern(imageMatrix, alignment[0], alignment[1]);
        }

        //Dummy format data part 2
        drawRect(imageMatrix, 8, size - 7, 1, 7, false);
        imageMatrix[8][size - 8] = true;
        drawRect(imageMatrix, size - 8, 8, 8, 1, false);

        //Version information
        if(version >= 7) {
            boolean[] versionInformation = new boolean[18];
            StringBuilder rawInfo = new StringBuilder();

            int v = version;
            for(int i = 5; i >= 0; i--) {
                rawInfo.insert(0, (v & 1));
                versionInformation[i] = (v & 1) == 1;
                v /= 2;
            }

            rawInfo.append("000000000000");

            int start = rawInfo.indexOf("1");
            rawInfo = new StringBuilder(rawInfo.substring(start));
            String versionGenerator = "1111100100101";

            while(rawInfo.length() > 12) {
                int diff = rawInfo.length() - versionGenerator.length();
                String paddedGen = versionGenerator;
                paddedGen += "0".repeat(diff);

                StringBuilder res = new StringBuilder();
                for(int i = 0; i < rawInfo.length(); i++) {
                    String bitVal = rawInfo.charAt(i) == '1' ? (paddedGen.charAt(i) == '1' ? "0" : "1") : String.valueOf(paddedGen.charAt(i));
                    res.append(bitVal);
                }

                start = res.indexOf("1");
                rawInfo = new StringBuilder(res.substring(start));
            }

            for(int i = 0; i < rawInfo.length(); i++) {
                int offset = 18 - rawInfo.length();
                versionInformation[i + offset] = rawInfo.charAt(i) == '1';
            }

            //Place version info
            for(int i = 0; i < 18; i++) {
                imageMatrix[(2 - (i % 3)) + size - 11][5 - (i / 3)] = versionInformation[i];
                imageMatrix[5 - (i / 3)][(2 - (i % 3)) + size - 11] = versionInformation[i];
            }
        }

        //Data
        int index = 0;
        boolean dir = true;
        out:
        for(int col = 0; col < size; col += 2) {
            int x = size - 1 - (col);
            if(x == 6) {
                col++;
                x--;
            }

            for(int row = 0; row < size; row++) {
                if(index >= data.length) break out;
                int y;
                if(dir) {
                    y = size - row - 1;
                } else {
                    y = row;
                }

                if(imageMatrix[x][y] == null) {
                    imageMatrix[x][y] = data[index++] ^ maskType.getPattern().get(x, y);
                }

                if(index >= data.length) break out;

                if(imageMatrix[x - 1][y] == null) {
                    imageMatrix[x - 1][y] = data[index++] ^ maskType.getPattern().get(x - 1, y);
                }
            }
            dir = !dir;
        }

        //Metadata calculation
        boolean[] metadata = new boolean[15];
        String rawInfo = "";

        if(errorCorrectionLevel.getId() > 1) {
            rawInfo += "1";
            metadata[0] = true;
        } else {
            rawInfo += "0";
            metadata[0] = false;
        }
        rawInfo += errorCorrectionLevel.getId() & 1;
        metadata[1] = rawInfo.charAt(1) == '1';

        int maskId = maskType.getValue();
        StringBuilder append = new StringBuilder();
        for(int i = 4; i > 1; i--) {
            append.insert(0, (maskId & 1));
            metadata[i] = (maskId & 1) == 1;
            maskId /= 2;
        }
        rawInfo += append;

        rawInfo += "0000000000";

        int start = rawInfo.indexOf("1");
        if(start == -1) {
            rawInfo = "0".repeat(15);
        } else {
            rawInfo = rawInfo.substring(start);
            String metadataGenerator = "10100110111";

            while(rawInfo.length() > 10) {
                int diff = rawInfo.length() - metadataGenerator.length();
                String paddedGen = metadataGenerator;
                paddedGen += "0".repeat(diff);

                StringBuilder res = new StringBuilder();
                for(int i = 0; i < rawInfo.length(); i++) {
                    String bitVal = rawInfo.charAt(i) == '1' ? (paddedGen.charAt(i) == '1' ? "0" : "1") : String.valueOf(paddedGen.charAt(i));
                    res.append(bitVal);
                }

                start = res.indexOf("1");
                if(start == -1) {
                    rawInfo = "0".repeat(15);
                } else {
                    rawInfo = res.substring(start);
                }
            }
        }

        for(int i = 0; i < rawInfo.length(); i++) {
            int offset = 15 - rawInfo.length();
            metadata[i + offset] = rawInfo.charAt(i) == '1';
        }

        boolean[] metadataMask = new boolean[] {true, false, true, false, true, false, false, false, false, false, true, false, false, true, false};

        for(int i = 0; i < 15; i++) {
            metadata[i] ^= metadataMask[i];
        }

        //Set metadata
        for(int i = 0; i < 9; i++) {
            if(i == 6) continue;

            int j = i < 6 ? i : i - 1;
            imageMatrix[i][8] = metadata[j];
        }

        for(int i = 0; i < 8; i++) {
            int j = i + 7;
            imageMatrix[size + i - 8][8] = metadata[j];
        }

        for(int i = 0; i < 7; i++) {
            imageMatrix[8][size - i - 1] = metadata[i];
        }

        for(int i = 0; i < 8; i++) {
            if(i == 1) continue;

            int j = i < 1 ? i : i - 1;
            imageMatrix[8][7 - i] = metadata[j + 8];
        }

        boolean[][] imageMatrixInternal = new boolean[size][size];
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                Boolean val = imageMatrix[i][j];
                imageMatrixInternal[i][j] = val != null && val;
            }
        }

        return imageMatrixInternal;
    }

    private boolean[][] getBestMask() {
        boolean[][] best = new boolean[0][0];
        int lowest = Integer.MAX_VALUE;

        for(MaskType type : MaskType.values()) {
            if(type == MaskType.AUTO) continue;

            boolean[][] res = generateMatrixWithMask(type);
            int score = evaluateMask(res);
            if(score < lowest) {
                lowest = score;
                best = res;
            }
        }

        return best;
    }

    private int evaluateMask(boolean[][] grid) {
        int runScore = 0;
        int boxScore = 0;
        int finderScore = 0;
        int balanceScore;
        int darkModuleCount = 0;

        int xRunLen = 0;
        int yRunLen = 0;
        for(int i = 0; i < grid.length; i++) {
            for(int j = 0; j < grid.length; j++) {
                if(grid[i][j]) {
                    darkModuleCount++;
                }

                //Run patterns
                if(j > 0 && (grid[j][i] != grid[j - 1][i])) {
                    if(xRunLen >= 5) {
                        runScore += xRunLen - 2;
                    }

                    xRunLen = 0;
                }
                xRunLen++;

                if(j > 0 && (grid[i][j] != grid[i][j - 1])) {
                    if(yRunLen >= 5) {
                        runScore += yRunLen - 2;
                    }

                    yRunLen = 0;
                }
                yRunLen++;

                //Box patterns
                if(i < grid.length - 1 && j < grid.length - 1) {
                    boolean a = grid[i][j];
                    boolean b = grid[i + 1][j];
                    boolean c = grid[i][j + 1];
                    boolean d = grid[i + 1][j + 1];

                    if(a == b && b == c && c == d) {
                        boxScore += 3;
                    }
                }

                //Finder-like patterns
                boolean[] testSubarrayX = new boolean[11];
                boolean[] testSubarrayY = new boolean[11];

                int x = i - 4;
                int y = j - 4;
                for(int k = 0; k < testSubarrayX.length; k++) {
                    if(x + k < 0) {
                        testSubarrayX[k] = false;
                    } else if(x + k >= grid.length) {
                        testSubarrayX[k] = false;
                    } else {
                        testSubarrayX[k] = grid[x + k][j];
                    }

                    if(y + k < 0) {
                        testSubarrayY[k] = false;
                    } else if(y + k >= grid.length) {
                        testSubarrayY[k] = false;
                    } else {
                        testSubarrayY[k] = grid[i][y + k];
                    }
                }

                if(Arrays.equals(testSubarrayX, FINDER_1) || Arrays.equals(testSubarrayX, FINDER_2)) {
                    finderScore += 40;
                }

                if(Arrays.equals(testSubarrayY, FINDER_1) || Arrays.equals(testSubarrayY, FINDER_2)) {
                    finderScore += 40;
                }
            }

            if(xRunLen >= 5) {
                runScore += xRunLen - 2;
            }

            if(yRunLen >= 5) {
                runScore += yRunLen - 2;
            }

            xRunLen = 0;
            yRunLen = 0;
        }

        double darkPercentage = ((double) darkModuleCount) / (grid.length * grid.length);
        double deviation = Math.abs(0.5D - darkPercentage);

        balanceScore = (int) (10 * Math.ceil(deviation / 0.05D - 1.0D));

        int totalScore = runScore + boxScore + finderScore + balanceScore;

        return totalScore;
    }

    private void generateImageAndMatrix() {
        if(maskType == MaskType.AUTO) {
            imageMatrixInternal = getBestMask();
        } else {
            imageMatrixInternal = generateMatrixWithMask(maskType);
        }

        //Generating image
        generateImage();
    }

    private void generateImage() {
        int size = 4 * version + 17;

        //Adding the quiet zone (4 modules on each side)
        image = new BufferedImage((size + 8) * moduleWidth, (size + 8) * moduleWidth, Image.SCALE_SMOOTH);
        int[] arr = new int[((size + 16) * size + 64) * moduleWidth * moduleWidth];
        Arrays.fill(arr, lightColor.getRGB());
        image.setRGB(0, 0, (size + 8) * moduleWidth, (size + 8) * moduleWidth, arr, 0, 1);

        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                for(int x = 0; x < moduleWidth; x++) {
                    for(int y = 0; y < moduleWidth; y++) {
                        assert imageMatrixInternal != null;
                        image.setRGB((i + 4) * moduleWidth + x, (j + 4) * moduleWidth + y, imageMatrixInternal[i][j] ? darkColor.getRGB() : lightColor.getRGB());
                    }
                }
            }
        }
    }
}