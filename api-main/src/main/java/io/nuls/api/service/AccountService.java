package io.nuls.api.service;

import com.mongodb.client.model.*;
import io.nuls.api.bean.annotation.Autowired;
import io.nuls.api.bean.annotation.Component;
import io.nuls.api.core.constant.MongoTableName;
import io.nuls.api.core.model.AccountInfo;
import io.nuls.api.core.model.PageInfo;
import io.nuls.api.core.model.TxRelationInfo;
import io.nuls.api.core.mongodb.MongoDBService;
import io.nuls.api.core.util.DocumentTransferTool;
import org.bson.Document;
import org.bson.conversions.Bson;

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

    public PageInfo<AccountInfo> pageQuery(int pageNumber, int pageSize) {
        List<Document> docsList = this.mongoDBService.pageQuery(MongoTableName.ACCOUNT_INFO, pageNumber, pageSize);
        List<AccountInfo> accountInfoList = new ArrayList<>();
        long totalCount = mongoDBService.getCount(MongoTableName.ACCOUNT_INFO);
        for (Document document : docsList) {
            accountInfoList.add(DocumentTransferTool.toInfo(document, "address", AccountInfo.class));
        }
        PageInfo<AccountInfo> pageInfo = new PageInfo<>(pageNumber, pageSize, totalCount, accountInfoList);
        return pageInfo;
    }

    public PageInfo<TxRelationInfo> getAccountTxs(String address, int pageIndex, int pageSize, int type, boolean isMark) {
        Bson filter = null;
        Bson addressFilter = Filters.eq("address", address);

        if (type == 0 && isMark) {
            filter = Filters.and(addressFilter, Filters.ne("type", 1));
        } else if (type > 0) {
            filter = Filters.and(addressFilter, Filters.eq("type", type));
        }

        long totalCount = mongoDBService.getCount(MongoTableName.TX_RELATION_INFO, filter);
        List<Document> docsList = this.mongoDBService.pageQuery(MongoTableName.TX_RELATION_INFO, filter, Sorts.descending("height", "createTime"), pageIndex, pageSize);
        List<TxRelationInfo> txRelationInfoList = new ArrayList<>();
        for (Document document : docsList) {
            txRelationInfoList.add(DocumentTransferTool.toInfo(document, TxRelationInfo.class));
        }
        PageInfo<TxRelationInfo> pageInfo = new PageInfo<>(pageIndex, pageSize, totalCount, txRelationInfoList);
        return pageInfo;
    }

    public AccountInfo getAccount(String address) {
        Bson filter = Filters.eq("address", address);
        Document document = mongoDBService.findOne(MongoTableName.ACCOUNT_INFO, filter);
        if (document == null) {
            return null;
        }
        AccountInfo accountInfo = DocumentTransferTool.toInfo(document, "address", AccountInfo.class);
        return accountInfo;

    }
}
