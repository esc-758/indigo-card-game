class EspressoMachine {
    val costPerServing: Float
    
    constructor(coffeeCapsulesCount: Int, totalCost: Float) {
        costPerServing = totalCost / coffeeCapsulesCount
    }
    
    constructor(coffeeBeansWeight: Float, totalCost: Float) {
        val cupsOfCoffee: Float = coffeeBeansWeight / 10

        costPerServing = totalCost / cupsOfCoffee
    }
}
