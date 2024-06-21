package org.example;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class Main {

    private static AppiumDriverLocalService getAppiumDriverService() {
        String strDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM_dd_yyyy"));
        String strTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH_mm_ss"));
        var strReportDir = Paths.get(System.getProperty("user.dir"), strDate);

        String logFileName = String.format("Appium_Server_%s.log", strTime);
        File logFile = new File(Paths.get(strReportDir.toString(), logFileName).toString());

        return AppiumDriverLocalService.buildService(new AppiumServiceBuilder()
                .withIPAddress("127.0.0.1")
                .usingPort(4723)
                .withArgument(() -> "--long-stacktrace")
                .withArgument(GeneralServerFlag.LOG_LEVEL, "info:debug")
                .withArgument(GeneralServerFlag.USE_DRIVERS, "uiautomator2")
                .withArgument(GeneralServerFlag.ALLOW_INSECURE, "adb_shell")
                .withArgument(GeneralServerFlag.RELAXED_SECURITY)
                .withArgument(GeneralServerFlag.USE_PLUGINS, "images, gestures, ocr, element-wait")
                .withArgument(GeneralServerFlag.SESSION_OVERRIDE)
                .withArgument(() -> "--config", new File(System.getProperty("user.dir") + File.separator + "config" + File.separator + "serverconfig.json").toString())
                .withTimeout(Duration.ofSeconds(240))
                .withLogOutput(System.err)
                .withLogFile(logFile));
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        AppiumDriverLocalService appiumDriverLocalService = getAppiumDriverService();
        AppiumDriver appiumDriver = null;
        try {
            appiumDriverLocalService.start();

            UiAutomator2Options uiAutomator2Options = new UiAutomator2Options()
                    .setAutomationName(AutomationName.ANDROID_UIAUTOMATOR2)
                    .setApp(System.getProperty("user.dir") + File.separator + "app" + File.separator + "android" + File.separator + "mda-2.0.2-23.apk")
                    .setFullReset(false)
                    .setNoReset(true)
                    .setPlatformName("android")
                    .setPlatformVersion("13")
                    .setUdid("ZD222CJSXB")
                    .setAppPackage("com.saucelabs.mydemoapp.android")
                    .setAppActivity("com.saucelabs.mydemoapp.android.view.activities.SplashActivity");

            uiAutomator2Options.setCapability("shouldTerminateApp", true);

            System.out.println("URL " + appiumDriverLocalService.getUrl());
            appiumDriver = new AndroidDriver(appiumDriverLocalService.getUrl(), uiAutomator2Options);
            System.out.println("SessionID " + appiumDriver.getSessionId());

            Thread.sleep(5000);

            byte[] navMenuByte = FileUtils.readFileToByteArray(new File(System.getProperty("user.dir") + File.separator + "image" + File.separator + "NavMenuIcon.png"));
            String navMenuEncodedString = Base64.getEncoder().encodeToString(navMenuByte);

            appiumDriver.findElement(AppiumBy.image(navMenuEncodedString)).click();

            Thread.sleep(5000);

        } finally {
            if (appiumDriver != null) {
                appiumDriver.quit();
            }
            appiumDriverLocalService.stop();
        }
    }
}