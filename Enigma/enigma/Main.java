package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.regex.Pattern;
import java.util.Scanner;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Har Vey Yuen
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            CommandArgs options =
                new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                            + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Open the necessary files for non-option arguments ARGS (see comment
      *  on main). */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        _machine = readConfig();
        if (!_input.hasNext("\\*")) {
            throw error("first char of setting line was not *");
        }
        while (_input.hasNextLine()) {
            String result = _input.nextLine();
            if (result.equals("")) {
                System.out.println();
            } else if (result.charAt(0) == '*') {
                setUp(_machine, result.substring(1));
            } else {
                printMessageLine(_machine.convert(result));
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            _alphabet = new Alphabet(_config.next());
            if (!_config.hasNextInt()) {
                throw error("next token should be "
                        + "an integer (number of rotor slots)");
            }
            int numRotors = _config.nextInt();
            if (!_config.hasNextInt()) {
                throw error("next token should be "
                        + "an integer (number of pawls)");
            }
            int numPawls = _config.nextInt();
            if (numPawls >= numRotors) {
                throw error("number of pawls should be "
                        + "less than number of rotors");
            }
            Collection<Rotor> allRotors = new ArrayList<Rotor>();
            while (_config.hasNext()) {
                allRotors.add(readRotor());
            }
            return new Machine(_alphabet, numRotors, numPawls, allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String name = _config.next();
            if (name.contains("(") || name.contains(")")) {
                throw error("name contains invalid characters");
            }
            String type = _config.next();
            String cycles = "";
            while (_config.hasNext(Pattern.compile("\\(.+\\)"))) {
                cycles += _config.next();
            }
            if (type.equals("R")) {
                return new Reflector(name,
                        new Permutation(cycles, _alphabet));
            } else if (type.equals("N")) {
                return new FixedRotor(name,
                        new Permutation(cycles, _alphabet));
            } else if (type.charAt(0) == 'M') {
                return new MovingRotor(name,
                        new Permutation(cycles, _alphabet), type.substring(1));
            } else {
                throw error("wrong rotor type");
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        String[] setArray = settings.trim().split("\\s+");
        if (setArray.length <= M.numRotors()) {
            throw error("not enough arguments");
        }
        String[] rotorArray = new String[M.numRotors()];
        for (int i = 0; i < M.numRotors(); i++) {
            rotorArray[i] = setArray[i];
        }
        String rotorSetting = setArray[M.numRotors()];
        int startingCycle;
        String ringSetting = "";
        if (setArray.length > M.numRotors() + 1
                && setArray[M.numRotors() + 1].charAt(0) != '(') {
            ringSetting = setArray[M.numRotors() + 1];
            startingCycle = M.numRotors() + 2;
        } else {
            startingCycle = M.numRotors() + 1;
        }
        String cycles = "";
        for (int i = startingCycle; i < setArray.length; i++) {
            cycles += setArray[i];
        }
        M.insertRotors(rotorArray);
        M.setRotors(rotorSetting);
        if (setArray.length > M.numRotors() + 1
                && setArray[M.numRotors() + 1].charAt(0) != '(') {
            M.setRing(ringSetting);
        }
        M.setPlugboard(new Permutation(cycles, _alphabet));
    }

    /** Return true iff verbose option specified. */
    static boolean verbose() {
        return _verbose;
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        char[] resultArray = msg.toCharArray();
        for (int i = 0; i < resultArray.length; i++) {
            _output.print(resultArray[i]);
            if ((i + 1) % 5 == 0) {
                _output.print(" ");
            }
        }
        System.out.println();
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** True if --verbose specified. */
    private static boolean _verbose;

    /** The enigma machine. */
    private Machine _machine;
}
