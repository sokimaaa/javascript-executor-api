package com.sokima.executor.persistence;

import com.sokima.executor.model.script.ScriptState;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class ScriptExecutorMongoTemplate extends AbstractExecutorMongoTemplate<ScriptState> {

    private static final String COLLECTION_NAME = "scripts";

    private static final String ID = "scriptId";

    public ScriptExecutorMongoTemplate(MongoTemplate mongoTemplate) {
        super(mongoTemplate);
    }

    @Override
    protected String getCollectionName() {
        return COLLECTION_NAME;
    }

    @Override
    protected String getIdName() {
        return ID;
    }

    @Override
    protected Class<ScriptState> getGenericClass() {
        return ScriptState.class;
    }
}
