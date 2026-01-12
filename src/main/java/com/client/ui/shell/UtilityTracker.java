package com.client.ui.shell;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight parser that updates {@link UtilityState} by watching chat.
 *
 * Why chat? It's the lowest-friction integration: you can add server messages
 * now, and later migrate to custom packets without changing the UI.
 */
public final class UtilityTracker {

    private UtilityTracker() {}

    // Examples supported:
    //  - "Next global boss in 05:00" / "Next global boss: 1h 15m" / "Next global boss in 90s"
    private static final Pattern NEXT_BOSS_MMSS = Pattern.compile("next\\s+global\\s+boss.*?(\\d{1,2})\\s*:(\\d{2})(?:\\s*:(\\d{2}))?", Pattern.CASE_INSENSITIVE);
    private static final Pattern NEXT_BOSS_HMS_WORDS = Pattern.compile("next\\s+global\\s+boss.*?(?:(\\d+)\\s*h)?\\s*(?:(\\d+)\\s*m)?\\s*(?:(\\d+)\\s*s)?", Pattern.CASE_INSENSITIVE);

    // Goals: "Donation goal: 120/500" , "Vote goal: 34/100"
    private static final Pattern DONATION_GOAL = Pattern.compile("donation\\s+goal\\s*[:=]\\s*(\\d+)\\s*/\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern VOTE_GOAL = Pattern.compile("vote\\s+goal\\s*[:=]\\s*(\\d+)\\s*/\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    // Slayer patterns (common RSPS phrasing)
    private static final Pattern SLAYER_ASSIGN_1 = Pattern.compile("(?:your|you have been)\\s+assigned\\s+to\\s+kill\\s+(\\d+)\\s+(.+?)\\.?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SLAYER_ASSIGN_2 = Pattern.compile("your\\s+task\\s+is\\s+to\\s+kill\\s+(\\d+)\\s+(.+?)\\.?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SLAYER_REMAINING = Pattern.compile("you\\s+have\\s+(\\d+)\\s+more\\s+to\\s+go", Pattern.CASE_INSENSITIVE);
    private static final Pattern SLAYER_POINTS = Pattern.compile("you\\s+have\\s+(\\d+)\\s+slayer\\s+points?", Pattern.CASE_INSENSITIVE);

    // Other points (best-effort)
    private static final Pattern VOTE_POINTS = Pattern.compile("you\\s+have\\s+(\\d+)\\s+vote\\s+points?", Pattern.CASE_INSENSITIVE);
    private static final Pattern DONATION_POINTS = Pattern.compile("you\\s+have\\s+(\\d+)\\s+(?:donation|donator|donator's|donators)\\s+points?", Pattern.CASE_INSENSITIVE);

    // Market lines: server can prefix them to get captured
    // Example: "[Market] Selling ..." or "[MARKET] ..."
    private static final Pattern MARKET_PREFIX = Pattern.compile("^\\s*\\[\\s*market\\s*]\\s*(.+)$", Pattern.CASE_INSENSITIVE);

    public static void onChatMessage(String msg, int type, String from) {
        if (msg == null) return;

        // Market capture first (so it works regardless of other parsing)
        Matcher mMarket = MARKET_PREFIX.matcher(msg);
        if (mMarket.find()) {
            UtilityState.INSTANCE.addMarketLine(mMarket.group(1).trim());
        }

        // Only parse server/system-ish lines; avoid player spam.
        // Type values vary by base, so this is a best-effort filter.
        if (type != 0 && type != 5 && type != 7 && type != 8 && type != 11) {
            return;
        }

        parseBossTimer(msg);
        parseGoals(msg);
        parseSlayer(msg);
        parseOtherPoints(msg);
    }

    private static void parseBossTimer(String msg) {
        long now = System.currentTimeMillis();

        Matcher mmss = NEXT_BOSS_MMSS.matcher(msg);
        if (mmss.find()) {
            int a = safeInt(mmss.group(1));
            int b = safeInt(mmss.group(2));
            String cStr = mmss.group(3);
            int c = cStr == null ? -1 : safeInt(cStr);

            long seconds;
            if (c >= 0) {
                // hh:mm:ss
                seconds = (long) a * 3600L + (long) b * 60L + c;
            } else {
                // mm:ss
                seconds = (long) a * 60L + b;
            }
            UtilityState.INSTANCE.setNextGlobalBossEpochMs(now + seconds * 1000L);
            return;
        }

        String lower = msg.toLowerCase(Locale.ROOT);
        if (!lower.contains("next") || !lower.contains("global") || !lower.contains("boss")) {
            return;
        }

        Matcher words = NEXT_BOSS_HMS_WORDS.matcher(msg);
        if (words.find()) {
            int h = safeInt(words.group(1));
            int m = safeInt(words.group(2));
            int s = safeInt(words.group(3));
            long seconds = (long) h * 3600L + (long) m * 60L + s;
            if (seconds > 0) {
                UtilityState.INSTANCE.setNextGlobalBossEpochMs(now + seconds * 1000L);
            }
        }
    }

    private static void parseGoals(String msg) {
        Matcher d = DONATION_GOAL.matcher(msg);
        if (d.find()) {
            UtilityState.INSTANCE.setDonationGoal(safeInt(d.group(1)), safeInt(d.group(2)));
        }
        Matcher v = VOTE_GOAL.matcher(msg);
        if (v.find()) {
            UtilityState.INSTANCE.setVoteGoal(safeInt(v.group(1)), safeInt(v.group(2)));
        }
    }

    private static void parseSlayer(String msg) {
        Matcher a1 = SLAYER_ASSIGN_1.matcher(msg);
        if (a1.find()) {
            int amount = safeInt(a1.group(1));
            String name = a1.group(2).trim();
            UtilityState.INSTANCE.setSlayerTask(name, amount, null);
            return;
        }
        Matcher a2 = SLAYER_ASSIGN_2.matcher(msg);
        if (a2.find()) {
            int amount = safeInt(a2.group(1));
            String name = a2.group(2).trim();
            UtilityState.INSTANCE.setSlayerTask(name, amount, null);
            return;
        }

        Matcher r = SLAYER_REMAINING.matcher(msg);
        if (r.find()) {
            UtilityState.INSTANCE.setSlayerRemaining(safeInt(r.group(1)));
        }

        Matcher p = SLAYER_POINTS.matcher(msg);
        if (p.find()) {
            UtilityState.INSTANCE.setSlayerPoints(safeInt(p.group(1)));
        }
    }

    private static void parseOtherPoints(String msg) {
        Matcher vp = VOTE_POINTS.matcher(msg);
        if (vp.find()) {
            UtilityState.INSTANCE.setVotePoints(safeInt(vp.group(1)));
        }
        Matcher dp = DONATION_POINTS.matcher(msg);
        if (dp.find()) {
            UtilityState.INSTANCE.setDonationPoints(safeInt(dp.group(1)));
        }
    }

    private static int safeInt(String s) {
        if (s == null) return 0;
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
