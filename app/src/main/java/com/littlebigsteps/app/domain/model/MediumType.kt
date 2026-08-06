package com.littlebigsteps.app.domain.model

/**
 * Les médiums créatifs proposés par l'app. Voir docs/data-model.md.
 * Musique et danse sont explicitement hors scope (barrière d'accès trop haute).
 */
enum class MediumType {
    PHOTO,
    DRAWING,
    WRITING,
    CRAFT
}
