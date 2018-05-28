package com.admin.bounceultra;

import android.graphics.Canvas;
import android.graphics.Paint;

class Obstacle extends GameObject {

     float x_left;
     float y_top;
     float x_right;
     float y_bottom;

     Obstacle(float x_left,float y_top,float x_right,float y_bottom) {
         this.x_left = x_left;
         this.x_right = x_right;
         this.y_bottom = y_bottom;
         this.y_top = y_top;
     }

     boolean iftouchinside (float x, float y){
         if ((x <= x_right) && (x >= x_left) && (y >= y_bottom) && (y <= y_top)){
             return true;
         }
         return false;
     }

    void draw(Canvas canvas, Paint paint) {
        canvas.drawRect(x_left, y_top, x_right, y_bottom, paint);
    }

 }
