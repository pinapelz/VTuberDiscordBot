package nijisanji;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;


public class ScreenShotTool {
    static WebDriver driver = null;
    public void buildNijisanjiSchedule(){
        initDriver();
        ScreenShotTool ws = new ScreenShotTool();
        try {
            ws.capture("https://www.itsukaralink.jp/");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error taking screenshots of the Nijisanji Page");
        }

        driver.quit();
    }
    public static void initDriver() {
        System.setProperty("webdriver.chrome.driver",
                "/usr/lib/chromium-browser/chromedriver");
        driver = new ChromeDriver();
        driver.manage().window().setPosition(new Point(0, 0));

    }
    public void capture(String site) throws IOException {
        driver.get(site);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile, new File("nijisanji.png"));
        System.out.println("Took Screenshot for " + site + " and saved as " + "nijisanji.png");
        js.executeScript("window.scrollBy(0,500)");
        scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile, new File("nijisanji2.png"));
        System.out.println("Took Screenshot for " + site + " and saved as " + "nijisanji2.png");
        js.executeScript("window.scrollBy(0,750)");
        scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile, new File("nijisanji3.png"));
        System.out.println("Took Screenshot for " + site + "nijisanji3.png");
    }
}
