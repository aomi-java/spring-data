package tech.aomi.spring.data.mongo.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import tech.aomi.spring.data.common.IdRepository;


/**
 * @author Sean createAt 17-12-26.
 */
public class MongodbIdRepository implements IdRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Long generateSequence(String sequenceName) {
        return generateNoSqlSequence(sequenceName);
    }

    @Override
    public void createSequence(String sequenceName) {
        save(sequenceName);
    }

    private Long generateNoSqlSequence(String sequenceName) {
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);
        DBSequence seq = mongoTemplate.findAndModify(
                new Query(Criteria.where("sequence").is(sequenceName)),
                new Update().inc("value", 1L),
                options,
                DBSequence.class
        );
        if (null == seq) {
            DBSequence init = save(sequenceName);
            return init.getValue();
        }
        return seq.getValue();
    }

    private DBSequence save(String sequenceName) {
        DBSequence exists = mongoTemplate.findOne(
                new Query(Criteria.where("sequence").is(sequenceName)),
                DBSequence.class
        );
        if (null == exists) {
            DBSequence sequence = new DBSequence();
            sequence.setSequence(sequenceName);
            sequence.setValue(1L);
            return mongoTemplate.insert(sequence);
        }
        return exists;
    }

}
