package com.xz.scorep.executor.exportexcel;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * (description)
 * created at 16/05/30
 *
 * @author yiding_he
 */
@Component
public class SheetManager implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public SheetGenerator getSheetGenerator(Class<? extends SheetGenerator> type) {
        return applicationContext.getBean(type);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
