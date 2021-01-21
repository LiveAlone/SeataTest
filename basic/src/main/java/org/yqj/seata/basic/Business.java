package org.yqj.seata.basic;

import io.seata.rm.RMClient;
import io.seata.tm.TMClient;
import io.seata.tm.api.GlobalTransaction;
import io.seata.tm.api.GlobalTransactionContext;
import org.yqj.seata.basic.service.AccountService;
import org.yqj.seata.basic.service.OrderService;
import org.yqj.seata.basic.service.StorageService;
import org.yqj.seata.basic.service.impl.AccountServiceImpl;
import org.yqj.seata.basic.service.impl.OrderServiceImpl;
import org.yqj.seata.basic.service.impl.StorageServiceImpl;

import java.util.concurrent.TimeUnit;

/**
 * Description:
 *
 * @author yaoqijun
 * @date 2021/1/20
 * Email: yaoqijunmail@foxmail.com
 */
public class Business {

    public static void main(String[] args) throws Exception {

        String userId = "U100001";
        String commodityCode = "C00321";
        int commodityCount = 100;
        int money = 999;

        AccountService accountService = new AccountServiceImpl();
        StorageService storageService = new StorageServiceImpl();
        OrderService orderService = new OrderServiceImpl();
        orderService.setAccountService(accountService);

        // 清空重置数据库
        accountService.reset(userId, String.valueOf(money));
        storageService.reset(commodityCode, String.valueOf(commodityCount));
        orderService.reset(null, null);

        //init seata; only once
        String applicationId = "basic";
        String txServiceGroup = "my_test_tx_group";
        TMClient.init(applicationId, txServiceGroup);
        RMClient.init(applicationId, txServiceGroup);

        //trx
        GlobalTransaction tx = GlobalTransactionContext.getCurrentOrCreate();
        try {
            tx.begin(60000, "testBiz");
            System.out.println("begin trx, xid is " + tx.getXid());

            //biz operate 3 dataSources
            //set >=5 will be rollback(200*5>999) else will be commit
            int opCount = 5;
            storageService.deduct(commodityCode, opCount);
            orderService.create(userId, commodityCode, opCount);

            //check data if negative
            boolean needCommit = ((StorageServiceImpl)storageService).validNegativeCheck("count", commodityCode)
                    && ((AccountServiceImpl)accountService).validNegativeCheck("money", userId);

            //if data negative rollback else commit
            if (needCommit) {
                tx.commit();
            } else {
                System.out.println("rollback trx, cause: data negative, xid is " + tx.getXid());
                tx.rollback();
            }
        } catch (Exception exx) {
            System.out.println("rollback trx, cause: " + exx.getMessage() + " , xid is " + tx.getXid());
            tx.rollback();
            throw exx;
        }
        TimeUnit.SECONDS.sleep(100);
    }
}
