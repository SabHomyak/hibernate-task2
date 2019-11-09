import javax.persistence.*;
import java.util.Objects;
@Entity
@Table(name = "currencies")
public class Currency {
    @Transient
    private String r030;
    @Transient
    private String txt;
    @Column
    private Double rate;
    @Id
    @Column(name = "name")
    private String cc;
    @Transient
    private String exchangeDate;

    public Currency() {
    }

    public String getR030() {
        return r030;
    }

    public void setR030(String r030) {
        this.r030 = r030;
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getExchangeDate() {
        return exchangeDate;
    }

    public void setExchangeDate(String exchangeDate) {
        this.exchangeDate = exchangeDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Currency currency = (Currency) o;
        return Objects.equals(r030, currency.r030) &&
                Objects.equals(txt, currency.txt) &&
                Objects.equals(rate, currency.rate) &&
                Objects.equals(cc, currency.cc) &&
                Objects.equals(exchangeDate, currency.exchangeDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(r030, txt, rate, cc, exchangeDate);
    }

    @Override
    public String toString() {
        return "Currency{" +
                ", cc='" + cc + '\'' +
                "rate='" + rate + '\'' +
                '}';
    }
}
