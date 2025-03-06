import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.Arrays;

public class Main extends Frame {
    private int[] array;
    private int arraySize = 20;
    private int delay = 100;
    private boolean isSorting = false;
    private boolean isPaused = false; // Pause state
    private final Object pauseLock = new Object(); // Lock for pause/resume
    private int currentIndex = -1, minIndex = -1, comparingIndex = -1;

    public Main() {
        setTitle("Selection Sort Visualizer");
        setSize(800, 600);
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        generateRandomArray();

        // Buttons
        Button startButton = new Button("Start Sorting");
        startButton.addActionListener(e -> {
            if (!isSorting) {
                isSorting = true;
                new Thread(this::selectionSort).start();
            }
        });

        Button pauseButton = new Button("Pause/Resume");
        pauseButton.addActionListener(e -> {
            isPaused = !isPaused;
            synchronized (pauseLock) {
                if (!isPaused) {
                    pauseLock.notify();
                }
            }
        });

        Button sizeButton = new Button("Set Size");
        sizeButton.addActionListener(e -> {
            String input = getInput("Enter array size (1-100):");
            if (input != null) {
                try {
                    int size = Integer.parseInt(input);
                    if (size > 0 && size <= 100) {
                        arraySize = size;
                        generateRandomArray();
                        repaint();
                    } else {
                        showMessage("Size must be between 1 and 100.");
                    }
                } catch (NumberFormatException ex) {
                    showMessage("Invalid input. Please enter a number.");
                }
            }
        });

        Button speedButton = new Button("Set Speed");
        speedButton.addActionListener(e -> {
            String input = getInput("Enter delay in milliseconds (1-1000):");
            if (input != null) {
                try {
                    int speed = Integer.parseInt(input);
                    if (speed > 0 && speed <= 1000) {
                        delay = speed;
                    } else {
                        showMessage("Speed must be between 1 and 1000.");
                    }
                } catch (NumberFormatException ex) {
                    showMessage("Invalid input. Please enter a number.");
                }
            }
        });

        Panel controlPanel = new Panel();
        controlPanel.add(startButton);
        controlPanel.add(pauseButton);
        controlPanel.add(sizeButton);
        controlPanel.add(speedButton);

        add(controlPanel, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });
    }

    private void generateRandomArray() {
        array = new int[arraySize];
        Random rand = new Random();
        for (int i = 0; i < arraySize; i++) {
            array[i] = rand.nextInt(500) + 50; // Ensures value is between 1 and 500
            // System.out.println("Generated: " + array[i]); // Debugging print
        }
        // System.out.println("Before Sorting: " + Arrays.toString(array)); // Print array before sorting
        currentIndex = -1;
        minIndex = -1;
        comparingIndex = -1;
    }

    private void selectionSort() {
        for (int i = 0; i < array.length - 1; i++) {
            minIndex = i;
            currentIndex = i;

            for (int j = i + 1; j < array.length; j++) {
                comparingIndex = j;
                repaint();
                sleep(delay);

                if (array[j] < array[minIndex]) {
                    minIndex = j;
                }

                // Pause handling
                synchronized (pauseLock) {
                    while (isPaused) {
                        try {
                            pauseLock.wait(); // Wait until resumed
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            // Swap the found minimum element with the first element
            int temp = array[minIndex];
            array[minIndex] = array[i];
            array[i] = temp;

            repaint();
            sleep(delay);
        }

        isSorting = false;
        currentIndex = -1;
        minIndex = -1;
        comparingIndex = -1;
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        int width = getWidth() / array.length;
        int heightFactor = (getHeight() - 50) / 500;

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        for (int i = 0; i < array.length; i++) {
            int height = array[i] * heightFactor;

            if (i == minIndex) {
                g.setColor(Color.RED); // Smallest element found
            } else if (i == comparingIndex) {
                g.setColor(Color.YELLOW); // Current element being compared
            } else {
                g.setColor(Color.GREEN); // Normal bars
            }

            g.fillRect(i * width, getHeight() - height, width - 2, height);
        }
    }

    private String getInput(String message) {
        Dialog inputDialog = new Dialog(this, "Input", true);
        inputDialog.setLayout(new FlowLayout());
        inputDialog.setSize(300, 120);

        Label label = new Label(message);
        TextField textField = new TextField(10);
        Button okButton = new Button("OK");

        final String[] result = {null};

        okButton.addActionListener(e -> {
            result[0] = textField.getText();
            inputDialog.dispose();
        });

        inputDialog.add(label);
        inputDialog.add(textField);
        inputDialog.add(okButton);
        inputDialog.setVisible(true);

        return result[0];
    }

    private void showMessage(String message) {
        Dialog dialog = new Dialog(this, "Message", true);
        dialog.setLayout(new FlowLayout());
        dialog.add(new Label(message));
        Button okButton = new Button("OK");
        okButton.addActionListener(e -> dialog.dispose());
        dialog.add(okButton);
        dialog.setSize(300, 100);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        Main visualizer = new Main();
        visualizer.setVisible(true);
    }
}
