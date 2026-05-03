package com.modex.modex.mechanic;

import com.modex.modex.datastruct.Graph;
import com.modex.modex.datastruct.ProvinceNode;

import com.modex.modex.model.Player;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SaveManager {

    private static final String GAME_FOLDER_NAME = "MODEx";
    private static final String SAVE_FILE_NAME = "playerData.json";
    
    private static Path getSaveDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        Path saveDir;

        if (os.contains("win")) {
            String appData = System.getenv("LOCALAPPDATA");
            saveDir = Paths.get(appData, GAME_FOLDER_NAME);
        } else if (os.contains("mac")) {
            saveDir = Paths.get(userHome, "Library", "Application Support", GAME_FOLDER_NAME);
        } else {
            saveDir = Paths.get(userHome, "." + GAME_FOLDER_NAME);
        }

        File dir = saveDir.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return saveDir;
    }

    private static Path getSaveFilePath() {
        return getSaveDirectory().resolve(SAVE_FILE_NAME);
    }

    public static void saveGame(Player player, int currentCost, Graph graph, TimeManager time, int startNode) {
        try {
            JSONObject saveObj = new JSONObject();
            saveObj.put("money", player.getMoney());
            saveObj.put("currentUnlockCost", currentCost);
            saveObj.put("startNode", startNode);

            JSONObject timeObj = new JSONObject();
            timeObj.put("day", time.getDay());
            timeObj.put("hour", time.getHour());
            timeObj.put("minute", time.getMinute());
            saveObj.put("time", timeObj);

            JSONArray nodesArr = new JSONArray();
            for (ProvinceNode node : graph.getAllNodes()) {
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

            Files.writeString(getSaveFilePath(), saveObj.toString(4));
            System.out.println("💾 บันทึกเกมสำเร็จ!");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static JSONObject loadGameData() {
        try {
            Path savePath = getSaveFilePath();
            if (Files.exists(savePath)) {
                String content = Files.readString(savePath);
                System.out.println("📂 โหลดเซฟเกมสำเร็จ! จาก: " + savePath.toString());
                return new JSONObject(content);
            } else {
                System.out.println("⚠️ ไม่พบไฟล์เซฟเกม (กำลังเริ่มเกมใหม่...)");
            }
        } catch (Exception e) {
            System.out.println("❌ เกิดข้อผิดพลาดในการโหลดเกม!");
            e.printStackTrace();
        }
        return null;
    }
}