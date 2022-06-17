package io.ducket.api.clients.rates

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class DataSetDto(
    @field:JacksonXmlProperty(localName = "Series")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val references: List<ReferenceDto>,
)
