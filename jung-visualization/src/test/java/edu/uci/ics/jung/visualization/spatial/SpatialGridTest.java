package edu.uci.ics.jung.visualization.spatial;

import java.awt.*;
import java.util.Arrays;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by Tom Nelson. */
public class SpatialGridTest {

  Logger log = LoggerFactory.getLogger(SpatialGridTest.class);

  @Test
  public void testBoxLocations() {
    SpatialGrid spatial = new SpatialGrid(new Rectangle(0, 0, 100, 100), 4, 4);
    log.info("grid is " + spatial.getGrid());
    showBoxIndex(spatial, 10, 10);
    showBoxIndex(spatial, 49, 49);
    showBoxIndex(spatial, 50, 50);
    showBoxIndex(spatial, 99, 1);
    showBoxIndex(spatial, 70, 10);

    showVisibleTiles(spatial, new Rectangle(0, 0, 100, 100));
    showVisibleTiles(spatial, new Rectangle(25, 25, 10, 10));
    showVisibleTiles(spatial, new Rectangle(25, 25, 30, 30));
    showVisibleTiles(spatial, new Rectangle(99, 99, 30, 30));

    //        log.info("spatial.getBoxIndex(10,10):"+ Arrays.toString(spatial.getBoxIndex(10,10)));
  }

  private void showBoxIndex(SpatialGrid spatial, int x, int y) {
    log.info(
        "spatial.getBoxIndex(" + x + "," + y + "):" + Arrays.toString(spatial.getBoxIndex(x, y)));
  }

  private void showVisibleTiles(SpatialGrid spatial, Rectangle r) {
    log.info("spatial.getVisibleTiles(" + r + "):" + spatial.getVisibleTiles(r));
  }
}
