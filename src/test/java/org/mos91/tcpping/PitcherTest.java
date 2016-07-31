package org.mos91.tcpping;

import com.google.common.primitives.Ints;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Assert;
import org.mos91.tcpping.messages.ping.PingMessage;
import org.mos91.tcpping.messages.ping.PingMessageDecoder;
import org.mos91.tcpping.messages.ping.PingMessageEncoder;
import org.mos91.tcpping.pitcher.handlers.PitcherHandler;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Queue;

import static com.google.common.primitives.Bytes.concat;
import static com.google.common.primitives.Longs.toByteArray;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mos91.tcpping.Utilities.shuffleData;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author OMeleshin.
 * @version 31.07.2016
 */
public class PitcherTest {

  private String data;

  @BeforeClass
  public void init() throws Exception {
    data = shuffleData(50);
  }

  @DataProvider
  public Object[][] sendMessage() {
    return new Object[][]{{buildPingMessage()}};
  }

  @DataProvider
  public Object[][] sendBuf() {
    return new Object[][]{{buildByteBuf()}};
  }

  @DataProvider
  public Object[][] sendAndRcv() {
    return new Object[][]{{buildPingMessage(), buildByteBuf()}};
  }

  @Test(dataProvider = "sendMessage")
  public void shouldSendMessage(PingMessage message) {
    PitcherHandler pitcherHandler = new PitcherHandler();
    EmbeddedChannel channel = shouldHaveAppropriateHandlers(new PingMessageEncoder(), new PingMessageDecoder(), pitcherHandler);

    checkSendMessage(pitcherHandler, channel, message);
  }

  @Test(dataProvider = "sendAndRcv")
  public void shouldReceiveMessage(PingMessage sent, ByteBuf received) {
    PitcherHandler handler = new PitcherHandler();
    EmbeddedChannel channel = shouldHaveAppropriateHandlers(new PingMessageEncoder(), new PingMessageDecoder(), handler);

    checkSendMessage(handler, channel, sent);

    channel.writeInbound(received);
    Queue<Object> messages = channel.inboundMessages();
    Assert.assertEquals(1, messages.size());

    Object actual = messages.poll();
    assertThat(actual, is(PingMessage.class));
    PingMessage receivedMsg = (PingMessage) actual;

    assertEquals(1, handler.getTotalNumber().get());
    assertEquals(1, handler.getMessagesPerPeriod().get());
    assertEquals(1, handler.getReceiveNumberPerPeriod().get());
    long sendDuration = receivedMsg.getRcvTime() - receivedMsg.getSendTime();
    assertEquals(sendDuration, handler.getSendDurationCumul().get());
    assertTrue(handler.getRcvDurationCumul().get() >= 0);
    assertTrue(handler.getRttCumul().get() >= handler.getRcvDurationCumul().get() + handler.getSendDurationCumul().get());
    assertTrue(handler.getMaxRtt().get() >= handler.getRcvDurationCumul().get() + handler.getSendDurationCumul().get());
    assertEquals(0, handler.getTotalLoss().get());
  }

  @Test(dataProvider = "sendBuf")
  public void shouldDropMessage(ByteBuf buf) {
    PitcherHandler handler = new PitcherHandler();
    EmbeddedChannel channel = shouldHaveAppropriateHandlers(new PingMessageEncoder(), new PingMessageDecoder(), handler);

    handler.setTimeBarrier();
    channel.writeInbound(buf);
    Queue<Object> messages = channel.inboundMessages();
    Assert.assertEquals(1, messages.size());

    assertEquals(0, handler.getReceiveNumberPerPeriod().get());
  }

  private PingMessage buildPingMessage() {
    PingMessage msg = new PingMessage(1L, data);
    msg.setSendTime(1469946444161L);
    msg.setRcvTime(1469946472026L);
    msg.setRcvTimezoneOffset(10800000L);

    return msg;
  }

  private ByteBuf buildByteBuf() {
    return Unpooled.wrappedBuffer(concat(toByteArray(1L),
      toByteArray(1469946444161L), toByteArray(1469946472026L),
      toByteArray(10800000L), Ints.toByteArray(50), data.getBytes()));
  }

  private void checkSendMessage(PitcherHandler handler, EmbeddedChannel channel, PingMessage message) {
    channel.writeAndFlush(message);

    assertEquals(1, handler.getTotalNumber().get());
    assertEquals(1, handler.getMessagesPerPeriod().get());
    assertEquals(0, handler.getReceiveNumberPerPeriod().get());
  }

  private EmbeddedChannel shouldHaveAppropriateHandlers(PingMessageEncoder encoder, PingMessageDecoder decoder, PitcherHandler handler) {
    EmbeddedChannel channel = new EmbeddedChannel(encoder, decoder, handler);

    assertThat(channel.pipeline().get(PingMessageEncoder.class), notNullValue());
    assertThat(channel.pipeline().get(PingMessageDecoder.class), notNullValue());
    assertThat(channel.pipeline().get(PitcherHandler.class), notNullValue());

    return channel;
  }
}
