package com.knova.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * prod 环境由 Milvus 自身负责落盘，因此无需额外处理。
 */
@Service
@Profile("prod")
public class MilvusVectorStorePersistence implements VectorStorePersistence {
    @Override
    public void persist() {
        // 1. Milvus 写入和删除已直接在服务端持久化。
        // 2. 保留空实现以满足统一持久化接口，调用方无需判断运行环境。
    }
}
