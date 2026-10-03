param(
    [Parameter(Mandatory)][string]$JavaHome,
    [string]$MavenHome,
    [string]$WrapperCache = (Join-Path $PSScriptRoot 'maven-wrapper-cache'),
    [string]$MavenRepository = (Join-Path $PSScriptRoot 'maven-repository'),
    [string]$DockerPath = (Join-Path $env:LOCALAPPDATA 'Programs/DockerDesktop/resources/bin/docker.exe'),
    [string]$GitEvidencePath,
    [switch]$IncludeDb,
    [switch]$IncludeHttp
)

$ErrorActionPreference = 'Stop'
if ($IncludeHttp -and -not $IncludeDb) { throw 'IncludeHttp requires IncludeDb for the dedicated databases.' }
$java = Join-Path ([IO.Path]::GetFullPath($JavaHome)) 'bin/java.exe'
$repository = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$maven = if ($MavenHome) { Join-Path ([IO.Path]::GetFullPath($MavenHome)) 'bin/mvn.cmd' } else { Join-Path $repository 'mvnw.cmd' }
foreach ($tool in @($java, $maven)) { if (-not (Test-Path -LiteralPath $tool)) { throw "Tool not found: $tool" } }
$MavenRepository = [IO.Path]::GetFullPath($MavenRepository)
$runName = 'verification-run-' + [DateTime]::Now.ToString('yyyyMMdd-HHmmss') + '-' + [Guid]::NewGuid().ToString('N').Substring(0,8)
$runPath = Join-Path $PSScriptRoot $runName
New-Item -ItemType Directory -Path $runPath | Out-Null
$savedEnvironment = @{}
foreach ($name in @('JAVA_HOME','MAVEN_SKIP_RC','MAVEN_BASEDIR','MAVEN_OPTS','PATH','MAVEN_USER_HOME')) {
    $savedEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, 'Process')
}
$steps = [Collections.Generic.List[object]]::new()
$record = [ordered]@{ Success=$false; JavaVersion=$null; MavenVersion=$null; JavaHome=$JavaHome; MavenHome=$MavenHome;
    MavenRepository=$MavenRepository; MavenCommand=$maven; WrapperCache=$WrapperCache; IncludeDb=[bool]$IncludeDb; IncludeHttp=[bool]$IncludeHttp;
    GitHead=$null; GitStatus=@(); GitEvidenceSource=$GitEvidencePath; Databases=@(); Steps=@(); Failure=$null }

function Invoke-MavenStep([string]$Name, [string[]]$Arguments) {
    $logPath = Join-Path $runPath ($Name + '.log')
    & $maven -B "-Dmaven.repo.local=$MavenRepository" -l $logPath @Arguments
    $stepExit = $LASTEXITCODE
    $steps.Add([pscustomobject]@{ Name=$Name; ExitCode=$stepExit; Arguments=$Arguments; Log=$logPath })
    if ($stepExit -ne 0) { throw "Maven step failed: $Name. See $logPath" }
}
function Save-TestReports([string]$Module) {
    $destination = Join-Path $runPath ($Module + '-reports')
    New-Item -ItemType Directory -Path $destination | Out-Null
    $count=0; $failures=0; $errors=0; $skipped=0
    foreach ($kind in @('surefire-reports','failsafe-reports')) {
        $source = Join-Path $repository "$Module/target/$kind"
        if (Test-Path -LiteralPath $source) {
            Copy-Item -LiteralPath $source -Destination $destination -Recurse
            foreach ($reportFile in (Get-ChildItem -LiteralPath $source -Filter 'TEST-*.xml')) {
                [xml]$report = Get-Content -LiteralPath $reportFile.FullName -Raw
                $count += [int]$report.testsuite.tests
                $failures += [int]$report.testsuite.failures
                $errors += [int]$report.testsuite.errors
                $skipped += [int]$report.testsuite.skipped
            }
        }
    }
    $steps.Add([pscustomobject]@{ Name=($Module+'-tests'); Tests=$count; Failures=$failures; Errors=$errors; Skipped=$skipped })
    if ($count -eq 0 -or $failures -or $errors) { throw "No successful test reports for $Module" }
}
try {
    $env:JAVA_HOME = [IO.Path]::GetFullPath($JavaHome)
    $env:MAVEN_SKIP_RC = '1'
    $env:MAVEN_BASEDIR = $repository
    if (-not $MavenHome) { $env:MAVEN_USER_HOME = [IO.Path]::GetFullPath($WrapperCache) }
    $env:MAVEN_OPTS = $savedEnvironment['MAVEN_OPTS'] + ' -Dfile.encoding=UTF-8'
    $env:PATH = (Join-Path $JavaHome 'bin') + ';' + $savedEnvironment['PATH']
    $record.JavaVersion = (& $java -version 2>&1 | Out-String).Trim()
    if ($LASTEXITCODE -ne 0 -or $record.JavaVersion -notmatch 'version "21[.\"]') { throw 'Java 21 is required.' }
    $record.MavenVersion = (& $maven -version 2>&1 | Out-String).Trim()
    if ($LASTEXITCODE -ne 0) { throw 'Maven version check failed.' }
    if ($GitEvidencePath) {
        $gitEvidence = Get-Content -LiteralPath $GitEvidencePath -Raw | ConvertFrom-Json
        if ($gitEvidence.Repository -ne $repository -or -not $gitEvidence.DiffCheckPassed -or
            [DateTimeOffset]::UtcNow - [DateTimeOffset]::Parse($gitEvidence.RecordedAt) -gt [TimeSpan]::FromMinutes(10)) {
            throw 'Git evidence must be for this checkout, pass diff checking, and be less than 10 minutes old.'
        }
        $record.GitHead = $gitEvidence.Head
        $record.GitStatus = @($gitEvidence.Status)
    } else {
        $record.GitHead = (& git -C $repository rev-parse HEAD | Out-String).Trim()
        if ($LASTEXITCODE -ne 0) { throw 'Git checkout is unavailable.' }
        $record.GitStatus = @(& git -C $repository status --short)
        if ($LASTEXITCODE -ne 0) { throw 'Git status failed.' }
    }
    if ($IncludeDb) {
        $env:PATH = [IO.Path]::GetDirectoryName([IO.Path]::GetFullPath($DockerPath)) + ';' + $env:PATH
        $dbStatus = & $DockerPath compose -f (Join-Path $repository 'user-agent-collect/db-test/compose.yaml') ps --format json
        if ($LASTEXITCODE -ne 0) { throw 'Docker is unavailable. Start it separately.' }
        $containers = @($dbStatus | ForEach-Object { $_ | ConvertFrom-Json })
        if ($containers.Count -ne 2 -or @($containers | Where-Object { $_.State -ne 'running' -or $_.Health -ne 'healthy' }).Count) {
            throw 'Start both dedicated DB containers and wait for Healthy.'
        }
        $record.Databases = @($containers | Select-Object Service,Image,ID,Health)
    }
    Push-Location $repository
    try {
        Invoke-MavenStep 'kami-stub-verify' @('-f','kami-stub/pom.xml','clean','verify')
        Save-TestReports 'kami-stub'
        $userAgentArguments = @('-f','user-agent-collect/pom.xml')
        if ($IncludeDb) { $userAgentArguments += '-Pdb-integration' }
        Invoke-MavenStep 'user-agent-collect-verify' ($userAgentArguments + @('clean','verify'))
        Save-TestReports 'user-agent-collect'
        if ($IncludeHttp) {
            $classpathPath = Join-Path $runPath 'runtime-classpath.txt'
            Invoke-MavenStep 'runtime-classpath' @('-f','user-agent-collect/pom.xml',
                'org.apache.maven.plugins:maven-dependency-plugin:3.6.1:build-classpath','-DincludeScope=runtime',"-Dmdep.outputFile=$classpathPath")
            $null = & (Join-Path $PSScriptRoot 'Test-KamiStubJar.ps1') -JavaPath $java -ResultDirectoryName ($runName+'/kami-stub-http')
            $kamiResults = @(Get-Content -LiteralPath (Join-Path $runPath 'kami-stub-http/results.json') -Raw | ConvertFrom-Json)
            if (@($kamiResults | Where-Object { -not $_.Success -or -not $_.Stopped }).Count) { throw 'kami-stub HTTP verification failed.' }
            $steps.Add([pscustomobject]@{ Name='kami-stub-http'; Checks=($kamiResults | Measure-Object PassedChecks -Sum).Sum; Success=$true })
            $null = & (Join-Path $PSScriptRoot 'Test-UserAgentCollectHttp.ps1') -JavaPath $java -DockerPath $DockerPath `
                -RuntimeClasspathPath $classpathPath -ResultDirectoryName ($runName+'/user-agent-collect-http')
            $httpResult = Get-Content -LiteralPath (Join-Path $runPath 'user-agent-collect-http/result.json') -Raw | ConvertFrom-Json
            if (-not $httpResult.Success -or -not $httpResult.CleanupSucceeded -or -not $httpResult.ProcessStopped) { throw 'user-agent-collect HTTP verification failed.' }
            $steps.Add([pscustomobject]@{ Name='user-agent-collect-http'; Checks=$httpResult.Checks.Count; Success=$true })
        }
        if (-not $GitEvidencePath) {
            & git -c core.whitespace=cr-at-eol -C $repository diff --check
            if ($LASTEXITCODE -ne 0) { throw 'git diff --check failed.' }
        }
    } finally { Pop-Location }
    $record.Success = $true
} catch { $record.Failure = $_.Exception.Message; throw }
finally {
    $record.Steps = $steps.ToArray()
    $record | ConvertTo-Json -Depth 7 | Set-Content -LiteralPath (Join-Path $runPath 'summary.json') -Encoding utf8
    foreach ($name in $savedEnvironment.Keys) { [Environment]::SetEnvironmentVariable($name,$savedEnvironment[$name],'Process') }
}
Write-Output ('Verification succeeded. Results: ' + $runPath)
$steps | Format-Table -AutoSize
