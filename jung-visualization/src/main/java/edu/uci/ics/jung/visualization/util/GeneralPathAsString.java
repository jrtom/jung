package edu.uci.ics.jung.visualization.util;

import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

public class GeneralPathAsString {

    public static String toString(GeneralPath newPath) {
    	StringBuilder sb = new StringBuilder();
        float[] coords = new float[6];
        for(PathIterator iterator=newPath.getPathIterator(null);
            iterator.isDone() == false;
            iterator.next()) {
            int type = iterator.currentSegment(coords);
            switch(type) {
            case PathIterator.SEG_MOVETO:
                Point2D p = new Point2D.Float(coords[0], coords[1]);
                sb.append("moveTo "+p+"--");
                break;
                
            case PathIterator.SEG_LINETO:
                p = new Point2D.Float(coords[0], coords[1]);
                sb.append("lineTo "+p+"--");
                break;
                
            case PathIterator.SEG_QUADTO:
                p = new Point2D.Float(coords[0], coords[1]);
                Point2D q = new Point2D.Float(coords[2], coords[3]);
                sb.append("quadTo "+p+" controlled by "+q);
                break;
                
            case PathIterator.SEG_CUBICTO:
                p = new Point2D.Float(coords[0], coords[1]);
                q = new Point2D.Float(coords[2], coords[3]);
                Point2D r = new Point2D.Float(coords[4], coords[5]);
                sb.append("cubeTo "+p+" controlled by "+q+","+r);

                break;
                
            case PathIterator.SEG_CLOSE:
                newPath.closePath();
                sb.append("close");
                break;
                    
            }
        }
        return sb.toString();
    }

}
