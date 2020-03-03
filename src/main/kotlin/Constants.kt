object LogColors {
    val DEBUG = "#20BB00".toColorInt()
    val INFO = "#29B7BB".toColorInt()
    val VERBOSE = "#BB7C00".toColorInt()
    val ASSERT = "#FF0600".toColorInt()
    val WARN = "#AEBB00".toColorInt()
    val ERROR = "#FF6B68".toColorInt()
    private fun String.toColorInt() = Integer.parseInt(this.replaceFirst("#", ""), 16)
}