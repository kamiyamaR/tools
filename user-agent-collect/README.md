# user-agent-collect

User-AgentをPostgreSQLとMySQLへ登録する学習用のSpring Bootアプリケーションです。
ライブラリ更新前の準備として、DBアクセスをモック（代替処理）に置き換えたテストで分岐とWeb応答を検証しています。

## テスト環境・実行方法

- JDK 21（現在の検証環境：Amazon Corretto 21.0.12.12.1）
- Maven 3.9.6
- リポジトリ直下のMaven Wrapperで3.9.6を取得できます。ルートから`./mvnw.cmd -B -f user-agent-collect/pom.xml clean verify`を実行してください。実DBは専用DB起動後に-Pdb-integrationを追加します。以下のmvn例は既存Mavenを使う場合の手順です。
- Spring Boot 4.1.1、MyBatis Starter 4.0.0
- セキュリティ修正としてTomcat 11.0.26、Jackson BOM 3.1.7を明示指定。更新後も通常21件・実DB9件とHTTP6項目の検証が成功しています。Boot更新時は上書きの必要性を再評価します。
- MySQL Connector/Jは26.7.0を明示指定。公式告知の影響範囲9.7.0–9.7.1から外れる現行GA版へ更新し、実DB9件とHTTP6項目を含む検証が成功しています。CVE番号と修正コミットの個別対応は未確認で、安全性の保証ではありません。MySQL Server 8.4以降を対象にしています。
- 初回の依存取得にはネットワーク接続が必要です。

2026-10-03に、ソース内に使用箇所がないassertj-db 2.0.2とDbSetup 2.1.0を依存関係から削除しました。削除後も両モジュールの57テスト（実DB9件を含む）とHTTP19項目が成功し、テスト実行時のクラスパスにも両ライブラリが含まれないことを確認しています。DBテストは既存のJDBCとSpringの機能で実行します。

リポジトリのルートで、Mavenが使用するJDKを確認して実行します。

```sh
mvn -version
mvn -B -f user-agent-collect/pom.xml test
mvn -B -f user-agent-collect/pom.xml clean verify
```

モジュールのフォルダでは `mvn -B test` で実行できます。
現在のテストには外部DB、DDL、初期データ、追加の環境変数は不要です。
既存のテスト設定とリソースを利用し、CollectUserAgentDbAccessFunctionをMockitoBeanでモックに置き換えています。

## 検証範囲

2026-10-03に18件成功、失敗0、エラー0、スキップ0を確認しました。

| テスト | 件数 | 確認内容 |
|---|---:|---|
| CollectUserAgentControllerTest | 10 | 既存のClient Hints欠落5件、新規登録、両DB登録済み、番号不一致、PostgreSQLのみ登録、MySQLのみ登録 |
| CollectUserAgentMvcTest | 8 | 新規・登録済みのJSON応答、不整合3種類の400、PostgreSQL検索・MySQL検索・登録の例外による500 |

- Client Hints欠落時は308、Accept-CHとLocationを返し、DB処理を呼びません。
- 両DBに未登録なら200と `{"detail_code":"0000"}` を返し、登録を1回呼びます。
- 両DBに登録済みで番号が一致すれば200と `{"detail_code":"0001"}` を返し、再登録しません。
- 不整合ではE_API001_0001〜0003、400、対応する番号パラメータを確認します。Web応答の本文は空です。
- DB処理が例外を投げた場合は500と空本文を返し、それ以降の検索・登録を行いません。

MVCテストはMockMvcを使い、実際のController、Service、例外ハンドラー、JSON変換を通します。
HTTPサーバや実DBは起動しません。Mapper、DDL、JDBC、実際の登録・シーケンス・ロールバックは未検証です。
この結果は、本番DB設定によるアプリ起動や両DBをまたぐ更新の原子性を保証するものではありません。

既存test0006のPostgreSQL検索スタブの重複を、MySQL検索の指定へ修正しました。
テスト整備時点では、本番コード、アプリ設定、POM、ライブラリは変更していませんでした。

## ライブラリ移行の進捗

2026-10-03にJava 11を維持してBoot 2.7.18・MyBatis Starter 2.3.2へ更新し、再度18件成功とJAR生成を確認しました。
MySQLの依存指定を `com.mysql:mysql-connector-j` へ変更し、バージョンはBootの管理に任せています。
解決された版はMySQL 8.0.33、PostgreSQL 42.3.8、MyBatis 3.5.14、MyBatis-Spring 2.1.2です。
assertj-db 2.0.2とDbSetup 2.1.0は、この段階では据え置き。本番コード・設定・テストの追加変更は不要でした。

この段階はBoot 4への移行途中であり、保守中の版への移行完了や脆弱性解消を意味しません。
生成物は通常のJARで、実行可能JAR化・起動・実DB接続は未検証です。
その後、Corretto 21.0.1で `mvn -B -Djava.version=21 -f user-agent-collect/pom.xml clean verify` を実行し、Java 21向けのコンパイル、18件のテスト、通常JAR生成が成功しました。
POMのJava設定は11のままです。引数を省略した場合はJava 11向けにビルドされます。
上記はBoot 2.7段階での履歴です。その後POMをJava 21・Boot 3.5.16・MyBatis Starter 3.0.5へ変更し、18件と通常JAR生成が成功しました。
Servletの参照をJakartaへ変更し、テストの `@MockBean` を `@MockitoBean` へ移行しました。Java標準APIの `javax.sql.DataSource` は維持しています。
現在のBoot管理版ドライバはMySQL 9.7.0・PostgreSQL 42.7.11。実DB接続は未検証です。
MockitoによるJavaエージェントの動的読み込み警告が残っています。次はBoot 4系・MyBatis Starter 4系へ移行します。

その後Boot 4.0.8・MyBatis Starter 4.0.0への移行を実施し、既存18件と例外応答の追加2件、計20件が成功しました。
Web MVC・AspectJ・Web MVCテストのスターターへ変更し、Bootの移動したパッケージと削除されたAPIに対応しています。
ヘッダー処理はHttpHeadersへ移行。JSON応答はJackson 3で既存の厳密比較が成功しました。
現在のPostgreSQL JDBCは42.7.13、MySQLは9.7.0です。実DB・起動は未検証。Mockitoの警告は残っています。
次はBoot 4.1系へ更新して再検証します。上記の旧バージョンに関する説明は移行履歴です。

Boot 4.1.1への更新も実施済み。Java 21・MyBatis Starter 4.0.0を維持し、20件のテストと通常JAR生成が成功しました。追加のコード・テスト修正は不要でした。
次は実DB検証の準備として、Docker等の利用可否と不足しているDDL・初期データを確認します。

## 実DBテスト

db-test/の専用Docker環境を起動した状態で、リポジトリルートから実行します。

```sh
mvn -B -f user-agent-collect/pom.xml -Pdb-integration clean verify
```

Java 21・Boot 4.1.1で、通常20件と実DB統合テスト1件（CollectUserAgentDbIT）、計21件の成功を確認しました。
通常のtest/verifyでは実DBテストは実行しません。明示的なdb-integrationプロファイルでFailsafeが実行します。
統合テストはMainの独自DB設定と実Mapperを使い、MockMvc経由の新規登録・再取得、日本語文字列、両DBの番号一致、重複登録なしを確認します。
接続設定をdb-test/application-db-test.propertiesから読み、専用URL・ユーザーを確認してからテスト処理を行います。
各実行のUUIDを持つ行だけを後片付けし、検証後の両テーブルは0件でした。シーケンスは巻き戻しません。
独立したHTTPサーバ・JAR起動、不整合ケースの実DB検証、失敗時ロールバック、update Mapperの不一致は未検証・未修正です。

その後、不整合3ケース（番号不一致、PostgreSQLのみ登録済み、MySQLのみ登録済み）を実DBテストへ追加し、通常20件＋実DB4件、計24件成功を確認しました。
400・空本文・対応するエラーID、対象行の不変、要求による追加採番なしを確認しています。各テストの行は後片付け済み。
次は登録途中の失敗時に、各DBのロールバック範囲を検証します。

その後、PostgreSQL/MySQLのINSERT主キー衝突2ケースを追加し、通常20件＋実DB6件、計26件が成功しました。
500・空本文、対象行が両DBに残らないこと、事前の衝突用行が維持されること、採番が巻き戻されないことを確認しました。
MySQL INSERT失敗時には先行したPostgreSQL INSERTが戻りました。ただしMySQLへの成功済み書込みやコミット失敗時の原子的更新は未検証です。

続いて、実MySQL INSERT成功直後のテスト限定例外を追加し、通常20件＋実DB7件、計27件が成功しました。
500応答後にPostgreSQLの行は戻りますが、MySQLの行は残る問題を確認しました。MySQL接続の自動コミットはtrueでした。
このテスト成功は問題の再現を意味します。本番設計の修正には進まず、修正方針を検討する段階です。

その後、DB別のTransactionTemplateを明示し、コミット前のINSERT後例外で両DBが戻るよう改善しました。
通常20件＋実DB9件、計29件成功。MySQL接続の自動コミットfalseと、両DBの対象行0件を確認しています。
ただし独立コミットの限界は残ります。MySQL確定後にPostgreSQL確定前で失敗すると、MySQLだけに行が残るテストも維持しています。
この構成は両DBの原子的更新を保証しません。テスト後の両テーブル全行数は0件でした。

通常JARと実行時依存のみでtool.Mainを起動し、実HTTPの登録200/0000・再取得200/0001、専用両DBの1件・同番号を確認しました。
Client Hints不足は308ですが、User-Agent不足は既存の包括的例外ハンドラーで500になります。400への変更は今回行っていません。
検証用スクリプトはテーマフォルダのverification/Test-UserAgentCollectHttp.ps1。起動・外部設定を含む6項目を確認し、Javaプロセスと固有テスト行を後片付けしました。

その後、MissingRequestHeaderException専用処理を追加し、User-Agent不足を400・空本文へ修正しました。
DB障害の500とClient Hints不足の308は維持。DBアクセスなしを確認するMVCテストを追加し、通常21件＋実DB9件の計30件、実HTTP6項目が成功しました。
実HTTP検証のスクリプトは現在400を期待します。過去の500の結果は変更前の履歴です。
