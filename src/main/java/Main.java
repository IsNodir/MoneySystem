import com.example.MoneySystem.Verticles.FundsVerticle;
import com.example.MoneySystem.Verticles.OperationsVerticle;
import com.example.MoneySystem.Verticles.UsersVerticle;
import io.vertx.core.Vertx;

public class Main {

  public static void main(String[] args){

    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new UsersVerticle());
    vertx.deployVerticle(new OperationsVerticle());
    vertx.deployVerticle(new FundsVerticle());
  }
}
