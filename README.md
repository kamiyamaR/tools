# tools

Javaの学習用プロジェクトです。kami-stubはHTTP応答を試すスタブ、user-agent-collectはUser-Agentを二つのDBへ記録するアプリです。

## 共通のビルド環境

全テスト・実DB・HTTPをまとめて確認する[一括検証手順](verification/README.md)も収録しています。

JDK 21とMaven Wrapperを使用します。Mavenを別途インストールする必要はありません。リポジトリ直下のmvnw.cmd（Windows）またはmvnw（Unix）で、固定したMaven 3.9.6を初回取得します。Wrapper 3.3.4のonly-script形式で、配布ZIPのSHA256を.mvn/wrapper/maven-wrapper.propertiesに固定しています。

Windows PowerShellで、JDK 21の場所をJAVA_HOMEへ指定して実行します。

```powershell
$env:JAVA_HOME = '自分のJDK21の絶対パス'
./mvnw.cmd -version
./mvnw.cmd -B -f kami-stub/pom.xml clean verify
./mvnw.cmd -B -f user-agent-collect/pom.xml clean verify
```

Unixでは`sh ./mvnw -B -f kami-stub/pom.xml clean verify`のように実行できます。Windowsで検証済みで、Unixでの動作は未検証です。

ルートに集約用POMはありません。対象モジュールを-fで指定してください。kami-stubは27件、user-agent-collectの通常テストは21件で、通常実行に外部DBは不要です。

実DB検証は[user-agent-collectの専用DB手順](user-agent-collect/db-test/README.md)を参照し、コンテナ2台がHealthyになってから`./mvnw.cmd -B -f user-agent-collect/pom.xml -Pdb-integration clean verify`を実行します。通常21件に実DB9件が追加されます。

初回はMavenと依存ライブラリの取得にネットワークが必要です。WrapperのMaven保存先は既定でユーザーの.m2配下です。テーマ内だけに置く場合はMAVEN_USER_HOMEを専用フォルダへ指定します。Mavenの依存キャッシュは別途-Dmaven.repo.localで指定できます。Javaの版・DB環境・ネットワーク設定はWrapperだけでは固定されません。

更新・実行の詳細は[kami-stub](kami-stub/README.md)と[user-agent-collect](user-agent-collect/README.md)を参照してください。
