package edu.uci.ics.jung.visualization.layout;

import com.google.common.collect.Lists;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;
import edu.uci.ics.jung.visualization.util.DefaultChangeEventSupport;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractObservableLayoutModel<N, P> extends AbstractLayoutModel<N, P>
    implements LayoutModel<N, P>, ChangeEventSupport, LayoutEventSupport<N, P> {

  private static final Logger log = LoggerFactory.getLogger(AbstractObservableLayoutModel.class);

  protected ChangeEventSupport changeSupport = new DefaultChangeEventSupport(this);
  protected List<LayoutChangeListener<N, P>> layoutChangeListeners = Lists.newArrayList();

  public AbstractObservableLayoutModel(
      Graph<N> graph, DomainModel<P> domainModel, int width, int height) { //Dimension size) {
    super(graph, domainModel, width, height);
  }

  public boolean isFireEvents() {
    return fireEvents;
  }

  @Override
  public void setFireEvents(boolean fireEvents) {
    log.trace("setFireEvents {}", fireEvents);
    this.fireEvents = fireEvents;
    // any time we turn this back on, fire an event in case
    // anything was missed while it was off
    if (fireEvents) {
      fireStateChanged();
    }
  }

  protected boolean fireEvents = true;

  @Override
  public void addChangeListener(ChangeListener l) {
    this.changeSupport.addChangeListener(l);
  }

  @Override
  public void removeChangeListener(ChangeListener l) {
    this.changeSupport.removeChangeListener(l);
  }

  @Override
  public ChangeListener[] getChangeListeners() {
    return changeSupport.getChangeListeners();
  }

  @Override
  public void fireStateChanged() {

    if (this.fireEvents) {
      this.changeSupport.fireStateChanged();
    }
  }

  @Override
  public void addLayoutChangeListener(LayoutChangeListener<N, P> listener) {
    this.layoutChangeListeners.add(listener);
  }

  @Override
  public void removeLayoutChangeListener(LayoutChangeListener<N, P> listener) {
    this.layoutChangeListeners.remove(listener);
  }

  protected void fireLayoutChanged(N node, P location, Graph<N> graph) {
    if (this.fireEvents && !layoutChangeListeners.isEmpty()) {
      LayoutEvent<N, P> evt = new LayoutGraphEvent<N, P>(new LayoutEvent(node, location), graph);
      for (LayoutChangeListener<N, P> listener : layoutChangeListeners) {
        listener.layoutChanged(evt);
      }
    }
  }
}
