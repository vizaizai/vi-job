package com.github.vizaizai.remote.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author liaochongwei
 * @date 2023/4/20 15:16
 */
public class RpcFuture implements Future<Object> {
    /**
     * 请求id
     */
    private final String requestId;
    /**
     * 响应
     */
    private Object response;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    public RpcFuture(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        return countDownLatch.getCount() == 0;
    }

    @Override
    public Object get() throws InterruptedException {
        countDownLatch.await();
        return response;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException {
        boolean await = countDownLatch.await(timeout, unit);
        if (await) {
            return response;
        }
        throw new RuntimeException("Timeout exception. Request id: " + this.requestId);
    }

    public void done(Object response) {
        this.response = response;
        countDownLatch.countDown();
    }

}
