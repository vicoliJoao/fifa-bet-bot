import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.fasterxml.jackson.core.JsonProcessingException;


public class Bet {

    public static void main(String [] args) throws InterruptedException, TelegramApiException {

        WebDriver driver;

        System.setProperty("webdriver.chrome.driver", "/home/joao/.m2/repository/webdriver/chromedriver/linux64/113.0.5672.63/chromedriver");
        driver = new ChromeDriver();

        driver.manage().window().maximize();
        driver.get("https://esoccerbet.com.br/");
        Thread.sleep(3000);

        driver.findElement(By.xpath("//*[@id=\"tudo\"]/div[4]/div[3]/a")).click();
        Thread.sleep(3000);

        while(true){

        WebElement divPartidas = driver.findElement(By.xpath("//*[@id=\"tudo\"]/div[3]"));
        WebElement divPartidasLive = divPartidas.findElement(By.xpath("//*[@id=\"tudo\"]/div[3]/div[3]"));
        List<WebElement> events = divPartidasLive.findElements(By.xpath("//*[contains(@class, 'partida min') and contains(@data-tipopartida, 'aovivo')]"));

        for (WebElement event : events) {

            var elementId = event.getAttribute("id");
            var elementClass = event.getAttribute("class");
            int totalGameTime;
            int instantGameTime;
            var player1 = driver.findElement(By.xpath("//*[@id=\"%s\"]/div[1]/div[1]/div[1]/a[1]".replace("%s", elementId))).getText();
            var player2 = driver.findElement(By.xpath("//*[@id=\"%s\"]/div[1]/div[1]/div[3]/a[1]".replace("%s", elementId))).getText();
            var scoreBoardTeam1 = Integer.valueOf(driver.findElement(By.xpath("//*[@id=\"%s\"]/div[1]/div[1]/div[2]/span/span[1]".replace("%s", elementId))).getText());
            var scoreBoardTeam2 = Integer.valueOf(driver.findElement(By.xpath("//*[@id=\"%s\"]/div[1]/div[1]/div[2]/span/span[3]".replace("%s", elementId))).getText());
            var scoreBoardDiff = Math.abs(scoreBoardTeam1 - scoreBoardTeam2);

            totalGameTime = getGameTime(elementClass);
            instantGameTime = Integer.parseInt(driver.findElement(By.xpath("//*[@id=\"%s\"]/div[2]/div[1]/span".replace("%s", elementId))).getText());

            if (isTimeToBet(scoreBoardDiff, totalGameTime, instantGameTime)) {
                HashMap<String, Integer> score = new HashMap<>();
                score.put(player1, scoreBoardTeam1);
                score.put(player2, scoreBoardTeam2);

                String responseScore = decidePlayer(score, player1, player2);

                HashMap<String, String> values = new HashMap<>();
                values.put("chat_id", "1526700411");
                values.put("data", "Aposta disponível em: %s1, para o jogador %s2".replace("%s1", score.toString()).replace("%s2", responseScore));

                sendMessage(values.get("data"));
            }
        }
            Thread.sleep(60000);
        }
    }

    private static Integer getGameTime(String elementClass) {
        int gameTime;
        if (elementClass.contains("min8")) {
            gameTime = 8;
        } else {
            if (elementClass.contains("min10")) {
                gameTime = 10;
            } else gameTime = 12;
        }

        return gameTime;
    }

    private static Boolean isTimeToBet(int scoreBoardDiff, int totalGameTime, int instantGameTime) {
        if (scoreBoardDiff == 2 || scoreBoardDiff == 3) {
            var timeToBet = totalGameTime - scoreBoardDiff;
            return instantGameTime == timeToBet || instantGameTime == timeToBet - 1;
        }
        return false;
    }

    private static void sendMessage(String response) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        MyBot bot = new MyBot(BotData.BOT_TOKEN);

        SendMessage message = new SendMessage();
        message.setChatId(BotData.CHAT_ID);
        message.setText(response);

        bot.execute(message);

        telegramBotsApi.registerBot(bot);
    }

    private static String decidePlayer(HashMap<String, Integer> score, String player1, String player2) {
        if (score.get(player1) > score.get(player2)) return player1;
        else return player2;
    }
}
