@file:JsModule("react-share") // https://www.npmjs.com/package/react-share
@file:JsNonModule

import react.FC
import react.Props

@JsName("EmailIcon")
external var emailIcon: FC<IconProps>

@JsName("EmailShareButton")
external var emailShareButton: FC<ShareButtonProps>

@JsName("TelegramIcon")
external var telegramIcon: FC<IconProps>

@JsName("TelegramShareButton")
external var telegramShareButton: FC<ShareButtonProps>

@JsName("ViberShareButton")
external var viberShareButton: FC<ShareButtonProps>

@JsName("ViberIcon")
external var viberIcon: FC<IconProps>

@JsName("VKShareButton")
external var vkShareButton: FC<ShareButtonProps>

@JsName("VKIcon")
external var vkIcon: FC<IconProps>

@JsName("WhatsappShareButton")
external var whatsappShareButton: FC<ShareButtonProps>

@JsName("WhatsappIcon")
external var whatsappIcon: FC<IconProps>

external interface ShareButtonProps : react.PropsWithChildren { // Gemini
    var url: String
}

external interface IconProps : Props {
    var size: Int
    var round: Boolean
}