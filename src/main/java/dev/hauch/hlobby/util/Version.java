package dev.hauch.hlobby.util;

/**
 * Utilities for comparing dotted version strings.
 */
public final class Version {

    private Version() {
    }

    /**
     * Compares two version strings.
     *
     * @param candidate the candidate version
     * @param current the currently installed version
     * @return {@code true} when {@code candidate} is newer than {@code current}
     */
    public static boolean isNewer(final String candidate, final String current) {
        if (candidate == null || candidate.isBlank()) {
            return false;
        }
        if (current == null || current.isBlank()) {
            return true;
        }
        final String[] candidateParts = candidate.split("[.\\-]");
        final String[] currentParts = current.split("[.\\-]");
        final int length = Math.max(candidateParts.length, currentParts.length);
        for (int i = 0; i < length; i++) {
            final int candidateValue = i < candidateParts.length ? numeric(candidateParts[i]) : 0;
            final int currentValue = i < currentParts.length ? numeric(currentParts[i]) : 0;
            if (candidateValue != currentValue) {
                return candidateValue > currentValue;
            }
        }
        return false;
    }

    /**
     * Extracts the leading numeric value of a version segment.
     *
     * @param value the segment, possibly containing a suffix such as
     *        {@code SNAPSHOT}
     * @return the numeric value, or {@code 0} when none is present
     */
    public static int numeric(final String value) {
        if (value == null) {
            return 0;
        }
        final String digits = value.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(digits);
        } catch (final NumberFormatException exception) {
            return 0;
        }
    }
}
