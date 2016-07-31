package org.mos91.tcpping;

import io.netty.channel.embedded.EmbeddedChannel;
import org.mos91.tcpping.messages.ping.PingMessageDecoder;
import org.mos91.tcpping.messages.ping.PingMessageEncoder;
import org.mos91.tcpping.pitcher.handlers.PitcherHandler;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author OMeleshin.
 * @version 31.07.2016
 */
public class PitcherTest {

  @Test
  public void shouldHaveAppropriateHandlers(PingMessageEncoder encoder, PingMessageDecoder decoder, PitcherHandler handler) {
    EmbeddedChannel channel = new EmbeddedChannel(encoder, decoder, handler);

    assertThat(channel.pipeline().get(PingMessageEncoder.class), notNullValue());
    assertThat(channel.pipeline().get(PingMessageDecoder.class), notNullValue());
    assertThat(channel.pipeline().get(PitcherHandler.class), notNullValue());
  }
}
