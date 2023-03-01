import com.example.MoneySystem.Verticles.FundsVerticle;
import com.example.MoneySystem.Verticles.MainVerticle;
import com.example.MoneySystem.Verticles.OperationsVerticle;
import com.example.MoneySystem.Verticles.UsersVerticle;
import io.vertx.core.Vertx;

public class Main {

  public static void main(String[] args){

    Vertx.vertx().deployVerticle(new UsersVerticle());
    Vertx.vertx().deployVerticle(new OperationsVerticle());
    Vertx.vertx().deployVerticle(new FundsVerticle());
  }
}
