import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.awt.*;

public class Main {
    private static JTextArea resultArea;
    private static JPanel threadPanels;
    static int total_words = 0;
    static int total_letters = 0;
    static int total_spaces = 0;
    static int total_vowels = 0;

    public static void main(String[] args) {
        StringBuilder content = new StringBuilder();
        String file_location = "C:\\Users\\pc\\Downloads\\file.txt";

        // Setup the JFrame
        JFrame frame = new JFrame("Text Processor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // Panel to hold the thread-specific panels
        threadPanels = new JPanel();
        threadPanels.setLayout(new BoxLayout(threadPanels, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(threadPanels);

        // Final results panel
        JPanel resultPanel = new JPanel();
        resultArea = new JTextArea(5, 40);
        resultArea.setEditable(false);
        resultPanel.add(new JScrollPane(resultArea));

        // Panel to hold the input field for the number of threads
        JPanel inputPanel = new JPanel();
        JLabel threadLabel = new JLabel("Enter the number of threads: ");
        JSpinner threadSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));  // Allows for selecting number of threads
        inputPanel.add(threadLabel);
        inputPanel.add(threadSpinner);

        // Button to start processing
        JButton processButton = new JButton("Start Processing");
        inputPanel.add(processButton);

        // Add panels to the frame
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(resultPanel, BorderLayout.SOUTH);
        frame.add(inputPanel, BorderLayout.NORTH);

        // Display the frame
        frame.setVisible(true);

        String text = read_file(file_location, content);

        processButton.addActionListener(e -> {
            int num_of_threads = (int) threadSpinner.getValue(); // Get the number of threads from the spinner

            String[] parts = split_text(text, num_of_threads);

            ExecutorService executor = Executors.newFixedThreadPool(num_of_threads);
            List<Future<int[]>> futures = new ArrayList<>();

            // Submit tasks for each part to process it in separate threads
            for (int i = 0; i < parts.length; i++) {
                final String part = parts[i];
                final int threadIndex = i;
                SwingUtilities.invokeLater(() -> {
                    // Create a panel for each thread
                    JPanel threadPanel = new JPanel();
                    threadPanel.setBorder(BorderFactory.createTitledBorder("Thread " + (threadIndex + 1)));
                    JTextArea textArea = new JTextArea(10, 40);
                    textArea.setEditable(false);
                    textArea.setText(part);
                    threadPanel.add(new JScrollPane(textArea));
                    threadPanels.add(threadPanel);
                    frame.revalidate();
                    frame.repaint();
                });

                Future<int[]> future = executor.submit(() -> process_text(part));
                futures.add(future);
            }
            try {
                for (Future<int[]> future : futures) {
                    int[] result = future.get();
                    total_words += result[0];
                    total_letters += result[1];
                    total_spaces += result[2];
                    total_vowels += result[3];
                }
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
            } finally {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException ex) {
                    executor.shutdownNow();
                }
            }

            // Display results in the result area
            SwingUtilities.invokeLater(() -> {
                resultArea.setText(String.format("Total Words: %d\nTotal Letters: %d\nTotal Spaces: %d\nTotal Vowels: %d",
                        total_words, total_letters, (total_spaces + num_of_threads - (num_of_threads > 2 ? 2 : 1)), total_vowels));
            });
        });
    }

    public static String read_file(String file_name, StringBuilder content) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file_name));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return content.toString();
    }

    public static String[] split_text(String text, int num_of_chunks) {
        String[] words = text.split("\\s+");
        int num_of_words = words.length;

        int words_per_chunk = num_of_words / num_of_chunks;
        int remainder = num_of_words % num_of_chunks;

        List<String> result = new ArrayList<>();
        StringBuilder current_chunk = new StringBuilder();
        int word_counter = 0;

        for (int i = 0; i < num_of_words; i++) {
            current_chunk.append(words[i]).append(" ");
            word_counter++;

            if (word_counter == words_per_chunk) { //if chunk is finished
                result.add(current_chunk.toString().trim());
                current_chunk.setLength(0);
                word_counter = 0;
            }
        }

        if (!current_chunk.toString().isEmpty() && (result.size() != 0)) {
            int size = result.size();
            result.set(size - 1, result.get(size - 1) + " " + current_chunk);
        }

        return result.toArray(new String[0]);
    }

    public static int[] process_text(String text) {
        int words;
        int letters = 0;
        int spaces = 0;
        int vowels = 0;

        String[] word_array = text.split("\\s+");
        words = word_array.length;
        System.out.println(Thread.activeCount());

        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                letters++;
                if ("AEIOUaeiou".indexOf(c) != -1) {
                    vowels++;
                }
            } else if (c == ' ') {
                spaces++;
            }
        }
        return new int[]{words, letters, spaces, vowels};
    }
}








/*
//Code Without GUI
import java.io.BufferedReader;
        import java.io.FileReader;
        import java.io.IOException;
        import java.util.Scanner;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        StringBuilder content = new StringBuilder();
        int total_words = 0;
        int total_letters = 0;
        int total_spaces = 0;
        int total_vowels = 0;
        String file_location = "C:\\Users\\pc\\Downloads\\file.txt";

        //Scanner scanner = new Scanner(System.in);
        //System.out.println("Enter File Location:");
        //String file_location = scanner.nextLine();
        String text = read_file(file_location, content);

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the number of threads: ");
        int num_of_threads = scanner.nextInt();

        String[] parts = split_text(text, num_of_threads);

        ExecutorService executor = Executors.newFixedThreadPool(num_of_threads);
        List<Future<int[]>> futures = new ArrayList<>();

        for (String part : parts) {
            Future<int[]> future = executor.submit(() -> process_text(part));
            futures.add(future);
        }
        try {
            for (Future<int[]> future : futures) {
                int[] result = future.get();
                total_words += result[0];
                total_letters += result[1];
                total_spaces += result[2];
                total_vowels += result[3];
            }
        } catch (InterruptedException | ExecutionException e) { e.printStackTrace();
        } finally { executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) { executor.shutdownNow();}
        }

        System.out.println("Total Words: " + total_words);
        System.out.println("Total Letters: " + total_letters);
        System.out.println("Total Spaces: " + (total_spaces + num_of_threads - 2)); // Add the number of threads as space
        System.out.println("Total Vowels: " + total_vowels);
    }
    public static String read_file(String file_name, StringBuilder content){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file_name));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) { e.printStackTrace(); }
        finally { try { if (reader != null) { reader.close(); } }
        catch (IOException e) { e.printStackTrace(); } }

        return content.toString();
    }

    public static String[] split_text(String text, int num_of_chunks){
        String[] words = text.split("\\s+");
        int num_of_words = words.length;

        int words_per_chunk = num_of_words / num_of_chunks;
        //int remainder = num_of_words % num_of_chunks;

        List<String> result = new ArrayList<>();
        StringBuilder current_chunk = new StringBuilder();
        int word_counter = 0;


        for(int i = 0 ; i< num_of_words ; i++){
            current_chunk.append(words[i]).append(" ");
            word_counter++;

            if(word_counter == words_per_chunk){ //if chunk is finished
                result.add(current_chunk.toString().trim());
                current_chunk.setLength(0);
                word_counter = 0;
            }
        }

        if (!current_chunk.toString().isEmpty() && (!result.isEmpty())){
            int size = result.size();
            result.set(size - 1, result.get(size - 1) + " " + current_chunk);
        }

        return result.toArray(new String[0]);
    }

    public static int[] process_text(String text){
        int words;
        int letters = 0;
        int spaces = 0;
        int vowels = 0;

        String[] word_array = text.split("\\s+");
        words = word_array.length;
        System.out.println(Thread.activeCount());

        for(char c : text.toCharArray()){
            if(Character.isLetter(c)){
                letters++;
                if("AEIOUaeiou".indexOf(c) != -1){
                    vowels++;
                }
            }
            else if (c == ' '){
                spaces++;
            }
        }
        return new int[] {words, letters, spaces, vowels };
    }
}
