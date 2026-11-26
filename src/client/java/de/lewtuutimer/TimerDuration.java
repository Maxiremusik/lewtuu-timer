package de.lewtuutimer;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class TimerDuration {
    private static final Pattern UNITS = Pattern.compile("(\\d+)\\s*([dhms])");

    private TimerDuration() { }

    static long parseMillis(String input) {
        String value = input.trim().toLowerCase(Locale.ROOT);
        long seconds = 0;
        if (value.matches("\\d+:\\d{1,2}:\\d{1,2}")) {
            String[] parts = value.split(":");
            long hours = Long.parseLong(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int secs = Integer.parseInt(parts[2]);
            if (minutes >= 60 || secs >= 60) throw new IllegalArgumentException();
            seconds = Math.addExact(Math.multiplyExact(hours, 3600), minutes * 60L + secs);
        } else {
            Matcher matcher = UNITS.matcher(value);
            int end = 0;
            int seen = 0;
            while (matcher.find()) {
                if (!value.substring(end, matcher.start()).isBlank()) throw new IllegalArgumentException();
                char unit = matcher.group(2).charAt(0);
                int bit = 1 << "dhms".indexOf(unit);
                if ((seen & bit) != 0) throw new IllegalArgumentException();
                seen |= bit;
                long multiplier = switch (unit) {
                    case 'd' -> 86400;
                    case 'h' -> 3600;
                    case 'm' -> 60;
                    default -> 1;
                };
                seconds = Math.addExact(seconds, Math.multiplyExact(Long.parseLong(matcher.group(1)), multiplier));
                end = matcher.end();
            }
            if (seen == 0 || !value.substring(end).isBlank()) throw new IllegalArgumentException();
        }
        return Math.multiplyExact(seconds, 1000);
    }
}
