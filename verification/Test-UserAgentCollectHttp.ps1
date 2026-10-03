param(
    [string]$JavaPath = 'C:\kamiyama\Amazon_Corretto\jdk21.0.1_12\bin\java.exe',
    [string]$ResultDirectoryName = 'user-agent-collect-http-smoke',
    [string]$RuntimeClasspathPath = (Join-Path $PSScriptRoot 'user-agent-collect-runtime-classpath.txt'),
    [string]$DockerPath = (Join-Path $env:LOCALAPPDATA 'Programs/DockerDesktop/resources/bin/docker.exe')
)

$ErrorActionPreference = 'Stop'
$modulePath = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../user-agent-collect'))
$composePath = Join-Path $modulePath 'db-test/compose.yaml'
$configPath = Join-Path $modulePath 'db-test/application-db-test.properties'
$jarPath = Join-Path $modulePath 'target/user-agent-collect.jar'
$dependencies = (Get-Content -LiteralPath $RuntimeClasspathPath -Raw).Trim()
foreach ($dependency in ($dependencies -split ';')) {
    if (-not (Test-Path -LiteralPath $dependency)) { throw 'Runtime dependency is missing; regenerate the classpath.' }
}
if (@(Get-ChildItem Env: | Where-Object { $_.Name -match '^(MYSQL_DB_|POSTGRESQL_DB_|SPRING_CONFIG_|SPRING_APPLICATION_JSON$|JAVA_TOOL_OPTIONS$|JDK_JAVA_OPTIONS$)' }).Count) {
    throw 'Potential configuration overrides exist; inspect them before testing.'
}
$config = Get-Content -LiteralPath $configPath -Raw
if ($config -notmatch '(?m)^mysql.db.datasource.properties.url=jdbc:mysql://127.0.0.1:13306/uac_integration\?' -or
    $config -notmatch '(?m)^postgresql.db.datasource.properties.url=jdbc:postgresql://127.0.0.1:15432/uac_integration\r?$') {
    throw 'Dedicated DB configuration does not match expected URLs.'
}
$dockerDirectory = [IO.Path]::GetDirectoryName([IO.Path]::GetFullPath($DockerPath))
$env:PATH = $dockerDirectory + ';' + $env:PATH
$docker = $DockerPath
$containerStatus = & $docker compose -f $composePath ps --format json
if ($LASTEXITCODE -ne 0) { throw 'Docker is unavailable.' }
$containers = @($containerStatus | ForEach-Object { $_ | ConvertFrom-Json })
if ($containers.Count -ne 2 -or @($containers | Where-Object { $_.State -ne 'running' -or $_.Health -ne 'healthy' }).Count) {
    throw 'Both dedicated DB containers must be healthy.'
}
$runPath = Join-Path $PSScriptRoot $ResultDirectoryName
if (Test-Path -LiteralPath $runPath) { throw 'Result directory already exists; choose a new name.' }
New-Item -ItemType Directory -Path $runPath | Out-Null
$loggingPath = Join-Path $runPath 'logback-smoke.xml'
@'
<configuration>
  <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder><charset>UTF-8</charset><pattern>%date %-5level %logger - %message%n</pattern></encoder>
  </appender>
  <logger name="tool.common.web.AfterBeanCheck" level="OFF" />
  <root level="INFO"><appender-ref ref="CONSOLE" /></root>
</configuration>
'@ | Set-Content -LiteralPath $loggingPath -Encoding utf8NoBOM
$userAgent = 'http-smoke-' + [Guid]::NewGuid().ToString()
$checks = [Collections.Generic.List[string]]::new()
$record = [ordered]@{ Success = $false; ProcessId = $null; Port = $null; Checks = @();
    CleanupSucceeded = $false; ProcessStopped = $false; Failure = $null }
$process = $null
$client = $null
$mayHaveWritten = $false

function Assert-Equal($Expected, $Actual, [string]$Description) {
    if ($Expected -cne $Actual) { throw "$Description expected=[$Expected], actual=[$Actual]" }
}
function Invoke-DbQuery([string]$Database, [string]$Sql) {
    if ($Database -eq 'mysql') {
        $output = & $docker compose -f $composePath exec -T -e MYSQL_PWD=playground_test_only mysql mysql -N -B -u uac_test -D uac_integration -e $Sql
    } else {
        $output = & $docker compose -f $composePath exec -T postgres psql -U uac_test -d uac_integration -v ON_ERROR_STOP=1 -t -A -c $Sql
    }
    if ($LASTEXITCODE -ne 0) { throw "$Database query failed." }
    return ($output | Out-String).Trim()
}
function Get-HttpResult([bool]$IncludeUserAgent, [bool]$IncludeHints) {
    $request = [Net.Http.HttpRequestMessage]::new([Net.Http.HttpMethod]::Get, ('http://127.0.0.1:' + $record.Port + '/api001'))
    try {
        if ($IncludeUserAgent) { $null = $request.Headers.TryAddWithoutValidation('User-Agent', $userAgent) }
        if ($IncludeHints) {
            foreach ($header in @{ 'Sec-CH-UA'='"Chromium";v="103"'; 'Sec-CH-UA-Mobile'='?0'; 'Sec-CH-UA-Model'='""'; 'Sec-CH-UA-Platform'='"Windows"'; 'Sec-CH-UA-Platform-Version'='"10.0.0"' }.GetEnumerator()) {
                $null = $request.Headers.TryAddWithoutValidation($header.Key, $header.Value)
            }
        }
        $response = $client.SendAsync($request).GetAwaiter().GetResult()
        try {
            return [pscustomobject]@{ Status=[int]$response.StatusCode; Body=$response.Content.ReadAsStringAsync().GetAwaiter().GetResult();
                Location=[string]$response.Headers.Location; AcceptCh=if ($response.Headers.Contains('Accept-CH')) { ($response.Headers.GetValues('Accept-CH') -join ',') } else { '' } }
        } finally { $response.Dispose() }
    } finally { $request.Dispose() }
}
try {
    $beforeCounts = @{}
    foreach ($database in @('mysql','postgres')) {
        $beforeCounts[$database] = Invoke-DbQuery $database 'SELECT COUNT(*) FROM user_agent_inf'
        Assert-Equal '0' (Invoke-DbQuery $database "SELECT COUNT(*) FROM user_agent_inf WHERE user_agent = '$userAgent'") 'Unique fixture absent'
    }
    $arguments = @('-Dfile.encoding=UTF-8', '-cp', ('"' + $jarPath + ';' + $dependencies + '"'), 'tool.Main',
        ('--spring.config.additional-location=' + ([Uri]::new($configPath)).AbsoluteUri),
        '--server.address=127.0.0.1', '--server.port=0', '--spring.main.banner-mode=off',
        ('--logging.config=' + ([Uri]::new($loggingPath)).AbsoluteUri))
    $stdout = Join-Path $runPath 'stdout.log'
    $stderr = Join-Path $runPath 'stderr.log'
    $process = Start-Process -FilePath $JavaPath -ArgumentList $arguments -WorkingDirectory $runPath -WindowStyle Hidden `
        -RedirectStandardOutput $stdout -RedirectStandardError $stderr -PassThru
    $record.ProcessId = $process.Id
    $deadline = [DateTime]::UtcNow.AddSeconds(45)
    while ([DateTime]::UtcNow -lt $deadline) {
        $process.Refresh()
        if ($process.HasExited) { throw 'Application exited before startup; inspect the saved logs.' }
        $log = Get-Content -LiteralPath $stdout -Raw -ErrorAction SilentlyContinue
        if ($log -match 'Tomcat started on port (\d+)') { $record.Port = [int]$Matches[1] }
        if ($record.Port -and $log -match 'Started Main') { break }
        Start-Sleep -Milliseconds 250
    }
    if (-not $record.Port -or $log -notmatch 'Started Main') { throw 'Application startup timed out.' }
    $handler = [Net.Http.HttpClientHandler]::new()
    $handler.AllowAutoRedirect = $false
    $handler.UseProxy = $false
    $client = [Net.Http.HttpClient]::new($handler)
    $client.Timeout = [TimeSpan]::FromSeconds(10)
    $missingUserAgent = Get-HttpResult $false $false
    Assert-Equal 400 $missingUserAgent.Status 'Missing User-Agent'
    Assert-Equal '' $missingUserAgent.Body 'Missing User-Agent response body'
    $checks.Add('Missing User-Agent: 400, empty body')
    $redirect = Get-HttpResult $true $false
    Assert-Equal 308 $redirect.Status 'Missing Client Hints'
    Assert-Equal '/api001' $redirect.Location 'Redirect Location'
    $hintNames = @($redirect.AcceptCh -split ',' | ForEach-Object { $_.Trim() } | Sort-Object)
    Assert-Equal 'Sec-CH-UA,Sec-CH-UA-Mobile,Sec-CH-UA-Model,Sec-CH-UA-Platform,Sec-CH-UA-Platform-Version' ($hintNames -join ',') 'Accept-CH'
    $checks.Add('Missing Client Hints: 308, Location, Accept-CH')
    $mayHaveWritten = $true
    $registered = Get-HttpResult $true $true
    Assert-Equal 200 $registered.Status 'Registration'
    Assert-Equal '0000' ($registered.Body | ConvertFrom-Json).detail_code 'New registration detail'
    $checks.Add('New registration: 200/0000')
    $retrieved = Get-HttpResult $true $true
    Assert-Equal 200 $retrieved.Status 'Retrieval'
    Assert-Equal '0001' ($retrieved.Body | ConvertFrom-Json).detail_code 'Existing registration detail'
    $checks.Add('Existing registration: 200/0001')
    foreach ($database in @('mysql','postgres')) {
        Assert-Equal '1' (Invoke-DbQuery $database "SELECT COUNT(*) FROM user_agent_inf WHERE user_agent = '$userAgent'") 'Single row'
    }
    $checks.Add('Exactly one target row in both dedicated DBs')
    Assert-Equal (Invoke-DbQuery 'postgres' "SELECT sequence_num FROM user_agent_inf WHERE user_agent = '$userAgent'") `
        (Invoke-DbQuery 'mysql' "SELECT sequence_num FROM user_agent_inf WHERE user_agent = '$userAgent'") 'Sequence equality'
    $checks.Add('Equal sequence numbers in both DBs')
    $record.Success = $true
} catch {
    $record.Failure = $_.Exception.Message
    throw
} finally {
    if ($client) { $client.Dispose() }
    if ($process) {
        $process.Refresh()
        if (-not $process.HasExited) { Stop-Process -Id $process.Id -ErrorAction Stop; $null = $process.WaitForExit(5000) }
        $process.Refresh()
        $record.ProcessStopped = $process.HasExited
    }
    try {
        foreach ($database in @('mysql','postgres')) {
            if ($mayHaveWritten) { $null = Invoke-DbQuery $database "DELETE FROM user_agent_inf WHERE user_agent = '$userAgent'" }
            Assert-Equal '0' (Invoke-DbQuery $database "SELECT COUNT(*) FROM user_agent_inf WHERE user_agent = '$userAgent'") 'Fixture cleanup'
            if ($beforeCounts.ContainsKey($database)) { Assert-Equal $beforeCounts[$database] (Invoke-DbQuery $database 'SELECT COUNT(*) FROM user_agent_inf') 'Preserved other rows' }
        }
        $record.CleanupSucceeded = $true
    } catch { $record.Success = $false; $record.Failure = 'Cleanup failed: ' + $_.Exception.Message; throw }
    finally {
        $record.Checks = $checks.ToArray()
        $record | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $runPath 'result.json') -Encoding utf8
    }
}
$record | ConvertTo-Json -Depth 5
