package com.club.poc.core.model

data class LodgeSummary(
    val id: String,
    val name: String,
    val bedCount: Int,
)

data class ClubDocumentSummary(
    val id: String,
    val title: String,
    val category: String,
    val url: String,
)
