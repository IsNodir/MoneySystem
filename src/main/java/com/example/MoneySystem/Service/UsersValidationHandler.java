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

import static io.vertx.json.schema.common.dsl.Keywords.minLength;
import static io.vertx.json.schema.common.dsl.Keywords.pattern;
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
    final ObjectSchemaBuilder schemaBuilder = buildBodySchemaBuilder();

    return ValidationHandler
      .builder(schemaParser)
      .predicate(RequestPredicate.BODY_REQUIRED)
      .body(Bodies.json(schemaBuilder))
      .build();
  }

  public ValidationHandler deleteOperation() {
    final SchemaParser schemaParser = buildSchemaParser();
    final ObjectSchemaBuilder schemaBuilder = objectSchema()
      .requiredProperty("id", stringSchema())
      .requiredProperty("receiver", stringSchema().with(minLength(3)))
      .requiredProperty("sender", stringSchema().with(minLength(3)));

    return ValidationHandler
      .builder(schemaParser)
      .predicate(RequestPredicate.BODY_REQUIRED)
      .body(Bodies.json(schemaBuilder))
      .build();
  }

  public ValidationHandler balance() {
    /** pattern(Pattern.compile(...)) in requiredProperty() makes only dates of form dd.MM.yyyy acceptable
        The writing inside Pattern.compile() is called "regex"  */
    final SchemaParser schemaParser = buildSchemaParser();
    final ObjectSchemaBuilder schemaBuilder = objectSchema()
      .requiredProperty("dayFrom", stringSchema().with(pattern(Pattern.compile("(0?[1-9]|[12][0-9]|3[01])\\.(0?[1-9]|1[012])\\.((?:19|20)[0-9][0-9])"))))
      .requiredProperty("dayTo", stringSchema().with(pattern(Pattern.compile("(0?[1-9]|[12][0-9]|3[01])\\.(0?[1-9]|1[012])\\.((?:19|20)[0-9][0-9])"))));
      //.requiredProperty("day", stringSchema().with(format(StringFormat.DATE)).with(pattern(Pattern.compile("dd.MM.yyyy"))))

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
