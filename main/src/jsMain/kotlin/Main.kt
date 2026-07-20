import kotlinx.browser.document
import react.*
import react.dom.client.createRoot
import web.dom.Element

fun main() {
    AgGridCommunity.ModuleRegistry.registerModules(
        arrayOf(AgGridCommunity.AllCommunityModule)
    )

    createRoot(document.getElementById("root").unsafeCast<Element>())
        .render(Fragment.create { AppClients() })
}