import java.util.List;
import java.util.Map;

public class Compressor {

    private int startBitOffset;

    public Compressor() {
        this.startBitOffset = 0; // Initialize startBitOffset as needed
    }

    public int getStartBitOffset() {
        return startBitOffset;
    }

    public void setStartBitOffset(int startBitOffset) {
        this.startBitOffset = startBitOffset;
    }

    public String appendBits(int number, String inputStr, int numBits) {
        int startChar = this.startBitOffset >> 3;
        numBits = numBits != -1 ? numBits : (int) Math.ceil(Math.log(number + 1) / Math.log(2));
        int totalBits = this.startBitOffset + numBits;
        int bitsLastByte = totalBits & 7;
        int totalChars = (totalBits >> 3) + (bitsLastByte == 0 ? 0 : 1);

        number &= (1 << numBits) - 1; // using low order here

        StringBuilder output = new StringBuilder(inputStr.substring(0, Math.min(startChar + 1, inputStr.length())));
        while (output.length() < totalChars) {
            output.append("\u0000");
        }

        int curChar = totalChars - 1;
        int curBits = number & ((1 << bitsLastByte) - 1);
        number >>= bitsLastByte;
        int startRemainingBits = numBits - bitsLastByte;
        int shiftLastByte = bitsLastByte == 0 ? 0 : 8 - bitsLastByte;

        if (curChar < output.length()) {
            int curCharValue = output.charAt(curChar) + (curBits << shiftLastByte);
            output.setCharAt(curChar, (char) curCharValue);
        }

        curChar--;

        int remainingBits = startRemainingBits;
        while (remainingBits > 7) {
            if (curChar < output.length()) {
                output.setCharAt(curChar, (char) (number & 255));
            }
            remainingBits -= 8;
            curChar--;
            number >>= 8;
        }

        if (remainingBits > 0 && startChar < output.length()) {
            int startCharOrd = output.charAt(startChar);
            startCharOrd &= 255 - (1 << (remainingBits - 1));
            output.setCharAt(startChar, (char) (startCharOrd + number));
        }

        this.startBitOffset = totalBits;
        return output.toString();
    }

    public String appendGamma(int number, String inputStr) {
        int bitLen = (int) Math.ceil(Math.log(number + 1) / Math.log(2));
        inputStr = appendUnary(bitLen, inputStr, true);
        this.startBitOffset -= 1; // startBitOffset will have advanced by appendUnary call
        return appendBits(number, inputStr, bitLen);
    }

    public String appendUnary(int number, String inputStr, boolean justBitOffset) {
        int totalBits = this.startBitOffset + number;

        if (justBitOffset) {
            // only compute new bit offset
            this.startBitOffset = totalBits;
            return inputStr;
        }

        int bitsLastByte = totalBits & 7;
        int totalChars = (totalBits >> 3) + (bitsLastByte == 0 ? 0 : 1);
        int bitsFirstByte = this.startBitOffset & 7;
        int startChar = this.startBitOffset >> 3;

        StringBuilder output = new StringBuilder(inputStr.substring(0, Math.min(startChar + 1, inputStr.length())));
        while (output.length() < totalChars) {
            output.append("\u0000");
        }

        int startCharOrd = startChar < output.length() ? output.charAt(startChar) : 0;
        startCharOrd &= (((1 << bitsFirstByte) - 1) << (8 - bitsFirstByte));
        if (startChar < output.length()) {
            output.setCharAt(startChar, (char) startCharOrd);
        }

        int lastOrd = totalChars > 0 ? output.charAt(totalChars - 1) : 0;
        lastOrd += (bitsLastByte == 0 ? 1 : (1 << (8 - bitsLastByte)));
        if (totalChars - 1 < output.length()) {
            output.setCharAt(totalChars - 1, (char) lastOrd);
        }

        this.startBitOffset = totalBits;
        return output.toString();
    }

    public String appendRiceSequence(Map<Integer, Integer> intSequence, int modulus, String output, int deltaStart) {
        int lastEncode = deltaStart;
        output = appendUnary(modulus, output, false);
        int mask = (1 << modulus) - 1;

        for (int preToEncode : intSequence.keySet()) {
            int toEncode = deltaStart < 0 ? preToEncode : preToEncode - lastEncode;
            // toEncode -= 1;
            lastEncode = preToEncode;

            output = appendGamma(intSequence.get(preToEncode), output);
            output = appendUnary((toEncode >> modulus) + 1, output, false);
            output = appendBits(toEncode & mask, output, modulus);
        }

        return output;
    }

    // vbyteEncode method
    public StringBuilder vbyteEncode(int posInt, StringBuilder inputStr) {
        String result = vbyteEncode1(posInt);
        this.startBitOffset += 8 * result.length();
        return inputStr.append(result);
    }

    public String vbyteEncode1(int posInt) {
        StringBuilder result = new StringBuilder();

        // Append the first character
        if (posInt < 128) {
            result.append((char) (posInt & 127));
        } else {
            result.append((char) (128 | (posInt & 127)));
        }

        // Shift right by 7 bits
        posInt >>= 7;

        // Process remaining parts of the integer
        while (posInt > 0) {
            if (posInt < 128) {
                result.append((char) (posInt & 127));
            } else {
                result.append((char) (128 | (posInt & 127)));
            }
            posInt >>= 7;
        }

        return result.toString();
    }
}
