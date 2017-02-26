package com.davidconneely;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Single-threaded Decathlon processing class for IBM / The Register competition.
 */
public final class Decathlon {
    private static final String IN_FILE = "Decathlon.dat";
    private static final String OUT_FILE = "Decathlon.out";

    private Decathlon() {
        /* Prevent instantiation by public default constructor. */
    }

    /**
     * Open the input and output files, then pass control to processFile.
     * Report exceptions using exit code as we must produce no "screen output".
     */
    public static void main(final String[] args) {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(IN_FILE), StandardCharsets.UTF_8))) {
            try (final PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(OUT_FILE), StandardCharsets.UTF_8)))) {
                try {
                    processFile(reader, writer);
                } catch (IOException e) {
                    System.exit(74); // EX_IOERR
                } catch (Throwable e) {
                    System.exit(70); // EX_SOFTWARE
                }
            } catch (IOException e) {
                System.exit(73); // EX_CANTCREAT
            }
        } catch (IOException e) {
            System.exit(66); // EX_NOINPUT
        }
    }

    /**
     * Process a series of data sets from the given file.
     */
    static void processFile(final BufferedReader reader, final PrintWriter writer) throws IOException {
        List<Score> scores = readDataSet(reader);
        if (scores != null) {
            writeDataSet(scores, writer);
        }
        while ((scores = readDataSet(reader)) != null) {
            writer.println();
            writer.println();
            writeDataSet(scores, writer);
        }
        writer.flush();
    }

    /**
     * Read in and process a data set.
     */
    private static List<Score> readDataSet(final BufferedReader reader) throws IOException {
        final Map<String, Score> data = new HashMap<>();
        while (true) {
            final String line = reader.readLine();
            if (line == null) {
                return null; // EOF
            } else if (line.isEmpty()) {
                continue;
            } else if (line.charAt(0) == '#') {
                final String trimmed = trimRight(line);
                if (trimmed.length() == 1) {
                    // Return a league table.
                    final List<Score> scores = new ArrayList<>(data.values());
                    Collections.sort(scores);
                    return scores;
                } else if (trimmed.equals("##")) {
                    return null; // EOF
                }
            }
            processLine(line, data);
        }
    }

    /**
     * Strip whitespace (space or tab) from the right of given string.
     */
    private static String trimRight(final String str) {
        int len = str.length();
        char lastch;
        while (true) {
            if (len == 0) {
                return "";
            }
            lastch = str.charAt(--len);
            if (!isWhitespace(lastch)) {
                return str.substring(0, len + 1);
            }
        }
    }

    /**
     * Process a "name event measure" line from the input and accumulate points into the data set.
     */
    private static void processLine(final String line, final Map<String, Score> data) {
        final int len = line.length();
        int sname = 0;
        while (sname < len && isWhitespace(line.charAt(sname))) {
            ++sname;
        }
        int ename = sname;
        while (ename < len && !isWhitespace(line.charAt(ename))) {
            ++ename;
        }
        int sevent = ename;
        while (sevent < len && isWhitespace(line.charAt(sevent))) {
            ++sevent;
        }
        int eevent = sevent;
        while (eevent < len && !isWhitespace(line.charAt(eevent))) {
            ++eevent;
        }
        int smeas = eevent;
        while (smeas < len && isWhitespace(line.charAt(smeas))) {
            ++smeas;
        }
        int emeas = smeas;
        while (emeas < len && !isWhitespace(line.charAt(emeas))) {
            ++emeas;
        }
        if (sname == ename || sevent == eevent || smeas == emeas) {
            return;
        }
        final String uname = line.substring(sname, ename).toUpperCase(Locale.ROOT);
        final String uevent = line.substring(sevent, eevent).toUpperCase(Locale.ROOT);
        final String meas = line.substring(smeas, emeas);
        try {
            final double measure = Double.parseDouble(meas);
            final int ipoints = points(uevent, measure);
            final Score score = data.get(uname);
            if (score != null) {
                score.addPoints(ipoints);
            } else {
                data.put(uname, new Score(uname, ipoints));
            }
        } catch (NumberFormatException | UnsupportedOperationException e) {
            return;
        }
    }

    private static boolean isWhitespace(final char ch) {
        return ch == ' ' || ch == '\t';
    }

    /**
     * Calculate the points for a given measure in a given event.
     */
    private static int points(final String uevent, final double measure) {
        switch (uevent) {
            case "100M":
                return (int) (25.4347 * Math.pow(18.00 - measure, 1.81));
            case "110M":
                return (int) (5.74352 * Math.pow(28.50 - measure, 1.92));
            case "400M":
                return (int) (1.53775 * Math.pow(82.00 - measure, 1.81));
            case "1500M":
                return (int) (0.03768 * Math.pow(480.00 - measure, 1.85));
            case "DISCUS":
                return (int) (12.91 * Math.pow(measure - 4.00, 1.10));
            case "JAVELIN":
                return (int) (10.14 * Math.pow(measure - 7.00, 1.08));
            case "SHOT":
                return (int) (51.39 * Math.pow(measure - 1.50, 1.05));
            case "LONG":
                return (int) (0.14354 * Math.pow(measure - 220.00, 1.40));
            case "HIGH":
                return (int) (0.8465 * Math.pow(measure - 75.00, 1.42));
            case "POLE":
                return (int) (0.2797 * Math.pow(measure - 100.00, 1.35));
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static void writeDataSet(final List<Score> scores, final PrintWriter writer) {
        final Iterator<Score> it = scores.iterator();
        if (it.hasNext()) {
            justify(it.next(), writer);
        }
        while (it.hasNext()) {
            writer.println();
            justify(it.next(), writer);
        }
    }

    /**
     * Write a score with left-justified name and right-justified points with min-width 25.
     */
    private static void justify(final Score score, final PrintWriter writer) {
        final String name = score.getName();
        final String points = Integer.toString(score.getPoints());
        final int pad = 25 - name.length() - points.length();
        writer.print(name);
        for (int i = 0; i < pad; ++i) {
            writer.print(' ');
        }
        writer.print(points);
    }

    /**
     * Represents a competitor' name and his/her mutable overall points.
     * Not suitable for use as the key in a hashtable because mutable.
     */
    private static final class Score implements Comparable<Score> {
        private final String name;
        private int points;

        Score(final String name, final int points) {
            this.name = name;
            this.points = points;
        }

        void addPoints(final int points) {
            this.points += points;
        }

        String getName() {
            return name;
        }

        int getPoints() {
            return points;
        }

        @Override
        public int compareTo(final Score other) {
            final int diff = other.points - points;
            if (diff != 0) {
                return diff;
            }
            return name.compareTo(other.name);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final Score other = (Score) obj;
            return points == other.points && Objects.equals(name, other.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, points);
        }
    }
}
