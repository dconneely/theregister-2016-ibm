package com.davidconneely;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVFormat;

public class DecathlonTest {
    @Test
    public void testFromComp() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("Carter 100m 10.64\n" +
                "Bush 100m 10.20\n" +
                "Reagan 100m 10.3\n" +
                "#\n" +
                "Reagan Javelin 60.4\n" +
                "Carter Javelin 64.3\n" +
                "REAGAN Long 690\n" +
                "Bush 400m 43.2\n" +
                "#\n" +
                "##\n\n\n"));
        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);
        Decathlon.processFile(reader, writer);
        Assert.assertEquals("BUSH                 1047\n" +
                "REAGAN               1023\n" +
                "CARTER                942\n" +
                "\n" +
                "REAGAN               1534\n" +
                "BUSH                 1155\n" +
                "CARTER                803", sw.toString());
    }

    @Test
    public void testValidData() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("  \t A  \t 100m \t 10.0 \t   \n" +
                "B 100m 10.1\n" +
                "    C 100m 10.2\n" +
                "D 100m 10.3                                               \r\n" +
                "EEE-EEE-EEE-EEE-EEE-EEE-EEE-EEE 100m 10.4\r\n" +
                "#  \t \n" +
                "F 100m 10.5\r\n" +
                "## \t \n" +
                "G 100m 10.6\n\n"));
        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);
        Decathlon.processFile(reader, writer);
        Assert.assertEquals("A                    1096\n" +
                "B                    1071\n" +
                "C                    1047\n" +
                "D                    1023\n" +
                "EEE-EEE-EEE-EEE-EEE-EEE-EEE-EEE999", sw.toString());
    }

    @Test
    public void testInvalidData() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("\n" +
                "\n" +
                "A\n" +
                "B C\n" +
                "D 100m\r\n" +
                "E 100m F\n" +
                "G H 10.0\r\n" +
                "I 100m J\r\n" +
                "K 100m 10.1L\n" +
                "M 100m 9999.9999\n" +
                "#\n" +
                "##"));
        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);
        Decathlon.processFile(reader, writer);
        Assert.assertEquals("M                       0", sw.toString());
    }

    public void makeCSV() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream("/Users/dconneely/Downloads/Decathlon2000.csv")), StandardCharsets.UTF_8))) {
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream("/Users/dconneely/Downloads/Decathlon2000.dat")), StandardCharsets.UTF_8))) {
                Iterable<CSVRecord> records = CSVFormat.EXCEL.withFirstRecordAsHeader().parse(reader);
                String lastYear = "1985";
                for (CSVRecord record : records) {
                    String name = record.get("DecathleteName");
                    int slastName = name.lastIndexOf(' ');
                    String lastName = name.substring(slastName + 1);
                    String meas100m = record.get("m100");
                    String measLong = record.get("Longjump");
                    double measureLong = Double.parseDouble(measLong) * 100.0;
                    String measShot = record.get("Shotput");
                    String measHigh = record.get("Highjump");
                    double measureHigh = Double.parseDouble(measHigh) * 100.0;
                    String meas400m = record.get("m400");
                    String meas110m = record.get("m110hurdles");
                    String measDiscus = record.get("Discus");
                    String measPole = record.get("Polevault");
                    double measurePole = Double.parseDouble(measPole) * 100.0;
                    String measJavelin = record.get("Javelin");
                    String meas1500m = record.get("m1500");
                    String year = record.get("yearEvent");
                    if (!year.equals(lastYear)) {
                        writer.println("#");
                        lastYear = year;
                    }
                    writer.println(lastName + " 100m " + meas100m);
                    writer.println(lastName + " Long " + measureLong);
                    writer.println(lastName + " Shot " + measShot);
                    writer.println(lastName + " High " + measureHigh);
                    writer.println(lastName + " 400m " + meas400m);
                    writer.println(lastName + " 110m " + meas110m);
                    writer.println(lastName + " Discus " + measDiscus);
                    writer.println(lastName + " Pole " + measurePole);
                    writer.println(lastName + " Javelin " + measJavelin);
                    writer.println(lastName + " 1500m " + meas1500m);
                }
                writer.println("#");
                writer.println("##");
                writer.flush();
            }
        }
    }

    private static final String IN_FILE = "/Users/dconneely/Downloads/Decathlon2000.dat";
    private static final String OUT_FILE = "/Users/dconneely/Downloads/Decathlon2000.out";

    @Test
    public void testFrom2000() throws IOException {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(IN_FILE), StandardCharsets.UTF_8))) {
            try (final PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(OUT_FILE), StandardCharsets.UTF_8)))) {
                Decathlon.processFile(reader, writer);
            }
        }
    }

    /*
    @Test
    public void testTrimRight() {
        Assert.assertEquals("##", Decathlon.trimRight("##\t    \t"));
        Assert.assertEquals("\t    \t##", Decathlon.trimRight("\t    \t##"));
        Assert.assertEquals("\t    ##", Decathlon.trimRight("\t    ##    \t"));
    }
    */

    public static void main(String[] args) throws IOException {
        final int ITERATIONS = 5;
        DecathlonTest instance = new DecathlonTest();
        long t0 = System.nanoTime();
        for (int i = 0; i < ITERATIONS; ++i) {
            instance.testFrom2000();
        }
        long t1 = System.nanoTime();
        System.out.println("[" + (t1 - t0) / (ITERATIONS * 1000000000.0) + " seconds]");
    }
}
