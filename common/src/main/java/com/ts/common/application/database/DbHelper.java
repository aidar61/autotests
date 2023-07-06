package com.ts.common.application.database;

import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.application.database.dbTables.GrUserTable;
import com.ts.common.config.AppConfigProvider;
import com.ts.common.entitites.BaseEntity;
import com.ts.common.enums.TaskStatuses;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.Locale;

@Getter
@Slf4j
public class DbHelper {
    private JdbcTemplate template;
    private GrTaskTable grTaskTable;
    private GrUserTable grUserTable;

    public DbHelper() {
        Locale.setDefault(Locale.ENGLISH);
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(AppConfigProvider.getDbConfig().driver());
        dataSource.setUrl(AppConfigProvider.getDbConfig().url());
        dataSource.setUsername(AppConfigProvider.getDbConfig().username());
        dataSource.setPassword(AppConfigProvider.getDbConfig().password());
        this.template = new JdbcTemplate(dataSource);
        this.grTaskTable = new GrTaskTable(template);
        this.grUserTable = new GrUserTable(template);
    }

}
