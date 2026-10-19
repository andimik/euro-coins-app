<#
.SYNOPSIS
    Aggiunge a app/src/main/assets/coins.json il campo `autore_retro` (+ `autore_retro_livello`) della pipeline.

.DESCRIPTION
    `autore_retro` = la persona a cui la pipeline attribuisce il lato nazionale: il designer se una fonte
    lo dichiara, altrimenti l'incisore Numista (livello "probabile"). Vedi NOTES.md della pipeline,
    § Autore del lato nazionale. Fonti ammesse: ufficiali (BCE, Commissione, Gazzetta, Banka Slovenije) e
    Numista. zwei-euro.com NON c'e' piu' nel dataset (licenza solo uso privato) e nell'app.

    Perche' NON si rifa l'export di coins.json: l'asset porta correzioni a mano che un export da
    `coins_with_mintages.jsonl` annullerebbe (tirature 2026 azzerate, note 2026 tradotte, foto
    duplicata di Monaco 2025: vedi CLAUDE.md § Cose da sapere sul dataset). Qui si cambiano SOLO i due
    campi, record per record, abbinando per (paese, anno, tema).

    Sorgente: `coins_with_mintages.jsonl` (584 monete con Numista) e `ecb_coins.jsonl` (in piu' le 29
    del 2026, che non hanno autore). Si esportano entrambi i campi per tutte le monete che li hanno; e'
    l'app a mostrare solo il livello "certo" (`CoinJson.toEntity`). Idempotente: rilanciarlo aggiorna i
    campi e toglie `disegnatore_retro`, il nome del campo della versione precedente.
#>
param(
    [string]$PipelineDir = (Join-Path $PSScriptRoot '..\..\euro-coins-data-pipeline'),
    [string]$Asset = (Join-Path $PSScriptRoot '..\app\src\main\assets\coins.json')
)

$ErrorActionPreference = 'Stop'
$processed = Join-Path $PipelineDir 'data\processed'

function Read-Jsonl([string]$name) {
    Get-Content (Join-Path $processed $name) -Encoding UTF8 |
        Where-Object { $_.Trim() } |
        ForEach-Object { $_ | ConvertFrom-Json }
}
function Key($c) { "$($c.paese)|$($c.anno)|$($c.tema)" }

$source = @{}
foreach ($c in (Read-Jsonl 'ecb_coins.jsonl')) { $source[(Key $c)] = $c }
foreach ($c in (Read-Jsonl 'coins_with_mintages.jsonl')) { $source[(Key $c)] = $c }

$coins = Get-Content $Asset -Raw -Encoding UTF8 | ConvertFrom-Json
$stats = @{}; $none = @()

$out = foreach ($coin in $coins) {
    $src = $source[(Key $coin)]
    if (-not $src) { throw "Moneta senza corrispondente nella pipeline: $(Key $coin)" }
    if (-not ($src.PSObject.Properties.Name -contains 'autore_retro')) {
        throw "Il dataset della pipeline non ha autore_retro: cartella/branch sbagliati? ($(Key $coin))"
    }

    $name = $null; $level = $null
    if (-not [string]::IsNullOrWhiteSpace($src.autore_retro)) {
        $name = $src.autore_retro.Trim(); $level = $src.autore_retro_livello
        $k = "$level / $($src.autore_retro_fonte)"
        $stats[$k] = 1 + [int]$stats[$k]
    } elseif (-not $coin.disegnatore_retro_raw) { $none += "$($coin.paese) $($coin.anno) $($coin.tema)" }

    # Stessa posizione in tutti i record: dopo disegnatore_retro_raw, senza toccare gli altri campi.
    $o = [ordered]@{}
    foreach ($p in $coin.PSObject.Properties) {
        if ($p.Name -in 'disegnatore_retro', 'autore_retro', 'autore_retro_livello') { continue }
        $o[$p.Name] = $p.Value
        if ($p.Name -eq 'disegnatore_retro_raw') { $o['autore_retro'] = $name; $o['autore_retro_livello'] = $level }
    }
    [pscustomobject]$o
}

$json = @($out) | ConvertTo-Json -Compress -Depth 5
[System.IO.File]::WriteAllText((Resolve-Path $Asset), $json, [System.Text.UTF8Encoding]::new($false))

Write-Host ("Monete: {0}; con autore: {1}; senza: {2}" -f @($out).Count, (($stats.Values | Measure-Object -Sum).Sum), $none.Count)
$stats.GetEnumerator() | Sort-Object Name | ForEach-Object { Write-Host ("  {0,-34} {1}" -f $_.Name, $_.Value) }
