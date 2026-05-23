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

    private final UIControl ui; //สำหรับการสื่อสารไปมากับ ui
    private final TimeManager timeManager; // สำหรับการนับเวลา
    private final Player player; // สำหรับดึงหรือดูข้อมูลผู้เล่น

    // --- [ GAME CONFIGURATIONS ] ---
    private static final int INITIAL_MONEY = 5000; //เงินเริ่มต้น
    private static final int INITIAL_UNLOCK_COST = 1000; // ค่า unlock province ครั้งแรก
    private static final int INITIAL_QUOTA = 200; // เป้าหมาย Quota สำหรับวันแรก
    private static final double QUOTA_MULTIPLIER = 1.1; // อัตราการเพิ่มของ Quota แต่ละรอบ
    private static final double UNLOCK_COST_MULTIPLIER = 1.0562626; // อัตราการเพิ่มของเงินที่ต้องใช้ในการ unlock Province ต่อไป
    
    private static final int AUTOSAVE_INTERVAL_HOURS = 6; // จำนวนชั่วโมงที่จะ Trigger การ save 1 ครั้ง
    private static final int BASE_REWARD = 25; // ค่าขั้นต่ำของเงินรางวัลการจัดส่งต่อกล่อง
    private static final double REWARD_DISTANCE_MULTIPLIER = 2.0; // ตัวคูณสำหรับการคำนวณเงินรางวัลเทียบกับระยะทางในหน่วย กิโลเมตร
    private static final double DELIVERY_EXPENSE_MULTIPLIER = 0.25; // ตัวคูณสำหรับต้นทุนการจัดส่งเทียบกับระยะทางในหน่วย กิโลเมตร
    private static final int BASE_DELIVERY_EXPENSE = 100; // ค่าใช้จ่ายชั้นต่ำในการส่งสินค้า 1 ครั้ง (ส่ง 5 กล่องภายใน 1 run จะถือว่าส่ง 1 ครั้ง)
    
    private static final double RIDER_PROGRESS_STEP = 0.005; // สำหรับการ track Rider
    private static final double AVERAGE_TRUCK_SPEED = 2000.0; // ความเร็วในการจัดส่ง จะถูกแปลงเลขใหม่ในหน่วย กม/ชม ในภายหลัง
    // -------------------------------

    private final Graph provinceGraph; // เป็น Map
    private final List<Rider> activeRiders = new ArrayList<>(); //รวม rider ที่กำลังวิ่งทั้งหมด
    private int currentUnlockCost; // จำนวนเงินที่ต้องใช้ในการ unlock ล่าสุด
    private int currentQuota; // จำนวนเงินเป้าหมายต่อวัน ล่าสุด
    private int latestSave = 0; // ชั่วโมงล่าสุดที่ทำการ save game data
    private int latestParcelSpawn = 0; // ชั่วโมงล่าสุดที่ทำการ generate Parcel ใหม่
    private int playerStartNode; // id Province ที่ผู้เล่นเริ่มเกม
    private int unlockNodeCounts = 0; // Province ที่ถูกunlock
    private Province startProvince; // ข้อมูล Province ที่ผู้เล่นเริ่ม
    private int dailyParcelDelivered = 0; //จำนวน Parcel ที่ถูกส่งในวันนี้
    private double dailyCumulativeDistance = 0; //ระยะทางรวมที่เดินทางวันนี้
    private int dailyIncome = 0; // รายรับวันนี้
    private int dailyExpenses = 0; // รายจ่ายวันนี้

    public GameController(UIControl ui) { // constructor assign ข้อมูลต่างๆลงไป
        this.ui = ui;
        this.timeManager = new TimeManager();
        this.player = new Player(INITIAL_MONEY);
        this.currentUnlockCost = INITIAL_UNLOCK_COST;
        this.currentQuota = INITIAL_QUOTA;

        this.provinceGraph = GraphLoader.loadFromJson("thailand_graph.json"); // load Graph แผนที่ประเทศไทย

        if (!loadGame()) { // กรณีไม่มี save file

            startProvince = spawnInitialProvince(); // สุ่มจุดเริ่มต้น
        }

    }
 // getter
    public Graph getProvinceGraph() {
        return provinceGraph;
    }
 // getter
    public int getCurrentQuota() {
        return currentQuota;
    }
 // override จาก animationTimer ทำหน้าที่วน loop เช็ค status ทั้งหมดภายในเกมที่เกี่ยวข้องกับ Logic ของเกม
    @Override
    public void handle(long now) {
        timeManager.update(now); //update เวลาล่าสุด

        if (ui != null) {
            double smoothHour = timeManager.getSmoothHour(now);
            double smoothMinute = timeManager.getSmoothMinute(now);

            ui.updateClock(smoothHour, smoothMinute, timeManager.isNightTime());
        }

        int totalHours = timeManager.getTotalHours();

        if ((totalHours > latestSave) && (totalHours % AUTOSAVE_INTERVAL_HOURS == 0)) { // เมื่อครบชั่วโมง ทำการ save game data
            latestSave = totalHours;
            saveGame();
            System.out.println("SAVING TOTAL HOURS: " + totalHours);
        }

        if ((totalHours > latestParcelSpawn)) { //หากผ่านไปแล้ว 1 ชม ในเกม จะทำการ generate Parcel ใหม่
            latestParcelSpawn = totalHours;
            System.out.println("parcel Gen: " + totalHours);
            parcelGeneration();
        }

        if (timeManager.isNewDay()) { // เมื่อหมดวัน ทำการสรุปข้อมูลของวันนั้น และทำกา่ร reset ค่าทั้งหมดเกี่ยวกับข้อมูลที่เป็นวันต่อวัน
            ui.showDailySummary(dailyParcelDelivered, dailyIncome, dailyCumulativeDistance, dailyExpenses); //แสดงสรุปข้อมูลและผล Quota

            dailyParcelDelivered = 0; // reset ข้อมูลวันต่อวัน
            dailyCumulativeDistance = 0;
            dailyIncome = 0;
            dailyExpenses = 0;

            currentQuota = (int) (currentQuota * QUOTA_MULTIPLIER); // ปรับ Quota ใหม่และ assign
            ui.updateQuotaLabel(0, currentQuota);
            System.out.println("--- เริ่มต้นวันที่ " + timeManager.getDay() + " ---");
        }

        if (ui != null && !timeManager.isPaused()) { // run ตลอดการเล่น
            int currentTotalHours = timeManager.getTotalHours(); // ดึงค่าชั่วโมงทั้งหมดที่เล่นไป

            for (Province node : provinceGraph.getAllNodes()) { // loop ทุก Province เพื่อเช็คว่ากำลังก่อสร้างและครบเวลาการก่อสร้างแล้วหรือไม่
                if (node.isConstructing && currentTotalHours >= node.constructionFinishHour) {
                    node.isConstructing = false; // ลบ status กำลังก่อสร้างออก
                    node.isUnlocked = true; // unlock Province นั้น
                    System.out.println("🎉 ก่อสร้าง " + node.name + " เสร็จสิ้น!");

                    saveGame(); // เซฟเกม

                    if (ui != null) {
                        ui.updateNodeColor(node); // เปลี่ยนสี node บ่งบอกว่า node นี้ถูก unlock แล้ว

                        for (Province neighbor : provinceGraph.getNeighbors(node)) { //ทำการวาด node ข้างเคียงให้สามารถ unlock ได้
                            if (neighbor.isUnlocked) {
                                ui.updateEdgeColor(neighbor, node);
                            }
                        }
                        expandVision(node);
                    }
                }
            }


            java.util.Iterator<Rider> iterator = activeRiders.iterator(); // เพื่อความสะดวกต่อการใช้งาน
            while (iterator.hasNext()) { // วนเรื่อยๆจนกว่าจะครบทั้งหมด
                Rider rider = iterator.next();

                // [แก้แล้ว] เปลี่ยนมาใช้ pathQueue และ peek()
                if (rider.pathQueue != null && !rider.pathQueue.isEmpty()) { // เช็คว่ายังมีเส้นทางที่ต้องไปต่อหรือไม่
                    Edge currentEdge = rider.pathQueue.peek(); // เอาเส้นทางต่อไป


                    if (rider.edgeProgress == 0.0) { //ถ้ายังไม่เริ่มเดินทาง
                        rider.truckSprite.setRoute(currentEdge.source, currentEdge.target);


                        if (timeManager.getTickInterval() != 0) { // ถ้าเกมไม่ได้หยุดอยู่
                            //BASE_TICK_INTERVAL =  250_000_000L
                            double averageSpeed = 60.0 * ((double) 250_000_000L / (double) timeManager.getTickInterval()); // ความเร็วรถ
                            double distance = Math.max(1.0, currentEdge.distance); // ระยะทาง (น้อยที่สุด 1 กม)


                            rider.edgeSpeed = averageSpeed / distance; // ความเร็วของ Sprite
                        } else {
                            rider.edgeSpeed = 0;
                        }

                    }

                    // [แก้แล้ว] ใช้ RIDER_PROGRESS_STEP แทน 0.005
                    rider.edgeProgress += rider.edgeSpeed * RIDER_PROGRESS_STEP; // ตัวบ่งบอกสถานะPercent การจัดส่ง Parcel

                    if (rider.edgeProgress >= 1.0) { // เมื่อเดินทางครบ 100% (สุดทางแล้ว)

                        rider.truckSprite.updateProgress(1.0); // update ว่าเดินจนสุดทางของ Edge นี้แล้ว
                        rider.currentProvince = currentEdge.target; // update Province ที่อยู่ล่าสุด
                        rider.edgeProgress = 0.0; // ปรับ progress กลับไป 0%
                        rider.removeOldPath(); // ลบ Edge ที่เดินทางล่าสุด


                        // [แก้แล้ว] เปลี่ยนมาใช้ parcelQueue และ peek()
                        while (!rider.parcelQueue.isEmpty() && rider.currentProvince == rider.parcelQueue.peek().getTo()) {
                            Parcel deliveredParcel = rider.parcelQueue.peek(); // รับข้อมูลของ Parcel ที่ส่งสำเร็จ

                            System.out.println("📦 โยนของลงที่ " + deliveredParcel.getTo().name + " | รับเงิน ฿ " + deliveredParcel.getReward());

                            completeDelivery(deliveredParcel); // จัดการเรื่องเงินรางวัล
                            dailyParcelDelivered++; // เพิ่มจำนวน Parcel ที่จัดส่งสำเร็จ

                            rider.removePackage(); // ลบ Parcel ที่จัดส่งสำเร็จแล้วออก
                        }
                    } else {

                        rider.truckSprite.updateProgress(rider.edgeProgress); // Update progress ต่อห่กยังจัดส่งไม่สำเร็จ
                    }
                } else {

                    rider.truckSprite.remove(); // ลบ Sprite ของ Rider ออกถ้าทำทุกอย่างเสร็จแล้ว
                    iterator.remove(); // ลบ Rider นั้นออกจาก Iterator ด้วย
                    System.out.println("🚚 Rider วิ่งส่งของเสร็จสมบูรณ์ ถอนตัวกลับฐาน!");
                }
            }

        }
    }

    public void saveGame() { // เรียกใช้คำสั่ง Save ข้อมูลเกม
        SaveManager.saveGame(player, currentUnlockCost, provinceGraph, timeManager, playerStartNode);
    }

    public void deleteGameData() throws IOException { // ลบข้อมูลเกมทั้งหมด
        SaveManager.deleteGameData();
    }

    public boolean loadGame() { // Load ข้อมูลเกมล่าสุด
        JSONObject saveData = SaveManager.loadGameData();
        if (saveData == null) return false; // ถ้าไม่มี Save file ให้หยุดการ Load

        player.setMoney(saveData.getInt("money")); // Load เงินที่มี ณ ตอนนี้
        currentUnlockCost = saveData.getInt("currentUnlockCost"); // Load ค่า Unlock Province ใหม่ ณ ตอนนี้
        playerStartNode = saveData.getInt("startNode"); // Load Province เริ่มต้น

        if (saveData.has("time")) {
            JSONObject timeObj = saveData.getJSONObject("time"); // Load เวลารวมและนำไปแยกผ่าน TimeManager.setTime()
            timeManager.setTime(timeObj.getInt("day"), timeObj.getInt("hour"), timeObj.getInt("minute"));
        }

        if (saveData.has("provincesData")) {
            JSONArray nodesArr = saveData.getJSONArray("provincesData"); // Load ข้อมูลของ Province ทั้งหมด
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
            for (Province node : provinceGraph.getAllNodes()) { // ทำซ้ำสำหรับทุก Province
                if (node.isUnlocked || node.isConstructing) { // ถ้า Province Unlock แล้ว หรือ กำลังสร้าง จะทำการวาดตาม Status ที่กำลังเป็น

                    unlockNodeCounts++;

                    ui.drawProvinceNode(node);
                    node.isDrawn = true;

                    if (node.isConstructing) { // ถ้า Province นี้กำลังสร้างให้แสดง Icon กำลังก่อสร้าง
                        ui.updateNodeToConstructing(node);

                    } else if (node.isUnlocked) {
                        ui.updateNodeColor(node);

                        for (Province neighbor : provinceGraph.getNeighbors(node)) { // ทำการวาด Province เพื่อนบ้านถ้า Province นี้ถูก unlock แล้ว
                            if (neighbor.isUnlocked) {
                                ui.updateEdgeColor(neighbor, node);
                            }
                        }

                        expandVision(node);
                    }
                }
            }
            ui.updateMoneyLabel(player.getMoney()); // update เงินของผู้เล่นให้ตรงกับข้อมูลที่ Load มา
        }

        return true;
    }

    private Province spawnInitialProvince() { // ทำการสุ่ม Province เริ่มต้นหากเป็นการเริ่มต้นเกมใหม่
        List<Province> allNodes = new ArrayList<>(provinceGraph.getAllNodes());
        if (allNodes.isEmpty()) return null;

        Random rand = new Random(); // ตัวสุ่ม
        Province startNode = allNodes.get(rand.nextInt(allNodes.size())); // สุ่ม Province เริ่มต้น

        startNode.isUnlocked = true; // ตั้ง Status ว่า Province นี้ ถูก Unlock แล้ว และเป็น Province เริ่มต้นด้วย
        startNode.isStartNode = true;

        playerStartNode = startNode.id; // เก็บค่า Province เริ่มต้นเข้าไปที่ Player ด้วย

        unlockNodeCounts++; // เพิ่ม Province ที่ถูก Unlock แล้วเป็น 1

        if (ui != null) {
            ui.drawProvinceNode(startNode); // วาด Province เริ่มต้น
            startNode.isDrawn = true;

            expandVision(startNode); // วาด Province เพื่อนบ้าน

            ui.updateMoneyLabel(player.getMoney()); // เปลี่ยนเงินของ Player ให้ตรงกับข้อมูลที่มี
        }

        return startNode;
    }

    private void expandVision(Province centerNode) { // วาด Province เพื่อนบ้านของ Province ที่ Unlock แล้ว
        for (Province neighbor : provinceGraph.getNeighbors(centerNode)) { // วนทุก Province ที่เป็นเพื่อนบ้านกับ Province ที่รับมา
            if (!neighbor.isDrawn) {
                ui.drawProvinceNode(neighbor); // วาด Province เพื่อนบ้านที่ยังไม่ถูกวาด
                neighbor.isDrawn = true;
            }
            ui.drawEdge(centerNode, neighbor, false); // วาด Edge ด้วย
        }
    }

    public void tryUnlockProvince(Province targetNode) { // ใช้สำหรับการเช็คว่าสามารภ Unlock Province ที่กำหนดได้หรือไม่
        if (targetNode.isUnlocked || targetNode.isConstructing) return; // ถ้า Unlock แล้วหรือกำลังก่อสร้างงจะไม่ run function นี้ต่อ

        boolean hasUnlockedNeighbor = false; // ตั้ง Default ของ Province ที่ได้รับมานี้ว่าไม่สามารถปลดล็อคได้ และค่าอาจถูกเปลี่ยนใน loop ข้างล่าง
        for (Province neighbor : provinceGraph.getNeighbors(targetNode)) {
            if (neighbor.isUnlocked) {
                hasUnlockedNeighbor = true;
                break;
            }
        }

        if (!hasUnlockedNeighbor) { // ถ้าไม่มี Province เพื่อนบ้านที่ Unlock แล้ว
            System.out.println("🚫 ไม่สามารถปลดล็อคได้! อาณาเขตยังไม่เชื่อมต่อ");
            return; // ออก function
        }

        if (player.deductMoney(currentUnlockCost)) { // ถ้าสามารถ Unlock ได้และสามารถจ่างเงินได้
            this.ui.moneyPopup(-currentUnlockCost); // ลบเงินและแสดงผ่าน UI เป็น Popup message
            targetNode.isConstructing = true; // เริ่ม Status กำลังสร้าง
            if (unlockNodeCounts > 5) { // ถ้า Province ที่ถูก Unlock ยังไม่ถึง 5 จะใช้เวลาแค่ 1 ชม ในเกมสำหรับการก่อสร้าง นอกเหนือจากนั้นจะใช้ 24 ชม ในการก่อสร้าง
                targetNode.constructionFinishHour = timeManager.getTotalHours() + 24;
            } else {
                targetNode.constructionFinishHour = timeManager.getTotalHours() + 1;
            }


            unlockNodeCounts++; // เพิ่มจำนวน Province ที่ Unlock แล้วโดย 1

            currentUnlockCost = (int) (currentUnlockCost * UNLOCK_COST_MULTIPLIER); // เพิ่ม เงินที่ต้องใช้ในการ Unlock Province ถัดไป
            System.out.println("🚧 เริ่มก่อสร้าง " + targetNode.name + " (จะเสร็จในอีก 24 ชม.)");

            saveGame(); // Save game

            if (ui != null) {
                ui.updateNodeToConstructing(targetNode); // เปลี่ยน Icon ของ Province นั้นให้เป็นกำลังก่อสร้าง
                ui.updateMoneyLabel(player.getMoney()); // เปลี่ยนเงินหลังจากทำการซื้อ Province นั้นแล้ว
            }
        } else {
            System.out.println("💸 เงินไม่พอ!");
        }
    }

    public Parcel parcelGeneration() { // สร้าง Parcel ใหม่แบบสุ่ม
        Random rand = new Random(); // ตัวสุ่ม


        if (this.startProvince == null) {

            this.startProvince = provinceGraph.getNodes().values().iterator().next();
        }

        List<Province> allNodes = provinceGraph.getUnlocks(this.startProvince); // รับ list ของ Province ที่ Unlock แล้ว
        if (allNodes.isEmpty()) return null;

        Province destination = allNodes.get(rand.nextInt(allNodes.size())); // สุ่มจุดหมายจาก list Province ที่ Unlock แล้ว

        List<Edge> path = this.provinceGraph.findShortestPath(this.startProvince, destination); // คำนวณเส้นทางที่สั้นที่สุดจากจุดเริ่ม


        double distance = destination.distanceFormSource;
        int reward;

        if (distance >= Double.MAX_VALUE) {
            reward = BASE_REWARD; // ตั้งต้นรางวัล ให้มีค่าตามที่กำหนด
        } else {
            reward = (int) (distance * REWARD_DISTANCE_MULTIPLIER) + BASE_REWARD; // ตั้งรางวัลตามที่ถูกคำนวณไว้
        }

        Parcel newParcel = new Parcel(this.startProvince, destination); // สร้าง Parcel ใหม่
        newParcel.setReward(reward); // ตั้งรางวัล

        System.out.println("From: " + this.startProvince.name + " -> To: " + destination.name);
        System.out.println("Distance: " + distance + " km | Reward: " + reward + " Baht");

        ui.drawParcelOnConveyor(newParcel); // วาด Parcel ใหม่ไว้บนสายพาน
        return newParcel;
    }

    public int getCurrentUnlockCost() {
        return currentUnlockCost;
    } //getter ของเงินที่ต้องใช้ในการ Unlock Province ใหม่

    public TimeManager getTimeManager() {
        return timeManager;
    } //getter TimeManager

    public int getMoney() {
        return player.getMoney();
    } //getter ของเงินที่ผู้เล่นมี

    public int getUnlockNodeCounts() {
        return unlockNodeCounts;
    } //getter ของจำนวน Province ที่ผู้เล่น Unlock ไปแล้ว

    public void deliveryParcels(java.util.List<Parcel> parcels) { // จัดการเกี่ยวกับลำดับพัสดุและลำดับการเดินทาง
        if (parcels == null || parcels.isEmpty()) return; // List ของ Parcel ที่รับมาต้องไม่ว่าง

        double cumulativeDistance = 0;
        double averageSpeed = AVERAGE_TRUCK_SPEED; // ความเร็วของรถ

        System.out.println("\n========== DELIVERY ROUTE SUMMARY ==========");


        Province lastStop = parcels.getFirst().getFrom(); // กำหนดจุดเริ่มของ run นี้

        for (int i = 0; i < parcels.size(); i++) {
            Parcel currentParcel = parcels.get(i); // วนทุก Parcel ใน list


            currentParcel.setFrom(lastStop);
            currentParcel.setCurrentProvince(lastStop);

            List<Edge> optimizedPath = this.provinceGraph.findShortestPath(lastStop, currentParcel.getTo()); // คำนวณ Edge ทั้งหมดที่ต้องเดินไป
            currentParcel.setPath(optimizedPath); // set Edge ที้ต้องเดินให้

            double segmentDistance = 0;
            if (optimizedPath != null && !optimizedPath.isEmpty()) { // ถ้า Edge ที่ต้องเดินไม่ว่าง และต้องไม่ใช่ Edge เดียวสำหรับการไปส่ง Parcel
                for (Edge edge : optimizedPath) { // วนทุก Edge ที่ต้องเดิน
                    segmentDistance += edge.distance; // บวกระยะทางไปเรื่อยๆจนครบ
                }
            } else {
                segmentDistance = currentParcel.getTo().distanceFormSource; // ถ้าว่างหรือใช้ Edge เดียวทำการ assign ค่าระยะทางของ Edge นั้นได้เลย
            }

            currentParcel.setDistanceDelivery(segmentDistance); // ตั้งระยะทางรวมของ run นี้
            cumulativeDistance += segmentDistance; // บวกระยะทางเข้ากับระยะทางรวมของวัน

            double etaInHours = (cumulativeDistance / averageSpeed);
            currentParcel.setEstimatedArrivalTime(etaInHours);


            System.out.printf("Box #%d: [%s -> %s]\n", (i + 1), currentParcel.getFrom().name, currentParcel.getTo().name);
            System.out.printf("   - Distance this leg: %.2f km\n", segmentDistance);
            System.out.printf("   - Total distance so far: %.2f km\n", cumulativeDistance);
            System.out.printf("   - Est. Arrival Time: +%.2f hr\n", etaInHours);
            System.out.println("--------------------------------------------");


            lastStop = currentParcel.getTo(); // เก็บ Province ล่าสุดที่ผ่าน
        }

        System.out.println("Total Route Distance: " + String.format("%.2f", cumulativeDistance) + " km");
        System.out.println("============================================\n");


        Province startLocation = parcels.getFirst().getFrom();  // ตั้งค่าจุดเริ่ม
        Rider newRider = new Rider(parcels, startLocation); // สร้าง Rider ใหม่

        //  [แก้ 12] วนลูปจับเส้นทางยัดลง Queue ของคุณทีละอันด้วยคำสั่ง enqueue (เพราะ Queue ไม่มีคำสั่ง addAll)
        for (Parcel p : parcels) { // วนทุก Parcel ใน List
            if (p.getPath() != null) { // Edge ที่จะต้องไปส่ง Parcel นี้จะต้องไม่ว่าง
                for (Edge e : p.getPath()) { // วนทุก Edge ที่ต้องใช้ในการเดินทางไปส่ง Parcel นี้
                    newRider.pathQueue.enqueue(e); // นำไปเพิ่มลงใน Edge ที่ Rider ต้องเดินทางไป
                }
            }
        }


        newRider.truckSprite = ui.new TruckSprite(startLocation, startLocation); // สร้าง Sprite rider ขึ้้นมา


        activeRiders.add(newRider); // เพิ่ม rider คนนี้เข้าไปใน list activeRider


        ui.removeTruckMenu(); // reset interface สำหรับจัดการพัสดุของ Rider คนนั้น
        ui.drawTruckMenu();


        int expense = BASE_DELIVERY_EXPENSE + (int) (cumulativeDistance * DELIVERY_EXPENSE_MULTIPLIER); // จัดการต้นทุนการส่ง
        dailyExpenses += expense; // เพิ่มรายจ่ายลงไปในรายจ่ายประจำวัน
        this.ui.moneyPopup(-expense); // ประกาศ Popup ว่าเสียเงินไปเท่าไหร่
        dailyCumulativeDistance += cumulativeDistance; // เพิ่มระยะทางที่เดินทางในวันนี้

    }

    public void completeDelivery(Parcel parcel) { // คำนวณรางวัลหลังจากจัดส่งสำเร็จ
        if (parcel == null) return; // Parcel จะต้องไม่ว่าง / จะต้องมี Parcel


        int moneyToEarn = parcel.getReward(); // เพิ่มเงินในรายรับประจำวัน
        dailyIncome += moneyToEarn;


        this.player.addMoney(moneyToEarn);  // เพิ่มเงินในกระเป๋า Player


        this.ui.updateMoneyLabel(this.player.getMoney()); // เปลี่ยนค่าเงินที่แสดงให้ตรงกับข้อมูลล่าสุด
        this.ui.updateQuotaLabel(dailyIncome, currentQuota); // เปลี่ยนเงินในส่วนของ Quota ประจำวัน
        this.ui.moneyPopup(moneyToEarn); // แสดง Popup เงินที่ได้รับ

        System.out.println("💰 ภารกิจสำเร็จ! ส่งพัสดุถึง " + parcel.getTo().name + " ได้รับเงิน " + moneyToEarn + " บาท");
    }


}