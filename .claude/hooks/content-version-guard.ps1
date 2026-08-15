# PostToolUse — garde-fou sur /content.
#
# Deux invariants de CLAUDE.md §3 et §7 qu'aucune compilation ne peut attraper,
# parce qu'ils ne se manifestent qu'a l'execution, sur un appareil deja
# installe, sous la forme d'une mise a jour qui n'arrive jamais :
#
#   1. Modifier content/<locale>/<medium>.json sans incrementer la version
#      correspondante dans content/<locale>/manifest.json (ou, pour packs.json,
#      son propre champ "version") : ContentSyncRepository.syncRemoteContent
#      compare les versions et conclut "deja a jour" — le nouveau contenu n'est
#      jamais telecharge.
#   2. Laisser diverger les numeros de version entre fr et en : les deux
#      catalogues doivent porter les memes numeros (invariant §7), sinon un
#      changement de langue resynchronise de travers.
#
# La comparaison se fait contre HEAD : tant que la version du working tree est
# identique a celle du dernier commit alors que le contenu a change, on alerte.
# Le hook redevient silencieux des que la version est incrementee.
#
# Sortie : code 2 + message sur stderr = l'agent lit l'alerte et corrige.

$ErrorActionPreference = 'Stop'

function Read-JsonFile($fullPath) {
    if (-not (Test-Path -LiteralPath $fullPath)) { return $null }
    try { return (Get-Content -Raw -LiteralPath $fullPath | ConvertFrom-Json) } catch { return $null }
}

function Read-JsonAtHead($repo, $relPath) {
    $text = & git -C $repo show "HEAD:$relPath" 2>$null
    if ($LASTEXITCODE -ne 0 -or -not $text) { return $null }
    try { return (($text -join "`n") | ConvertFrom-Json) } catch { return $null }
}

function Get-MediumEntry($manifest, $fileName) {
    if (-not $manifest -or -not $manifest.mediums) { return $null }
    return ($manifest.mediums | Where-Object { $_.file -eq $fileName } | Select-Object -First 1)
}

try {
    $raw = [Console]::In.ReadToEnd()
    if (-not $raw) { exit 0 }
    $payload = $raw | ConvertFrom-Json

    $filePath = $payload.tool_input.file_path
    if (-not $filePath) { exit 0 }

    $normalized = $filePath -replace '\\', '/'
    if ($normalized -notmatch '/content/(fr|en)/([A-Za-z0-9_-]+)\.json$') { exit 0 }

    $locale   = $Matches[1]
    $baseName = $Matches[2]
    $fileName = "$baseName.json"

    # .claude/hooks/x.ps1 -> .claude -> racine du depot
    $repo = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)

    $warnings = @()

    if ($baseName -eq 'packs') {
        $current = Read-JsonFile (Join-Path $repo "content/$locale/packs.json")
        $head    = Read-JsonAtHead $repo "content/$locale/packs.json"
        if ($head -and $current -and $current.version -eq $head.version) {
            $warnings += "content/$locale/packs.json a change mais son champ ""version"" est reste a ""$($current.version)"". ContentSyncRepository.syncPacksRemoteIfNeeded compare ce champ : sans increment, les packs ne seront pas retelecharges."
        }
    }
    elseif ($baseName -ne 'manifest') {
        $currentManifest = Read-JsonFile (Join-Path $repo "content/$locale/manifest.json")
        $headManifest    = Read-JsonAtHead $repo "content/$locale/manifest.json"
        $currentEntry    = Get-MediumEntry $currentManifest $fileName
        $headEntry       = Get-MediumEntry $headManifest $fileName

        if (-not $currentEntry) {
            $warnings += "content/$locale/$fileName n'est reference par aucune entree de content/$locale/manifest.json : la synchro ignorera ce fichier."
        }
        elseif ($headEntry -and $currentEntry.version -eq $headEntry.version) {
            $warnings += "content/$locale/$fileName a change mais sa version dans content/$locale/manifest.json est restee a ""$($currentEntry.version)"". La synchro conclura ""deja a jour"" et ne retelechargera rien (CLAUDE.md, section 3)."
        }
    }

    # Parite fr/en — invariant §7, verifie apres toute modification de /content.
    $frManifest = Read-JsonFile (Join-Path $repo 'content/fr/manifest.json')
    $enManifest = Read-JsonFile (Join-Path $repo 'content/en/manifest.json')
    if ($frManifest -and $enManifest) {
        foreach ($entry in $frManifest.mediums) {
            $counterpart = Get-MediumEntry $enManifest $entry.file
            if (-not $counterpart) {
                $warnings += "Le medium ""$($entry.id)"" existe dans content/fr/manifest.json mais pas dans content/en/manifest.json."
            }
            elseif ($counterpart.version -ne $entry.version) {
                $warnings += "Versions divergentes pour ""$($entry.id)"" : fr=$($entry.version), en=$($counterpart.version). Les deux catalogues doivent porter le meme numero (CLAUDE.md, section 7)."
            }
        }
    }

    $frPacks = Read-JsonFile (Join-Path $repo 'content/fr/packs.json')
    $enPacks = Read-JsonFile (Join-Path $repo 'content/en/packs.json')
    if ($frPacks -and $enPacks -and $frPacks.version -ne $enPacks.version) {
        $warnings += "Versions de packs.json divergentes : fr=$($frPacks.version), en=$($enPacks.version)."
    }

    if ($warnings.Count -gt 0) {
        [Console]::Error.WriteLine("Garde-fou /content :")
        foreach ($w in $warnings) { [Console]::Error.WriteLine("  - $w") }
        exit 2
    }

    exit 0
}
catch {
    # Un garde-fou casse ne doit jamais bloquer le travail.
    exit 0
}
