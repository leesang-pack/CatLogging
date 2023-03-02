package com.catlogging.app;

import org.hibernate.dialect.MySQL57Dialect;
import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

public class CustomMySQLDialect extends MySQL57Dialect {

    public CustomMySQLDialect() {
        super();
        registerFunction("diff_minutes", new SQLFunctionTemplate(StandardBasicTypes.LONG, "TIMESTAMPDIFF(MINUTE,?1,?2)"));
    }

}