<#
.SYNOPSIS
    Esporta app/src/main/assets/mint_letters.json: le tirature PER LETTERA DI ZECCA (oggi solo la
    Germania: A Berlino, D Monaco, F Stoccarda, G Karlsruhe, J Amburgo).

.DESCRIPTION
    File separato da coins.json e regular_issues.json, per scelta: sono dati Numista, quindi il piano B
    (CLAUDE.md § Backlog, "Gate da ricontrollare") è togliere questo solo file, senza toccare gli altri due
    né rifare i loro export. L'app lo legge a parte e lo unisce per chiave naturale:
      - circolanti:      (paese, taglio, anno), la stessa chiave di RegularIssueImage.tirature;
      - commemorative:   (numista_id, anno), dove numista_id è Coin.numistaId.

    Sorgenti:
      - circolanti: data/processed/numista_divisional.jsonl della pipeline, campo
        tirature_per_anno[].per_zecca (la pipeline lo scrive solo con >= 2 lettere distinte nell'anno e
        ha già controllato che la somma per lettera coincida col totale dell'anno);
      - commemorative: data/raw/numista/issues_<id>.json, perché coins_with_mintages.jsonl somma le
        lettere in un solo numero. I numista_id sono quelli di app/src/main/assets/coins.json (il file
        che l'app usa davvero, con le correzioni a mano), non quelli della pipeline.

    Classificazione delle issue delle commemorative, uguale a quella della pipeline: senza commento =
    standard; commento con "proof" = proof; commento con "BU" = bu; altro = altro (il bucket che l'app
    non mostra). Ogni totale per lettera viene sommato e CONTROLLATO contro i totali di coins.json
    (tiratura_numista_*): un disaccordo è scritto nel riepilogo.

.PARAMETER Countries
    Paesi da esportare, nel formato `paese` della pipeline. Oggi solo "Germania": l'unico con più zecche
    a lettera in contemporanea. La struttura del file non dipende dal paese.
#>
param(
    [string]$PipelineDir = (Join-Path $PSScriptRoot '..\..\euro-coins-data-pipeline'),
    [string]$CoinsJson = (Join-Path $PSScriptRoot '..\app\src\main\assets\coins.json'),
    [string]$Output = (Join-Path $PSScriptRoot '..\app\src\main\assets\mint_letters.json'),
    [string[]]$Countries = @('Germania')
)

$ErrorActionPreference = 'Stop'
$processed = Join-Path $PipelineDir 'data\processed'
$rawNumista = Join-Path $PipelineDir 'data\raw\numista'

# Le lettere ammesse per paese: una lettera fuori da qui è un errore del dato, non una zecca nuova.
$Letters = @{ 'Germania' = @('A', 'D', 'F', 'G', 'J') }

function Read-JsonArray([string]$path) {
    # ConvertFrom-Json (PowerShell 5.1) restituisce l'array intero come un solo oggetto: lo srotolo.
    Get-Content $path -Raw -Encoding UTF8 | ConvertFrom-Json | ForEach-Object { $_ }
}

function New-Share([string]$letter, $std, $bu, $proof, $altro) {
    $s = [ordered]@{ lettera = $letter }
    if ($null -ne $std) { $s['standard'] = [long]$std }
    if ($null -ne $bu) { $s['bu'] = [long]$bu }
    if ($null -ne $proof) { $s['proof'] = [long]$proof }
    if ($null -ne $altro) { $s['altro'] = [long]$altro }
    return $s
}

$warnings = New-Object System.Collections.Generic.List[string]
$circulation = New-Object System.Collections.Generic.List[object]
$commemorative = New-Object System.Collections.Generic.List[object]

# ---- Circolanti ------------------------------------------------------------------------------
$byKey = @{}
$noLetters = New-Object System.Collections.Generic.List[string]
$noData = New-Object System.Collections.Generic.List[string]
$emptyYears = New-Object System.Collections.Generic.List[string]
foreach ($rec in (Get-Content (Join-Path $processed 'numista_divisional.jsonl') -Encoding UTF8 |
        Where-Object { $_.Trim() } | ForEach-Object { $_ | ConvertFrom-Json })) {
    if ($Countries -notcontains $rec.paese) { continue }
    foreach ($y in $rec.tirature_per_anno) {
        $label = "$($rec.paese) $($rec.taglio) $($y.anno) (N#$($rec.numista_id))"
        $perZecca = $y.per_zecca
        if (-not $perZecca) {
            # La pipeline scrive per_zecca solo con >= 2 lettere: con UNA sola lettera nota il totale
            # dell'anno è di quella lettera (es. 20 cent 2007, type 2002-2007: solo la F).
            $one = @($y.zecche_lettere | Where-Object { $_ })
            if (-not $y.senza_dati -and $one.Count -eq 1) {
                $perZecca = @([pscustomobject]@{
                    lettera = $one[0]; tiratura_standard = $y.tiratura_standard; tiratura_bu = $y.tiratura_bu
                    tiratura_proof = $y.tiratura_proof; tiratura_altro = $y.tiratura_altro
                })
            } else {
                if ($y.senza_dati) { $noData.Add($label) } else { $noLetters.Add($label) }
                continue
            }
        }
        $allowed = $Letters[$rec.paese]
        $key = "$($rec.paese)|$($rec.taglio)|$($y.anno)"
        if (-not $byKey.ContainsKey($key)) {
            $byKey[$key] = @{ paese = $rec.paese; taglio = $rec.taglio; anno = [int]$y.anno; letters = @{} }
        }
        $acc = $byKey[$key].letters
        foreach ($p in $perZecca) {
            if (-not $p.lettera) {
                # Riga senza lettera (es. un "In Sets" di Numista): non si attribuisce a nessuna zecca.
                $warnings.Add("Riga senza lettera ignorata in $label (std=$($p.tiratura_standard) bu=$($p.tiratura_bu) proof=$($p.tiratura_proof) altro=$($p.tiratura_altro))")
                continue
            }
            if ($allowed -notcontains $p.lettera) {
                throw "Lettera inattesa '$($p.lettera)' in $label"
            }
            # Lo stesso (taglio, anno) può stare in DUE type Numista (cambio del lato comune nel 2007:
            # il 20 cent 2007 ha la F nel type 2002-2007 e le cinque lettere nel type 2007+): il
            # catalogo Regular somma i type, quindi si somma anche qui, altrimenti lettere e totale
            # dell'anno direbbero numeri diversi.
            if (-not $acc.ContainsKey($p.lettera)) { $acc[$p.lettera] = @{ standard = $null; bu = $null; proof = $null; altro = $null } }
            foreach ($pair in @(@('standard', $p.tiratura_standard), @('bu', $p.tiratura_bu), @('proof', $p.tiratura_proof), @('altro', $p.tiratura_altro))) {
                if ($null -ne $pair[1]) { $acc[$p.lettera][$pair[0]] = [long]$acc[$p.lettera][$pair[0]] + [long]$pair[1] }
            }
        }
    }
}
foreach ($k in ($byKey.Keys | Sort-Object)) {
    $e = $byKey[$k]
    $shares = @()
    foreach ($letter in ($e.letters.Keys | Sort-Object)) {
        $v = $e.letters[$letter]
        $shares += , (New-Share $letter $v.standard $v.bu $v.proof $v.altro)
    }
    $withNumbers = @($shares | Where-Object { $_.Count -gt 1 })
    if ($shares.Count -gt 0 -and $withNumbers.Count -eq 0) {
        $emptyYears.Add("$($e.paese) $($e.taglio) $($e.anno)")
    } elseif ($shares.Count -gt 0) {
        $circulation.Add([ordered]@{ paese = $e.paese; taglio = $e.taglio; anno = $e.anno; zecche = $shares })
    }
}

# ---- Commemorative ---------------------------------------------------------------------------
$coins = @(Read-JsonArray $CoinsJson | Where-Object { $Countries -contains $_.paese })
$unknownComments = @{}
$mismatches = New-Object System.Collections.Generic.List[string]
$withoutId = New-Object System.Collections.Generic.List[string]
foreach ($c in $coins) {
    if (-not $c.numista_id) { $withoutId.Add("$($c.anno) $($c.tema)"); continue }
    $path = Join-Path $rawNumista "issues_$($c.numista_id).json"
    if (-not (Test-Path $path)) { $warnings.Add("Manca $path ($($c.anno) $($c.tema))"); continue }
    $issues = @(Read-JsonArray $path)
    $perLetter = @{}
    foreach ($i in $issues) {
        $letter = $i.mint_letter
        if (-not $letter) { $warnings.Add("Issue senza lettera in N#$($c.numista_id) ($($c.anno) $($c.tema))"); continue }
        if ($Letters[$c.paese] -notcontains $letter) { throw "Lettera inattesa '$letter' in N#$($c.numista_id)" }
        if (-not $perLetter.ContainsKey($letter)) { $perLetter[$letter] = @{ standard = $null; bu = $null; proof = $null; altro = $null } }
        $kind = 'altro'
        if (-not $i.comment) { $kind = 'standard' }
        elseif ($i.comment -match '(?i)proof') { $kind = 'proof' }
        elseif ($i.comment -match '(?i)\bBU\b') { $kind = 'bu' }
        else { $unknownComments[$i.comment] = 1 }
        $m = [long]$i.mintage
        $perLetter[$letter][$kind] = [long]($perLetter[$letter][$kind]) + $m
    }
    $shares = @()
    foreach ($letter in ($perLetter.Keys | Sort-Object)) {
        $v = $perLetter[$letter]
        $shares += , (New-Share $letter $v.standard $v.bu $v.proof $v.altro)
    }
    # Controllo contro i totali di coins.json (la stessa fonte Numista, sommata dalla pipeline).
    foreach ($kind in 'standard', 'bu', 'proof') {
        $sum = [long]0
        foreach ($s in $shares) { if ($s.Contains($kind)) { $sum += $s[$kind] } }
        $expected = $c."tiratura_numista_$kind"
        $expectedL = if ($null -eq $expected) { [long]0 } else { [long]$expected }
        if ($sum -ne $expectedL) { $mismatches.Add("N#$($c.numista_id) $($c.anno) $($c.tema): $kind somma lettere $sum, coins.json $expectedL") }
    }
    $commemorative.Add([ordered]@{ numista_id = [int]$c.numista_id; anno = [int]$c.anno; zecche = $shares })
}

$root = [ordered]@{
    schema = 1
    fonte = 'numista'
    circolanti = $circulation
    commemorative = $commemorative
}
$json = ConvertTo-Json -InputObject $root -Compress -Depth 8
[System.IO.File]::WriteAllText($Output, $json, [System.Text.UTF8Encoding]::new($false))

# ---- Riepilogo (da leggere a ogni rilancio) --------------------------------------------------
"Scritto $Output ($([Math]::Round($json.Length / 1KB, 1)) KB)"
"  circolanti:    $($circulation.Count) combinazioni (taglio, anno) con le lettere"
$perTaglio = $circulation | Group-Object { $_.taglio } | ForEach-Object { "$($_.Name)=$($_.Count)" }
"                 per taglio: $($perTaglio -join ', ')"
"  commemorative: $($commemorative.Count) monete su $($coins.Count) di coins.json"
if ($withoutId.Count) { "  senza numista_id (nessun dato per lettera): $($withoutId -join '; ')" }
if ($noLetters.Count) { "  anni con totale ma SENZA le lettere (una sola lettera nel dato): $($noLetters -join '; ')" }
if ($noData.Count) { "  anni senza dati Numista: $($noData -join '; ')" }
if ($emptyYears.Count) { "  anni con le lettere ma senza nessun numero (non esportati): $($emptyYears -join '; ')" }
if ($unknownComments.Count) { "  commenti non classificati (finiti in 'altro'): $($unknownComments.Keys -join ' | ')" }
if ($mismatches.Count) { "  DISACCORDI con coins.json:"; $mismatches | ForEach-Object { "    $_" } } else { "  somme per lettera = totali di coins.json: tutte coincidono" }
if ($warnings.Count) { "  avvisi:"; $warnings | ForEach-Object { "    $_" } }
