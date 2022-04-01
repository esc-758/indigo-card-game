fun solution(first: List<Int>, second: List<Int>): MutableList<Int> {
    val result = first.toMutableList()
    result.addAll(second)
    return result
}
