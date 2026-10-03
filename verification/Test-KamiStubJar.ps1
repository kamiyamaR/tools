param(
    [string]$JavaPath = 'C:\kamiyama\Amazon_Corretto\jdk21.0.1_12\bin\java.exe',
    [string]$ResultDirectoryName = 'kami-stub-jar-smoke'
)

$ErrorActionPreference = 'Stop'
$verificationRoot = $PSScriptRoot
$modulePath = [System.IO.Path]::GetFullPath((Join-Path $verificationRoot '../kami-stub'))
$jarPath = Join-Path $modulePath 'target/kami-stub.jar'
$runRoot = Join-Path $verificationRoot $ResultDirectoryName
New-Item -ItemType Directory -Path $runRoot -Force | Out-Null
$loggingPath = Join-Path $runRoot 'logback-smoke.xml'
@'
<configuration>
  <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder><charset>UTF-8</charset><pattern>%date %-5level %logger - %message%n</pattern></encoder>
  </appender>
  <logger name="stub.controller.close" level="OFF" />
  <root level="INFO"><appender-ref ref="CONSOLE" /></root>
</configuration>
'@ | Set-Content -LiteralPath $loggingPath -Encoding utf8
$externalConfigPath = Join-Path $runRoot 'external.properties'
@'
server.servlet.context-path=/external-check
spring.main.banner-mode=off
'@ | Set-Content -LiteralPath $externalConfigPath -Encoding utf8

function Assert-Equal($Expected, $Actual, [string]$Description) {
    if ($Expected -cne $Actual) {
        throw "$Description : expected=[$Expected], actual=[$Actual]"
    }
}

function Get-Response([System.Net.Http.HttpClient]$Client, [string]$Url, [string]$Method = 'GET') {
    $request = [System.Net.Http.HttpRequestMessage]::new([System.Net.Http.HttpMethod]::new($Method), $Url)
    try {
        return $Client.SendAsync($request).GetAwaiter().GetResult()
    } finally {
        $request.Dispose()
    }
}

$results = [System.Collections.Generic.List[object]]::new()
foreach ($mode in @('baseline', 'external-config')) {
    $workPath = Join-Path $runRoot $mode
    New-Item -ItemType Directory -Path $workPath -Force | Out-Null
    Copy-Item -LiteralPath (Join-Path $modulePath 'default.txt') -Destination (Join-Path $workPath 'default.txt')
    @'
<?xml version="1.0" encoding="UTF-8"?>
<response>
  <status>201</status>
  <headers>
    <header><name>Content-Type</name><value>text/plain;charset=UTF-8</value></header>
    <header><name>X-Jar-Smoke</name><value>external-response-file</value></header>
  </headers>
  <body>JARからの日本語応答</body>
</response>
'@ | Set-Content -LiteralPath (Join-Path $workPath 'jar-response.txt') -Encoding utf8NoBOM
    $stdoutPath = Join-Path $workPath 'stdout.log'
    $stderrPath = Join-Path $workPath 'stderr.log'
    $arguments = @('-Dfile.encoding=UTF-8', '-jar', ('"' + $jarPath + '"'),
        '--server.address=127.0.0.1', '--server.port=0', '--server.tomcat.accesslog.enabled=false',
        '--server.tomcat.basedir=./tomcat', '--logging.level.web=INFO',
        ('--logging.config=' + ([Uri]::new($loggingPath)).AbsoluteUri))
    $contextPath = ''
    if ($mode -eq 'external-config') {
        $arguments += '--spring.config.additional-location=' + ([Uri]::new($externalConfigPath)).AbsoluteUri
        $contextPath = '/external-check'
    }
    $appProcess = $null
    $client = $null
    $record = [ordered]@{ Mode = $mode; ProcessId = $null; Port = $null; ContextPath = $contextPath;
        PassedChecks = 0; Stopped = $false; Success = $false }
    try {
        $startOptions = @{ FilePath = $JavaPath; ArgumentList = $arguments; WorkingDirectory = $workPath;
            WindowStyle = 'Hidden'; RedirectStandardOutput = $stdoutPath; RedirectStandardError = $stderrPath;
            PassThru = $true }
        $appProcess = Start-Process @startOptions
        $record.ProcessId = $appProcess.Id
        $deadline = [DateTime]::UtcNow.AddSeconds(45)
        while ([DateTime]::UtcNow -lt $deadline) {
            $appProcess.Refresh()
            if ($appProcess.HasExited) {
                throw "JAR exited before startup. See $stdoutPath and $stderrPath"
            }
            $log = Get-Content -LiteralPath $stdoutPath -Raw -ErrorAction SilentlyContinue
            if ($log -match 'Tomcat started on port (\d+)') {
                $record.Port = [int]$Matches[1]
                if ($log -match 'Started StubStart') { break }
            }
            Start-Sleep -Milliseconds 200
        }
        if ($null -eq $record.Port -or $log -notmatch 'Started StubStart') {
            throw "Startup did not complete in 45 seconds. See $stdoutPath"
        }
        $handler = [System.Net.Http.HttpClientHandler]::new()
        $handler.AllowAutoRedirect = $false
        $handler.UseProxy = $false
        $client = [System.Net.Http.HttpClient]::new($handler)
        $client.Timeout = [TimeSpan]::FromSeconds(10)
        $baseUrl = 'http://127.0.0.1:' + $record.Port + $contextPath
        $expectedXml = [xml](Get-Content -LiteralPath (Join-Path $workPath 'default.txt') -Raw)
        foreach ($path in @('/default', '/default/default')) {
            $response = Get-Response $client ($baseUrl + $path)
            try {
                Assert-Equal 200 ([int]$response.StatusCode) $path
                Assert-Equal 'application/json' $response.Content.Headers.ContentType.ToString() 'Content-Type'
                Assert-Equal $expectedXml.response.body.InnerText.Trim() $response.Content.ReadAsStringAsync().Result.Trim() 'JSON body'
                $acceptCh = ($response.Headers.GetValues('Accept-CH') -join ', ')
                Assert-Equal 'Sec-CH-UA, Sec-CH-UA-Mobile, Sec-CH-UA-Model, Sec-CH-UA-Platform, Sec-CH-UA-Platform-Version, Sec-CH-UA-Full-Version-List' $acceptCh 'Accept-CH'
                $record.PassedChecks++
            } finally { $response.Dispose() }
        }
        $response = Get-Response $client ($baseUrl + '/default/jar-response')
        try {
            Assert-Equal 201 ([int]$response.StatusCode) 'Named response status'
            Assert-Equal 'JARからの日本語応答' $response.Content.ReadAsStringAsync().Result 'Named UTF-8 body'
            Assert-Equal 'external-response-file' ($response.Headers.GetValues('X-Jar-Smoke') -join '') 'Named response header'
            $record.PassedChecks++
        } finally { $response.Dispose() }
        $response = Get-Response $client ($baseUrl + '/redirect-giji.jsp')
        try {
            Assert-Equal 200 ([int]$response.StatusCode) 'Static GET'
            $expectedBytes = [System.IO.File]::ReadAllBytes((Join-Path $modulePath 'src/main/resources/static/redirect-giji.jsp'))
            $actualBytes = $response.Content.ReadAsByteArrayAsync().Result
            Assert-Equal ([Convert]::ToBase64String($expectedBytes)) ([Convert]::ToBase64String($actualBytes)) 'Static bytes'
            $record.PassedChecks++
        } finally { $response.Dispose() }
        $response = Get-Response $client ($baseUrl + '/redirect-giji.jsp') 'HEAD'
        try {
            Assert-Equal 200 ([int]$response.StatusCode) 'Static HEAD'
            Assert-Equal 0 $response.Content.ReadAsByteArrayAsync().Result.Length 'HEAD body'
            Assert-Equal $expectedBytes.Length $response.Content.Headers.ContentLength 'HEAD length'
            $record.PassedChecks++
        } finally { $response.Dispose() }
        $response = Get-Response $client ($baseUrl + '/missing-jar-smoke-resource.txt')
        try {
            Assert-Equal 404 ([int]$response.StatusCode) 'Missing resource'
            $record.PassedChecks++
        } finally { $response.Dispose() }
        if ($mode -eq 'external-config') {
            $response = Get-Response $client ('http://127.0.0.1:' + $record.Port + '/default')
            try {
                Assert-Equal 404 ([int]$response.StatusCode) 'Old context path is not active'
                $record.PassedChecks++
            } finally { $response.Dispose() }
        }
        $record.Success = $true
    } finally {
        if ($null -ne $client) { $client.Dispose() }
        if ($null -ne $appProcess) {
            $appProcess.Refresh()
            if (-not $appProcess.HasExited) { Stop-Process -Id $appProcess.Id }
            if (-not $appProcess.WaitForExit(10000)) { throw "Could not stop PID $($appProcess.Id)" }
            $record.Stopped = $true
            $appProcess.Dispose()
        }
        $results.Add([pscustomobject]$record)
        $results | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $runRoot 'results.json') -Encoding utf8
    }
}
$results | Format-Table -AutoSize
