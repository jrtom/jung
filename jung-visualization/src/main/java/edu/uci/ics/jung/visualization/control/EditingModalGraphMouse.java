package edu.uci.ics.jung.visualization.control;

import edu.uci.ics.jung.visualization.MultiLayerTransformer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.annotations.AnnotatingGraphMousePlugin;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.ItemSelectable;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Supplier;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.plaf.basic.BasicIconFactory;

public class EditingModalGraphMouse<V, E> extends AbstractModalGraphMouse
    implements ModalGraphMouse, ItemSelectable {

  protected Supplier<V> vertexFactory;
  protected Supplier<E> edgeFactory;
  protected EditingGraphMousePlugin<V, E> editingPlugin;
  protected LabelEditingGraphMousePlugin<V, E> labelEditingPlugin;
  protected EditingPopupGraphMousePlugin<V, E> popupEditingPlugin;
  protected AnnotatingGraphMousePlugin<V, E> annotatingPlugin;
  protected MultiLayerTransformer basicTransformer;
  protected RenderContext<V, E> rc;

  /**
   * Creates an instance with the specified rendering context and vertex/edge factories, and with
   * default zoom in/out values of 1.1 and 1/1.1.
   *
   * @param rc the rendering context
   * @param vertexFactory used to construct vertices
   * @param edgeFactory used to construct edges
   */
  public EditingModalGraphMouse(
      RenderContext<V, E> rc, Supplier<V> vertexFactory, Supplier<E> edgeFactory) {
    this(rc, vertexFactory, edgeFactory, 1.1f, 1 / 1.1f);
  }

  /**
   * Creates an instance with the specified rendering context and vertex/edge factories, and with
   * the specified zoom in/out values.
   *
   * @param rc the rendering context
   * @param vertexFactory used to construct vertices
   * @param edgeFactory used to construct edges
   * @param in amount to zoom in by for each action
   * @param out amount to zoom out by for each action
   */
  public EditingModalGraphMouse(
      RenderContext<V, E> rc,
      Supplier<V> vertexFactory,
      Supplier<E> edgeFactory,
      float in,
      float out) {
    super(in, out);
    this.vertexFactory = vertexFactory;
    this.edgeFactory = edgeFactory;
    this.rc = rc;
    this.basicTransformer = rc.getMultiLayerTransformer();
    loadPlugins();
    setModeKeyListener(new ModeKeyAdapter(this));
  }

  /** create the plugins, and load the plugins for TRANSFORMING mode */
  @Override
  protected void loadPlugins() {
    pickingPlugin = new PickingGraphMousePlugin<V, E>();
    animatedPickingPlugin = new AnimatedPickingGraphMousePlugin<V, E>();
    translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
    scalingPlugin = new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, in, out);
    rotatingPlugin = new RotatingGraphMousePlugin();
    shearingPlugin = new ShearingGraphMousePlugin();
    editingPlugin = new EditingGraphMousePlugin<V, E>(vertexFactory, edgeFactory);
    labelEditingPlugin = new LabelEditingGraphMousePlugin<V, E>();
    annotatingPlugin = new AnnotatingGraphMousePlugin<V, E>(rc);
    popupEditingPlugin = new EditingPopupGraphMousePlugin<V, E>(vertexFactory, edgeFactory);
    add(scalingPlugin);
    setMode(Mode.EDITING);
  }

  /** setter for the Mode. */
  @Override
  public void setMode(Mode mode) {
    if (this.mode != mode) {
      fireItemStateChanged(
          new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this.mode, ItemEvent.DESELECTED));
      this.mode = mode;
      if (mode == Mode.TRANSFORMING) {
        setTransformingMode();
      } else if (mode == Mode.PICKING) {
        setPickingMode();
      } else if (mode == Mode.EDITING) {
        setEditingMode();
      } else if (mode == Mode.ANNOTATING) {
        setAnnotatingMode();
      }
      if (modeBox != null) {
        modeBox.setSelectedItem(mode);
      }
      fireItemStateChanged(
          new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, mode, ItemEvent.SELECTED));
    }
  }

  @Override
  protected void setPickingMode() {
    remove(translatingPlugin);
    remove(rotatingPlugin);
    remove(shearingPlugin);
    remove(editingPlugin);
    remove(annotatingPlugin);
    add(pickingPlugin);
    add(animatedPickingPlugin);
    add(labelEditingPlugin);
    add(popupEditingPlugin);
  }

  @Override
  protected void setTransformingMode() {
    remove(pickingPlugin);
    remove(animatedPickingPlugin);
    remove(editingPlugin);
    remove(annotatingPlugin);
    add(translatingPlugin);
    add(rotatingPlugin);
    add(shearingPlugin);
    add(labelEditingPlugin);
    add(popupEditingPlugin);
  }

  protected void setEditingMode() {
    remove(pickingPlugin);
    remove(animatedPickingPlugin);
    remove(translatingPlugin);
    remove(rotatingPlugin);
    remove(shearingPlugin);
    remove(labelEditingPlugin);
    remove(annotatingPlugin);
    add(editingPlugin);
    add(popupEditingPlugin);
  }

  protected void setAnnotatingMode() {
    remove(pickingPlugin);
    remove(animatedPickingPlugin);
    remove(translatingPlugin);
    remove(rotatingPlugin);
    remove(shearingPlugin);
    remove(labelEditingPlugin);
    remove(editingPlugin);
    remove(popupEditingPlugin);
    add(annotatingPlugin);
  }

  /** @return the modeBox. */
  @Override
  public JComboBox<Mode> getModeComboBox() {
    if (modeBox == null) {
      modeBox =
          new JComboBox<Mode>(
              new Mode[] {Mode.TRANSFORMING, Mode.PICKING, Mode.EDITING, Mode.ANNOTATING});
      modeBox.addItemListener(getModeListener());
    }
    modeBox.setSelectedItem(mode);
    return modeBox;
  }

  /**
   * create (if necessary) and return a menu that will change the mode
   *
   * @return the menu
   */
  @Override
  public JMenu getModeMenu() {
    if (modeMenu == null) {
      modeMenu = new JMenu(); // {
      Icon icon = BasicIconFactory.getMenuArrowIcon();
      modeMenu.setIcon(BasicIconFactory.getMenuArrowIcon());
      modeMenu.setPreferredSize(new Dimension(icon.getIconWidth() + 10, icon.getIconHeight() + 10));

      final JRadioButtonMenuItem transformingButton =
          new JRadioButtonMenuItem(Mode.TRANSFORMING.toString());
      transformingButton.addItemListener(
          new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
              if (e.getStateChange() == ItemEvent.SELECTED) {
                setMode(Mode.TRANSFORMING);
              }
            }
          });

      final JRadioButtonMenuItem pickingButton = new JRadioButtonMenuItem(Mode.PICKING.toString());
      pickingButton.addItemListener(
          new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
              if (e.getStateChange() == ItemEvent.SELECTED) {
                setMode(Mode.PICKING);
              }
            }
          });

      final JRadioButtonMenuItem editingButton = new JRadioButtonMenuItem(Mode.EDITING.toString());
      editingButton.addItemListener(
          new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
              if (e.getStateChange() == ItemEvent.SELECTED) {
                setMode(Mode.EDITING);
              }
            }
          });

      ButtonGroup radio = new ButtonGroup();
      radio.add(transformingButton);
      radio.add(pickingButton);
      radio.add(editingButton);
      transformingButton.setSelected(true);
      modeMenu.add(transformingButton);
      modeMenu.add(pickingButton);
      modeMenu.add(editingButton);
      modeMenu.setToolTipText("Menu for setting Mouse Mode");
      addItemListener(
          new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
              if (e.getStateChange() == ItemEvent.SELECTED) {
                if (e.getItem() == Mode.TRANSFORMING) {
                  transformingButton.setSelected(true);
                } else if (e.getItem() == Mode.PICKING) {
                  pickingButton.setSelected(true);
                } else if (e.getItem() == Mode.EDITING) {
                  editingButton.setSelected(true);
                }
              }
            }
          });
    }
    return modeMenu;
  }

  public static class ModeKeyAdapter extends KeyAdapter {
    private char t = 't';
    private char p = 'p';
    private char e = 'e';
    private char a = 'a';
    protected ModalGraphMouse graphMouse;

    public ModeKeyAdapter(ModalGraphMouse graphMouse) {
      this.graphMouse = graphMouse;
    }

    public ModeKeyAdapter(char t, char p, char e, char a, ModalGraphMouse graphMouse) {
      this.t = t;
      this.p = p;
      this.e = e;
      this.a = a;
      this.graphMouse = graphMouse;
    }

    @Override
    public void keyTyped(KeyEvent event) {
      char keyChar = event.getKeyChar();
      if (keyChar == t) {
        ((Component) event.getSource())
            .setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        graphMouse.setMode(Mode.TRANSFORMING);
      } else if (keyChar == p) {
        ((Component) event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        graphMouse.setMode(Mode.PICKING);
      } else if (keyChar == e) {
        ((Component) event.getSource())
            .setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        graphMouse.setMode(Mode.EDITING);
      } else if (keyChar == a) {
        ((Component) event.getSource())
            .setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        graphMouse.setMode(Mode.ANNOTATING);
      }
    }
  }

  /** @return the annotatingPlugin */
  public AnnotatingGraphMousePlugin<V, E> getAnnotatingPlugin() {
    return annotatingPlugin;
  }

  /** @return the editingPlugin */
  public EditingGraphMousePlugin<V, E> getEditingPlugin() {
    return editingPlugin;
  }

  /** @return the labelEditingPlugin */
  public LabelEditingGraphMousePlugin<V, E> getLabelEditingPlugin() {
    return labelEditingPlugin;
  }

  /** @return the popupEditingPlugin */
  public EditingPopupGraphMousePlugin<V, E> getPopupEditingPlugin() {
    return popupEditingPlugin;
  }
}
