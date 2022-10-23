public class MOESIBus extends MESIBus{
    int numInvalidate;

    public MOESIBus() {
        super();
        numInvalidate = 0;
    }

    public void invalidate(long address, MESI writer) {
        numInvalidate++;
        for (MESI m : caches) {
            if (m != writer) {
                m.invalidate(address);
            }
        }
    }

    public void printStats() {
        super.printStats();
        System.out.printf("Number of invalidations on the bus: %d%n", numInvalidate);
    }
}