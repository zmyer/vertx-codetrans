package control;

import io.vertx.core.AbstractVerticle;
import io.vertx.examples.CodeTrans;
import io.vertx.examples.ControlTest;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ThenSkip extends AbstractVerticle {

  @Override
  @CodeTrans
  public void start() throws Exception {
    if (false) {
      ControlTest.o = "ok";
    }
  }
}
