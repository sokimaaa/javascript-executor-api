package com.sokima.executor.service.v1;

import com.sokima.executor.model.script.ScriptState;
import com.sokima.executor.service.ScriptRetryService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("scriptRetryServiceV1")
public class ScriptRetryServiceV1 implements ScriptRetryService {
    @Override
    public ScriptState retryScript(Long scriptId) {
        throw new UnsupportedOperationException("TODO");
    }
}
