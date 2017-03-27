package io.mainada.domain;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import rx.Observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Cell {
    private String id;
    private Double value;
    private CellFunction function;
    private List<String> functionArguments;
    private Observable<Cell> functionCells;

    public Cell() {
        this.functionArguments = new ArrayList<>();
    }

    public Cell(final String id, final Double value) {
        this.id = id;
        this.value = value;
    }

    public Cell(final JsonObject cell) {
        this.id = cell.getString("id");
        this.value = cell.getDouble("value");
        this.function = CellFunction.getRelatedToFromId(cell.getString("function")).orElse(null);
        this.functionArguments = cell.getJsonArray("arguments", new JsonArray(Collections.emptyList())).stream()
                .map(String.class::cast)
                .collect(Collectors.toList());
    }

    public String getId() {
        return id;
    }

    public Double getValue() {
        return value;
    }

    public Optional<CellFunction> getFunction() {
        return Optional.ofNullable(function);
    }

    public List<String> getFunctionArguments() {
        return functionArguments == null ? Collections.emptyList() : functionArguments;
    }

    public Cell withId(final String id) {
        this.id = id;
        return this;
    }

    public Cell withValue(final Double value) {
        this.value = value;
        return this;
    }

    public Cell withFunction(final CellFunction function) {
        this.function = function;
        return this;
    }

    public Cell withFunctionArguments(final List<String> functionArguments) {
        this.functionArguments = functionArguments;
        return this;
    }

    public JsonObject toJsonObject() {
        return new JsonObject()
                .put("id", id)
                .put("value", value)
                .put("function", function == null ? null : function.getName())
                .put("functionArguments", getFunctionArguments().stream().reduce(new JsonArray(), JsonArray::add, JsonArray::addAll));
    }
}