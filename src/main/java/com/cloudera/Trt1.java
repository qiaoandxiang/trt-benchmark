package com.cloudera;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by hsun on 1/4/17.
 */
public class Trt1 {
    static final long INITIAL_MIN_TIMESTAMP = Long.MAX_VALUE;
    static final long INITIAL_MAX_TIMESTAMP = -1L;

    AtomicLong minimumTimestamp = new AtomicLong(INITIAL_MIN_TIMESTAMP);
    AtomicLong maximumTimestamp = new AtomicLong(INITIAL_MAX_TIMESTAMP);

    /**
     * Default constructor.
     * Initializes TimeRange to be null
     */
    public Trt1() {}

    void includeTimestamp(final long timestamp) {
      long curMinTimestamp = this.minimumTimestamp.get();
      if (timestamp < curMinTimestamp) {
        while (timestamp < curMinTimestamp) {
          if (!this.minimumTimestamp.compareAndSet(curMinTimestamp, timestamp)) {
            curMinTimestamp = this.minimumTimestamp.get();
          } else {
            // successfully set minimumTimestamp, break
            break;
          }
        }

        // When it hits here, there are two possibilities:
        //  1). timestamp >= curMinTimestamp, someone already sets the min, return;
        //  2). timestamp < curMinTimestamp, we set the min successfully. In this case,
        //      we still need to check if curMinTimestamp == INITIAL_MIN_TIMESTAMP to see
        //      if we need to proceed to set maximumTimestamp if no one else sets the
        //      maximumTimestamp yet (the very first time call)
        if (curMinTimestamp != INITIAL_MIN_TIMESTAMP) {
          // If this has not been set by anyone yet, let's try to set
          // the maximumTimestamp
          return;
        }
      }

      long curMaxTimestamp = this.maximumTimestamp.get();

      if (timestamp > curMaxTimestamp) {
        while (timestamp > curMaxTimestamp) {
          if (!this.maximumTimestamp.compareAndSet(curMaxTimestamp, timestamp)) {
            curMaxTimestamp = this.maximumTimestamp.get();
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
