import java.io.FileNotFoundException;
import java.io.IOException;

public class MesiProcessor {
    MESI mesiCache;
    Logger logger;
    InstructionReader reader;

    MesiProcessor(int cacheSize, int associativity, int blockSize, String inputFile, MESIBus bus) throws FileNotFoundException {
        logger = new Logger(bus, blockSize);
        mesiCache = new MESI(cacheSize, associativity, blockSize, bus, logger);
        reader = new InstructionReader(inputFile, logger);
    }

    void executeOneCycle(long clockCycle) throws IOException {
        if (logger.getTotalTime() > clockCycle) {
            return;
        }
        if (reader.hasNext) {
            executeInstruction();
        }
    }

    void executeInstruction() throws IOException {
        if (reader.fetchNextIntruction()) {
            mesiCache.executeInstruction(reader.getNextInstruction());
        }
    }

    void printInfo() {
        logger.printInfo();
    }

    boolean hasNext() {
        return reader.hasNext;
    }

}
