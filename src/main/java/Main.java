import com.example.MoneySystem.Verticles.MainVerticle;
import io.vertx.core.Vertx;

public class Main {

  public static void main(String[] args){

    Vertx.vertx().deployVerticle(new MainVerticle());

  }
}
