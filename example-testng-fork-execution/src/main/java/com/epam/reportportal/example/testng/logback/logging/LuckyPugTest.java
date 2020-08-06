package com.epam.reportportal.example.testng.logback.logging;

import com.epam.reportportal.example.testng.logback.MagicRandomizer;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Logs image
 *
 * @author Andrei Varabyeu
 */
public class LuckyPugTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(LuckyPugTest.class);

	@Test
	public void logImageBase64() throws IOException, InterruptedException {
		/* Generate 10 logs with pugs. Pug may be lucky or unlucky based on randomizer */
		for (int i = 0; i < 20; i++) {
			/* 50 percents. So we should have approximately same count of lucky and unlucky pugs */
			boolean happy = MagicRandomizer.checkYourLucky(30);
			String image = getImageResource(happy);

			LOGGER.trace("RP_MESSAGE#BASE64#{}#{}",
					BaseEncoding.base64().encode(Resources.asByteSource(Resources.getResource(image)).read()),
					"Pug is " + (happy ? "HAPPY" : "NOT HAPPY"));
		}

	}

	private String getImageResource(boolean lucky) {
		return "pug/" + (lucky ? "lucky.jpg" : "unlucky.jpg");
	}
}
