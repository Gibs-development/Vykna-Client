package com.client;


import com.client.features.gameframe.ScreenMode;

import java.util.HashMap;
import java.util.Map;

/**
 * Status bars.
 *
 * @author C.T, for RuneRogue.
 */

public class StatusBars {

    public Restore restore = Restore.NONE;

    public enum Restore {

        NONE(0,0,BarType.PRAYER),
        PRAYER_3(139,40,BarType.PRAYER);

        private final int item;
        private final int restore;
        private final BarType type;

        public int getItem() {
            return item;
        }

        public int getRestore() {
            return restore;
        }

        public BarType getType() {
            return type;
        }

        Restore(int item, int restore,BarType type) {
            this.item = item;
            this.restore = restore;
            this.type = type;
        }

        private static final Map<Integer, Restore> restoreMap = new HashMap<>();

        static {
            for (Restore restore : values()) {
                restoreMap.put(restore.item, restore);
            }
        }

        public static Restore get(int item) {
            return restoreMap.getOrDefault(item,NONE);
        }

    }

    public enum BarType {
       HP(0x7F230E,0xFF7006,0x0009100,0x004100 ),
       PRAYER(0x2D9491,0x3970BA);

        private final int normal;
        private final int heal;
        private int poisoned;
        private int venom;



        public int getNormal() {
            return normal;
        }

        public int getHeal() {
            return heal;
        }

        public int getPoisoned() {
            return poisoned;
        }

        public int getVenom() {
            return venom;
        }

        BarType(int normal, int heal) {
            this.normal = normal;
            this.heal = heal;
        }


        BarType(int normal, int heal,int poisoned,int venom) {
            this.normal = normal;
            this.heal = heal;
            this.poisoned = poisoned;
            this.venom = venom;
        }
    }

    public void drawStatusBars(int xOffset,int yOffset) {
       if(!Configuration.statusBars) {
            return;
        }

        int hpColor = 0;
        int prayerColor = BarType.PRAYER.getNormal();


        if (Client.instance.poisonType == 0) {
            hpColor = BarType.HP.getNormal();
        } else if (Client.instance.poisonType == 1) {
            hpColor = BarType.HP.getPoisoned();
        } else if (Client.instance.poisonType == 2) {
            hpColor = BarType.HP.getVenom();
        }

         renderStatusBars(xOffset,yOffset,BarType.HP,hpColor);
         renderStatusBars(xOffset,yOffset,BarType.PRAYER,prayerColor);

    }

    public void renderStatusBars(int xOffset,int  yOffset,BarType type, int backgroundColor) {
        int hitpoints = Client.instance.currentStats[3];
        int prayer = Client.instance.currentStats[5];
        int percent = getPercent(getPercent(type,0),250);
        Sprite statusBar = new Sprite("/Interfaces/Wiki/3");
        Sprite statusBar1 = new Sprite("/Interfaces/Wiki/5");

        DrawingArea.drawBoxOutline(xOffset + 11 + getBarOffsetX(type), 42 + yOffset, 20, 250, 0x000000);

        DrawingArea.drawTransparentBox(xOffset + 11 + getBarOffsetX(type), 42 + yOffset, 20, 250 , 0x000000,130);

        DrawingArea.drawTransparentBox(xOffset + 11 + getBarOffsetX(type), 242 - percent + 50 + yOffset , 18, percent , backgroundColor,135);

        if(restore.type == type && restore != Restore.NONE) {
            DrawingArea.drawTransparentBox(xOffset + 12 + getBarOffsetX(type), 242 - getPercent(getPercent(type,restore.restore),250) + 50 + yOffset, 18, getPercent(getPercent(type,restore.restore),250) , type.getHeal(),140);
        }


    if(type == BarType.HP) {
        if(getPercent(type,0) < 20) {
            if(Client.loopCycle % 20 < 10) {
                statusBar.drawSprite(xOffset + 13 + getBarOffsetX(type), 50 + yOffset);
                }
            } else {
                statusBar.drawSprite(xOffset + 13 + getBarOffsetX(type), 50 + yOffset);
                }
            } else {
        if(getPercent(type,0) < 20) {
            if(Client.loopCycle % 20 < 10) {
                statusBar1.drawSprite(xOffset + 11 + getBarOffsetX(type), 50 + yOffset);
                }
            } else {
                statusBar1.drawSprite(xOffset + 11 + getBarOffsetX(type), 50 + yOffset);
            }
        }
        Client.instance.newSmallFont.drawCenteredString(type == BarType.HP ? hitpoints + "" : prayer + "", xOffset + 21 + getBarOffsetX(type), 80 + yOffset,0xFFFFFF, 1);
    }


    public int getBarOffsetX(BarType type) {
        if(type == BarType.HP) {
            return Client.currentScreenMode == ScreenMode.RESIZABLE ? - 5 : 0;
        } else if(type == BarType.PRAYER) {
            return  Client.currentScreenMode == ScreenMode.RESIZABLE ? 210 + - 6 : 210;
        }
        return 0;
    }

    public static int getPercent(int current,int pixels) {
        return  (int) ((pixels) * .01 * current);
    }

    public int getPercent(BarType type, int extra) {
        if(type == BarType.HP) {
            int level = Client.instance.currentStats[3] + extra;
            int max = Client.instance.maxStats[3];
            double percent = level / (double) max;
            return  level > 99 ? 100 : (int) (percent * 100);
        } else if(type == BarType.PRAYER) {
            int level = Client.instance.currentStats[5] + extra;
            int max = Client.instance.maxStats[5];
            double percent = level / (double) max;
            return level > 99 ? 100 : (int) (percent * 100);
        }
        return 0;
    }

    public void setConsume(Restore restore) {
        this.restore = restore;
    }

}
