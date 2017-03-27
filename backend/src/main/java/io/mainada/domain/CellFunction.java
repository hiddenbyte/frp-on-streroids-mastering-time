package io.mainada.domain;

import rx.Observable;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

public enum CellFunction {
    SUM("sum") {
        @Override
        public Observable<Double> apply(final Observable<Double> values) {
            return values.reduce(0.0d, (acc, ele) -> acc + ele);
        }
    },
    PRODUCT("product") {
        @Override
        public Observable<Double> apply(final Observable<Double> values) {
            return values.reduce(0.0d, (acc, ele) -> acc * ele);
        }
    },
    SQRT("sqrt") {
        @Override
        public Observable<Double> apply(final Observable<Double> values) {
            return values
                    .first()
                    .map(Math::sqrt)
                    .single();
        }
    },
    QUOTIENT("quotient") {
        @Override
        public Observable<Double> apply(final Observable<Double> values) {
            return values
                    .take(2)
                    .reduce(0.0d, (acc, ele) -> acc / ele)
                    .map(Math::floor);
        }
    },
    MOD("mod") {
        @Override
        public Observable<Double> apply(final Observable<Double> values) {
            return values
                    .take(2)
                    .reduce(0.0d, (acc, ele) -> acc % ele)
                    .map(Math::floor);
        }
    };

    private static final CellFunction[] VALUES = CellFunction.values();

    private String name;

    CellFunction(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private static final Map<String, CellFunction> CONVERTER = Arrays.stream(VALUES)
            .collect(Collectors.toMap(CellFunction::getName, identity()));

    /**
     * converts an id to a Related To
     */
    public static Optional<CellFunction> getRelatedToFromId(String relatedToID) {
        return Optional.ofNullable(CONVERTER.get(relatedToID));
    }

    public abstract Observable<Double> apply(Observable<Double> values);
}
