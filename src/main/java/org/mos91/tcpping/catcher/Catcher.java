package org.mos91.tcpping.catcher;

import org.mos91.tcpping.catcher.handlers.CatcherHandler;
import org.mos91.tcpping.commons.NettyServer;
import org.mos91.tcpping.messages.ping.PingMessageDecoder;
import org.mos91.tcpping.messages.ping.PingMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author OMeleshin.
 * @version 23.07.2016
 */
public class Catcher implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(Catcher.class);

  private String bindAddress;

  private int port;

  public Catcher(String bindAddress, int port) {
    this.bindAddress = bindAddress;
    this.port = port;
  }

  @Override
  public void run() {
    LOG.info("Start catcher on {}:{}", bindAddress, port);

    try {
      new NettyServer(bindAddress, port,
        Arrays.asList(new PingMessageDecoder(), new PingMessageEncoder(), new CatcherHandler())).start().sync();
    } catch (InterruptedException e) {
        LOG.error("Catcher shutdown abnormally", e);
    }
  }
}
