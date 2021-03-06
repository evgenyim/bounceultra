package com.admin.bounceultra;

import static java.lang.Math.*;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;

class Ball extends GameObject implements Cloneable{
    float x;
    float y;
    float r;
    float x_speed;
    float y_speed;
    double k = 5e-2;
    double mu = 1 - 2e-2;
    double g = MainMenu.height * 1.0 / 1500;
    float decreas = (float) 0.2;
    float eps = (float) 1e-1;
    boolean stop;
    Vector velocity = new Vector(0,0);
    ArrayList<GameObject> inventory = new ArrayList<>();



    void shot(float to_x, float to_y) {
        x_speed += (to_x) * k;
        y_speed += (to_y) * k;
    }

    @Override
    void moveToXY(float newX, float newY) {
        x = newX;
        y = newY;
    }

    void draw(Canvas canvas, Paint paint, Bitmap bitmap) {
        canvas.drawCircle(x, y, r, paint);
    }

    Point centre() {
        Point p = new Point(x, y);
        return p;
    }

    void compress(float k) {
        r /= k;
    }

    void move(ArrayList<GameObject> ObjectList, boolean draft) {
        if (stop) {
            return;
        }
        velocity = new Vector(x_speed, y_speed);
        Segment bias = new Segment(x, y, x + velocity.x, y + velocity.y);
        Ball future_ball = new Ball(x + x_speed, y + y_speed, r);

        float min_d = 1000000;
        int intersected_seg_ind = -1;
        int intersected_obj_ind = -1;

        for (int i = 0; i < ObjectList.size(); i++) {
            ArrayList<Segment> segments = ObjectList.get(i).segments;
            for(int j = 0; j < segments.size(); j++) {
                if (segments.get(j).intersect_ball(future_ball) || Segment.segments_intersect(bias, segments.get(j))) {
                    if (Vector.dPointSegment(segments.get(j).a, segments.get(j).b, centre()) < min_d) {
                        min_d = Vector.dPointSegment(segments.get(j).a, segments.get(j).b, centre());
                        intersected_seg_ind = j;
                        intersected_obj_ind = i;
                    }
                }
            }
        }
        for (int i = 0; i < ObjectList.size(); i++) {
            if(inside(ObjectList.get(i)) && ObjectList.get(i).imageId != 2) {
                ObjectList.get(i).insideCommunicate(this, ObjectList, i, draft);
            }
        }

        Segment cur_seg;
        if (intersected_seg_ind != -1) {
            cur_seg = ObjectList.get(intersected_obj_ind).segments.get(intersected_seg_ind);
            cur_seg.comunicate(this, intersected_seg_ind, cur_seg, ObjectList, intersected_obj_ind, min_d, draft);
        } else {
            y_speed += g;
            x += x_speed;
            y += y_speed;
        }
    }

    void roll(Segment segment) {
        Vector horizon = new Vector(1, 0);
        Vector seg = new Vector(segment);
        float betta = Math.abs(Vector.good_angle(seg, horizon));
        float a = (float) (g * Math.sin(betta));
        Vector vel = new Vector(x_speed, y_speed);
        float alfa = Vector.good_angle(vel, seg);
        vel = Vector.rotate_by_angle(vel, alfa);
        float sin = seg.y / seg.length();
        float cos = seg.x / seg.length();
        Vector axi = new Vector(a * cos, a * sin);
        if (axi.y < 0) {
            axi.reverse();
        }
        vel.add(axi);
        if (vel.length() < eps) {
            vel.x = 0;
            vel.y = 0;
        }

        vel.x *= mu;
        vel.y *= mu;
        velocity.x = vel.x;
        velocity.y = vel.y;
    }

    boolean stick_to_segment(Segment segment) {
        Vector seg = new Vector(segment);
        Vector vel = new Vector(x_speed, y_speed);
        float alfa = Vector.good_angle(vel, seg);
        if (Math.abs(vel.length() * Math.sin(alfa)) <  1) {
            return true;
        } else {
            return false;
        }
    }

    void MindTheGap(Segment segment, float axil, float min_d) {
        if (velocity.x == 0 && velocity.y == 0) {
            return;
        }
        Segment bias = new Segment(x, y, x + velocity.x, y + velocity.y);
        Line n = new Line(bias);
        Line m = new Line(segment);
        Point A = Line.intersect(n, m);
        float l;
        float d;
        float new_x;
        float new_y;
        Point E;
        if (centre().dist(segment.a) < centre().dist(segment.b)) {
            E = segment.a;
        } else{
            E = segment.b;
        }
        if ((A == null)) {
            Vector normal = Segment.normal(segment);
            Line p = new Line(E, normal);
            Point B = Line.intersect(n, p);
            l = (float) sqrt((x - B.x) * (x - B.x) + (y - B.y) * (y - B.y));
            d = (float) sqrt(Math.abs(r * r - E.dist(B) * E.dist(B)));
            new_x = x + (B.x - x) * (l - d) / l;
            new_y = y + (B.y - y) * (l - d) / l;
        } else {
            Ball new_ball = new Ball(E.x, E.y, r);
            if (bias.intersect_ball(new_ball)) {
                Line normal = new Line(n.b / n.a, -1, E.y - n.b * E.x / n.a);
                Point inters = Line.intersect(normal, n);
                float OI = (float) sqrt(r * r - (E.x - inters.x) * (E.x - inters.x) - (E.y - inters.y) * (E.y - inters.y));
                Point Q = new Point(-n.c / n.a,0);
                Vector vect_OI = new Vector(inters, Q);
                vect_OI.unit();
                vect_OI.multiplying(OI);

                float new_x_1 = inters.x + vect_OI.x;
                float new_y_1 = inters.y + vect_OI.y;
                Point one = new Point(new_x_1, new_y_1);
                float dist_1 = one.dist(centre());
                float new_x_2 = inters.x - vect_OI.x;
                float new_y_2 = inters.y - vect_OI.y;
                Point two = new Point(new_x_2, new_y_2);
                float dist_2 = two.dist(centre());

                if (dist_1 < dist_2) {
                    new_x = new_x_1;
                    new_y = new_y_1;
                } else {
                    new_x = new_x_2;
                    new_y = new_y_2;
                }
            } else {
                float alfa = Segment.angle(bias, segment);
                l = (float) sqrt((x - A.x) * (x - A.x) + (y - A.y) * (y - A.y));
                d = (float) (r / sin(alfa));
                new_x = x + (A.x - x) * (l - d) / l;
                new_y = y + (A.y - y) * (l - d) / l;
            }
        }

        Segment real_bias = new Segment(x, y, new_x, new_y);
        if (bias.length() == 0) {
            axil = 1;
        } else {
            axil = real_bias.length() / bias.length();
        }
        x = new_x;
        y = new_y;
    }

    boolean inside (GameObject flow) {
        Point ballCenter = new Point(x, y);
        float x_left = flow.x_left;
        float y_top = flow.y_top;
        float x_right = flow.x_right;
        float y_bottom = flow.y_bottom;
        double degree = toRadians(flow.degrees);
        Point point_right_top = new Point(x_left + (x_right - x_left) * (float) cos(degree) - (y_top - y_top) * (float) sin(degree), y_top + (x_right - x_left) * (float) sin(degree) + (y_top - y_top) * (float) cos(degree));
        Point point_left_top = new Point(x_left, y_top);
        Point point_left_bottom = new Point(x_left + (x_left - x_left)  * (float) cos(degree) - (y_bottom - y_top) * (float) sin(degree), y_top + (x_left - x_left) * (float) sin(degree) + (y_bottom - y_top) * (float) cos(degree));
        Point point_right_bottom = new Point(x_left + (x_right - x_left) * (float) cos(degree) - (y_bottom - y_top) * (float) sin(degree), y_top + (x_right - x_left) * (float) sin(degree) + (y_bottom - y_top) * (float) cos(degree));
        Vector vec1 = new Vector(ballCenter, point_left_bottom);
        Vector vec2 = new Vector(ballCenter, point_left_top);
        Vector vec3 = new Vector(ballCenter, point_right_bottom);
        Vector vec4 = new Vector(ballCenter, point_right_top);
        float angleSum = vec1.bigOriantedAngle(vec2) + vec3.bigOriantedAngle(vec1) + vec4.bigOriantedAngle(vec3) + vec2.bigOriantedAngle(vec4);
        if(Math.abs(angleSum) >= Math.PI) {
            return true;
        } else {
            return false;
        }
    }

    Ball(Point p, float r) {
        this.x = p.x;
        this.y = p.y;
        this.r = r;
        this.x_speed = 0;
        this.y_speed = 0;
    }
    Ball(float x, float y, float r) {
        this.x = x;
        this.y = y;
        this.r = r;
    }

    public Ball clone() throws CloneNotSupportedException{

        Ball newBall = (Ball) super.clone();
        newBall.velocity = velocity.clone();
        return newBall;
    }

}
