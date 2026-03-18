package com.barberflow.core.pagination

data class Page<T>(
    val content: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int
) {
    companion object {
        fun <T> of(content: List<T>, pageNumber: Int, pageSize: Int, totalElements: Long): Page<T> {
            val totalPages = if (pageSize == 0) 1 else Math.ceil(totalElements.toDouble() / pageSize).toInt()
            return Page(content, pageNumber, pageSize, totalElements, totalPages)
        }

        fun <T> empty(pageSize: Int = 20): Page<T> =
            Page(emptyList(), 0, pageSize, 0L, 0)
    }

    fun hasNext(): Boolean = pageNumber < totalPages - 1
    fun hasPrevious(): Boolean = pageNumber > 0
}
