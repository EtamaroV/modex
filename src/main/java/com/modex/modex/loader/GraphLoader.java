package com.modex.modex.loader;

import com.modex.modex.datastruct.*;

import org.json.*;

import java.io.InputStream;
import java.nio.file.*;

public class GraphLoader {

    public static Graph loadFromJson(String path) {
        Graph graph = new Graph();

        try {
            InputStream is = GraphLoader.class
                    .getClassLoader()
                    .getResourceAsStream(path);

            if (is == null) {
                throw new RuntimeException("File not found: " + path);
            }

            String content = new String(is.readAllBytes());
            JSONArray arr = new JSONArray(content);

            // 1. สร้าง node ก่อน
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                int id = obj.getInt("id");
                String name = obj.getString("name_en");

                JSONObject coord = obj.getJSONObject("coordinates");
                double lat = coord.getDouble("lat");
                double lon = coord.getDouble("lon");

                ProvinceNode node = new ProvinceNode(id, name, lat, lon);
                graph.addNode(node);
            }

            // 2. สร้าง edge
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                int id = obj.getInt("id");
                JSONArray adj = obj.getJSONArray("adjacent");

                for (int j = 0; j < adj.length(); j++) {
                    JSONObject a = adj.getJSONObject(j);

                    int targetId = a.getInt("id");
                    double distance = a.getDouble("distance_km");

                    graph.addEdge(id, targetId, distance);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return graph;
    }
}