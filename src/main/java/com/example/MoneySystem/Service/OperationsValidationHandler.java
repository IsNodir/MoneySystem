package com.example.MoneySystem.Service;

import io.vertx.core.Vertx;
import io.vertx.ext.web.validation.RequestPredicate;
import io.vertx.ext.web.validation.ValidationHandler;
import io.vertx.ext.web.validation.builder.Bodies;
import io.vertx.json.schema.SchemaParser;
import io.vertx.json.schema.SchemaRouter;
import io.vertx.json.schema.SchemaRouterOptions;
import io.vertx.json.schema.common.dsl.ObjectSchemaBuilder;

import java.util.regex.Pattern;

import static io.vertx.json.schema.common.dsl.Keywords.pattern;
import static io.vertx.json.schema.common.dsl.Schemas.*;

public class OperationsValidationHandler {

  private final Vertx vertx;

  public OperationsValidationHandler(Vertx vertx) {
    this.vertx = vertx;
  }

  public ValidationHandler operationsNew() {
    final SchemaParser schemaParser = buildSchemaParser();
    final ObjectSchemaBuilder schemaBuilder = objectSchema()
      .requiredProperty("id_user", intSchema())
      .requiredProperty("amount", numberSchema())
      .requiredProperty("date", stringSchema().with(pattern(Pattern.compile("(0?[1-9]|[12][0-9]|3[01])\\.(0?[1-9]|1[012])\\.((?:19|20)[0-9][0-9])"))))
      .requiredProperty("is_operation", booleanSchema())
      .requiredProperty("is_expense", booleanSchema());

    return ValidationHandler
      .builder(schemaParser)
      .predicate(RequestPredicate.BODY_REQUIRED)
      .body(Bodies.json(schemaBuilder))
      .build();
  }

  public ValidationHandler operationsHistory() {
    final SchemaParser schemaParser = buildSchemaParser();
    final ObjectSchemaBuilder schemaBuilder = objectSchema()
      .requiredProperty("id_user", intSchema())
      .requiredProperty("dayFrom", stringSchema().with(pattern(Pattern.compile("(0?[1-9]|[12][0-9]|3[01])\\.(0?[1-9]|1[012])\\.((?:19|20)[0-9][0-9])"))))
      .requiredProperty("dayTo", stringSchema().with(pattern(Pattern.compile("(0?[1-9]|[12][0-9]|3[01])\\.(0?[1-9]|1[012])\\.((?:19|20)[0-9][0-9])"))));;

    return ValidationHandler
      .builder(schemaParser)
      .predicate(RequestPredicate.BODY_REQUIRED)
      .body(Bodies.json(schemaBuilder))
      .build();
  }

  public ValidationHandler operationsTransaction() {
    final SchemaParser schemaParser = buildSchemaParser();
    final ObjectSchemaBuilder schemaBuilder = objectSchema()
      .requiredProperty("id_sender", intSchema())
      .requiredProperty("id_receiver", intSchema())
      .requiredProperty("amount", numberSchema())
      .requiredProperty("date", stringSchema().with(pattern(Pattern.compile("(0?[1-9]|[12][0-9]|3[01])\\.(0?[1-9]|1[012])\\.((?:19|20)[0-9][0-9])"))));

    return ValidationHandler
      .builder(schemaParser)
      .predicate(RequestPredicate.BODY_REQUIRED)
      .body(Bodies.json(schemaBuilder))
      .build();
  }

  public ValidationHandler operationsDelete() {
    final SchemaParser schemaParser = buildSchemaParser();
    final ObjectSchemaBuilder schemaBuilder = objectSchema()
      .requiredProperty("id", intSchema());

    return ValidationHandler
      .builder(schemaParser)
      .predicate(RequestPredicate.BODY_REQUIRED)
      .body(Bodies.json(schemaBuilder))
      .build();
  }

  private SchemaParser buildSchemaParser() {
    return SchemaParser.createDraft7SchemaParser(SchemaRouter.create(vertx, new SchemaRouterOptions()));
  }


}
