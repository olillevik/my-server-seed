package net.openright.simpleserverseed.application;

import javax.sql.DataSource;

import net.openright.infrastructure.util.LogUtil;

import org.openqa.selenium.WebDriver;

import ch.qos.logback.core.joran.spi.JoranException;

public class SimpleseedTestConfig extends SeedAppConfigFile {

    public SimpleseedTestConfig() {
        super("seedapp-test.properties");
        try {
			LogUtil.setupLogging("logging-seedapp-test.xml");
		} catch (JoranException e) {
			e.printStackTrace();
		}
    }

    @Override
    public DataSource createDataSource() {
        return createTestDataSource("seed");
    }

    public String getWebDriverName() {
        String webdriverClass = WebDriver.class.getName();
        return System.getProperty(webdriverClass, getProperty(webdriverClass, "org.openqa.selenium.firefox.FirefoxDriver"));
    }

}
