package com.github.vizaizai.server.router;

import com.github.vizaizai.server.router.impl.BroadcastRouter;
import com.github.vizaizai.server.router.impl.FailoverRouter;
import com.github.vizaizai.server.router.impl.IdleRouter;
import com.github.vizaizai.server.router.impl.RandomRouter;

/**
 * 路由策略
 * @author liaochongwei
 * @date 2023/5/19 15:02
 */
public enum RouteType {
    RANDOM(1, new RandomRouter()),
    FAILOVER(2, new FailoverRouter()),
    Idle(3, new IdleRouter()),
    BROADCAST(4, new BroadcastRouter()),
    ;
    private final int code;
    private final NodeRouter router;

    RouteType(int code, NodeRouter router) {
        this.code = code;
        this.router = router;
    }

    public static RouteType getInstance(int code) {
        for (RouteType triggerType : RouteType.values()) {
            if (triggerType.getCode() == code) {
                return triggerType;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public NodeRouter getRouter() {
        return router;
    }
}
