import kotlinx.serialization.Serializable

const val HTTP = "http"
const val QUERY_BD = "QUERY_BD"
const val JSON_PG = "jsonPG"
const val JSON_PG_UPD = "jsonPGupd"
const val ROUTE_BD = "bd"
const val PASSW = "passw"
const val TRYING_TO_SAVE = "Trying to save... "
const val TRYING_TO_FETCH = "Trying to fetch... "
const val ERROR_IN_FETCH = "ERROR in fetch FOR "
const val UPDATE_BASE_SUCCESS = "Update Success !!!"
const val UPDATE_BASE_ERROR = "Update Error !!!"
const val NOT_FOUND_QUERY_NAME = "NOT fount query name"
const val CONSTRUCTION = "Construction"
const val HIDE = "hide"
const val SHOW = "show"
const val NOT_JSON_CHAR = "" // early "\n{}\"';,:=\t\b\n\\#&"
//const val REPLACE_STR = "^^%d@@"
const val QUOTE_ONE = "'"
const val QUOTE_THREE = "\"\"\""
const val LOADENV = "loadEnv"
const val JSR223 = "jsr223"
const val SUBMENU = "submenu"
const val VIEW_QUERY = "v_querys"
const val FIRSTQUERY = "q"
const val ENDPOINT_SERVICE_URL = "b"
const val VIEW_QUERY_NAME = "v"
const val VIEW_QUERY_WHERE = "w"
const val VIEW_QUERY_WHERE_DEFAULT = " and left(name,2)!='--'"
const val ROW_FIELDS_VALUE_PARAM = "rFV"
const val URI_FOR_QUERY = " Uri for query OR name query: "
const val TITLES_FOR_QUERY = " Title for query"
const val ADMIN_PASSW = "Admin passw"
const val SAVE_TO_SERVER = " 'SAVE to server ??'"
const val EDIT_YES = "edit_yes"
const val EDIT_NO = "edit_no"
const val ERROR_FIELD = "\"error\""
const val NEW_TAB = "*"
const val GIT_VERSION = "git.version"
const val SQL_STATE_RETRY = "08003,08006,57P01"  // terminating connection
const val DATE_FROM = "@dateFrom"
const val DATE_TO = "@dateTo"
const val DATE_MIN = "2015-01-01"
const val DATE_MAX = "2027-12-31"
const val OPEN_IN_NEW_TAB = "Open in New Tab"
const val SERVER_TYPE = "https"
const val LOAD_TESTING_KEYS = "LOAD_TESTING_KEYS" // delimiter is ';'
const val META_SECURITY = "<meta http-equiv='Content-Security-Policy' content='upgrade-insecure-requests'>"
var startEnv = mutableMapOf<String, String>()
var rowFieldsValueMap = mutableMapOf<String, String>()

@Serializable
data class UpdateData(val rowsUpd: List<String>, val rowsDel: List<String>, val rowsIns: List<String>,val fieldsValue:Map<String, String>)

fun decodeJson(inString: String) = inString
//fun decodeJson(inString: String, replacedChar: String = NOT_JSON_CHAR) = inString
//    run {
//        var rez = inString
//        replacedChar.indices.forEach {
//            rez = rez.replace(REPLACE_STR.replace("%d", it.toString()), replacedChar[it].toString())
//        }
//        rez
//    }

fun encodeJson(inString: String) = inString
//fun encodeJson(inString: String, replacedChar: String = NOT_JSON_CHAR) = inString
//    run {
//        var rez = inString
//        replacedChar.indices.forEach {
//            rez = rez.replace(replacedChar[it].toString(), REPLACE_STR.replace("%d", it.toString()))
//        }
//        rez
//    }

fun insertParameterValue(queryIn: String, fieldsValueMap: Map<String, String> = rowFieldsValueMap) = run {
    var query = queryIn
    if (queryIn.contains("{")) { // {
        fieldsValueMap.keys
            .ifEmpty { null }
            ?.forEach { key ->
                query = query
                    .replace("{$key}", fieldsValueMap[key].toString())
            }
    }
    query
}

fun nameValue(nameAndValue: String, delimiter: String = ":") =
    NameValue (
        nameAndValue.substring(0,nameAndValue.indexOf(delimiter)).trim(),
        nameAndValue.substring(nameAndValue.indexOf(delimiter) + 1).trim()
    )
data class NameValue (val name: String, val value: String)