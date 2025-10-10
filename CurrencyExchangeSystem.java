import java.util.*;
import java.net.*;
import java.io.*;

public class CurrencyExchangeSystem {
    
    // API Configuration
    private static final String API_URL = "https://api.frankfurter.app/latest";
    private static final double EPSILON = 1e-9; // For floating-point precision
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Currency Exchange Analyzer with Real-Time Data\n");
        System.out.println("Choose mode:");
        System.out.println("1. Run with real-time data");
        System.out.println("2. Manual input");
        System.out.println("3. Run tests");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        
        String[] currencies = null;
        double[][] exchangeRates = null;
        
        if (choice == 1) {
            // Real-time data
            currencies = new String[]{"USD", "EUR", "JPY", "GBP", "AUD", "CAD", "CHF", "CNY", "NZD", "SGD"};
            System.out.println("Fetching real-time exchange rates...");
            exchangeRates = fetchRealTimeRates(currencies);
            if (exchangeRates == null) {
                System.out.println("Failed to fetch real-time data. Check your internet connection");
                System.exit(1);
            }
        } else if (choice == 2) {
            // Manual input
            System.out.println("Enter input in the format: n, CUR1, CUR2, ..., CURn");
            System.out.println("Followed by n*n exchange rates, one per line.");
            Object[] input = readManualInput(scanner);
            currencies = (String[]) input[0];
            exchangeRates = (double[][]) input[1];
        } else if (choice == 3) {
            // Run tests
            testAssignmentExample(); // Arbitrage with 5 currencies
            testRandomizedRates(); // Random, may have arbitrage, 5 currencies
            testBetterIntermediate(); // No arbitrage but better intermediate path, 5 currencies
            testNoArbitrageCase(); // No arbitrage, consistent rates, 5 currencies
            testRealWorldRates(); // Real-world rates from API, 5 currencies
            System.exit(0);
        } else {
            System.out.println("Invalid choice.");
            System.exit(1);
        }

        
        // Task 1: Detect Arbitrage Opportunities
        System.out.println("\n TASK 1: ARBITRAGE DETECTION \n");
        boolean hasArbitrage = detectArbitrage(currencies, exchangeRates);
        
        // Task 2: Find Best Conversion Rate (only if no arbitrage)
        System.out.println("\nTASK 2: BEST CONVERSION RATE FINDER\n");
        
        if (hasArbitrage) {
            System.out.println("Warning: Arbitrage detected! Best conversion rate may be infinite by exploiting cycles.");
            System.out.println("Proceeding with calculation assuming no infinite profit loops.");
        }
        
        System.out.println("\nAvailable currencies: " + String.join(", ", currencies));
        System.out.print("\nEnter source currency: ");
        String source = scanner.nextLine().toUpperCase().trim();
        
        System.out.print("Enter target currency: ");
        String target = scanner.nextLine().toUpperCase().trim();
        
        if (Arrays.asList(currencies).contains(source) && Arrays.asList(currencies).contains(target)) {
            List<Integer> arbitrageCycle = new ArrayList<>();
            if (hasArbitrage) {
                arbitrageCycle = findArbitrageCycle(currencies, exchangeRates);
            }
            findBestConversionRate(currencies, exchangeRates, source, target, arbitrageCycle);
        } else {
            System.out.println("Error: Invalid currency code(s). Please use one of the available currencies.");
        }
        
        scanner.close();
    }
    
    /**
     * Reads manual input from scanner in the specified format.
     * @param scanner Scanner for input
     * @return Object[] {currencies, exchangeRates}
     */
    private static Object[] readManualInput(Scanner scanner) {
        String line = scanner.nextLine();
        String[] parts = line.split(",");
        int n = Integer.parseInt(parts[0].trim());
        String[] currencies = new String[n];
        for (int i = 0; i < n; i++) {
            currencies[i] = parts[i + 1].trim();
        }
        double[][] rates = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                rates[i][j] = scanner.nextDouble();
            }
        }
        scanner.nextLine(); // Consume if needed
        return new Object[]{currencies, rates};
    }
    
    /**
     * Test method for the example given in the assignment PDF (arbitrage case), expanded to 5 currencies.
     */
    private static void testAssignmentExample() {
        System.out.println("TEST: Assignment Example with Arbitrage (Expanded to 5 currencies)");
        String[] currencies = {"A", "B", "C", "D", "E"};
        double[][] exchangeRates = new double[5][5];
        // Original 3x3
        exchangeRates[0] = new double[]{1.0, 0.651, 0.581, 1.0, 1.0};
        exchangeRates[1] = new double[]{1.531, 1.0, 0.952, 1.0, 1.0};
        exchangeRates[2] = new double[]{1.711, 1.049, 1.0, 1.0, 1.0};
        exchangeRates[3] = new double[]{1.0, 1.0, 1.0, 1.0, 1.0};
        exchangeRates[4] = new double[]{1.0, 1.0, 1.0, 1.0, 1.0};
        detectArbitrage(currencies, exchangeRates);
    }
    
    /**
     * Test method for randomized exchange rates (may or may not have arbitrage), 5 currencies.
     */
    private static void testRandomizedRates() {
        System.out.println("\nTEST: Randomized Exchange Rates (5 currencies)");
        int n = 5;
        String[] currencies = new String[n];
        for (int i = 0; i < n; i++) {
            currencies[i] = "C" + (i + 1);
        }
        
        double[][] exchangeRates = generateRandomRates(n);
        detectArbitrage(currencies, exchangeRates);
        // Explanation for report: Randomized rates generated between 0.01 and 2.01 to simulate varied exchanges.
        // Diagonal set to 1.0. This may introduce arbitrage due to inconsistency.
    }
    
    /**
     * Test method for no arbitrage but better intermediate path, 5 currencies.
     */
    private static void testBetterIntermediate() {
        System.out.println("\nTEST: No Arbitrage but Better Intermediate Path (5 currencies)");
        String[] currencies = {"A", "B", "C", "D", "E"};
        double[][] exchangeRates = new double[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                exchangeRates[i][j] = (i == j ? 1.0 : 1.0); // Default 1
            }
        }
        // Triangle for better path
        int A = 0, B = 1, C = 2, D = 3, E = 4;
        exchangeRates[A][B] = 2.0; exchangeRates[B][A] = 0.4;
        exchangeRates[B][C] = 2.0; exchangeRates[C][B] = 0.4;
        exchangeRates[A][C] = 1.0; exchangeRates[C][A] = 0.25;
        // Adjust for D to avoid arb
        exchangeRates[A][D] = 1.0; exchangeRates[D][A] = 1.0;
        exchangeRates[B][D] = 0.5; exchangeRates[D][B] = 2.0;
        exchangeRates[C][D] = 0.25; exchangeRates[D][C] = 4.0;
        // Adjust for E similar
        exchangeRates[A][E] = 1.0; exchangeRates[E][A] = 1.0;
        exchangeRates[B][E] = 0.5; exchangeRates[E][B] = 2.0;
        exchangeRates[C][E] = 0.25; exchangeRates[E][C] = 4.0;
        exchangeRates[D][E] = 1.0; exchangeRates[E][D] = 1.0;
        boolean hasArbitrage = detectArbitrage(currencies, exchangeRates);
        if (!hasArbitrage) {
            // Test best conversion A to C, should use via B, rate 4 >1 direct
            List<Integer> arbitrageCycle = new ArrayList<>();
            findBestConversionRate(currencies, exchangeRates, "A", "C", arbitrageCycle);
        }
    }

    /**
 * TEST: No arbitrage case with consistent rates (5 currencies)
 * This demonstrates a market in equilibrium where no arbitrage exists.
 */
private static void testNoArbitrageCase() {
    System.out.println("\nTEST: No Arbitrage - Equilibrium Market (5 currencies)");
    String[] currencies = {"USD", "EUR", "GBP", "JPY", "AUD"};
    double[][] exchangeRates = new double[5][5];
    
    // Set up consistent rates: USD base rates
    double[] usdRates = {1.0, 0.85, 0.73, 110.0, 1.35}; // USD, EUR, GBP, JPY, AUD
    
    // Calculate consistent cross-rates to avoid arbitrage
    for (int i = 0; i < 5; i++) {
        for (int j = 0; j < 5; j++) {
            exchangeRates[i][j] = usdRates[j] / usdRates[i];
        }
    }
    
    System.out.println("Explanation: All rates calculated from USD base to ensure consistency");
    System.out.println("Formula: rate(i→j) = usdRate[j] / usdRate[i]");
    detectArbitrage(currencies, exchangeRates);
}

/**
 * TEST: Real-world rates from API (5 currencies minimum)
 * Fetches live data to test with actual market conditions.
 */
private static void testRealWorldRates() {
    System.out.println("\nTEST: Real-World Exchange Rates from API (5 currencies)");
    String[] currencies = {"USD", "EUR", "GBP", "JPY", "AUD"};
    System.out.println("Fetching real-time data from Frankfurter API...");
    
    double[][] exchangeRates = fetchRealTimeRates(currencies);
    if (exchangeRates != null) {
        System.out.println("\nExplanation: Real-world rates should not have arbitrage due to market efficiency");
        detectArbitrage(currencies, exchangeRates);
    } else {
        System.out.println("Note: API fetch failed. This test requires internet connection.");
        System.out.println("Using simulated real-world rates instead:");
        // Fallback to realistic rates
        testNoArbitrageCase();
    }
}
    
    /**
     * Generates a random n x n exchange rate matrix with positive rates.
     * Diagonal is 1.0, others random between 0.01 and 2.01.
     * @param n Number of currencies
     * @return Random exchange rate matrix
     */
    private static double[][] generateRandomRates(int n) {
        double[][] rates = new double[n][n];
        Random rand = new Random();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    rates[i][j] = 1.0;
                } else {
                    rates[i][j] = rand.nextDouble() * 2 + 0.01;  // Positive random rate
                }
            }
        }
        return rates;
    }
    
    /**
     * Fetches real-time exchange rates from Frankfurter API for specified currencies.
     * Uses USD as the base currency and calculates cross rates.
     * Manual JSON parsing without external libraries.
     * @param currencies Array of currency codes
     * @return 2D array of exchange rates, or null if fetch fails
     */
    public static double[][] fetchRealTimeRates(String[] currencies) {
        try {
            int n = currencies.length;
            double[][] rates = new double[n][n];
            
            // Fetch rates with USD as base
            URL url = new URL(API_URL + "?base=USD");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                String jsonResponse = response.toString();
                
                // Store USD rates for each currency
                double[] usdRates = new double[n];
                System.out.println("\nFetched Exchange Rates (base: USD):");
                
                for (int i = 0; i < n; i++) {
                    String currency = currencies[i];
                    if (currency.equals("USD")) {
                        usdRates[i] = 1.0;
                        System.out.printf("  USD -> %s: %.6f (base currency)\n", currency, usdRates[i]);
                    } else {
                        double rate = extractCurrencyRate(jsonResponse, currency);
                        if (rate > 0) {
                            usdRates[i] = rate;
                            System.out.printf("  USD -> %s: %.6f\n", currency, usdRates[i]);
                        } else {
                            System.out.println("Warning: " + currency + " not found in API response");
                            return null;
                        }
                    }
                }
                
                // Calculate cross rates: rate(i,j) = usdRates[j] / usdRates[i]
                
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        if (i == j) {
                            rates[i][j] = 1.0;
                        } else if (usdRates[i] > 0) {
                            rates[i][j] = usdRates[j] / usdRates[i];
                        } else {
                            rates[i][j] = 0.0;  // Error case
                        }
                    }
                }
                
                System.out.println("Real-time rates fetched successfully!");
                
                return rates;
            } else {
                System.out.println("Error: API returned status code " + responseCode);
                return null;
            }
        } catch (Exception e) {
            System.out.println("Exception during API fetch: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    
    /**
     * Extracts currency rate from JSON manually.
     * @param json JSON string
     * @param currency Currency code
     * @return Rate or -1 on error
     */
    private static double extractCurrencyRate(String json, String currency) {
        try {
            int ratesStart = json.indexOf("\"rates\":");
            if (ratesStart == -1) return -1;
            
            String searchKey = "\"" + currency + "\":";
            int startIndex = json.indexOf(searchKey, ratesStart);
            if (startIndex == -1) return -1;
            
            startIndex += searchKey.length();
            int endIndex = startIndex;
            while (endIndex < json.length()) {
                char c = json.charAt(endIndex);
                if (c == ',' || c == '}') break;
                endIndex++;
            }
            
            String rateStr = json.substring(startIndex, endIndex).trim();
            return Double.parseDouble(rateStr);
        } catch (Exception e) {
            System.out.println("Error parsing rate for " + currency + ": " + e.getMessage());
            return -1;
        }
    }

    
    /**
     * TASK 1: Detects arbitrage opportunities using Bellman-Ford algorithm.
     * Uses -log transformation to convert product >1 to negative sum.
     * Returns true if arbitrage detected, false otherwise.
     * @param currencies Currency codes
     * @param exchangeRates Exchange rates matrix
     * @return boolean indicating if arbitrage exists
     */
    public static boolean detectArbitrage(String[] currencies, double[][] exchangeRates) {
        long startTime = System.nanoTime();
        
        int n = currencies.length;
        
        System.out.println("\nStep 1: Transform exchange rates to negative logarithms");
        System.out.println("  Formula: w(u,v) = -log(r_uv)");
        System.out.println("  Reason: Product of rates > 1 <-> Sum of weights < 0");
        
        // Convert to negative logarithms, handle invalid rates
        double[][] logRates = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (exchangeRates[i][j] <= 0) {
                    System.out.println("Error: Invalid exchange rate <=0 at [" + i + "][" + j + "]");
                    return false;
                }
                logRates[i][j] = -Math.log(exchangeRates[i][j]);
            }
        }
        
        System.out.println("\nStep 2: Apply Bellman-Ford algorithm to detect negative cycles");
        System.out.println("  Starting from currency: " + currencies[0]);
        
        // Initialize distances and predecessors
        double[] distances = new double[n];
        int[] predecessors = new int[n];
        Arrays.fill(distances, Double.MAX_VALUE);
        Arrays.fill(predecessors, -1);
        distances[0] = 0;
        
        System.out.println("\nStep 3: Relax edges (n-1) times");
        
        // Relax n-1 times
        for (int k = 0; k < n - 1; k++) {
            boolean updated = false;
            for (int u = 0; u < n; u++) {
                for (int v = 0; v < n; v++) {
                    if (distances[u] != Double.MAX_VALUE && 
                        distances[u] + logRates[u][v] < distances[v] - EPSILON) {
                        distances[v] = distances[u] + logRates[u][v];
                        predecessors[v] = u;
                        updated = true;
                    }
                }
            }
            if (!updated) {
                System.out.println("  Iteration " + (k+1) + ": No updates (early termination)");
                break;
            } else {
                System.out.println("  Iteration " + (k+1) + ": Updates occurred");
            }
        }
        
        System.out.println("\nStep 4: Check for negative cycles (arbitrage) and perform nth relaxation if needed");
        
        // nth relaxation to detect and update for cycle reconstruction
        boolean hasNegativeCycle = false;
        int arbitrageStart = -1;
        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                if (distances[u] != Double.MAX_VALUE && 
                    distances[u] + logRates[u][v] < distances[v] - EPSILON) {
                    distances[v] = distances[u] + logRates[u][v];
                    predecessors[v] = u;
                    hasNegativeCycle = true;
                    arbitrageStart = v;  // Remember a vertex in the negative cycle path
                }
            }
        }
        
        if (hasNegativeCycle) {
            System.out.println("\nARBITRAGE OPPORTUNITY DETECTED!");
            if (arbitrageStart != -1) {
                printArbitrageCycle(predecessors, arbitrageStart, currencies, exchangeRates);
            } else {
                System.out.println("Arbitrage detected but cycle reconstruction failed.");
            }
        } else {
            System.out.println("\nNo arbitrage opportunities detected.");
            System.out.println("  (Market is in equilibrium - no negative cycles exist)");
        }
        
        double timeMs = (System.nanoTime() - startTime) / 1_000_000.0;
        System.out.println("\nExecution time for arbitrage detection: " + String.format("%.3f", timeMs) + " ms");
        
        return hasNegativeCycle;
    }
    
    /**
     * Reconstructs and prints the arbitrage cycle using predecessors.
     * Prints sequence v0, v1, ..., vk-1 as per spec.
     * @param predecessors Predecessor array
     * @param start Starting vertex for tracing
     * @param currencies Currency codes
     * @param exchangeRates Exchange rates matrix
     */
    private static void printArbitrageCycle(int[] predecessors, int start, 
                                           String[] currencies, double[][] exchangeRates) {
        // Trace back to find cycle
        Set<Integer> visited = new HashSet<>();
        int current = start;
        
        while (!visited.contains(current) && current != -1) {
            visited.add(current);
            current = predecessors[current];
        }
        
        if (current == -1) {
            System.out.println("Cycle reconstruction failed: No path found.");
            return;
        }
        
        // Build the cycle starting from the loop point
        int cycleStart = current;
        List<Integer> cycle = new ArrayList<>();
        do {
            cycle.add(current);
            current = predecessors[current];
        } while (current != cycleStart && current != -1);
        
        if (current == -1) {
            System.out.println("Cycle reconstruction failed: Incomplete cycle.");
            return;
        }
        
        // Do not add closing for spec: v0 to vk-1
        Collections.reverse(cycle);
        
        // Calculate product
        double productOfRates = 1.0;
        System.out.println("\nArbitrage Cycle Details:");
        System.out.println("-".repeat(60));
        
        for (int i = 0; i < cycle.size() - 1; i++) {
            int from = cycle.get(i);
            int to = cycle.get(i + 1);
            double rate = exchangeRates[from][to];
            productOfRates *= rate;
            
            System.out.printf("  %s -> %s: %.6f\n", 
                currencies[from], currencies[to], rate);
        }
        
        // Closing product with last to first
        int last = cycle.get(cycle.size() - 1);
        int first = cycle.get(0);
        double closingRate = exchangeRates[last][first];
        productOfRates *= closingRate;
        System.out.printf("  %s -> %s: %.6f\n", 
            currencies[last], currencies[first], closingRate);
        
        double profitPercentage = (productOfRates - 1.0) * 100;
        
        System.out.println("\nCycle Sequence (v0, v1, ..., vk-1): ");
        System.out.print("  ");
        for (int i = 0; i < cycle.size(); i++) {
            System.out.print(currencies[cycle.get(i)]);
            if (i < cycle.size() - 1) System.out.print(", ");
        }
        System.out.println();
        
        System.out.printf("\nProduct of exchange rates: %.6f\n", productOfRates);
        System.out.printf("Profit Percentage: %.2f%%\n", profitPercentage);
        
        if (profitPercentage > 0) {
            System.out.println("\nExample: Starting with 1000 " + currencies[cycle.get(0)] + 
                             " -> End with " + String.format("%.2f", 1000 * productOfRates) + 
                             " " + currencies[cycle.get(0)]);
        }
    }
    
    /**
     * TASK 2: Finds the best conversion rate from source to target.
     * Uses Bellman-Ford on -log graph to find path with maximum product.
     * @param currencies Currency codes
     * @param exchangeRates Exchange rates matrix
     * @param source Source currency
     * @param target Target currency
     */
    private static List<Integer> findArbitrageCycle(String[] currencies, double[][] exchangeRates) {
        int n = currencies.length;
        double[][] logRates = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                logRates[i][j] = -Math.log(exchangeRates[i][j]);
            }
        }
        
        double[] distances = new double[n];
        int[] predecessors = new int[n];
        Arrays.fill(distances, Double.MAX_VALUE);
        distances[0] = 0;
        
        int cycleStart = -1;
        for (int k = 0; k < n; k++) {
            cycleStart = -1;
            for (int u = 0; u < n; u++) {
                for (int v = 0; v < n; v++) {
                    if (distances[u] != Double.MAX_VALUE && 
                        distances[u] + logRates[u][v] < distances[v] - EPSILON) {
                        distances[v] = distances[u] + logRates[u][v];
                        predecessors[v] = u;
                        cycleStart = v;
                    }
                }
            }
        }
        
        if (cycleStart == -1) {
            return new ArrayList<>();
        }
        
        List<Integer> cycle = new ArrayList<>();
        boolean[] visited = new boolean[n];
        int current = cycleStart;
        while (!visited[current]) {
            visited[current] = true;
            cycle.add(current);
            current = predecessors[current];
        }
        
        int startIndex = cycle.indexOf(current);
        return new ArrayList<>(cycle.subList(startIndex, cycle.size()));
    }
    
    public static void findBestConversionRate(String[] currencies, double[][] exchangeRates, 
                                             String source, String target, List<Integer> arbitrageCycle) {
        
        int n = currencies.length;
        int sourceIndex = -1, targetIndex = -1;
        
        // Find indices
        for (int i = 0; i < n; i++) {
            if (currencies[i].equals(source)) sourceIndex = i;
            if (currencies[i].equals(target)) targetIndex = i;
        }
        
        if (sourceIndex == -1 || targetIndex == -1) {
            System.out.println("Error: Currency not found.");
            return;
        }
        
        System.out.println("\nStep 1: Transform rates to negative logarithms");
        
        // Convert to negative logarithms
        double[][] logRates = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (exchangeRates[i][j] <= 0) {
                    System.out.println("Error: Invalid rate <=0.");
                    return;
                }
                logRates[i][j] = -Math.log(exchangeRates[i][j]);
            }
        }
        
        System.out.println("Step 2: Initialize Bellman-Ford from source: " + source);
        
        // Bellman-Ford
        double[] distances = new double[n];
        int[] predecessors = new int[n];
        Arrays.fill(distances, Double.MAX_VALUE);
        Arrays.fill(predecessors, -1);
        distances[sourceIndex] = 0;
        
        System.out.println("Step 3: Relax edges to find shortest path");
        
        for (int k = 0; k < n - 1; k++) {
            boolean updated = false;
            for (int u = 0; u < n; u++) {
                for (int v = 0; v < n; v++) {
                    if (distances[u] != Double.MAX_VALUE && 
                        distances[u] + logRates[u][v] < distances[v] - EPSILON) {
                        distances[v] = distances[u] + logRates[u][v];
                        predecessors[v] = u;
                        updated = true;
                    }
                }
            }
            if (!updated) {
                System.out.println("  Iteration " + (k+1) + ": No updates");
                break;
            } else {
                System.out.println("  Iteration " + (k+1) + ": Updates occurred");
            }
        }
        
        // Find the basic path first, with cycle detection
        List<Integer> path = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        int current = targetIndex;
        
        while (current != -1 && !visited.contains(current)) {
            path.add(current);
            visited.add(current);
            current = predecessors[current];
        }
        
        if (current == -1) {
            if (path.get(path.size() - 1) != sourceIndex) {
                System.out.println("Path reconstruction failed: Did not reach source currency.");
                return;
            }
        } else {
            // We found a cycle
            System.out.println("\nWarning: Cycle detected in path reconstruction!");
            System.out.println("This indicates an arbitrage opportunity that affects the optimal path.");
            System.out.println("Will show the basic path ignoring potential infinite improvements.");
            
            // Truncate the path at the cycle point
            int cycleIndex = path.indexOf(current);
            path = new ArrayList<>(path.subList(0, cycleIndex + 1));
        }
        
        Collections.reverse(path);
        
        // Calculate and display the basic conversion rate
        double totalRate = 1.0;
        System.out.println("\nBasic Conversion Path:");
        System.out.println("-".repeat(60));
        
        for (int i = 0; i < path.size() - 1; i++) {
            int from = path.get(i);
            int to = path.get(i + 1);
            double rate = exchangeRates[from][to];
            totalRate *= rate;
            System.out.printf("  %s -> %s: %.6f\n", 
                currencies[from], currencies[to], rate);
        }
        
        System.out.printf("\nBasic Conversion Rate: %.6f\n", totalRate);
        
        // If there's an arbitrage cycle, show how it can be exploited
        if (!arbitrageCycle.isEmpty()) {
            System.out.println("\nArbitrage Opportunity Found!");
            System.out.println("The rate can be improved using this cycle:");
            System.out.println("-".repeat(60));
            
            double cycleRate = 1.0;
            for (int i = 0; i < arbitrageCycle.size(); i++) {
                int from = arbitrageCycle.get(i);
                int to = arbitrageCycle.get((i + 1) % arbitrageCycle.size());
                cycleRate *= exchangeRates[from][to];
                System.out.printf("  %s -> %s: %.6f\n", 
                    currencies[from], currencies[to], exchangeRates[from][to]);
            }
            
            System.out.printf("\nCycle Multiplication Factor: %.6f\n", cycleRate);
            
            // Show example with starting amount and improvements
            double startAmount = 1000;
            System.out.printf("\nExample starting with %.2f %s:\n", startAmount, source);
            System.out.printf("1. Basic conversion: %.2f %s\n", startAmount * totalRate, target);
            
            double improvedRate = totalRate * cycleRate;
            System.out.printf("2. With one arbitrage cycle: %.2f %s\n", startAmount * improvedRate, target);
            System.out.printf("3. With two arbitrage cycles: %.2f %s\n", startAmount * improvedRate * cycleRate, target);
            System.out.println("\nEach additional cycle will multiply the rate by: " + String.format("%.6f", cycleRate));
        }
        
        // Compare with direct conversion if no arbitrage
        if (arbitrageCycle.isEmpty()) {
            double directRate = exchangeRates[sourceIndex][targetIndex];
            System.out.printf("\nDirect rate (%s -> %s): %.6f\n", source, target, directRate);
            
            if (Math.abs(totalRate - directRate) < EPSILON) {
                System.out.println("Direct exchange is optimal!");
            } else if (totalRate > directRate + EPSILON) {
                double improvement = ((totalRate / directRate) - 1.0) * 100;
                System.out.printf("Multi-step exchange is %.2f%% better!\n", improvement);
            }
            
            // Example with optimal rate
            System.out.println("\nExample:");
            System.out.printf("  1000 %s -> %.2f %s\n", source, 1000 * totalRate, target);
        }
    }
}