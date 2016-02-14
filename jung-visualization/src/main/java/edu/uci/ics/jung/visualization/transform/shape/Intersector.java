/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * 
 */
package edu.uci.ics.jung.visualization.transform.shape;

import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

public class Intersector {
    
    protected Rectangle rectangle;
    Line2D line;
    Set<Point2D> points = new HashSet<Point2D>();

    public Intersector(Rectangle rectangle) {
        this.rectangle = rectangle;
    }

    public Intersector(Rectangle rectangle, Line2D line) {
        this.rectangle = rectangle;
       intersectLine(line);
    }
    
    public void intersectLine(Line2D line) {
        this.line = line;
        points.clear();
        float rx0 = (float) rectangle.getMinX();
        float ry0 = (float) rectangle.getMinY();
        float rx1 = (float) rectangle.getMaxX();
        float ry1 = (float) rectangle.getMaxY();
        
        float x1 = (float) line.getX1();
        float y1 = (float) line.getY1();
        float x2 = (float) line.getX2();
        float y2 = (float) line.getY2();
        
        float dy = y2 - y1;
        float dx = x2 - x1;
        
        if(dx != 0) {
            float m = dy/dx;
            float b = y1 - m*x1;
            
            // base of rect where y == ry0
            float x = (ry0 - b) / m;
            
            if(rx0 <= x && x <= rx1) {
                points.add(new Point2D.Float(x, ry0));
            }
            
            // top where y == ry1
            x = (ry1 - b) / m;
            if(rx0 <= x && x <= rx1) {
                points.add(new Point2D.Float(x, ry1));
            }
            
            // left side, where x == rx0
            float y = m * rx0 + b;
            if(ry0 <= y && y <= ry1) {
                points.add(new Point2D.Float(rx0, y));
            }
            
            
            // right side, where x == rx1
            y = m * rx1 + b;
            if(ry0 <= y && y <= ry1) {
                points.add(new Point2D.Float(rx1, y));
            }
            
        } else {
            
            // base, where y == ry0
            float x = x1;
            if(rx0 <= x && x <= rx1) {
                points.add(new Point2D.Float(x, ry0));
            }
            
            // top, where y == ry1
            x = x2;
            if(rx0 <= x && x <= rx1) {
                points.add(new Point2D.Float(x, ry1));
            }
        }
    }
    public Line2D getLine() {
        return line;
    }
    public Set<Point2D> getPoints() {
        return points;
    }
    public Rectangle getRectangle() {
        return rectangle;
    }

    public String toString() {
        return "Rectangle: "+rectangle+", points:"+points;
    }
    
    public static void main(String[] args) {
        Rectangle rectangle = new Rectangle(0,0,10,10);
        Line2D line = new Line2D.Float(4,4,5,5);
        System.err.println(""+new Intersector(rectangle, line));
        System.err.println(""+new Intersector(rectangle, new Line2D.Float(9,11,11,9)));
        System.err.println(""+new Intersector(rectangle, new Line2D.Float(1,1,3,2)));
        System.err.println(""+new Intersector(rectangle, new Line2D.Float(4,6,6,4)));
    }

}
