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

package io.nuls.api.core.mongodb;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.*;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 */
public class MongoDBService {

    private MongoDatabase db;

    public MongoDBService(MongoDatabase db) {
        this.db = db;
        if (null == db) {
            throw new RuntimeException();
        }
    }

    public void createCollection(String collName) {
        db.createCollection(collName);
    }

    public MongoCollection<Document> getCollection(String collName) {
        MongoCollection<Document> collection = db.getCollection(collName);
        return collection;
    }

    public void insertOne(String collName, Document document) {
        MongoCollection<Document> collection = getCollection(collName);
        collection.insertOne(document);
    }

    public void insertOne(String collName, Map<String, Object> map) {
        Document doc = new Document();
        doc.putAll(map);
        this.insertOne(collName, doc);
    }

    public void insertMany(String collName, List<Document> docList) {
        if (null == docList || docList.isEmpty()) {
            return;
        }
        MongoCollection<Document> collection = getCollection(collName);
        collection.insertMany(docList);

    }

    public List<Document> getDocumentListOfCollection(String collName) {
        MongoCollection<Document> collection = getCollection(collName);
        //检索所有文档
        /**
         * 1. 获取迭代器FindIterable<Document>
         * 2. 获取游标MongoCursor<Document>
         * 3. 通过游标遍历检索出的文档集合
         * */
        FindIterable<Document> findIterable = collection.find();
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        List<Document> docList = new ArrayList<>();
        while (mongoCursor.hasNext()) {
            docList.add(mongoCursor.next());
        }
        return docList;
    }

    /**
     * for example (var1): eq("a",1)
     *
     * @param collName
     * @param var1
     * @return
     */
    public Document findOne(String collName, Bson var1) {
        MongoCollection<Document> collection = getCollection(collName);
//        collection.up
        return collection.find(var1).first();
    }

    public List<Document> query(String collName, Bson var1) {
        MongoCollection<Document> collection = getCollection(collName);
        FindIterable<Document> iterable = collection.find(var1);
        List<Document> list = new ArrayList<>();
        MongoCursor<Document> documentMongoCursor = iterable.iterator();
        while (documentMongoCursor.hasNext()) {
            list.add(documentMongoCursor.next());
        }
        return list;
    }

    public List<Document> query(String collName, Bson var1, Bson sort) {
        MongoCollection<Document> collection = getCollection(collName);

        FindIterable<Document> iterable = collection.find(var1).sort(sort);
        List<Document> list = new ArrayList<>();
        MongoCursor<Document> documentMongoCursor = iterable.iterator();
        while (documentMongoCursor.hasNext()) {
            list.add(documentMongoCursor.next());
        }
        return list;
    }

    public long updateOne(String collName, Bson var1, Document docs) {
        return this.updateOne(collName, var1, "$set", docs);
    }

    public long updateOne(String collName, Bson var1, String op, Document docs) {
        MongoCollection<Document> collection = getCollection(collName);
        return collection.updateOne(var1, new Document(op, docs)).getModifiedCount();

    }

    public long update(String collName, Bson var1, Document docs) {
        return this.update(collName, var1, "$set", docs);
    }

    public long update(String collName, Bson var1, String op, Document docs) {
        MongoCollection<Document> collection = getCollection(collName);
        return collection.updateMany(var1, new Document(op, docs)).getModifiedCount();
    }

    public long delete(String collName, Bson var1) {
        MongoCollection<Document> collection = getCollection(collName);
        return collection.deleteMany(var1).getDeletedCount();
    }

    public String createIndex(String collName, Document index) {
        MongoCollection<Document> collection = getCollection(collName);
        return collection.createIndex(index);
    }

    public List<String> createIndexes(String collName, List<IndexModel> indexModels) {
        MongoCollection<Document> collection = getCollection(collName);
        return collection.createIndexes(indexModels);
    }

    public void dropIndexes(String collName) {
        MongoCollection<Document> collection = getCollection(collName);
        collection.dropIndexes();
    }

    public void dropTable(String collName) {
        MongoCollection<Document> collection = getCollection(collName);
        collection.drop();
    }

    public ListIndexesIterable<Document> getIndexes(String collName) {
        MongoCollection<Document> collection = getCollection(collName);
        return collection.listIndexes();
    }


    public List<Document> pageQuery(String collName, int pageNumber, int pageSize) {
        return pageQuery(collName, null, null, pageNumber, pageSize);
    }

    public List<Document> pageQuery(String collName, Bson sort, int pageNumber, int pageSize) {
        return pageQuery(collName, null, sort, pageNumber, pageSize);
    }

    public List<Document> pageQuery(String collName, Bson var1, Bson sort, int pageNumber, int pageSize) {
        MongoCollection<Document> collection = getCollection(collName);
        //todo skip在大数据情况会非常慢
        FindIterable<Document> iterable;

        if (var1 == null && sort == null) {
            iterable = collection.find().skip((pageNumber - 1) * pageSize).limit(pageSize);
        } else if (var1 == null && sort != null) {
            iterable = collection.find().sort(sort).skip((pageNumber - 1) * pageSize).limit(pageSize);
        } else if (var1 != null && sort == null) {
            iterable = collection.find(var1).skip((pageNumber - 1) * pageSize).limit(pageSize);
        } else {
            iterable = collection.find(var1).sort(sort).skip((pageNumber - 1) * pageSize).limit(pageSize);
        }

        List<Document> list = new ArrayList<>();
        MongoCursor<Document> documentMongoCursor = iterable.iterator();
        while (documentMongoCursor.hasNext()) {
            list.add(documentMongoCursor.next());
        }
        return list;
    }

    public long getCount(String collName, Bson var1) {
        MongoCollection<Document> collection = getCollection(collName);
        if (var1 == null) {
            return collection.countDocuments();
        }
        return collection.countDocuments(var1);
    }

    public long getCount(String collName) {
        return getCount(collName, null);
    }

    public BulkWriteResult bulkWrite(String collName, List<? extends WriteModel<? extends Document>> modelList) {
        MongoCollection<Document> collection = getCollection(collName);
        return collection.bulkWrite(modelList);
    }

}
