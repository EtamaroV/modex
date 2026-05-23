package com.modex.modex.mechanic;

import com.modex.modex.datastruct.Graph;
import com.modex.modex.datastruct.Province;
import com.modex.modex.model.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SaveManager { // ตัวเซฟข้อมูลผู้เล่น

    private static final String GAME_FOLDER_NAME = "ModEx"; // Folder name
    private static final String SAVE_FILE_NAME = "playerData.json"; // File name

    private static Path getSaveDirectory() { // ดึงค่าที่ที่จะเอาข้อมูลไปเก็บ
        String os = System.getProperty("os.name").toLowerCase(); // ดึงค่า os
        String userHome = System.getProperty("user.home"); // ดึง user.home
        Path saveDir;

        if (os.contains("win")) { // Windows
            String appData = System.getenv("LOCALAPPDATA"); // ดึง path LocalAppData
            saveDir = Paths.get(appData, GAME_FOLDER_NAME); // path สำหรับเซฟ
        } else if (os.contains("mac")) { // macOS
            saveDir = Paths.get(userHome, "Library", "Application Support", GAME_FOLDER_NAME); // path สำหรับเซฟ
        } else { // Linux, อื่นๆ
            saveDir = Paths.get(userHome, "." + GAME_FOLDER_NAME); // path สำหรับเซฟ
        }

        File dir = saveDir.toFile();
        if (!dir.exists()) { // folder ไม่มี
            dir.mkdirs(); // สร้าง folder
        }

        return saveDir; // คืนค่า
    }

    private static Path getSaveFilePath() { // ดึง save game path
        return getSaveDirectory().resolve(SAVE_FILE_NAME);
    }

    // Save game
    public static void saveGame(Player player, int currentCost, Graph graph, TimeManager time, int startNode) {
        try {
            JSONObject saveObj = new JSONObject(); // สร้าง json object
            // นำข้อมูลผู้เล่นใน json
            saveObj.put("money", player.getMoney());
            saveObj.put("currentUnlockCost", currentCost);
            saveObj.put("startNode", startNode);

            JSONObject timeObj = new JSONObject();
            timeObj.put("day", time.getDay());
            timeObj.put("hour", time.getHour());
            timeObj.put("minute", time.getMinute());
            saveObj.put("time", timeObj);

            JSONArray nodesArr = new JSONArray();
            for (Province node : graph.getAllNodes()) {
                if (node.isUnlocked || node.isConstructing) {
                    JSONObject nodeData = new JSONObject();
                    nodeData.put("id", node.id);
                    nodeData.put("isUnlocked", node.isUnlocked);
                    nodeData.put("isConstructing", node.isConstructing);
                    nodeData.put("finishHour", node.constructionFinishHour);
                    nodesArr.put(nodeData);
                }
            }
            saveObj.put("provincesData", nodesArr);

            Files.writeString(getSaveFilePath(), saveObj.toString(4)); // เขียนลงในไฟล์
            System.out.println("💾 บันทึกเกมสำเร็จ!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // load save game
    public static JSONObject loadGameData() {
        try {
            Path savePath = getSaveFilePath(); // ดึงไฟล์ save game
            if (Files.exists(savePath)) { // ถ้ามี
                String content = Files.readString(savePath); // อ่านไฟล์
                System.out.println("📂 โหลดเซฟเกมสำเร็จ! จาก: " + savePath);
                return new JSONObject(content); // คืน Json object ของข้อมูลในไฟล์
            } else {
                System.out.println("⚠️ ไม่พบไฟล์เซฟเกม (กำลังเริ่มเกมใหม่...)");
            }
        } catch (Exception e) {
            System.out.println("❌ เกิดข้อผิดพลาดในการโหลดเกม!");
            e.printStackTrace();
        }
        return null;
    }

    // ลบ save game
    public static void deleteGameData() throws IOException {
        Path path = getSaveFilePath(); // save game path
        if (Files.exists(path)) { // มี
            Files.delete(path); // ลบ
            System.out.println("Save data deleted successfully.");
        } else {
            System.out.println("No save data found to delete.");
        }
    }
}