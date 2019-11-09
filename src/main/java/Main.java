import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class Main {
    static EntityManagerFactory emf;
    static EntityManager em;

    public static void main(String[] args) {
        try {
            emf = Persistence.createEntityManagerFactory("JPABank");
            em = emf.createEntityManager();
            getExchangeRates();
            User user = new User("Vasya", "Pupkin");
            User user2 = new User("Sanya", "Rozhkin");
            User.setEntityManager(em);
            user.replenishAccount(CurrencyE.USD, 100);
            user.replenishAccount(CurrencyE.EUR, 100);
            user.transferMoney(user2, CurrencyE.USD, 50);
            user2.convertCurrency(CurrencyE.USD, CurrencyE.UAH, 50);
            user2.withdrawMoney(CurrencyE.UAH, 1000);
            System.out.println("Баланс пользвателя " + user2.getName() + " " + user2.getLastName() + ": " + user2.getOverallBalance());
            System.out.println("Баланс пользвателя " + user.getName() + " " + user.getLastName() + ": " + user.getOverallBalance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addCurrency(Currency... currencies) {
        try {
            em.getTransaction().begin();
            for (int i = 0; i < currencies.length; i++) {
                em.persist(currencies[i]);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }
    }

    public static void getExchangeRates() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            URL url = new URL("https://old.bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try (InputStream is = connection.getInputStream()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] bytes = new byte[10240];
                int len;
                while ((len = is.read(bytes)) > 0) {
                    bos.write(bytes, 0, len);
                }
                List<Currency> currencies = gson.fromJson(bos.toString(), new TypeToken<List<Currency>>() {
                }.getType());
                for (Currency currency : currencies) {
                    if (currency.getCc().equals("USD") || currency.getCc().equals("EUR")) {
                        addCurrency(currency);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
