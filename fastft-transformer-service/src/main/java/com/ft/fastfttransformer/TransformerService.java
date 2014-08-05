package com.ft.fastfttransformer;

import com.ft.api.util.buildinfo.BuildInfoResource;
import com.ft.fastfttransformer.configuration.TransformerConfiguration;
import com.ft.fastfttransformer.health.TransformerHealthCheck;
import com.ft.fastfttransformer.resources.TransformerResource;
import com.ft.platform.dropwizard.AdvancedHealthCheckBundle;

import io.dropwizard.Application;
import io.dropwizard.servlets.SlowRequestFilter;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;

import javax.servlet.DispatcherType;

import java.util.EnumSet;

public class TransformerService extends Application<TransformerConfiguration> {

    public static void main(final String[] args) throws Exception {
        new TransformerService().run(args);
    }

    @Override
    public void initialize(Bootstrap<TransformerConfiguration> bootstrap) {
        bootstrap.addBundle(new AdvancedHealthCheckBundle());
    }

    @Override
    public void run(final TransformerConfiguration configuration, final Environment environment) throws Exception {
        environment.jersey().register(new BuildInfoResource());
        environment.jersey().register(new TransformerResource(configuration.getClamoBaseURL()));

        environment.servlets().addFilter(
                "Slow Servlet Filter",
                new SlowRequestFilter(Duration.milliseconds(configuration.getSlowRequestTimeout()))).addMappingForUrlPatterns(
                EnumSet.of(DispatcherType.REQUEST),
                false,
                configuration.getSlowRequestPattern());

        environment.healthChecks().register("My Health", new TransformerHealthCheck("replace me"));

    }

}
