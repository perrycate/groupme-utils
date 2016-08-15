package me.perrycate.groupmeutils;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

/**
 * Displays a simple progress bar in a specified output stream.
 * 
 * NOTE: Assumes that nothing else is writing to the output stream at the same
 * time.
 * 
 * No guarantees are made that the progress bar is 100% accurate, although it
 * should be close enough for display purposes.
 */
public class ProgressBar {

    private static String BAR_FORMAT = "[%s] %3d%%\r";
    // number of characters in the line such as [], %, etc.
    private static int NON_BAR_CHARACTERS = 7;
    // fills in "complete" side of bar
    private static char BAR_COMPLETE_CHAR = '#';
    // fills in "incomplete" side of bar
    private static char BAR_INCOMPLETE_CHAR = '-';

    private PrintStream output;
    // length of the progress bar, not counting anything else in the line like
    // [], %, etc.
    private int barWidth;
    // Number of times that update() should be called before reaching 100%
    private double maxValue;

    // currentValue / maxValue = progress
    private double currentValue;
    // int between 0 and barWidth indicating how close to being done we are
    private int progress;

    // When true, will throw an exception if update() is called after progress
    // has already reached 100. If false, update() will simply do nothing.
    private boolean strictMode = false;

    /**
     * Create and print an empty progress bar of width barWidth to output.
     * maxValue is the number of times that update() must be called before
     * the progress bar reaches 100%
     */
    public ProgressBar(OutputStream output, int lineWidth, int maxValue) {

        this.output = new PrintStream(output);
        this.maxValue = maxValue;
        this.currentValue = 0;
        this.progress = 0;
        this.barWidth = lineWidth - NON_BAR_CHARACTERS;

        draw();

    }

    /**
     * Updates the progressBar, redrawing if necessary 
     */
    public void update() {

        // If we're already complete, don't bother proceeding.
        if (progress == barWidth) {
            if (strictMode) {
                throw new RuntimeException(
                        "Unecessary update() to progress bar,"
                                + " progress is already at 100%");
            } else {
                return;
            }
        }

        currentValue++;

        // redraw if necessary
        if ((currentValue / maxValue) * barWidth > progress) {
            progress = (int) ((currentValue / maxValue) * barWidth);
            draw();
        }

        // if we just reached 100%, print a newline so that future output
        // doesn't erase the bar
        if (progress == barWidth) {
            output.println();
        }

    }

    private void draw() {

        // Generate string to fill bar
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < progress; i++) {
            bar.append(BAR_COMPLETE_CHAR);
        }
        for (int i = progress; i < barWidth; i++) {
            bar.append(BAR_INCOMPLETE_CHAR);
        }

        int donePercentage = (int) ((double) progress / barWidth * 100.0);
        output.printf(BAR_FORMAT, bar.toString(), donePercentage);
    }

    /**
     * Will cause update() to throw an exception if it is called again after
     * progress has already reached 100%.
     * 
     * Potentially useful for debugging client applications. 
     */
    public void enableExceptionOnExcessUpdate() {
        strictMode = true;
    }

    public static void main(String[] args) {
        ProgressBar test = new ProgressBar(System.out, 70, 70);

        for (int i = 0; i < 70; i++) {
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            test.update();
        }
    }
}
