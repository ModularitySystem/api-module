package io.nuls.api.service;

import com.mongodb.client.model.*;
import io.nuls.api.bean.annotation.Autowired;
import io.nuls.api.bean.annotation.Component;
import io.nuls.api.core.constant.MongoTableName;
import io.nuls.api.core.model.DepositInfo;
import io.nuls.api.core.mongodb.MongoDBService;
import io.nuls.api.core.util.DocumentTransferTool;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

@Component
public class DepositService {

    @Autowired
    private MongoDBService mongoDBService;


    public DepositInfo getDepositInfoByHash(String hash) {
        Document document = mongoDBService.findOne(MongoTableName.DEPOSIT_INFO, Filters.eq("txHash", hash));
        if (document == null) {
            return null;
        }
        DepositInfo depositInfo = DocumentTransferTool.toInfo(document, DepositInfo.class);
        return depositInfo;
    }


    public List<DepositInfo> getDepositListByAgentHash(String hash) {
        List<DepositInfo> depositInfos = new ArrayList<>();
        Bson bson = Filters.and(Filters.eq("agentHash", hash), Filters.eq("deleteHash", null), Filters.eq("type", 0));
        List<Document> documentList = mongoDBService.query(MongoTableName.DEPOSIT_INFO, bson);
        if (documentList == null && documentList.isEmpty()) {
            return depositInfos;
        }
        for (Document document : documentList) {
            DepositInfo depositInfo = DocumentTransferTool.toInfo(document, DepositInfo.class);
            depositInfos.add(depositInfo);
        }
        return depositInfos;
    }

    public List<DepositInfo> getCancelDepositListByAgentHash(String hash) {
        List<DepositInfo> depositInfos = new ArrayList<>();
        Bson bson = Filters.and(Filters.eq("agentHash", hash),  Filters.eq("type", 1));
        List<Document> documentList = mongoDBService.query(MongoTableName.DEPOSIT_INFO, bson);
        if (documentList == null && documentList.isEmpty()) {
            return depositInfos;
        }
        for (Document document : documentList) {
            DepositInfo depositInfo = DocumentTransferTool.toInfo(document, DepositInfo.class);
            depositInfos.add(depositInfo);
        }
        return depositInfos;
    }


    public void saveDepositList(List<DepositInfo> depositInfoList) {
        if (depositInfoList.isEmpty()) {
            return;
        }
        List<WriteModel<Document>> modelList = new ArrayList<>();

        for (DepositInfo depositInfo : depositInfoList) {
            Document document = DocumentTransferTool.toDocument(depositInfo);
            if (depositInfo.isNew()) {
                modelList.add(new InsertOneModel(document));
            } else {
                modelList.add(new ReplaceOneModel<>(Filters.eq("txHash", depositInfo.getTxHash()), document));
            }
        }

        mongoDBService.bulkWrite(MongoTableName.DEPOSIT_INFO, modelList);
    }

    public List<DepositInfo> getDepositList(long startHeight) {
        //todo 返回全部在此高度有效的委托列表 from cache.
        return new ArrayList<>();
    }
}