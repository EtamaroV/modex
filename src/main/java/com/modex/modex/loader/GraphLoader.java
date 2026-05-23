package com.modex.modex.loader;

import com.modex.modex.datastruct.Graph;
import com.modex.modex.datastruct.Province;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;

public class GraphLoader { // Json ไปเป็น Graph จังหวัด

    public static Graph loadFromJson(String path) { // load from json
        Graph graph = new Graph(); // สร้าง Graph

        try {
            InputStream is = GraphLoader.class
                    .getClassLoader()
                    .getResourceAsStream(path);

            if (is == null) {
                throw new RuntimeException("File not found: " + path);
            }

            String content = new String(is.readAllBytes()); // อ่านไฟล์
            JSONArray arr = new JSONArray(content); // ทำเป็น json array


            for (int i = 0; i < arr.length(); i++) { // วนทั้ง json array (Vertex)
                JSONObject obj = arr.getJSONObject(i); // ดึง json object จากใน array

                int id = obj.getInt("id"); // id
                String name = obj.getString("name_en"); // ชื่อ eng

                JSONObject coord = obj.getJSONObject("coordinates"); // json object ใน object อีกที
                double lat = coord.getDouble("lat"); // ละติจูด
                double lon = coord.getDouble("lon"); // ลองจิจูด

                Province node = new Province(id, name, lat, lon); // สร้าง Node
                graph.addNode(node); // เอาใส่ Graph
            }

            for (int i = 0; i < arr.length(); i++) { // วนซ้ำ สร้าง Edge
                JSONObject obj = arr.getJSONObject(i); // JsonObject จาก Json Array

                int id = obj.getInt("id"); // id
                JSONArray adj = obj.getJSONArray("adjacent"); // ปลายทาง Json Array

                for (int j = 0; j < adj.length(); j++) { // วนซ้ำทุกตัว
                    JSONObject a = adj.getJSONObject(j); // Json object ข้างในอีกที

                    int targetId = a.getInt("id"); // id
                    double distance = a.getDouble("distance_km"); // distance (weight)

                    graph.addEdge(id, targetId, distance); // เพิ่ม edge ถนน
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return graph; // คืนค่า graph ที่มีจังหวัดและเชื่อมถนน
    }
}