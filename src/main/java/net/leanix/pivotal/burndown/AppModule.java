package net.leanix.pivotal.burndown;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author alexanderalt
 */
public class AppModule extends AbstractModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    @Override
    protected void configure() {
    }

    @Provides
    public Logger logger() {
        return LOGGER;
    }
}
