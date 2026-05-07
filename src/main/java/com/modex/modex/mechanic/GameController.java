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

        List<Province> allNodes = provinceGraph.getUnlocks(this.startProvince);
        if (allNodes.isEmpty()) return null;

        Province destination = allNodes.get(rand.nextInt(allNodes.size()));

        List<Edge> path = this.provinceGraph.findShortestPath(this.startProvince, destination);

        
        double distance = destination.distanceFormSource;
        int reward;

        if (distance >= 1000000.0) {
            reward = 25; // กรณีหาทางไม่เจอจริงๆ ให้แค่ค่าธรรมเนียม
        } else {
            reward = (int) (distance * 2) + 25;
        }

        Parcel newParcel = new Parcel(this.startProvince, destination);
        newParcel.setReward(reward);

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
        if (parcels == null || parcels.isEmpty()) return;

        // 1. เตรียมตัวแปรสำหรับเก็บข้อมูลสรุป
        double cumulativeDistance = 0;
        // สมมติความเร็วรถเฉลี่ยที่ 60 km/h (หรือปรับตาม Logic เกมของคุณ)
        double averageSpeed = 60.0; 

        System.out.println("\n========== DELIVERY ROUTE SUMMARY ==========");
        
        // เริ่มต้นจุดแรกด้วยตำแหน่งปัจจุบัน (from ของพัสดุกล่องแรก)
        Province lastStop = parcels.get(0).getFrom();

        for (int i = 0; i < parcels.size(); i++) {
            Parcel currentParcel = parcels.get(i);

            // 2. Chaining Logic: อัปเดตจุดเริ่มให้ต่อจากจุดหมายก่อนหน้า
            currentParcel.setFrom(lastStop);
            currentParcel.setCurrentProvince(lastStop);

            // 3. คำนวณเส้นทางด้วย Dijkstra
            List<Edge> optimizedPath = this.provinceGraph.findShortestPath(lastStop, currentParcel.getTo());
            currentParcel.setPath(optimizedPath);

            // 4. คำนวณระยะทางช่วงปัจจุบัน
            double segmentDistance = 0;
            if (optimizedPath != null && !optimizedPath.isEmpty()) {
                for (Edge edge : optimizedPath) {
                    segmentDistance += edge.distance; // อ้างอิงตัวแปร distance จาก Edge.java
                }
            } else {
                // หากหาเส้นทางไม่ได้ ให้ใช้ค่าที่ Dijkstra บันทึกไว้ในโหนดปลายทาง
                segmentDistance = currentParcel.getTo().distanceFormSource;
            }

            // 5. อัปเดตข้อมูลลงใน Parcel
            currentParcel.setDistanceDelivery(segmentDistance);
            cumulativeDistance += segmentDistance;

            // คำนวณเวลาที่คาดว่าจะถึง (ETA) ของกล่องนี้
            double etaInMinutes = (cumulativeDistance / averageSpeed);
            currentParcel.setEstimatedArrivalTime(etaInMinutes);

            // 6. แสดงข้อมูลของแต่ละกล่อง
            System.out.printf("Box #%d: [%s -> %s]\n", (i + 1), currentParcel.getFrom().name, currentParcel.getTo().name);
            System.out.printf("   - Distance this leg: %.2f km\n", segmentDistance);
            System.out.printf("   - Total distance so far: %.2f km\n", cumulativeDistance);
            System.out.printf("   - Est. Arrival Time: +%.0f hr\n", etaInMinutes);
            System.out.println("--------------------------------------------");

            // อัปเดตจุดจอดเพื่อใช้คำนวณกล่องถัดไป
            lastStop = currentParcel.getTo();
        }

        System.out.println("Total Route Distance: " + String.format("%.2f", cumulativeDistance) + " km");
        System.out.println("============================================\n");

        // อัปเดต UI
        ui.removeTruckMenu();
        ui.drawTruckMenu();

    }

    public void completeDelivery(Parcel parcel) {
        if (parcel == null) return;

        // 1. ดึงเงินรางวัลที่เก็บไว้ใน parcel ออกมา
        int moneyToEarn = parcel.getReward();

        // 2. เพิ่มเงินให้ตัวละคร (Player)
        this.player.addMoney(moneyToEarn);

        // 3. อัปเดตเงินบนหน้าจอ UI
        this.ui.updateMoneyLabel(this.player.getMoney());

        System.out.println("💰 ภารกิจสำเร็จ! ส่งพัสดุถึง " + parcel.getTo().name + " ได้รับเงิน " + moneyToEarn + " บาท");
    }


}