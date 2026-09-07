package com.example.ecommerce.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Global Thread-Safe Spring ApplicationContext Lookup Utility.
 * Allows Jakarta Servlets, Filters, and Listeners to obtain managed Spring bean
 * singletons and dynamic AOP transaction proxies (@Transactional) rather than
 * instantiating services via direct 'new', preventing split-brain architecture.
 */
@Component
public class SpringContextLookup implements ApplicationContextAware {

    private static volatile ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    /**
     * Obtains a managed Spring bean of the specified type.
     *
     * @param beanClass the bean type
     * @param <T>       the generic type
     * @return the managed Spring bean instance
     */
    public static <T> T getBean(Class<T> beanClass) {
        if (context == null) {
            throw new IllegalStateException("Spring ApplicationContext has not been initialized yet.");
        }
        return context.getBean(beanClass);
    }

    /**
     * Checks if the Spring context has been initialized.
     *
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return context != null;
    }

    public static ApplicationContext getContext() {
        return context;
    }
}
