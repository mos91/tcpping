package org.mos91.tcpping;

import com.google.common.primitives.Ints;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.mos91.tcpping.catcher.handlers.CatcherHandler;
import org.mos91.tcpping.messages.ping.PingMessage;
import org.mos91.tcpping.messages.ping.PingMessageDecoder;
import org.mos91.tcpping.messages.ping.PingMessageEncoder;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.google.common.primitives.Bytes.concat;
import static com.google.common.primitives.Longs.toByteArray;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mos91.tcpping.Utilities.shuffleData;
import static org.testng.Assert.assertNull;

/**
 * @author OMeleshin.
 * @version 31.07.2016
 */
public class CatcherTest {

  @DataProvider
  public Object[][] correctMessageAndBufMask() {
    PingMessage msg = new PingMessage(1, "");
    msg.setSendTime(1469946444161L);

    ByteBuf bufMask = Unpooled.wrappedBuffer(concat(toByteArray(1L),
      toByteArray(0xFFFFFFFF), toByteArray(0xFFFFFFFF)));

    return new Object[][]{{msg, bufMask}};
  }

  @DataProvider
  public Object[][] byteBuf() {
    ByteBuf buf = Unpooled.wrappedBuffer(concat(toByteArray(1L),
      toByteArray(1469946444161L), toByteArray(1469946472026L),
      toByteArray(10800000L), Ints.toByteArray(50), shuffleData(50).getBytes()));

    return new Object[][]{{buf}};
  }

  @DataProvider
  public Object[][] message() {
    PingMessage msg = new PingMessage(1, "");
    msg.setSendTime(1469946444161L);

    return new Object[][]{{msg}};
  }

  @Test(dataProvider = "message")
  public void shouldEncode(PingMessage message) throws Exception {
    PingMessageEncoder encoderMock = mock(PingMessageEncoder.class);

    EmbeddedChannel channel = shouldHaveAppropriateHandlers(
      encoderMock, mock(PingMessageDecoder.class), mock(CatcherHandler.class));

    channel.writeAndFlush(message);

    verify(encoderMock).write(
      any(ChannelHandlerContext.class), any(), any(ChannelPromise.class));
  }

  @Test(dataProvider = "byteBuf")
  public void shouldDecode(ByteBuf buf) throws Exception {
    PingMessageDecoder decoderMock = mock(PingMessageDecoder.class);
    EmbeddedChannel channel = shouldHaveAppropriateHandlers(
      mock(PingMessageEncoder.class), decoderMock, mock(CatcherHandler.class));

    channel.writeInbound(buf);

    verify(decoderMock, times(1)).channelRead(any(ChannelHandlerContext.class), any());
  }

  @Test(dataProvider = "correctMessageAndBufMask")
  public void shouldSetRcvTime(PingMessage msg, ByteBuf mask) {
    EmbeddedChannel channel =
      shouldHaveAppropriateHandlers(new PingMessageEncoder(), new PingMessageDecoder(), new CatcherHandler());

    channel.writeInbound(msg);

    ByteBuf actual = channel.readOutbound();

    assertThat(actual, byteMask(mask));
  }

  @Test
  public void shouldDropMessageWithoudSendTime() {
    PingMessage msg = new PingMessage(1L, "");

    EmbeddedChannel channel =
      shouldHaveAppropriateHandlers(new PingMessageEncoder(), new PingMessageDecoder(), new CatcherHandler());

    channel.writeInbound(msg);

    ByteBuf actual = channel.readOutbound();

    assertNull(actual);
  }

  private EmbeddedChannel shouldHaveAppropriateHandlers(PingMessageEncoder encoder,
    PingMessageDecoder decoder, CatcherHandler handler) {
    EmbeddedChannel channel = new EmbeddedChannel(encoder, decoder, handler);

    assertThat(channel.pipeline().get(PingMessageEncoder.class), notNullValue());
    assertThat(channel.pipeline().get(PingMessageDecoder.class), notNullValue());
    assertThat(channel.pipeline().get(CatcherHandler.class), notNullValue());

    return channel;
  }

  private static Matcher<ByteBuf> byteMask(ByteBuf mask) {
    return new BaseMatcher<ByteBuf>() {

      @Override
      public boolean matches(Object o) {
        ByteBuf buf = (ByteBuf) o;

        return ((buf.readLong() & mask.readLong()) > 0) &&
          ((buf.readLong() & mask.readLong()) > 0) && ((buf.readLong() & mask.readLong()) > 0);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("Should conform to appropriate byte footprint");
      }
    };
  }
}
