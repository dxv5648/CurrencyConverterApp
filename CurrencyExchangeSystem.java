import java.util.*;
import java.net.*;
import java.io.*;

public class CurrencyExchangeSystem {
    
    // API Configuration
    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/";
    private static final double EPSILON = 1e-9; // For floating-point precision
    
    public static void main(String[] args) {
        System.out.println("Currency Exchange Analyzer with Real-Time Data\n");
        
        // Test with assignment example (arbitrage case)
        System.out.println("TEST: Assignment Example with Arbitrage");
        testAssignmentExample();
        
        // Test with randomized rates
        System.out.println("\nTEST: Randomized Exchange Rates");
        testRandomizedRates();
        
        // Main analysis with real-time data
        System.out.println("\nMain Analysis with Real-Time Data");
        
        // Define currencies to analyze (10 major world currencies)
        String[] currencies = {"USD", "EUR", "JPY", "GBP", "AUD", "CAD", "CHF", "CNY", "NZD", "SGD"};
        
        System.out.println("Fetching real-time exchange rates...");
        double[][] exchangeRates = fetchRealTimeRates(currencies);
        
        if (exchangeRates == null) {
            System.out.println("Failed to fetch real-time data.");
            System.exit(1);
        }
        
        // Display the exchange rate matrix
        displayExchangeRates(currencies, exchangeRates);
        
        // Task 1: Detect Arbitrage Opportunities
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TASK 1: ARBITRAGE DETECTION");
        System.out.println("=".repeat(60));
        boolean hasArbitrage = detectArbitrage(currencies, exchangeRates);
        
        // Task 2: Find Best Conversion Rate (only if no arbitrage)
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TASK 2: BEST CONVERSION RATE FINDER");
        System.out.println("=".repeat(60));
        
        if (hasArbitrage) {
            System.out.println("Warning: Arbitrage detected! Best conversion rate may be infinite by exploiting cycles.");
            System.out.println("Proceeding with calculation assuming no infinite profit loops.");
        }
        
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\nAvailable currencies: " + String.join(", ", currencies));
        System.out.print("\nEnter source currency: ");
        String source = scanner.nextLine().toUpperCase().trim();
        
        System.out.print("Enter target currency: ");
        String target = scanner.nextLine().toUpperCase().trim();
        
        if (Arrays.asList(currencies).contains(source) && Arrays.asList(currencies).contains(target)) {
            findBestConversionRate(currencies, exchangeRates, source, target);
        } else {
            System.out.println("Error: Invalid currency code(s). Please use one of the available currencies.");
        }
        
        scanner.close();
    }
    
    /**
     * Test method for the example given in the assignment PDF (arbitrage case)
     */
    private static void testAssignmentExample() {
        String[] currencies = {"A", "B", "C"};
        double[][] exchangeRates = {
            {1.0, 0.651, 0.584},  // A to A, A to B, A to C (1/rCA ≈0.584)
            {1.536, 1.0, 0.952},  // B to A (1/rAB ≈1.536), B to B, B to C
            {1.711, 1.050, 1.0}   // C to A, C to B (1/rBC ≈1.050), C to C
        };
        
        displayExchangeRates(currencies, exchangeRates);
        detectArbitrage(currencies, exchangeRates);
    }
    
    /**
     * Test method for randomized exchange rates (may or may not have arbitrage)
     */
    private static void testRandomizedRates() {
        int n = 5;  // Small n for demonstration
        String[] currencies = new String[n];
        for (int i = 0; i < n; i++) {
            currencies[i] = "C" + (i + 1);
        }
        
        double[][] exchangeRates = generateRandomRates(n);
        displayExchangeRates(currencies, exchangeRates);
        detectArbitrage(currencies, exchangeRates);
        
        // Explanation for report: Randomized rates generated between 0.01 and 2.01 to simulate varied exchanges.
        // Diagonal set to 1.0. This may introduce arbitrage due to inconsistency.
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
     * Fetches real-time exchange rates from API for specified currencies.
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
            System.out.println("API Request: " + API_URL + "USD");
            URL url = new URL(API_URL + "USD");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            int responseCode = conn.getResponseCode();
            System.out.println("API Response Code: " + responseCode);
            
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                String jsonResponse = response.toString();
                
                // Manual JSON parsing
                System.out.println("\nParsing API Response...");
                System.out.println("Raw JSON (first 200 chars): " + 
                    jsonResponse.substring(0, Math.min(200, jsonResponse.length())) + "...\n");
                
                // Extract date
                String date = extractJsonValue(jsonResponse, "date");
                System.out.println("Data Date: " + date);
                
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
                System.out.println("\nCalculating cross-rates...");
                System.out.println("Formula: rate(i→j) = usdRate[j] / usdRate[i]");
                
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
                
                System.out.println("✓ Real-time rates fetched successfully!");
                System.out.println("  Timestamp: " + date);
                
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
     * Simple JSON value extractor for string values.
     * @param json JSON string
     * @param key Key to extract
     * @return Value or "Unknown"
     */
    private static String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) return "Unknown";
        
        startIndex += searchKey.length();
        int endIndex = json.indexOf("\"", startIndex);
        
        if (endIndex == -1) return "Unknown";
        return json.substring(startIndex, endIndex);
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
     * Displays the exchange rate matrix in a formatted table.
     * @param currencies Currency codes
     * @param rates Exchange rates matrix
     */
    private static void displayExchangeRates(String[] currencies, double[][] rates) {
        int n = currencies.length;
        System.out.println("\n" + "=".repeat(60));
        System.out.println("EXCHANGE RATE MATRIX");
        System.out.println("=".repeat(60));
        
        // Header
        System.out.print("FROM\\TO  ");
        for (String currency : currencies) {
            System.out.printf("%-12s", currency);
        }
        System.out.println();
        System.out.println("-".repeat(10 + 12 * n));
        
        // Rows
        for (int i = 0; i < n; i++) {
            System.out.printf("%-9s", currencies[i]);
            for (int j = 0; j < n; j++) {
                System.out.printf("%-12.6f", rates[i][j]);
            }
            System.out.println();
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
        int n = currencies.length;
        
        System.out.println("\nStep 1: Transform exchange rates to negative logarithms");
        System.out.println("  Formula: w(u,v) = -log(r_uv)");
        System.out.println("  Reason: Product of rates > 1 ⟺ Sum of weights < 0");
        
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
            System.out.println("\n✓ ARBITRAGE OPPORTUNITY DETECTED!");
            if (arbitrageStart != -1) {
                printArbitrageCycle(predecessors, arbitrageStart, currencies, exchangeRates);
            } else {
                System.out.println("Arbitrage detected but cycle reconstruction failed.");
            }
            return true;
        } else {
            System.out.println("\n✗ No arbitrage opportunities detected.");
            System.out.println("  (Market is in equilibrium - no negative cycles exist)");
            return false;
        }
    }
    
    /**
     * Reconstructs and prints the arbitrage cycle using predecessors.
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
        
        cycle.add(cycleStart);  // Close the cycle
        Collections.reverse(cycle);
        
        // Calculate product
        double productOfRates = 1.0;
        System.out.println("\nArbitrage Cycle Details:");
        System.out.println("-".repeat(60));
        
        for (int i = 0; i < cycle.size() - 1; i++) {  // -1 to avoid double closing
            int from = cycle.get(i);
            int to = cycle.get(i + 1);
            double rate = exchangeRates[from][to];
            productOfRates *= rate;
            
            System.out.printf("  %s -> %s: %.6f\n", 
                currencies[from], currencies[to], rate);
        }
        
        double profitPercentage = (productOfRates - 1.0) * 100;
        
        System.out.println("\nCycle Path: ");
        System.out.print("  ");
        for (int idx : cycle) {
            System.out.print(currencies[idx] + " → ");
        }
        System.out.println(currencies[cycle.get(0)]);  // Close
        
        System.out.printf("\nProduct of exchange rates: %.6f\n", productOfRates);
        System.out.printf("Profit Percentage: %.2f%%\n", profitPercentage);
        
        if (profitPercentage > 0) {
            System.out.println("\nExample: Starting with 1000 " + currencies[cycle.get(0)] + 
                             " → End with " + String.format("%.2f", 1000 * productOfRates) + 
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
    public static void findBestConversionRate(String[] currencies, double[][] exchangeRates, 
                                             String source, String target) {
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
        
        // Check for negative cycle affecting the path (though assumed no arb)
        boolean hasNegativeCycle = false;
        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                if (distances[u] != Double.MAX_VALUE && 
                    distances[u] + logRates[u][v] < distances[v] - EPSILON) {
                    hasNegativeCycle = true;
                }
            }
        }
        if (hasNegativeCycle) {
            System.out.println("Warning: Negative cycle detected; rates may be infinite.");
        }
        
        System.out.println("Step 4: Reconstruct optimal path\n");
        
        if (distances[targetIndex] == Double.MAX_VALUE) {
            System.out.println("No path exists between " + source + " and " + target);
            return;
        }
        
        // Reconstruct path
        List<Integer> path = new ArrayList<>();
        int current = targetIndex;
        while (current != -1) {
            path.add(current);
            current = predecessors[current];
        }
        if (path.get(path.size() - 1) != sourceIndex) {
            System.out.println("Path reconstruction failed.");
            return;
        }
        Collections.reverse(path);
        
        // Display
        System.out.println("=".repeat(60));
        System.out.println("OPTIMAL CONVERSION PATH");
        System.out.println("=".repeat(60));
        
        System.out.print("\nPath: ");
        for (int i = 0; i < path.size(); i++) {
            System.out.print(currencies[path.get(i)]);
            if (i < path.size() - 1) System.out.print(" → ");
        }
        System.out.println("\n");
        
        // Calculate rate
        double totalRate = 1.0;
        System.out.println("Exchange Details:");
        System.out.println("-".repeat(60));
        
        for (int i = 0; i < path.size() - 1; i++) {
            int from = path.get(i);
            int to = path.get(i + 1);
            double rate = exchangeRates[from][to];
            totalRate *= rate;
            System.out.printf("  %s → %s: %.6f\n", 
                currencies[from], currencies[to], rate);
        }
        
        System.out.println("\n" + "=".repeat(60));
        System.out.printf("Best Conversion Rate: %.6f\n", totalRate);
        System.out.println("=".repeat(60));
        
        // Compare with direct
        double directRate = exchangeRates[sourceIndex][targetIndex];
        System.out.printf("\nDirect rate (%s → %s): %.6f\n", source, target, directRate);
        
        if (Math.abs(totalRate - directRate) < EPSILON) {
            System.out.println("→ Direct exchange is optimal!");
        } else if (totalRate > directRate + EPSILON) {
            double improvement = ((totalRate / directRate) - 1.0) * 100;
            System.out.printf("→ Multi-step exchange is %.2f%% better!\n", improvement);
        } else {
            System.out.println("→ Multi-step exchange is worse (should not happen in arb-free graph).");
        }
        
        // Example
        System.out.println("\nExample:");
        System.out.printf("  1000 %s → %.2f %s\n", source, 1000 * totalRate, target);
    }
}  