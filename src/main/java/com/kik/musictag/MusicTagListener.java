package com.kik.musictag;

import java.util.HashMap;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.eclipse.jetty.servlet.DefaultServlet;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class MusicTagListener extends GuiceServletContextListener
{


    @Override
    protected Injector getInjector()
    {
        return Guice.createInjector(new JerseyServletModule()
        {
            @Override
            protected void configureServlets()
            {
                super.configureServlets();
                bind(DefaultServlet.class).in(Singleton.class);
                bind(MessageResource.class).in(Singleton.class);

                bind(MessageBodyReader.class).to(JacksonJsonProvider.class);
                bind(MessageBodyWriter.class).to(JacksonJsonProvider.class);

                HashMap<String, String> options = new HashMap<String, String>();
                options.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
                options.put("com.sun.jersey.spi.container.ContainerRequestFilters", "com.sun.jersey.api.container.filter.PostReplaceFilter");
                serve("/*").with(GuiceContainer.class, options);
            }

        });
    }
}
