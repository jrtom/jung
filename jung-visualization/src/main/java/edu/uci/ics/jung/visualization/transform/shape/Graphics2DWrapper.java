/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 11, 2005
 */

package edu.uci.ics.jung.visualization.transform.shape;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;


/**
 * a complete wrapping of Graphics2D, useful as a base class.
 * Contains no additional methods, other than direct calls
 * to the delegate.
 * 
 * @see GraphicsDecorator as an example subclass that
 * adds additional methods.
 * 
 * @author Tom Nelson 
 *
 *
 */
public class Graphics2DWrapper {
    
    protected Graphics2D delegate;
    
    public Graphics2DWrapper() {
        this(null);
    }
    public Graphics2DWrapper(Graphics2D delegate) {
        this.delegate = delegate;
    }
    
    public void setDelegate(Graphics2D delegate) {
        this.delegate = delegate;
    }
    
    public Graphics2D getDelegate() {
        return delegate;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#addRenderingHints(java.util.Map)
     */
    public void addRenderingHints(Map<?,?> hints) {
        delegate.addRenderingHints(hints);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#clearRect(int, int, int, int)
     */
    public void clearRect(int x, int y, int width, int height) {
        delegate.clearRect(x, y, width, height);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#clip(java.awt.Shape)
     */
    public void clip(Shape s) {
        delegate.clip(s);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#clipRect(int, int, int, int)
     */
    public void clipRect(int x, int y, int width, int height) {
        delegate.clipRect(x, y, width, height);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#copyArea(int, int, int, int, int, int)
     */
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        delegate.copyArea(x, y, width, height, dx, dy);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#create()
     */
    public Graphics create() {
        return delegate.create();
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#create(int, int, int, int)
     */
    public Graphics create(int x, int y, int width, int height) {
        return delegate.create(x, y, width, height);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#dispose()
     */
    public void dispose() {
        delegate.dispose();
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#draw(java.awt.Shape)
     */
    public void draw(Shape s) {
        delegate.draw(s);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#draw3DRect(int, int, int, int, boolean)
     */
    public void draw3DRect(int x, int y, int width, int height, boolean raised) {
        delegate.draw3DRect(x, y, width, height, raised);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawArc(int, int, int, int, int, int)
     */
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        delegate.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawBytes(byte[], int, int, int, int)
     */
    public void drawBytes(byte[] data, int offset, int length, int x, int y) {
        delegate.drawBytes(data, offset, length, x, y);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawChars(char[], int, int, int, int)
     */
    public void drawChars(char[] data, int offset, int length, int x, int y) {
        delegate.drawChars(data, offset, length, x, y);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawGlyphVector(java.awt.font.GlyphVector, float, float)
     */
    public void drawGlyphVector(GlyphVector g, float x, float y) {
        delegate.drawGlyphVector(g, x, y);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawImage(java.awt.image.BufferedImage, java.awt.image.BufferedImageOp, int, int)
     */
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        delegate.drawImage(img, op, x, y);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawImage(java.awt.Image, java.awt.geom.AffineTransform, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        return delegate.drawImage(img, xform, obs);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, java.awt.Color, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        return delegate.drawImage(img, x, y, bgcolor, observer);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return delegate.drawImage(img, x, y, observer);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, java.awt.Color, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        return delegate.drawImage(img, x, y, width, height, bgcolor, observer);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        return delegate.drawImage(img, x, y, width, height, observer);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.Color, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        return delegate.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        return delegate.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
    }
    
    /* (non-Javadoc)
     * @see java.awt.Graphics#drawLine(int, int, int, int)
     */
    public void drawLine(int x1, int y1, int x2, int y2) {
        delegate.drawLine(x1, y1, x2, y2);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawOval(int, int, int, int)
     */
    public void drawOval(int x, int y, int width, int height) {
        delegate.drawOval(x, y, width, height);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawPolygon(int[], int[], int)
     */
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        delegate.drawPolygon(xPoints, yPoints, nPoints);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawPolygon(java.awt.Polygon)
     */
    public void drawPolygon(Polygon p) {
        delegate.drawPolygon(p);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawPolyline(int[], int[], int)
     */
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        delegate.drawPolyline(xPoints, yPoints, nPoints);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawRect(int, int, int, int)
     */
    public void drawRect(int x, int y, int width, int height) {
        delegate.drawRect(x, y, width, height);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawRenderableImage(java.awt.image.renderable.RenderableImage, java.awt.geom.AffineTransform)
     */
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        delegate.drawRenderableImage(img, xform);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawRenderedImage(java.awt.image.RenderedImage, java.awt.geom.AffineTransform)
     */
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        delegate.drawRenderedImage(img, xform);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawRoundRect(int, int, int, int, int, int)
     */
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        delegate.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator, float, float)
     */
    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        delegate.drawString(iterator, x, y);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator, int, int)
     */
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        delegate.drawString(iterator, x, y);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawString(java.lang.String, float, float)
     */
    public void drawString(String s, float x, float y) {
        delegate.drawString(s, x, y);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawString(java.lang.String, int, int)
     */
    public void drawString(String str, int x, int y) {
        delegate.drawString(str, x, y);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#fill(java.awt.Shape)
     */
    public void fill(Shape s) {
        delegate.fill(s);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#fill3DRect(int, int, int, int, boolean)
     */
    public void fill3DRect(int x, int y, int width, int height, boolean raised) {
        delegate.fill3DRect(x, y, width, height, raised);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#fillArc(int, int, int, int, int, int)
     */
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        delegate.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#fillOval(int, int, int, int)
     */
    public void fillOval(int x, int y, int width, int height) {
        delegate.fillOval(x, y, width, height);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#fillPolygon(int[], int[], int)
     */
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        delegate.fillPolygon(xPoints, yPoints, nPoints);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#fillPolygon(java.awt.Polygon)
     */
    public void fillPolygon(Polygon p) {
        delegate.fillPolygon(p);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#fillRect(int, int, int, int)
     */
    public void fillRect(int x, int y, int width, int height) {
        delegate.fillRect(x, y, width, height);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#fillRoundRect(int, int, int, int, int, int)
     */
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        delegate.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#finalize()
     */
    public void finalize() {
        delegate.finalize();
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getBackground()
     */
    public Color getBackground() {
        return delegate.getBackground();
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#getClip()
     */
    public Shape getClip() {
        return delegate.getClip();
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#getClipBounds()
     */
    public Rectangle getClipBounds() {
        return delegate.getClipBounds();
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#getClipBounds(java.awt.Rectangle)
     */
    public Rectangle getClipBounds(Rectangle r) {
        return delegate.getClipBounds(r);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#getClipRect()
     */
    @SuppressWarnings("deprecation")
    public Rectangle getClipRect() {
        return delegate.getClipRect();
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#getColor()
     */
    public Color getColor() {
        return delegate.getColor();
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getComposite()
     */
    public Composite getComposite() {
        return delegate.getComposite();
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getDeviceConfiguration()
     */
    public GraphicsConfiguration getDeviceConfiguration() {
        return delegate.getDeviceConfiguration();
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#getFont()
     */
    public Font getFont() {
        return delegate.getFont();
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#getFontMetrics()
     */
    public FontMetrics getFontMetrics() {
        return delegate.getFontMetrics();
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#getFontMetrics(java.awt.Font)
     */
    public FontMetrics getFontMetrics(Font f) {
        return delegate.getFontMetrics(f);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getFontRenderContext()
     */
    public FontRenderContext getFontRenderContext() {
        return delegate.getFontRenderContext();
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getPaint()
     */
    public Paint getPaint() {
        return delegate.getPaint();
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getRenderingHint(java.awt.RenderingHints.Key)
     */
    public Object getRenderingHint(Key hintKey) {
        return delegate.getRenderingHint(hintKey);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getRenderingHints()
     */
    public RenderingHints getRenderingHints() {
        return delegate.getRenderingHints();
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getStroke()
     */
    public Stroke getStroke() {
        return delegate.getStroke();
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getTransform()
     */
    public AffineTransform getTransform() {
        return delegate.getTransform();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return delegate.hashCode();
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#hit(java.awt.Rectangle, java.awt.Shape, boolean)
     */
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        return delegate.hit(rect, s, onStroke);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#hitClip(int, int, int, int)
     */
    public boolean hitClip(int x, int y, int width, int height) {
        return delegate.hitClip(x, y, width, height);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#rotate(double, double, double)
     */
    public void rotate(double theta, double x, double y) {
        delegate.rotate(theta, x, y);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#rotate(double)
     */
    public void rotate(double theta) {
        delegate.rotate(theta);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#scale(double, double)
     */
    public void scale(double sx, double sy) {
        delegate.scale(sx, sy);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#setBackground(java.awt.Color)
     */
    public void setBackground(Color color) {
        delegate.setBackground(color);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#setClip(int, int, int, int)
     */
    public void setClip(int x, int y, int width, int height) {
        delegate.setClip(x, y, width, height);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#setClip(java.awt.Shape)
     */
    public void setClip(Shape clip) {
        delegate.setClip(clip);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#setColor(java.awt.Color)
     */
    public void setColor(Color c) {
        delegate.setColor(c);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#setComposite(java.awt.Composite)
     */
    public void setComposite(Composite comp) {
        delegate.setComposite(comp);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#setFont(java.awt.Font)
     */
    public void setFont(Font font) {
        delegate.setFont(font);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#setPaint(java.awt.Paint)
     */
    public void setPaint(Paint paint) {
        delegate.setPaint(paint);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#setPaintMode()
     */
    public void setPaintMode() {
        delegate.setPaintMode();
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#setRenderingHint(java.awt.RenderingHints.Key, java.lang.Object)
     */
    public void setRenderingHint(Key hintKey, Object hintValue) {
        delegate.setRenderingHint(hintKey, hintValue);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#setRenderingHints(java.util.Map)
     */
    public void setRenderingHints(Map<?,?> hints) {
        delegate.setRenderingHints(hints);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#setStroke(java.awt.Stroke)
     */
    public void setStroke(Stroke s) {
        delegate.setStroke(s);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#setTransform(java.awt.geom.AffineTransform)
     */
    public void setTransform(AffineTransform Tx) {
        delegate.setTransform(Tx);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#setXORMode(java.awt.Color)
     */
    public void setXORMode(Color c1) {
        delegate.setXORMode(c1);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#shear(double, double)
     */
    public void shear(double shx, double shy) {
        delegate.shear(shx, shy);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#toString()
     */
    public String toString() {
        return delegate.toString();
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#transform(java.awt.geom.AffineTransform)
     */
    public void transform(AffineTransform Tx) {
        delegate.transform(Tx);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#translate(double, double)
     */
    public void translate(double tx, double ty) {
        delegate.translate(tx, ty);
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#translate(int, int)
     */
    public void translate(int x, int y) {
        delegate.translate(x, y);
    }

}
