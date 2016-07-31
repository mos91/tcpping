package org.mos91.tcpping.commons;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author OMeleshin.
 * @version 30.07.2016
 */
public class NettyClient<T> {

  private static final Logger LOG = LoggerFactory.getLogger(NettyClient.class);

  boolean sessionInitialized;

  private String host;

  private int port;

  private List<ChannelHandler> handlers;

  public NettyClient(String host, int port, List<ChannelHandler> handlers) {
    this.host = host;
    this.port = port;
    this.handlers = handlers;
  }

  public T execute(Callback<T> callback) {
    final EventLoopGroup group = new NioEventLoopGroup();
    T result = null;
    try {
      Bootstrap bootstrap = new Bootstrap();
      bootstrap.group(group)
        .channel(NioSocketChannel.class)
        .option(ChannelOption.TCP_NODELAY, true)
        .handler(new AppChannelInitializer(handlers));

      result = callback.execute(bootstrap.connect(host, port).sync().channel());
    } catch (Exception e) {
      sessionInitialized = false;
      LOG.error("Unexpected error", e);
    } finally {
      group.shutdownGracefully();
    }

    return result;
  }

  public boolean isSessionInitialized() {
    return sessionInitialized;
  }

  @FunctionalInterface
  public interface Callback<T> {
    T execute(Channel channel) throws Exception;
  }
}
