package com.aol.cyclops2.data.collections.extensions.lazy.immutable;


import cyclops.Reducers;
import cyclops.collections.SetX;

import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.stream.ReactiveSeq;
import org.pcollections.PSet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * An extended List type {@see java.util.List}
 * Extended List operations execute lazily e.g.
 * <pre>
 * {@code
 *    LazyListX<Integer> q = LazyListX.of(1,2,3)
 *                                      .map(i->i*2);
 * }
 * </pre>
 * The map operation above is not executed immediately. It will only be executed when (if) the data inside the
 * queue is accessed. This allows lazy operations to be chained and executed more efficiently e.g.
 *
 * <pre>
 * {@code
 *    DequeX<Integer> q = DequeX.of(1,2,3)
 *                              .map(i->i*2);
 *                              .filter(i->i<5);
 * }
 * </pre>
 *
 * The operation above is more efficient than the equivalent operation with a ListX.
 *
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this collection
 */
public class LazyPSetX2<T> extends AbstractLazyPersistentCollection2<T,PSet<T>> implements SetX<T> {


    public LazyPSetX2(PSet<T> list, ReactiveSeq<T> seq) {
        super(list, seq, Reducers.toPSet());
        

    }
    public LazyPSetX2(PSet<T> list, ReactiveSeq<T> seq, Reducer<PSet<T>> reducer) {
        super(list, seq, reducer);


    }
    public LazyPSetX2(PSet<T> list) {
        super(list, null, Reducers.toPSet());
        
    }

    public LazyPSetX2(ReactiveSeq<T> seq) {
        super(null, seq, Reducers.toPSet());
       

    }


    @Override
    public SetX<T> persistent() {
        return this;
    }

    @Override
    public SetX<T> persistent(Reducer<? extends Set<T>> reducer) {
        return new LazyPSetX2<T>(getList(),getSeq().get(),(Reducer)reducer);
    }

    //@Override
    public SetX<T> materialize() {
        get();
        return this;
    }

    @Override
    public SetX<T> take(long num) {
        return null;
    }

    @Override
    public SetX<T> drop(long num) {
        return null;
    }

    @Override
    public SetX<T> withCollector(Collector<T, ?, Set<T>> collector) {
        return null;
    }

    @Override
    public SetX<T> combine(Monoid<T> op, BiPredicate<? super T, ? super T> predicate) {
        return null;
    }


    //  @Override
    public <X> SetX<X> fromStream(Stream<X> stream) {

        return new LazyPSetX2<X>((PSet)getList(),ReactiveSeq.fromStream(stream));
    }

    @Override
    public <T1> SetX<T1> from(Collection<T1> c) {
        if(c instanceof PSet)
            return new LazyPSetX2<T1>((PSet)c,null);
        return fromStream(ReactiveSeq.fromIterable(c));
    }

    @Override
    public <T1> Collector<T1, ?, Set<T1>> getCollector() {
        return null;
    }


    @Override
    public SetX<T> plus(T e) {
        return from(get().plus(e));
    }

    @Override
    public SetX<T> plusAll(Collection<? extends T> list) {
        return from(get().plusAll(list));
    }


    @Override
    public SetX<T> minusAll(Collection<?> list) {
        return from(get().minusAll(list));
    }


    @Override
    public SetX<T> minus(Object remove) {
        return from(get().minus(remove));
    }


    

    @Override
    public <U> SetX<U> unitIterator(Iterator<U> it) {
        return fromStream(ReactiveSeq.fromIterator(it));
    }



    @Override
    public <R> SetX<R> unit(Collection<R> col) {
        return from(col);
    }

    @Override
    public SetX<T> plusLoop(int max, IntFunction<T> value) {
        return (SetX<T>)super.plusLoop(max,value);
    }

    @Override
    public SetX<T> plusLoop(Supplier<Optional<T>> supplier) {
        return (SetX<T>)super.plusLoop(supplier);
    }
}
