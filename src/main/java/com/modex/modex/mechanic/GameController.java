package com.modex.modex.mechanic;

import com.modex.modex.datastruct.*;
import com.modex.modex.loader.GraphLoader;
import com.modex.modex.model.Player;
import com.modex.modex.view.UIControl;
import javafx.animation.AnimationTimer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameController extends AnimationTimer {

    private final UIControl ui;
    private final TimeManager timeManager;
    private final Player player;

    // --- [ GAME CONFIGURATIONS ] ---
    private static final int INITIAL_MONEY = 5000;
    private static final int INITIAL_UNLOCK_COST = 1000;
    private static final int INITIAL_QUOTA = 200;
    private static final double QUOTA_MULTIPLIER = 1.1;
    private static final double UNLOCK_COST_MULTIPLIER = 1.0562626;
    
    private static final int AUTOSAVE_INTERVAL_HOURS = 6;
    private static final int BASE_REWARD = 25;
    private static final double REWARD_DISTANCE_MULTIPLIER = 2.0;
    private static final double DELIVERY_EXPENSE_MULTIPLIER = 0.25;
    private static final int BASE_DELIVERY_EXPENSE = 100;
    
    private static final double RIDER_PROGRESS_STEP = 0.005;
    private static final double AVERAGE_TRUCK_SPEED = 2000.0;
    // -------------------------------

    private final Graph provinceGraph;
    private final List<Rider> activeRiders = new ArrayList<>();
    private int currentUnlockCost;
    private int currentQuota;
    private int latestSave = 0;
    private int latestParcelSpawn = 0;
    private int playerStartNode;
    private int unlockNodeCounts = 0;
    private Province startProvince;
    private int dailyParcelDelivered = 0;
    private double dailyCumulativeDistance = 0;
    private int dailyIncome = 0;
    private int dailyExpenses = 0;

    public GameController(UIControl ui) {
        this.ui = ui;
        this.timeManager = new TimeManager();
        this.player = new Player(INITIAL_MONEY);
        this.currentUnlockCost = INITIAL_UNLOCK_COST;
        this.currentQuota = INITIAL_QUOTA;

        this.provinceGraph = GraphLoader.loadFromJson("thailand_graph.json");

        if (!loadGame()) {

            startProvince = spawnInitialProvince();
        }

    }

    public Graph getProvinceGraph() {
        return provinceGraph;
    }

    public int getCurrentQuota() {
        return currentQuota;
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

        if ((totalHours > latestSave) && (totalHours % AUTOSAVE_INTERVAL_HOURS == 0)) {
            latestSave = totalHours;
            saveGame();
            System.out.println("SAVING TOTAL HOURS: " + totalHours);
        }

        if ((totalHours > latestParcelSpawn)) {
            latestParcelSpawn = totalHours;
            System.out.println("parcel Gen: " + totalHours);
            parcelGeneration();
        }

        if (timeManager.isNewDay()) {
            ui.showDailySummary(dailyParcelDelivered, dailyIncome, dailyCumulativeDistance, dailyExpenses);

            dailyParcelDelivered = 0;
            dailyCumulativeDistance = 0;
            dailyIncome = 0;
            dailyExpenses = 0;

            currentQuota = (int) (currentQuota * QUOTA_MULTIPLIER);
            ui.updateQuotaLabel(0, currentQuota);
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


            java.util.Iterator<Rider> iterator = activeRiders.iterator();
            while (iterator.hasNext()) {
                Rider rider = iterator.next();

                // [แก้แล้ว] เปลี่ยนมาใช้ pathQueue และ peek()
                if (rider.pathQueue != null && !rider.pathQueue.isEmpty()) {
                    Edge currentEdge = rider.pathQueue.peek();


                    if (rider.edgeProgress == 0.0) {
                        rider.truckSprite.setRoute(currentEdge.source, currentEdge.target);


                        if (timeManager.getTickInterval() != 0) {
                            //BASE_TICK_INTERVAL =  250_000_000L
                            double averageSpeed = 60.0 * ((double) 250_000_000L / (double) timeManager.getTickInterval());
                            double distance = Math.max(1.0, currentEdge.distance);


                            rider.edgeSpeed = averageSpeed / distance;
                        } else {
                            rider.edgeSpeed = 0;
                        }

                    }

                    // [แก้แล้ว] ใช้ RIDER_PROGRESS_STEP แทน 0.005
                    rider.edgeProgress += rider.edgeSpeed * RIDER_PROGRESS_STEP;

                    if (rider.edgeProgress >= 1.0) {

                        rider.truckSprite.updateProgress(1.0);
                        rider.currentProvince = currentEdge.target;
                        rider.edgeProgress = 0.0;
                        rider.removeOldPath();


                        // [แก้แล้ว] เปลี่ยนมาใช้ parcelQueue และ peek()
                        while (!rider.parcelQueue.isEmpty() && rider.currentProvince == rider.parcelQueue.peek().getTo()) {
                            Parcel deliveredParcel = rider.parcelQueue.peek();

                            System.out.println("📦 โยนของลงที่ " + deliveredParcel.getTo().name + " | รับเงิน ฿ " + deliveredParcel.getReward());

                            completeDelivery(deliveredParcel);
                            dailyParcelDelivered++;

                            rider.removePackage();
                        }
                    } else {

                        rider.truckSprite.updateProgress(rider.edgeProgress);
                    }
                } else {

                    rider.truckSprite.remove();
                    iterator.remove();
                    System.out.println("🚚 Rider วิ่งส่งของเสร็จสมบูรณ์ ถอนตัวกลับฐาน!");
                }
            }

        }
    }

    public void saveGame() {
        SaveManager.saveGame(player, currentUnlockCost, provinceGraph, timeManager, playerStartNode);
    }

    public void deleteGameData() throws IOException {
        SaveManager.deleteGameData();
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
            this.ui.moneyPopup(-currentUnlockCost);
            targetNode.isConstructing = true;
            if (unlockNodeCounts > 5) {
                targetNode.constructionFinishHour = timeManager.getTotalHours() + 24;
            } else {
                targetNode.constructionFinishHour = timeManager.getTotalHours() + 1;
            }


            unlockNodeCounts++;

            currentUnlockCost = (int) (currentUnlockCost * UNLOCK_COST_MULTIPLIER);
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


        if (this.startProvince == null) {

            this.startProvince = provinceGraph.getNodes().values().iterator().next();
        }

        List<Province> allNodes = provinceGraph.getUnlocks(this.startProvince);
        if (allNodes.isEmpty()) return null;

        Province destination = allNodes.get(rand.nextInt(allNodes.size()));

        List<Edge> path = this.provinceGraph.findShortestPath(this.startProvince, destination);


        double distance = destination.distanceFormSource;
        int reward;

        if (distance >= Double.MAX_VALUE) {
            reward = BASE_REWARD;
        } else {
            reward = (int) (distance * REWARD_DISTANCE_MULTIPLIER) + BASE_REWARD;
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

        double cumulativeDistance = 0;
        double averageSpeed = AVERAGE_TRUCK_SPEED;

        System.out.println("\n========== DELIVERY ROUTE SUMMARY ==========");


        Province lastStop = parcels.getFirst().getFrom();

        for (int i = 0; i < parcels.size(); i++) {
            Parcel currentParcel = parcels.get(i);


            currentParcel.setFrom(lastStop);
            currentParcel.setCurrentProvince(lastStop);

            List<Edge> optimizedPath = this.provinceGraph.findShortestPath(lastStop, currentParcel.getTo());
            currentParcel.setPath(optimizedPath);

            double segmentDistance = 0;
            if (optimizedPath != null && !optimizedPath.isEmpty()) {
                for (Edge edge : optimizedPath) {
                    segmentDistance += edge.distance;
                }
            } else {
                segmentDistance = currentParcel.getTo().distanceFormSource;
            }

            currentParcel.setDistanceDelivery(segmentDistance);
            cumulativeDistance += segmentDistance;

            double etaInHours = (cumulativeDistance / averageSpeed);
            currentParcel.setEstimatedArrivalTime(etaInHours);


            System.out.printf("Box #%d: [%s -> %s]\n", (i + 1), currentParcel.getFrom().name, currentParcel.getTo().name);
            System.out.printf("   - Distance this leg: %.2f km\n", segmentDistance);
            System.out.printf("   - Total distance so far: %.2f km\n", cumulativeDistance);
            System.out.printf("   - Est. Arrival Time: +%.2f hr\n", etaInHours);
            System.out.println("--------------------------------------------");


            lastStop = currentParcel.getTo();
        }

        System.out.println("Total Route Distance: " + String.format("%.2f", cumulativeDistance) + " km");
        System.out.println("============================================\n");


        Province startLocation = parcels.getFirst().getFrom();
        Rider newRider = new Rider(parcels, startLocation);

        // 🚨 [แก้ 12] วนลูปจับเส้นทางยัดลง Queue ของคุณทีละอันด้วยคำสั่ง enqueue (เพราะ Queue ไม่มีคำสั่ง addAll)
        for (Parcel p : parcels) {
            if (p.getPath() != null) {
                for (Edge e : p.getPath()) {
                    newRider.pathQueue.enqueue(e);
                }
            }
        }


        newRider.truckSprite = ui.new TruckSprite(startLocation, startLocation);


        activeRiders.add(newRider);


        ui.removeTruckMenu();
        ui.drawTruckMenu();


        int expense = BASE_DELIVERY_EXPENSE + (int) (cumulativeDistance * DELIVERY_EXPENSE_MULTIPLIER);
        dailyExpenses += expense;
        this.ui.moneyPopup(-expense);
        dailyCumulativeDistance += cumulativeDistance;

    }

    public void completeDelivery(Parcel parcel) {
        if (parcel == null) return;


        int moneyToEarn = parcel.getReward();
        dailyIncome += moneyToEarn;


        this.player.addMoney(moneyToEarn);


        this.ui.updateMoneyLabel(this.player.getMoney());
        this.ui.updateQuotaLabel(dailyIncome, currentQuota);
        this.ui.moneyPopup(moneyToEarn);

        System.out.println("💰 ภารกิจสำเร็จ! ส่งพัสดุถึง " + parcel.getTo().name + " ได้รับเงิน " + moneyToEarn + " บาท");
    }


}