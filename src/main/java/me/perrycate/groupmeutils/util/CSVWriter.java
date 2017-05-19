package me.perrycate.groupmeutils.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Flexible class to handle writing data to a .csv file.
 *
 * Does not escape commas.
 *
 * @author perry
 */
public class CSVWriter<T> {

    private List<String> columns;

    private List<Map<String, T>> data;

    /**
     * Creates a new CSVWriter with the given columns. When printing, the
     * columns will be in the given order.
     *
     * @param c
     */
    public CSVWriter(String[] c) {
        columns = new ArrayList<>(Arrays.asList(c));
        data = new ArrayList<>();
    }

    public CSVWriter() {
        columns = new ArrayList<>();
        data = new ArrayList<>();
    }

    /**
     * Adds a row of data to the output. Values should hold keys corresponding
     * to the names of existing columns. If a key does not match an existing
     * column, addRow will simply add a new column after the existing ones and
     * return true.
     *
     * @param values
     *            the key->value pairs to be added for each row
     * @returns true if a new column was created
     */
    public boolean addRow(Map<String, T> row) {

        // Update columns if necessary
        boolean modified = false;
        for (String c : row.keySet()) {
            if (!columns.contains(c)) {
                columns.add(c);
                modified = true;
            }
        }

        // Add row to data to be written
        data.add(row);

        return modified;
    }

    /**
     * Writes the data to the given file.
     *
     * Does not reset after printing, so subsequent calls will still contain the
     * previous data.
     *
     * @returns true if successful
     */
    public boolean writeTo(File file) {
        try {
            PrintStream out = new PrintStream(file);
            // Print heading
            printAsRow(columns.toArray(new String[0]), out);

            // Print data
            for (Map<String, T> row : data) {
                formatAndPrint(row, out);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    private void formatAndPrint(Map<String, T> data, PrintStream out) {
        String[] row = new String[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            Object d = data.getOrDefault(columns.get(i), null);
            if(d == null)
                row[i] = "";
            else
                row[i] = d.toString();

        }
        printAsRow(row, out);
    }

    private void printAsRow(String[] row, PrintStream out) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < row.length; i++) {
            line.append(row[i]);
            if (i + 1 < row.length)
                line.append(",");
        }
        out.println(line);
    }

}
