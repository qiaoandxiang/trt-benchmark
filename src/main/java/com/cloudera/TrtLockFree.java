package com.cloudera;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by hsun on 1/4/17.
 */
public class TrtLockFree {
    static final long INITIAL_MIN_TIMESTAMP = Long.MAX_VALUE;
    static final long INITIAL_MAX_TIMESTAMP = -1L;

    AtomicLong minimumTimestamp = new AtomicLong(INITIAL_MIN_TIMESTAMP);
    AtomicLong maximumTimestamp = new AtomicLong(INITIAL_MAX_TIMESTAMP);
    public volatile long collision = 0;

    /**
     * Default constructor.
     * Initializes TimeRange to be null
     */
    public TrtLockFree() {}

    void includeTimestamp(final long timestamp) {
      long initialMinTimestamp = this.minimumTimestamp.get();
      if (timestamp < initialMinTimestamp) {
        long curMinTimestamp = initialMinTimestamp;
        while (timestamp < curMinTimestamp) {
          if (!this.minimumTimestamp.compareAndSet(curMinTimestamp, timestamp)) {
            curMinTimestamp = this.minimumTimestamp.get();
            //collision ++;
          } else {
            // successfully set minimumTimestamp, break.
            break;
          }
        }

        // When it hits here, there are two possibilities:
        //  1). timestamp >= curMinTimestamp, someone already sets the min. In this case,
        //      it still needs to check if firstMinTimestamp == INITIAL_MIN_TIMESTAMP to see
        //      if it needs to proceed to set maximumTimestamp. Someone may already set both
        //      min/max to the same value(curMinTimestamp), need to check if maximumTimestamp needs
        //      to be updated.
        //  2). timestamp < curMinTimestamp, this call sets the min successfully. In this case,
        //      it still needs to check if firstMinTimestamp == INITIAL_MIN_TIMESTAMP to see
        //      if it needs to proceed to set maximumTimestamp.
        if (initialMinTimestamp != INITIAL_MIN_TIMESTAMP) {
          // Someone already sets maximumTimestamp and timestamp is less than maximumTimestamp,
          // no need to proceed to set it.
          return;
        }
      }

      long curMaxTimestamp = this.maximumTimestamp.get();

      if (timestamp > curMaxTimestamp) {
        while (timestamp > curMaxTimestamp) {
          if (!this.maximumTimestamp.compareAndSet(curMaxTimestamp, timestamp)) {
            curMaxTimestamp = this.maximumTimestamp.get();
            //collision++;
          } else {
            // successfully set maximumTimestamp, break
            break;
          }
        }
      }
    }

    public boolean includesTimeRange(final long min, final long max) {
      return (this.minimumTimestamp.get() < max && this.maximumTimestamp.get() >= min);
    }
}
