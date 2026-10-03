# 実DB検証専用環境

Java 21・Boot 4.1のuser-agent-collectを、既存DBから分離して検証する準備です。
Docker DesktopのLinuxエンジンとDocker Composeが必要です。

## 構成

| DB | 固定イメージ | ホスト接続先 |
|---|---|---|
| MySQL | 8.4.11、linux/amd64、digest固定 | 127.0.0.1:13306 |
| PostgreSQL | 18.6-bookworm、linux/amd64、digest固定 | 127.0.0.1:15432 |

専用プロジェクト名uac-db-test、専用DB名uac_integration、専用の名前付きボリュームを使います。
公開ポートはループバックのみ。既存の3306/5432や既存DBのデータディレクトリを使いません。
ユーザーuac_testとパスワードはComposeと接続設定に記載した検証専用の固定値です。実際の認証情報を入れないでください。

初期化SQLは両DBのuser_agent_inf（整数sequence_num主キー、TEXT user_agent NOT NULL）を作成し、PostgreSQLに整数シーケンスsequence_num_createを1から作成します。
初期行は0件。元DBの定義を復元したものではなく、ソースから作った検証用の前提です。User-Agentの一意制約・最大長は未確定。
MySQLはutf8mb4/utf8mb4_bin、PostgreSQLはUTF8/Cロケール。

## 実行方法

リポジトリルートからこのフォルダへ移動します。Docker導入直後はプロセスのPATHを補います。

```powershell
Set-Location user-agent-collect/db-test
$dockerBinDirectory = Join-Path $env:LOCALAPPDATA 'Programs/DockerDesktop/resources/bin'
$env:PATH = $dockerBinDirectory + ';' + $env:PATH
docker version
docker compose config --quiet
docker compose up -d --wait --wait-timeout 180
docker compose ps
```

初回に公式イメージを取得します。healthcheckは専用ユーザーでTCP接続し、作成したテーブルをSELECTできることを確認します。
起動に失敗した場合はcompose logsで確認し、既存サービスやWSL設定を自動変更しません。

```powershell
# 空テーブルとPostgreSQLのシーケンス定義を確認。既存DBには接続しない。
docker compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysql -h 127.0.0.1 -u "$MYSQL_USER" -D "$MYSQL_DATABASE" -e "SELECT VERSION(); SHOW CREATE TABLE user_agent_inf; SELECT COUNT(*) FROM user_agent_inf;"'
docker compose exec -T postgres psql -U uac_test -d uac_integration -v ON_ERROR_STOP=1 -c 'SELECT version(); SELECT COUNT(*) FROM user_agent_inf; SELECT sequencename, data_type, start_value, increment_by FROM pg_sequences WHERE sequencename = ''sequence_num_create'';'
docker compose stop
```

stopは専用コンテナを停止し、データを保持します。再開はup。イメージ・コンテナ・ボリュームの削除や全体pruneはこの手順に含めません。
初期化SQLは空のデータ領域を初回起動した時だけ実行されます。既存ボリュームでSQLを変更しても再実行されないため、初期化し直す際は専用データを消してよいか確認してから操作します。

## Java側への適用

application-db-test.propertiesに両接続先を明示しています。既存main/testのdb-access.propertiesは変更していません。
今後の実DBテスト構成からこのファイルを明示的に読み込み、標準ポートへのフォールバックを防ぎます。
通常の20件はDBモックのまま。この構成を起動しただけではJavaテストは実DBに切り替わりません。

## 検証状況

2026-10-03：Composeの構文検証成功。公式レジストリからdigestを確認して固定。
Dockerが起動要求後に停止しており、イメージ取得・DB起動・DDL実行は未完了。SQLの実DB検証、JDBC・Mapper・API・トランザクション検証は次の段階です。
既存Mapperのupdateパラメーター不一致は未修正。

その後、ユーザーが規約へ同意したことを確認。Dockerエンジンが応答し、固定イメージの取得・両コンテナ起動・healthcheck・DDL初期化が成功しました。
Test-DbSchema.ps1でサーバ版、両テーブルの列数2・主キー1・初期行0、PostgreSQLシーケンスの整数型・開始1・増分1・未使用を確認しました。

```powershell
# このフォルダで、初回の空DB状態を再確認
./Test-DbSchema.ps1
```

このスクリプトは読み取り専用ですが、初期行0を期待するため、データ投入後の状態では失敗します。
Java/JDBCを通す実DBテストはまだ実施していません。上記の起動未完了の記述は前段階の履歴です。

その後、Java/JDBC・実Mapperを通す登録/再取得も確認済み。モジュールフォルダで `mvn -B -Pdb-integration clean verify` を実行すると、通常20件と実DB1件を検証します。
db-test/application-db-test.propertiesを明示的に読み込み、テストデータはUUIDで分離して後片付けします。
シーケンスは使用されるため、以後のTest-DbSchema.ps1の初回状態チェック（sequenceCalled=false）は失敗するのが正常です。初期状態へ戻す操作は自動実行しません。

## 根拠

- [MySQL公式イメージ](https://hub.docker.com/_/mysql)
- [PostgreSQL公式イメージ](https://hub.docker.com/_/postgres)：18系のデータマウント先は/var/lib/postgresql。
- 元コードのMapper XML・DTO・CollectUserAgentDbAccessFunction。digestの取得結果はテーマのverification/db-*-manifest.txtに保存。
