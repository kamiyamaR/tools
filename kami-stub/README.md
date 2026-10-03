# kami-stub

Spring BootでHTTPの応答を試すためのスタブアプリケーションです。
今回の移行準備では、ライブラリを更新する前にHTTP応答、静的リソース、外向きHTTP通信を検証するテストを用意しました。

## テスト環境・実行方法

- JDK 21（現在の検証環境：Amazon Corretto 21.0.12.12.1）
- Maven 3.9.6
- リポジトリ直下のMaven Wrapperで3.9.6を取得できます。ルートから`./mvnw.cmd -B -f kami-stub/pom.xml clean verify`を実行してください。以下のmvn例は既存Mavenを使う場合の手順です。
- Spring Boot 4.1.1（Java 21で検証済み）
- セキュリティ修正としてTomcat 11.0.26、Jackson BOM 3.1.7を明示指定。Boot更新時に標準管理版を確認し、上書きの必要性を再評価します。
- 初回の依存取得にはネットワーク接続が必要です。外部DBは不要です。

このモジュールのフォルダで実行します。Mavenが使用するJDKを確認してください。

```sh
mvn -version
mvn -B test
```

リポジトリのルートからは `mvn -B -f kami-stub/pom.xml test` で実行できます。

### Java 21・Spring Boot 4.1での確認

修正版適用後はJSONの正常入力・数値範囲エラーの実HTTPテスト2件を追加し、27件すべて成功。生成JARでTomcat・Jacksonの更新版を確認し、JAR起動・HTTP応答・静的リソースの検証も成功しました。以下は更新前の検証履歴です。

2026-10-03にAmazon Corretto 21.0.1とMaven 3.9.6で、Java 21向けの再コンパイル、25件のテスト、ZIPレイアウトの実行可能JAR生成が成功しました。
POMの `java.version` を21、Spring Bootを4.1.1に変更済みです。JDK 21を選択して実行します。

```sh
mvn -version
mvn -B -f kami-stub/pom.xml clean verify
```

生成したアプリクラスはJava 21形式（major version 65）です。このJARの実行にはJava 21以上が必要です。
2026-10-03にJAR自体の起動と外部設定読込みも確認済みです（下記）。
旧構成で発生した `javax.annotation.meta.When.MAYBE` のコンパイル警告は今回のビルドでは出ていません。
テスト時のByte Buddyの動的エージェント読込みに関するJDK警告は残っています。
Boot 3.5.16、4.0.8を経由し、4.1.1で全25件のテストとJAR起動13件が成功しました。
Web・AspectJ・MVCテストのスターターをBoot 4の構成へ変更し、JSON変換の拡張クラスはJackson 3対応へ移行しました。
`configureMessageConverters` は新しいServerBuilder APIへ、例外ハンドラーはHttpHeadersを使うResponseEntityコンストラクタへ置き換え、削除予定APIのコンパイル警告を解消しました。
追加したOnlineExceptionHandlerTestの2件で、独自ステータス・複数値ヘッダー・本文と、ヘッダー・本文未指定時の応答を確認しています。
Boot 4.1への更新と対象テストは完了しています。全機能・実DB、最新JDKパッチ版での検証はまだ完了していません。

### 実行可能JARと外部設定

Java 21・Boot 4.1.1の `target/kami-stub.jar` を `java -jar` で起動し、通常設定6件・外部設定7件が成功しました。
JARのMain-Classは `org.springframework.boot.loader.launch.PropertiesLauncher` です。
モジュールのフォルダ（default.txtがある場所）から、例えば次のように起動できます。

```sh
java -jar target/kami-stub.jar --server.address=127.0.0.1 --server.port=0 --server.tomcat.accesslog.enabled=false
```

割り当てられたポートは起動ログで確認します。終了する際はCtrl+Cを使ってください。
`/default` は実行時の作業フォルダにある `default.txt` を読みます。このファイルはJARに含まれていないため、JARだけを別フォルダへコピーしても同じ応答は再現できません。
名前付きの応答定義も同じ作業フォルダに配置します。

外部のpropertiesファイルを追加する場合は、起動引数 `--spring.config.additional-location=file:/絶対パス/external.properties` を指定します。
今回の検証では `server.servlet.context-path=/external-check` を外部ファイルだけで設定し、同じJARの `/external-check/default` が200、接頭辞なしの `/default` が404となることを確認しました。
この指定はJAR内の設定を維持しながら外部設定を追加します。`loader.path` による外部クラス・ライブラリ追加は今回検証していません。

繰り返し検証する場合は、PowerShell 7で [Test-KamiStubJar.ps1](../../verification/Test-KamiStubJar.ps1) を実行できます。
検証用の応答定義と外部設定を `verification/kami-stub-jar-smoke/` に用意し、127.0.0.1のランダムポートで2回起動します。
各ケース終了後、スクリプトが起動したプロセスを停止します。

## HTTP応答テスト

`DefaultControllerTest` はアプリケーション全体とTomcatを起動し、`127.0.0.1` のランダムポートへHTTPリクエストを送ります。
既存の8080ポートを使用せず、テスト完了時にテスト用JVMが終了します。
アクセスログを無効にし、Tomcatの作業ファイルを `target/test-tomcat/` に配置します。

| ケース | 確認する結果 |
|---|---|
| `/default` | 既存のdefault.txtに基づく200、Content-Type、Accept-CH、JSON本文 |
| `/default/default` | 名前指定でも同じ応答 |
| テスト用の名前付きファイル | 指定した201、独自ヘッダー、UTF-8の日本語本文 |
| status要素なし | 200と本文 |
| ファイル不在 | 500 |
| 不正なXML | 500 |

名前付きファイルはテストがモジュールの作業ディレクトリに一意な名前で作成し、各ケース終了後に削除します。
既存の `default.txt` は読み取るだけで変更しません。
ファイル不在・不正XMLのケースではアプリがエラーログを出しますが、500を返すことが期待する結果です。

この6ケースは2026-10-03の追加検証でも成功しました。

## 静的リソース・外向き通信テスト

| テスト | 件数 | 確認内容 | 2026-10-03の結果 |
|---|---:|---|---|
| StaticResourceTest | 3 | 既存静的ファイルのGET、HEAD、不在ファイルの404 | 成功 |
| RepositoryAccessorTest | 12 | Apache/Java標準の両実装でステータス、複数値ヘッダー、バイナリ応答、UTF-8のPOST/PUT、HEAD、404、応答待ちタイムアウト。Apacheの転送ヘッダー再設定も確認 | 成功 |
| RepositoryControllerTest | 2 | 実アプリの中継APIでGET/POSTの転送と応答 | 成功 |

例外ハンドラーの追加2件を含め、現在は25件すべて成功、失敗0、エラー0、スキップ0で、`mvn test` の終了コードは0です。
追加検証の初回には、受信した `Content-Length` の転送でApache HttpClientが例外を出し、中継APIの2件が500になりました。
Apache実装で `Host`、`Content-Length`、`Transfer-Encoding` を大文字・小文字を区別せず転送対象から除外し、HttpClientに設定させることで解消しました。
GET・POSTの既存期待値201は維持しています。追加のPOST・PUTテストでは、入力に異なる本文長とchunked指定があっても、実際の送信本文長が設定され、UTF-8本文と独自ヘッダーが保持されることを確認しました。

通信先はJDK内蔵HTTPサーバの `127.0.0.1` とランダムポートです。実際のMaven Centralへ接続しません。
中継APIの宛先設定はテスト内だけで上書きし、本番設定は変更しません。
タイムアウトは応答ヘッダーが届かない条件を確認しており、接続タイムアウト・応答本文受信中のタイムアウトは未検証です。
ApacheのHEAD本文はnull、Java標準のHEAD本文は空配列という既存の違いも確認しています。

`/redirect-giji.jsp` はJSPとして実行されず、`application/octet-stream` でファイル内容がそのまま配信されます。
静的リソーステストはこの現在の挙動を記録するものです。
独自の `/view/**` マッピング、TLS、バイナリのリクエスト本文、クエリ文字列の中継、他のコントローラ、実行可能JARは未検証です。
