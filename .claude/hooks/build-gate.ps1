# PreToolUse — garde-fou sur la compilation.
#
# CLAUDE.md §0 : "Compilation / APK : jamais de sa propre initiative —
# l'utilisateur donne le feu vert." Cette regle ne tenait que par la discipline
# de l'agent. Elle devient ici une demande d'autorisation explicite : toute
# commande contenant "gradlew" declenche une confirmation, que le feu vert
# transforme en un clic.
#
# Le filtrage porte sur la sous-chaine et non sur le prefixe de la commande :
# la commande de reference commence par JAVA_HOME=..., un prefixe ne
# l'attraperait pas.
#
# Un emulateur lance depuis l'agent tombe sous la meme regle (§0).

$ErrorActionPreference = 'Stop'

try {
    $raw = [Console]::In.ReadToEnd()
    if (-not $raw) { exit 0 }
    $payload = $raw | ConvertFrom-Json

    $command = $payload.tool_input.command
    if (-not $command) { exit 0 }

    $reason = $null
    if ($command -match 'gradlew') {
        $reason = "Compilation Gradle. CLAUDE.md section 0 : l'agent ne compile jamais de sa propre initiative, cette execution demande ton feu vert."
    }
    elseif ($command -match 'emulator(\.exe)?\s' -or $command -match 'avdmanager') {
        $reason = "Lancement d'emulateur. CLAUDE.md section 0 : l'agent ne demarre jamais un emulateur de sa propre initiative."
    }

    if ($reason) {
        $out = @{
            hookSpecificOutput = @{
                hookEventName          = 'PreToolUse'
                permissionDecision     = 'ask'
                permissionDecisionReason = $reason
            }
        } | ConvertTo-Json -Depth 5 -Compress
        Write-Output $out
    }

    exit 0
}
catch {
    exit 0
}
