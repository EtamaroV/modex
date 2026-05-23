package com.modex.modex.datastruct;

import java.util.*;

public class Graph {
    private final Map<Integer, Province> nodes = new HashMap<>(); // เก็บข้อมูล Province ทั้งหมดและเก็บ Key เป็น ID ของจังหวัด

    public void addNode(Province node) { // เพิ่ม Province ลงใน nodes HashMap
        nodes.put(node.id, node);
    }

    public Province getNode(int id) { // getter เรียก Province ตาม ID
        return nodes.get(id);
    }

    public Collection<Province> getAllNodes() { // getter เรียกทุก Province
        return nodes.values();
    }

    public void addEdge(int id1, int id2, double distance) { // เพิ่มเส้นทางระหว่าง 2 จังหวัด
        Province a = nodes.get(id1); // ดึงข้อมูล 2 จังหวัด
        Province b = nodes.get(id2);

        if (a == null || b == null) return; // ถ้าหาไม่เจอจะทำการออกจาก function

        a.edges.add(new Edge(a, b, distance)); // เพิ่ม Edge ลงไปใน edges list
    }

    public Map<Integer, Province> getNodes() { // getter เรียก Province เป็น Map
        return nodes;
    }

    public List<Province> getNeighbors(Province node) { // getter เรียกหา Province เพื่อนบ้านของ Province ที่กำหนด
        List<Province> neighbors = new ArrayList<>(); // สร้าง List ใหม่
        if (node != null && node.edges != null) { // Provonce ที่รับมาจะต้องไม่ว่าง และต้องมีเส้นทางเชื่อมไปหา Province อื่น
            for (Edge e : node.edges) { // วนให้ครบทุกเส้นทางที่ไปได้รอบๆ
                neighbors.add(e.target); // เพิ่ม Province เพื่อนบ้านลงใน List
            }
        }
        return neighbors; // คืน List ของ Province เพื่อนบ้าน
    }

    public List<Province> getUnlocks(Province startProvince) { // getter เรียกหา Province ที่ Unlock เรียบร้อย
        List<Province> unlockProvinces = new ArrayList<>(); // สร้าง List ใหม่
        for (Province node : nodes.values()) { // วน Province ให้ครบทั้งหมด
            if (node.isUnlocked && node.id != startProvince.id) unlockProvinces.add(node); // ถ้า Province นี้ unlock เรียบร้อยและไม่ใช่ Province เริ่มต้น ทำการเพิ่ม Province นี้ลงไปใน List นี้
        }
        return unlockProvinces; // ส่งค่า List ที่เกิดขึ้นใน Function นี้ออกไป
    }

    public void printAdjacencyList() { // สำหรับการทดสอบว่ามีการเชื่อมต่อของ Province ถูกหรือไม่ในรูปแบบ List
        for (Province node : nodes.values()) {
            System.out.print(node.name + " -> ");

            for (Edge e : node.edges) {
                System.out.print(e.target.name + "(" + e.distance + "km), ");
            }

            System.out.println();
        }
    }

    public void printAdjacencyMatrix() { // สำหรับการทดสอบว่ามีการเชื่อมต่อของ Province ถูกหรือไม่ในรูปแบบ Matrix
        List<Province> nodeList = new ArrayList<>(nodes.values());

        int n = nodeList.size();
        double[][] matrix = new double[n][n];


        Map<Province, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            indexMap.put(nodeList.get(i), i);
        }


        for (int i = 0; i < n; i++) {
            Arrays.fill(matrix[i], Double.POSITIVE_INFINITY); // Assign ค่าdistance ให้เป็น infinity ทั้งหมดก่อน
            matrix[i][i] = 0; // Assign ให้ตัวที่อยู่แนวทแยงกลสงเป็น 0
        }


        for (Province node : nodeList) {
            int i = indexMap.get(node);

            for (Edge e : node.edges) {
                int j = indexMap.get(e.target);
                matrix[i][j] = e.distance;
            }
        }


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

    
    public void resetGraphPaths() { // เปลี่ยน ค่าของ Province ทั้งหมดให้พร้อมสำหรับการทำ Dijkstra ครั้งต่อไป
        for (Province node : nodes.values()) {
            node.distanceFormSource = Double.MAX_VALUE;
            node.from = null;
            node.isVisited = false;
        }
    }

    public List<Edge> findShortestPath(Province startNode, Province endNode) { // Dijkstra Algorithm
        if (startNode == null || endNode == null) return null; // Province ที่รับมาทั้งคู่จะต้องไม่ว่าง

        
        this.resetGraphPaths(); // reset ตัวแปร

        startNode.distanceFormSource = 0.0; // ตั้งค่า Distance ให้เป็น 0 สำหรับ Province เริ่มต้น

        
        while (true) { // วนตลอด
            Province u = null;
            double minDistance = Double.MAX_VALUE;

            for (Province temp : nodes.values()) { // วนทุก Province ที่มีเช็คหา Province ที่มีระยะทางจาก Source สั้นที่สุดที่ unlock แล้วและยังไม่เคยถูกเข้าถึงใน function นี้
                
                if (temp.isUnlocked && !temp.isVisited && temp.distanceFormSource < minDistance) {
                    minDistance = temp.distanceFormSource;
                    u = temp;
                }
            }

            if (u == null || u == endNode) { // ถ้า Province ผลลัพธ์จาก Loop ด้านบนคือ Province ที่ต้องการหรือว่าง ให้ออกจาก While loop
                break;
            }

            u.isVisited = true; // ตั้งว่าเคยเข้าถึง Province ผลลัพธ์จาก Loop ด้านบนแล้ว

            if (u.edges != null) { // ถ้า Province นี้มี Edge
                for (Edge e : u.edges) { // วนทุก Edge ของ Province ที่ได้
                    Province v = e.target;

                    if (v.isUnlocked && !v.isVisited) { // ถ้า Province ที่ Edge นี้ชี้ไป unlock แล้วและยังไม่เคยเข้าถึงจาก function นี้
                        double alt = u.distanceFormSource + e.distance; // ระยะทางของ Province ต้น + ระยะทางจาก Edge นี้

                        if (alt < v.distanceFormSource) { // เช็คถ้าการคำนวณใหม้ระยะทางสั้นกว่าระยะทางเก่า ให้ทำการเปลี่ยนเป็นระยะทางใหม่แทน และ เปลี่ยน Province ก่อน Province นี้เป็น u แทน
                            v.distanceFormSource = alt;
                            v.from = u;
                        }
                    }
                }
            }
        }

        
        
        Stack<Edge> pathStack = new Stack<>(); // Stack  ของเส้นทางที่จะเดินทางไป
        Province curr = endNode; // curr เริ่มจาก Province ปลายทาง

        while (curr != null && curr.from != null) { // วนไปจนกว่า curr จะชี้ไปที่ null
            Province parent = curr.from; // รับ Province ก่อนที่เก็บไว้ใน Province นี้

            for (Edge e : parent.edges) {// วน Edge จนกว่าจะเจอ parent
                if (e.target == curr) {
                    pathStack.push(e);  // เมื่อเจอ ทำการ Push Edge นี้
                    break;
                }
            }
            curr = parent; // ย้าย curr ไปชี้ที่ parent แทน
        }

        
        List<Edge> path = new ArrayList<>(); // สร้าง List สำหรับ Edge ที่ต้องผ่านทั้งหมด
        while (!pathStack.isEmpty()) {
            path.add(pathStack.pop());  // เพิ่ม Edge จาก Stack เข้าไปใน List
        }

        

        
        if (endNode.distanceFormSource >= Double.MAX_VALUE) { // ถ้าไม่เจอ
            
            System.out.println("❌ No path found to: " + endNode.name); 
        } else { // ถ้าเจอ
            System.out.printf("✅ Path found! Total Distance to %s: %.2f km\n", endNode.name, endNode.distanceFormSource);
        }

        return path; // ส่งค่า List ของ Edge ออกไป
    }
}