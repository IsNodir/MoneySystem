package com.example.MoneySystem.Repository;

import com.example.MoneySystem.Model.DateDTO;
import com.example.MoneySystem.Model.DayDTO;
import com.example.MoneySystem.Model.FundDTO;
import com.example.MoneySystem.Model.OperationDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.*;

public class FundRepository {
  private static final String SQL_SELECT_BALANCE_BY_LOGIN_DATES = "SELECT balance, spent, received FROM public.funds f INNER JOIN public.dates d ON d.id = f.date_id WHERE user_login = #{login} AND (day BETWEEN #{dayFrom} AND #{dayTo})";
  private static final String SQL_SELECT_BALANCE_BY_LOGIN_DATE = "SELECT balance FROM public.funds, public.users, public.dates WHERE login = #{login} AND day BETWEEN #{dayFrom} AND #{dayTo}";
  private static final String SQL_SELECT_CURRENT_BALANCE_ID = "SELECT max(id) as id FROM public.funds WHERE user_login = #{login}";
  private static final String SQL_SELECT_CURRENT_BALANCE = "SELECT balance FROM public.funds WHERE user_login = #{login} AND id = #{id}";
  private static final String SQL_UPDATE_BALANCE_SENDER = "UPDATE public.funds SET balance = (balance - #{money_amount}) WHERE user_login = #{login} AND id = #{id}";
  private static final String SQL_UPDATE_BALANCE_RECEIVER = "UPDATE public.funds SET balance = (balance + #{money_amount}) WHERE user_login = #{login} AND id = #{id}";
  private static final String SQL_SELECT_DATE = "SELECT day FROM public.dates WHERE day = #{day}";
  private static final String SQL_UPDATE_SENDER_STATUS = "UPDATE public.operation SET sender_delete = true WHERE id = #{id}";
  private static final String SQL_UPDATE_RECEIVER_STATUS = "UPDATE public.operation SET receiver_delete = true WHERE id = #{id}";
  private static final String SQL_DELETE_OPERATION = "DELETE FROM public.operation WHERE id = #{id} AND sender_delete = true AND receiver_delete = true";

  public FundRepository() {
  }

  public Future<? extends ObjectReader> selectDate (PgPool dbClient, ObjectReader date) throws JsonProcessingException {

//    LocalDate day = date.getDay();

    ObjectMapper mapper = new ObjectMapper();
    mapper.findAndRegisterModules();

    return SqlTemplate
      .forQuery(dbClient, SQL_SELECT_DATE)
      .mapTo(mapper.readerForMapOf(DayDTO.class).getClass())
      .execute(Collections.singletonMap("day", date.readValue("day").toString()))
      .map(rowSet -> {
        final RowIterator<? extends ObjectReader> iterator = rowSet.iterator();
        if (iterator.hasNext()) {
          return iterator.next();
        } else {
          throw new NoSuchElementException();
        }
      })
      .onFailure(error -> System.out.println("Day is NOT found: " + error.getMessage()));
  }

  public Future<FundDTO> selectByLoginDate (PgPool dbClient, String login, DateDTO date) {

    JsonObject object = new JsonObject()
      .put("login", login)
      .put("dayFrom", date.getDayFrom().toLocalDate())
      .put("dayTo", date.getDayTo().toLocalDate());

    return SqlTemplate
      .forQuery(dbClient, SQL_SELECT_BALANCE_BY_LOGIN_DATE)
      .mapTo(FundDTO.class)
      .execute(object.getMap())
      .map(rowSet -> {
        final RowIterator<FundDTO> iterator = rowSet.iterator();

        if (iterator.hasNext()) {
          return iterator.next();
        } else {
          throw new NoSuchElementException(login);
        }
      })
      .onFailure(error -> System.out.println("Balance for this User and Day is NOT found: " + error.getMessage()));
  }

  public Future<List<FundDTO>> selectByLoginDates (PgPool dbClient, String login, DateDTO date) {

    JsonObject object = new JsonObject()
      .put("login", login)
      .put("dayFrom", date.getDayFrom().toLocalDate())
      .put("dayTo", date.getDayTo().toLocalDate());

    return SqlTemplate
      .forQuery(dbClient, SQL_SELECT_BALANCE_BY_LOGIN_DATES)
      .mapTo(FundDTO.class)
      .execute(object.getMap())
      .map(rowSet -> {

        final List<FundDTO> funds = new ArrayList<>();
        rowSet.forEach(funds::add);

//        while (rowSet.iterator().hasNext()) {
//           FundDTO fundDTO = new FundDTO(rowSet.iterator().next().getBalance(),
//             rowSet.iterator().next().getSpent(),
//             rowSet.iterator().next().getReceived(),
//             rowSet.iterator().next().getDay());
//
//           funds.add(fundDTO);
//        }

        return funds;
      })
      .onFailure(error -> System.out.println("Balance for this User and Day is NOT found: " + error.getMessage()));
  }

  public Future<FundDTO> selectCurrentBalanceID (PgPool dbClient, String login) {
    JsonObject object = new JsonObject()
      .put("login", login);

    return SqlTemplate
      .forQuery(dbClient, SQL_SELECT_CURRENT_BALANCE_ID)
      .mapTo(FundDTO.class)
      .execute(object.getMap())
      .map(rowSet -> {
        final RowIterator<FundDTO> iterator = rowSet.iterator();

        if (iterator.hasNext()) {
          return iterator.next();
        } else {
          throw new NoSuchElementException(login);
        }
      })
      .onFailure(error -> System.out.println("Balance ID for this User NOT found: " + error.getMessage()));
  }

  public Future<FundDTO> selectCurrentBalance (PgPool dbClient, String login, int id) {
    JsonObject object = new JsonObject()
      .put("login", login)
      .put("id", id);

    return SqlTemplate
      .forQuery(dbClient, SQL_SELECT_CURRENT_BALANCE)
      .mapTo(FundDTO.class)
      .execute(object.getMap())
      .map(rowSet -> {
        final RowIterator<FundDTO> iterator = rowSet.iterator();

        if (iterator.hasNext()) {
          return iterator.next();
        } else {
          throw new NoSuchElementException(login);
        }
      })
      .onFailure(error -> System.out.println("Balance for this User NOT found: " + error.getMessage()));
  }

  public Future<Object> sendOperation(PgPool dbClient, String sender, int id, OperationDTO operation) {

    JsonObject senderExecute = new JsonObject()
      .put("login", sender)
      .put("id", id)
      .put("money_amount", operation.getMoney_amount());

    return SqlTemplate
      .forUpdate(dbClient, SQL_UPDATE_BALANCE_SENDER)
      .execute(senderExecute.getMap())
      .flatMap(rowSet -> {
        if (rowSet.rowCount() > 0) {
          return Future.succeededFuture();
        } else {
          return Future.failedFuture(new NoSuchElementException());
        }
      })
      .onSuccess(res -> {
        System.out.println("Send operation is successful");
      })
      .onFailure(res -> {
        System.out.println("Send operation failed: " + res.getMessage());
      });
  }

  public Future<Object> receiveOperation(PgPool dbClient, OperationDTO operation, int id) {

    JsonObject receiverExecute = new JsonObject()
      .put("login", operation.getReceiver())
      .put("id", id)
      .put("money_amount", operation.getMoney_amount());

    return SqlTemplate
      .forUpdate(dbClient, SQL_UPDATE_BALANCE_RECEIVER)
      .execute(receiverExecute.getMap())
      .flatMap(rowSet -> {
        if (rowSet.rowCount() > 0) {
          return Future.succeededFuture();
        } else {
          return Future.failedFuture(new NoSuchElementException());
        }
      })
      .onSuccess(res -> {
        System.out.println("Receive operation is successful");
      })
      .onFailure(res -> {
        System.out.println("Receive operation failed: " + res.getMessage());
      });
  }

  public Future<String> sendMoney (PgPool dbClient, String sender, OperationDTO operation, int id) {

//    JsonObject senderExecute = new JsonObject()
//      .put("login", sender)
//      .put("id", id);
//
//    JsonObject receiverExecute = new JsonObject()
//      .put("login", operation.getReceiver())
//      .put("id", id);

    return dbClient.withTransaction(client -> sendOperation(dbClient, sender, id, operation)
        .flatMap(res -> receiveOperation(dbClient, operation, id)
          .map("Transaction is successful")))
      .onSuccess(v -> System.out.println("Transaction succeeded"))
      .onFailure(err -> System.out.println("Transaction failed: " + err.getMessage()));

//    return dbClient.withTransaction(client -> client
//        .query(SQL_UPDATE_BALANCE_SENDER)
//        .execute()
//        .flatMap(res -> client
//          .query(SQL_UPDATE_BALANCE_RECEIVER)
//          .execute()
//          .map("Transaction is successful")))
//      .onSuccess(v -> System.out.println("Transaction succeeded"))
//      .onFailure(err -> System.out.println("Transaction failed: " + err.getMessage()));
  }

  /** Operation - is transaction of money between users. */
  public Future<Object> deleteOperation (PgPool dbClient, int id) {
    return SqlTemplate
      .forUpdate(dbClient, SQL_DELETE_OPERATION)
      .execute(Collections.singletonMap("id", id))
      .flatMap(rowSet -> {
        if (rowSet.rowCount() > 0) {
          return Future.succeededFuture();
        } else {
          throw new NoSuchElementException();
        }
      })
      .onSuccess(res -> {
        System.out.println("Delete operation is successful");
      })
      .onFailure(res -> {
        System.out.println("Delete operation failed: " + res.getMessage());
      });
  }

  /** For deleteOperation(). To check before deleting operation (operation - transaction of money between users).
   *  Both receiver and sender should delete operation in order to delete it from database. */
  public Future<Object> updateSenderStatus (PgPool dbClient, int id) {
    return SqlTemplate
      .forUpdate(dbClient, SQL_UPDATE_SENDER_STATUS)
      .execute(Collections.singletonMap("id", id))
      .flatMap(rowSet -> {
        if (rowSet.rowCount() > 0) {
          return Future.succeededFuture();
        } else {
          throw new NoSuchElementException();
        }
      })
      .onSuccess(res -> {
        System.out.println("Sender status update is successful");
      })
      .onFailure(res -> {
        System.out.println("Sender status update failed: " + res.getMessage());
      });
  }

  /** For deleteOperation(). To check before deleting operation (operation - transaction of money between users).
   *  Both receiver and sender should delete operation in order to delete it from database. */
  public Future<Object> updateReceiverStatus (PgPool dbClient, int id) {
    return SqlTemplate
      .forUpdate(dbClient, SQL_UPDATE_RECEIVER_STATUS)
      .execute(Collections.singletonMap("id", id))
      .flatMap(rowSet -> {
        if (rowSet.rowCount() > 0) {
          return Future.succeededFuture();
        } else {
          throw new NoSuchElementException();
        }
      })
      .onSuccess(res -> {
        System.out.println("Receiver status update is successful");
      })
      .onFailure(res -> {
        System.out.println("Receiver status update failed: " + res.getMessage());
      });
  }

}
