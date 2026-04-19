param(
    [string]$Source = "$PSScriptRoot\InputMethodBlocker-Natives-x64.cpp",
    [string]$Output = "$PSScriptRoot\..\..\src\main\resources\InputMethodBlocker-Natives-x64.dll"
)

$ErrorActionPreference = "Stop"

if (-not $env:JAVA_HOME) {
    throw "JAVA_HOME must be set before building the native DLL."
}

$javaInclude = Join-Path $env:JAVA_HOME "include"
$javaWin32Include = Join-Path $javaInclude "win32"

if (-not (Test-Path $Source)) {
    throw "Native source file not found: $Source"
}

$outputDir = Split-Path -Parent $Output
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

function Invoke-MingwBuild {
    $compiler = Get-Command x86_64-w64-mingw32-g++.exe -ErrorAction SilentlyContinue
    if ($null -eq $compiler) {
        $compiler = Get-Command g++.exe -ErrorAction SilentlyContinue
    }
    if ($null -eq $compiler) {
        return $false
    }

    & $compiler.Source `
        -shared `
        -O2 `
        -std=gnu++17 `
        -static-libgcc `
        -static-libstdc++ `
        "-I$javaInclude" `
        "-I$javaWin32Include" `
        $Source `
        -o $Output `
        -limm32 `
        -luser32

    if ($LASTEXITCODE -ne 0) {
        throw "MinGW native build failed with exit code $LASTEXITCODE"
    }

    return $true
}

function Invoke-MsvcBuild {
    $vswhere = Join-Path ${env:ProgramFiles(x86)} "Microsoft Visual Studio\Installer\vswhere.exe"
    if (-not (Test-Path $vswhere)) {
        return $false
    }

    $installationPath = & $vswhere -latest -products * -property installationPath
    if (-not $installationPath) {
        return $false
    }

    $vcvars = Join-Path $installationPath "VC\Auxiliary\Build\vcvars64.bat"
    if (-not (Test-Path $vcvars)) {
        return $false
    }

    $compileCommand = 'cl.exe /nologo /LD /EHsc /O2 /I"{0}" /I"{1}" "{2}" /link /OUT:"{3}" imm32.lib user32.lib' -f `
        $javaInclude, $javaWin32Include, $Source, $Output
    $command = 'call "' + $vcvars + '" && ' + $compileCommand

    cmd.exe /c $command

    if ($LASTEXITCODE -ne 0) {
        throw "MSVC native build failed with exit code $LASTEXITCODE"
    }

    return $true
}

$built = Invoke-MingwBuild
if (-not $built) {
    $built = Invoke-MsvcBuild
}

if (-not $built) {
    throw "No usable native compiler was found. Install MinGW-w64 or Visual Studio C++ Build Tools."
}

if (-not (Test-Path $Output)) {
    throw "Native build reported success but no DLL was produced: $Output"
}

Write-Host "Built native DLL: $Output"
