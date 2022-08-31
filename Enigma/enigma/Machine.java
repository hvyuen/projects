package enigma;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Har Vey Yuen
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = allRotors;
        _currentRotors = new ArrayList<>();
        _plugboard = new Permutation("", alpha);
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {
        Iterator<Rotor> rotorIterator = _currentRotors.iterator();
        for (int i = 0; i < k; i++) {
            rotorIterator.next();
        }
        return rotorIterator.next();
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        _currentRotors = new ArrayList<>();
        for (String str : rotors) {
            for (Rotor r : _allRotors) {
                if (str.equals(r.name())) {
                    _currentRotors.add(r);
                }
            }
        }
        int pawlCounter = 0;
        for (Rotor r : _currentRotors) {
            if (r.rotates()) {
                pawlCounter++;
            }
        }
        if (pawlCounter != _pawls) {
            throw error("wrong number of moving rotors");
        }
        if (!_currentRotors.get(0).reflecting()) {
            throw error("reflector in wrong place");
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        char[] settingArray = setting.toCharArray();
        if (settingArray.length != _currentRotors.size() - 1) {
            throw error("wrong number of arguments");
        }
        for (int i = 1; i < _currentRotors.size(); i++) {
            _currentRotors.get(i).set(settingArray[i - 1]);
        }
    }

    void setRing(String ringSetting) {
        for (int i = 1; i < _currentRotors.size(); i++) {
            _currentRotors.get(i).setRing(
                    alphabet().toInt(ringSetting.charAt(i - 1)));
        }
    }

    /** Return the current plugboard's permutation. */
    Permutation plugboard() {
        return _plugboard;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /** Advance all rotors to their next position. */
    private void advanceRotors() {
        for (int i = 1; i < _currentRotors.size() - 1; i++) {
            if (_currentRotors.get(i).rotates()
                    && (_currentRotors.get(i + 1).atNotch()
                    ||  (_currentRotors.get(i).atNotch()
                    && _currentRotors.get(i - 1).rotates()))) {
                _currentRotors.get(i).advance();
            }
        }
        _currentRotors.get(_currentRotors.size() - 1).advance();
    }

    /** Return the result of applying the rotors to the character C (as an
     *  index in the range 0..alphabet size - 1). */
    private int applyRotors(int c) {
        for (int i = _currentRotors.size() - 1; i > 0; i--) {
            c = _currentRotors.get(i).convertForward(c);
        }
        for (int i = 0; i < _currentRotors.size(); i++) {
            c = _currentRotors.get(i).convertBackward(c);
        }
        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        msg = msg.replace(" ", "");
        char[] msgArray = msg.toCharArray();
        for (int i = 0; i < msgArray.length; i++) {
            msgArray[i]
                    = alphabet().toChar(convert(alphabet().toInt(msgArray[i])));
        }
        String result = "";
        for (char c : msgArray) {
            result += c;
        }
        return result;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Number of rotors. */
    private int _numRotors;

    /** Number of pawls. */
    private int _pawls;

    /** Collection of all possible rotors. */
    private Collection<Rotor> _allRotors;

    /** List of current rotors. */
    private ArrayList<Rotor> _currentRotors;

    /** Plugboard permutation. */
    private Permutation _plugboard;
}
