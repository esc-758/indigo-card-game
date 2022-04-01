fun solution(products: List<String>, product: String) {    
    products.forEachIndexed { index, item -> if (item == product) print("$index ") }
}
