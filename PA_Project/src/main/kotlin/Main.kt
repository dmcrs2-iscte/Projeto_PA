fun main() {
    val jsonGenerator = JSONGenerator()
    val mediator = Mediator()
    jsonGenerator.addObserver(mediator)

    val ui = UI()
    mediator.addObserver(ui)

    ui.open()
}