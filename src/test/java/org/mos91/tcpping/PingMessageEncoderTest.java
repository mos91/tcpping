package org.mos91.tcpping;

import com.google.common.primitives.Ints;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.mos91.tcpping.messages.ping.PingMessage;
import org.mos91.tcpping.messages.ping.PingMessageEncoder;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.google.common.primitives.Bytes.concat;
import static com.google.common.primitives.Longs.toByteArray;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mos91.tcpping.Utilities.shuffleData;
import static org.testng.Assert.assertFalse;

/**
 * @author OMeleshin.
 * @version 31.07.2016
 */
public class PingMessageEncoderTest {

  private String data;

  @BeforeClass
  public void init() throws Exception {
    data = shuffleData(50);
  }

  @DataProvider(name = "dataForEncoder")
  public Object[][] dataForEncoder() {
    PingMessage msgWithData = new PingMessage(1, data);
    msgWithData.setSendTime(1469946444161L);
    msgWithData.setRcvTime(1469946472026L);
    msgWithData.setRcvTimezoneOffset(10800000L);

    ByteBuf bufWithData = Unpooled.wrappedBuffer(concat(toByteArray(1L),
      toByteArray(1469946444161L), toByteArray(1469946472026L),
      toByteArray(10800000L), Ints.toByteArray(50),
      data.getBytes()));

    PingMessage msgWithoutData = new PingMessage(1, "");
    msgWithoutData.setSendTime(1469946444161L);
    msgWithoutData.setRcvTime(1469946472026L);
    msgWithoutData.setRcvTimezoneOffset(10800000L);

    ByteBuf bufWithoutData = Unpooled.wrappedBuffer(concat(toByteArray(1L),
      toByteArray(1469946444161L), toByteArray(1469946472026L),
      toByteArray(10800000L), Ints.toByteArray(0)));

    return new Object[][]{{bufWithData, msgWithData}, {bufWithoutData, msgWithoutData}};
  }

  @DataProvider(name = "invalidData")
  public Object[][] invalidData() {
    PingMessage msg = new PingMessage(1, data);
    msg.setSendTime(1469946444161L);
    msg.setRcvTime(1469946472026L);
    msg.setRcvTimezoneOffset(10800000L);

    ByteBuf invalidBuf = Unpooled.wrappedBuffer(concat(toByteArray(1L),
      toByteArray(1469946444161L), data.getBytes()));

    return new Object[][]{{invalidBuf, msg}};
  }

  @Test(dataProvider = "dataForEncoder")
  public void shouldEncodeCorrectly(ByteBuf expected, PingMessage written) {
    assertTrue(ByteBufUtil.equals(shouldReturnByteBuf(written), expected));
  }

  @Test(dataProvider = "invalidData")
  public void neverReturnInvalidBuffer(ByteBuf expected, PingMessage written) {
    assertFalse(ByteBufUtil.equals(shouldReturnByteBuf(written), expected));
  }

  private ByteBuf shouldReturnByteBuf(PingMessage written) {
    EmbeddedChannel channel = encoderShouldExists();

    channel.writeAndFlush(written);
    Object actual = channel.readOutbound();
    assertThat(actual, is(ByteBuf.class));
    return (ByteBuf) actual;
  }

  private EmbeddedChannel encoderShouldExists() {
    PingMessageEncoder encoder = new PingMessageEncoder();
    EmbeddedChannel channel = new EmbeddedChannel(encoder);

    assertThat(channel.pipeline().get(PingMessageEncoder.class), is(notNullValue()));

    return channel;
  }
}
