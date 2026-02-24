package edu.uci.ics.jung.visualization.control

import edu.uci.ics.jung.visualization.MultiLayerTransformer
import edu.uci.ics.jung.visualization.RenderContext
import edu.uci.ics.jung.visualization.annotations.AnnotatingGraphMousePlugin
import java.awt.Component
import java.awt.Cursor
import java.awt.Dimension
import java.awt.ItemSelectable
import java.awt.event.InputEvent
import java.awt.event.ItemEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.function.Supplier
import javax.swing.ButtonGroup
import javax.swing.JComboBox
import javax.swing.JMenu
import javax.swing.JRadioButtonMenuItem
import javax.swing.plaf.basic.BasicIconFactory

open class EditingModalGraphMouse<N : Any, E : Any> : AbstractModalGraphMouse, ModalGraphMouse, ItemSelectable {

    protected var nodeFactory: Supplier<N>
    protected var edgeFactory: Supplier<E>
    protected lateinit var _editingPlugin: EditingGraphMousePlugin<N, E>
    protected lateinit var _labelEditingPlugin: LabelEditingGraphMousePlugin<N, E>
    protected lateinit var _popupEditingPlugin: EditingPopupGraphMousePlugin<N, E>
    protected lateinit var _annotatingPlugin: AnnotatingGraphMousePlugin<N, E>
    protected var basicTransformer: MultiLayerTransformer
    protected var rc: RenderContext<N, E>

    /**
     * Creates an instance with the specified rendering context and node/edge factories, and with
     * default zoom in/out values of 1.1 and 1/1.1.
     *
     * @param rc the rendering context
     * @param nodeFactory used to construct nodes
     * @param edgeFactory used to construct edges
     */
    constructor(
        rc: RenderContext<N, E>,
        nodeFactory: Supplier<N>,
        edgeFactory: Supplier<E>
    ) : this(rc, nodeFactory, edgeFactory, 1.1f, 1 / 1.1f)

    /**
     * Creates an instance with the specified rendering context and node/edge factories, and with the
     * specified zoom in/out values.
     *
     * @param rc the rendering context
     * @param nodeFactory used to construct nodes
     * @param edgeFactory used to construct edges
     * @param in amount to zoom in by for each action
     * @param out amount to zoom out by for each action
     */
    constructor(
        rc: RenderContext<N, E>,
        nodeFactory: Supplier<N>,
        edgeFactory: Supplier<E>,
        `in`: Float,
        out: Float
    ) : super(`in`, out) {
        this.nodeFactory = nodeFactory
        this.edgeFactory = edgeFactory
        this.rc = rc
        this.basicTransformer = rc.getMultiLayerTransformer()
        loadPlugins()
        modeKeyListener = ModeKeyAdapter(this)
    }

    /** create the plugins, and load the plugins for TRANSFORMING mode */
    override fun loadPlugins() {
        pickingPlugin = PickingGraphMousePlugin<N, E>()
        animatedPickingPlugin = AnimatedPickingGraphMousePlugin<N, E>()
        translatingPlugin = TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK)
        scalingPlugin = ScalingGraphMousePlugin(CrossoverScalingControl(), 0, `in`, out)
        rotatingPlugin = RotatingGraphMousePlugin()
        shearingPlugin = ShearingGraphMousePlugin()
        _editingPlugin = EditingGraphMousePlugin(nodeFactory = nodeFactory, edgeFactory = edgeFactory)
        _labelEditingPlugin = LabelEditingGraphMousePlugin()
        _annotatingPlugin = AnnotatingGraphMousePlugin(rc)
        _popupEditingPlugin = EditingPopupGraphMousePlugin(nodeFactory, edgeFactory)
        add(scalingPlugin)
        setMode(ModalGraphMouse.Mode.EDITING)
    }

    /** setter for the Mode. */
    override fun setMode(mode: ModalGraphMouse.Mode) {
        if (this.mode != mode) {
            fireItemStateChanged(
                ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this.mode, ItemEvent.DESELECTED)
            )
            this.mode = mode
            when (mode) {
                ModalGraphMouse.Mode.TRANSFORMING -> setTransformingMode()
                ModalGraphMouse.Mode.PICKING -> setPickingMode()
                ModalGraphMouse.Mode.EDITING -> setEditingMode()
                ModalGraphMouse.Mode.ANNOTATING -> setAnnotatingMode()
            }
            modeBox?.selectedItem = mode
            fireItemStateChanged(
                ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, mode, ItemEvent.SELECTED)
            )
        }
    }

    override fun setPickingMode() {
        remove(translatingPlugin)
        remove(rotatingPlugin)
        remove(shearingPlugin)
        remove(_editingPlugin)
        remove(_annotatingPlugin)
        add(pickingPlugin)
        add(animatedPickingPlugin)
        add(_labelEditingPlugin)
        add(_popupEditingPlugin)
    }

    override fun setTransformingMode() {
        remove(pickingPlugin)
        remove(animatedPickingPlugin)
        remove(_editingPlugin)
        remove(_annotatingPlugin)
        add(translatingPlugin)
        add(rotatingPlugin)
        add(shearingPlugin)
        add(_labelEditingPlugin)
        add(_popupEditingPlugin)
    }

    protected fun setEditingMode() {
        remove(pickingPlugin)
        remove(animatedPickingPlugin)
        remove(translatingPlugin)
        remove(rotatingPlugin)
        remove(shearingPlugin)
        remove(_labelEditingPlugin)
        remove(_annotatingPlugin)
        add(_editingPlugin)
        add(_popupEditingPlugin)
    }

    protected fun setAnnotatingMode() {
        remove(pickingPlugin)
        remove(animatedPickingPlugin)
        remove(translatingPlugin)
        remove(rotatingPlugin)
        remove(shearingPlugin)
        remove(_labelEditingPlugin)
        remove(_editingPlugin)
        remove(_popupEditingPlugin)
        add(_annotatingPlugin)
    }

    /**
     * @return the modeBox.
     */
    override fun getModeComboBox(): JComboBox<ModalGraphMouse.Mode> {
        if (modeBox == null) {
            modeBox = JComboBox(
                arrayOf(
                    ModalGraphMouse.Mode.TRANSFORMING,
                    ModalGraphMouse.Mode.PICKING,
                    ModalGraphMouse.Mode.EDITING,
                    ModalGraphMouse.Mode.ANNOTATING
                )
            )
            modeBox!!.addItemListener(getModeListener())
        }
        modeBox!!.selectedItem = mode
        return modeBox!!
    }

    /**
     * create (if necessary) and return a menu that will change the mode
     *
     * @return the menu
     */
    override fun getModeMenu(): JMenu {
        if (modeMenu == null) {
            modeMenu = JMenu()
            val icon = BasicIconFactory.getMenuArrowIcon()
            modeMenu!!.icon = BasicIconFactory.getMenuArrowIcon()
            modeMenu!!.preferredSize = Dimension(icon.iconWidth + 10, icon.iconHeight + 10)

            val transformingButton = JRadioButtonMenuItem(ModalGraphMouse.Mode.TRANSFORMING.toString())
            transformingButton.addItemListener { e ->
                if (e.stateChange == ItemEvent.SELECTED) {
                    setMode(ModalGraphMouse.Mode.TRANSFORMING)
                }
            }

            val pickingButton = JRadioButtonMenuItem(ModalGraphMouse.Mode.PICKING.toString())
            pickingButton.addItemListener { e ->
                if (e.stateChange == ItemEvent.SELECTED) {
                    setMode(ModalGraphMouse.Mode.PICKING)
                }
            }

            val editingButton = JRadioButtonMenuItem(ModalGraphMouse.Mode.EDITING.toString())
            editingButton.addItemListener { e ->
                if (e.stateChange == ItemEvent.SELECTED) {
                    setMode(ModalGraphMouse.Mode.EDITING)
                }
            }

            val radio = ButtonGroup()
            radio.add(transformingButton)
            radio.add(pickingButton)
            radio.add(editingButton)
            transformingButton.isSelected = true
            modeMenu!!.add(transformingButton)
            modeMenu!!.add(pickingButton)
            modeMenu!!.add(editingButton)
            modeMenu!!.toolTipText = "Menu for setting Mouse Mode"
            addItemListener { e ->
                if (e.stateChange == ItemEvent.SELECTED) {
                    when (e.item) {
                        ModalGraphMouse.Mode.TRANSFORMING -> transformingButton.isSelected = true
                        ModalGraphMouse.Mode.PICKING -> pickingButton.isSelected = true
                        ModalGraphMouse.Mode.EDITING -> editingButton.isSelected = true
                    }
                }
            }
        }
        return modeMenu!!
    }

    open class ModeKeyAdapter : KeyAdapter {
        private var t = 't'
        private var p = 'p'
        private var e = 'e'
        private var a = 'a'
        protected var graphMouse: ModalGraphMouse

        constructor(graphMouse: ModalGraphMouse) {
            this.graphMouse = graphMouse
        }

        constructor(t: Char, p: Char, e: Char, a: Char, graphMouse: ModalGraphMouse) {
            this.t = t
            this.p = p
            this.e = e
            this.a = a
            this.graphMouse = graphMouse
        }

        override fun keyTyped(event: KeyEvent) {
            val keyChar = event.keyChar
            when (keyChar) {
                t -> {
                    (event.source as Component).cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
                    graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING)
                }
                p -> {
                    (event.source as Component).cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    graphMouse.setMode(ModalGraphMouse.Mode.PICKING)
                }
                e -> {
                    (event.source as Component).cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)
                    graphMouse.setMode(ModalGraphMouse.Mode.EDITING)
                }
                a -> {
                    (event.source as Component).cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)
                    graphMouse.setMode(ModalGraphMouse.Mode.ANNOTATING)
                }
            }
        }
    }

    /**
     * @return the _annotatingPlugin
     */
    fun getAnnotatingPlugin(): AnnotatingGraphMousePlugin<N, E> {
        return _annotatingPlugin
    }

    /**
     * @return the _editingPlugin
     */
    fun getEditingPlugin(): EditingGraphMousePlugin<N, E> {
        return _editingPlugin
    }

    /**
     * @return the _labelEditingPlugin
     */
    fun getLabelEditingPlugin(): LabelEditingGraphMousePlugin<N, E> {
        return _labelEditingPlugin
    }

    /**
     * @return the _popupEditingPlugin
     */
    fun getPopupEditingPlugin(): EditingPopupGraphMousePlugin<N, E> {
        return _popupEditingPlugin
    }
}
