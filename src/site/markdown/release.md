# リリースノート

## ver 0.2.1 - 2021-01-30

- [#13](https://github.com/mygreen/splate/pull/13) フィールド名や引数名の間違い

  - ``valueTypeRestRegistry`` -> ``valueTypeRegistry``


## ver 0.2 - 2020-08-02

- [#4](https://github.com/mygreen/splate/pull/4) ライブラリの依存関係のバージョンの見直し

    - SpringFramework依存関係を v5.1 -> v5.0 に変更。

- [#5](https://github.com/mygreen/splate/pull/5) 役割をより明確にするために一部のクラス名を変更。

    - SqlContext -> SqlTemplateContext

    - BeanPropertySqlContext -> BeanPropertySqlTemplateContext

    - MapSqlContext -> MapSqlTemplateContext

    - EmptyValueSqlContext -> EmptyValueSqlTemplateContext

    - ProcessContext -> NodeProcessContext

- [#7](https://github.com/mygreen/splate/pull/7) エラー発生時のテンプレートの位置などを詳細に出す用変更

- [#8](https://github.com/mygreen/splate/pull/8) 使用していないクラス、メソッドを削除

- [#9](https://github.com/mygreen/splate/pull/9) / [#11](https://github.com/mygreen/splate/pull/11) 静的解析の指摘に対する修正


