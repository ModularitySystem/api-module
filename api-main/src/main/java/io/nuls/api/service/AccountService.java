package io.nuls.api.service;

import com.mongodb.client.model.*;
import io.nuls.api.bean.annotation.Autowired;
import io.nuls.api.bean.annotation.Component;
import io.nuls.api.core.constant.MongoTableName;
import io.nuls.api.core.model.AccountInfo;
import io.nuls.api.core.mongodb.MongoDBService;
import io.nuls.api.core.util.DocumentTransferTool;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AccountService {

    @Autowired
    private MongoDBService mongoDBService;

    public void saveAccounts(Map<String, AccountInfo> accountInfoMap) {
        if (accountInfoMap.isEmpty()) {
            return;
        }
        List<WriteModel<Document>> modelList = new ArrayList<>();
        for (AccountInfo accountInfo : accountInfoMap.values()) {
            Document document = DocumentTransferTool.toDocument(accountInfo, "address");
            if (accountInfo.isNew()) {
                modelList.add(new InsertOneModel(document));
            } else {
                modelList.add(new ReplaceOneModel<>(Filters.eq("_id", accountInfo.getAddress()), document));
            }
        }
        mongoDBService.bulkWrite(MongoTableName.ACCOUNT_INFO, modelList);
    }

    public AccountInfo getAccountInfoByAddress(String address) {
        Document document = mongoDBService.findOne(MongoTableName.ACCOUNT_INFO, Filters.eq("_id", address));
        if (document == null) {
            return null;
        }
        AccountInfo accountInfo = DocumentTransferTool.toInfo(document, "address", AccountInfo.class);
        return accountInfo;
    }

    public List<AccountInfo> pageQuery(int pageNumber, int pageSize) {
        List<Document> docsList = this.mongoDBService.pageQuery(MongoTableName.BLOCK_HEADER, Sorts.descending("height"), pageNumber, pageSize);
        List<AccountInfo> accountInfoList = new ArrayList<>();
        for (Document document : docsList) {
            accountInfoList.add(DocumentTransferTool.toInfo(document, "address", AccountInfo.class));
        }
        return accountInfoList;
    }
}