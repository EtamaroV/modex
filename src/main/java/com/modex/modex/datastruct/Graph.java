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

    public List<Province> getUnlocks(Province startProvince) {
        List<Province> unlockProvinces = new ArrayList<>();
        for (Province node : nodes.values()) {
            if (node.isUnlocked && node.id != startProvince.id) unlockProvinces.add(node);
        }
        return unlockProvinces;
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
    public List<Edge> findShortestPath(Province startNode, Province endNode) {
        if (startNode == null || endNode == null) return null;

        // 1. Reset ข้อมูลพื้นฐานของทุก Node ใน Graph ก่อนเริ่มคำนวณ
        for (Province node : nodes.values()) {
            node.distanceFormSource = 1000000.0; // เทียบเท่า Infinity
            node.from = null;
            node.isVisited = false;
        }

        // 2. กำหนดระยะทางเริ่มต้นที่จุด Start
        startNode.distanceFormSource = 0.0;

        while (true) {
            // ค้นหาจังหวัดที่ระยะทางน้อยที่สุด (Min Distance) และยังไม่ได้ถูก Visit
            Province u = null;
            double minDistance = 1000000.0;

            for (Province temp : nodes.values()) {
                // เงื่อนไข: ต้อง Unlocked แล้ว และยังไม่เคยถูก Visit ในรอบนี้
                if (temp.isUnlocked && !temp.isVisited && temp.distanceFormSource < minDistance) {
                    minDistance = temp.distanceFormSource;
                    u = temp;
                }
            }

            // ถ้าหาโหนดถัดไปไม่ได้ หรือถึงจุดหมายแล้วให้หยุด
            if (u == null || u == endNode) {
                break;
            }

            u.isVisited = true;

            // 3. ตรวจสอบเส้นทางที่เชื่อมจาก u ไปยังจังหวัดข้างเคียง (Relaxation)
            if (u.edges != null) {
                for (Edge e : u.edges) {
                    Province v = e.target;

                    // เดินผ่านได้เฉพาะจังหวัดที่ Unlocked แล้วเท่านั้น
                    if (v.isUnlocked && !v.isVisited) {
                        double alt = u.distanceFormSource + e.distance;
                        
                        // ถ้าเจอเส้นทางที่สั้นกว่าเดิม ให้ทำการ Update
                        if (alt < v.distanceFormSource) {
                            v.distanceFormSource = alt;
                            v.from = u; // เก็บข้อมูลว่ามาจากจังหวัดไหน
                        }
                    }
                }
            }
        }

        // 4. สร้างรายการเส้นทาง (Build Path) ย้อนกลับจากปลายทางไปต้นทาง
        List<Edge> path = new ArrayList<>();
        Province curr = endNode;
        
        while (curr != null && curr.from != null) {
            Province parent = curr.from;
            // หา Edge ที่เชื่อมระหว่าง Parent มายัง Current
            for (Edge e : parent.edges) {
                if (e.target == curr) {
                    path.add(e);
                    break;
                }
            }
            curr = parent;
        }

        Collections.reverse(path);
        
        
        if (endNode.distanceFormSource >= 1000000.0) {
            System.out.println("❌ No path found to: " + endNode.name);
        } else {
            System.out.printf("✅ Path found! Total Distance to %s: %.2f km\n", endNode.name, endNode.distanceFormSource);
        }

        return path;
    }
}