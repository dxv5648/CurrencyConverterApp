import java.util.*;

public class Currency {

    public static void main(String[] args) {
        System.out.println("Welcome to the Currency Converter App!");

        String[] currencies = {"USD", "EUR", "JPY", "NZD", "GBP"};
        double[][] exchangeRates = {
            {1.0, 0.85, 110.0, 1.4, 0.75},
            {1.18, 1.0, 129.53, 1.65, 0.88},
            {0.0091, 0.0077, 1.0, 0.0127, 0.0068},
            {0.71, 0.61, 78.74, 1.0, 0.53},
            {1.33, 1.14, 147.5, 1.89, 1.0}
        };

        // Task 1: Detect Arbitrage Opportunities
        detectArbitrage(currencies, exchangeRates);

        // Task 2: Find Best Conversion Rate
        findBestConversionRate(currencies, exchangeRates, "NZD", "GBP");
    }

    // Task 1: Detect Arbitrage Opportunities
    public static void detectArbitrage(String[] currencies, double[][] exchangeRates) {
        int n = currencies.length;

        // Convert exchange rates to negative logarithms
        double[][] logRates = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                logRates[i][j] = -Math.log(exchangeRates[i][j]);
            }
        }

        // Bellman-Ford algorithm to detect negative weight cycles
        double[] distances = new double[n];
        int[] predecessors = new int[n];
        Arrays.fill(distances, Double.MAX_VALUE);
        distances[0] = 0;

        for (int k = 0; k < n - 1; k++) {
            boolean updated = false; // Track if any update happens in this iteration

            for (int u = 0; u < n; u++) {
                for (int v = 0; v < n; v++) {
                    if (distances[u] != Double.MAX_VALUE && distances[u] + logRates[u][v] < distances[v]) {
                        distances[v] = distances[u] + logRates[u][v];
                        predecessors[v] = u;
                        updated = true; // Mark that an update occurred
                    }
                }
            }

            // If no update happens, the algorithm can terminate early
            if (!updated) {
                break;
            }
        }

        // Check for negative weight cycles
        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                if (distances[u] != Double.MAX_VALUE && distances[u] + logRates[u][v] < distances[v]) {
                    System.out.println("Arbitrage opportunity detected!");
                    printCycle(predecessors, v, currencies);
                    return;
                }
            }
        }

        System.out.println("No arbitrage opportunities detected.");
    }

    private static void printCycle(int[] predecessors, int start, String[] currencies) {
        List<Integer> cycle = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        int current = start;

        while (!visited.contains(current)) {
            visited.add(current);
            current = predecessors[current];
        }

        int cycleStart = current;
        do {
            cycle.add(current);
            current = predecessors[current];
        } while (current != cycleStart);

        cycle.add(cycleStart);
        Collections.reverse(cycle);

        System.out.print("Arbitrage cycle: ");
        for (int i = 0; i < cycle.size(); i++) {
            System.out.print(currencies[cycle.get(i)]);
            if (i < cycle.size() - 1) System.out.print(" -> ");
        }
        System.out.println();
    }







    // Task 2: Find Best Conversion Rate
    public static void findBestConversionRate(String[] currencies, double[][] exchangeRates, String source, String target) {
        System.out.println("Part 2");
        int n = currencies.length;
        int sourceIndex = Arrays.asList(currencies).indexOf(source);
        int targetIndex = Arrays.asList(currencies).indexOf(target);

        // Convert exchange rates to negative logarithms
        double[][] logRates = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                logRates[i][j] = -Math.log(exchangeRates[i][j]);
            }
        }

        // Bellman-Ford algorithm to find shortest path
        double[] distances = new double[n];
        int[] predecessors = new int[n];
        Arrays.fill(distances, Double.MAX_VALUE);
        Arrays.fill(predecessors, -1);
        distances[sourceIndex] = 0;

        // Initialize direct paths from source
        for (int i = 0; i < n; i++) {
            if (exchangeRates[sourceIndex][i] > 0) {
                distances[i] = -Math.log(exchangeRates[sourceIndex][i]);
                predecessors[i] = sourceIndex;
            }
        }

        for (int k = 0; k < n - 1; k++) {
            boolean updated = false; // Track if any update happens in this iteration

            for (int u = 0; u < n; u++) {
                for (int v = 0; v < n; v++) {
                    if (distances[u] != Double.MAX_VALUE && distances[u] + logRates[u][v] < distances[v]) {
                        distances[v] = distances[u] + logRates[u][v];
                        predecessors[v] = u;
                        updated = true; // Mark that an update occurred
                    }
                }
            }

            // If no update happens, the algorithm can terminate early
            if (!updated) {
                break;
            }
        }

        // Reconstruct the path
        if (distances[targetIndex] == Double.MAX_VALUE) {
            System.out.println("No path exists between " + source + " and " + target);
            return;
        }

        // Handle direct path case
        if (predecessors[targetIndex] == sourceIndex) {
            System.out.print("Best conversion path: " + source + " -> " + target);
            System.out.println();
            double bestRate = exchangeRates[sourceIndex][targetIndex];
            System.out.println("Best conversion rate from " + source + " to " + target + ": " + bestRate);
            return;
        }

        // Use a set to detect cycles during path reconstruction for indirect paths
        Set<Integer> visited = new HashSet<>();
        List<Integer> path = new ArrayList<>();
        int current = targetIndex;
        
        while (current != sourceIndex && current != -1) {
            if (visited.contains(current)) {
                // If we detect a cycle, try using the direct rate if it exists
                if (exchangeRates[sourceIndex][targetIndex] > 0) {
                    System.out.print("Best conversion path: " + source + " -> " + target);
                    System.out.println();
                    double bestRate = exchangeRates[sourceIndex][targetIndex];
                    System.out.println("Best conversion rate from " + source + " to " + target + ": " + bestRate);
                    return;
                }
                System.out.println("Error: No valid path found between " + source + " and " + target);
                return;
            }
            path.add(current);
            visited.add(current);
            current = predecessors[current];
        }
        
        if (current == -1) {
            System.out.println("Error: Invalid path - broken predecessor chain");
            return;
        }
        
        path.add(sourceIndex);
        Collections.reverse(path);

        // Validate the path before printing
        if (path.size() < 2) {
            System.out.println("Error: Invalid path length");
            return;
        }

        // Print the path and conversion rate
        System.out.print("Best conversion path: ");
        for (int i = 0; i < path.size(); i++) {
            if (i >= 0 && i < currencies.length) {
                System.out.print(currencies[path.get(i)]);
                if (i < path.size() - 1) System.out.print(" -> ");
            }
        }
        System.out.println();

        double bestRate = Math.exp(-distances[targetIndex]);
        System.out.println("Best conversion rate from " + source + " to " + target + ": " + bestRate);
    }
}