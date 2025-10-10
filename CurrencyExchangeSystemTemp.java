    private static List<Integer> findBasicPath(String[] currencies, double[][] exchangeRates, 
                                                int sourceIndex, int targetIndex) {
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
        Arrays.fill(predecessors, -1);
        distances[sourceIndex] = 0;
        
        // Regular Bellman-Ford without cycle detection
        for (int k = 0; k < n - 1; k++) {
            for (int u = 0; u < n; u++) {
                for (int v = 0; v < n; v++) {
                    if (distances[u] != Double.MAX_VALUE && 
                        distances[u] + logRates[u][v] < distances[v] - EPSILON) {
                        distances[v] = distances[u] + logRates[u][v];
                        predecessors[v] = u;
                    }
                }
            }
        }
        
        if (distances[targetIndex] == Double.MAX_VALUE) {
            return new ArrayList<>();
        }
        
        List<Integer> path = new ArrayList<>();
        int current = targetIndex;
        while (current != -1 && current != sourceIndex) {
            path.add(current);
            current = predecessors[current];
        }
        if (current == -1) {
            return new ArrayList<>();
        }
        path.add(sourceIndex);
        Collections.reverse(path);
        return path;
    }
    
    private static double calculatePathRate(List<Integer> path, double[][] rates) {
        double totalRate = 1.0;
        for (int i = 0; i < path.size() - 1; i++) {
            totalRate *= rates[path.get(i)][path.get(i + 1)];
        }
        return totalRate;
    }
    
    private static void printPath(List<Integer> path, String[] currencies, double[][] rates) {
        for (int i = 0; i < path.size() - 1; i++) {
            int from = path.get(i);
            int to = path.get(i + 1);
            System.out.printf("%s -> %s: %.6f\n", 
                currencies[from], currencies[to], rates[from][to]);
        }
    }