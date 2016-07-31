package org.mos91.tcpping.commons;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import java.util.List;
import java.util.Collections;
import java.util.Objects;

/**
 * @author OMeleshin.
 * @version 30.07.2016
 */
public class AppChannelInitializer extends ChannelInitializer<SocketChannel> {

  private List<ChannelHandler> handlerList = Collections.emptyList();

  private ChannelPipeline pipeline;

  public AppChannelInitializer(List<ChannelHandler> handlerList) {
    this.handlerList = handlerList;
  }

  @Override
  protected void initChannel(SocketChannel socketChannel) throws Exception {
    pipeline = socketChannel.pipeline();

    handlerList.stream().forEachOrdered(handler -> addLast(handler));
  }

  private void addLast(ChannelHandler handler) {
    Objects.requireNonNull(pipeline);

    pipeline.addLast(handler.getClass().getSimpleName(), handler);
  }

}
