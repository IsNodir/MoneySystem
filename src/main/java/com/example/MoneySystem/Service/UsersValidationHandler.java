package com.example.MoneySystem.Service;

import io.vertx.core.Vertx;
import io.vertx.ext.web.validation.RequestPredicate;
import io.vertx.ext.web.validation.ValidationHandler;
import io.vertx.ext.web.validation.builder.Bodies;
import io.vertx.json.schema.SchemaParser;
import io.vertx.json.schema.SchemaRouter;
import io.vertx.json.schema.SchemaRouterOptions;
import io.vertx.json.schema.common.dsl.ObjectSchemaBuilder;

import static io.vertx.json.schema.common.dsl.Keywords.minLength;
import static io.vertx.json.schema.common.dsl.Schemas.*;

public class UsersValidationHandler {

  private final Vertx vertx;

  public UsersValidationHandler(Vertx vertx) {
    this.vertx = vertx;
  }

  public ValidationHandler create() {
    final SchemaParser schemaParser = buildSchemaParser();
    final ObjectSchemaBuilder schemaBuilder = buildBodySchemaBuilder();

    return ValidationHandler
      .builder(schemaParser)
      .predicate(RequestPredicate.BODY_REQUIRED)
      .body(Bodies.json(schemaBuilder))
      .build();
  }

  public ValidationHandler update() {
    final SchemaParser schemaParser = buildSchemaParser();
    final ObjectSchemaBuilder schemaBuilder = objectSchema()
      .requiredProperty("login", stringSchema().with(minLength(3)))
      .requiredProperty("password", stringSchema().with(minLength(8)))
      .requiredProperty("new_password", stringSchema().with(minLength(8)));

    return ValidationHandler
      .builder(schemaParser)
      .predicate(RequestPredicate.BODY_REQUIRED)
      .body(Bodies.json(schemaBuilder))
      .build();
  }

  private SchemaParser buildSchemaParser() {
    return SchemaParser.createDraft7SchemaParser(SchemaRouter.create(vertx, new SchemaRouterOptions()));
  }

  private ObjectSchemaBuilder buildBodySchemaBuilder() {

    ObjectSchemaBuilder objectSchemaBuilder = objectSchema()
      .requiredProperty("login", stringSchema().with(minLength(3)))
      .requiredProperty("password", stringSchema().with(minLength(8)));

    return objectSchemaBuilder;
  }

}
