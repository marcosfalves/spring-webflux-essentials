package academy.devdojo.webflux.exception

import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.server.ResponseStatusException

@Component
class CustomAttributes(): DefaultErrorAttributes() {

    override fun getErrorAttributes(request: ServerRequest, options: ErrorAttributeOptions): Map<String, Object>? {
        val errorAttributesMap: MutableMap<String, Object> = super.getErrorAttributes(request, options) as MutableMap<String, Object>
        val throwable = getError(request)
        if (throwable is ResponseStatusException) {
            errorAttributesMap["message"] = throwable.message as Object
            errorAttributesMap["developerMessage"] = "A ResponseStatusException Happened" as Object
            return errorAttributesMap
        }
        return errorAttributesMap
    }

}