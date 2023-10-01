package com.sokima.executor.configuration;

import com.sokima.executor.script.manager.ScriptStateManager;
import com.sokima.executor.script.output.MockOutputStream;
import com.sokima.executor.script.output.PersistenceOutputStream;
import com.sokima.executor.script.output.PersistenceOutputStreamImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;
import java.util.function.Function;

@Configuration
public class GeneralConfiguration {
    @Bean
    @Qualifier("scriptIdGenerator")
    public Random scriptIdGenerator() {
        return new Random();
    }

    @Bean
    @Qualifier("defaultOutputStream")
    public PersistenceOutputStream defaultOutputStream() {
        return new MockOutputStream();
    }

    @Bean
    @Qualifier("persistenceOutputStreamFactory")
    public Function<Long, PersistenceOutputStream> persistenceOutputStreamFactory(ScriptStateManager scriptStateManager) {
        return scriptId -> new PersistenceOutputStreamImpl(scriptId, scriptStateManager);
    }
}
