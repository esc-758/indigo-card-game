fun solution(numbers: List<Int>, number: Int): MutableList<Int> {
    val mutableNumbers = numbers.toMutableList()
    mutableNumbers.add(number)
    return mutableNumbers
}
