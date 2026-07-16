package com.knova.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/** prod 环境由 Milvus 自身负责落盘，因此无需额外处理。 */
@Service
@Profile("prod")
public class MilvusVectorStorePersistence implements VectorStorePersistence {
    @Override public void persist() { }
}
