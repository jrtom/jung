/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on Jun 17, 2005
 */

package edu.uci.ics.jung.visualization;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Provides factory methods that, given a BufferedImage, an Image,
 * or the fileName of an image, will return a java.awt.Shape that
 * is the contiguous traced outline of the opaque part of the image.
 * This could be used to define an image for use in a Vertex, where
 * the shape used for picking and edge-arrow placement follows the
 * opaque part of an image that has a transparent background.
 * The methods try to detect lines in order to minimize points
 * in the path
 * 
 * @author Tom Nelson
 *
 * 
 */
public class FourPassImageShaper {
    
    /**
     * given the fileName of an image, possibly with a transparent
     * background, return the Shape of the opaque part of the image
     * @param fileName name of the image, loaded from the classpath
     * @return the Shape
     */
    public static Shape getShape(String fileName) {
        return getShape(fileName, Integer.MAX_VALUE);
    }
    public static Shape getShape(String fileName, int max) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(FourPassImageShaper.class.getResource(fileName));
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        return getShape(image, max);
    }
    
    /**
     * Given an image, possibly with a transparent background, return
     * the Shape of the opaque part of the image
     * @param image
     * @return the Shape
     */
    public static Shape getShape(Image image) {
        return getShape(image, Integer.MAX_VALUE);
    }
    public static Shape getShape(Image image, int max) {
        BufferedImage bi = 
            new BufferedImage(image.getWidth(null), image.getHeight(null), 
                    BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return getShape(bi, max);
    }
    
    /**
     * Given an image, possibly with a transparent background, return
     * the Shape of the opaque part of the image
     * 
     * If the image is larger than max in either direction, scale the
     * image down to max-by-max, do the trace (on fewer points) then
     * scale the resulting shape back up to the size of the original
     * image.
     * 
     * @param image the image to trace
     * @param max used to restrict number of points in the resulting shape
     * @return the Shape
     */
    public static Shape getShape(BufferedImage image, int max) {
        
        float width = image.getWidth();
        float height = image.getHeight();
        if(width > max || height > max) {
            BufferedImage smaller = 
                new BufferedImage(max, max, BufferedImage.TYPE_INT_ARGB);
            Graphics g = smaller.createGraphics();
            AffineTransform at = AffineTransform.getScaleInstance(max/width,max/height);
            AffineTransform back = AffineTransform.getScaleInstance(width/max,height/max);
            Graphics2D g2 = (Graphics2D)g;
            g2.drawImage(image, at, null);
            g2.dispose();
            return back.createTransformedShape(getShape(smaller));
        } else {
            return getShape(image);
        }
    }
    
    public static Shape getShape(BufferedImage image) {
        Area area = new Area(leftEdge(image));
        area.intersect(new Area(bottomEdge(image)));
        area.intersect(new Area(rightEdge(image)));
        area.intersect(new Area(topEdge(image)));
        return area;
    }
    /**
     * Checks to see if point p is on a line that passes thru
     * points p1 and p2. If p is on the line, extend the line
     * segment so that it is from p1 to the location of p.
     * If the point p is not on the line, update my shape
     * with a line extending to the old p2 location, make
     * the old p2 the new p1, and make p2 the old p
     * @param p1
     * @param p2
     * @param p
     * @param line
     * @param path
     * @return
     */
    private static Point2D detectLine(Point2D p1, Point2D p2, Point2D p, 
            Line2D line, GeneralPath path) {
        if(p2 == null) {
            p2 = p;
            line.setLine(p1,p2);
        }
        // check for line
        else if(line.ptLineDistSq(p) < 1) { // its on the line
            // make it p2
            p2.setLocation(p);
        } else { // its not on the current line
            p1.setLocation(p2);
            p2.setLocation(p);
            line.setLine(p1,p2);
            path.lineTo((float)p1.getX(), (float)p1.getY());
        }
        return p2;
    }
    /**
     * trace the left side of the image
     * @param image
     * @param path
     * @return
     */
    private static Shape leftEdge(BufferedImage image) {
        GeneralPath path = new GeneralPath();
        Point2D p1 = new Point2D.Float(image.getWidth()-1, 0);
        Point2D p2 = null;
        Line2D line = new Line2D.Float();
        Point2D p = new Point2D.Float();
        path.moveTo(image.getWidth()-1, 0);
        
        for(int i=0; i<image.getHeight(); i++) {
            p.setLocation(image.getWidth()-1, i);
            // go until we reach an opaque point, then stop
            for(int j=0; j<image.getWidth(); j++) {
                if((image.getRGB(j,i) & 0xff000000) != 0) {
                    // this is a point I want
                    p.setLocation(j,i);
                    break;
                }
            }
            p2 = detectLine(p1, p2, p, line, path);
        }
        p.setLocation(image.getWidth()-1, image.getHeight()-1);
        detectLine(p1, p2, p, line, path);
        path.closePath();
        return path;
    }
    
    /**
     * trace the bottom of the image
     * @param image
     * @param path
     * @param start
     * @return
     */
    private static Shape bottomEdge(BufferedImage image) {
        GeneralPath path = new GeneralPath();
        Point2D p1 = new Point2D.Float(0, 0);
        Point2D p2 = null;
        Line2D line = new Line2D.Float();
        Point2D p = new Point2D.Float();
        path.moveTo(0, 0);
        for(int i=0; i<image.getWidth(); i++) {
            p.setLocation(i, 0);
            for(int j=image.getHeight()-1; j>=0; j--) {
                if((image.getRGB(i,j) & 0xff000000) != 0) {
                    // this is a point I want
                    p.setLocation(i,j);
                    break;
                }
            }
            p2 = detectLine(p1, p2, p, line, path);
        }
        p.setLocation(image.getWidth()-1, 0);
        detectLine(p1, p2, p, line, path);
        path.closePath();
        return path;
    }
    
    /**
     * trace the right side of the image
     * @param image
     * @param path
     * @param start
     * @return
     */
    private static Shape rightEdge(BufferedImage image) {
        GeneralPath path = new GeneralPath();
        Point2D p1 = new Point2D.Float(0, image.getHeight()-1);
        Point2D p2 = null;
        Line2D line = new Line2D.Float();
        Point2D p = new Point2D.Float();
        path.moveTo(0, image.getHeight()-1);
        
        for(int i=image.getHeight()-1; i>=0; i--) {
            p.setLocation(0, i);
            for(int j=image.getWidth()-1; j>=0; j--) {
                if((image.getRGB(j,i) & 0xff000000) != 0) {
                    // this is a point I want
                    p.setLocation(j,i);
                    break;
                }
            }
            p2 = detectLine(p1, p2, p, line, path);
        }
        p.setLocation(0, 0);
        detectLine(p1, p2, p,line, path);
        path.closePath();
        return path;
    }
    
    /**
     * trace the top of the image
     * @param image
     * @param path
     * @param start
     * @return
     */
    private static Shape topEdge(BufferedImage image) {
        GeneralPath path = new GeneralPath();
        Point2D p1 = new Point2D.Float(image.getWidth()-1, image.getHeight()-1);
        Point2D p2 = null;
        Line2D line = new Line2D.Float();
        Point2D p = new Point2D.Float();
        path.moveTo(image.getWidth()-1, image.getHeight()-1);
        
        for(int i=image.getWidth()-1; i>=0; i--) {
            p.setLocation(i, image.getHeight()-1);
            for(int j=0; j<image.getHeight(); j++) {
                if((image.getRGB(i,j) & 0xff000000) != 0) {
                    // this is a point I want
                    p.setLocation(i,j);
                    break;
                }
            }
            p2 = detectLine(p1, p2, p, line, path);
        }
        p.setLocation(0, image.getHeight()-1);
        detectLine(p1, p2, p, line, path);
        path.closePath();
        return path;
    }
}
