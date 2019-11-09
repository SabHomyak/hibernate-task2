import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String lastName;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<BankAccount> accounts = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<Transaction> transactions = new ArrayList<>();
    @Transient
    private static EntityManager entityManager;

    {
        accounts.add(new BankAccount("USD", 0d, this));
        accounts.add(new BankAccount("EUR", 0d, this));
        accounts.add(new BankAccount("UAH", 0d, this));
    }

    public User() {
    }

    public User(String name, String lastName) {
        this.name = name;
        this.lastName = lastName;
    }

    public static EntityManager getEntityManager() {
        return entityManager;
    }

    public static void setEntityManager(EntityManager entityManager) {
        User.entityManager = entityManager;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void replenishAccount(CurrencyE currency, double sum) throws Exception {
        if (sum < 0) {
            throw new Exception();
        }
        entityManager.getTransaction().begin();
        BankAccount bankAccount = getBankAccount(currency);
        double beforeBalance = bankAccount.getBalance();
        double newBalance = beforeBalance + sum;
        bankAccount.setBalance(newBalance);
        transactions.add(new Transaction(this, currency.name(), "replenish", sum));
        entityManager.persist(this);
        entityManager.getTransaction().commit();
    }

    public void withdrawMoney(CurrencyE currency, double sum) throws Exception {
        entityManager.getTransaction().begin();
        BankAccount bankAccount = getBankAccount(currency);
        double beforeBalance = bankAccount.getBalance();
        if ((sum < 0) || sum > beforeBalance) {
            entityManager.getTransaction().rollback();
            throw new Exception();
        }
        double newBalance = beforeBalance - sum;
        bankAccount.setBalance(newBalance);
        transactions.add(new Transaction(this, currency.name(), "withdraw", sum));
        entityManager.persist(this);
        entityManager.getTransaction().commit();
    }

    public void transferMoney(User anotherUser, CurrencyE currency, double sum) throws Exception {
        BankAccount bankAccount = getBankAccount(currency);
        double balanceThisUser = bankAccount.getBalance();
        if (sum > balanceThisUser) {
            throw new Exception();
        }
        try {
            this.withdrawMoney(currency, sum);
            anotherUser.replenishAccount(currency, sum);
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            e.printStackTrace();
        }
    }

    public void convertCurrency(CurrencyE currencyFrom, CurrencyE currencyTo, double sum) throws Exception {
        if (currencyFrom.equals(currencyTo)) {
            throw new Exception();
        }
        Currency currFrom = entityManager.find(Currency.class, currencyFrom.name());
        Currency currTo = entityManager.find(Currency.class, currencyTo.name());
        if (currFrom == null) {
            currFrom = new Currency();
            currFrom.setRate(1d);
        }
        if (currTo == null) {
            currTo = new Currency();
            currTo.setRate(1d);
        }
        double currFromInUAH = currFrom.getRate() * sum;
        double currToInUAH = currFromInUAH / currTo.getRate();
        try {
            withdrawMoney(currencyFrom, sum);
            replenishAccount(currencyTo, currToInUAH);
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            e.printStackTrace();
        }
    }

    private BankAccount getBankAccount(CurrencyE currency) {
        BankAccount bankAccount = accounts.stream()
                .filter(a -> a.getCurrency().equals(currency.name()))
                .findFirst()
                .get();
        return bankAccount;
    }

    public double getOverallBalance() {
        double balance = 0;
        for (BankAccount account : accounts) {
            if (account.getCurrency().equals("USD") || account.getCurrency().equals("EUR")) {
                Currency currency = entityManager.find(Currency.class, account.getCurrency());
                balance += (account.getBalance() * currency.getRate());
            } else {
                balance += account.getBalance();
            }
        }
        return balance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id &&
                Objects.equals(name, user.name) &&
                Objects.equals(lastName, user.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, lastName);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
