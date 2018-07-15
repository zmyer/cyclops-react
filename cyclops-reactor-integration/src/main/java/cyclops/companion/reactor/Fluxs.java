package cyclops.companion.reactor;

import cyclops.control.Either;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import lombok.experimental.UtilityClass;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * Companion class for working with Reactor Flux types
 *
 * @author johnmcclean
 *
 */
@UtilityClass
public class Fluxs {


    public static  <T,R> Flux<R> tailRec(T initial, Function<? super T, ? extends Flux<? extends Either<T, R>>> fn) {
        Flux<Either<T, R>> next = Flux.just(Either.left(initial));

        boolean newValue[] = {true};
        for(;;){

            next = next.flatMap(e -> e.fold(s -> {
                        newValue[0]=true;
                        return fn.apply(s); },
                    p -> {
                        newValue[0]=false;
                        return Flux.just(e);
                    }));
            if(!newValue[0])
                break;

        }

        return next.filter(Either::isRight).map(e->e.orElse(null));
    }
    public static <T> Flux<T> narrow(Flux<? extends T> observable) {
        return (Flux<T>)observable;
    }
    public static  <T> Flux<T> fluxFrom(ReactiveSeq<T> stream){

        return stream.fold(sync->Flux.fromStream(stream), rs->Flux.from(stream), async->Flux.from(stream));


    }

    public static <T> Flux<T> generate(Supplier<? extends T> supplier){
        return Flux.from(Spouts.generate(supplier));
    }

    /**
     * Perform a For Comprehension over a Flux, accepting 3 generating functions.
     * This results in a four level nested internal iteration over the provided Publishers.
     *
     *  <pre>
      * {@code
      *
      *   import static cyclops.companion.reactor.Fluxs.forEach4;
      *
          forEach4(Flux.range(1,10),
                  a-> ReactiveSeq.iterate(a,i->i+1).limit(10),
                  (a,b) -> Maybe.<Integer>of(a+b),
                  (a,b,c) -> Mono.<Integer>just(a+b+c),
                  Tuple::tuple)
     *
     * }
     * </pre>
     *
     * @param value1 top level Flux
     * @param value2 Nested publisher
     * @param value3 Nested publisher
     * @param value4 Nested publisher
     * @param yieldingFunction  Generates a result per combination
     * @return Flux with an element per combination of nested publishers generated by the yielding function
     */
    public static <T1, T2, T3, R1, R2, R3, R> Flux<R> forEach4(Flux<? extends T1> value1,
                                                               Function<? super T1, ? extends Publisher<R1>> value2,
            BiFunction<? super T1, ? super R1, ? extends Publisher<R2>> value3,
            Function3<? super T1, ? super R1, ? super R2, ? extends Publisher<R3>> value4,
            Function4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Flux<R1> a = Flux.from(value2.apply(in));
            return a.flatMap(ina -> {
                Flux<R2> b = Flux.from(value3.apply(in,ina));
                return b.flatMap(inb -> {
                    Flux<R3> c = Flux.from(value4.apply(in,ina,inb));
                    return c.map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });


    }

    /**
     * Perform a For Comprehension over a Flux, accepting 3 generating functions.
     * This results in a four level nested internal iteration over the provided Publishers.
     * <pre>
     * {@code
     *
     *  import static cyclops.companion.reactor.Fluxs.forEach4;
     *
     *  forEach4(Flux.range(1,10),
                            a-> ReactiveSeq.iterate(a,i->i+1).limit(10),
                            (a,b) -> Maybe.<Integer>just(a+b),
                            (a,b,c) -> Mono.<Integer>just(a+b+c),
                            (a,b,c,d) -> a+b+c+d <100,
                            Tuple::tuple);
     *
     * }
     * </pre>
     *
     * @param value1 top level Flux
     * @param value2 Nested publisher
     * @param value3 Nested publisher
     * @param value4 Nested publisher
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return Flux with an element per combination of nested publishers generated by the yielding function
     */
    public static <T1, T2, T3, R1, R2, R3, R> Flux<R> forEach4(Flux<? extends T1> value1,
            Function<? super T1, ? extends Publisher<R1>> value2,
            BiFunction<? super T1, ? super R1, ? extends Publisher<R2>> value3,
            Function3<? super T1, ? super R1, ? super R2, ? extends Publisher<R3>> value4,
            Function4<? super T1, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            Function4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Flux<R1> a = Flux.from(value2.apply(in));
            return a.flatMap(ina -> {
                Flux<R2> b = Flux.from(value3.apply(in,ina));
                return b.flatMap(inb -> {
                    Flux<R3> c = Flux.from(value4.apply(in,ina,inb));
                    return c.filter(in2->filterFunction.apply(in,ina,inb,in2))
                            .map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
    }

    /**
     * Perform a For Comprehension over a Flux, accepting 2 generating functions.
     * This results in a three level nested internal iteration over the provided Publishers.
     *
     * <pre>
     * {@code
     *
     * import static cyclops.companion.reactor.Fluxs.forEach;
     *
     * forEach(Flux.range(1,10),
                            a-> ReactiveSeq.iterate(a,i->i+1).limit(10),
                            (a,b) -> Maybe.<Integer>of(a+b),
                            Tuple::tuple);
     *
     * }
     * </pre>
     *
     *
     * @param value1 top level Flux
     * @param value2 Nested publisher
     * @param value3 Nested publisher
     * @param yieldingFunction Generates a result per combination
     * @return Flux with an element per combination of nested publishers generated by the yielding function
     */
    public static <T1, T2, R1, R2, R> Flux<R> forEach3(Flux<? extends T1> value1,
            Function<? super T1, ? extends Publisher<R1>> value2,
            BiFunction<? super T1, ? super R1, ? extends Publisher<R2>> value3,
            Function3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Flux<R1> a = Flux.from(value2.apply(in));
            return a.flatMap(ina -> {
                Flux<R2> b = Flux.from(value3.apply(in, ina));
                return b.map(in2 -> yieldingFunction.apply(in, ina, in2));
            });


        });

    }
        /**
         * Perform a For Comprehension over a Flux, accepting 2 generating functions.
         * This results in a three level nested internal iteration over the provided Publishers.
         * <pre>
         * {@code
         *
         * import static cyclops.companion.reactor.Fluxs.forEach;
         *
         * forEach(Flux.range(1,10),
                       a-> ReactiveSeq.iterate(a,i->i+1).limit(10),
                       (a,b) -> Maybe.<Integer>of(a+b),
                       (a,b,c) ->a+b+c<10,
                       Tuple::tuple).toListX();
         * }
         * </pre>
         *
         * @param value1 top level Flux
         * @param value2 Nested publisher
         * @param value3 Nested publisher
         * @param filterFunction A filtering function, keeps values where the predicate holds
         * @param yieldingFunction Generates a result per combination
         * @return
         */
    public static <T1, T2, R1, R2, R> Flux<R> forEach3(Flux<? extends T1> value1,
            Function<? super T1, ? extends Publisher<R1>> value2,
            BiFunction<? super T1, ? super R1, ? extends Publisher<R2>> value3,
            Function3<? super T1, ? super R1, ? super R2, Boolean> filterFunction,
            Function3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Flux<R1> a = Flux.from(value2.apply(in));
            return a.flatMap(ina -> {
                Flux<R2> b = Flux.from(value3.apply(in,ina));
                return b.filter(in2->filterFunction.apply(in,ina,in2))
                        .map(in2 -> yieldingFunction.apply(in, ina, in2));
            });



        });

    }

    /**
     * Perform a For Comprehension over a Flux, accepting an additonal generating function.
     * This results in a two level nested internal iteration over the provided Publishers.
     *
     * <pre>
     * {@code
     *
     *  import static cyclops.companion.reactor.Fluxs.forEach;
     *  forEach(Flux.range(1, 10), i -> Flux.range(i, 10), Tuple::tuple)
              .subscribe(System.out::println);

       //(1, 1)
         (1, 2)
         (1, 3)
         (1, 4)
         ...
     *
     * }</pre>
     *
     * @param value1 top level Flux
     * @param value2 Nested publisher
     * @param yieldingFunction Generates a result per combination
     * @return
     */
    public static <T, R1, R> Flux<R> forEach(Flux<? extends T> value1, Function<? super T, Flux<R1>> value2,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Flux<R1> a = Flux.from(value2.apply(in));
            return a.map(in2 -> yieldingFunction.apply(in,  in2));
        });

    }

    /**
     *
     * <pre>
     * {@code
     *
     *   import static cyclops.companion.reactor.Fluxs.forEach;
     *
     *   forEach(Flux.range(1, 10), i -> Flux.range(i, 10),(a,b) -> a>2 && b<10,Tuple::tuple)
               .subscribe(System.out::println);

       //(3, 3)
         (3, 4)
         (3, 5)
         (3, 6)
         (3, 7)
         (3, 8)
         (3, 9)
         ...

     *
     * }</pre>
     *
     *
     * @param value1 top level Flux
     * @param value2 Nested publisher
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return
     */
    public static <T, R1, R> Flux<R> forEach(Flux<? extends T> value1,
            Function<? super T, ? extends Publisher<R1>> value2,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Flux<R1> a = Flux.from(value2.apply(in));
            return a.filter(in2->filterFunction.apply(in,in2))
                    .map(in2 -> yieldingFunction.apply(in,  in2));
        });

    }


}
