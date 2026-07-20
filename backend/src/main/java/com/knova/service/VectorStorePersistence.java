package com.knova.service;

/**
 * 向量库持久化钩子；正式 Milvus 会自行持久化，本地开发需要写回文件。
 */
public interface VectorStorePersistence {
    /** 在向量新增或删除后触发存储层持久化；不同环境提供不同实现。 */
    void persist();
}
