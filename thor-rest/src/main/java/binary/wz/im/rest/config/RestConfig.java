package binary.wz.im.rest.config;

import binary.wz.im.common.domain.po.DbModel;
import binary.wz.im.rest.handler.ValidateHandler;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import javax.validation.Validator;

/**
 * @author binarywz
 * @date 2022/6/1 23:38
 * @description:
 */
@Configuration
@MapperScan(value = "binary.wz.im.rest.mapper")
public class RestConfig {

    @Bean
    @Primary
    public MybatisPlusProperties mybatisPlusProperties() {
        MybatisPlusProperties properties = new MybatisPlusProperties();
        GlobalConfig globalConfig = new GlobalConfig();

        properties.setTypeAliasesSuperType(DbModel.class);
        properties.setMapperLocations(new String[]{"classpath*:/mapper/**/*.xml"});
        properties.setGlobalConfig(globalConfig);

        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        dbConfig.setTablePrefix("im_");
        globalConfig.setDbConfig(dbConfig);

        return properties;
    }

    @Bean
    public Integer init(Validator validator, RedisTemplate<String, String> redisTemplate) {
        ValidateHandler.setValidator(validator);
        return 1;
    }

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplateString(ReactiveRedisConnectionFactory connectionFactory) {
        return new ReactiveRedisTemplate<>(connectionFactory, RedisSerializationContext.string());
    }
}
