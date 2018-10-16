package com.apm70.bizfuse.flume;

import java.util.List;

public interface IQueue<E> {

    int size();

    boolean add(E e);

    E poll();

    List<E> batchPeek();

    List<E> batchPoll();
}
