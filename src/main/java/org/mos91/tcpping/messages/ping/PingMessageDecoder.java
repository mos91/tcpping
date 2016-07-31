package org.mos91.tcpping.messages.ping;

import static org.mos91.tcpping.messages.ping.DecoderState.READ_DATA_LENGTH;
import static org.mos91.tcpping.messages.ping.DecoderState.READ_ID;
import static org.mos91.tcpping.messages.ping.DecoderState.READ_RCV_TIME;
import static org.mos91.tcpping.messages.ping.DecoderState.READ_SEND_TIME;
import static org.mos91.tcpping.messages.ping.DecoderState.READ_TZ_OFFSET;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * @author OMeleshin.
 * @version 23.07.2016
 */
public class PingMessageDecoder extends ReplayingDecoder<DecoderState> {

  private long id;

  private long sendTime;

  private long rcvTime;

  private long rcvTzOffset;

  private int messageSize;

  private byte[] data;

  public PingMessageDecoder() {
    super(READ_ID);
  }

  @Override
  protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf buf, List<Object> out) throws Exception {
    switch (state()) {
      case READ_ID:
        id = buf.readLong();
        checkpoint(READ_SEND_TIME);
        break;
      case READ_SEND_TIME:
        sendTime = buf.readLong();
        checkpoint(READ_RCV_TIME);
        break;
      case READ_RCV_TIME:
        rcvTime = buf.readLong();
        checkpoint(READ_TZ_OFFSET);
        break;
      case READ_TZ_OFFSET:
        rcvTzOffset = buf.readLong();
        checkpoint(READ_DATA_LENGTH);
        break;
      case READ_DATA_LENGTH:
        messageSize = buf.readInt();
        checkpoint(DecoderState.READ_DATA);
        break;
      case READ_DATA:
        if (data == null) {
          data = new byte[messageSize];
        }
        buf.readBytes(data);

        PingMessage message = new PingMessage(id, new String(data));
        message.setSendTime(sendTime);
        message.setRcvTime(rcvTime);
        message.setRcvTimezoneOffset(rcvTzOffset);

        out.add(message);
        checkpoint(READ_ID);
        break;
      default:
        throw new IllegalStateException("Unexpected error while decoding byte message");
    }
  }

}
