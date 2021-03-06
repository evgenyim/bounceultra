package com.admin.bounceultra;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;

import static java.lang.Math.*;

class Room {

    Point startPoint;
    ArrayList<GameObject> objectList = new ArrayList<GameObject>();
    ArrayList<Bitmap> bitmapList = new ArrayList<>();
    ArrayList<Bitmap> mapBitmapList = new ArrayList<>();
    Ball ball;
    static Obstacle obstacle;
    boolean complited = false;

    void createMapBitmapList(float k, ArrayList<Bitmap> imageList) {
        for (int i = 0; i < objectList.size(); i++) {
            GameObject cur_object = null;
            try {
                cur_object = objectList.get(i).clone();
            } catch (CloneNotSupportedException ex) {
                System.out.println("Clonable not implemented");
            }
            cur_object.compress(k);
            Bitmap source = Bitmap.createBitmap(imageList.get(cur_object.imageId));
            Bitmap cur_bitmap = Bitmap.createScaledBitmap(source,
                    (int) (cur_object.x_right - cur_object.x_left),
                    (int) (cur_object.y_bottom - cur_object.y_top),
                    false);
            mapBitmapList.add(cur_bitmap);
            objectList.get(i).mapBitmapId = mapBitmapList.size() - 1;
        }
    }

    void draw(float x, float y, float k, Canvas canvas, Paint paint, ArrayList<Bitmap> imageList) {
        float x_left = x;
        float x_right = (float) (x + MainMenu.width * 1.0 / k);
        float y_top = y;
        float y_bottom = (float) (y + MainMenu.height * 1.0 / k);
        for (int i = 0; i < objectList.size(); i++) {
            GameObject cur_object = null;
            try {
                cur_object = objectList.get(i).clone();
            } catch (CloneNotSupportedException ex){
                System.out.println("Clonable not implemented");
            }
            float newX = (float)  (cur_object.x * 1.0 / k + x);
            float newY = (float) (cur_object.y * 1.0 / k + y);
            cur_object.moveToXY(newX, newY);
            cur_object.compress(k);
            if (cur_object.x_right <= x_left || cur_object.x_left >= x_right || cur_object.y_bottom <= y_top || cur_object.y_top >= y_bottom) {
                continue;
            }
            cur_object.draw(canvas, paint, mapBitmapList.get(cur_object.mapBitmapId));
        }
    }

    static void restart() {
        ArrayList<Room> roomArrayList = RoomCreate.create();
        for(int i= 0;i < MainActivity.RoomList.size(); i++) {
            Log.d(String.valueOf(i), String.valueOf(MainActivity.RoomList.get(i).complited));
            if(!MainActivity.RoomList.get(i).complited) {
                MainActivity.RoomList.get(i).objectList = roomArrayList.get(i).objectList;
                MainActivity.RoomList.get(i).ball = new Ball(MainActivity.RoomList.get(i).startPoint, MainActivity.RoomList.get(i).ball.r);
                MainActivity.RoomList.get(i).bitmapList = roomArrayList.get(i).bitmapList;
            }
        }
    }

    void addItem(float x_left, float y_top, float x_right, float y_bottom, float degree, String name, int imageId) {
        Item item = new Item(x_left, y_top, x_right, y_bottom, degree, name, imageId);
        degree = (float) toRadians(degree);
        Point point_right_top = new Point(x_left + (x_right - x_left) * (float) cos(degree) - (y_top - y_top) * (float) sin(degree), y_top + (x_right - x_left) * (float) sin(degree) + (y_top - y_top) * (float) cos(degree));
        Point point_left_top = new Point(x_left, y_top);
        Point point_left_bottom = new Point(x_left + (x_left - x_left)  * (float) cos(degree) - (y_bottom - y_top) * (float) sin(degree), y_top + (x_left - x_left) * (float) sin(degree) + (y_bottom - y_top) * (float) cos(degree));
        Point point_right_bottom = new Point(x_left + (x_right - x_left) * (float) cos(degree) - (y_bottom - y_top) * (float) sin(degree), y_top + (x_right - x_left) * (float) sin(degree) + (y_bottom - y_top) * (float) cos(degree));
        ItemSegment segm_right =  new ItemSegment(point_right_top, point_right_bottom);
        ItemSegment segm_bottom =  new ItemSegment(point_left_bottom, point_right_bottom);
        ItemSegment segm_left = new ItemSegment(point_left_top, point_left_bottom);
        ItemSegment segm_top = new ItemSegment(point_left_top, point_right_top);
        item.segments.add(segm_left);
        item.segments.add(segm_right);
        item.segments.add(segm_bottom);
        item.segments.add(segm_top);
        objectList.add(item);
    }

    void addWater(float x_left, float y_top, float x_right, float y_bottom, int imageId) {
        Water water = new Water(x_left, y_top, x_right, y_bottom, imageId);
        objectList.add(water);
    }

    void addSwitch(float x_left, float y_top, float x_right, float y_bottom, float degree, int imageId, ArrayList <GameObject> target) {
        Switch aSwitch = new Switch(x_left, y_top, x_right, y_bottom, degree, imageId, target);
        degree = (float) toRadians(degree);
        Point point_right_top = new Point(x_left + (x_right - x_left) * (float) cos(degree) - (y_top - y_top) * (float) sin(degree), y_top + (x_right - x_left) * (float) sin(degree) + (y_top - y_top) * (float) cos(degree));
        Point point_left_top = new Point(x_left, y_top);
        Point point_left_bottom = new Point(x_left + (x_left - x_left)  * (float) cos(degree) - (y_bottom - y_top) * (float) sin(degree), y_top + (x_left - x_left) * (float) sin(degree) + (y_bottom - y_top) * (float) cos(degree));
        Point point_right_bottom = new Point(x_left + (x_right - x_left) * (float) cos(degree) - (y_bottom - y_top) * (float) sin(degree), y_top + (x_right - x_left) * (float) sin(degree) + (y_bottom - y_top) * (float) cos(degree));
        SwitchSegment segm_right =  new SwitchSegment(point_right_top, point_right_bottom, aSwitch);
        SwitchSegment segm_bottom =  new SwitchSegment(point_left_bottom, point_right_bottom, aSwitch);
        SwitchSegment segm_left = new SwitchSegment(point_left_top, point_left_bottom, aSwitch);
        SwitchSegment segm_top = new SwitchSegment(point_left_top, point_right_top, aSwitch);
        aSwitch.segments.add(segm_left);
        aSwitch.segments.add(segm_right);
        aSwitch.segments.add(segm_bottom);
        aSwitch.segments.add(segm_top);
        objectList.add(aSwitch);
    }

    void addLock(float x_left, float y_top, float x_right, float y_bottom, float degree, int imageId) {
        Lock lock = new Lock(x_left, y_top, x_right, y_bottom, degree, imageId);
        degree = (float) toRadians(degree);
        Point point_right_top = new Point(x_left + (x_right - x_left) * (float) cos(degree) - (y_top - y_top) * (float) sin(degree), y_top + (x_right - x_left) * (float) sin(degree) + (y_top - y_top) * (float) cos(degree));
        Point point_left_top = new Point(x_left, y_top);
        Point point_left_bottom = new Point(x_left + (x_left - x_left)  * (float) cos(degree) - (y_bottom - y_top) * (float) sin(degree), y_top + (x_left - x_left) * (float) sin(degree) + (y_bottom - y_top) * (float) cos(degree));
        Point point_right_bottom = new Point(x_left + (x_right - x_left) * (float) cos(degree) - (y_bottom - y_top) * (float) sin(degree), y_top + (x_right - x_left) * (float) sin(degree) + (y_bottom - y_top) * (float) cos(degree));
        LockSegment segm_right =  new LockSegment(point_right_top, point_right_bottom);
        LockSegment segm_bottom =  new LockSegment(point_left_bottom, point_right_bottom);
        LockSegment segm_left = new LockSegment(point_left_top, point_left_bottom);
        LockSegment segm_top = new LockSegment(point_left_top, point_right_top);
        lock.segments.add(segm_left);
        lock.segments.add(segm_right);
        lock.segments.add(segm_bottom);
        lock.segments.add(segm_top);
        objectList.add(lock);
    }

    void addHole(float x_left, float y_top, float x_right, float y_bottom, float degree, int imageId, Point startPoint) {
        Hole hole = new Hole(x_left, y_top, x_right, y_bottom, degree, imageId, startPoint);
        degree = (float) toRadians(degree);
        Point point_right_top = new Point(x_left + (x_right - x_left) * (float) cos(degree) - (y_top - y_top) * (float) sin(degree), y_top + (x_right - x_left) * (float) sin(degree) + (y_top - y_top) * (float) cos(degree));
        Point point_left_top = new Point(x_left, y_top);
        Point point_left_bottom = new Point(x_left + (x_left - x_left)  * (float) cos(degree) - (y_bottom - y_top) * (float) sin(degree), y_top + (x_left - x_left) * (float) sin(degree) + (y_bottom - y_top) * (float) cos(degree));
        Point point_right_bottom = new Point(x_left + (x_right - x_left) * (float) cos(degree) - (y_bottom - y_top) * (float) sin(degree), y_top + (x_right - x_left) * (float) sin(degree) + (y_bottom - y_top) * (float) cos(degree));
        HoleSegment segm_right =  new HoleSegment(point_right_top, point_right_bottom);
        HoleSegment segm_bottom =  new HoleSegment(point_left_bottom, point_right_bottom);
        HoleSegment segm_left = new HoleSegment(point_left_top, point_left_bottom);
        HoleSegment segm_top = new HoleSegment(point_left_top, point_right_top);
        hole.segments.add(segm_left);
        hole.segments.add(segm_right);
        hole.segments.add(segm_bottom);
        hole.segments.add(segm_top);
        objectList.add(hole);
    }

    void addFlow(float x_left, float y_top, float x_right, float y_bottom, float degree, int imageId, float a, boolean active) {
        Flow flow = new Flow(x_left, y_top, x_right, y_bottom, degree, imageId, a, active);
        degree = (float) toRadians(degree);
        Point point_right_top = new Point(x_left + (x_right - x_left) * (float) cos(degree) - (y_top - y_top) * (float) sin(degree), y_top + (x_right - x_left) * (float) sin(degree) + (y_top - y_top) * (float) cos(degree));
        Point point_left_top = new Point(x_left, y_top);
        Point point_left_bottom = new Point(x_left + (x_left - x_left)  * (float) cos(degree) - (y_bottom - y_top) * (float) sin(degree), y_top + (x_left - x_left) * (float) sin(degree) + (y_bottom - y_top) * (float) cos(degree));
        Point point_right_bottom = new Point(x_left + (x_right - x_left) * (float) cos(degree) - (y_bottom - y_top) * (float) sin(degree), y_top + (x_right - x_left) * (float) sin(degree) + (y_bottom - y_top) * (float) cos(degree));
        FlowSegment segm_right =  new FlowSegment(point_right_top, point_right_bottom);
        FlowSegment segm_bottom =  new FlowSegment(point_left_bottom, point_right_bottom);
        FlowSegment segm_left = new FlowSegment(point_left_top, point_left_bottom);
        FlowSegment segm_top = new FlowSegment(point_left_top, point_right_top);
        /*flow.segments.add(segm_left);
        flow.segments.add(segm_right);
        flow.segments.add(segm_bottom);
        flow.segments.add(segm_top);*/
        objectList.add(flow);
    }

    void addObstacle(float x_left, float y_top, float x_right, float y_bottom, float degrees, int imageId) {
        obstacle = new Obstacle(x_left, y_top, x_right, y_bottom, degrees, imageId);
        obstacle.x_centre = (x_left + x_right) / 2;
        obstacle.y_centre = (y_bottom + y_top) / 2;
        objectList.add(obstacle);
        int obstId = objectList.size() - 1;
        addSegments(x_left, y_top, x_right, y_bottom, degrees, obstId);
    }

    void addSegments(float x_left, float y_top, float x_right, float y_bottom, double degree, int obstId) {
        degree = toRadians(degree);
        Point point_right_top = new Point(x_left + (x_right - x_left) * (float) cos(degree) - (y_top - y_top) * (float) sin(degree), y_top + (x_right - x_left) * (float) sin(degree) + (y_top - y_top) * (float) cos(degree));
        Point point_left_top = new Point(x_left, y_top);
        Point point_left_bottom = new Point(x_left + (x_left - x_left)  * (float) cos(degree) - (y_bottom - y_top) * (float) sin(degree), y_top + (x_left - x_left) * (float) sin(degree) + (y_bottom - y_top) * (float) cos(degree));
        Point point_right_bottom = new Point(x_left + (x_right - x_left) * (float) cos(degree) - (y_bottom - y_top) * (float) sin(degree), y_top + (x_right - x_left) * (float) sin(degree) + (y_bottom - y_top) * (float) cos(degree));
        SimpleSegment segm_top = new SimpleSegment(point_left_top, point_right_top);
        SimpleSegment segm_right = new SimpleSegment(point_right_top, point_right_bottom);
        SimpleSegment segm_bottom = new SimpleSegment(point_left_bottom, point_right_bottom);
        SimpleSegment segm_left = new SimpleSegment(point_left_top, point_left_bottom);
        objectList.get(obstId).segments.add(segm_top);
        objectList.get(obstId).segments.add(segm_right);
        objectList.get(obstId).segments.add(segm_bottom);
        objectList.get(obstId).segments.add(segm_left);
    }

    void addBall(Point p, float r) {
        ball = new Ball(p, r);
        MainActivity.ball_r = r;
    }

    void addGate(float x_left, float y_top, float x_right, float y_bottom, float degree, int next_room_id, int imageId, boolean compliting) {
        Gate gate = new Gate(x_left, y_top, x_right, y_bottom, degree, next_room_id, imageId, compliting);
        degree = (float) toRadians(degree);
        Point point_right_top = new Point(x_left + (x_right - x_left) * (float) cos(degree) - (y_top - y_top) * (float) sin(degree), y_top + (x_right - x_left) * (float) sin(degree) + (y_top - y_top) * (float) cos(degree));
        Point point_left_top = new Point(x_left, y_top);
        Point point_left_bottom = new Point(x_left + (x_left - x_left)  * (float) cos(degree) - (y_bottom - y_top) * (float) sin(degree), y_top + (x_left - x_left) * (float) sin(degree) + (y_bottom - y_top) * (float) cos(degree));
        Point point_right_bottom = new Point(x_left + (x_right - x_left) * (float) cos(degree) - (y_bottom - y_top) * (float) sin(degree), y_top + (x_right - x_left) * (float) sin(degree) + (y_bottom - y_top) * (float) cos(degree));
        SimpleSegment segm_right =  new SimpleSegment(point_right_top, point_right_bottom);
        SimpleSegment segm_bottom =  new SimpleSegment(point_left_bottom, point_right_bottom);
        SimpleSegment segm_left = new SimpleSegment(point_left_top, point_left_bottom);
        GateSegment segm_top = new GateSegment(point_left_top, point_right_top);
        gate.segments.add(segm_left);
        gate.segments.add(segm_right);
        gate.segments.add(segm_bottom);
        gate.segments.add(segm_top);
        objectList.add(gate);
    }
}