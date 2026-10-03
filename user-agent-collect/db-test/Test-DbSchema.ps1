param(
    [string]$DockerPath = (Join-Path $env:LOCALAPPDATA 'Programs/DockerDesktop/resources/bin/docker.exe'),
    [string]$ResultPath
)

$ErrorActionPreference = 'Stop'
$env:PATH = (Split-Path -Parent $DockerPath) + ';' + $env:PATH
$composeFile = Join-Path $PSScriptRoot 'compose.yaml'

$mysqlSql = @'
SELECT JSON_OBJECT(
 'version', VERSION(),
 'rows', (SELECT COUNT(*) FROM user_agent_inf),
 'columns', (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='user_agent_inf'),
 'primaryKeys', (SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema=DATABASE() AND table_name='user_agent_inf' AND constraint_type='PRIMARY KEY')
);
'@
$mysqlOutput = & $DockerPath compose -f $composeFile exec -T -e MYSQL_PWD=playground_test_only mysql mysql -h 127.0.0.1 -u uac_test -D uac_integration -N -B -e $mysqlSql
if ($LASTEXITCODE -ne 0) { throw 'Dedicated MySQL schema query failed' }
$mysqlResult = ($mysqlOutput -join "`n") | ConvertFrom-Json
if ($mysqlResult.version -ne '8.4.11' -or $mysqlResult.rows -ne 0 -or $mysqlResult.columns -ne 2 -or $mysqlResult.primaryKeys -ne 1) {
    throw 'Unexpected MySQL version or initial schema state'
}

$postgresSql = @'
SELECT json_build_object(
 'version', current_setting('server_version'),
 'rows', (SELECT COUNT(*) FROM user_agent_inf),
 'columns', (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='public' AND table_name='user_agent_inf'),
 'primaryKeys', (SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema='public' AND table_name='user_agent_inf' AND constraint_type='PRIMARY KEY'),
 'sequenceType', (SELECT data_type FROM pg_sequences WHERE schemaname='public' AND sequencename='sequence_num_create'),
 'sequenceStart', (SELECT start_value FROM pg_sequences WHERE schemaname='public' AND sequencename='sequence_num_create'),
 'sequenceIncrement', (SELECT increment_by FROM pg_sequences WHERE schemaname='public' AND sequencename='sequence_num_create'),
 'sequenceCalled', (SELECT is_called FROM sequence_num_create)
);
'@
$postgresOutput = & $DockerPath compose -f $composeFile exec -T postgres psql -U uac_test -d uac_integration -v ON_ERROR_STOP=1 -A -t -c $postgresSql
if ($LASTEXITCODE -ne 0) { throw 'Dedicated PostgreSQL schema query failed' }
$postgresResult = ($postgresOutput -join "`n") | ConvertFrom-Json
if ($postgresResult.version -notmatch '^18\.6(?:\s|$)' -or $postgresResult.rows -ne 0 -or $postgresResult.columns -ne 2 -or $postgresResult.primaryKeys -ne 1 -or $postgresResult.sequenceType -ne 'integer' -or $postgresResult.sequenceStart -ne 1 -or $postgresResult.sequenceIncrement -ne 1 -or $postgresResult.sequenceCalled) {
    throw 'Unexpected PostgreSQL version or initial schema state'
}

$result = [ordered]@{ checkedAt = (Get-Date -Format 'yyyy-MM-ddTHH:mm:ssK'); mysql = $mysqlResult; postgres = $postgresResult; status = 'passed' }
$resultJson = $result | ConvertTo-Json -Depth 4
if ($ResultPath) { $resultJson | Set-Content -LiteralPath $ResultPath -Encoding utf8 }
Write-Output $resultJson
