package com.github.mygreen.sqltemplate.type;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.mygreen.sqltemplate.MapSqlContext;
import com.github.mygreen.sqltemplate.ProcessResult;
import com.github.mygreen.sqltemplate.SqlContext;
import com.github.mygreen.sqltemplate.SqlTemplate;
import com.github.mygreen.sqltemplate.SqlTemplateEngine;


/**
 * 各ノードの{@link SqlTemplateValueType} による変換処理の実装のテスタ。
 *
 *
 * @author T.TSUCHIE
 *
 */
public class SqlTemplateValueTypeTest {

    private SqlTemplateEngine templateEngine;

    enum JobType {
        /**店員*/
        CLERK,
        /**調理師*/
        COOKS,
        /**オーナー*/
        OWNER
    }

    private static class JobValueType implements SqlTemplateValueType<JobType>{

        @Override
        public Object getBindVariableValue(JobType value) throws SqlTypeConversionException {
            return value.ordinal();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        this.templateEngine = new SqlTemplateEngine();
    }

    @Test
    void testBindVariableNode() {
        String sql = "SELECT * FROM emp WHERE job = /*job*/'CLERK'";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlContext context = new MapSqlContext(Map.of("job", JobType.COOKS));
        context.registerValueType(JobType.class, new JobValueType());

        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE job = ?");
        assertThat(result.getParameters()).containsExactly(1);
    }

    @Test
    void testParenBindVariableNode() {
        String sql = "SELECT * FROM emp WHERE job in /*job*/('CLERK', 'COOKS')";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlContext context = new MapSqlContext(Map.of("job", List.of(JobType.COOKS, JobType.OWNER)));
        context.registerValueType(JobType.class, new JobValueType());

        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE job in (?, ?)");
        assertThat(result.getParameters()).containsExactly(1, 2);
    }

    @Test
    void testEmbeddedValueNode() {
        String sql = "SELECT * FROM emp WHERE job = /*$job*/'CLERK'";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlContext context = new MapSqlContext(Map.of("job", JobType.COOKS));
        context.registerValueType(JobType.class, new JobValueType());

        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE job = COOKS");
        assertThat(result.getParameters()).isEmpty();
    }
}