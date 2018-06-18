package com.admin.bounceultra;

import static java.lang.Math.*;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;

class Ball extends GameObject {
    float x;
    float y;
    float r;
    float x_speed;
    float y_speed;
    double k = 5e-2;
    double mu = 1 - 2e-2;
    double g = MainActivity.height * 1.0 / 1500;
    float decreas = (float) 0.2;
    float eps = (float) 1e-1;
    Vector velocity;


    void shot(float to_x, float to_y) {
        x_speed += (to_x) * k;
        y_speed += (to_y) * k;
    }

    void draw(Canvas canvas, Paint paint) {
        canvas.drawCircle(x, y, r, paint);
    }

    Point centre() {
        Point p = new Point(x, y);
        return p;
    }

    void move(ArrayList<GameObject> ObjectList) {

        velocity = new Vector(x_speed, y_speed);
        Segment bias = new Segment(x, y, x + velocity.x, y + velocity.y);
        Ball future_ball = new Ball(x + x_speed, y + y_speed, r);
        Room room;
        ArrayList<GameObject> cur_room = MainActivity.RoomList.get(MainActivity.current_room).ObjectList;

        float min_d = 1000000;
        int intersected_seg_ind = -1;
        int intersected_obst_ind = -1;

        for (int i = 0; i < ObjectList.size(); i++) {
            ArrayList<Segment> segments = ObjectList.get(i).segments;
            for(int j = 0; j < segments.size(); j++) {
                if (segments.get(j).intersect_ball(future_ball) || Segment.segments_intersect(bias, segments.get(j))) {
                    if (Vector.dPointSegment(segments.get(j).a, segments.get(j).b, centre()) < min_d) {
                        min_d = Vector.dPointSegment(segments.get(j).a, segments.get(j).b, centre());
                        intersected_seg_ind = j;
                        intersected_obst_ind = i;
                    }
                }
            }
        }

        Segment cur_seg = null;
        if (intersected_seg_ind != -1) {
            cur_seg = ObjectList.get(intersected_obst_ind).segments.get(intersected_seg_ind);
        }
        if (intersected_seg_ind != - 1 && !stick_to_segment(cur_seg)) {
            ArrayList<Segment> segments = ObjectList.get(intersected_obst_ind).segments;
            float axil = 1;
            MindTheGap(segments.get(intersected_seg_ind), axil);
            axil *= g;
            min_d = Vector.dPointSegment(segments.get(intersected_seg_ind).a, segments.get(intersected_seg_ind).b, centre());
            Vector segment = new Vector(segments.get(intersected_seg_ind));
            if (min_d  == centre().dist(segments.get(intersected_seg_ind).a) || min_d  == centre().dist(segments.get(intersected_seg_ind).b)) {
                Vector new_velocity;
                if (min_d  == centre().dist(segments.get(intersected_seg_ind).a)) {
                    new_velocity = new Vector(segments.get(intersected_seg_ind).a, centre());
                } else {
                    new_velocity = new Vector(segments.get(intersected_seg_ind).b, centre());
                }
                new_velocity.unit();
                new_velocity.multiplying(velocity.length());
                velocity = new_velocity;
            } else {
                if (!velocity.if_null()) {
                    velocity = Vector.reflect(velocity, segment);
                }
            }
            velocity.y = (float) (velocity.y - g + axil);
            x_speed = velocity.x * (1 - decreas);
            y_speed = velocity.y * (1 - decreas);
            return;
        }

        if (intersected_seg_ind != - 1 && stick_to_segment(cur_seg)) {
            roll(cur_seg);
            x_speed = velocity.x;
            y_speed = velocity.y;
            x += x_speed;
            y += y_speed;
            return;
        }

        y_speed += g;
        x += x_speed;
        y += y_speed;

        for (int i = 0; i < cur_room.size(); i++) {
            if(cur_room.get(i).id == 3) {
                Segment main_segment = cur_room.get(i).main_segment;
                if (Segment.segments_intersect(bias, main_segment) || main_segment.intersect_ball(future_ball)) {
                    room = MainActivity.RoomList.get(cur_room.get(i).next_room_id);
                    room.ball.y_speed = this.y_speed;
                    room.ball.x_speed = this.x_speed;
                    room.ball.velocity = this.velocity;
                    for (int j = 0; j < room.ObjectList.size(); j++) {
                        if (room.ObjectList.get(j).id == 3 && room.ObjectList.get(j).next_room_id == MainActivity.current_room) {
                            room.ball.x = room.ObjectList.get(j).x;
                            room.ball.y = room.ObjectList.get(j).y;
                        }
                    }
                    MainActivity.current_room = cur_room.get(i).next_room_id;
                    return;
                }
            }
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

    void MindTheGap(Segment segment, float axil) {
        if (velocity.x == 0 && velocity.y == 0) {
            return;
        };
        Segment bias = new Segment(x, y, x + velocity.x, y + velocity.y);
        Line n = new Line(bias);
        Line m = new Line(segment);
        Point A = Line.intersect(n, m);
        float l;
        float d;
        float new_x;
        float new_y;
        if (A == null) {
            Vector normal = Segment.normal(segment);
            Point E;
            if (centre().dist(segment.a) < centre().dist(segment.b)) {
                E = segment.a;
            } else{
                E = segment.b;
            }
            Line p = new Line(E, normal);
            Point B = Line.intersect(n, p);
            l = (float) sqrt((x - B.x) * (x - B.x) + (y - B.y) * (y - B.y));
            d = (float) sqrt(Math.abs(r * r - E.dist(B) * E.dist(B)));
            new_x = x + (B.x - x) * (l - d) / l;
            new_y = y + (B.y - y) * (l - d) / l;
        } else {
            float alfa = Segment.angle(bias, segment);
            l = (float) sqrt((x - A.x) * (x - A.x) + (y - A.y) * (y - A.y));
            d = (float) (r / sin(alfa));
            new_x = x + (A.x - x) * (l - d) / l;
            new_y = y + (A.y - y) * (l - d) / l;
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
}
