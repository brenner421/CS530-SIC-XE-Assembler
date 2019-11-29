import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.lang.Integer;
import java.util.Map;
import java.util.HashMap;


public class SICXESimulator implements Runnable {

    public Memory mem;
    private static Map<Integer, Operation> opcodeMap;

    private interface Operation {
        boolean execute(Memory mem, int addr, boolean indexed);
    }

    static {
        opcodeMap = new HashMap<>(26, 0.7f);
        
        opcodeMap.put(Commands.OP_LDA,  (op, addr, ind) ->  Commands.lda(op, addr, ind));
        opcodeMap.put(Commands.OP_STA,  (op, addr, ind) ->  Commands.sta(op, addr, ind));
        opcodeMap.put(Commands.OP_STCH, (op, addr, ind) ->  Commands.stch(op, addr, ind));
    }

    public SICXESimulator() {
        mem = new Memory();
    }

    public SICXESimulator(String filename, int size) {
        mem = new Memory(size);
        loadProgram(filename);
    }

    // Load file into the memory at the specified location.
    // Should also change the value of PC so it executes from loaded program
    public void loadProgram(String filename) {
        String progName = "";
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(new File(filename)))) {

            // This code pretty gross but it works :/
            while ((line = br.readLine()) != null) {
                if ((line.charAt(0) == 'H') && (line.length() >= 19)) {
                    progName = line.substring(1, 7);
                    mem.PC = Integer.parseInt(line.substring(7, 13), 16);
                    System.err.printf("Loading \"%s\" at %04x ... ", progName, mem.PC);
                }
                else if (line.charAt(0) == 'T' && (line.length() >= 9)) {
                    int addr = Integer.parseInt(line.substring(1, 7), 16);
                    int len = 2 * Integer.parseInt(line.substring(7, 9), 16);
                    for (int i = 0; i < len; i += 2) {
                        mem.setByte(Integer.parseInt(line.substring(9 + i, 11 + i), 16), addr + (i / 2), false);
                    }
                }
                else if (line.charAt(0) == 'E') {
                    System.err.printf("Loaded %s\n", progName);
                }
            }
        }
        catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void run() {
        boolean error = false;
        while (!error && (mem.PC >= 0) && (mem.PC < mem.memory.length)) {
            // Fetch the next word in memory at the PC
            int nextWord = mem.getWord(mem.PC, false);
            mem.PC += 3; // increment PC before executing the instruction

            // Decode the next word
            int opcode = nextWord >> 16;
            int address = nextWord & 0x7FFF;
            boolean indexed = (nextWord & 0x8000) != 0;

            // Execute the instruction
            Operation op = opcodeMap.get(opcode); // Get method associated with opcode
            if (op != null) {
                try {
                    error = op.execute(mem, address, indexed);
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    error = true; // There was an error with addressing
                }
            }
            else {
                error = true; // The opcode was invalid
            }
        }
        // So we can see contents of memory when error occurred
        mem.hexDump("data.txt");
    }

    public static void main(String[] args) {

        SICXESimulator sim = new SICXESimulator("hfive.obj", 0x3000);
        int PC = sim.mem.PC; // save PC because it will change with next load
        sim.loadProgram("hcopy.obj");
        sim.mem.PC = PC;

        sim.run(); // start!!
    }
}