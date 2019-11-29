import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

public class Memory {

    public byte[] memory;
    public int A, X, L, SW, PC, B, S, T, F;

    public Memory() {
        this(1024);
    }
    public Memory(int size) {
        if (size > 32768 || size <= 0) {
            size = 32768;
        }
        memory = new byte[size];
    }

    // Get the word in memory at addr; most significant byte is zeroed.
    public int getWord(int addr, boolean indexed) {
        int offset = addr;
        if (indexed)
            offset += X;
        int msb = memory[offset];
        int mid = memory[offset + 1];
        int lsb = memory[offset + 2];
        if (msb < 0) msb += 256;
        if (mid < 0) mid += 256;
        if (lsb < 0) lsb += 256;
        return (msb << 16) | mid << 8 | lsb;
    }

    public int getByte(int addr, boolean indexed) {
        if (indexed)
            return memory[addr + X];
        return (int) memory[addr];
    }

    // Sets the word in memory at addr to the first 24 bits of val
    public void setWord(int val, int addr, boolean indexed) {
        int offset = addr;
        if (indexed)
            offset += X;
        memory[offset]     = (byte) (val >> 16);
        memory[offset + 1] = (byte) (val >> 8);
        memory[offset + 2] = (byte) val;
    }

    public void setByte(int val, int addr, boolean indexed) {
        if (indexed)
            memory[addr + X] = (byte) val;
        else
            memory[addr] = (byte) val;
    }

    // Save the memory contents to a file so we can see the state of memory
    public void hexDump(String filename) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filename)))) {
            bw.write("Registers: \n");
            bw.write(String.format(" A: %06x\t  X: %06x\t  L: %06x\t SW: %06x\t PC: %06x\t B: %06x\t S: %06x\t T: %06x\t F: %06x\n", A, X, L, SW, PC, B, S, T, F));
            for (int i = 0; i < memory.length; i++) {
                if (i % 16 == 0) bw.write(String.format("\n%04x   ", i));
                bw.write(String.format("%02x", memory[i]));
                if (i + 1 < memory.length) bw.write(" ");
            }
        }
        catch (IOException e) {
            System.err.println("Could not do hexdump: " + e.getMessage());
        }
    }
}