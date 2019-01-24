/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.api.service;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import io.nuls.api.bean.annotation.Autowired;
import io.nuls.api.bean.annotation.Component;
import io.nuls.api.core.constant.MongoTableName;
import io.nuls.api.core.model.KeyValue;
import io.nuls.api.core.model.StatisticalInfo;
import io.nuls.api.core.mongodb.MongoDBService;
import io.nuls.api.core.util.DocumentTransferTool;
import io.nuls.sdk.core.utils.DoubleUtils;
import org.bson.Document;

import java.util.*;

import static com.mongodb.client.model.Filters.*;

/**
 * @author Niels
 */
@Component
public class StatisticalService {
    @Autowired
    private MongoDBService mongoDBService;

    public long getBestId() {
        Document document = mongoDBService.findOne(MongoTableName.NEW_INFO, Filters.eq("_id", MongoTableName.LAST_STATISTICAL_TIME));
        if (null == document) {
            return 0;
        }
        return document.getLong("value");
    }

    public void saveBestId(long id) {
        Document document = new Document();
        document.put("_id", MongoTableName.LAST_STATISTICAL_TIME);
        document.put("value", id);
        mongoDBService.insertOne(MongoTableName.NEW_INFO, document);
    }

    public void updateBestId(long id) {
        Document document = new Document();
        document.put("_id", MongoTableName.LAST_STATISTICAL_TIME);
        document.put("value", id);
        mongoDBService.updateOne(MongoTableName.NEW_INFO, Filters.eq("_id", MongoTableName.LAST_STATISTICAL_TIME), document);
    }

    public void insert(StatisticalInfo info) {
        Document document = DocumentTransferTool.toDocument(info, "time");
        mongoDBService.insertOne(MongoTableName.STATISTICAL_INFO, document);
    }

    public long calcTxCount(long start, long end) {

        long count = this.mongoDBService.getCount(MongoTableName.TX_INFO, and(gte("createTime", start), lte("createTime", end)));

        return count;
    }

    /**
     * @param type 0:14天，1:周，2：月，3：年，4：全部
     * @return
     */
    public List getStatisticalList(int type, String field) {
        List<KeyValue> list = new ArrayList<>();
        long startTime = getStartTime(type);
        List<Document> documentList = mongoDBService.query(MongoTableName.STATISTICAL_INFO, gte("_id", startTime), Sorts.ascending("_id"));
        if (documentList.size() < 32) {
            for (Document document : documentList) {
                KeyValue keyValue = new KeyValue();
                keyValue.setKey(document.get("month") + "/" + document.get("date"));
                keyValue.setValue(document.getLong(field));
                list.add(keyValue);
            }
        } else {
            if ("txCount".equals(field)) {
                summaryLong(list, documentList, field);
            } else if ("annualizedReward".equals(field)) {
                avgDouble(list, documentList, field);
            } else {
                avgLong(list, documentList, field);
            }
        }
        return list;
    }

    private void summaryLong(List<KeyValue> list, List<Document> documentList, String field) {
        List<String> keyList = new ArrayList<>();
        Map<String, Long> map = new HashMap<>();

        for (Document document : documentList) {
            String key = document.get("year") + "/" + document.get("month");
            Long value = map.get(key);
            if (null == value) {
                value = 0L;
                keyList.add(key);
            }
            value += document.getLong(field);
            map.put(key, value);
        }
        for (String key : keyList) {
            KeyValue keyValue = new KeyValue();
            keyValue.setKey(key);
            keyValue.setValue(map.get(key));
            list.add(keyValue);
        }
    }

    private void avgLong(List<KeyValue> list, List<Document> documentList, String field) {
        List<String> keyList = new ArrayList<>();
        Map<String, List<Long>> map = new HashMap<>();

        for (Document document : documentList) {
            String key = document.get("year") + "/" + document.get("month");
            List<Long> value = map.get(key);
            if (null == value) {
                value = new ArrayList<>();
                keyList.add(key);
                map.put(key, value);
            }
            value.add(Long.parseLong(document.get(field) + ""));
        }
        for (String key : keyList) {
            KeyValue keyValue = new KeyValue();
            keyValue.setKey(key);
            long value = 0;
            List<Long> valueList = map.get(key);
            for (long val : valueList) {
                value += val;
            }
            keyValue.setValue(value / valueList.size());
            list.add(keyValue);
        }
    }

    private void avgDouble(List<KeyValue> list, List<Document> documentList, String field) {
        List<String> keyList = new ArrayList<>();
        Map<String, List<Double>> map = new HashMap<>();

        for (Document document : documentList) {
            String key = document.get("year") + "/" + document.get("month");
            List<Double> value = map.get(key);
            if (null == value) {
                value = new ArrayList<>();
                keyList.add(key);
                map.put(key, value);
            }
            value.add(document.getDouble(field));
        }
        for (String key : keyList) {
            KeyValue keyValue = new KeyValue();
            keyValue.setKey(key);
            double value = 0;
            List<Double> valueList = map.get(key);
            for (double val : valueList) {
                value += val;
            }
            keyValue.setValue(DoubleUtils.div(value, valueList.size(), 2));
            list.add(keyValue);
        }
    }

    private long getStartTime(int type) {
        if (4 == type) {
            return 0;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        switch (type) {
            case 0:
                calendar.add(Calendar.DATE, -14);
                break;
            case 1:
                calendar.add(Calendar.DATE, -7);
                break;
            case 2:
                calendar.add(Calendar.MONTH, -1);
                break;
            case 3:
                calendar.add(Calendar.YEAR, -1);
                break;
        }
        return calendar.getTime().getTime();
    }
}
