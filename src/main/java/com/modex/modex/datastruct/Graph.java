package com.modex.modex.datastruct;

import java.util.*;

public class Graph {
    private Map<Integer, Province> nodes = new HashMap<>();

    public void addNode(Province node) {
        nodes.put(node.id, node);
    }

    public Province getNode(int id) {
        return nodes.get(id);
    }

    public Collection<Province> getAllNodes() {
        return nodes.values();
    }

    public void addEdge(int id1, int id2, double distance) {
        Province a = nodes.get(id1);
        Province b = nodes.get(id2);

        if (a == null || b == null) return;

        a.edges.add(new Edge(a ,b, distance));
    }

    public Map<Integer, Province> getNodes(){
        return nodes;
    }

    public List<Province> getNeighbors(Province node) {
        List<Province> neighbors = new ArrayList<>();
        if (node != null && node.edges != null) {
            for (Edge e : node.edges) {
                neighbors.add(e.target);
            }
        }
        return neighbors;
    }

    public void printAdjacencyList() {
        for (Province node : nodes.values()) {
            System.out.print(node.name + " -> ");

            for (Edge e : node.edges) {
                System.out.print(e.target.name + "(" + e.distance + "km), ");
            }

            System.out.println();
        }
    }

    public void printAdjacencyMatrix() {
        List<Province> nodeList = new ArrayList<>(nodes.values());

        int n = nodeList.size();
        double[][] matrix = new double[n][n];

        // map node -> index
        Map<Province, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            indexMap.put(nodeList.get(i), i);
        }

        // ใส่ค่าเริ่มต้น (ไม่มีเส้นทาง = infinity)
        for (int i = 0; i < n; i++) {
            Arrays.fill(matrix[i], Double.POSITIVE_INFINITY);
            matrix[i][i] = 0;
        }

        // เติม edge ลง matrix
        for (Province node : nodeList) {
            int i = indexMap.get(node);

            for (Edge e : node.edges) {
                int j = indexMap.get(e.target);
                matrix[i][j] = e.distance;
            }
        }

        // 🖥️ แสดงผล
        System.out.print("      ");
        for (Province node : nodeList) {
            System.out.printf("%-12s", node.name);
        }
        System.out.println();

        for (int i = 0; i < n; i++) {
            System.out.printf("%-6s", nodeList.get(i).name);

            for (int j = 0; j < n; j++) {
                if (matrix[i][j] == Double.POSITIVE_INFINITY) {
                    System.out.printf("%-12s", "0");
                } else {
                    System.out.printf("%-12.1f", matrix[i][j]);
                }
            }
            System.out.println();
        }
    }
}