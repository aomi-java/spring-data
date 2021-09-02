package tech.aomi.spring.data.mongo;

import org.bson.types.Decimal128;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import tech.aomi.spring.data.common.IdRepository;
import tech.aomi.spring.data.mongo.repository.DocumentRepository;
import tech.aomi.spring.data.mongo.repository.DocumentRepositoryImpl;
import tech.aomi.spring.data.mongo.repository.MongodbIdRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 田尘殇Sean(sean.snow @ live.com) createAt 2018/7/14
 */
@Configuration
@ConditionalOnBean(MongoTemplate.class)
public class MongoAutoConfiguration {

    @Bean
    @Autowired
    public DocumentRepository documentRepository(MongoTemplate mongoTemplate) {
        return new DocumentRepositoryImpl(mongoTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public IdRepository mongodbIdRepository() {
        return new MongodbIdRepository();
    }

    @Configuration
    static class ConversionConfiguration {

        @Bean
        public MongoCustomConversions mongoCustomConversions() {
            List<Converter<?, ?>> converterList = new ArrayList<>();
            converterList.add(new BigDecimalReadingConverter());
            converterList.add(new BigDecimalWritingConverter());
            return new MongoCustomConversions(converterList);
        }

    }

    @ReadingConverter
    static class BigDecimalReadingConverter implements Converter<Decimal128, BigDecimal> {

        @Override
        public BigDecimal convert(Decimal128 source) {
            return source.bigDecimalValue();
        }

    }

    @WritingConverter
    static class BigDecimalWritingConverter implements Converter<BigDecimal, Decimal128> {

        @Override
        public Decimal128 convert(BigDecimal source) {
            return new Decimal128(source);
        }
    }


}
