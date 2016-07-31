package org.mos91.tcpping.commons;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.List;

/**
 * @author OMeleshin.
 * @version 30.07.2016
 */
public class NettyServer {

  private boolean started;

  private String bindAddress;

  private int port;

  private List<ChannelHandler> childHandlers;

  public NettyServer(String bindAddress, int port, List<ChannelHandler> childHandlers) {
    this.bindAddress = bindAddress;
    this.port = port;
    this.childHandlers = childHandlers;
  }

  public ChannelFuture start() {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    ChannelFuture closeFuture = null;
    try {
      ServerBootstrap bootstrap = new ServerBootstrap();
      bootstrap.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG, 100)
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new AppChannelInitializer(childHandlers));

      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
      }));

      ChannelFuture f = bootstrap.bind(bindAddress, port).sync();

      closeFuture = f.channel().closeFuture();
    } catch (Exception e) {
      started = false;
    }

    return closeFuture;
  }

  public boolean isStarted() {
    return started;
  }
}
