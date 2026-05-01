package com.modex.modex.mechanic;

import com.modex.modex.view.UIControl;
import javafx.animation.AnimationTimer;

public class GameController extends AnimationTimer {

    private UIControl ui;
    private TimeManager timeManager;

    public GameController(UIControl ui) {
        this.ui = ui;
        this.timeManager = new TimeManager();
    }

    @Override
    public void handle(long now) {
        timeManager.update(now);

        if (ui != null) {
            double smoothHour = timeManager.getSmoothHour(now);
            double smoothMinute = timeManager.getSmoothMinute(now);

            ui.updateClock(smoothHour, smoothMinute);
        }

        if (timeManager.isNewDay()) {
            System.out.println("--- เริ่มต้นวันที่ " + timeManager.getDay() + " ---");
        }

        if (ui != null && !timeManager.isPaused()) {
            //ui.moveConveyor(); // สั่งให้สายพานเลื่อน
        }
    }

    public void pauseGame() {
        timeManager.setPaused(true);
        // this.stop();
        System.out.println("Game Paused");
    }

    public void resumeGame() {
        timeManager.setPaused(false);
        // this.start();
        System.out.println("Game Resumed");
    }

    public TimeManager getTimeManager() {
        return timeManager;
    }
}