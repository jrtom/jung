package edu.uci.ics.jung.visualization;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.uci.ics.jung.visualization.transform.MutableAffineTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.transform.shape.ShapeTransformer;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;
import edu.uci.ics.jung.visualization.util.DefaultChangeEventSupport;

/**
 * A basic implementation of the MultiLayerTransformer interface that 
 * provides two Layers: VIEW and LAYOUT. It also provides ChangeEventSupport
 * @author Tom Nelson - tomnelson@dev.java.net
 *
 */
public class BasicTransformer implements MultiLayerTransformer, 
	ShapeTransformer, ChangeListener, ChangeEventSupport  {

    protected ChangeEventSupport changeSupport =
        new DefaultChangeEventSupport(this);

    protected MutableTransformer viewTransformer = 
        new MutableAffineTransformer(new AffineTransform());
    
    protected MutableTransformer layoutTransformer =
        new MutableAffineTransformer(new AffineTransform());

    /**
     * Creates an instance and notifies the view and layout Functions to listen to
     * changes published by this instance.
     */
    public BasicTransformer() {
		super();
		viewTransformer.addChangeListener(this);
		layoutTransformer.addChangeListener(this);
	}

    protected void setViewTransformer(MutableTransformer Function) {
        this.viewTransformer.removeChangeListener(this);
        this.viewTransformer = Function;
        this.viewTransformer.addChangeListener(this);
    }

    protected void setLayoutTransformer(MutableTransformer Function) {
        this.layoutTransformer.removeChangeListener(this);
        this.layoutTransformer = Function;
        this.layoutTransformer.addChangeListener(this);
    }


	protected MutableTransformer getLayoutTransformer() {
		return layoutTransformer;
	}

	protected MutableTransformer getViewTransformer() {
		return viewTransformer;
	}

	public Point2D inverseTransform(Point2D p) {
	    return inverseLayoutTransform(inverseViewTransform(p));
	}
	
	protected Point2D inverseViewTransform(Point2D p) {
	    return viewTransformer.inverseTransform(p);
	}

    protected Point2D inverseLayoutTransform(Point2D p) {
        return layoutTransformer.inverseTransform(p);
    }

	public Point2D transform(Point2D p) {
	    return viewTransform(layoutTransform(p));
	}
    
    protected Point2D viewTransform(Point2D p) {
        return viewTransformer.transform(p);
    }
    
    protected Point2D layoutTransform(Point2D p) {
        return layoutTransformer.transform(p);
    }
    
	public Shape inverseTransform(Shape shape) {
	    return inverseLayoutTransform(inverseViewTransform(shape));
	}
	
	protected Shape inverseViewTransform(Shape shape) {
	    return viewTransformer.inverseTransform(shape);
	}

    protected Shape inverseLayoutTransform(Shape shape) {
        return layoutTransformer.inverseTransform(shape);
    }

	public Shape transform(Shape shape) {
	    return viewTransform(layoutTransform(shape));
	}
    
    protected Shape viewTransform(Shape shape) {
        return viewTransformer.transform(shape);
    }
    
    protected Shape layoutTransform(Shape shape) {
        return layoutTransformer.transform(shape);
    }
    
    public void setToIdentity() {
    	layoutTransformer.setToIdentity();
    	viewTransformer.setToIdentity();
    }
    
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    public ChangeListener[] getChangeListeners() {
        return changeSupport.getChangeListeners();
    }

    public void fireStateChanged() {
        changeSupport.fireStateChanged();
    }   
    
	public void stateChanged(ChangeEvent e) {
	    fireStateChanged();
	}

	public MutableTransformer getTransformer(Layer layer) {
		if(layer == Layer.LAYOUT) return layoutTransformer;
		if(layer == Layer.VIEW) return viewTransformer;
		return null;
	}

	public Point2D inverseTransform(Layer layer, Point2D p) {
		if(layer == Layer.LAYOUT) return inverseLayoutTransform(p);
		if(layer == Layer.VIEW) return inverseViewTransform(p);
		return null;
	}

	public void setTransformer(Layer layer, MutableTransformer Function) {
		if(layer == Layer.LAYOUT) setLayoutTransformer(Function);
		if(layer == Layer.VIEW) setViewTransformer(Function);
		
	}

	public Point2D transform(Layer layer, Point2D p) {
		if(layer == Layer.LAYOUT) return layoutTransform(p);
		if(layer == Layer.VIEW) return viewTransform(p);
		return null;
	}

	public Shape transform(Layer layer, Shape shape) {
		if(layer == Layer.LAYOUT) return layoutTransform(shape);
		if(layer == Layer.VIEW) return viewTransform(shape);
		return null;
	}
	
	public Shape inverseTransform(Layer layer, Shape shape) {
		if(layer == Layer.LAYOUT) return inverseLayoutTransform(shape);
		if(layer == Layer.VIEW) return inverseViewTransform(shape);
		return null;
	}
}
