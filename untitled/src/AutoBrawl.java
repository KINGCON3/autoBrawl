import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class AutoBrawl {
    public static HashMap<Integer, String> ourList = new HashMap<>();
    public static HashMap<Integer, String> theirList = new HashMap<>();

    public static HashMap<Integer, Integer> enemyOrder = new HashMap<>();

    public static HashMap<Integer, Integer> enemyIndex = new HashMap<>();

    public static ArrayList<Integer> finalPositions = new ArrayList<>();

    public static ArrayList<Integer> initial = new ArrayList<>();

    public static ArrayList<String> extras = new ArrayList<>();

    public static int boundary;

    public static boolean all;

    public static void readFile(String file) {
        ourList.clear();
        theirList.clear();
        enemyOrder.clear();
        enemyIndex.clear();
        finalPositions.clear();
        initial.clear();
        extras.clear();

        try {
            //Create a buffered reader from a file reader using the parameter file as input
            BufferedReader bReader = new BufferedReader(new FileReader(file));
            String current;

            //Read until end of file one line at a time
            while ((current = bReader.readLine()) != null) {
                //0 = ourName, 1 = ourPower, 2 = theirPower, 3 = theirName
                String[] components = current.split("\t");

                if (components[0].equals("!") || components[1].equals("!")) {
                    extras.add(current);
                } else if (components[2].equals("!") || components[3].equals("!")) {
                    if (ourList.containsKey(Integer.parseInt(components[1]))) {
                        throw new RuntimeException("dupe detected from our side: " + " " + components[0] + " " + Integer.parseInt(components[1]));
                    }
                    ourList.put(Integer.parseInt(components[1]), components[0]);
                } else {
                    if (ourList.containsKey(Integer.parseInt(components[1]))) {
                        throw new RuntimeException("dupe detected from our side: " + components[0] + " " + Integer.parseInt(components[1]));
                    }
                    ourList.put(Integer.parseInt(components[1]), components[0]);
                    if (!components[2].startsWith("?") && !components[3].startsWith("?")) {
                        if (theirList.containsKey(Integer.parseInt(components[2]))) {
                            throw new RuntimeException("dupe detected from their side: " + components[3] + " " + Integer.parseInt(components[2]));
                        }
                        theirList.put(Integer.parseInt(components[2]), components[3]);
                        enemyOrder.put(Integer.parseInt(components[2]), enemyOrder.size());
                        enemyIndex.put(enemyIndex.size(), Integer.parseInt(components[2]));
                    }
                }
            }
            bReader.close();
        } catch (IOException e) {
            System.out.println("Failed to read file");
        }
    }

    public static void doMatchup(int threshold) {
        ArrayList<Integer> ourValues = new ArrayList<>(ourList.keySet());
        ArrayList<Integer> theirValues = new ArrayList<>(theirList.keySet());

        if (ourValues.size() < theirValues.size()) {
            boundary = ourValues.size();
        } else {
            boundary = theirValues.size();
        }

        while (finalPositions.size() < boundary) {
            finalPositions.add(99);
        }

        //Set our tops at the three or nine lowest
        int mins;
        if (all) {
            mins = 9;
        } else {
            mins = 3;
        }

        for (int i = 0; i < mins; i++) {
            int currentMin = ourValues.stream().min(Comparator.naturalOrder()).get();
            initial.add(currentMin);
            ourValues.remove(Integer.valueOf(currentMin));
        }


        theirValues.sort(Comparator.reverseOrder());

        //int counter = 0;
        for (int p : theirValues) {
            //System.out.println(p);
            if (!ourValues.isEmpty() && enemyOrder.get(p) < boundary) {
                int currentMax = ourValues.stream().max(Comparator.naturalOrder()).get();
                int currentMin = ourValues.stream().min(Comparator.naturalOrder()).get();
                if (currentMax - threshold >= p) {
                    finalPositions.set(enemyOrder.get(p), currentMax);
                    ourValues.remove(Integer.valueOf(currentMax));
                } else {
                    finalPositions.set(enemyOrder.get(p), currentMin);
                    ourValues.remove(Integer.valueOf(currentMin));
                }
            }
        }
    }

    public static int writeToFile(String output, int threshold) {
        try {
            int wins = 0, losses = 0;
            //Delete current output file
            File file = new File(output);
            if (file.exists()) {
                file.delete();
            }

            FileWriter writer = new FileWriter(output, false);
            for (int i = 0; i < initial.size(); i++) {
                writer.write(ourList.get(initial.get(i)) + "\t" + initial.get(i) + "\t" + "?" + "\t" + "?" + "\n");
                ourList.remove(initial.get(i));
            }

            for (int i = 0; i < finalPositions.size(); i++) {
                if (finalPositions.get(i) > enemyIndex.get(i)) {
                    wins += 1;
                } else {
                    losses += 1;
                }
                writer.write(ourList.get(finalPositions.get(i)) + "\t" + finalPositions.get(i) + "\t" + enemyIndex.get(i) + "\t" + theirList.get(enemyIndex.get(i)) + "\n");
                ourList.remove(finalPositions.get(i));
            }

            for (String line : extras) {
                writer.write(line + "\n");
            }

            while (!ourList.isEmpty()) {
                ArrayList<Integer> ourValues = new ArrayList<>(ourList.keySet());
                int currentMin = ourValues.stream().min(Comparator.naturalOrder()).get();
                writer.write(ourList.get(currentMin) + "\t" + currentMin + "\t" + "!" + "\t" + "!" + "\n");
                ourList.remove(currentMin);
            }

            writer.close();
            //System.out.println("Power Threshold: " + threshold + " " + "wins: " + wins + " " + "losses: " + losses);
            return losses;
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
        }
        return 100;
    }

    public static void tryAll() {
        int bestLosses = 100;
        int currentLosses = 0;
        int finalThreshold = 0;
        for (int i = 20000; i > 0; i--) {
            readFile("input.txt");
            doMatchup(i);
            currentLosses = writeToFile("output.txt", i);
            if (currentLosses < bestLosses) {
                bestLosses = currentLosses;
                finalThreshold = i;
                System.out.println("Power Threshold: " + i + " " + "losses: " + bestLosses);
            }
        }
        readFile("input.txt");
        doMatchup(finalThreshold);
        writeToFile("output.txt", finalThreshold);
    }

    public static void trySingle(int threshold) {
        readFile("input.txt");
        doMatchup(threshold);
        writeToFile("output.txt", threshold);
    }

    public static void main(String[] args) {
        all = true;
        //tryAll();
        trySingle(5882);
    }
}
