package app.district.data

enum class PracticeCategory(val id: String, val label: String) {
    MATH("math", "Math"),
    QUANT("quant", "Quant"),
    LOGIC("logic", "Logic"),
    ENGLISH("english", "English"),
    GK("gk", "General knowledge");

    companion object {
        fun fromId(id: String?): PracticeCategory =
            entries.firstOrNull { it.id == id } ?: MATH
    }
}
