package enigma;

import static enigma.EnigmaException.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.util.ArrayList;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Har Vey Yuen
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycles = new int[alphabet().size()];
        for (int i = 0; i < alphabet().size(); i++) {
            _cycles[i] = i;
        }
        _invertCycles = new int[alphabet().size()];
        for (int i = 0; i < alphabet().size(); i++) {
            _invertCycles[i] = i;
        }
        ArrayList<String> splitCycles = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\((.*?)\\)");
        Matcher matcher = pattern.matcher(cycles);
        while (matcher.find()) {
            splitCycles.add(matcher.group());
        }
        for (String cycle : splitCycles) {
            addCycle(cycle);
        }

    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        String checkCycle = cycle.substring(1, cycle.length() - 2);
        char[] checkArray = checkCycle.toCharArray();
        for (char c : checkArray) {
            if (!alphabet().contains(c)) {
                throw error("cycle contains char not in alphabet");
            }
        }
        int temp1 = _cycles[alphabet().toInt(cycle.charAt(1))];
        int temp2 =
                _invertCycles[alphabet().toInt(
                        cycle.charAt(cycle.length() - 2))];
        for (int i = 1; i < cycle.length() - 2; i++) {
            _cycles[alphabet().toInt(cycle.charAt(i))]
                    = _cycles[alphabet().toInt(cycle.charAt(i + 1))];
            _invertCycles[alphabet().toInt
                    (cycle.charAt(cycle.length() - 1 - i))] =
                    _invertCycles[alphabet().toInt(
                            cycle.charAt(cycle.length() - 2 - i))];
        }
        _cycles[alphabet().toInt(cycle.charAt(cycle.length() - 2))] = temp1;
        _invertCycles[alphabet().toInt(cycle.charAt(1))] = temp2;
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return alphabet().size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        return _cycles[wrap(p)];
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        return _invertCycles[wrap(c)];
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        return alphabet().toChar(permute(alphabet().toInt(p)));
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        return alphabet().toChar(invert(alphabet().toInt(c)));
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (int i : _cycles) {
            if (_cycles[i] == i) {
                return false;
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** Array representing forward permutations. */
    private int[] _cycles;

    /** Array representing backward permutations. */
    private int[] _invertCycles;
}
