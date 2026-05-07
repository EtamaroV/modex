package com.modex.modex.mechanic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.modex.modex.datastruct.Edge;
import org.json.JSONArray;
import org.json.JSONObject;

import com.modex.modex.datastruct.Graph;
import com.modex.modex.datastruct.Parcel;
import com.modex.modex.datastruct.Province;
import com.modex.modex.loader.GraphLoader;
import com.modex.modex.model.Player;
import com.modex.modex.view.UIControl;

import javafx.animation.AnimationTimer;

import static com.modex.modex.datastruct.Utility.dijkstra;

public class GameController extends AnimationTimer {

    private UIControl ui;
    private TimeManager timeManager;
    private Player player;

    private Graph provinceGraph;

    private int currentUnlockCost;

    private int latestSave = 0;

    private int latestParcelSpawn = 0;

    private int playerStartNode;

    private int unlockNodeCounts = 0;

    private Province startProvince;

    public Graph getProvinceGraph(){
        return provinceGraph;
    }

    public GameController(UIControl ui) { // Init
        this.ui = ui;
        this.timeManager = new TimeManager();
        this.player = new Player(5000);
        this.currentUnlockCost = 1000;

        this.provinceGraph = GraphLoader.loadFromJson("thailand_graph.json");

        if (!loadGame()) {
            //IO.println("haeel");
            startProvince = spawnInitialProvince();
        }

    }

    @Override
    public void handle(long now) {
        timeManager.update(now);

        if (ui != null) {
            double smoothHour = timeManager.getSmoothHour(now);
            double smoothMinute = timeManager.getSmoothMinute(now);

            ui.updateClock(smoothHour, smoothMinute, timeManager.isNightTime());
        }

        int totalHours = timeManager.getTotalHours();
        if ((totalHours > latestSave) && (totalHours % 6 == 0)) {
            latestSave = totalHours;
            saveGame();
            System.out.println("SAVING TOTAL HOURS: " + totalHours);
        }

        if ((totalHours > latestParcelSpawn) && (totalHours % 1 == 0)) { // ต้อง 8 นะ อันนี้เทส
            latestParcelSpawn = totalHours;
            System.out.println("parcel Gen: " + totalHours);
            parcelGeneration();
        }

        if (timeManager.isNewDay()) {
            ui.showDailySummary(1, 2, 3.5, 4);
            System.out.println("--- เริ่มต้นวันที่ " + timeManager.getDay() + " ---");
        }

        if (ui != null && !timeManager.isPaused()) {
            int currentTotalHours = timeManager.getTotalHours();

            for (Province node : provinceGraph.getAllNodes()) {
                if (node.isConstructing && currentTotalHours >= node.constructionFinishHour) {
                    node.isConstructing = false;
                    node.isUnlocked = true;
                    System.out.println("🎉 ก่อสร้าง " + node.name + " เสร็จสิ้น!");

                    saveGame();

                    if (ui != null) {
                        ui.updateNodeColor(node);

                        for (Province neighbor : provinceGraph.getNeighbors(node)) {
                            if (neighbor.isUnlocked) {
                                ui.updateEdgeColor(neighbor, node);
                            }
                        }
                        expandVision(node);
                    }
                }
            }
        }
    }

    public void saveGame() {
        SaveManager.saveGame(player, currentUnlockCost, provinceGraph, timeManager, playerStartNode);
    }

    public boolean loadGame() {
        JSONObject saveData = SaveManager.loadGameData();
        if (saveData == null) return false;

        player.setMoney(saveData.getInt("money"));
        currentUnlockCost = saveData.getInt("currentUnlockCost");
        playerStartNode = saveData.getInt("startNode");

        if (saveData.has("time")) {
            JSONObject timeObj = saveData.getJSONObject("time");
            timeManager.setTime(timeObj.getInt("day"), timeObj.getInt("hour"), timeObj.getInt("minute"));
        }

        if (saveData.has("provincesData")) {
            JSONArray nodesArr = saveData.getJSONArray("provincesData");
            for (int i = 0; i < nodesArr.length(); i++) {
                JSONObject nodeData = nodesArr.getJSONObject(i);
                int id = nodeData.getInt("id");

                Province node = provinceGraph.getNode(id);
                if (node != null) {
                    node.isUnlocked = nodeData.getBoolean("isUnlocked");
                    node.isConstructing = nodeData.getBoolean("isConstructing");
                    node.constructionFinishHour = nodeData.getInt("finishHour");
                    node.isStartNode = (id == playerStartNode);
                    if (id == playerStartNode) {
                        startProvince = node;
                    }
                }
            }
        }

        if (ui != null) {
            for (Province node : provinceGraph.getAllNodes()) {
                if (node.isUnlocked || node.isConstructing) {

                    unlockNodeCounts++;

                    ui.drawProvinceNode(node);
                    node.isDrawn = true;

                    if (node.isConstructing) {
                        ui.updateNodeToConstructing(node);

                    } else if (node.isUnlocked) {
                        ui.updateNodeColor(node);

                        for (Province neighbor : provinceGraph.getNeighbors(node)) {
                            if (neighbor.isUnlocked) {
                                ui.updateEdgeColor(neighbor, node);
                            }
                        }

                        expandVision(node);
                    }
                }
            }
            ui.updateMoneyLabel(player.getMoney());
        }

        return true;
    }

    private Province spawnInitialProvince() {
        List<Province> allNodes = new ArrayList<>(provinceGraph.getAllNodes());
        if (allNodes.isEmpty()) return null;

        Random rand = new Random();
        Province startNode = allNodes.get(rand.nextInt(allNodes.size()));

        startNode.isUnlocked = true;
        startNode.isStartNode = true;

        playerStartNode = startNode.id;

        unlockNodeCounts++;

        if (ui != null) {
            ui.drawProvinceNode(startNode);
            startNode.isDrawn = true;

            expandVision(startNode);

            ui.updateMoneyLabel(player.getMoney());
        }

        return startNode;
    }

    private void expandVision(Province centerNode) {
        for (Province neighbor : provinceGraph.getNeighbors(centerNode)) {
            if (!neighbor.isDrawn) {
                ui.drawProvinceNode(neighbor);
                neighbor.isDrawn = true;
            }
            ui.drawEdge(centerNode, neighbor, false);
        }
    }

    public void tryUnlockProvince(Province targetNode) {
        if (targetNode.isUnlocked || targetNode.isConstructing) return;

        boolean hasUnlockedNeighbor = false;
        for (Province neighbor : provinceGraph.getNeighbors(targetNode)) {
            if (neighbor.isUnlocked) {
                hasUnlockedNeighbor = true;
                break;
            }
        }

        if (!hasUnlockedNeighbor) {
            System.out.println("🚫 ไม่สามารถปลดล็อคได้! อาณาเขตยังไม่เชื่อมต่อ");
            return;
        }

        if (player.deductMoney(currentUnlockCost)) {

            targetNode.isConstructing = true;
            if (unlockNodeCounts > 5) {
                targetNode.constructionFinishHour = timeManager.getTotalHours() + 24;
            } else {
                targetNode.constructionFinishHour = timeManager.getTotalHours() + 1;
            }


            unlockNodeCounts++;

            currentUnlockCost = (int) (currentUnlockCost * 1.0562626);
            System.out.println("🚧 เริ่มก่อสร้าง " + targetNode.name + " (จะเสร็จในอีก 24 ชม.)");

            saveGame();

            if (ui != null) {
                ui.updateNodeToConstructing(targetNode);
                ui.updateMoneyLabel(player.getMoney());
            }
        } else {
            System.out.println("💸 เงินไม่พอ!");
        }
    }

    public Parcel parcelGeneration() {
        Random rand = new Random();

        // 1. ตรวจสอบจุดเริ่มต้น (ป้องกัน Null)
        if (this.startProvince == null) {
            // ดึงจังหวัดแรกใน Graph มาเป็นค่าเริ่มต้น
            this.startProvince = provinceGraph.getNodes().values().iterator().next();
        }

        // 2. ดึงรายการจังหวัดที่ปลดล็อกแล้ว (ไม่รวมจุดเริ่ม)
        List<Province> allNodes = provinceGraph.getUnlocks(this.startProvince);
        if (allNodes.isEmpty()) return null;

        // 3. สุ่มจังหวัดปลายทาง
        Province destination = allNodes.get(rand.nextInt(allNodes.size()));

        // 4. 🔥 วิธีเรียกใช้ Dijkstra ที่คุณเขียนไว้ใน Graph
        // เรียกผ่าน Object ของคลาส Graph ได้เลย
        List<Edge> path = this.provinceGraph.findShortestPath(this.startProvince, destination);

        // 5. นำระยะทางที่ได้มาคำนวณเงิน (กิโลละ 2 บาท + ค่าธรรมเนียม 25)
        double distance = destination.distanceFormSource;
        int reward;

        if (distance >= 1000000.0) {
            reward = 25; // กรณีหาทางไม่เจอจริงๆ ให้แค่ค่าธรรมเนียม
        } else {
            reward = (int) (distance * 2) + 25;
        }

        // 6. สร้างและตั้งค่า Parcel
        Parcel newParcel = new Parcel(this.startProvince, destination);
        newParcel.setReward(reward);

        // แสดงผลตรวจสอบ
        System.out.println("From: " + this.startProvince.name + " -> To: " + destination.name);
        System.out.println("Distance: " + distance + " km | Reward: " + reward + " Baht");

        ui.drawParcelOnConveyor(newParcel);
        return newParcel;
    }



    public int getCurrentUnlockCost() {
        return currentUnlockCost;
    }

    public TimeManager getTimeManager() {
        return timeManager;
    }

    public int getMoney() {
        return player.getMoney();
    }

    public int getUnlockNodeCounts() {
        return unlockNodeCounts;
    }

    public void deliveryParcels(java.util.List<Parcel> parcels) {
        for (Parcel parcel : parcels) {
            System.out.println(parcel.getTo().name);
        }

        ui.removeTruckMenu();
        ui.drawTruckMenu();
    }

}