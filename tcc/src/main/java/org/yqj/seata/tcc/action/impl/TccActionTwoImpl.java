package org.yqj.seata.tcc.action.impl;

import io.seata.rm.tcc.api.BusinessActionContext;
import org.yqj.seata.tcc.action.TccActionTwo;

/**
 * The type Tcc action two.
 *
 * @author zhangsen
 */
public class TccActionTwoImpl implements TccActionTwo {

    @Override
    public boolean prepare(BusinessActionContext actionContext, String b) {
        String xid = actionContext.getXid();
        System.out.println("TccActionTwo prepare, xid:" + xid);
        return true;
    }

    @Override
    public boolean commit(BusinessActionContext actionContext) {
        String xid = actionContext.getXid();
        System.out.println("TccActionTwo commit, xid:" + xid);
        return true;
    }

    @Override
    public boolean rollback(BusinessActionContext actionContext) {
        String xid = actionContext.getXid();
        System.out.println("TccActionTwo rollback, xid:" + xid);
        return true;
    }

}
