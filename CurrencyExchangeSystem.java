import java.util.*;
import java.net.*;
import java.io.*;

public class CurrencyExchangeSystem {
    private static final String API_URL = "https://api.frankfurter.app/latest";
    private static final double EPSILON = 1e-9;

    // Natural Language: Main method serves as the entry point, allowing users to choose between real-time data, manual input, or test cases.
    // Pseudocode: 
    // 1. Prompt user for mode choice (1: real-time, 2: manual, 3: tests)
    // 2. Based on choice, initialize currencies and exchange rates
    // 3. Execute arbitrage detection and best conversion rate tasks
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Currency Exchange Analyzer with Real-Time Data\n");
        System.out.println("Choose mode:");
        System.out.println("1. Run with real-time data");
        System.out.println("2. Manual input");
        System.out.println("3. Run tests");
        int choice = scanner.nextInt();
        scanner.nextLine();

        String[] currencies = null;
        double[][] exchangeRates = null;

        if (choice == 1) {
            currencies = new String[]{"USD", "EUR", "JPY", "GBP", "AUD", "CAD", "CHF", "CNY", "NZD", "SGD"};
            System.out.println("Fetching real-time exchange rates...");
            exchangeRates = fetchRealTimeRates(currencies);
            if (exchangeRates == null) {
                System.out.println("Failed to fetch real-time data. Check your internet connection");
                System.exit(1);
            }
        } else if (choice == 2) {
            System.out.println("Enter input in the format: n, CUR1, CUR2, ..., CURn");
            System.out.println("Followed by n*n exchange rates, one per line.");
            Object[] input = readManualInput(scanner);
            currencies = (String[]) input[0];
            exchangeRates = (double[][]) input[1];
        } else if (choice == 3) {
            testAssignmentExample();
            testRandomizedRates();
            testBetterIntermediate();
            testNoArbitrageCase();
            testRealWorldRates();
            System.exit(0);
        } else {
            System.out.println("Invalid choice.");
            System.exit(1);
        }

        System.out.println("\n TASK 1: ARBITRAGE DETECTION \n");
        boolean hasArbitrage = detectArbitrage(currencies, exchangeRates);

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

    // Natural Language: Reads manual input from user to set up currencies and exchange rates.
    // Pseudocode: 
    // 1. Read line with number of currencies and currency codes
    // 2. Read n*n matrix of exchange rates
    // 3. Return array containing currencies and rates
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
        scanner.nextLine();
        return new Object[]{currencies, rates};
    }

    // Natural Language: Tests the assignment example with a known arbitrage case.
    // Pseudocode: 
    // 1. Define currencies A, B, C, D, E
    // 2. Set up exchange rate matrix with arbitrage opportunity
    // 3. Call detectArbitrage to verify
    private static void testAssignmentExample() {
        System.out.println("TEST: Assignment Example with Arbitrage");
        String[] currencies = {"A", "B", "C", "D", "E"};
        double[][] exchangeRates = new double[5][5];
        exchangeRates[0] = new double[]{1.0, 0.651, 0.581, 1.0, 1.0};
        exchangeRates[1] = new double[]{1.531, 1.0, 0.952, 1.0, 1.0};
        exchangeRates[2] = new double[]{1.711, 1.049, 1.0, 1.0, 1.0};
        exchangeRates[3] = new double[]{1.0, 1.0, 1.0, 1.0, 1.0};
        exchangeRates[4] = new double[]{1.0, 1.0, 1.0, 1.0, 1.0};
        detectArbitrage(currencies, exchangeRates);
    }

    // Natural Language: Generates and tests a matrix with random exchange rates.
    // Pseudocode: 
    // 1. Create n currencies
    // 2. Generate random rates between 0.01 and 2.0
    // 3. Call detectArbitrage to check for opportunities
    private static void testRandomizedRates() {
        System.out.println("\nTEST: Randomized Exchange Rates");
        int n = 5;
        String[] currencies = new String[n];
        for (int i = 0; i < n; i++) {
            currencies[i] = "C" + (i + 1);
        }

        double[][] exchangeRates = generateRandomRates(n);
        detectArbitrage(currencies, exchangeRates);
    }

    // Natural Language: Tests a case where an intermediate path is better without arbitrage.
    // Pseudocode: 
    // 1. Define currencies A, B, C, D, E
    // 2. Set up matrix with better intermediate path
    // 3. Call detectArbitrage and findBestConversionRate
    private static void testBetterIntermediate() {
        System.out.println("\nTEST: No Arbitrage but Better Intermediate Path");
        String[] currencies = {"A", "B", "C", "D", "E"};
        double[][] exchangeRates = new double[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                exchangeRates[i][j] = (i == j ? 1.0 : 1.0);
            }
        }
        int A = 0, B = 1, C = 2, D = 3, E = 4;
        exchangeRates[A][B] = 2.0; exchangeRates[B][A] = 0.4;
        exchangeRates[B][C] = 2.0; exchangeRates[C][B] = 0.4;
        exchangeRates[A][C] = 1.0; exchangeRates[C][A] = 0.25;
        exchangeRates[A][D] = 1.0; exchangeRates[D][A] = 1.0;
        exchangeRates[B][D] = 0.5; exchangeRates[D][B] = 2.0;
        exchangeRates[C][D] = 0.25; exchangeRates[D][C] = 4.0;
        exchangeRates[A][E] = 1.0; exchangeRates[E][A] = 1.0;
        exchangeRates[B][E] = 0.5; exchangeRates[E][B] = 2.0;
        exchangeRates[C][E] = 0.25; exchangeRates[E][C] = 4.0;
        exchangeRates[D][E] = 1.0; exchangeRates[E][D] = 1.0;
        boolean hasArbitrage = detectArbitrage(currencies, exchangeRates);
        if (!hasArbitrage) {
            List<Integer> arbitrageCycle = new ArrayList<>();
            findBestConversionRate(currencies, exchangeRates, "A", "C", arbitrageCycle);
        }
    }

    // Natural Language: Tests a no-arbitrage equilibrium market case.
    // Pseudocode: 
    // 1. Define currencies USD, EUR, GBP, JPY, AUD
    // 2. Set rates based on USD equilibrium
    // 3. Call detectArbitrage to verify
    private static void testNoArbitrageCase() {
        System.out.println("\nTEST: No Arbitrage - Equilibrium Market");
        String[] currencies = {"USD", "EUR", "GBP", "JPY", "AUD"};
        double[][] exchangeRates = new double[5][5];

        double[] usdRates = {1.0, 0.85, 0.73, 110.0, 1.35};

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                exchangeRates[i][j] = usdRates[j] / usdRates[i];
            }
        }

        System.out.println("Explanation: All rates calculated from USD base to ensure consistency");
        System.out.println("Formula: rate(i→j) = usdRate[j] / usdRate[i]");
        detectArbitrage(currencies, exchangeRates);
    }

    // Natural Language: Tests real-world rates using an API or falls back to simulation.
    // Pseudocode: 
    // 1. Define currencies
    // 2. Fetch real-time rates or use simulated rates
    // 3. Call detectArbitrage
    private static void testRealWorldRates() {
        System.out.println("\nTEST: Real-World Exchange Rates from API");
        String[] currencies = {"USD", "EUR", "GBP", "JPY", "AUD"};
        System.out.println("Fetching real-time data from Frankfurter API...");

        double[][] exchangeRates = fetchRealTimeRates(currencies);
        if (exchangeRates != null) {
            System.out.println("\nExplanation: Real-world rates should not have arbitrage due to market efficiency");
            detectArbitrage(currencies, exchangeRates);
        } else {
            System.out.println("Note: API fetch failed. This test requires internet connection.");
            System.out.println("Using simulated real-world rates instead:");
            testNoArbitrageCase();
        }
    }

    // Natural Language: Generates a random exchange rate matrix.
    // Pseudocode: 
    // 1. Initialize n x n matrix
    // 2. Set diagonal to 1.0, others to random values between 0.01 and 2.0
    // 3. Return matrix
    private static double[][] generateRandomRates(int n) {
        double[][] rates = new double[n][n];
        Random rand = new Random();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    rates[i][j] = 1.0;
                } else {
                    rates[i][j] = rand.nextDouble() * 2 + 0.01;
                }
            }
        }
        return rates;
    }

    // Natural Language: Fetches real-time exchange rates from an API.
    // Pseudocode: 
    // 1. Connect to API with USD base
    // 2. Parse JSON response to extract rates
    // 3. Construct n x n matrix from rates
    // 4. Return matrix or null on failure
    public static double[][] fetchRealTimeRates(String[] currencies) {
        try {
            int n = currencies.length;
            double[][] rates = new double[n][n];

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

                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        if (i == j) {
                            rates[i][j] = 1.0;
                        } else if (usdRates[i] > 0) {
                            rates[i][j] = usdRates[j] / usdRates[i];
                        } else {
                            rates[i][j] = 0.0;
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

    // Natural Language: Extracts a specific currency rate from a JSON response.
    // Pseudocode: 
    // 1. Find "rates" section in JSON
    // 2. Search for currency key
    // 3. Extract and parse rate value
    // 4. Return rate or -1 on failure
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

    // Natural Language: Detects arbitrage opportunities using Bellman-Ford algorithm.
    // Pseudocode: 
    // 1. Convert rates to negative logarithms
    // 2. Initialize distances and predecessors
    // 3. Run Bellman-Ford for n-1 iterations
    // 4. Check for negative cycles
    // 5. Output results
    public static boolean detectArbitrage(String[] currencies, double[][] exchangeRates) {
        long startTime = System.nanoTime();

        int n = currencies.length;

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

        double[] distances = new double[n];
        int[] predecessors = new int[n];
        Arrays.fill(distances, Double.MAX_VALUE);
        Arrays.fill(predecessors, -1);
        distances[0] = 0;

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
                break;
            }
        }

        boolean hasNegativeCycle = false;
        int arbitrageStart = -1;
        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                if (distances[u] != Double.MAX_VALUE && 
                    distances[u] + logRates[u][v] < distances[v] - EPSILON) {
                    distances[v] = distances[u] + logRates[u][v];
                    predecessors[v] = u;
                    hasNegativeCycle = true;
                    arbitrageStart = v;
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
            System.out.println("(Market is in equilibrium - no negative cycles exist)");
        }

        double timeMs = (System.nanoTime() - startTime) / 1_000_000.0;
        System.out.println("\nExecution time for arbitrage detection: " + String.format("%.3f", timeMs) + " ms");

        return hasNegativeCycle;
    }

    // Natural Language: Prints the details of an arbitrage cycle.
    // Pseudocode: 
    // 1. Trace predecessors to find cycle
    // 2. Calculate product of rates
    // 3. Output cycle sequence and profit
    private static void printArbitrageCycle(int[] predecessors, int start, 
                                           String[] currencies, double[][] exchangeRates) {
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

        Collections.reverse(cycle);

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

    // Natural Language: Finds a specific arbitrage cycle in the graph.
    // Pseudocode: 
    // 1. Convert rates to negative logarithms
    // 2. Run Bellman-Ford to detect cycle
    // 3. Trace back cycle using predecessors
    // 4. Return cycle list
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

    // Natural Language: Finds the best conversion rate using Bellman-Ford algorithm.
    // Pseudocode: 
    // 1. Convert rates to negative logarithms
    // 2. Run Bellman-Ford from source to target
    // 3. Reconstruct path and calculate total rate
    // 4. Output results, considering arbitrage if present
    public static void findBestConversionRate(String[] currencies, double[][] exchangeRates, 
                                             String source, String target, List<Integer> arbitrageCycle) {
        int n = currencies.length;
        int sourceIndex = -1, targetIndex = -1;

        for (int i = 0; i < n; i++) {
            if (currencies[i].equals(source)) sourceIndex = i;
            if (currencies[i].equals(target)) targetIndex = i;
        }

        if (sourceIndex == -1 || targetIndex == -1) {
            System.out.println("Error: Currency not found.");
            return;
        }

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

        double[] distances = new double[n];
        int[] predecessors = new int[n];
        Arrays.fill(distances, Double.MAX_VALUE);
        Arrays.fill(predecessors, -1);
        distances[sourceIndex] = 0;

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
                break;
            }
        }

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
            System.out.println("\nWarning: Cycle detected in path reconstruction!");
            System.out.println("This indicates an arbitrage opportunity that affects the optimal path.");
            System.out.println("Will show the basic path ignoring potential infinite improvements.");

            int cycleIndex = path.indexOf(current);
            path = new ArrayList<>(path.subList(0, cycleIndex + 1));
        }

        Collections.reverse(path);

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

            double startAmount = 1000;
            System.out.printf("\nExample starting with %.2f %s:\n", startAmount, source);
            System.out.printf("1. Basic conversion: %.2f %s\n", startAmount * totalRate, target);

            double improvedRate = totalRate * cycleRate;
            System.out.printf("2. With one arbitrage cycle: %.2f %s\n", startAmount * improvedRate, target);
            System.out.printf("3. With two arbitrage cycles: %.2f %s\n", startAmount * improvedRate * cycleRate, target);
            System.out.println("\nEach additional cycle will multiply the rate by: " + String.format("%.6f", cycleRate));
        }

        if (arbitrageCycle.isEmpty()) {
            double directRate = exchangeRates[sourceIndex][targetIndex];
            System.out.printf("\nDirect rate (%s -> %s): %.6f\n", source, target, directRate);

            if (Math.abs(totalRate - directRate) < EPSILON) {
                System.out.println("Direct exchange is optimal!");
            } else if (totalRate > directRate + EPSILON) {
                double improvement = ((totalRate / directRate) - 1.0) * 100;
                System.out.printf("Multi-step exchange is %.2f%% better!\n", improvement);
            }

            System.out.println("\nExample:");
            System.out.printf("  1000 %s -> %.2f %s\n", source, 1000 * totalRate, target);
        }
    }
}