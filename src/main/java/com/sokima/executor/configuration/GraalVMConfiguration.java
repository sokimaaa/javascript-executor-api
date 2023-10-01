package com.sokima.executor.configuration;

import com.sokima.executor.holder.configuration.PolyglotContextConfigurationHolder;
import com.sokima.executor.script.output.PersistenceOutputStream;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class GraalVMConfiguration {

    @Autowired
    @Qualifier("defaultOutputStream")
    private PersistenceOutputStream defaultPersistenceOutputStream;

    @Autowired
    @Qualifier("persistenceOutputStreamFactory")
    private Function<Long, PersistenceOutputStream> persistenceOutputStreamFactory;

    /**
     * A GraalVM engine shared between multiple JavaScript contexts.
     */
    @Bean
    @Qualifier("javascriptEngine")
    public Engine engine() {
        return Engine.newBuilder()
                .option("engine.WarnInterpreterOnly", "false")
                .build();
    }

    @Bean
    @Qualifier("contextFactory")
    public Function<PolyglotContextConfigurationHolder, Context> contextFactory() {
        return this::context;
    }

    /*
     * For simplicity, allow ALL accesses. In a real application, access to resources should be restricted.
     */
    private Context context(PolyglotContextConfigurationHolder conf) {
        return Context.newBuilder(conf.languageId())
                .engine(engine())
                .out(conf.isPersistableConsole() ? persistenceOutputStreamFactory.apply(conf.scriptId()) : defaultPersistenceOutputStream)
                .allowHostAccess(HostAccess.ALL)
                // Allow access to other languages
                .allowPolyglotAccess(PolyglotAccess.NONE)
                // Allows the guest application to look up the host application classes permitted by the lookup predicate
                .allowHostClassLookup(x -> false)
                .allowCreateThread(false)
                .allowIO(false)
                .build();
    }
}
