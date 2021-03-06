package com.admin.bounceultra;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;
class Flow extends GameObject {

    float a;
    float v;
    float sideV;
  
    boolean active;

    void switchAction() {
        if (active) {
            active = false;
        } else {
            active = true;
        }
    }

    void insideCommunicate(Ball ball, ArrayList<GameObject> objectList, int intersected_obj_ind, boolean draft){
        if (!active) {
            return;
        }
        float angle =(float) Math.toRadians(objectList.get(intersected_obj_ind).degrees);
        if(ball.x_speed > v * Math.cos(angle)){
            ball.x_speed -= a * (float) Math.cos(angle);
            if(ball.x_speed < v * Math.cos(angle)) {
                ball.x_speed = v * (float) Math.cos(angle);
            }
        } else if(ball.x_speed <= v * Math.cos(angle)) {
            ball.x_speed += a * (float) Math.cos(angle);
            if(ball.x_speed > v * Math.cos(angle)) {
                ball.x_speed = v * (float) Math.cos(angle);
            }
        }
        if(ball.y_speed > v * Math.sin(angle)) {
            ball.y_speed -= a * (float) Math.sin(angle);
            if(ball.y_speed < v * Math.sin(angle)) {
                ball.y_speed = v * (float) Math.sin(angle);
            }
        } else if (ball.y_speed <= v * Math.sin(angle))  {
            ball.y_speed += a * (float) Math.sin(angle);
            if(ball.y_speed > v * Math.sin(angle)) {
                ball.y_speed = v * (float) Math.sin(angle);
            }
        }
        if(ball.x_speed > 0){
            ball.x_speed -= sideV * (float) Math.sin(angle);
            if(ball.x_speed < 0) {
                ball.x_speed = 0;
            }
        } else if(ball.x_speed <= 0) {
            ball.x_speed += sideV * (float) Math.sin(angle);
            if(ball.x_speed > 0) {
                ball.x_speed = 0;
            }
        }
        if(ball.y_speed > 0) {
            ball.y_speed -= sideV * (float) Math.cos(angle);
            if(ball.y_speed < 0) {
                ball.y_speed = 0;
            }
        } else if (ball.y_speed <= 0)  {
            ball.y_speed += sideV * (float) Math.cos(angle);
            if(ball.y_speed > 0) {
                ball.y_speed = 0;
            }
        }


    }

    void draw(Canvas canvas, Paint paint, Bitmap bitmap) {
        if (!active) {
            return;
        }
        Matrix m = new Matrix();
        m.setTranslate(x_left, y_top);
        m.preRotate(degrees);
        canvas.drawBitmap(bitmap, m, null);
    }

    Flow(float x_left, float y_top, float x_right, float y_bottom, float degrees, int imageId, float a, boolean active) {
        this.x_left = x_left;
        this.x_right = x_right;
        this.y_bottom = y_bottom;
        this.y_top = y_top;
        this.degrees = degrees;
        this.imageId = imageId;
        this.x = (x_left + x_right) / 2;
        this.y = (y_bottom + y_top) / 2;
        this.a = a;             
        this.v = a * 2;
        this.sideV = v / 3;
        this.active = active;

    }

    public Flow clone() throws CloneNotSupportedException{

        Flow newFlow = (Flow) super.clone();
        return newFlow;
    }
}
