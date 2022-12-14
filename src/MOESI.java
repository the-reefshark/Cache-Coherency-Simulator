public class MOESI extends MESI {
    MOESIBus moesiBus;

    MOESI(int cacheSize, int associativity, int blockSize, MOESIBus bus, Logger logger) {
        super(cacheSize, associativity, blockSize, null, logger);
        this.moesiBus = bus;

        for (int i = 0; i < numSets;i++) {
            sets[i] = new MOESILRUCache(associativity, logger);
        }
    }

    void load(long address) {
        int setIndex = getSetIndex(address);
        MESILRUCache cacheSet = sets[setIndex];
        int t = getTag(address);
        cacheSet.put(t);

        if (moesiBus.otherCacheContainsCache(address, this)) {
            // cache to cache sharing
            moesiBus.share(address);
            logger.incrementIdleTime(2 * (blockSize / 4));
            moesiBus.incrementDataTraffic(blockSize);
            CacheLine cacheLine = getCacheLine(address);
            cacheLine.setState('S');
        } else {
            // load from memory
            exclusive(address);
            logger.incrementIdleTime(100);
        }
    }

    void write(long address) {
        if (!contains(address)) {
            load(address);
            logger.incrementMiss();
        } else {
            logger.incrementIdleTime(1);
        }
        CacheLine cacheLine = getCacheLine(address);
        
        if (cacheLine.getState() == 'E') {
            cacheLine.setState('M');
        }
        else if ( cacheLine.getState() == 'S' || cacheLine.getState() == 'O') {
            if (moesiBus.otherCacheContainsCache(address, this)) {
                moesiBus.invalidate(address, this);
            }
            cacheLine.setState('M');
        }
        cacheLine.setDirty();
        countPrivatePublicAccess(address);
    }

    boolean invalidate(long address) {
        int setIndex = getSetIndex(address);
        MESILRUCache cache = sets[setIndex];
        int tag = getTag(address);
        if (!cache.contains(tag)) return false;

        CacheLine m =  cache.getCacheLine(tag);
        if ( m.isDirty && (m.getState() == 'O' || m.getState() == 'M') ) {
            m.isDirty = false;
            return true;
        }
        m.setState('I');
        return false;
    }

    void share(long address) {
        int setIndex = getSetIndex(address);
        MESILRUCache cache = sets[setIndex];
        int tag = getTag(address);
        if (!cache.contains(tag))
            return;

        CacheLine m = cache.getCacheLine(tag);

        if (m.getState() == 'M') {
            m.setState('O');
        }
        else if (m.getState() != 'O' && m.getState() != 'I') {
            m.setState('S');
        }


        logger.incrementIdleTime(2 * (blockSize / 4));
        moesiBus.incrementDataTraffic(blockSize);
    }
}
