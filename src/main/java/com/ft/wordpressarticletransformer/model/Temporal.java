package com.ft.wordpressarticletransformer.model;

import com.google.common.base.Objects;

/**
 * @author Simon
 */
public class Temporal {

    private boolean realtime;
    private boolean inProgress;

    public Temporal(boolean realtime, boolean inProgress) {
        this.realtime = realtime;
        this.inProgress = inProgress;
    }

    public boolean isRealtime() {
        return realtime;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Temporal temporal = (Temporal) o;

        if (inProgress != temporal.inProgress) return false;
        if (realtime != temporal.realtime) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (realtime ? 1 : 0);
        result = 31 * result + (inProgress ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("realtime", realtime)
                .add("inProgress", inProgress)
                .toString();
    }
}
