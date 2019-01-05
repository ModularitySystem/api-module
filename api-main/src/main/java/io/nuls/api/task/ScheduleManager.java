package io.nuls.api.task;

import io.nuls.api.bean.annotation.Autowired;
import io.nuls.api.bean.annotation.Component;
import io.nuls.api.core.constant.MongoTableName;
import io.nuls.api.core.mongodb.MongoDBService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ScheduleManager {

    private ScheduledExecutorService executorService;

    @Autowired
    private SyncBlockTask syncBlockTask;
    @Autowired
    private MongoDBService mongoDBService;

    public void start() {

//        mongoDBService.dropTable(MongoTableName.BLOCK_HEADER);
//        mongoDBService.dropTable(MongoTableName.NEW_INFO);
//        mongoDBService.dropTable(MongoTableName.AGENT_INFO);
//        mongoDBService.dropTable(MongoTableName.ALIAS_INFO);
//        mongoDBService.dropTable(MongoTableName.ACCOUNT_INFO);
//        mongoDBService.dropTable(MongoTableName.DEPOSIT_INFO);
//        mongoDBService.dropTable(MongoTableName.TX_RELATION_INFO);
//        mongoDBService.dropTable(MongoTableName.PUNISH_INFO);
        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(syncBlockTask,1,10, TimeUnit.SECONDS);
    }
}
