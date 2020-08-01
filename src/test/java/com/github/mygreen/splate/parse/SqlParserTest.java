package com.github.mygreen.splate.parse;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.mygreen.splate.EmptyValueSqlTemplateContext;
import com.github.mygreen.splate.MapSqlTemplateContext;
import com.github.mygreen.splate.ProcessResult;
import com.github.mygreen.splate.SqlTemplateContext;
import com.github.mygreen.splate.SqlTemplate;
import com.github.mygreen.splate.SqlTemplateEngine;
import com.github.mygreen.splate.TwoWaySqlException;

/**
 * {@link SqlParserTest}のテスタ。
 *
 *
 * @author T.TSUCHIE
 *
 */
public class SqlParserTest {

    private SqlTemplateEngine templateEngine;

    @BeforeEach
    public void setUp() throws Exception {
        this.templateEngine = new SqlTemplateEngine();
    }

    @Test
    public void testParse_noParams() {

        String sql = "SELECT * FROM emp";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new EmptyValueSqlTemplateContext();
        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp");
        assertThat(result.getParameters()).isEmpty();

    }

    @Test
    public void testParse_commentEndNotFound() {

        String sql = "SELECT * FROM emp/*hoge";

        assertThatThrownBy(() -> templateEngine.getTemplateByText(sql))
            .isInstanceOf(TwoWaySqlException.class)
            .hasMessageContaining("hoge is not closed with */");

    }

    @Test
    public void testBindVariable() {

        String sql = "SELECT * FROM emp WHERE job = /*job*/'CLERK' AND deptno = /*deptno*/20";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("job", "Normal", "deptno", 10));
        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE job = ? AND deptno = ?");
        assertThat(result.getParameters()).containsExactly("Normal", 10);

    }

    @Test
    public void testParenBindVariable_collection() {
        String sql = "SELECT * FROM emp WHERE id in /*id*/(10, 20)";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("id", List.of(1, 2)));
        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE id in (?, ?)");
        assertThat(result.getParameters()).containsExactly(1, 2);

    }

    @Test
    public void testParenBindVariable_array() {
        String sql = "SELECT * FROM emp WHERE id in /*id*/(10, 20)";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("id", new int[] {1, 2}));
        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE id in (?, ?)");
        assertThat(result.getParameters()).containsExactly(1, 2);

    }

    @Test
    public void testParenBindVariable_object() {
        String sql = "SELECT * FROM emp WHERE id in /*id*/(10, 20)";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("id", 1));
        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE id in ?");
        assertThat(result.getParameters()).containsExactly(1);

    }

    @Test
    public void testEmbeddedValue() {

        String sql = "SELECT * FROM emp limit /*$limit*/10 offset /*$offset*/5";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("limit", 100, "offset", 10));
        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp limit 100 offset 10");
        assertThat(result.getParameters()).isEmpty();

    }

    @Test
    public void testIf_null() {

        String sql = "SELECT * FROM emp/*IF job != null*/ WHERE job = /*job*/'CLERK'/*END*/";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("job", "Normal"));
        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE job = ?");
        assertThat(result.getParameters()).containsExactly("Normal");

    }

    @Test
    public void testIf_compare() {

        String sql = "SELECT * FROM emp/*IF age >= 1*/ WHERE age = /*age*/20/*END*/";

        SqlTemplate template = templateEngine.getTemplateByText(sql);

        {
            SqlTemplateContext context = new MapSqlTemplateContext(Map.of("age", 1));
            ProcessResult result = template.process(context);

            assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE age = ?");
            assertThat(result.getParameters()).containsExactly(1);

        }

        {
            SqlTemplateContext context = new MapSqlTemplateContext(Map.of("age", -1));
            ProcessResult result = template.process(context);

            assertThat(result.getSql()).isEqualTo("SELECT * FROM emp");
            assertThat(result.getParameters()).isEmpty();

        }

    }

    @Test
    public void testElse() {

        String sql = "SELECT * FROM emp WHERE /*IF job != null*/job = /*job*/'CLERK'-- ELSE job is null/*END*/";

        SqlTemplate template = templateEngine.getTemplateByText(sql);

        {
            // ifの評価
            SqlTemplateContext context = new MapSqlTemplateContext(Map.of("job", "Normal"));
            ProcessResult result = template.process(context);

            assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE job = ?");
            assertThat(result.getParameters()).containsExactly("Normal");

        }

        {
            // elseの評価
            MapSqlTemplateContext context = new MapSqlTemplateContext();
            context.setVariable("job", null);
            ProcessResult result = template.process(context);

            assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE job is null");
            assertThat(result.getParameters()).isEmpty();

        }

    }

    @Test
    public void testBegin() {

        String sql = "SELECT * FROM emp/*BEGIN*/ WHERE /*IF job != null*/job = /*job*/'CLERK'/*END*//*IF deptno != null*/ AND deptno = /*deptno*/20/*END*//*END*/";

        SqlTemplate template = templateEngine.getTemplateByText(sql);

        {
            // 全てのプロパティがnull
            MapSqlTemplateContext context = new MapSqlTemplateContext();
            context.setVariable("job", null);
            context.setVariable("deptno", null);

            ProcessResult result = template.process(context);

            assertThat(result.getSql()).isEqualTo("SELECT * FROM emp");
            assertThat(result.getParameters()).isEmpty();

        }

        {
            // １つのプロパティがnull
            MapSqlTemplateContext context = new MapSqlTemplateContext();
            context.setVariable("job", "Normal");
            context.setVariable("deptno", null);
            ProcessResult result = template.process(context);

            assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE job = ?");
            assertThat(result.getParameters()).containsExactly("Normal");

        }


    }

}