package com.github.mygreen.splate;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.github.mygreen.splate.type.SqlTemplateValueTypeRegistry;

import lombok.Getter;
import lombok.NonNull;

/**
 * SQLテンプレートのパラメータをJavaBean として渡すときのSQLテンプレートのコンテキスト。
 * SQLテンプレート中では、JavaBeanのプロパティ名で参照できます。
 *
 * @version 0.2
 * @author T.TSUCHIE
 *
 */
public class BeanPropertySqlTemplateContext extends SqlTemplateContext {

    /**
     * JavaBeanのインスタンス。
     */
    @Getter
    private final Object value;

    /**
     * JavaBeanを指定するコンストラクタ。
     * @param object SQLテンプレート中のパラメータとして渡すJavaBeanのインスタンス
     */
    public BeanPropertySqlTemplateContext(final @NonNull Object object) {
        super();
        this.value = object;
    }

    /**
     *  {@link SqlTemplateValueTypeRegistry}とJavaBeanを指定してインスタンスを作成します。
     *
     * @param valueTypeRegistry SQLテンプレートのパラメータの変換処理を管理する処理。
     * @param object JavaBeanのインスタンス
     */
    public BeanPropertySqlTemplateContext(SqlTemplateValueTypeRegistry valueTypeRegistry, final @NonNull Object object) {
        super(valueTypeRegistry);
        this.value = object;
    }

    @Override
    public EvaluationContext createEvaluationContext() {
        final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.setRootObject(value);
        return evaluationContext;
    }
}
