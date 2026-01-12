package com.client.ui.shell;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Shared state for the sidebar panels.
 *
 * This is intentionally client-side only. The server can feed it via chat
 * messages (see {@link UtilityTracker}) or later via custom packets.
 */
public final class UtilityState {

    public static final UtilityState INSTANCE = new UtilityState();

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    // Event countdowns
    private volatile long nextGlobalBossEpochMs = 0L;

    // Goals
    private volatile int donationCurrent = 0;
    private volatile int donationTarget = 0;
    private volatile int voteCurrent = 0;
    private volatile int voteTarget = 0;

    // Slayer
    private volatile String slayerTaskName = "";
    private volatile int slayerTaskAmount = 0;
    private volatile int slayerTaskRemaining = 0;
    private volatile int slayerPoints = 0;

    // Other common RSPS points
    private volatile int votePoints = 0;
    private volatile int donationPoints = 0;

    // Market
    private final Deque<String> marketLines = new ArrayDeque<>(200);

    private UtilityState() {}

    public void addListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    // ---------------- Events ----------------

    public long getNextGlobalBossEpochMs() {
        return nextGlobalBossEpochMs;
    }

    public void setNextGlobalBossEpochMs(long epochMs) {
        long old = this.nextGlobalBossEpochMs;
        this.nextGlobalBossEpochMs = Math.max(0L, epochMs);
        pcs.firePropertyChange("nextGlobalBossEpochMs", old, this.nextGlobalBossEpochMs);
    }

    // ---------------- Goals ----------------

    public int getDonationCurrent() { return donationCurrent; }
    public int getDonationTarget() { return donationTarget; }
    public int getVoteCurrent() { return voteCurrent; }
    public int getVoteTarget() { return voteTarget; }

    public void setDonationGoal(int current, int target) {
        int oc = this.donationCurrent, ot = this.donationTarget;
        this.donationCurrent = Math.max(0, current);
        this.donationTarget = Math.max(0, target);
        pcs.firePropertyChange("donationGoal", oc + "/" + ot, this.donationCurrent + "/" + this.donationTarget);
    }

    public void setVoteGoal(int current, int target) {
        int oc = this.voteCurrent, ot = this.voteTarget;
        this.voteCurrent = Math.max(0, current);
        this.voteTarget = Math.max(0, target);
        pcs.firePropertyChange("voteGoal", oc + "/" + ot, this.voteCurrent + "/" + this.voteTarget);
    }

    // ---------------- Slayer ----------------

    public String getSlayerTaskName() { return slayerTaskName; }
    public int getSlayerTaskAmount() { return slayerTaskAmount; }
    public int getSlayerTaskRemaining() { return slayerTaskRemaining; }
    public int getSlayerPoints() { return slayerPoints; }

    public int getVotePoints() { return votePoints; }

    public int getDonationPoints() { return donationPoints; }

    public void setSlayerTask(String name, int amount, Integer remainingOrNull) {
        String oldName = this.slayerTaskName;
        this.slayerTaskName = name == null ? "" : name;
        this.slayerTaskAmount = Math.max(0, amount);
        this.slayerTaskRemaining = remainingOrNull == null ? this.slayerTaskAmount : Math.max(0, remainingOrNull);
        pcs.firePropertyChange("slayerTask", oldName, this.slayerTaskName);
    }

    public void setSlayerRemaining(int remaining) {
        int old = this.slayerTaskRemaining;
        this.slayerTaskRemaining = Math.max(0, remaining);
        pcs.firePropertyChange("slayerRemaining", old, this.slayerTaskRemaining);
    }

    public void setSlayerPoints(int points) {
        int old = this.slayerPoints;
        this.slayerPoints = Math.max(0, points);
        pcs.firePropertyChange("slayerPoints", old, this.slayerPoints);
    }

    public void setVotePoints(int points) {
        int old = this.votePoints;
        this.votePoints = Math.max(0, points);
        pcs.firePropertyChange("votePoints", old, this.votePoints);
    }

    public void setDonationPoints(int points) {
        int old = this.donationPoints;
        this.donationPoints = Math.max(0, points);
        pcs.firePropertyChange("donationPoints", old, this.donationPoints);
    }

    // ---------------- Market ----------------

    public void addMarketLine(String line) {
        if (line == null || line.isEmpty()) return;
        synchronized (marketLines) {
            if (marketLines.size() >= 200) {
                marketLines.removeFirst();
            }
            marketLines.addLast(line);
        }
        pcs.firePropertyChange("market", null, line);
    }

    public List<String> snapshotMarket() {
        synchronized (marketLines) {
            return new ArrayList<>(marketLines);
        }
    }
}
