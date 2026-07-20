import kotlin.js.JsModule
import kotlin.js.JsNonModule

@JsModule("ag-grid-community")
@JsNonModule
external object AgGridCommunity {
    val ModuleRegistry: dynamic
    val AllCommunityModule: dynamic
    // Add other specific modules here if you want to optimize your bundle
    // val ClientSideRowModelModule: dynamic 
}