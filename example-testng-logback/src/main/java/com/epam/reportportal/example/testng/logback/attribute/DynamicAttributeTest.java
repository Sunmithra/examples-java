package com.epam.reportportal.example.testng.logback.attribute;

import com.epam.reportportal.service.tree.ItemTreeReporter;
import com.epam.reportportal.service.tree.TestItemTree;
import com.epam.reportportal.testng.ReportPortalTestNGListener;
import com.epam.reportportal.testng.TestNGService;
import com.epam.reportportal.testng.util.ItemTreeUtils;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import com.google.common.collect.Sets;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import static com.epam.reportportal.testng.TestNGService.ITEM_TREE;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Listeners(ReportPortalTestNGListener.class)
public class DynamicAttributeTest {

	private static final String SAUCELABS_USERNAME_PARAMETER = "SAUCE_USERNAME";
	private static final String SAUCELABS_ACCESS_KEY_PARAMETER = "SAUCE_ACCESS_KEY";

	//your username
	private static final String USERNAME = null;
	//your access key
	private static final String ACCESS_KEY = null;

	//EU central
	private static final String SAUCE_DATA_CENTER = "ondemand.eu-central-1.saucelabs.com";

	private RemoteWebDriver driver;

	@BeforeMethod
	public void initDriver() throws MalformedURLException {
		/**
		 * In this section, we will configure our SauceLabs credentials in order to run our tests on saucelabs.com
		 */
		String sauceUserName = ofNullable(USERNAME).orElseGet(() -> getParameter(SAUCELABS_USERNAME_PARAMETER));
		String sauceAccessKey = ofNullable(ACCESS_KEY).orElseGet(() -> getParameter(SAUCELABS_ACCESS_KEY_PARAMETER));

		DesiredCapabilities capabilities = new DesiredCapabilities();

		capabilities.setCapability("username", sauceUserName);
		capabilities.setCapability("accessKey", sauceAccessKey);
		capabilities.setCapability("browserName", "Safari");
		capabilities.setCapability("platform", "macOS 10.13");
		capabilities.setCapability("version", "11.1");
		capabilities.setCapability("build", "Report Portal Saucelabs integration example");
		capabilities.setCapability("name", "shouldOpenSafari");

		//create a new Remote driver that will allow your test to send commands to the Sauce Labs grid so that Sauce can execute your tests
		driver = new RemoteWebDriver(new URL("https://" + SAUCE_DATA_CENTER + "/wd/hub"), capabilities);
	}

	private String getParameter(String parameterName) {

		return ofNullable(getSystemProperty("SAUCE_USERNAME")).orElseGet(() -> ofNullable(getEnvironmentVariable("SAUCE_USERNAME")).orElseThrow(
				() -> new RuntimeException("Parameter '" + parameterName + "' was not found")));
	}

	private String getSystemProperty(String key) {
		return System.getProperty(key);
	}

	private String getEnvironmentVariable(String key) {
		return System.getenv(key);
	}

	@Test
	public void shouldOpenSafari() {
		driver.navigate().to("https://www.saucedemo.com");
		Assert.assertTrue(true);
	}

	@AfterMethod
	public void cleanUpAfterTestMethod(ITestResult result) {
		((JavascriptExecutor) driver).executeScript("sauce:job-result=" + (result.isSuccess() ? "passed" : "failed"));
		SessionId sessionId = driver.getSessionId();
		ItemTreeUtils.retrieveLeaf(result, ITEM_TREE)
				.ifPresent(testResultLeaf -> sendFinishRequest(testResultLeaf, ofNullable(sessionId).map(SessionId::toString).orElse("")));
		driver.quit();
	}

	private void sendFinishRequest(TestItemTree.TestItemLeaf testResultLeaf, String sauceLabsJobId) {
		FinishTestItemRQ finishTestItemRQ = new FinishTestItemRQ();
		finishTestItemRQ.setStatus("PASSED");
		finishTestItemRQ.setEndTime(Calendar.getInstance().getTime());
		finishTestItemRQ.setAttributes(Sets.newHashSet(new ItemAttributesRQ("SLID", sauceLabsJobId)));
		ItemTreeReporter.finishItem(TestNGService.getReportPortal().getClient(), finishTestItemRQ, ITEM_TREE.getLaunchId(), testResultLeaf)
				.cache()
				.blockingGet();
	}
}
