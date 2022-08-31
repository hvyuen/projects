package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Har Vey Yuen
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        if (notches.contains("(") || notches.contains(")")
                || notches.contains("*")) {
            throw error("notches contained invalid character");
        }
        _notches = notches;
    }

    @Override
    boolean rotates() {
        return true;
    }

    @Override
    void advance() {
        set(permutation().wrap(setting() + 1));
    }

    @Override
    String notches() {
        return _notches;
    }

    /** String representing characters of the notches. */
    private String _notches;
}
