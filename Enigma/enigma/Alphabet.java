package enigma;
import static enigma.EnigmaException.error;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Har Vey Yuen
 */
class Alphabet {

    /** A new alphabet containing CHARS. The K-th character has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        if (chars.contains("*") || chars.contains("(") || chars.contains(")")) {
            throw error("invalid character");
        }
        for (int i = 0; i < chars.length(); i++) {
            for (int j = i + 1; j < chars.length(); j++) {
                if (chars.charAt(i) == chars.charAt(j)) {
                    throw error("duplicate character in alphabet");
                }
            }
        }
        _chars = chars;
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _chars.length();
    }

    /** Returns true if CH is in this alphabet. */
    boolean contains(char ch) {
        return _chars.indexOf(ch) > -1;
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        return _chars.charAt(index);
    }

    /** Returns the index of character CH which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        return _chars.indexOf(ch);
    }

    /** The chars of the alphabet. */
    private String _chars;
}
