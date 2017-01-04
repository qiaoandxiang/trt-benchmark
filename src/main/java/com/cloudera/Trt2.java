package com.cloudera;

/**
 * Created by hsun on 1/4/17.
 */
public class Trt2 {
    static final long INITIAL_MIN_TIMESTAMP = Long.MAX_VALUE;
    static final long INITIAL_MAX_TIMESTAMP = -1L;
    long minimumTimestamp = INITIAL_MIN_TIMESTAMP;
    long maximumTimestamp = INITIAL_MAX_TIMESTAMP;

    /**
     * Default constructor.
     * Initializes TimeRange to be null
     */
    public Trt2() {}

    private void set(final long min, final long max) {
      this.minimumTimestamp = min;
      this.maximumTimestamp = max;
    }

    /**
     * @param l
     * @return True if we initialized values
     */
    private boolean init(final long l) {
      if (this.minimumTimestamp != INITIAL_MIN_TIMESTAMP) return false;
      set(l, l);
      return true;
    }

    public synchronized void includeTimestamp(final long timestamp) {
      // Do test outside of synchronization block.  Synchronization in here can be problematic
      // when many threads writing one Store -- they can all pile up trying to add in here.
      // Happens when doing big write upload where we are hammering on one region.
      if (timestamp < this.minimumTimestamp) {
        synchronized (this) {
          if (!init(timestamp)) {
            if (timestamp < this.minimumTimestamp) {
              this.minimumTimestamp = timestamp;
            }
          }
        }
      } else if (timestamp > this.maximumTimestamp) {
        synchronized (this) {
          if (!init(timestamp)) {
            if (this.maximumTimestamp < timestamp) {
              this.maximumTimestamp =  timestamp;
            }
          }
        }
      }
    }

    public synchronized boolean includesTimeRange(final long min, final long max) {
      return (this.minimumTimestamp < max && this.maximumTimestamp >= min);
    }

    public synchronized long getMin() {
      return minimumTimestamp;
    }

    public synchronized long getMax() {
      return maximumTimestamp;
    }
}
