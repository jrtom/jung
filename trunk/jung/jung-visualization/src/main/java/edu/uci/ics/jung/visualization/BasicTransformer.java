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
     * Creates an instance and notifies the view and layout transformers to listen to
     * changes published by this instance.
     */
    public BasicTransformer() {
		super();
		viewTransformer.addChangeListener(this);
		layoutTransformer.addChangeListener(this);
	}

    /**
	 * @see edu.uci.ics.jung.visualization.MultiLayerTransformer#setViewTransformer(edu.uci.ics.jung.visualization.transform.MutableTransformer)
	 */
    protected void setViewTransformer(MutableTransformer transformer) {
        this.viewTransformer.removeChangeListener(this);
        this.viewTransformer = transformer;
        this.viewTransformer.addChangeListener(this);
    }

    /* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.MultiLayerTransformer#setLayoutTransformer(edu.uci.ics.jung.visualization.transform.MutableTransformer)
	 */
    protected void setLayoutTransformer(MutableTransformer transformer) {
        this.layoutTransformer.removeChangeListener(this);
        this.layoutTransformer = transformer;
        this.layoutTransformer.addChangeListener(this);
    }


	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.MultiLayerTransformer#getLayoutTransformer()
	 */
	protected MutableTransformer getLayoutTransformer() {
		return layoutTransformer;
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.MultiLayerTransformer#getViewTransformer()
	 */
	protected MutableTransformer getViewTransformer() {
		return viewTransformer;
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.MultiLayerTransformer#inverseTransform(java.awt.geom.Point2D)
	 */
	public Point2D inverseTransform(Point2D p) {
	    return inverseLayoutTransform(inverseViewTransform(p));
	}
	
	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.MultiLayerTransformer#inverseViewTransform(java.awt.geom.Point2D)
	 */
	protected Point2D inverseViewTransform(Point2D p) {
	    return viewTransformer.inverseTransform(p);
	}

    /* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.MultiLayerTransformer#inverseLayoutTransform(java.awt.geom.Point2D)
	 */
    protected Point2D inverseLayoutTransform(Point2D p) {
        return layoutTransformer.inverseTransform(p);
    }

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.MultiLayerTransformer#transform(java.awt.geom.Point2D)
	 */
	public Point2D transform(Point2D p) {
	    return viewTransform(layoutTransform(p));
	}
    
    /* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.MultiLayerTransformer#viewTransform(java.awt.geom.Point2D)
	 */
    protected Point2D viewTransform(Point2D p) {
        return viewTransformer.transform(p);
    }
    
    /* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.MultiLayerTransformer#layoutTransform(java.awt.geom.Point2D)
	 */
    protected Point2D layoutTransform(Point2D p) {
        return layoutTransformer.transform(p);
    }
    
	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.MultiLayerTransformer#inverseTransform(java.awt.Shape)
	 */
	public Shape inverseTransform(Shape shape) {
	    return inverseLayoutTransform(inverseViewTransform(shape));
	}
	
	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.MultiLayerTransformer#inverseViewTransform(java.awt.Shape)
	 */
	protected Shape inverseViewTransform(Shape shape) {
	    return viewTransformer.inverseTransform(shape);
	}

    /* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.MultiLayerTransformer#inverseLayoutTransform(java.awt.Shape)
	 */
    protected Shape inverseLayoutTransform(Shape shape) {
        return layoutTransformer.inverseTransform(shape);
    }

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.MultiLayerTransformer#transform(java.awt.Shape)
	 */
	public Shape transform(Shape shape) {
	    return viewTransform(layoutTransform(shape));
	}
    
    /* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.MultiLayerTransformer#viewTransform(java.awt.Shape)
	 */
    protected Shape viewTransform(Shape shape) {
        return viewTransformer.transform(shape);
    }
    
    /* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.MultiLayerTransformer#layoutTransform(java.awt.Shape)
	 */
    protected Shape layoutTransform(Shape shape) {
        return layoutTransformer.transform(shape);
    }
    
    /* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.MultiLayerTransformer#setToIdentity()
	 */
    public void setToIdentity() {
    	layoutTransformer.setToIdentity();
    	viewTransformer.setToIdentity();
    }
    
    /* (non-Javadoc)
     */
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    /* (non-Javadoc)
     */
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    /* (non-Javadoc)
     */
    public ChangeListener[] getChangeListeners() {
        return changeSupport.getChangeListeners();
    }

    /* (non-Javadoc)
     */
    public void fireStateChanged() {
        changeSupport.fireStateChanged();
    }   
    
	/* (non-Javadoc)
     */
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

	public void setTransformer(Layer layer, MutableTransformer transformer) {
		if(layer == Layer.LAYOUT) setLayoutTransformer(transformer);
		if(layer == Layer.VIEW) setViewTransformer(transformer);
		
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
