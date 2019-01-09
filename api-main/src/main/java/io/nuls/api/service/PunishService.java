package io.nuls.api.service;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import io.nuls.api.bean.annotation.Autowired;
import io.nuls.api.bean.annotation.Component;
import io.nuls.api.core.constant.MongoTableName;
import io.nuls.api.core.model.PunishLog;
import io.nuls.api.core.mongodb.MongoDBService;
import io.nuls.api.core.util.DocumentTransferTool;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;

@Component
public class PunishService {

    @Autowired
    private MongoDBService mongoDBService;


    public void savePunishList(List<PunishLog> punishLogList) {
        if (punishLogList.isEmpty()) {
            return;
        }

        List<Document> documentList = new ArrayList<>();
        for (PunishLog punishLog : punishLogList) {
            documentList.add(DocumentTransferTool.toDocument(punishLog));
        }

        mongoDBService.insertMany(MongoTableName.PUNISH_INFO, documentList);
    }

    /**
     * 根据类型和地址获取红黄牌列表
     *
     * @param type    1：yellow,2:red
     * @param address
     * @return
     */
    public List<PunishLog> getPunishLogList(int type, String address, long currentRoundIndex) {
        Bson filter = and(eq("type", type), eq("address", address));
        if (type == 0) {
            filter = and(filter, gte("roundIndex", currentRoundIndex));
        }
        List<Document> documentList = mongoDBService.query(MongoTableName.PUNISH_INFO, filter, Sorts.descending("height"));
        List<PunishLog> punishLogList = new ArrayList<>();
        for (Document document : documentList) {
            punishLogList.add(DocumentTransferTool.toInfo(document, PunishLog.class));
        }
        return punishLogList;
    }

}