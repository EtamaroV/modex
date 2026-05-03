package com.modex.modex.mechanic;

import com.modex.modex.datastruct.Edge;
import com.modex.modex.datastruct.Graph;
import com.modex.modex.datastruct.ProvinceNode;
import com.modex.modex.loader.GraphLoader;
import com.modex.modex.model.Player;
import com.modex.modex.view.UIControl;
import javafx.animation.AnimationTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.*;

public class GameController extends AnimationTimer {

    private UIControl ui;
    private TimeManager timeManager;
    private Player player;

    private Graph provinceGraph;

    private int currentUnlockCost;

    private int latestSave = 0;

    private int playerStartNode;

    public GameController(UIControl ui) { // Init
        this.ui = ui;
        this.timeManager = new TimeManager();
        this.player = new Player(5000);
        this.currentUnlockCost = 1000;

        this.provinceGraph = GraphLoader.loadFromJson("thailand_graph.json");

        if (!loadGame()) {
            spawnInitialProvince();
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

        if (timeManager.isNewDay()) {
            System.out.println("--- เริ่มต้นวันที่ " + timeManager.getDay() + " ---");
        }

        if (ui != null && !timeManager.isPaused()) {
            int currentTotalHours = timeManager.getTotalHours();

            for (ProvinceNode node : provinceGraph.getAllNodes()) {
                if (node.isConstructing && currentTotalHours >= node.constructionFinishHour) {
                    node.isConstructing = false;
                    node.isUnlocked = true;
                    System.out.println("🎉 ก่อสร้าง " + node.name + " เสร็จสิ้น!");

                    saveGame();

                    if (ui != null) {
                        ui.updateNodeColor(node);

                        for (ProvinceNode neighbor : provinceGraph.getNeighbors(node)) {
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

                ProvinceNode node = provinceGraph.getNode(id);
                if (node != null) {
                    node.isUnlocked = nodeData.getBoolean("isUnlocked");
                    node.isConstructing = nodeData.getBoolean("isConstructing");
                    node.constructionFinishHour = nodeData.getInt("finishHour");
                    node.isStartNode = (id == playerStartNode);
                }
            }
        }

        if (ui != null) {
            for (ProvinceNode node : provinceGraph.getAllNodes()) {
                if (node.isUnlocked || node.isConstructing) {

                    ui.drawProvinceNode(node);
                    node.isDrawn = true;

                    if (node.isConstructing) {
                        ui.updateNodeToConstructing(node);

                    } else if (node.isUnlocked) {
                        ui.updateNodeColor(node);

                        for (ProvinceNode neighbor : provinceGraph.getNeighbors(node)) {
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

    private void spawnInitialProvince() {
        List<ProvinceNode> allNodes = new ArrayList<>(provinceGraph.getAllNodes());
        if (allNodes.isEmpty()) return;

        Random rand = new Random();
        ProvinceNode startNode = allNodes.get(rand.nextInt(allNodes.size()));

        startNode.isUnlocked = true;
        startNode.isStartNode = true;

        playerStartNode = startNode.id;

        if (ui != null) {
            ui.drawProvinceNode(startNode);
            startNode.isDrawn = true;

            expandVision(startNode);

            ui.updateMoneyLabel(player.getMoney());
        }
    }

    private void expandVision(ProvinceNode centerNode) {
        for (ProvinceNode neighbor : provinceGraph.getNeighbors(centerNode)) {
            if (!neighbor.isDrawn) {
                ui.drawProvinceNode(neighbor);
                neighbor.isDrawn = true;
            }
            ui.drawEdge(centerNode, neighbor, false);
        }
    }

    public void tryUnlockProvince(ProvinceNode targetNode) {
        if (targetNode.isUnlocked || targetNode.isConstructing) return;

        boolean hasUnlockedNeighbor = false;
        for (ProvinceNode neighbor : provinceGraph.getNeighbors(targetNode)) {
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
            targetNode.constructionFinishHour = timeManager.getTotalHours() + 24;

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

    public int getCurrentUnlockCost() {
        return currentUnlockCost;
    }

    public TimeManager getTimeManager() {
        return timeManager;
    }

    public int getMoney() {
        return player.getMoney();
    }

}