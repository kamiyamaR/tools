# 一括検証と移行結果

Windows、PowerShell 7、Git、JDK 21を使用します。ルートのMaven WrapperでMaven 3.9.6を取得します。リポジトリのルートから実行してください。

```powershell
./verification/Invoke-ToolsVerification.ps1 -JavaHome '自分のJDK21の絶対パス'
```

通常はkami-stub 27件・user-agent-collect 21件を検証します。実DBとHTTPの確認には、DockerのLinuxエンジンと[専用DB](../user-agent-collect/db-test/README.md)が必要です。

```powershell
docker compose -f ./user-agent-collect/db-test/compose.yaml up -d --wait --wait-timeout 180
./verification/Invoke-ToolsVerification.ps1 -JavaHome '自分のJDK21の絶対パス' `
  -WrapperCache 'Mavenを保存する短い絶対パス' -IncludeDb -IncludeHttp
```

両DBがHealthyでない場合は停止します。通常・実DBテストは57件、HTTP検証は19項目です。検証データは固有IDで分離・削除し、一時アプリも停止します。DBコンテナは維持し、PostgreSQLシーケンスは巻き戻しません。

WrapperCacheはMaven配布のキャッシュで、MavenRepositoryは依存ライブラリのキャッシュです。深いWindowsパスではMavenの起動に失敗する場合があるため、短いWrapperCacheを推奨します。MavenHomeを指定すると既存Mavenを使い、Wrapperによる版固定は適用しません。DockerPathも指定できます。

実行ごとのverification-runフォルダへsummary.json、Mavenログ、テストレポート、HTTP結果を保存します。キャッシュ・生成結果はGitの対象外です。Unixの直接ビルドはルートのmvnwを使用できますが、一括検証はWindows向けです。

Git所有者確認が実行権限の違いで失敗する場合、通常権限で記録し、10分以内の記録を渡せます。

```powershell
$repositoryPath = [IO.Path]::GetFullPath($PWD.Path)
git -c core.whitespace=cr-at-eol diff --check
if ($LASTEXITCODE -ne 0) { throw 'diff check failed' }
$head = git rev-parse HEAD
if ($LASTEXITCODE -ne 0) { throw 'HEAD check failed' }
$status = @(git status --short)
if ($LASTEXITCODE -ne 0) { throw 'status check failed' }
@{Repository=$repositoryPath;Head=$head;Status=$status;DiffCheckPassed=$true;
  RecordedAt=[DateTimeOffset]::UtcNow.ToString('o')} | ConvertTo-Json |
  Set-Content ./verification/git-evidence.json -Encoding utf8
# 一括検証の引数へ -GitEvidencePath ./verification/git-evidence.json を追加
```

検証中はコードを編集せず、終了後にも通常権限でdiff --checkを実行してください。

## 移行で確認したこと

Java 21・Boot 4.1.1・MyBatis Starter 4.0.0へ移行。Tomcat 11.0.26、Jackson 3.1.7、Connector/J 26.7.0を適用しました。HTTP中継のヘッダー、必須User-Agent欠落時の400応答、各DBの例外時ロールバックを修正・検証しています。

2026-10-03に別作業コピーと新規Maven・依存キャッシュで57テスト・HTTP19項目が成功しました。[結果要約](baseline-result.json)は、そのコミット前の確認記録です。同じPCと既存専用DBでの確認で、別マシン・新しいDBボリュームの検証ではありません。

二つのDBは独立したトランザクションで、片方のコミット後に他方が失敗すると不整合が残る制約をテストしています。分散トランザクションによる完全な原子性は保証しません。未使用Mapperのupdateパラメーター不一致も未修正です。

[Tomcat公式](https://tomcat.apache.org/security-11.html)、[Jackson公式](https://github.com/FasterXML/jackson-core/security/advisories/GHSA-7hhh-6rmp-j9qf)、[Connector/J公式](https://dev.mysql.com/doc/relnotes/connector-j/en/news-26-7-0.html)を確認して更新しました。OSVで解決済み依存96組を再照合して一致0件でしたが、安全性の保証ではありません。MySQLのCVE-2026-60586/60623の影響範囲9.7.0–9.7.1から外れる版へ更新した一方、各CVEと修正コミットの個別対応は未確認です。

本作業は非稼働・非公開の学習用という前提です。CI・別マシン確認、ビルドプラグイン・DBイメージの網羅的監査は未実施です。
