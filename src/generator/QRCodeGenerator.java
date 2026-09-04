package generator;

import generator.data.EncodingType;
import generator.data.ErrorCorrectionLevel;
import generator.data.MaskType;
import generator.data.QRCode;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class responsible for generating QR codes
 */
public class QRCodeGenerator {
    private static final Map<Character, Integer> alphaNumericMap = new HashMap<>();
    private static final boolean[] BYTE_EC = new boolean[] {true, true, true, false, true, true, false, false};
    private static final boolean[] BYTE_11 = new boolean[] {false, false, false, true, false, false, false, true};
    private static final List<Character> alphaNumericCharacters = List.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
            'X', 'Y', 'Z', ' ', '$', '%', '*', '+', '-', '.', '/', ':');
    private static final int[] dataCapacityLookup = new int[] {19, 16, 13, 9, 34, 28, 22, 16, 55, 44, 34, 26, 80, 64, 48, 36,
            108, 86, 62, 46, 136, 108, 76, 60, 156, 124, 88, 66, 194, 154, 110, 86, 232, 182, 132, 100, 274, 216, 154,
            122, 324, 254, 180, 140, 370, 290, 206, 158, 428, 334, 244, 180, 461, 365, 261, 197, 523, 415, 295, 223,
            589, 453, 325, 253, 647, 507, 367, 283, 721, 563, 397, 313, 795, 627, 445, 341, 861, 669, 485, 385, 932, 714,
            512, 406, 1006, 782, 568, 442, 1094, 860, 614, 464, 1174, 914, 664, 514, 1276, 1000, 718, 538, 1370, 1062, 754,
            596, 1468, 1128, 808, 628, 1531, 1193, 871, 661, 1631, 1267, 911, 701, 1735, 1373, 985, 745, 1843, 1455, 1033,
            793, 1955, 1541, 1115, 845, 2071, 1631, 1171, 901, 2191, 1725, 1231, 961, 2306, 1812, 1286, 986, 2434, 1914,
            1354, 1054, 2566, 1992, 1426, 1096, 2702, 2102, 1502, 1142, 2812, 2216, 1582, 1222, 2956, 2334, 1666, 1276};

    private static final int[] ECCBytesLookup = new int[] {7, 10, 13, 17, 10, 16, 22, 28, 15, 26, 36, 44, 20, 36, 52, 64, 26,
            48, 72, 88, 36, 64, 96, 112, 40, 72, 108, 130, 48, 88, 132, 156, 60, 110, 160, 192, 72, 130, 192, 224, 80,
            150, 224, 264, 96, 176, 260, 308, 104, 198, 288, 352, 120, 216, 320, 384, 132, 240, 360, 432, 144, 280, 408,
            480, 168, 308, 448, 532, 180, 338, 504, 588, 196, 364, 546, 650, 224, 416, 600, 700, 224, 442, 644, 750,
            252, 476, 690, 816, 270, 504, 750, 900, 300, 560, 810, 960, 312, 588, 870, 1050, 336, 644, 952, 1110, 360,
            700, 1020, 1200, 390, 728, 1050, 1260, 420, 784, 1140, 1350, 450, 812, 1200, 1440, 480, 868, 1290, 1530, 510,
            924, 1350, 1620, 540, 980, 1440, 1710, 570, 1036, 1530, 1800, 570, 1064, 1590, 1890, 600, 1120, 1680, 1980,
            630, 1204, 1770, 2100, 660, 1260, 1860, 2220, 720, 1316, 1950, 2310, 756, 1372, 2040, 2430};

    private static final int[] totalBlocks = new int[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 2, 2, 4, 1, 2, 4, 4,
            2, 4, 4, 4, 2, 4, 6, 5, 2, 4, 6, 6, 2, 5, 8, 8, 4, 5, 8, 8, 4, 5, 8, 11, 4, 8, 10, 11, 4, 9, 12, 16, 4, 9, 16,
            16, 6, 10, 12, 18, 6, 10, 17, 16, 6, 11, 16, 19, 6, 13, 18, 21, 7, 14, 21, 25, 8, 16, 20, 25, 8, 17, 23, 25,
            9, 17, 23, 34, 9, 18, 25, 30, 10, 20, 27, 32, 12, 21, 29, 35, 12, 23, 34, 37, 12, 25, 34, 40, 13, 26, 35, 42,
            14, 28, 38, 45, 15, 29, 40, 48, 16, 31, 43, 51, 17, 33, 45, 54, 18, 35, 48, 57, 19, 37, 51, 60, 19, 38, 53,
            63, 20, 40, 56, 66, 21, 43, 59, 70, 22, 45, 62, 74, 24, 47, 65, 77, 25, 49, 68, 81};

    /**
     * Generates a new QR code.
     * @param data Data to encode
     * @param errorCorrectionLevel Sets the error correction level.
     * @return The generated QR code
     * @throws InvalidAlgorithmParameterException Is thrown when the data is too long to be encoded.
     */
    public static QRCode generate(String data, ErrorCorrectionLevel errorCorrectionLevel) throws InvalidAlgorithmParameterException {
        return generate(data, 1, MaskType.AUTO, errorCorrectionLevel);
    }

    /**
     * Generates a new QR code.
     * @param data Data to encode
     * @param minVersion Forces the QR code to use only the specified version or higher.
     * @param maskType Sets a specific XOR mask to use ({@code MaskType.AUTO} is recommended and should be used).
     * @param errorCorrectionLevel Sets the error correction level.
     * @return The generated QR code
     * @throws InvalidAlgorithmParameterException Is thrown when the data is too long to be encoded.
     */
    public static QRCode generate(String data, int minVersion, MaskType maskType, ErrorCorrectionLevel errorCorrectionLevel) throws InvalidAlgorithmParameterException {
        EncodingType type = getEncodingType(data);

        boolean[][] binaryData;
        binaryData = switch(type) {
            case NUMERIC -> getNumericBinaryData(data);
            case ALPHANUMERIC -> getAlphaNumericBinaryData(data);
            case BYTE -> getByteBinaryData(data);
            case KANJI -> getKanjiBinaryData(data);
        };

        int numBits = 0;
        for(boolean[] values : binaryData) {
            numBits += values.length;
        }
        int numBytes = Math.ceilDiv(numBits, 8);

        int version = Math.max(getVersion(numBits, errorCorrectionLevel, type), minVersion);
        if(version < 0) {
            throw new InvalidAlgorithmParameterException("Text string is too long to be encoded in a qr code");
        }
        int countLength = getCountLength(type, version);

        int dataBitLength = getDataLengthForVersion(version, errorCorrectionLevel) * 8;
        boolean[] linearData = new boolean[dataBitLength];

        linearData[type.getIndicatorBit()] = true;

        int n;
        if(type == EncodingType.BYTE) {
            n = numBytes;
        } else {
            n = data.length();
        }
        for(int i = countLength + 3; i >= 4; i--) {
            linearData[i] = (n % 2) == 1;
            n /= 2;
        }

        int index = 4 + countLength;
        for(boolean[] values : binaryData) {
            for(boolean value : values) {
                linearData[index++] = value;
            }
        }

        int filledBytes = Math.ceilDiv(8 + countLength + numBits, 8);
        int paddingByteLength = dataBitLength - (filledBytes * 8);
        boolean[] byteArray = BYTE_EC;
        int bitIndex = 0;
        for(int i = dataBitLength - paddingByteLength; i < dataBitLength; i++) {
            linearData[i] = byteArray[bitIndex++];
            if(bitIndex >= 8) {
                bitIndex = 0;
                byteArray = Arrays.equals(byteArray, BYTE_EC) ? BYTE_11 : BYTE_EC;
            }
        }

        boolean[] completeData = getEccData(version, errorCorrectionLevel, linearData);

        return new QRCode(version, completeData, errorCorrectionLevel, maskType);
    }

    private static boolean[] getEccData(int version, ErrorCorrectionLevel level, boolean[] linearData) {
        int numECCodewords = getECCLengthForVersion(version, level);
        int totalBlocks = getTotalBlocks(version, level);
        int numDataCodewords = getDataLengthForVersion(version, level);

        int numEccBytes = numECCodewords / totalBlocks;
        int blockALen = numDataCodewords / totalBlocks;
        int numBlocksB = numDataCodewords % totalBlocks;
        int numBlocksA = totalBlocks - numBlocksB;
        int blockBLen = blockALen + 1;

        int numBlocks = numBlocksA + numBlocksB;

        boolean[][] blocks = new boolean[numBlocks][];
        boolean[][] eccBlocks = new boolean[numBlocks][];

        for(int i = 0; i < numBlocksA; i++) {
            boolean[] blockData = Arrays.copyOfRange(linearData, i * blockALen * 8, ((i + 1) * blockALen) * 8);
            blocks[i] = blockData;
            eccBlocks[i] = ReedSolomonErrorCorrectionHelper.getEDC(blockData, blockALen + numEccBytes);
        }

        int last = (numBlocksA * blockALen) * 8;
        for(int i = 0; i < numBlocksB; i++) {
            boolean[] blockData = Arrays.copyOfRange(linearData, last + i * blockBLen * 8, last + ((i + 1) * blockBLen) * 8);
            blocks[numBlocksA + i] = blockData;
            eccBlocks[numBlocksA + i] = ReedSolomonErrorCorrectionHelper.getEDC(blockData, blockBLen + numEccBytes);
        }

        boolean[] weavedData = new boolean[(numBlocks * numEccBytes + numBlocks * blockALen + numBlocksB) * 8];

        int offset = 0;
        int skipped = 0;
        for(int i = 0; i < blockBLen; i++) {
            for(int block = 0; block < numBlocks; block++) {
                if(blocks[block].length <= i * 8) {
                    skipped++;
                    continue;
                }
                for(int bit = 0; bit < 8; bit++) {
                    offset = (i * numBlocks + block - skipped) * 8 + bit;
                    weavedData[offset] = blocks[block][i * 8 + bit];
                }
            }
        }

        offset++;

        for(int i = 0; i < numEccBytes; i++) {
            for(int block = 0; block < numBlocks; block++) {
                for(int bit = 0; bit < 8; bit++) {
                    int index = offset + (i * numBlocks + block) * 8 + bit;
                    weavedData[index] = eccBlocks[block][i * 8 + bit];
                }
            }
        }

        return weavedData;
    }

    static {
        for(int i = 0; i < alphaNumericCharacters.size(); i++) {
            alphaNumericMap.put(alphaNumericCharacters.get(i), i);
        }
    }

    private static boolean[][] getByteBinaryData(String data) {
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

        boolean[][] binary = new boolean[bytes.length][8];
        for(int i = 0; i < bytes.length; i++) {
            int n = bytes[i] & 0xFF;

            for(int j = 7; j >= 0; j--) {
                binary[i][j] = (n % 2) == 1;
                n /= 2;
            }
        }

        return binary;
    }

    private static int getCountLength(EncodingType type, int version) {
        int countLength;
        switch(type) {
            case NUMERIC:
                countLength = 10;
                if(version > 9) {
                    countLength += 2;
                }
                if(version > 26) {
                    countLength += 2;
                }
                break;
            case ALPHANUMERIC:
                countLength = 9;
                if(version > 9) {
                    countLength += 2;
                }
                if(version > 26) {
                    countLength += 2;
                }
                break;
            case BYTE:
                countLength = 8;
                if(version > 9) {
                    countLength += 8;
                }
                break;
            case KANJI:
                countLength = 8;
                if(version > 9) {
                    countLength += 2;
                }
                if(version > 26) {
                    countLength += 2;
                }
                break;
            default:
                countLength = 8;
        }

        return countLength;
    }

    private static boolean[][] getNumericBinaryData(String data) {
        char[] chars = data.toCharArray();
        int[] numbers = new int[chars.length];
        for(int i = 0; i < chars.length; i++) {
            numbers[i] = Integer.parseInt(String.valueOf(chars[i]));
        }

        int div = numbers.length / 3;
        int mod = numbers.length % 3;
        int len = div + (mod > 0 ? 1 : 0);
        boolean[][] groups = new boolean[len][];

        for(int i = 0; i < groups.length - (mod > 0 ? 1 : 0); i++) {
            int j = 3 * i;
            groups[i] = numsToBinary(numbers[j], numbers[j + 1], numbers[j + 2]);
        }

        if(mod == 2) {
            groups[len - 1] = numsToBinary(numbers[numbers.length - 2], numbers[numbers.length - 1]);
        } else if(mod == 1) {
            boolean[] bits = new boolean[4];
            int n = numbers[numbers.length - 1];

            for(int i = 3; i >= 0; i--) {
                bits[i] = (n % 2) == 1;
                n /= 2;
            }

            groups[len - 1] = bits;
        }

        return groups;
    }

    private static boolean[][] getAlphaNumericBinaryData(String data) {
        char[] chars = data.toCharArray();

        int div = chars.length / 2;
        int mod = chars.length % 2;
        int len = div + (mod > 0 ? 1 : 0);
        boolean[][] groups = new boolean[len][];

        for(int i = 0; i < groups.length - (mod > 0 ? 1 : 0); i++) {
            int j = 2 * i;
            groups[i] = alphaNumericToBinary(chars[j], chars[j + 1]);
        }

        if(mod == 1) {
            boolean[] bits = new boolean[6];

            int n = getAlphaNumericIndex(chars[chars.length - 1]);

            for(int i = 5; i >= 0; i--) {
                bits[i] = (n % 2) == 1;
                n /= 2;
            }

            groups[len - 1] = bits;
        }

        return groups;
    }

    private static final CharsetEncoder ENCODER = Charset.forName("Shift_JIS").newEncoder();
    private static boolean[][] getKanjiBinaryData(String s) {
        try {
            char[] oldValues = s.toCharArray();
            CharBuffer buffer = ENCODER.encode(CharBuffer.wrap(oldValues)).asCharBuffer();
            char[] values = new char[buffer.length()];

            for(int i = 0; i < buffer.length(); i++) {
                values[i] = buffer.get(i);
            }

            boolean[][] binary = new boolean[values.length][];

            for(int i = 0; i < values.length; i++) {
                binary[i] = kanjiToBinary(values[i]);
            }

            return binary;
        } catch(CharacterCodingException e) {
            e.printStackTrace();
            return new boolean[0][];
        }
    }

    private static EncodingType getEncodingType(String s) {
        char[] chars = s.toCharArray();

        boolean isNumeric = true;
        boolean isAlphaNumeric = true;
        boolean isKanji = true;

        for(char c : chars) {
            isNumeric = isNumeric & EncodingType.NUMERIC.canEncodeChar(c);
            isAlphaNumeric = isAlphaNumeric & EncodingType.ALPHANUMERIC.canEncodeChar(c);
            isKanji = isKanji & EncodingType.KANJI.canEncodeChar(c);
        }

        return isNumeric ? EncodingType.NUMERIC : (isAlphaNumeric ? EncodingType.ALPHANUMERIC : (isKanji ? EncodingType.KANJI : EncodingType.BYTE));
    }

    private static boolean[] numsToBinary(int a, int b, int c) {
        int combined = a * 100 + b * 10 + c;
        boolean[] binary = new boolean[10];

        for(int i = 9; i >= 0; i--) {
            binary[i] = (combined % 2) == 1;
            combined /= 2;
        }

        return binary;
    }

    private static boolean[] numsToBinary(int a, int b) {
        int combined = a * 10 + b;
        boolean[] binary = new boolean[7];

        for(int i = 6; i >= 0; i--) {
            binary[i] = (combined % 2) == 1;
            combined /= 2;
        }

        return binary;
    }

    private static boolean[] alphaNumericToBinary(char a, char b) {
        int combined = getAlphaNumericIndex(a) * 45 + getAlphaNumericIndex(b);
        boolean[] binary = new boolean[11];

        for(int i = 10; i >= 0; i--) {
            binary[i] = (combined % 2) == 1;
            combined /= 2;
        }

        return binary;
    }

    private static int getAlphaNumericIndex(char c) {
        return alphaNumericMap.getOrDefault(c, 0);
    }

    private static boolean[] kanjiToBinary(int a) {
        int upper = a >> 8;
        int lower = a & 255;

        if(a > 0x8140 && a < 0x9FFC) {
            upper -= 0x81;
        } else if(a > 0xE040 && a < 0xEBBF) {
            upper -= 0xC1;
        }

        lower -= 0x40;
        upper *= 0x00C0;

        int n = upper + lower;
        boolean[] binary = new boolean[13];

        for(int i = 12; i >= 0; i--) {
            binary[i] = (n % 2) == 1;
            n /= 2;
        }

        return binary;
    }

    private static int getVersion(int numBits, ErrorCorrectionLevel level, EncodingType type) {
        for(int i = 1; i <= 40; i++) {
            int testVal = Math.ceilDiv(numBits + getCountLength(type, i), 8);
            if(getDataLengthForVersion(i, level) >= testVal) {
                return i;
            }
        }

        return -1;
    }

    private static int getDataLengthForVersion(int version, ErrorCorrectionLevel level) {
        int index = (version - 1) * 4 + level.ordinal();

        return dataCapacityLookup[index];
    }

    private static int getECCLengthForVersion(int version, ErrorCorrectionLevel level) {
        int index = (version - 1) * 4 + level.ordinal();

        return ECCBytesLookup[index];
    }

    private static int getTotalBlocks(int version, ErrorCorrectionLevel level) {
        int index = (version - 1) * 4 + level.ordinal();

        return totalBlocks[index];
    }
}