package org.mos91.tcpping.catcher.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.mos91.tcpping.messages.ping.PingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.TimeZone;

/**
 * @author OMeleshin.
 * @version 27.07.2016
 */
public class CatcherHandler extends ChannelInboundHandlerAdapter {

  private static final Logger LOG = LoggerFactory.getLogger(CatcherHandler.class);

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    PingMessage pingMessage = (PingMessage) msg;

    if (pingMessage.getSendTime() > 0) {
      pingMessage.setRcvTime(new Date().getTime());
      pingMessage.setRcvTimezoneOffset(TimeZone.getDefault().getRawOffset());

      ctx.writeAndFlush(msg);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    LOG.error("Unexpected error was occured", cause);

    ctx.close();
  }

}
