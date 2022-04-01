class City(val name: String) {
    var degrees: Int = 0
        set(value) {
            field = if (value < -92 || value > 57 ) {
                when (name) {
                    "Dubai" -> 30
                    "Moscow" -> 5
                    "Hanoi" -> 20
                    else -> 0
                }
            } else value
        }
}

fun main() {
    val first = readLine()!!.toInt()
    val second = readLine()!!.toInt()
    val third = readLine()!!.toInt()
    val firstCity = City("Dubai")
    firstCity.degrees = first
    val secondCity = City("Moscow")
    secondCity.degrees = second
    val thirdCity = City("Hanoi")
    thirdCity.degrees = third

    val orderedCities = listOf(firstCity, secondCity, thirdCity).sortedBy { it.degrees }

    print(if (orderedCities[0].degrees == orderedCities[1].degrees) "neither" else orderedCities[0].name)
}