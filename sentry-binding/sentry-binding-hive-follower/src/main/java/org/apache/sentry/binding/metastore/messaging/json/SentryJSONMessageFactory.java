/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.sentry.binding.metastore.messaging.json;

import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.metastore.api.*;
// import org.apache.hadoop.hive.metastore.api.Index;

import java.util.*;

import org.apache.hadoop.hive.metastore.messaging.*;
// import org.apache.hadoop.hive.metastore.messaging.AlterIndexMessage;
// import org.apache.hadoop.hive.metastore.messaging.CreateIndexMessage;
// import org.apache.hadoop.hive.metastore.messaging.DropIndexMessage;
// import org.apache.hadoop.hive.metastore.messaging.json.JSONAlterIndexMessage;
import org.apache.hadoop.hive.metastore.messaging.json.JSONCreateFunctionMessage;
// import org.apache.hadoop.hive.metastore.messaging.json.JSONCreateIndexMessage;
import org.apache.hadoop.hive.metastore.messaging.json.JSONDropFunctionMessage;
// import org.apache.hadoop.hive.metastore.messaging.json.JSONDropIndexMessage;
import org.apache.hadoop.hive.metastore.messaging.json.JSONInsertMessage;

public class SentryJSONMessageFactory extends MessageFactory {
  private static final Log LOG = LogFactory.getLog(SentryJSONMessageFactory.class.getName());
  private static SentryJSONMessageDeserializer deserializer = new SentryJSONMessageDeserializer();

  // This class has basic information from a Partition. It is used to get a list of Partition
  // information from an iterator instead of having a list of Partition objects. Partition objects
  // may hold more information that can cause memory issues if we get a large list.
  private class PartitionBasicInfo {
    private List<Map<String, String>> partitionList = Lists.newLinkedList();
    private List<String> locations = Lists.newArrayList();

    public List<Map<String, String>> getPartitionList() {
      return partitionList;
    }

    public List<String> getLocations() {
      return locations;
    }
  }

  public SentryJSONMessageFactory() {
    LOG.info("Using SentryJSONMessageFactory for building Notification log messages ");
  }

  public MessageDeserializer getDeserializer() {
    return deserializer;
  }

  public String getVersion() {
    return "0.1";
  }

  public String getMessageFormat() {
    return "json";
  }

  @Override
  public SentryJSONCreateDatabaseMessage buildCreateDatabaseMessage(Database db) {
    return new SentryJSONCreateDatabaseMessage(MS_SERVER_URL, MS_SERVICE_PRINCIPAL, db,
        now(), db.getLocationUri());
  }

  @Override
  public AlterDatabaseMessage buildAlterDatabaseMessage(Database database, Database database1) {
    return null;
  }

  @Override
  public SentryJSONDropDatabaseMessage buildDropDatabaseMessage(Database db) {
    return new SentryJSONDropDatabaseMessage(MS_SERVER_URL, MS_SERVICE_PRINCIPAL, db.getName(),
        now(), db.getLocationUri());
  }

  @Override
  public SentryJSONCreateTableMessage buildCreateTableMessage(Table table, Iterator<String> fileIter) {
    // fileIter is used to iterate through a full list of files that partition have. This
    // may be too verbose and it is overloading the Sentry store. Sentry does not use these files
    // so it is safe to ignore them.
    return new SentryJSONCreateTableMessage(MS_SERVER_URL, MS_SERVICE_PRINCIPAL, table, Collections.emptyIterator(), now());
  }

  @Override
  public AlterTableMessage buildAlterTableMessage(Table before, Table after, boolean isTruncateOp) {
    return new SentryJSONAlterTableMessage(MS_SERVER_URL, MS_SERVICE_PRINCIPAL, before, after, isTruncateOp, now());
  }

  /*
  @Override
  public SentryJSONAlterTableMessage buildAlterTableMessage(Table before, Table after) {
    return new SentryJSONAlterTableMessage(MS_SERVER_URL, MS_SERVICE_PRINCIPAL, before, after, now());
  }
  */

  @Override
  public DropTableMessage buildDropTableMessage(Table table) {
    return new SentryJSONDropTableMessage(MS_SERVER_URL, MS_SERVICE_PRINCIPAL, table.getDbName(),
        table.getTableName(), now(), table.getSd().getLocation());
  }

  @Override
  public AlterPartitionMessage buildAlterPartitionMessage(Table table, Partition before, Partition after, boolean isTruncateOp) {
    return new SentryJSONAlterPartitionMessage(MS_SERVER_URL, MS_SERVICE_PRINCIPAL,
        table, before, after, isTruncateOp, now());
  }

  /*
  @Override
  public SentryJSONAlterPartitionMessage buildAlterPartitionMessage(Table table,
      Partition before, Partition after) {
    return new SentryJSONAlterPartitionMessage(MS_SERVER_URL, MS_SERVICE_PRINCIPAL,
        table, before, after, now());
  }
  */

  @Override
  public DropPartitionMessage buildDropPartitionMessage(Table table, Iterator<Partition> partitions) {
    PartitionBasicInfo partitionBasicInfo = getPartitionBasicInfo(table, partitions);

    return new SentryJSONDropPartitionMessage(MS_SERVER_URL, MS_SERVICE_PRINCIPAL,
        table, partitionBasicInfo.getPartitionList(), now(), partitionBasicInfo.getLocations());
  }

  @Override
  public CreateFunctionMessage buildCreateFunctionMessage(Function function) {
    // Sentry would be not be interested in CreateFunctionMessage as these are generated when is data is
    // added inserted. This method is implemented for completeness. This is reason why, new sentry
    // JSON class is not defined for CreateFunctionMessage
    return new JSONCreateFunctionMessage(MS_SERVER_URL, MS_SERVICE_PRINCIPAL, function, now());
  }

  @Override
  public DropFunctionMessage buildDropFunctionMessage(Function function) {
    // Sentry would be not be interested in DropFunctionMessage as these are generated when is data is
    // added inserted. This method is implemented for completeness. This is reason why, new sentry
    // JSON class is not defined for DropFunctionMessage
    return new JSONDropFunctionMessage(MS_SERVER_URL, MS_SERVICE_PRINCIPAL, function, now());

  }



  @Override
  public OpenTxnMessage buildOpenTxnMessage(Long aLong, Long aLong1) {
    return null;
  }

  @Override
  public CommitTxnMessage buildCommitTxnMessage(Long aLong) {
    return null;
  }

  @Override
  public AbortTxnMessage buildAbortTxnMessage(Long aLong) {
    return null;
  }

  @Override
  public AllocWriteIdMessage buildAllocWriteIdMessage(List<TxnToWriteId> list, String s, String s1) {
    return null;
  }

  @Override
  public AddPrimaryKeyMessage buildAddPrimaryKeyMessage(List<SQLPrimaryKey> list) {
    return null;
  }

  @Override
  public AddForeignKeyMessage buildAddForeignKeyMessage(List<SQLForeignKey> list) {
    return null;
  }

  @Override
  public AddUniqueConstraintMessage buildAddUniqueConstraintMessage(List<SQLUniqueConstraint> list) {
    return null;
  }

  @Override
  public AddNotNullConstraintMessage buildAddNotNullConstraintMessage(List<SQLNotNullConstraint> list) {
    return null;
  }

  @Override
  public DropConstraintMessage buildDropConstraintMessage(String s, String s1, String s2) {
    return null;
  }

  @Override
  public CreateCatalogMessage buildCreateCatalogMessage(Catalog catalog) {
    return null;
  }

  @Override
  public DropCatalogMessage buildDropCatalogMessage(Catalog catalog) {
    return null;
  }

  @Override
  public AlterCatalogMessage buildAlterCatalogMessage(Catalog catalog, Catalog catalog1) {
    return null;
  }

  /*
  @Override
  public CreateIndexMessage buildCreateIndexMessage(Index index) {
    // Sentry would be not be interested in CreateIndexMessage as these are generated when is data is
    // added inserted. This method is implemented for completeness. This is reason why, new sentry
    // JSON class is not defined for CreateIndexMessage
    return new JSONCreateIndexMessage(MS_SERVER_URL, MS_SERVICE_PRINCIPAL, index, now());
  }
  */

  /*
  @Override
  public DropIndexMessage buildDropIndexMessage(Index index) {
    // Sentry would be not be interested in DropIndexMessage as these are generated when is data is
    // added inserted. This method is implemented for completeness. This is reason why, new sentry
    // JSON class is not defined for DropIndexMessage
    return new JSONDropIndexMessage(MS_SERVER_URL, MS_SERVICE_PRINCIPAL, index, now());
  }
  */

  /*
  @Override
  public AlterIndexMessage buildAlterIndexMessage(Index before, Index after) {
    // Sentry would be not be interested in AlterIndexMessage as these are generated when is data is
    // added inserted. This method is implemented for completeness. This is reason why, new sentry
    // JSON class is not defined for AlterIndexMessage
    return new JSONAlterIndexMessage(MS_SERVER_URL, MS_SERVICE_PRINCIPAL, before, after, now());
  }
  */

  @Override
  public InsertMessage buildInsertMessage(Table tableObj, Partition ptnObj, boolean replace, Iterator<String> files) {
    return new JSONInsertMessage(MS_SERVER_URL, MS_SERVICE_PRINCIPAL, tableObj, ptnObj, replace, files, now());
  }


  /*
  @Override
  public InsertMessage buildInsertMessage(String db, String table, Map<String, String> partKeyVals,
      Iterator<String> fileIter) {
    // Sentry would be not be interested in InsertMessage as these are generated when is data is
    // added inserted. This method is implemented for completeness. This is reason why, new sentry
    // JSON class is not defined for InsertMessage.
    return new JSONInsertMessage(MS_SERVER_URL, MS_SERVICE_PRINCIPAL, db, table, partKeyVals,
        fileIter, now());
  }
  */

  @Override
  public AddPartitionMessage buildAddPartitionMessage(Table table,
      Iterator<Partition> partitionsIterator, Iterator<PartitionFiles> partitionFileIter) {
    PartitionBasicInfo partitionBasicInfo = getPartitionBasicInfo(table, partitionsIterator);

    // partitionFileIter is used to iterate through a full list of files that partition have. This
    // may be too verbose and it is overloading the Sentry store. Sentry does not use these files
    // so it is safe to ignore them.
    return new SentryJSONAddPartitionMessage(MS_SERVER_URL, MS_SERVICE_PRINCIPAL, table,
        partitionsIterator, Collections.emptyIterator(), now(), partitionBasicInfo.getLocations());
  }

  private PartitionBasicInfo getPartitionBasicInfo(Table table, Iterator<Partition> iterator) {
    PartitionBasicInfo partitionBasicInfo = new PartitionBasicInfo();
    while(iterator.hasNext()) {
      Partition partition = iterator.next();

      partitionBasicInfo.getPartitionList().add(getPartitionKeyValues(table, partition));
      partitionBasicInfo.getLocations().add(partition.getSd().getLocation());
    }

    return partitionBasicInfo;
  }

  private static Map<String, String> getPartitionKeyValues(Table table, Partition partition) {
    LinkedHashMap partitionKeys = new LinkedHashMap();

    for (int i = 0; i < table.getPartitionKeysSize(); ++i) {
      partitionKeys.put((table.getPartitionKeys().get(i)).getName(), partition.getValues().get(i));
    }

    return partitionKeys;
  }

  //This is private in parent class
  private long now() {
    return System.currentTimeMillis() / 1000L;
  }
}
