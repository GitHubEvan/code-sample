package com.evan.codesample;

import org.junit.Test;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hduser on 10/29/16.
 */
public class RmqTest {

    @Test
    public void javaProducer() throws Exception
    {
        JavaRabbitmq mq = new JavaRabbitmq();
        mq.producer();
    }

    @Test
    public void javaConsumer() throws Exception {
        JavaRabbitmq mq  = new JavaRabbitmq();
        mq.consumer();
    }
}
