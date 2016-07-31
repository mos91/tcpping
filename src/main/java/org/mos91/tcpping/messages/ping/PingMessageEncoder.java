package org.mos91.tcpping.messages.ping;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author OMeleshin.
 * @version 23.07.2016
 */
public class PingMessageEncoder extends MessageToByteEncoder<PingMessage> {

  @Override
  protected void encode(ChannelHandlerContext ctx, PingMessage message, ByteBuf out) throws Exception {
    out.writeLong(message.getId());
    out.writeLong(message.getSendTime());
    out.writeLong(message.getRcvTime());
    out.writeLong(message.getRcvTimezoneOffset());
    byte[] data = message.getData().getBytes();
    out.writeInt(data.length);
    out.writeBytes(data);
  }
}
